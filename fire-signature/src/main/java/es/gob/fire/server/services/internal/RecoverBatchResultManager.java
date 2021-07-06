/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.SignOperation;
import es.gob.fire.server.services.crypto.CryptoHelper;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;
import es.gob.fire.signature.ConfigManager;


/**
 * Manejador que gestiona las peticiones para la recuperaci&oacute;n del resultado de la firma
 * de un lote de firma.
 */
public class RecoverBatchResultManager {

	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();
	private static final Logger LOGGER = Logger.getLogger(RecoverBatchResultManager.class.getName());

	/**
	 * Finaliza un proceso de firma y devuelve el resultado del mismo.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverResult(final RequestParameters params, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String appId = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

        // Recuperamos el resto de parametros de la sesion
        FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, false);
        if (session == null) {
    		LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode());
        	return;
        }

		// Si la operacion anterior no fue el inicio de una firma, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_PRE != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true);
		}

        // Comprobamos que no se haya declarado ya un error
        if (session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {
        	final String errMessage = session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
        	LOGGER.warning(logF.f("Ocurrio un error durante la operacion de firma de lote: " + errMessage)); //$NON-NLS-1$
        	TRANSLOGGER.register(session, false);
        	SessionCollector.cleanSession(session);
        	response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode(), HttpCustomErrors.INVALID_TRANSACTION.getErrorDescription());
        	return;
        }


        // En el caso de firma con un certificado en la nube, todavia tendremos que
        // componer la propia firma
        final String tdB64			= session.getString(ServiceParams.SESSION_PARAM_TRIPHASE_DATA);
        final String certB64		= session.getString(ServiceParams.SESSION_PARAM_CERT);
        final String signOperation	= session.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION);
        final String algorithm		= session.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
        final String format			= session.getString(ServiceParams.SESSION_PARAM_FORMAT);
        final Properties extraParams = (Properties) session.getObject(ServiceParams.SESSION_PARAM_EXTRA_PARAM);
        final String defaultUpgrade	= session.getString(ServiceParams.SESSION_PARAM_UPGRADE);

        final Properties upgradeConfig	= (Properties) session.getObject(ServiceParams.SESSION_PARAM_UPGRADE_CONFIG);

        final String remoteTrId		= session.getString(ServiceParams.SESSION_PARAM_REMOTE_TRANSACTION_ID);

        // Decodificamos el certificado en caso de que se nos indique (en las firmas de
        // lote con certificado local podria no indicarse).
        X509Certificate signingCert = null;
        if (certB64 != null && !certB64.isEmpty()) {
        	try {
        		signingCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
        				new ByteArrayInputStream(Base64.decode(certB64, true))
        				);
        	}
        	catch (final Exception e) {
        		LOGGER.severe(logF.f("No se ha podido decodificar el certificado del firmante: " + e)); //$NON-NLS-1$
        		TRANSLOGGER.register(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        				"No se ha podido decodificar el certificado proporcionado: " + e); //$NON-NLS-1$
        		return;
        	}
        }

        // Obtenemos si se debe detener el proceso en caso de error
        final boolean stopOnError = Boolean.parseBoolean(
        		session.getString(ServiceParams.SESSION_PARAM_BATCH_STOP_ON_ERROR));

    	// Obtenemos si se debe detener el proceso en caso de error
        final FIReDocumentManager docManager = (FIReDocumentManager) session.getObject(
        		ServiceParams.SESSION_PARAM_DOCUMENT_MANAGER);

        // Recuperamos el objeto con el estado actual de cada firma
        final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
    	if (batchResult == null || batchResult.documentsCount() == 0) {
    		LOGGER.severe(logF.f("No encontraron firmas en el lote")); //$NON-NLS-1$
    		TRANSLOGGER.register(session, false);
    		SessionCollector.removeSession(session);
    		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No encontraron firmas en el lote"); //$NON-NLS-1$
    		return;
    	}
    	batchResult.setSigningCertificate(signingCert);
    	final String origin	= session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
    	batchResult.setProviderName(origin);

    	// En el caso de la firma con certificado local, ya se habra realizado la firma completa
        // del lote y actualizado su estado en la sesion, por lo que solo registrar que esta firmado y devolverlo
    	if (ProviderManager.PROVIDER_NAME_LOCAL.equals(origin)) {

        	LOGGER.info(logF.f("Se firmo con certificado local y ya se dispone de las firmas del lote")); //$NON-NLS-1$

        	// Si se pidio que se detuviese la operacion en caso de error y se
        	// encuentra alguno, entonces no sera necesario actualizar las firmas
        	boolean needPostProcess = true;
        	if (stopOnError) {
        		final Iterator<String> it = batchResult.iterator();
        		// Si detectamos un error y se tienen que abortar la operacion, evitamos que se manden
        		// mas firmas a actualizar
        		while (needPostProcess && it.hasNext()) {
        			final String docId = it.next();
        			if (batchResult.isSignFailed(docId)) {
        				needPostProcess = false;
        			}
        		}
        	}

        	if (needPostProcess) {

        		final SignBatchConfig defaultSignConfig = new SignBatchConfig();
        		defaultSignConfig.setCryptoOperation(signOperation);
        		defaultSignConfig.setFormat(format);
        		defaultSignConfig.setExtraParams(extraParams);
        		defaultSignConfig.setUpgrade(defaultUpgrade);
        		defaultSignConfig.setUpgradeConfig(upgradeConfig);

        		LOGGER.info(logF.f("Se actualizan las firmas que lo necesitan")); //$NON-NLS-1$
        		upgradeLocalSignatures(appId, transactionId, batchResult, defaultSignConfig,
        				docManager, session, stopOnError);
        	}
        }

        // Firma en la nube
        else {

        	LOGGER.info(logF.f(String.format("Se firmo con el proveedor %3s y es necesario recuperar el PKCS#1 de las firmas para completar el trabajo", origin))); //$NON-NLS-1$

        	final TransactionConfig connConfig	=
        			(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
        	if (connConfig == null) {
        		LOGGER.warning(logF.f("No se proporcionaron datos para la conexion con el backend")); //$NON-NLS-1$
        		TRANSLOGGER.register(session, false);
        		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        				"No se proporcionaron datos para la conexion con el backend"); //$NON-NLS-1$
        		return;
        	}

        	// Obtenemos el conector con el backend ya configurado
        	final FIReConnector connector;
        	try {
        		connector = ProviderManager.getProviderConnector(origin, connConfig.getProperties());
        	}
        	catch (final FIReConnectorFactoryException e) {
        		LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", origin), e); //$NON-NLS-1$
        		TRANSLOGGER.register(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        		return;
        	}

        	// Si llegamos hasta aqui usando correctamente el API, es que no se produjo ningun
        	// error en la plataforma de autorizacion, asi que podemos eliminar el valor bandera
        	// que nos indicaba que habiamos sido redirigidos para evitar confundir posibles
        	// errores futuros con esta misma transaccion.
        	session.removeAttribute(ServiceParams.SESSION_PARAM_REDIRECTED);

        	LOGGER.info(logF.f("Se solicita el PKCS#1 al proveedor " + origin)); //$NON-NLS-1$

        	final Map<String, byte[]> ret;
        	try {
        		ret = connector.sign(remoteTrId);
        	}
        	catch(final FIReConnectorUnknownUserException e) {
    			LOGGER.log(Level.SEVERE, logF.f("El usuario no esta dado de alta en el sistema"), e); //$NON-NLS-1$
    			TRANSLOGGER.register(session, false);
                SessionCollector.removeSession(session);
    			response.sendError(HttpCustomErrors.NO_USER.getErrorCode());
        		return;
        	}
        	catch(final FIReConnectorNetworkException e) {
    			LOGGER.log(Level.SEVERE, logF.f("No se pudo conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
    			TRANSLOGGER.register(session, false);
    			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, origin);
                SessionCollector.removeSession(session);
    			response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, "No se ha podido conectar con el sistema: " + e); //$NON-NLS-1$
        		return;
        	}
        	catch(final Exception e) {
        		LOGGER.log(Level.SEVERE, logF.f("Ocurrio un error durante la operacion de firma de lote en la nube"), e); //$NON-NLS-1$
        		TRANSLOGGER.register(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpCustomErrors.SIGN_ERROR.getErrorCode());
        		return;
        	}

        	// Verificamos cada uno de los PKCS#1 generados
    		final Set<String> keys = ret.keySet();
    		for (final String key : keys) {

    			final byte[] pkcs1 = ret.get(key);
    			try {
    				CryptoHelper.verifyPkcs1(pkcs1, signingCert.getPublicKey(), logF);
    			}
    			catch (final Exception e) {
            		LOGGER.log(Level.SEVERE, logF.f("Error de integridad. Uno de los PKCS#1 recibido no se genero con el certificado indicado"), e); //$NON-NLS-1$
            		TRANSLOGGER.register(session, false);
            		SessionCollector.removeSession(session);
            		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            				"Error de integridad. Uno de los PKCS#1 recibido no se genero con el certificado indicado: " + e); //$NON-NLS-1$
            		return;
    			}
    		}

        	final TriphaseData td;
        	try {
        		td = TriphaseData.parser(Base64.decode(tdB64, true));
        	}
        	catch (final Exception e) {
        		LOGGER.log(Level.SEVERE, logF.f("Error de codificacion en los datos de firma trifasica proporcionados"), e); //$NON-NLS-1$
        		TRANSLOGGER.register(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        				"Error de codificacion en los datos de firma trifasica proporcionados: " + e); //$NON-NLS-1$
        		return;
        	}

        	// Si hay que detener el proceso en caso de error, comprobamos si
        	// ha ocurrido alguno ya y marcamos todas las operaciones que ya
        	// estaban iniciadas como abortadas
        	if (stopOnError) {
        		Iterator<String> it = batchResult.iterator();
        		while (it.hasNext()) {
        			if (batchResult.isSignFailed(it.next())) {
        				it = batchResult.iterator();
        				while (it.hasNext()) {
        					final String docId = it.next();
        					if (!batchResult.isSignFailed(docId)) {
        						batchResult.setErrorResult(docId, BatchResult.ABORTED);
        					}
        				}
        			}
        		}
        	}

    		// Obtenemos si se ha usado un proveedor seguro de firma
        	final boolean secureProvider = ConfigManager.isSecureProvider(origin);

        	// Para cada uno de los documentos del lote, cargamos el propio documento,
        	// obtenemos la informacion de firma trifasica y realizamos la postfirma.
        	// La firma generada se almacena en lugar del documento y se compone un XML
        	// con la informacion del resultado de cada firma.
        	final List<ConcurrentProcessThread> threads = new ArrayList<>();
        	final Iterator<String> it = batchResult.iterator();
        	while (it.hasNext()) {

        		final String docId = it.next();
        		if (batchResult.isSignFailed(docId)) {
        			continue;
        		}

        		// Usamos la configuracion del lote por defecto o la del documento si esta establecida
        		SignBatchConfig signConfig = batchResult.getSignConfig(docId);
        		if (signConfig == null) {
        			signConfig = new SignBatchConfig();
        			signConfig.setCryptoOperation(signOperation);
        			signConfig.setFormat(format);
        			signConfig.setExtraParams(extraParams);
        			signConfig.setUpgrade(defaultUpgrade);
        		}
        		// La configuracion para la plataforma de actualizacion siempre es la por defecto
        		// para todos los elementos del lote
        		signConfig.setUpgradeConfig(upgradeConfig);

        		// Verificamos que es necesaria la validacion segun el tipo de firma a realizar (las cofirmas
        		// y contrafirmas deben validarse porque no sabemos de la validez de firmas anteriores).
        		final boolean signValidationNeeded = needValidation(
        				secureProvider, signConfig.getCryptoOperation(), signConfig.getFormat());
        		// Configuramos el objeto para la composicion de las firmas
    			final PostSignBatchRecover signRecover = new CloudPostSignBatchRecover(
    					docId, algorithm, signConfig, ret, td, batchResult, logF);
    			// Ejecutamos un hilo encargado de componer las firmas y actualizarlas
    			final ConcurrentProcessThread t = new PostSignBatchThread(
    					appId, transactionId, docId, batchResult, signConfig,
    					signValidationNeeded, docManager, signRecover);

        		threads.add(t);
        		t.start();
        	}

        	// Esperamos a que terminen todos los hilos y los
        	// interrumpimos todos si detectamos que alguno de
        	// ellos fue interrumpido y no se admiten errores parciales
        	ConcurrentProcessThread.waitThreads(threads, stopOnError,
        			session, ServiceParams.SESSION_PARAM_BATCH_PENDING_SIGNS);
        }

    	LOGGER.info(logF.f("Devolvemos el resultado del lote")); //$NON-NLS-1$

        // Si todas las firmas fallaron, damos por terminada la transaccion y eliminamos la sesion.
        if (isAllFailed(batchResult)) {
    		TRANSLOGGER.register(session, false);
        	SessionCollector.removeSession(session);
        }
        // Si no, indicamos que ya se ha firmado el lote para permitir que se puedan recuperar los
        // resultados particulares de cada una de las firmas
        else {
            session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_SIGNED, Boolean.TRUE.toString());
            session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
            session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_RECOVER);

            // Registramos el resultado de la operacion y los errores de las firmas. Los exitos
            // en las firmas no se registran hasta que se recuperen con &eacute;xito.
            registryResults(batchResult, session, logF);
            SessionCollector.commit(session);
        }

        // Enviamos el XML resultado de la firma del lote
        sendResult(response, batchResult.encode());
	}

	/**
	 * Elimina los temporales que no se necesiten ya, se registra el exito de la transaci&oacute;n
	 * y los errores en las firmas para las estad&iacute;sticas.
	 * @param batchResult Resultado de la firma del lote.
	 * @param session Sesi&oacute;n con la informaci&oacute;n de la transacci&oacute;n.
	 * @throws IOException
	 */
	private static void registryResults(final BatchResult batchResult, final FireSession session,
			final LogTransactionFormatter logF)
			throws IOException {
        final Iterator<String> it = batchResult.iterator();
    	while (it.hasNext()) {
    		final String docId = it.next();
    		final boolean failed = batchResult.isSignFailed(docId);
    		if (failed || batchResult.getGracePeriod(docId) != null) {
    			final String docRef = batchResult.getDocumentReference(docId);
    			try {
    				TempDocumentsManager.deleteDocument(docRef);
    			}
    			catch (final Exception e) {
    				LOGGER.log(Level.WARNING, logF.f("No se pudo eliminar el temporal del documento: " + docRef), e); //$NON-NLS-1$
				}
    		}
    		if (failed) {
    			SIGNLOGGER.register(session, false, docId);
    		}
    	}
        TRANSLOGGER.register(session, true);
	}

