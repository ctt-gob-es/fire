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
import javax.servlet.http.HttpSession;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.Responser;

/**
 * Servicio interno para registrar el error de cancelaci&oacute;n de la operaci&oacute;n y
 * redirigir a la pantalla correspondiente.
 */
public class CancelOperationService extends HttpServlet {

	/** SerialId. */
	private static final long serialVersionUID = 2777535691667138137L;

	private static final Logger LOGGER = Logger.getLogger(CancelOperationService.class.getName());

	private static final String URL_ENCODING = "utf-8"; //$NON-NLS-1$

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String userRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		final LogTransactionFormatter logF = new LogTransactionFormatter(null, transactionId);

		// Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

        if (userRef == null || userRef.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado la referencia del usuario")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

        if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado URL de redireccion")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }
		try {
        	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, URL_ENCODING);
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: %1s", e)); //$NON-NLS-1$
		}

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de cancelacion")); //$NON-NLS-1$

		final FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, userRef, request.getSession(false), true, false);
    	if (session == null) {
    		LOGGER.warning(logF.f("La transaccion %1s no se ha inicializado o ha caducado", transactionId)); //$NON-NLS-1$
			redirectToExternalUrl(redirectErrorUrl, request, response);
    		return;
    	}

    	final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
    	logF.setAppId(appId);

		final TransactionConfig connConfig = (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			SessionCollector.removeSession(session);
			LOGGER.warning(logF.f("No se encontro en la sesion la URL de redireccion de la operacion")); //$NON-NLS-1$
			redirectToExternalUrl(redirectErrorUrl, request, response);
			return;
		}

		ErrorManager.setErrorToSession(session, FIReError.OPERATION_CANCELED);

		redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response);

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de cancelacion")); //$NON-NLS-1$
	}

    /**
     * Redirige al usuario a una URL externa y elimina su sesion HTTP, si la
     * tuviese, para borrar cualquier dato que hubiese en ella.
     * @param url URL a la que redirigir al usuario.
     * @param request Objeto de petici&oacute;n realizada al servlet.
     * @param response Objeto de respuesta con el que realizar la redirecci&oacute;n.
     */
    private static void redirectToExternalUrl(final String url, final HttpServletRequest request, final HttpServletResponse response) {

        // Invalidamos la sesion entre el navegador y el componente central porque no se usara mas
    	final HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
        	httpSession.invalidate();
        }

        try {
        	response.sendRedirect(url);
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, "No se ha podido redirigir al usuario a la URL externa", e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.INTERNAL_ERROR);
		}
    }
}
