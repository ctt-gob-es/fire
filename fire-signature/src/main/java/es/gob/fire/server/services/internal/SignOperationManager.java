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
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.connector.DocInfo;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.FIReDocumentManagerFactory;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.signature.ConfigManager;

/**
 * Manejador encargado de la gesti&oacute;n de las operaciones de firma ordenadas al
 * componente central.
 */
public class SignOperationManager {

	private static final Logger LOGGER = Logger.getLogger(SignOperationManager.class.getName());

	/**
	 * Inicia la operaci&oacute;n de firma asociada al componente central.
	 * @param request Solicitud HTTP.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta HTTP.
	 * @throws IOException Cuando se produce un error en la comunicaci&oacute;n con el cliente
	 * o en el guardado de temporales.
	 */
	public static void sign(final HttpServletRequest request, final RequestParameters params, final HttpServletResponse response) throws IOException {

		final String op				= params.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
		final String appId			= params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String configB64      = params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);
        final String subjectId      = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
        final String algorithm      = params.getParameter(ServiceParams.HTTP_PARAM_ALGORITHM);
        final String cop			= params.getParameter(ServiceParams.HTTP_PARAM_CRYPTO_OPERATION);
        final String format         = params.getParameter(ServiceParams.HTTP_PARAM_FORMAT);
        final String dataB64        = params.getParameter(ServiceParams.HTTP_PARAM_DATA);
        String extraParamsB64 		= params.getParameter(ServiceParams.HTTP_PARAM_EXTRA_PARAM);