	/**
	 * Actualiza las firmas del resultado que finalizaron correctamente y para
	 * las que haya configurado un formato de actualizaci&oacute;n, ya se a nivel
	 * de lote o de la propia firma.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param trId Identificador de transacci&oacute;n.
	 * @param batchResult Listado con los resultados parciales de cada firma.
	 * @param defaultConfig Configuraci&oacute;n por defecto en caso de no tener una espec&iacute;fica.
	 * @param stopOnError Ser&aacute; {@code true} si debe pararse el proceso tras encontrar un error,
	 * {@code false} en caso contrario.
	 * @param session Sesi&oacute;n en la que se ir&aacute; almacenando el numero de peticiones pendientes
	 * en cada momento para su consulta en paralelo.
	 */
	private static void upgradeLocalSignatures(final String appId, final String trId,
			final BatchResult batchResult, final SignBatchConfig defaultSignConfig,
			final FIReDocumentManager docManager, final FireSession session,
			final boolean stopOnError) {

		final List<ConcurrentProcessThread> threads = new ArrayList<>();
    	final Iterator<String> it = batchResult.iterator();
    	while (it.hasNext()) {

    		final String docId = it.next();

    		final SignBatchConfig signConfig = batchResult.getSignConfig(docId) != null ?
    				batchResult.getSignConfig(docId) : defaultSignConfig;

    		if (docManager != null || session.getString(ServiceParams.SESSION_PARAM_UPGRADE) != null ||
    				signConfig != null && signConfig.getUpgrade() != null) {

        		// La configuracion para la plataforma de actualizacion siempre es la por defecto
        		// para todos los elementos del lote
    			signConfig.setUpgradeConfig(defaultSignConfig.getUpgradeConfig());

    			final PostSignBatchRecover signRecover = new ClienteAfirmaPostSignBatchRecover(
    					docId, batchResult);
    			final ConcurrentProcessThread t = new PostSignBatchThread(
    					appId, trId, docId, batchResult, signConfig, true,
    					docManager, signRecover);
    			threads.add(t);
    			t.start();
    		}
    	}

        // Esperamos a que terminen todos los hilos y los
        // interrumpimos todos si detectamos que alguno de
        // ellos fue interrumpido y no se admiten errores parciales
        ConcurrentProcessThread.waitThreads(threads, stopOnError,
        		session, ServiceParams.SESSION_PARAM_BATCH_PENDING_SIGNS);
	}

