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

import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.FIReDocumentManagerFactory;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.ServiceUtil;

/**
 * Manejador que gestiona las peticiones de creaci&oacute;n de un lote de firma, al que posteriormente
 * se le podr&aacute;an agregar documentos a firmar.
 */
public class CreateBatchManager {

	private static final Logger LOGGER = Logger.getLogger(CreateBatchManager.class.getName());

	/**
	 * Create un lote de firma.
	 * @param request Petici&oacute;n para la creaci&oacute;n del lote.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la creaci&oacute;n del lote.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void createBatch(final HttpServletRequest request, final RequestParameters params, final HttpServletResponse response)
		throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String op				= params.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
		final String appId			= params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String subjectId		= params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String cop			= params.getParameter(ServiceParams.HTTP_PARAM_CRYPTO_OPERATION);
		final String algorithm		= params.getParameter(ServiceParams.HTTP_PARAM_ALGORITHM);
		final String format 		= params.getParameter(ServiceParams.HTTP_PARAM_FORMAT);
		final String extraParamsB64	= params.getParameter(ServiceParams.HTTP_PARAM_EXTRA_PARAM);
		final String configB64		= params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);
		final String upgrade		= params.getParameter(ServiceParams.HTTP_PARAM_UPGRADE);

		// Comprobamos que se hayan prorcionado los parametros indispensables
		if (subjectId == null || subjectId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador del usuario que crea el lote"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se ha proporcionado el identificador del usuario que crea el lote"); //$NON-NLS-1$
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

		Properties connConfig = null;
		if (configB64 != null && configB64.length() > 0) {
			try {
				connConfig = ServiceUtil.base642Properties(configB64);
			}
			catch(final Exception e) {
				LOGGER.warning("Se proporcionaron datos malformados para la conexion con el backend"); //$NON-NLS-1$
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Se proporcionaron datos malformados para la conexion con el backend"); //$NON-NLS-1$
				return;
			}
		}

		if (connConfig == null || !connConfig.containsKey(ServiceParams.CONNECTION_PARAM_ERROR_URL)) {
			LOGGER.warning("No se proporcionaron las URL de redireccion para la operacion"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se proporcionaron las URL de redireccion para la operacion"); //$NON-NLS-1$
			return;
		}

		String origin = null;
		if (connConfig.containsKey(ServiceParams.CONNECTION_PARAM_CERT_ORIGIN)) {
			origin = connConfig.getProperty(ServiceParams.CONNECTION_PARAM_CERT_ORIGIN);
		}

		String appName = null;
		if (connConfig.containsKey(ServiceParams.CONNECTION_PARAM_APPLICATION_NAME)) {
			appName = connConfig.getProperty(ServiceParams.CONNECTION_PARAM_APPLICATION_NAME);
			connConfig.remove(ServiceParams.CONNECTION_PARAM_APPLICATION_NAME);
		}

		String docManagerName = null;
		if (connConfig.containsKey(ServiceParams.CONNECTION_PARAM_DOCUMENT_MANAGER)) {
			docManagerName = connConfig.getProperty(ServiceParams.CONNECTION_PARAM_DOCUMENT_MANAGER);
			connConfig.remove(ServiceParams.CONNECTION_PARAM_DOCUMENT_MANAGER);
		}

        // Creamos la transaccion
        final FireSession session = SessionCollector.createFireSession(request.getSession());
        final String transactionId = session.getTransactionId();

        LOGGER.info("Iniciada transaccion de lote: " + transactionId); //$NON-NLS-1$

        // Guardamos los datos recibidos en la sesion
        session.setAttribute(ServiceParams.SESSION_PARAM_OPERATION, op);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_ID, appId);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_NAME, appName);
        session.setAttribute(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG, connConfig);
        session.setAttribute(ServiceParams.SESSION_PARAM_SUBJECT_ID, subjectId);
        session.setAttribute(ServiceParams.SESSION_PARAM_ALGORITHM, algorithm);
        session.setAttribute(ServiceParams.SESSION_PARAM_EXTRA_PARAM, extraParamsB64);
        session.setAttribute(ServiceParams.SESSION_PARAM_UPGRADE, upgrade);
        session.setAttribute(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION, cop);
        session.setAttribute(ServiceParams.SESSION_PARAM_FORMAT, format);
        session.setAttribute(ServiceParams.SESSION_PARAM_TRANSACTION_ID, transactionId);

        if (origin != null) {
        	session.setAttribute(ServiceParams.SESSION_PARAM_CERT_ORIGIN, origin);
        }

        SessionCollector.commit(session);

        // Obtenemos el DocumentManager con el que recuperar los datos. Si no se especifico ninguno,
        // cargamos el por defecto
        FIReDocumentManager docManager;
        try {
        	docManager = FIReDocumentManagerFactory.newDocumentManager(docManagerName);
        }
        catch (final IllegalArgumentException e) {
        	LOGGER.log(Level.SEVERE, "No existe el gestor de documentos: " + docManagerName, e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No existe el gestor de documentos"); //$NON-NLS-1$
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, "No se ha podido cargar el gestor de documentos con el nombre: " + docManagerName, e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha podido cargar el gestor de documentos"); //$NON-NLS-1$
        	return;
        }

        session.setAttribute(ServiceParams.SESSION_PARAM_DOCUMENT_MANAGER, docManager);

        SessionCollector.commit(session);

        sendResult(response, new CreateBatchResult(transactionId));
	}

	/** Env&iacute;a el resultado al componente cliente. */
	private static void sendResult(final HttpServletResponse response, final CreateBatchResult result) throws IOException {
        response.setContentType("application/json"); //$NON-NLS-1$
        final PrintWriter out = response.getWriter();
        out.print(result.toString());
        out.flush();
        out.close();
	}
}
