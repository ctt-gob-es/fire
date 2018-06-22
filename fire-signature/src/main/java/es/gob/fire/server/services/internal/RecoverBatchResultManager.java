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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.ServiceUtil;
import es.gob.fire.server.services.statistics.FireSignLogger;
import es.gob.fire.server.services.statistics.SignatureLogger;
import es.gob.fire.server.services.statistics.TransactionLogger;


/**
 * Manejador que gestiona las peticiones para la recuperaci&oacute;n del resultado de la firma
 * de un lote de firma.
 */
public class RecoverBatchResultManager {

	private static Logger LOGGER =  FireSignLogger.getFireSignLogger().getFireLogger().getLogger();
	private static final SignatureLogger SIGNLOGGER = SignatureLogger.getSignatureLogger();
	private static final TransactionLogger TRANSLOGGER = TransactionLogger.getTransactLogger();
//	private static final Logger LOGGER = Logger.getLogger(RecoverBatchResultManager.class.getName());

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

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        LOGGER.fine(String.format("TrId %1s: RecoverBatchManager", transactionId)); //$NON-NLS-1$

        // Recuperamos el resto de parametros de la sesion
        FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, false);
        if (session == null) {
    		LOGGER.warning("La transaccion no se ha inicializado o ha caducado"); //$NON-NLS-1$
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
        	LOGGER.warning("Ocurrio un error durante la operacion de firma de lote: " + errMessage); //$NON-NLS-1$
        	SIGNLOGGER.log(session, false);
        	TRANSLOGGER.log(session, false);
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
        final String extraParamsB64	= session.getString(ServiceParams.SESSION_PARAM_EXTRA_PARAM);
        final String defaultUpgrade	= session.getString(ServiceParams.SESSION_PARAM_UPGRADE);

        final String remoteTrId		= session.getString(ServiceParams.SESSION_PARAM_REMOTE_TRANSACTION_ID);

        // Decodificamos el certificado en caso de que se nos indique (en las firmas de
        // lote con certificado local podria no indicarse).
        X509Certificate signingCert = null;
        if (certB64 != null) {
        	try {
        		signingCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
        				new ByteArrayInputStream(Base64.decode(certB64, true))
        				);
        	}
        	catch (final Exception e) {
        		LOGGER.severe("No se ha podido decodificar el certificado del firmante: " + e); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		TRANSLOGGER.log(session, false);
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

        // En el caso de la firma con certificado local, ya se habra realizado la firma completa
        // del lote y actualizado su estado en la sesion, por lo que solo registrar que esta firmado y devolverlo
        BatchResult batchResult;
        final String origin	= session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
        if (ServiceParams.CERTIFICATE_ORIGIN_LOCAL.equals(origin)) {

            // Recuperamos el objeto con el estado actual de cada firma (aun no procesadas)
        	batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
        	if (batchResult == null || batchResult.documentsCount() == 0) {
        		LOGGER.severe("No encontraron firmas en el lote"); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		TRANSLOGGER.log(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No encontraron firmas en el lote"); //$NON-NLS-1$
        		return;
    		}

        	batchResult.setSigningCertificate(signingCert);
        	batchResult.setProviderName(origin);

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
        		upgradeLocalSignatures(appId, batchResult, docManager, session, stopOnError);
        	}


        }

        // Firma en la nube
        else {
        	final TransactionConfig connConfig	=
        			(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
        	if (connConfig == null) {
        		LOGGER.warning("No se proporcionaron datos para la conexion con el backend"); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        				"No se proporcionaron datos para la conexion con el backend"); //$NON-NLS-1$
        		return;
        	}

        	// Obtenemos el conector con el backend ya configurado
        	final FIReConnector connector;
        	try {
        		connector = ProviderManager.initTransacction(origin, connConfig.getProperties());
        	}
        	catch (final FIReConnectorFactoryException e) {
        		LOGGER.log(Level.SEVERE, "Error en la configuracion del conector del servicio de custodia", e); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		TRANSLOGGER.log(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        		return;
        	}

        	// Si llegamos hasta aqui usando correctamente el API, es que no se produjo ningun
        	// error en la plataforma de autorizacion, asi que podemos eliminar el valor bandera
        	// que nos indicaba que habiamos sido redirigidos para evitar confundir posibles
        	// errores futuros con esta misma transaccion.
        	session.removeAttribute(ServiceParams.SESSION_PARAM_REDIRECTED);

        	final Map<String, byte[]> ret;
        	try {
        		ret = connector.sign(remoteTrId);
        	}
        	catch(final FIReConnectorUnknownUserException e) {
    			LOGGER.log(Level.SEVERE, "El usuario no esta dado de alta en el sistema", e); //$NON-NLS-1$
    			SIGNLOGGER.log(session, false);
    			TRANSLOGGER.log(session, false);
                SessionCollector.removeSession(session);
    			response.sendError(HttpCustomErrors.NO_USER.getErrorCode());
        		return;
        	}
        	catch(final Exception e) {
        		LOGGER.log(Level.SEVERE, "Ocurrio un error durante la operacion de firma", e); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		TRANSLOGGER.log(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpCustomErrors.SIGN_ERROR.getErrorCode());
        		return;
        	}


        	final X509Certificate signerCert;
        	try {
        		signerCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
        				new ByteArrayInputStream(Base64.decode(certB64, true))
        				);
        	}
        	catch (final Exception e) {
        		LOGGER.severe("No se ha podido decodificar el certificado del firmante: " + e); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		TRANSLOGGER.log(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        				"No se ha podido decodificar el certificado proporcionado: " + e); //$NON-NLS-1$
        		return;
        	}

        	final Properties extraParams;
        	try {
        		extraParams = ServiceUtil.base642Properties(extraParamsB64);
        	}
        	catch (final Exception e) {
        		LOGGER.severe("Parametros extra de configuracion de la firma mal formatos: " + e); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		TRANSLOGGER.log(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        				"Parametros extra de configuracion de la firma mal formatos: " + e); //$NON-NLS-1$
        		return;
        	}

        	final TriphaseData td;
        	try {
        		td = TriphaseData.parser(Base64.decode(tdB64, true));
        	}
        	catch (final Exception e) {
        		LOGGER.log(Level.SEVERE, "Error de codificacion en los datos de firma trifasica proporcionados", e); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		TRANSLOGGER.log(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        				"Error de codificacion en los datos de firma trifasica proporcionados: " + e //$NON-NLS-1$
        				);
        		return;
        	}

        	// Recuperamos el objeto con el estado actual de cada firma
        	batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
        	if (batchResult == null || batchResult.documentsCount() == 0) {
        		LOGGER.severe("No encontraron firmas en el lote"); //$NON-NLS-1$
        		SIGNLOGGER.log(session, false);
        		TRANSLOGGER.log(session, false);
        		SessionCollector.removeSession(session);
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No encontraron firmas en el lote"); //$NON-NLS-1$
        		return;
        	}

        	batchResult.setProviderName(origin);

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

        	// Para cada uno de los documentos del lote, cargamos el propio documento,
        	// obtenemos la informacion de firma trifasica y realizamos la postfirma.
        	// La firma generada se almacena en lugar del documento y se compone un XML
        	// con la informacion del resultado de cada firma.
        	final Iterator<String> it = batchResult.iterator();
        	final List<ConcurrentProcessThread> threads = new ArrayList<>();
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

        		final FIRePostSignaturesThread t = new FIRePostSignaturesThread(appId, docId, batchResult,
        				algorithm, signConfig, signerCert, ret, td, docManager,session);
        		threads.add(t);
        		t.start();
        	}

        	// Esperamos a que terminen todos los hilos y los
        	// interrumpimos todos si detectamos que alguno de
        	// ellos fue interrumpido y no se admiten errores parciales
        	ConcurrentProcessThread.waitThreads(threads, stopOnError,
        			session, ServiceParams.SESSION_PARAM_BATCH_PENDING_SIGNS);

        	// Notificamos al conector que ha terminado la operacion para que libere recursos y
        	// cierre la transaccion
        	connector.endSign(remoteTrId);
        }

        // Si todas las firmas fallaron, damos por terminada la transaccion y eliminamos la sesion.
        if (isAllFailed(batchResult)) {
        	SIGNLOGGER.log(session, false);
    		TRANSLOGGER.log(session, false);
        	SessionCollector.removeSession(session);
        }
        // Si no, indicamos que ya se ha firmado el lote para permitir que se puedan recuperar los
        // resultados particulares de cada una de las firmas
        else {
            session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_SIGNED, Boolean.TRUE.toString());
            session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
            session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_RECOVER);
            SessionCollector.commit(session);
        }

        //Se registra que la transac&oacute;n a sido correcta
        TRANSLOGGER.log(session, true);

        // Enviamos el XML resultado de la firma del lote
        sendResult(response, batchResult.encode());
	}

	/**
	 * Actualiza las firmas del resultado que finalizaron correctamente y para
	 * las que haya configurado un formato de actualizaci&oacute;n, ya se a nivel
	 * de lote o de la propia firma.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param batchResult Listado con los resultados parciales de cada firma.
	 * @param defaultConfig Configuraci&oacute;n por defecto en caso de no tener una espec&iacute;fica.
	 * @param stopOnError Ser&aacute; {@code true} si debe pararse el proceso tras encontrar un error,
	 * {@code false} en caso contrario.
	 * @param session Sesi&oacute;n en la que se ir&aacute; almacenando el numero de peticiones pendientes
	 * en cada momento para su consulta en paralelo.
	 */
	private static void upgradeLocalSignatures(final String appId, final BatchResult batchResult,
			final FIReDocumentManager docManager, final FireSession session,
			final boolean stopOnError) {

		final List<ConcurrentProcessThread> threads = new ArrayList<>();
    	final Iterator<String> it = batchResult.iterator();
    	while (it.hasNext()) {

    		final String docId = it.next();
    		if (docManager != null || session.getString(ServiceParams.SESSION_PARAM_UPGRADE) != null ||
    				batchResult.getSignConfig(docId) != null &&
    				batchResult.getSignConfig(docId).getUpgrade() != null) {
    			final ConcurrentProcessThread t = new ClienteAFirmaUpdateSignaturesThread(
    					appId, docId, batchResult, session, docManager);
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
        final OutputStream output = ((ServletResponse) response).getOutputStream();
        output.write(result);
        output.flush();
        output.close();
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
}
