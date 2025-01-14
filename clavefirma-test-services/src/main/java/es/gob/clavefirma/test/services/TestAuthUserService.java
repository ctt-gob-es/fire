/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.test.services;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestGestCertificateService
 */
public class TestAuthUserService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(TestAuthUserService.class.getName());

	/** Clave para la propiedad de identificador del tutular. */
	public static final String PARAM_SUBJECT_ID ="subjectid"; //$NON-NLS-1$
	/** Clave para la propiedad de la URL de redirecci&oacute;n en caso de error. */
	public static final String PARAM_ERROR_URL ="redirectko"; //$NON-NLS-1$

	private static final String EXT_ERR_UNKNOWN_USER = "02"; //$NON-NLS-1$
	private static final String EXT_ERR_BLOCKED_USER = "03"; //$NON-NLS-1$

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String subjectId = request.getParameter(PARAM_SUBJECT_ID);
		final String errorUrl = request.getParameter(PARAM_ERROR_URL);
		String errorParams = errorUrl.contains("?") ? "&" : "?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		errorParams += "errortype="; //$NON-NLS-1$

//		try {
//			TestHelper.checkSubject(subjectId);
//		}
//		catch (final InvalidUserException e) {
//			LOGGER.log(Level.WARNING, "El usuario " + subjectId + " no existe", e); //$NON-NLS-1$ //$NON-NLS-2$
//			response.sendRedirect(errorUrl + errorParams + EXT_ERR_UNKNOWN_USER);
//			return;
//		}
//		catch (final BlockedCertificateException e) {
//			LOGGER.log(Level.WARNING, "El usuario " + subjectId + " tiene el certificado bloqueado", e); //$NON-NLS-1$ //$NON-NLS-2$
//			response.sendRedirect(errorUrl + errorParams + EXT_ERR_BLOCKED_USER);
//			return;
//		}
//		catch (final Exception e) {
//			// El resto de errores no nos afecta, asi que seguimos con la ejecuion
//		}

		request.getRequestDispatcher("test_pages/TestUserCertAuth.jsp").forward(request, response); //$NON-NLS-1$
	}
}
