/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.test.webapp;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.SignOperationResult;

/**
 * Servicio para dar proceso de firma de un lote.
 */
public class SignBatchService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -5629277386563840791L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SignBatchService.class);

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		final HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) { //$NON-NLS-1$
			response.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		// El identificador de aplicacion es propio de cada aplicacion. En esta de
		// ejemplo,
		// se lee del fichero de configuracion
		final String appId = ConfigManager.getInstance().getAppId();

		final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
		if (transactionId == null) {
			LOGGER.error("No se ha encontrado id de transaccion iniciada"); //$NON-NLS-1$
			response.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		final String userId = (String) session.getAttribute("user"); //$NON-NLS-1$
		
		final String language = (String) session.getAttribute("language"); //$NON-NLS-1$

		final boolean stopOnError = request.getParameter("stoponerror") != null; //$NON-NLS-1$	

		SignOperationResult signOperationResult;
		try {
			signOperationResult = ConfigManager.getInstance().getFireClient(appId).signBatch(transactionId, userId, language,
					stopOnError);
		} catch (final Exception e) {
			LOGGER.error("Error durante la operacion de firma del lote", e); //$NON-NLS-1$
			final String message = URLEncoder.encode(
					"Error en la llamada a la operacion de firma de lote:<br>" + e.getMessage(), //$NON-NLS-1$
					"UTF-8"); //$NON-NLS-1$
			response.sendRedirect("ErrorPage.jsp?msg=" + message); //$NON-NLS-1$
			return;
		}

		response.sendRedirect(signOperationResult.getRedirectUrl());
	}
}
