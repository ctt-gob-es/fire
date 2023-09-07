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

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.DocInfo;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.document.FireDocumentManagerBase;
import es.gob.fire.server.services.FIReDocumentManagerFactory;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;
import es.gob.fire.server.services.statistics.TransactionType;

/**
 * Manejador encargado de la gesti&oacute;n de las operaciones de firma ordenadas al
 * componente central.
 */
public class SignOperationManager {

	private static final Logger LOGGER = Logger.getLogger(SignOperationManager.class.getName());
	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();

	/**
	 * Inicia la operaci&oacute;n de firma asociada al componente central.
	 * @param request Solicitud HTTP.
	 * @param appName Nombre de la aplicaci&oacute;n.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @param response Respuesta HTTP.
	 * @throws IOException Cuando se produce un error en la comunicaci&oacute;n con el cliente
	 * o en el guardado de temporales.
	 */
	public static void sign(final HttpServletRequest request, final String appName, final RequestParameters params,
			final TransactionAuxParams trAux, final HttpServletResponse response) throws IOException {

		final String appId			= params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String configB64      = params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);
        final String subjectId      = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
        final String algorithm      = params.getParameter(ServiceParams.HTTP_PARAM_ALGORITHM);
        final String cop			= params.getParameter(ServiceParams.HTTP_PARAM_CRYPTO_OPERATION);
        final String format         = params.getParameter(ServiceParams.HTTP_PARAM_FORMAT);
        final String improvedFormat = params.getParameter(ServiceParams.HTTP_PARAM_UPGRADE);
        final String dataB64        = params.getParameter(ServiceParams.HTTP_PARAM_DATA);
        final String extraParamsB64 = params.getParameter(ServiceParams.HTTP_PARAM_EXTRA_PARAM);

		final LogTransactionFormatter logF = trAux.getLogFormatter();

        if (subjectId == null || subjectId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el identificador del usuario que solicita la firma")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.PARAMETER_USER_ID_NEEDED);
            return;
        }

        if (algorithm == null || algorithm.isEmpty()) {
            LOGGER.warning(logF.f("No se ha proporcionado el algoritmo de firma")); //$NON-NLS-1$
            Responser.sendError(response, FIReError.PARAMETER_SIGNATURE_ALGORITHM_NEEDED);
            return;
        }

        if (cop == null || cop.isEmpty()) {
            LOGGER.warning(logF.f("No se ha indicado la operacion de firma a realizar")); //$NON-NLS-1$
            Responser.sendError(response, FIReError.PARAMETER_SIGNATURE_OPERATION_NEEDED);
            return;
        }

        if (format == null || format.isEmpty()) {
            LOGGER.warning(logF.f("No se ha indicado el formato de firma")); //$NON-NLS-1$
            Responser.sendError(response, FIReError.PARAMETER_SIGNATURE_FORMAT_NEEDED);
            return;
        }

        if (dataB64 == null || dataB64.isEmpty()) {
			LOGGER.warning(logF.f("No se han proporcionado los datos a firmar")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.PARAMETER_DATA_TO_SIGN_NEEDED);
            return;
        }

		if (configB64 == null || configB64.isEmpty()) {
			LOGGER.warning(logF.f("No se proporciono la configuracion de la transaccion de firma")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.PARAMETER_CONFIG_TRANSACTION_NEEDED);
			return;
		}

		TransactionConfig connConfig = null;
		try {
			connConfig = new TransactionConfig(configB64);
		}
		catch(final Exception e) {
			LOGGER.warning(logF.f("Se proporcionaron datos malformados para la conexion y configuracion de los proveedores de firma")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.PARAMETER_CONFIG_TRANSACTION_INVALID);
			return;
		}

		if (!connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.warning(logF.f("No se proporcionaron las URL de redireccion para la operacion")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.PARAMETER_URL_ERROR_REDIRECION_NEEDED);
			return;
		}

		LOGGER.fine(logF.f("Peticion de inicio de transaccion de FIRMA bien formada")); //$NON-NLS-1$

		final String redirectErrorUrl = connConfig.getRedirectErrorUrl();

        // Se obtiene el listado final de proveedores para la operacion, filtrando la
        // lista de proveedores dados de alta con los solicitados
		String[] provs;
		final String[] requestedProvs = connConfig.getProviders();
		if (requestedProvs != null) {
			provs = ProviderManager.getFilteredProviders(requestedProvs);
		}
        else {
        	provs = ProviderManager.getProviderNames();
        }

		final String appTitle = connConfig.getAppTitle();
		final String docManagerName = connConfig.getDocumentManager();

