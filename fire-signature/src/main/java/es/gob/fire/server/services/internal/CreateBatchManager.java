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
import es.gob.fire.server.services.FIReServiceOperation;
import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.ServiceUtil;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;
import es.gob.fire.server.services.statistics.TransactionType;
import es.gob.fire.upgrade.UpgraderUtils;

/**
 * Manejador que gestiona las peticiones de creaci&oacute;n de un lote de firma, al que posteriormente
 * se le podr&aacute;an agregar documentos a firmar.
 */
public class CreateBatchManager {

	private static final Logger LOGGER = Logger.getLogger(CreateBatchManager.class.getName());
	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();

	/**
	 * Create un lote de firma.
	 * @param request Petici&oacute;n para la creaci&oacute;n del lote.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la creaci&oacute;n del lote.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void createBatch(final HttpServletRequest request, final String appName,
			final RequestParameters params, final HttpServletResponse response)
		throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String op			= params.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
		final String appId		= params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String subjectId	= params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String cop		= params.getParameter(ServiceParams.HTTP_PARAM_CRYPTO_OPERATION);
		final String algorithm	= params.getParameter(ServiceParams.HTTP_PARAM_ALGORITHM);
		final String format 	= params.getParameter(ServiceParams.HTTP_PARAM_FORMAT);
		final String configB64	= params.getParameter(ServiceParams.HTTP_PARAM_CONFIG);
		final String upgrade	= params.getParameter(ServiceParams.HTTP_PARAM_UPGRADE);
		final String extraParamsB64	= params.getParameter(ServiceParams.HTTP_PARAM_EXTRA_PARAM);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId);

		// Comprobamos que se hayan prorcionado los parametros indispensables
		if (subjectId == null || subjectId.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el identificador del usuario que crea el lote")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se ha proporcionado el identificador del usuario que crea el lote"); //$NON-NLS-1$
			return;
		}

		if (algorithm == null || algorithm.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el algoritmo de firma")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se ha proporcionado el algoritmo de firma"); //$NON-NLS-1$
			return;
		}

		if (cop == null || cop.isEmpty()) {
			LOGGER.warning(logF.f("No se ha indicado la operacion de firma a realizar")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se ha indicado la operacion de firma a realizar"); //$NON-NLS-1$
			return;
		}

		if (format == null || format.isEmpty()) {
			LOGGER.warning(logF.f("No se ha indicado el formato de firma")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se ha indicado el formato de firma"); //$NON-NLS-1$
			return;
		}

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

		// Si se especificaron filtros de certificados para su uso con el
		// Cliente @firma, se habran indicado junto a las propiedades por defecto
		// de configuracion para las firmas del lote. Extraemos los filtros de esas
		// propiedades (si los hubiese) y los almacenamos por separado
		Properties extraParams;
		try {
			extraParams = ServiceUtil.base642Properties(extraParamsB64);
		}
		catch (final Exception e) {
			LOGGER.warning("Se ha proporcionado extraParams mal formados: " + e); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se proporcionaron las URL de redireccion para la operacion"); //$NON-NLS-1$
			return;
		}
		final String filters = MiniAppletHelper.extractCertFiltersParams(extraParams);


		TransactionConfig connConfig = null;
		if (configB64 != null && configB64.length() > 0) {
			try {
				connConfig = new TransactionConfig(configB64);
			}
			catch(final Exception e) {
				LOGGER.warning(logF.f("Se proporcionaron datos malformados para la conexion y configuracion de los proveedores de firma")); //$NON-NLS-1$
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Se proporcionaron datos malformados para la conexion y configuracion de los proveedores de firma"); //$NON-NLS-1$
				return;
			}
		}

		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.warning(logF.f("No se proporcionaron las URL de redireccion para la operacion")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No se proporcionaron las URL de redireccion para la operacion"); //$NON-NLS-1$
			return;
		}

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

		// Extraemos de la configuracion general posibles opciones de configuracion para el sistema de actualizacion
		final Properties upgradeConfig = UpgraderUtils.extractUpdaterProperties(connConfig.getProperties());

        // Creamos la transaccion
        final FireSession session = SessionCollector.createFireSession(request.getSession());
        final String transactionId = session.getTransactionId();

        logF.setTransactionId(transactionId);
		LOGGER.info(logF.f("Iniciada transaccion de tipo LOTE")); //$NON-NLS-1$

        // Guardamos los datos recibidos en la sesion
        session.setAttribute(ServiceParams.SESSION_PARAM_OPERATION, op);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_ID, appId);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_NAME, appName);
        session.setAttribute(ServiceParams.SESSION_PARAM_APPLICATION_TITLE, appTitle);
        session.setAttribute(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG, connConfig.cleanConfig());
        session.setAttribute(ServiceParams.SESSION_PARAM_SUBJECT_ID, subjectId);
        session.setAttribute(ServiceParams.SESSION_PARAM_ALGORITHM, algorithm);
        session.setAttribute(ServiceParams.SESSION_PARAM_EXTRA_PARAM, extraParams);
        session.setAttribute(ServiceParams.SESSION_PARAM_FILTERS, filters != null ? filters.toString() : null);
        session.setAttribute(ServiceParams.SESSION_PARAM_UPGRADE, upgrade);
        session.setAttribute(ServiceParams.SESSION_PARAM_UPGRADE_CONFIG, upgradeConfig);
        session.setAttribute(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION, cop);
        session.setAttribute(ServiceParams.SESSION_PARAM_FORMAT, format);
        session.setAttribute(ServiceParams.SESSION_PARAM_TRANSACTION_ID, transactionId);
        session.setAttribute(ServiceParams.SESSION_PARAM_PROVIDERS, provs);
        session.setAttribute(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE, TransactionType.valueOf(FIReServiceOperation.parse(op)).toString());


        // Obtenemos el DocumentManager con el que recuperar los datos. Si no se especifico ninguno,
        // cargamos el por defecto
        FIReDocumentManager docManager;
        try {
        	docManager = FIReDocumentManagerFactory.newDocumentManager(appId, docManagerName);
        }
        catch (final IllegalAccessException | IllegalArgumentException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El gestor de documentos no existe o no se tiene permiso para acceder a el: " + docManagerName), e); //$NON-NLS-1$
        	TRANSLOGGER.register(session, false);
        	// En el mensaje de error se indica que no existe para no revelar si no existe simplemente es un tema de permisos
        	response.sendError(HttpCustomErrors.INVALID_DOCUMENT_MANAGER.getErrorCode(),
        			HttpCustomErrors.INVALID_DOCUMENT_MANAGER.getErrorDescription());
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el gestor de documentos con el nombre: " + docManagerName), e); //$NON-NLS-1$
        	TRANSLOGGER.register(session, false);
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha podido cargar el gestor de documentos"); //$NON-NLS-1$
        	return;
        }

        LOGGER.info(logF.f("La transaccion usara el DocumentManager " + docManager.getClass().getName())); //$NON-NLS-1$

        session.setAttribute(ServiceParams.SESSION_PARAM_DOCUMENT_MANAGER, docManager);

        SessionCollector.commit(session);

		LOGGER.info(logF.f("Se devuelve el identificador de sesion a la aplicacion")); //$NON-NLS-1$

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
