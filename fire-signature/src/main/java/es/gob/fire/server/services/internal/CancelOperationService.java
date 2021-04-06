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
import java.net.URLDecoder;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String userId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);

		final LogTransactionFormatter logF = new LogTransactionFormatter(null, trId);

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de cancelacion")); //$NON-NLS-1$

		final FireSession session = SessionCollector.getFireSession(trId, userId, request.getSession(false), true, false);
    	if (session == null) {
    		LOGGER.warning(logF.f("La transaccion %1s no se ha inicializado o ha caducado", trId)); //$NON-NLS-1$

    		String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
    		if (redirectErrorUrl != null) {
    			try {
                	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, URL_ENCODING);
                }
                catch (final Exception e) {
                	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: %1s", e)); //$NON-NLS-1$
        		}
    			response.sendRedirect(redirectErrorUrl);
    		}
    		else {
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "La transaccion no se ha inicializado o ha caducado"); //$NON-NLS-1$
    		}
    		return;
    	}

    	final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
    	logF.setAppId(appId);

		final TransactionConfig connConfig = (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			SessionCollector.removeSession(session);
			LOGGER.warning(logF.f("No se pudo obtener la configuracion de la transaccion")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se proporcionaron datos para la conexion con el backend"); //$NON-NLS-1$
			return;
		}

		ErrorManager.setErrorToSession(session, OperationError.OPERATION_CANCELED);

		response.sendRedirect(connConfig.getRedirectErrorUrl());

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de cancelacion")); //$NON-NLS-1$
	}
}