		// Copiamos al extraParams la informacion del documento firmado y el formato de firma
		Properties extraParams = null;
		try {
			extraParams = AOUtil.base642Properties(extraParamsB64);
		}
		catch (final Exception e) {
			LOGGER.warning(logF.f("Se ha proporcionado un extraParam mal formado: ") + e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.PARAMETER_SIGNATURE_PARAMS_INVALID);
			return;
		}
		final DocInfo docInfo = DocInfo.extractDocInfo(connConfig.getProperties());
		DocInfo.addDocInfoToSign(extraParams, docInfo);

		final FireSession session = SessionCollector.createFireSession(subjectId, trAux);
		final String transactionId = session.getTransactionId();

		logF.setTransactionId(transactionId);

		LOGGER.info(logF.f("Iniciada transaccion de FIRMA")); //$NON-NLS-1$

        // Guardamos en la sesion la configuracion de la operacion
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_ID, appId);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_NAME, appName);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_TITLE, appTitle);
        session.setAttribute(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG, connConfig.cleanConfig());
        session.setAttribute(ServiceParams.SESSION_PARAM_ALGORITHM, algorithm);
        session.setAttribute(ServiceParams.SESSION_PARAM_EXTRA_PARAM, extraParams);
        session.setAttribute(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION, cop);
        session.setAttribute(ServiceParams.SESSION_PARAM_FORMAT, format);
        session.setAttribute(ServiceParams.SESSION_PARAM_UPGRADE, improvedFormat);
        session.setAttribute(ServiceParams.SESSION_PARAM_PROVIDERS, provs);
    	session.setAttribute(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE, TransactionType.SIGN);

        // Obtenemos el DocumentManager con el que recuperar los datos. Si no se especifico ninguno,
        // cargamos el por defecto
        FIReDocumentManager docManager;
        try {
        	docManager = FIReDocumentManagerFactory.newDocumentManager(appId, transactionId, docManagerName);
        }
        catch (final IllegalAccessException | IllegalArgumentException e) {
        	LOGGER.log(Level.WARNING, logF.f("No existe o no se tiene permisos para acceder al gestor de documentos: " + docManagerName), e); //$NON-NLS-1$
        	ErrorManager.setErrorToSession(session, FIReError.PARAMETER_DOCUMENT_MANAGER_INVALID, trAux);
        	// En el mensaje de error se indica que no existe para no revelar si simplemente no se tiene permiso
        	Responser.sendError(response, FIReError.PARAMETER_DOCUMENT_MANAGER_INVALID, new SignOperationResult(transactionId, redirectErrorUrl));
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el gestor de documentos con el nombre: " + docManagerName), e); //$NON-NLS-1$
        	ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
        	Responser.sendError(response, FIReError.INTERNAL_ERROR, new SignOperationResult(transactionId, redirectErrorUrl));
        	return;
        }

        LOGGER.fine(logF.f("La transaccion usara el DocumentManager " + docManager.getClass().getName())); //$NON-NLS-1$

        // Obtenemos el identificador del documento (que puede ser el propio documento)
        final byte[] docId;
        try {
        	docId = Base64.decode(dataB64, true);
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("El documento enviado a firmar no esta bien codificado"), e); //$NON-NLS-1$
			SIGNLOGGER.register(session, false, null);
			TRANSLOGGER.register(session, false);
			ErrorManager.setErrorToSession(session, FIReError.PARAMETER_DATA_TO_SIGN_INVALID, trAux);
			Responser.sendError(response, FIReError.PARAMETER_DATA_TO_SIGN_INVALID, new SignOperationResult(transactionId, redirectErrorUrl));
        	return;
        }

        // Agregamos a la sesion el gestor de documentos
        session.setAttribute(ServiceParams.SESSION_PARAM_DOCUMENT_MANAGER, docManager);

        // Agregamos a la sesion el identificador del documento para
        // disponer de el durante la postfirma, a menos que usemos el
        // DocumentManager por defecto porque entonces el identificador
        // seria el documento completo y, por tanto, podria ser muy grande
        if (!FIReDocumentManagerFactory.isDefaultDocumentManager(docManager)) {
        	session.setAttribute(ServiceParams.SESSION_PARAM_DOC_ID, docId);
        }

        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_SIGN);

        LOGGER.fine(logF.f("Se inicia la carga de los datos")); //$NON-NLS-1$

        // Obtenemos los datos a firmar a partir de los datos proporcionados
        // mediante del DocumentManager que corresponda
        final byte[] data;
        try {
        	// Por motivos de compatibilidad, mantenemos el uso de las funciones de la interfaz en
        	// base a la que se construyen los DocumentManager, pero, si es posible, se utilizara
        	// la funcion equivalente de la implementacion de FireDocumentManagerBase, que recibe
        	// mas parametros
        	if (docManager instanceof FireDocumentManagerBase) {
        		data = ((FireDocumentManagerBase) docManager).getDocument(docId, transactionId, appId, format, extraParams);
        	}
        	else {
        		data = docManager.getDocument(docId, appId, format, extraParams);
        	}
        }
        catch (final Exception e) {
    		LOGGER.log(Level.SEVERE, logF.f("Error al obtener los datos a firmar del servidor remoto"), e); //$NON-NLS-1$
    		SIGNLOGGER.register(session, false, null);
    		TRANSLOGGER.register(session, false);
    		AlarmsManager.notify(Alarm.CONNECTION_DOCUMENT_MANAGER, docManager.getClass().getCanonicalName());
    		ErrorManager.setErrorToSession(session, FIReError.PARAMETER_DATA_TO_SIGN_NOT_FOUND, trAux);
			Responser.sendError(response, FIReError.PARAMETER_DATA_TO_SIGN_NOT_FOUND, new SignOperationResult(transactionId, redirectErrorUrl));
    		return;
        }

    	if (data == null) {
    		LOGGER.severe(logF.f("No se han obtenido los datos a firmar")); //$NON-NLS-1$
    		SIGNLOGGER.register(session, false, null);
    		TRANSLOGGER.register(session, false);
    		ErrorManager.setErrorToSession(session, FIReError.PARAMETER_DATA_TO_SIGN_NOT_FOUND, trAux);
			Responser.sendError(response, FIReError.PARAMETER_DATA_TO_SIGN_NOT_FOUND, new SignOperationResult(transactionId, redirectErrorUrl));
    		return;
    	}

        // Creamos un temporal con los datos a procesar asociado a la sesion
        try {
        	TempDocumentsManager.storeDocument(transactionId, data, true);
        	// Registramos el tamano del documento con fines estadisticos
       	 	session.setAttribute(ServiceParams.SESSION_PARAM_DOCSIZE, Long.valueOf(data.length));
        }
        catch (final Exception e) {
        	LOGGER.severe(logF.f("Error en el guardado temporal de los datos a firmar: " + e)); //$NON-NLS-1$
        	ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
        	Responser.sendError(response, FIReError.INTERNAL_ERROR, new SignOperationResult(transactionId, redirectErrorUrl));
        	return;
		}

        // Guardamos los datos de la transaccion en la sesion del servidor y en la coleccion de sesiones
        SessionCollector.commit(session, true, trAux);
        session.saveIntoHttpSession(request.getSession());

        LOGGER.fine(logF.f("Generamos la URL de redireccion")); //$NON-NLS-1$

        // Obtenemos la referencia al usuario de la sesion
        final String subjectRef = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_REF);

        // Si hay proveedor disponible, se selecciona automaticamente;
        // si no, se envia a la pagina de seleccion de proveedor
		final String redirectUrl;
        if (provs.length == 1) {
        	// Si el unico proveedor que hay requiere que el usuario se autentique previamente, lo
        	// redirigimos a la pagina de autenticacion. Si no, a la de seleccion de certificado
        	final ProviderInfo info = ProviderManager.getProviderInfo(provs[0]);
        	redirectUrl = (info.isUserRequiredAutentication() ? ServiceNames.PUBLIC_SERVICE_AUTH_USER : ServiceNames.PUBLIC_SERVICE_CHOOSE_CERT_ORIGIN)
        			+ "?" + ServiceParams.HTTP_PARAM_CERT_ORIGIN + "=" + provs[0] //$NON-NLS-1$ //$NON-NLS-2$
        			+ "&" + ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED + "=true" //$NON-NLS-1$ //$NON-NLS-2$
        			+ "&" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId //$NON-NLS-1$ //$NON-NLS-2$
        			+ "&" + ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef //$NON-NLS-1$ //$NON-NLS-2$
        			+ "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + redirectErrorUrl; //$NON-NLS-1$ //$NON-NLS-2$
        	LOGGER.fine(logF.f("Se forzara el uso del proveedor " + provs[0])); //$NON-NLS-1$
        } else {
        	redirectUrl = ServiceNames.PUBLIC_SERVICE_CHOOSE_ORIGIN
        			+ "?" +  ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId //$NON-NLS-1$ //$NON-NLS-2$
        			+ "&" + ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef //$NON-NLS-1$ //$NON-NLS-2$
        			+ "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + redirectErrorUrl; //$NON-NLS-1$ //$NON-NLS-2$
        }

		// Obtenemos la URL de las paginas web de FIRe (parte publica). Si no se define,
		// se calcula en base a la URL actual
		final String redirectUrlBase = PublicContext.getPublicContext(request);

        // Devolvemos al usuario el ID de la transaccion y la pagina a la que debe dirigir al usuario
        final SignOperationResult result = new SignOperationResult(
        		transactionId,
        		redirectUrlBase + redirectUrl);

        LOGGER.info(logF.f("Devolvemos la URL de redireccion con el ID de transaccion")); //$NON-NLS-1$

        Responser.sendResult(response, result);
	}
}
