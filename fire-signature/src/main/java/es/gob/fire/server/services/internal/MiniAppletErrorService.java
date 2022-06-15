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
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servicio para procesar los errores encontrados por el MiniApplet y los clientes nativos.
 */
public class MiniAppletErrorService extends HttpServlet {


	/** Serial Id. */
	private static final long serialVersionUID = 6742462095455032887L;

	private static final Logger LOGGER = Logger.getLogger(MiniAppletErrorService.class.getName());

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		// Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final LogTransactionFormatter logF = new LogTransactionFormatter(null, transactionId);

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de error de firma con certificado local")); //$NON-NLS-1$

        final String userId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);

        String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
        if (redirectErrorUrl != null && !redirectErrorUrl.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado la URL de redireccion de error")); //$NON-NLS-1$
            SessionCollector.removeSession(transactionId);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Se recibe el tipo de error pero por ahora no hacemos nada con el
		//final String errorType = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_TYPE);
		final String errorMessage = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_MESSAGE);

		final FireSession session = SessionCollector.getFireSession(transactionId, userId, request.getSession(false), true, false);
		if (session == null) {
			LOGGER.warning(logF.f("La transaccion %1s no se ha inicializado o ha caducado", transactionId)); //$NON-NLS-1$
        	SessionCollector.removeSession(transactionId);
        	redirectToExternalUrl(redirectErrorUrl, request, response);
    		return;
        }

        // Obtenenmos la configuracion del conector
        final TransactionConfig connConfig	= (TransactionConfig) session
        		.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
        if (connConfig != null && connConfig.isDefinedRedirectErrorUrl()) {
			redirectErrorUrl = connConfig.getRedirectErrorUrl();
		}

        ErrorManager.setErrorToSession(session, OperationError.SIGN_LOCAL, true, errorMessage);

    	// Redirigimos a la pagina de error
        redirectToExternalUrl(redirectErrorUrl, request, response);

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de error de firma con certificado local")); //$NON-NLS-1$
	}

    /**
     * Redirige al usuario a una URL externa y elimina su sesion HTTP, si la
     * tuviese, para borrar cualquier dato que hubiese en ella.
     * @param url URL a la que redirigir al usuario.
     * @param request Objeto de petici&oacute;n realizada al servlet.
     * @param response Objeto de respuesta con el que realizar la redirecci&oacute;n.
     * @throws IOException Cuando no se puede redirigir al usuario.
     */
    private static void redirectToExternalUrl(final String url, final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        // Invalidamos la sesion entre el navegador y el componente central porque no se usara mas
    	final HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
        	httpSession.invalidate();
        }

    	response.sendRedirect(url);
    }
}
