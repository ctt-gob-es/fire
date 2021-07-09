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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Properties;
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

	/** @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response) */
	@Override
	protected void service(final HttpServletRequest request,
			               final HttpServletResponse response) throws ServletException,
			                                                          IOException {

		final String redirectKo = request.getParameter("redirectko"); //$NON-NLS-1$
		if (redirectKo == null || "".equals(redirectKo.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha obtenido la pagina de error a la que redirigir"); //$NON-NLS-1$
			response.sendRedirect(ERROR_PAGE);
			return;
		}

		final String transaction = request.getParameter("transactionid"); //$NON-NLS-1$
		if (transaction == null || "".equals(transaction.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha proporcionado id de transaccion"); //$NON-NLS-1$
			response.sendRedirect(redirectKo);
			return;
		}
		final File transactionFile = TestHelper.getCanonicalFile(TestHelper.getDataFolder(), transaction.trim());
		if (!transactionFile.exists() || !transactionFile.canRead()) {
			LOGGER.warning("La transaccion '" + transaction + "' no existe o no es valida"); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendRedirect(redirectKo);
			return;
		}

		final String subjectId = request.getParameter("subjectid"); //$NON-NLS-1$
		if (subjectId == null || "".equals(subjectId.trim())) { //$NON-NLS-1$
			LOGGER.warning("El objeto de transaccion no contiene el identificador del titular"); //$NON-NLS-1$
			response.sendError(
				HttpURLConnection.HTTP_INTERNAL_ERROR,
				"El objeto de transaccion no contiene el identificador del titular" //$NON-NLS-1$
			);
			return;
		}

		final Properties subjectProps = new Properties();
		subjectProps.load(
			TestServiceUserCertAuthServlet.class.getResourceAsStream(
				"/testservice/" + subjectId + ".properties" //$NON-NLS-1$ //$NON-NLS-2$
			)
		);

		if (!TestHelper.subjectKeyStoreExist(subjectId)) {
			try {
				subjectProps.setProperty("password", TestHelper.getSubjectPassword(subjectId)); //$NON-NLS-1$
			}
			catch (final Exception e) {
				LOGGER.warning("No se ha encontrado la contrasena predefinida para el almacen del usuario: " + e); //$NON-NLS-1$
				response.sendRedirect(redirectKo);
				return;
			}
		}

		final String password = request.getParameter("password"); //$NON-NLS-1$

		final String redirectOk = request.getParameter("redirectok"); //$NON-NLS-1$
		if (redirectOk == null || "".equals(redirectOk.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha enviado en la peticion la URL a la que redirigir en caso de exito"); //$NON-NLS-1$
			response.sendRedirect(redirectKo);
			return;
		}

		if (password == null || "".equals(password.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha obtenido en la peticion la contrasena del certificado de firma"); //$NON-NLS-1$
			response.sendRedirect(redirectKo);
			return;
		}

		if (!password.equals(subjectProps.getProperty("password"))) { //$NON-NLS-1$
			LOGGER.warning("La contrasena introducida no es valida para la transaccion " + transaction); //$NON-NLS-1$
			response.sendRedirect(redirectKo);
			return;
		}

		final String subjectRef = request.getParameter(TestServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String origin = request.getParameter(TestServiceParams.HTTP_PARAM_CERT_ORIGIN);
		final String originForced = request.getParameter(TestServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED);

		final String redirectOkWithParams = completeRedirectOkUrl(redirectOk, transaction, subjectRef,
																	origin, originForced, redirectKo);

		response.sendRedirect(redirectOkWithParams);
	}

	private static String completeRedirectOkUrl(final String redirectOkUrl, final String transactionId, final String subjectRef,
			final String origin, final String originForced, final String errorUrl) {
		final StringBuilder res = new StringBuilder(redirectOkUrl);
		res.append("?transactionid=").append(transactionId) //$NON-NLS-1$
		.append("&subjectref=").append(subjectRef) //$NON-NLS-1$
		.append("&certorigin=").append(origin) //$NON-NLS-1$
		.append("&originforced=").append(originForced) //$NON-NLS-1$
		.append("&errorurl=").append(errorUrl); //$NON-NLS-1$

		return res.toString();
	}

}
