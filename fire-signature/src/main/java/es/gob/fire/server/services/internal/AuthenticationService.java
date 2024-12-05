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
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.Responser;
import es.gob.fire.statistics.entity.Browser;

/**
 * Servlet que redirige a la autenticacion de usuarios para la obtenci&oacute;n
 * de certificados en la nube.
 */
public class AuthenticationService extends HttpServlet {

	/** Serial ID. */
	private static final long serialVersionUID = -1459483346687635386L;

	private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());


	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		// Obtenemos los datos proporcionados por parametro
		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String origin = request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
		final boolean originForced = Boolean.parseBoolean(request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED));

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		final TransactionAuxParams trAux = new TransactionAuxParams(null, transactionId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de seleccion de origen")); //$NON-NLS-1$

		// Comprobamos que se haya indicado el identificador de transaccion
		if (transactionId == null || transactionId.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el identificador de transaccion")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado el identificador de usuario
		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia del usuario")); //$NON-NLS-1$
			SessionCollector.removeSession(transactionId, trAux);
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado el proveedor
		if (origin == null || origin.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el proveedor")); //$NON-NLS-1$
			SessionCollector.removeSession(transactionId, trAux);
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Cargamos los datos de sesion
		final FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true, trAux);
		if (session == null) {
			// Este es uno de los servicios de entrada de la aplicacion una vez redirigido al
			// usuario, por lo que no deberia darse el caso de que la sesion no existiese o
			// hubiese caducado y devolveremos directamente un error en lugar de redirigir a la URL
			// de error
			LOGGER.severe(logF.f("No existe sesion vigente asociada a la transaccion")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.INVALID_TRANSACTION);
			return;
		}

		// Terminamos de configurar el formateador para los logs
		final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		trAux.setAppId(appId);

		// Agregamos a la sesion el origen del certificado
		session.setAttribute(ServiceParams.SESSION_PARAM_CERT_ORIGIN, origin);

		// Si se forzo a ese origen desde la aplicacion, se registra
		if (originForced) {
			session.setAttribute(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED, Boolean.TRUE.toString());
		}

		final TransactionConfig connConfig =
				(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		// Usamos la URL de error indicada en la transaccion
		final String redirectErrorUrl = connConfig.getRedirectErrorUrl();

		// Registramos el navegador usado si no lo estaba ya
		if (!session.containsAttribute(ServiceParams.SESSION_PARAM_BROWSER)) {
			final String userAgent = request.getHeader("user-agent"); //$NON-NLS-1$
			final Browser browser =  Browser.identify(userAgent);
			session.setAttribute(ServiceParams.SESSION_PARAM_BROWSER, browser.getName());
		}

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

			String encodedErrorUrl;
			try {
				encodedErrorUrl = URLEncoder.encode(redirectErrorUrl, "utf-8"); //$NON-NLS-1$
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, logF.f("Error al codificar la URL " + redirectErrorUrl), e); //$NON-NLS-1$
				ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, originForced, trAux);
				redirectToErrorPage(originForced, connConfig, request, response, trAux);
				return;
			}

			final String baseUrl = PublicContext.getPublicContext(request);
			final String okRedirectUrl = baseUrl
					+ ServiceNames.PUBLIC_SERVICE_CHOOSE_CERT_ORIGIN
					+ "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_CERT_ORIGIN + "=" + origin //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED + "=" + originForced //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + encodedErrorUrl; //$NON-NLS-1$ //$NON-NLS-2$

			final String errorRedirectUrl = baseUrl
					+ ServiceNames.PUBLIC_SERVICE_EXTERNAL_ERROR
					+ "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef //$NON-NLS-1$ //$NON-NLS-2$
					+ "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + encodedErrorUrl; //$NON-NLS-1$ //$NON-NLS-2$
			try {
				authUrl = connector.userAutentication(subjectId, okRedirectUrl, errorRedirectUrl);
			} catch (final FIReConnectorNetworkException e) {
				LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
				AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, origin);
				ErrorManager.setErrorToSession(session, FIReError.PROVIDER_INACCESIBLE_SERVICE, originForced, trAux);
				redirectToErrorPage(originForced, connConfig, request, response, trAux);
				return;
			} catch (final FIReConnectorUnknownUserException e) {
				LOGGER.log(Level.WARNING, logF.f("El usuario " + subjectId + " no esta registrado en el sistema: " + e)); //$NON-NLS-1$ //$NON-NLS-2$
				ErrorManager.setErrorToSession(session, FIReError.UNKNOWN_USER, originForced, trAux);
				redirectToErrorPage(originForced, connConfig, request, response, trAux);
				return;
			} catch (final Error e) {
				LOGGER.log(Level.SEVERE,
						logF.f("Error grave, probablemente relacionado con la inicializacion del conector"), e); //$NON-NLS-1$
				ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, originForced, trAux);
				redirectToErrorPage(originForced, connConfig, request, response, trAux);
				return;
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE,
						logF.f("Error indeterminado al recuperar la URL de autenticacion del usuario " + subjectId), e); //$NON-NLS-1$
				ErrorManager.setErrorToSession(session, FIReError.PROVIDER_ERROR, originForced, trAux);
				redirectToErrorPage(originForced, connConfig, request, response, trAux);
				return;
			}
		} else {
			LOGGER.warning(logF.f("El conector no puede ser nulo")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		if (authUrl == null) {
			LOGGER.log(Level.SEVERE,logF.f("No se ha obtenido la URL de redireccion del para la autenticacion del usuario " + subjectId)); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.PROVIDER_ERROR, originForced, trAux);
			redirectToErrorPage(originForced, connConfig, request, response, trAux);
			return;
		}

		// Registramos que vamos a redirigir al proveedor externo para autenticar al usuario
		session.setAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_LOGIN, Boolean.TRUE);
		SessionCollector.commit(session, trAux);

    	Responser.redirectToExternalUrl(authUrl, request, response, trAux);
	}

	/**
	 * Redirige a una p&aacute;gina de error. La p&aacute;gina sera de de error de
	 * firma, si existe la posibilidad de
	 * que se pueda reintentar la operaci&oacute;n, o la p&aacute;gina de error
	 * proporcionada por el usuario.
	 *
	 * @param originForced Indica si era obligatorio el uso de un proveedor de firma
	 *                     concreto.
	 * @param connConfig   Configuraci&oacute;n de la transacci&oacute;n.
	 * @param request      Objeto de petici&oacute;n al servlet.
	 * @param response     Objeto de respuesta del servlet.
	 * @param trAux        Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 *
	 */
	private static void redirectToErrorPage(final boolean originForced, final TransactionConfig connConfig,
			final HttpServletRequest request, final HttpServletResponse response, final TransactionAuxParams trAux) {
		if (originForced) {
			Responser.redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response, trAux);
		} else {
			try {
				request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			} catch (final Exception e) {
				Responser.redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response, trAux);
			}
		}
	}
}
