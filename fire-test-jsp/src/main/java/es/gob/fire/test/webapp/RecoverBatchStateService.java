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

/**
 * Servicio para la obtenci&oacute;n del porcentaje de progreso del lote de firma.
 */
public class RecoverBatchStateService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 8440195378013059743L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RecoverBatchStateService.class);

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(false);
		if ( session == null || session.getAttribute("user") == null) { //$NON-NLS-1$
			response.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		// El identificador de aplicacion es propio de cada aplicacion. En esta de ejemplo,
		// se lee del fichero de configuracion
		final String appId = ConfigManager.getInstance().getAppId();

		final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
		if (transactionId == null) {
			LOGGER.error("No se ha encontrado id de transaccion iniciada"); //$NON-NLS-1$
			response.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		final String userId = (String) session.getAttribute("user"); //$NON-NLS-1$

		float state;
		try {
			state = ConfigManager.getInstance().getFireClient(appId).recoverBatchResultState(transactionId, userId);
		} catch (final Exception e) {
			LOGGER.error("Error durante la evaluacion del estado del lote", e); //$NON-NLS-1$
			response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode(e.getMessage(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Devolvemos el porcentaje
		response.getOutputStream().print((int) (state * 100));
	}
}
