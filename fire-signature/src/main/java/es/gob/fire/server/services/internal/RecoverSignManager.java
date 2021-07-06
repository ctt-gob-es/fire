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
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
import es.gob.fire.server.connector.FIReSignatureException;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.document.FireAsyncDocumentManager;
import es.gob.fire.server.document.FireDocumentManagerBase;
import es.gob.fire.server.services.FIReTriHelper;
import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.ServiceUtil;
import es.gob.fire.server.services.SignOperation;
import es.gob.fire.server.services.crypto.CryptoHelper;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.upgrade.ConnectionException;
import es.gob.fire.upgrade.GracePeriodInfo;
import es.gob.fire.upgrade.SignatureValidator;
import es.gob.fire.upgrade.UpgradeException;
import es.gob.fire.upgrade.UpgradeParams;
import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.UpgradeResult.State;
import es.gob.fire.upgrade.UpgraderUtils;
import es.gob.fire.upgrade.ValidatorException;
import es.gob.fire.upgrade.VerifyException;
import es.gob.fire.upgrade.VerifyResult;


/**
 * Manejador encargado de la composici&oacute;n de las firmas, su actualizaci&oacute;n
 * de ser preciso, y la devoluci&oacute;n al cliente.
 */
public class RecoverSignManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverSignManager.class.getName());
	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();

	/**
	 * Finaliza un proceso de firma y devuelve el resultado del mismo.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverSignature(final RequestParameters params, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String appId = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String upgrade = params.getParameter(ServiceParams.HTTP_PARAM_UPGRADE);
		final String configB64 = params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

        // Comprobamos que se hayan proporcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		// Cargamos la configuracion de la operacion
        Properties config = null;
    	if (configB64 != null && configB64.length() > 0) {
    		try {
    			config = ServiceUtil.base642Properties(configB64);
    		}
    		catch (final Exception e) {
            	LOGGER.log(Level.SEVERE, logF.f("Error al decodificar las configuracion de los proveedores de firma"), e); //$NON-NLS-1$
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
			}
    	}

        // Obtenemos la informacion para la configuracion particular de la mejora/validacion de la firma
        final Properties upgraterConfig = UpgraderUtils.extractUpdaterProperties(config);

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

        // Comprobamos que no se haya declarado ya un error, en cuyo caso, lo devolvemos
        if (session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {
        	final String errType = session.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
        	final String errMessage = session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
        	SIGNLOGGER.register(session, false, null);
        	TRANSLOGGER.register(session, false);
        	SessionCollector.removeSession(session);
        	LOGGER.warning(logF.f("Ocurrio un error durante la operacion de firma: " + errMessage)); //$NON-NLS-1$
        	sendResult(
        			response,
        			new TransactionResult(
        					TransactionResult.RESULT_TYPE_SIGN,
        					Integer.parseInt(errType),
        					errMessage));
        	return;
        }

        // Extraemos la configuracion de firma
        final String tdB64			= session.getString(ServiceParams.SESSION_PARAM_TRIPHASE_DATA); // 1
        final String certB64		= session.getString(ServiceParams.SESSION_PARAM_CERT); // 2
        final byte[] docId			= (byte[]) session.getObject(ServiceParams.SESSION_PARAM_DOC_ID);
        final String cop			= session.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION);
        final String algorithm		= session.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
        final String format			= session.getString(ServiceParams.SESSION_PARAM_FORMAT);
        final Properties extraParams = (Properties) session.getObject(ServiceParams.SESSION_PARAM_EXTRA_PARAM);

        final String providerName	= session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
        final String remoteTrId		= session.getString(ServiceParams.SESSION_PARAM_REMOTE_TRANSACTION_ID); // 3

        if (providerName == null) {
    		LOGGER.severe(logF.f("No se selecciono un proveedor de firma. Probablemente el usuario no fue redirigido a la URL indicada en la transaccion")); //$NON-NLS-1$
    		sendError(response, session, HttpServletResponse.SC_BAD_REQUEST,
    				"No se selecciono un proveedor de firma. Probablemente el usuario no fue redirigido a la URL indicada en la transaccion"); //$NON-NLS-1$
    		return;
        }

        final TransactionConfig connConfig	=
        		(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

        // En caso de haberse indicado un DocumentManager, lo recogemos
        final FIReDocumentManager docManager = (FIReDocumentManager) session.getObject(ServiceParams.SESSION_PARAM_DOCUMENT_MANAGER);

        // Decodificamos el certificado si se indico.
        // Si no se indico, es posible que el proceso falle mas adelante si resulta que este era obligatorio
        X509Certificate signingCert = null;
        if (certB64 != null) {
        	try {
        		signingCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
        				new ByteArrayInputStream(Base64.decode(certB64, true))
        				);
        	}
        	catch (final Exception e) {
        		LOGGER.severe(logF.f("No se ha podido decodificar el certificado del firmante: " + e)); //$NON-NLS-1$
        		sendError(response, session, HttpServletResponse.SC_BAD_REQUEST,
            			"El proveedor o cliente de firma proporciono un certificado mal formado"); //$NON-NLS-1$
        		return;
        	}
        }

    	// Recuperamos la firma parcial o los datos (segun el proveedor utilizado)
        // del temporal en el que lo almacenamos
    	byte[] partialResult;
    	try {
    		partialResult = TempDocumentsManager.retrieveDocument(transactionId);
    	}
    	catch (final Exception e) {
    		LOGGER.warning(logF.f("No se encuentra la firma parcial generada. Puede haber caducado la sesion: " + e)); //$NON-NLS-1$
			sendError(response, session, HttpServletResponse.SC_REQUEST_TIMEOUT,
					"Ha caducado la sesion"); //$NON-NLS-1$
    		return;
    	}

    	LOGGER.info(logF.f("Firma parcial cargada")); //$NON-NLS-1$

        // En el caso de la firma con certificado local, tendremos ya la firma
        // completa, pero si se uso un certificado en la nube, se debera completar el proceso de firma
    	if (!ProviderManager.PROVIDER_NAME_LOCAL.equalsIgnoreCase(providerName)) {

    		LOGGER.info(logF.f(String.format("Se enviaron los datos al proveedor remoto %3s y ahora se le solicitara su PKCS#1", providerName))); //$NON-NLS-1$

    		// Comprobamos el certificado
    		if (signingCert == null) {
    			LOGGER.severe(logF.f("El certificado firmante es obligatorio para componer la firma con proveedores en la nube y no se devolvio")); //$NON-NLS-1$
    			sendError(response, session, HttpServletResponse.SC_BAD_REQUEST,
    					"El proveedor de firma no proporciono el certificado utilizado para firmar"); //$NON-NLS-1$
    			return;
    		}

    		// Decodificamos la informacion de la firma trifasica
    		TriphaseData td;
    		try {
    			td = TriphaseData.parser(Base64.decode(tdB64, true));
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, logF.f("Error de codificacion en los datos de firma trifasica proporcionados"), e); //$NON-NLS-1$
    			sendError(response, session, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
    		}

    		// Realizamos la segunda fase de la firma trifasica
    		LOGGER.info(logF.f("Se solicita el PKCS#1 al proveedor " + providerName)); //$NON-NLS-1$
    		try {
    			td = triphaseSign(providerName, remoteTrId, connConfig, td, signingCert, logF);
    		}
    		catch (final IllegalArgumentException e) {
    			LOGGER.log(Level.SEVERE, logF.f("Parametro no valido"), e); //$NON-NLS-1$
    			sendError(response, session, HttpServletResponse.SC_BAD_REQUEST,
    					"Parametro no valido: " + e.getMessage()); //$NON-NLS-1$
    			return;
			}
    		catch(final FIReConnectorFactoryException e) {
    			LOGGER.log(Level.WARNING, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", providerName), e); //$NON-NLS-1$
    			sendError(response, session, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
    		}
    		catch(final FIReConnectorUnknownUserException e) {
    			LOGGER.log(Level.WARNING, logF.f("El usuario no esta dado de alta en el sistema"), e); //$NON-NLS-1$
    			sendError(response, session, HttpCustomErrors.NO_USER.getErrorCode());
    			return;
    		}
    		catch(final FIReConnectorNetworkException e) {
    			LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
    			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
                sendError(response, session, HttpCustomErrors.SIGN_ERROR.getErrorCode());
    			return;
    		}
    		catch(final FIReSignatureException e) {
    			LOGGER.log(Level.WARNING, logF.f("Error durante el proceso de firma"), e); //$NON-NLS-1$
    			sendError(response, session, HttpCustomErrors.SIGN_ERROR.getErrorCode());
    			return;
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, logF.f("Error interno durante la firma"), e); //$NON-NLS-1$
    			sendError(response, session, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			return;
			}

    		// Realizamos la tercera fase de la firma trifasica
    		LOGGER.info(logF.f("Se completa el proceso de firma")); //$NON-NLS-1$
    		try {
    			partialResult = FIReTriHelper.getPostSign(cop, format, algorithm, extraParams,
    					signingCert, partialResult, td, logF);
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, logF.f("Error durante la postfirma"), e); //$NON-NLS-1$
    			sendError(response, session, HttpCustomErrors.POSTSIGN_ERROR.getErrorCode());
    			return;
    		}
    	}

		// Se actualiza o valida la firma si se ha solicitado
    	String finalUpgradeFormat = null;
    	if (upgrade != null && !upgrade.isEmpty()) {

    		// Comprobamos si es necesaria la validacion de la firma
        	final boolean signValidationNeeded = needValidation(
        			ConfigManager.isSecureProvider(providerName), cop, format);

        	final PostProcessResult postProcessResult;
    		try {
    			postProcessResult = postProcessSignature(partialResult, upgrade,
    					upgraterConfig, signValidationNeeded, logF);
    		}
    		catch (final InvalidSignatureException e) {
    			LOGGER.log(Level.WARNING, logF.f("La firma generada no es valida")); //$NON-NLS-1$
    			sendError(response, session, HttpCustomErrors.INVALID_SIGNATURE_ERROR.getErrorCode());
    			return;
    		}
    		catch (final ConnectionException e) {
    			LOGGER.log(Level.SEVERE, logF.f("No se pudo conectar con el servicio de validacion y mejora de firmas")); //$NON-NLS-1$
    			AlarmsManager.notify(Alarm.CONNECTION_VALIDATION_PLATFORM);
    			sendError(response, session, HttpCustomErrors.UPGRADING_ERROR.getErrorCode());
    			return;
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, logF.f("Error al validar o actualizar la firma"), e); //$NON-NLS-1$
    			sendError(response, session, HttpCustomErrors.UPGRADING_ERROR.getErrorCode());
    			return;
    		}

    		finalUpgradeFormat = postProcessResult.getUpgradeFormat();
    		partialResult = postProcessResult.getResult();

    		// Si hay que esperar un periodo de gracia, damos la operacion por completada correctamente y
    		// enviamos la informacion de la operacion y del propio periodo
    		if (postProcessResult.getState() == PostProcessResult.State.PENDING) {
    	    	LOGGER.info(logF.f("Se requiere la espera de un periodo de gracia para la obtencion de la firma")); //$NON-NLS-1$
    			// Si nuestro DocumentManager soporta el uso de firma asincronas, registramos
    			// la operacion
    			final GracePeriodInfo gracePeriod = postProcessResult.getGracePeriodInfo();
    			if (docManager instanceof FireAsyncDocumentManager) {
    				try {
    					((FireAsyncDocumentManager) docManager).registryAsyncOperation(
    							gracePeriod.getResponseId(), gracePeriod.getResolutionDate(), appId,
    							docId, partialResult, signingCert, format, finalUpgradeFormat,
    							extraParams);
    				}
    				catch (final Exception e) {
    					LOGGER.log(Level.WARNING, logF.f("Error al registrar la operacion asincrona en el DocumentManager"), e); //$NON-NLS-1$
    					AlarmsManager.notify(Alarm.CONNECTION_DOCUMENT_MANAGER, docManager.getClass().getCanonicalName());
    		    		sendError(response, session, HttpCustomErrors.SAVING_ERROR.getErrorCode());
    					return;
					}
    			}
    			SIGNLOGGER.register(session, true);
    			TRANSLOGGER.register(session, true);
    			SessionCollector.removeSession(session);
    	        sendResult(response, buildResult(providerName, signingCert, gracePeriod));
    	        return;
    		}
    	}

        // Operamos con la firma tal como lo indique el DocumentManager. Si nuestro
    	// DocumentManager soporta el que indiquemos tambien el formato al que se ha
    	// actualizado la firma, lo proporcionamos
        try {
        	if (docManager instanceof FireDocumentManagerBase) {
        		partialResult = ((FireDocumentManagerBase) docManager).storeDocument(
        				docId, transactionId, appId, partialResult, signingCert, format,
        				upgrade, extraParams);
        	} else {
        		partialResult = docManager.storeDocument(docId, appId, partialResult,
        				signingCert, format, extraParams);
        	}
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("Error en el guardado de la firma del documento " + docId), e); //$NON-NLS-1$
        	AlarmsManager.notify(Alarm.CONNECTION_DOCUMENT_MANAGER, docManager.getClass().getCanonicalName());
    		sendError(response, session, HttpCustomErrors.SAVING_ERROR.getErrorCode());
			return;
		}

    	// Guardamos la firma resultante para devolverla despues, ya que en un primer
    	// momento solo responderemos con el resultado de la operacion y no con la propia
    	// firma generada
        LOGGER.info(logF.f("Se almacena temporalmente el resultado de la operacion")); //$NON-NLS-1$
    	try {
    		TempDocumentsManager.storeDocument(transactionId, partialResult, false);
    	}
    	catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, logF.f("Error al almacenar la firma despues de haberla completado"), e); //$NON-NLS-1$
        	sendError(response, session, HttpCustomErrors.SAVING_ERROR.getErrorCode());
			return;
    	}

    	// Ya no necesitaremos de nuevo la sesion, asi que la eliminamos del pool
    	session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_RECOVER);
    	SessionCollector.commit(session);

    	LOGGER.info(logF.f("Se devuelve la informacion del resultado de la operacion")); //$NON-NLS-1$

    	// Enviamos el resultado de la operacion y los datos de la misma, aunque no la propia firma generada
        sendResult(response, buildResult(providerName, signingCert, finalUpgradeFormat));
	}

	/**
	 * Se encarga de postprocesar la firma, ya sea mejor&aacute;ndola a formato longevo o
	 * valid&aacute;ndola.
	 * @param signature Firma a procesar.
	 * @param upgradeLevel Nivel de actualizaci&oacute;n o validaci&oacute;n de firma.
	 * @param upgraderConfig Configuraci&oacute;n de la operaci&oacute;n de actualizacion de firma.
	 * @param needValidation {@code true} si debe validarse la firma cuando se solicite, {@code false} si
	 * la firma no tiene que ser validada en ning&uacute;n caso.
	 * @return La propia firma o la versi&oacute;n actualizada de la misma si aplica.
	 * @throws InvalidSignatureException Cuando se detecta que la firma no es v&aacute;lida.
	 * @throws ConnectionException Cuando no se puede conectar con la plataforma de validaci&oacute;n
	 * y actualizaci&oacute;n de firmas.
	 * @throws UpgradeException Cuando ocurre un error durante la actualizaci&oacute;n de firmas.
	 * @throws VerifyException  Cuando ocurre un error durante la validaci&oacute;n de firmas.
	 * @throws ValidatorException Cuando no ha sido posible cargar el conector para la
	 * validaci&oacute;n y actualizaci&oacute;n de firnas,
	 */
	private static PostProcessResult postProcessSignature(final byte[] signature, final String upgradeLevel,
			final Properties upgraderConfig, final boolean needValidation,
			final LogTransactionFormatter logF)
					throws InvalidSignatureException, ConnectionException, UpgradeException,
					VerifyException, ValidatorException {

		PostProcessResult result;
		final SignatureValidator validator = SignatureValidatorBuilder.getSignatureValidator(logF);
		if (ServiceParams.UPGRADE_VERIFY.equalsIgnoreCase(upgradeLevel)) {
			if (needValidation) {
				LOGGER.info(logF.f("Validamos la firma")); //$NON-NLS-1$
				final VerifyResult verifyResult = validator.validateSignature(signature, upgraderConfig);
				if (!verifyResult.isOk()) {
					throw new InvalidSignatureException("La firma generada no es valida"); //$NON-NLS-1$
				}
			}
			else {
				LOGGER.info(logF.f("El proveedor es seguro y no es necesario validar el tipo de firma generada")); //$NON-NLS-1$
			}
			result = new PostProcessResult(signature);
		}
		else {
			LOGGER.info(logF.f("Actualizamos la firma a: " + upgradeLevel)); //$NON-NLS-1$
			UpgradeResult upgradeResult;
			try {
				upgradeResult = validator.upgradeSignature(signature, upgradeLevel, upgraderConfig);
			}
			catch (final VerifyException e) {
				throw new InvalidSignatureException("Se ha intentado actualizar una firma invalida", e); //$NON-NLS-1$
			}

			boolean allowPartialUpgrade = false;
			if (upgraderConfig != null) {
				allowPartialUpgrade = Boolean.parseBoolean(upgraderConfig.getProperty(UpgradeParams.ALLOW_PARTIAL_UPGRADE));
			}

	        // Comprobamos si era necesario recuperar la firma totalmente actualizada y si se ha hecho asi
	        if (!allowPartialUpgrade && upgradeResult.getState() == State.PARTIAL) {
	        	throw new UpgradeException("La firma no se actualizo hasta el formato solicitado"); //$NON-NLS-1$
	        }

			if (upgradeResult.getState() == UpgradeResult.State.PENDING) {
				result = new PostProcessResult(upgradeResult.getGracePeriodInfo());
			}
			else {
				result = new PostProcessResult(upgradeResult.getResult());
				result.setUpgradeFormat(upgradeResult.getFormat());
			}
		}
		return result;
	}

	/** Construye el resultado de la operaci&oacute;n.
	 * @param providerName Nombre del proveedor seleccionado.
	 * @param signingCert Certificado utilizado para firmar.
	 * @param upgradeFormat Formato al que se ha actualizado la firma.
	 * @return Resultado de la operaci&oacute;n de firma.
	 */
	private static TransactionResult buildResult(final String providerName,
			final X509Certificate signingCert, final String upgradeFormat) {
		final TransactionResult result = new TransactionResult(TransactionResult.RESULT_TYPE_SIGN, providerName);
		result.setSigningCert(signingCert);
		result.setUpgradeFormat(upgradeFormat);
		return result;
	}

	/** Construye el resultado de la operaci&oacute;n.
	 * @param providerName Nombre del proveedor seleccionado.
	 * @param signingCert Certificado utilizado para firmar.
	 * @return Resultado de la operaci&oacute;n de firma.
	 */
	private static TransactionResult buildResult(final String providerName,
			final X509Certificate signingCert, final GracePeriodInfo gracePeriod) {
		final TransactionResult result = new TransactionResult(TransactionResult.RESULT_TYPE_SIGN, providerName);
		result.setSigningCert(signingCert);
		result.setGracePeriod(gracePeriod);
		return result;
	}

	/**
	 * Devuelve la firma resultante.
	 * @param response Respuesta a la petici&oacute;n realizada.
	 * @param result Resultado de firmar.
	 * @throws IOException Cuando ocurre un error al enviar el resultado.
	 */
	private static void sendResult(final HttpServletResponse response, final TransactionResult result) throws IOException {
		// El servicio devuelve el resultado de la operacion de firma.
        final OutputStream output = ((ServletResponse) response).getOutputStream();
        output.write(result.encodeResult());
        output.flush();
        output.close();
	}

	/**
	 * Recupera el PKCS#1 de una firma del proveedor de firma en la nube que la gener&oacute;
	 * y comprueba que realmente se generasen con el certificado indicado.
	 * @param providerName Nombre del proveedor.
	 * @param remoteTrId Identificador de transacci&oacute;n del proveedor.
	 * @param trConfig Configuraci&oacute;n para el proveedor.
	 * @param td Informaci&oacute;n de firma trif&aacute;sica en el que insertar el PKCS#1.
	 * @return informaci&oacute;n de firma trif&aacute;sica con el PKCS#1.
	 * @throws IllegalArgumentException Cuando falta un par&aacute;metro o se ha proporcionado uno no v&aacute;lido.
	 * @throws FIReConnectorUnknownUserException Cuando el usuario no est&aacute; registrado.
	 * @throws FIReConnectorNetworkException Cuando no se puede conectar con el proveedor de firma en la nube.
	 * @throws FIReSignatureException Cuando falla la petici&oacute;n de firma al proveedor.
	 * @throws FIReConnectorFactoryException Cuando falla la carga del conector del proveedor de firma en la nube.
	 * @throws FireInternalException Cuando se produce cualquier otro error.
	 */
	private static TriphaseData triphaseSign(final String providerName, final String remoteTrId,
			final TransactionConfig trConfig, final TriphaseData td, final Certificate signingCert,
			final LogTransactionFormatter logF)
					throws IllegalArgumentException, FIReConnectorUnknownUserException,
					FIReConnectorNetworkException, FIReSignatureException, FIReConnectorFactoryException,
					FireInternalException {

		if (trConfig == null) {
			throw new IllegalArgumentException(
					"No se proporcionaron datos para la configuracion del proveedor remoto de firma"); //$NON-NLS-1$
		}

		// Obtenemos el conector con el backend ya configurado
		final FIReConnector connector = ProviderManager.getProviderConnector(providerName, trConfig.getProperties());

		final Map<String, byte[]> ret;
		ret = connector.sign(remoteTrId);

		// Notificamos al conector que ha terminado la operacion para que libere recursos y
		// cierre la transaccion
		connector.endSign(remoteTrId);

		// Insertamos los PKCS#1 en la sesion trifasica
		final Set<String> keys = ret.keySet();
		for (final String key : keys) {

			final byte[] pkcs1 = ret.get(key);

			try {
				CryptoHelper.verifyPkcs1(pkcs1, signingCert.getPublicKey(), logF);
			}
			catch (final Exception e) {
				throw new FIReSignatureException("Error de integridad. El PKCS#1 recibido no se genero con el certificado indicado", e); //$NON-NLS-1$
			}

			FIReTriHelper.addPkcs1ToTriSign(pkcs1, key, td);
		}

		return td;
	}

	/**
	 * Responde a la petici&oacute;n con un c&oacute;digo de error, registra dicho error y
	 * limpia la sesi&oacute;n.
	 * @param response Respuesta a la petici&oacute;n.
	 * @param session Sesi&oacute;n de la que extraer los datos de la operaci&oacute;n y que limpiar.
	 * @param errorCode C&oacute;digo de error que enviar.
	 * @throws IOException Cuando ocurre un error al enviar el error.
	 */
	private static void sendError(final HttpServletResponse response, final FireSession session,
			final int errorCode) throws IOException {
		SIGNLOGGER.register(session, false, null);
		TRANSLOGGER.register(session, false);
		SessionCollector.removeSession(session);
		response.sendError(errorCode);
	}

	/**
	 * Responde a la petici&oacute;n con un c&oacute;digo de error, registra dicho error y
	 * limpia la sesi&oacute;n.
	 * @param response Respuesta a la petici&oacute;n.
	 * @param session Sesi&oacute;n de la que extraer los datos de la operaci&oacute;n y que limpiar.
	 * @param errorCode C&oacute;digo de error que enviar.
	 * @param message Mensaje de error.
	 * @throws IOException Cuando ocurre un error al enviar el error.
	 */
	private static void sendError(final HttpServletResponse response, final FireSession session, final int errorCode,
			final String message) throws IOException {
		SIGNLOGGER.register(session, false, null);
		TRANSLOGGER.register(session, false);
		SessionCollector.removeSession(session);
		response.sendError(errorCode, message);
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
