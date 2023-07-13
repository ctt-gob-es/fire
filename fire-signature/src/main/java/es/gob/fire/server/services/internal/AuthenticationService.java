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

import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.Responser;

/**
 * Servlet que redirige a la autenticacion de usuarios para la obtenci&oacute;n
 * de certificados en la nube.
 */
public class AuthenticationService extends HttpServlet {

	/** Serial ID. */
	private static final long serialVersionUID = -1459483346687635386L;

	private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

	private static final String URL_ENCODING = "utf-8"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		// Obtenemos los datos proporcionados por parametro
		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String origin = request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
		final boolean originForced = Boolean.parseBoolean(request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED));
		String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		final TransactionAuxParams trAux = new TransactionAuxParams(null, transactionId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de seleccion de origen")); //$NON-NLS-1$

		// Comprobamos que se haya indicado la URL a la que redirigir en caso de error
		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}
		try {
        	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, URL_ENCODING);
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: %1s", e)); //$NON-NLS-1$
		}

		// Comprobamos que se haya indicado el identificador de transaccion
		if (transactionId == null || transactionId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Comprobamos que se haya indicado el identificador de usuario
		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia del usuario")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Comprobamos que se haya indicado el proveedor
		if (origin == null || origin.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el proveedor")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Cargamos los datos de sesion
		FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, false, trAux);
		if (session == null) {
			LOGGER.severe(logF.f("No existe sesion vigente asociada a la transaccion")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_SIGN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true, trAux);
		}

		// Terminamos de configurar el formateador para los logs
		final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		trAux.setAppId(appId);

		final TransactionConfig connConfig =
				(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		// Usamos la URL de error indicada en la transaccion
		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.warning(logF.f("No se encontro en la sesion la URL de redireccion de error para la operacion")); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}
		redirectErrorUrl = connConfig.getRedirectErrorUrl();

		FIReConnector connector = null;
		try {
			connector = ProviderManager.getProviderConnector(
					origin,
					connConfig.getProperties()
			);
		} catch (final FIReConnectorFactoryException e) {
			LOGGER.warning(logF.f("Error al obtener el conector")); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		final String subjectId = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);

		String authUrl = ""; //$NON-NLS-1$

		if (connector != null) {

			final String baseUrl = PublicContext.getPublicContext(request);
			final String okRedirectUrl = baseUrl
					+ ServiceNames.PUBLIC_SERVICE_CHOOSE_CERT_ORIGIN
					+ "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_CERT_ORIGIN + "=" + origin //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED + "=" + originForced //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + redirectErrorUrl; //$NON-NLS-1$ //$NON-NLS-2$

			try {
				authUrl = connector.userAutentication(subjectId, okRedirectUrl, connConfig.getRedirectErrorUrl());
			}
			catch (final Exception e) {
	            LOGGER.log(Level.SEVERE, logF.f("Error en la autenticacion del usuario en el proveedor: ") + e, e); //$NON-NLS-1$
	            ErrorManager.setErrorToSession(session, FIReError.PROVIDER_ERROR, originForced, trAux);
	            Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
	        	return;
			}
		} else {
			LOGGER.warning(logF.f("El conector no puede ser nulo")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Registramos que vamos a redirigir al proveedor externo para autenticar al usuario
		session.setAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_LOGIN, Boolean.TRUE);
		SessionCollector.commit(session, trAux);

    	Responser.redirectToExternalUrl(authUrl, request, response, trAux);
	}
}
