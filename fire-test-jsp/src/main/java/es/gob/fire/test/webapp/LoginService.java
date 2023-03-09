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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servicio fake como ejemplo de login .
 */
public class LoginService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 1991462934952495784L;

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String userId = request.getParameter("user"); //$NON-NLS-1$
		if (userId == null || userId.isEmpty()) {
			response.sendRedirect("Login.jsp?login=fail"); //$NON-NLS-1$
			return;
		}

		// Solo para pruebas, eliminamos los datos de sesion que pudiese haber de
		// alguna prueba anterior
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		session = request.getSession();
		session.setAttribute("user", userId); //$NON-NLS-1$
		session.setAttribute("UserAgent", request.getHeader("user-agent"));  //$NON-NLS-1$ //$NON-NLS-2$

		response.sendRedirect("SelectOperation.jsp"); //$NON-NLS-1$
	}
}
