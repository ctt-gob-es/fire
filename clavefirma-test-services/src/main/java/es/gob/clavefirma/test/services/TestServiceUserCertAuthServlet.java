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
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servicio simulado de autenticaci&oacute;n de obtenci&oacute;n de certificados de la nube.*/
public final class TestServiceUserCertAuthServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(TestServiceUserCertAuthServlet.class.getName());

	private static final String ERROR_PAGE = "test_pages/ErrorTransaction.html"; //$NON-NLS-1$

	private static final String EXT_ERR_UNKOWN_ERROR = "00"; //$NON-NLS-1$
	private static final String EXT_ERR_AUTENTICATION_ERROR = "01"; //$NON-NLS-1$
	private static final String EXT_ERR_UNKNOWN_USER = "02"; //$NON-NLS-1$
	private static final String EXT_ERR_BLOCKED_USER = "03"; //$NON-NLS-1$

	/** @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response) */
	@Override
	protected void service(final HttpServletRequest request,
			               final HttpServletResponse response) throws ServletException,
			                                                          IOException {

		String redirectKo = request.getParameter("redirectko"); //$NON-NLS-1$
		if (redirectKo == null || "".equals(redirectKo.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha obtenido la pagina de error a la que redirigir"); //$NON-NLS-1$
			response.sendRedirect(ERROR_PAGE);
			return;
		}

		try {
			redirectKo = URLDecoder.decode(redirectKo, "utf-8"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "No se pudo decodificar la URL de redireccion de error. Se usara la URL sin decodificar", e); //$NON-NLS-1$
		}

		String errorTypeParam = redirectKo.contains("?") ? "&" : "?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		errorTypeParam += "errortype="; //$NON-NLS-1$


		final String subjectId = request.getParameter("subjectid"); //$NON-NLS-1$
		if (subjectId == null || "".equals(subjectId.trim())) { //$NON-NLS-1$
			LOGGER.warning("El objeto de transaccion no contiene el identificador del titular"); //$NON-NLS-1$
			response.sendRedirect(redirectKo + errorTypeParam + EXT_ERR_UNKOWN_ERROR);
			return;
		}

		final String redirectOk = request.getParameter("redirectok"); //$NON-NLS-1$
		if (redirectOk == null || "".equals(redirectOk.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha enviado en la peticion la URL a la que redirigir en caso de exito"); //$NON-NLS-1$
			response.sendRedirect(redirectKo + errorTypeParam + EXT_ERR_UNKOWN_ERROR);
			return;
		}

		final String password = request.getParameter("password"); //$NON-NLS-1$
		if (password == null || "".equals(password.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha obtenido en la peticion la contrasena del certificado de firma"); //$NON-NLS-1$
			response.sendRedirect(redirectKo + errorTypeParam + EXT_ERR_UNKOWN_ERROR);
			return;
		}

		final Properties subjectProps = new Properties();
		try (InputStream is = TestServiceUserCertAuthServlet.class.getResourceAsStream("/testservice/" + subjectId + ".properties")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (is == null) {
				LOGGER.log(Level.WARNING, "El usuario " + subjectId + " no existe"); //$NON-NLS-1$ //$NON-NLS-2$
				response.sendRedirect(redirectKo + errorTypeParam + EXT_ERR_UNKNOWN_USER);
				return;
			}
			subjectProps.load(is);
		}

		if (!TestHelper.subjectKeyStoreExist(subjectId)) {
			try {
				subjectProps.setProperty("password", TestHelper.getSubjectPassword(subjectId)); //$NON-NLS-1$
			}
			catch (final Exception e) {
				LOGGER.warning("No se ha encontrado la contrasena predefinida para el almacen del usuario: " + e); //$NON-NLS-1$
				response.sendRedirect(redirectKo + errorTypeParam + EXT_ERR_UNKNOWN_USER);
				return;
			}
		}

		if (!password.equals(subjectProps.getProperty("password"))) { //$NON-NLS-1$
			LOGGER.warning("La contrasena introducida no es valida"); //$NON-NLS-1$
			response.sendRedirect(redirectKo + errorTypeParam + EXT_ERR_AUTENTICATION_ERROR);
			return;
		}

		final String redirectOkWithParams = completeRedirectOkUrl(redirectOk, redirectKo);

		response.sendRedirect(redirectOkWithParams);
	}

	/**
	 * M&eacute;todo para completar la URL de redirecci&oacute;n en caso de &eacute;xito.
	 * @param redirectOkUrl URL a completar.
	 * @param errorUrl URL a unir para casos de error.
	 * @return URL de redirecci&oacute;n en caso de &eacute;xito completa.
	 */
	private static String completeRedirectOkUrl(final String redirectOkUrl,
			 final String errorUrl) {
		final StringBuilder res = new StringBuilder(redirectOkUrl);
		res.append(redirectOkUrl.contains("?") ? "&" : "?").append("errorurl=").append(errorUrl); //$NON-NLS-1$

		return res.toString();
	}

}
