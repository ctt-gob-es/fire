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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para la recuperaci&oacute;n del resultado de firma de un lote.
 */
public class DownloadSignService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 2571572136476032759L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadSignService.class);

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		final HttpSession session = request.getSession(false);
		try {
			if ( session == null || session.getAttribute("user") == null) { //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

			// Obtenemos el id de transaccion
			final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
			if (transactionId == null) {
				LOGGER.error("No se ha encontrado id de transaccion iniciada"); //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

			final String mimetype = (String) session.getAttribute("sigmimetype"); //$NON-NLS-1$
			final String filename = (String) session.getAttribute("sigfilename"); //$NON-NLS-1$
			final byte[] signature = (byte[]) session.getAttribute("sig"); //$NON-NLS-1$

	    	response.setContentType(mimetype);
	    	response.setHeader("Content-Disposition", "filename=\"" + filename + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    	response.getOutputStream().write(signature);
	    	response.getOutputStream().flush();

		} catch (final Exception e) {
			LOGGER.error("Error interno al recuperar la firma: " + e); //$NON-NLS-1$
			response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode(e.getMessage(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (session != null) {
			session.invalidate();
		}
	}
}