        if (subjectId == null || subjectId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el identificador del usuario que solicita la firma"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se ha proporcionado el identificador del usuario que solicita la firma"); //$NON-NLS-1$
            return;
        }

        if (algorithm == null || algorithm.isEmpty()) {
            LOGGER.warning("No se ha proporcionado el algoritmo de firma"); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha proporcionado el algoritmo de firma"); //$NON-NLS-1$
            return;
        }

        if (cop == null || cop.isEmpty()) {
            LOGGER.warning("No se ha indicado la operacion de firma a realizar"); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha indicado la operacion de firma a realizar"); //$NON-NLS-1$
            return;
        }

        if (format == null || format.isEmpty()) {
            LOGGER.warning("No se ha indicado el formato de firma"); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se ha indicado el formato de firma"); //$NON-NLS-1$
            return;
        }

        if (dataB64 == null || dataB64.isEmpty()) {
			LOGGER.warning("No se han proporcionado los datos a firmar"); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "No se han proporcionado los datos a firmar"); //$NON-NLS-1$
            return;
        }

		TransactionConfig connConfig = null;
		if (configB64 != null && configB64.length() > 0) {
			try {
				connConfig = new TransactionConfig(configB64);
			}
			catch(final Exception e) {
				LOGGER.warning("Se proporcionaron datos malformados para la conexion y configuracion del backend"); //$NON-NLS-1$
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Se proporcionaron datos malformados para la conexion y configuracion del backend"); //$NON-NLS-1$
				return;
			}
		}

		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.warning("No se proporcionaron las URL de redireccion para la operacion"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se proporcionaron las URL de redireccion para la operacion"); //$NON-NLS-1$
			return;
		}
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

		final String appName = connConfig.getAppName();
		final String docManagerName = connConfig.getDocumentManager();

		// Copiamos al extraParams la informacion del documento firmado y el formato de firma
		final Properties extraParams = AOUtil.base642Properties(extraParamsB64);
		final DocInfo docInfo = DocInfo.extractDocInfo(connConfig.getProperties());
		DocInfo.addDocInfoToSign(extraParams, docInfo);
		extraParamsB64 = AOUtil.properties2Base64(extraParams);

		final FireSession session = SessionCollector.createFireSession(request.getSession());
		final String transactionId = session.getTransactionId();

		LOGGER.fine(String.format("TrId %1s: SignOperationManager", transactionId)); //$NON-NLS-1$

        // Guardamos en la sesion la configuracion de la operacion
        session.setAttribute(ServiceParams.SESSION_PARAM_OPERATION, op);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_ID, appId);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_NAME, appName);
        session.setAttribute(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG, connConfig.cleanConfig());
        session.setAttribute(ServiceParams.SESSION_PARAM_SUBJECT_ID, subjectId);
        session.setAttribute(ServiceParams.SESSION_PARAM_ALGORITHM, algorithm);
        session.setAttribute(ServiceParams.SESSION_PARAM_EXTRA_PARAM, extraParamsB64);
        session.setAttribute(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION, cop);
        session.setAttribute(ServiceParams.SESSION_PARAM_FORMAT, format);
        session.setAttribute(ServiceParams.SESSION_PARAM_PROVIDERS, provs);

        // Obtenemos el DocumentManager con el que recuperar los datos. Si no se especifico ninguno,
        // cargamos el por defecto
        FIReDocumentManager docManager;
        try {
        	docManager = FIReDocumentManagerFactory.newDocumentManager(docManagerName);
        }
        catch (final IllegalArgumentException e) {
        	LOGGER.log(Level.SEVERE, "No existe el gestor de documentos: " + docManagerName, e); //$NON-NLS-1$
        	setErrorToSession(session, OperationError.INTERNAL_ERROR);
        	sendResult(response, new SignOperationResult(transactionId, redirectErrorUrl));
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, "No se ha podido cargar el gestor de documentos con el nombre: " + docManagerName, e); //$NON-NLS-1$
        	setErrorToSession(session, OperationError.INTERNAL_ERROR);
        	sendResult(response, new SignOperationResult(transactionId, redirectErrorUrl));
        	return;
        }

        // Obtenemos el identificador del documento (que puede ser el propio documento)
        byte[] docId;
        try {
        	docId = Base64.decode(dataB64, true);
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, "El documento enviado a firmar no esta bien codificado", e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        			"El documento enviado a firmar no esta bien codificado"); //$NON-NLS-1$
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
        SessionCollector.commit(session);

        // Obtenemos los datos a firmar a partir de los datos proporcionados
        // mediante del DocumentManager que corresponda
        byte[] data;
        try {
        	data = docManager.getDocument(docId, appId, format, extraParams);
        }
        catch (final Exception e) {
    		LOGGER.warning("Error al obtener los datos a firmar"); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"Error al obtener los datos a firmar"); //$NON-NLS-1$
    		return;
        }

    	if (data == null) {
    		LOGGER.warning("No se han podido obtener los datos a firmar"); //$NON-NLS-1$
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST,
    				"No se han podido obtener los datos a firmar"); //$NON-NLS-1$
    		return;
    	}

        // Creamos un temporal con los datos a procesar asociado a la sesion
        try {
        	TempFilesHelper.storeTempData(transactionId, data);
        }
        catch (final Exception e) {
        	LOGGER.severe("Error en el guardado temporal de los datos a firmar: " + e); //$NON-NLS-1$
        	setErrorToSession(session, OperationError.INTERNAL_ERROR);
        	sendResult(response, new SignOperationResult(transactionId, redirectErrorUrl));
        	return;
		}

		// Obtenemos la URL de las paginas web de FIRe (parte publica). Si no se define,
		// se calcula en base a la URL actual
		String redirectUrlBase = ConfigManager.getPublicContextUrl();
		if (redirectUrlBase == null || redirectUrlBase.isEmpty()){
			redirectUrlBase = request.getRequestURL().toString();
			redirectUrlBase = redirectUrlBase.substring(0, redirectUrlBase.lastIndexOf('/'));
		}
		redirectUrlBase += "/public/"; //$NON-NLS-1$

        // Si hay proveedor disponible, se selecciona automaticamente;
        // si no, se envia a la pagina de seleccion de proveedor
		String redirectUrl;
        if (provs.length == 1) {
        	redirectUrl = "chooseCertificateOriginService?" + //$NON-NLS-1$
        			ServiceParams.HTTP_PARAM_CERT_ORIGIN + "=" + provs[0] + "&" + //$NON-NLS-1$ //$NON-NLS-2$
 					ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED + "=true"; //$NON-NLS-1$
        } else {
        	redirectUrl = "ChooseCertificateOrigin.jsp"; //$NON-NLS-1$
        }

        // Devolvemos al usuario el ID de la transaccion y la pagina a la que debe dirigir al usuario
        final SignOperationResult result = new SignOperationResult(
        		transactionId,
        		redirectUrlBase + redirectUrl +
        			(redirectUrl.indexOf('?') == -1 ? "?" : "&") + //$NON-NLS-1$ //$NON-NLS-2$
        			ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId + //$NON-NLS-1$
        			"&" + ServiceParams.HTTP_PARAM_SUBJECT_ID + "=" + subjectId + //$NON-NLS-1$ //$NON-NLS-2$
        			"&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + redirectErrorUrl); //$NON-NLS-1$ //$NON-NLS-2$

        sendResult(response, result);
	}

	private static void setErrorToSession(final FireSession session, final OperationError error) {
		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE, Integer.toString(error.getCode()));
		session.setAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE, error.getMessage());
		SessionCollector.commit(session);
	}

	private static void sendResult(final HttpServletResponse response, final SignOperationResult result) throws IOException {
        response.setContentType("application/json"); //$NON-NLS-1$
        final PrintWriter out = response.getWriter();
        out.print(result.toString());
        out.flush();
        out.close();
	}
}