	/**
	 * Envia el XML resultado de la operaci&oacute;n como respuesta del servicio.
	 * @param response Respuesta del servicio.
	 * @param result Resultado de la operaci&oacute;n.
	 * @throws IOException Cuando falla el env&iacute;o.
	 */
	private static void sendResult(final HttpServletResponse response, final byte[] result) throws IOException {
		try (final OutputStream output = ((ServletResponse) response).getOutputStream();) {
			output.write(result);
			output.flush();
		}
	}

	/**
	 * Comprueba si todas las firmas de un lote son err&oacute;neas.
	 * @param batchResult Informaci&oacute;n del lote.
	 * @return {@code true} si todas las firmas notifican alg&uacute;n error,
	 * {@code false} en caso contrario.
	 */
	private static boolean isAllFailed(final BatchResult batchResult) {
        boolean recovered = true;
        final Iterator<String> it = batchResult.iterator();
        while (it.hasNext() && recovered) {
        	if (!batchResult.isSignFailed(it.next())) {
        		recovered = false;
        	}
        }
        return recovered;
	}

	/**
	 * Realiza las comprobaciones necesarias para identificar si es necesario validar una firma
	 * cuando se ha solicitado que se haga. Si este m&eacute;todo devuelve {@code false} se
	 * ignorar&aacute;n las peticiones de validaci&oacute;n.
	 * @return {@code true} si es necesario validar las firmas que se soliciten, {@code false} en
	 * caso contrario.
	 */
	static boolean needValidation(final boolean secureProvider, final String signOperation, final String signFormat) {
		try {
			return !secureProvider ||
					SignOperation.parse(signOperation) != SignOperation.SIGN ||
					AOSignConstants.SIGN_FORMAT_PADES.equals(signFormat);
		}
		catch (final Exception e) {
			LOGGER.warning("No se pudo comprobar si la firma era apta para validacion: " + e); //$NON-NLS-1$
			return true;
		}
	}
}
