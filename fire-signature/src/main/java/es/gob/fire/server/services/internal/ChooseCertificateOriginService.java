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
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.Responser;
import es.gob.fire.signature.ConfigManager;
import es.gob.fire.statistics.entity.Browser;

/**
 * Servlet para la selecci&oacute;n del proveedor local o en la nube con el
 * que se desea firmar.
 */
public class ChooseCertificateOriginService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -1367908808211903695L;

	private static final Logger LOGGER = Logger.getLogger(ChooseCertificateOriginService.class.getName());

	private static final String URL_ENCODING = "utf-8"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		// Obtenemos los datos proporcionados por parametro
		final String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String origin = request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN);
		String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
		final String needAuthUser = request.getParameter(ServiceParams.HTTP_PARAM_NEED_AUTH_USER);

		final TransactionAuxParams trAux = new TransactionAuxParams(null, LogUtils.limitText(trId));
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		// Comprobamos que se haya indicado el identificador de transaccion
		if (trId == null || trId.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el identificador de transaccion")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado el identificador de usuario
		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia del usuario")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		// Comprobamos que se haya indicado la URL a la que redirigir en caso de error
		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}
		try {
			redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, URL_ENCODING);
		} catch (final Exception e) {
			LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: ") + e); //$NON-NLS-1$
		}

		// Cargamos los datos de sesion
		final FireSession session = loadSession(trId, subjectRef, request, trAux);
		if (session == null) {
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Terminamos de configurar el objeto auxiliar de la transaccion
		final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		trAux.setAppId(appId);

		final TransactionConfig connConfig = (TransactionConfig) session
				.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		// Usamos la URL de error indicada en la transaccion
		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.severe(logF.f("No se encontro en la sesion la URL de redireccion de error para la operacion")); //$NON-NLS-1$
			SessionCollector.removeSession(session, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}
		redirectErrorUrl = connConfig.getRedirectErrorUrl();

		// Registramos el navegador usado si no lo estaba ya
		if (!session.containsAttribute(ServiceParams.SESSION_PARAM_BROWSER)) {
			final String userAgent = request.getHeader("user-agent"); //$NON-NLS-1$
			final Browser browser = Browser.identify(userAgent);
			session.setAttribute(ServiceParams.SESSION_PARAM_BROWSER, browser.getName());
		}

		// Comprobamos que se haya indicado el origen del certificado
		if (origin == null || origin.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el origen del certificado")); //$NON-NLS-1$
			ErrorManager.setErrorToSession(session, FIReError.FORBIDDEN, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		// Agregamos a la sesion el origen del certificado
		session.setAttribute(ServiceParams.SESSION_PARAM_CERT_ORIGIN, origin);

		// Indicamos que la ultima operacion de la transaccion fue elegir certificado
		session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_CHOOSE);

		// Si es el proveedor de firma con certificado local, firmamos con el
		if (ProviderManager.PROVIDER_NAME_LOCAL.equalsIgnoreCase(origin)) {
			ProviderBusiness.signWithLocalProvider(session, request, response, trAux);
		}
		// Si no, se tratara de un proveedor de firma en la nube.
		// Si requiere autenticacion previa, la solicitamos
		else if (Boolean.parseBoolean(needAuthUser)) {
			ProviderBusiness.preprocessSignWithCloudProvider(origin, trId, session, request, response, trAux);
		}
		// Si no, firmamos con certificado en la nube
		else {
			ProviderBusiness.signWithCloudProvider(origin, trId, session, request, response, trAux);
		}

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de seleccion de origen")); //$NON-NLS-1$
	}

	private static FireSession loadSession(final String transactionId, final String subjectRef, final HttpServletRequest request,
			final TransactionAuxParams trAux) {

		FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, ConfigManager.isSessionSharingForced(), trAux);
		if (session == null && ConfigManager.isSessionSharingForced()) {
			return null;
		}

		// Si la operacion anterior no fue de solicitud de firma, forzamos a que se
		// recargue por si faltan datos
		if (session == null || SessionFlags.OP_SIGN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			LOGGER.info(trAux.getLogFormatter().f("No se encontro la sesion o no estaba actualizada. Forzamos la carga")); //$NON-NLS-1$
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef,
					request.getSession(false), false, true, trAux);
		}

		return session;
	}
}
