/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.AplicationsDAO;

/**
 * Servicio para el alta de una nueva aplicaci&oacute;n en el sistema.
 */
public class DeleteAppService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -4783299472733570868L;

	private static final Logger LOGGER = Logger.getLogger(DeleteAppService.class.getName());

	private static final String PARAM_ID = "id-app"; //$NON-NLS-1$

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		final HttpSession session = req.getSession(false);
		if (session == null) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		final String id = req.getParameter(PARAM_ID);

		LOGGER.info("Baja de la app con ID: " + LogUtils.cleanText(id)); //$NON-NLS-1$

		boolean isOk = true;
		if (id == null) {
			isOk = false;
		}
		else {
			try {
				AplicationsDAO.removeApplication(id);
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error al dar de baja la aplicacion", e); //$NON-NLS-1$
				isOk = false;
			}
		}

		resp.sendRedirect("Application/AdminMainPage.jsp?op=baja&r=" + (isOk ? "1" : "0")+"&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
