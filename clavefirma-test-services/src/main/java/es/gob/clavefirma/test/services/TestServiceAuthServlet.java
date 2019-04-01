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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servicio simulado de autorizaci&oacute;n de firma por Clave.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestServiceAuthServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(TestServiceAuthServlet.class.getName());

	private static final String ERROR_PAGE = "test_pages/ErrorTransaction.html"; //$NON-NLS-1$

	/** @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response) */
	@Override
	protected void service(final HttpServletRequest request,
			               final HttpServletResponse response) throws ServletException,
			                                                          IOException {
		final String transaction = request.getParameter("transactionid"); //$NON-NLS-1$
		if (transaction == null || "".equals(transaction.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha proporcionado id de transaccion"); //$NON-NLS-1$
			response.sendRedirect(ERROR_PAGE);
			return;
		}
		final File transactionFile = new File(
			TestHelper.getDataFolder(),
			transaction.trim()
		);
		if (!transactionFile.exists() || !transactionFile.canRead()) {
			LOGGER.warning("La transaccion '" + transaction + "' no existe o no es valida"); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendRedirect(ERROR_PAGE);
			return;
		}

		final Properties transactionProps = new Properties();
		try (
			final InputStream fis = new FileInputStream(transactionFile);
		) {
			transactionProps.load(fis);
			fis.close();
		}

		final String subjectId = transactionProps.getProperty("subjectid"); //$NON-NLS-1$
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
			TestServiceAuthServlet.class.getResourceAsStream(
				"/testservice/" + subjectId + ".properties" //$NON-NLS-1$ //$NON-NLS-2$
			)
		);

		if (!TestHelper.subjectKeyStoreExist(subjectId)) {
			subjectProps.setProperty("password", TestHelper.getNewSubjectPassword()); //$NON-NLS-1$
		}

		final String password = request.getParameter("password"); //$NON-NLS-1$

		final String redirectKo = request.getParameter("redirectko"); //$NON-NLS-1$
		if (redirectKo == null || "".equals(redirectKo.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha obtenido la pagina de error a la que redirigir"); //$NON-NLS-1$
			response.sendRedirect(ERROR_PAGE);
			return;
		}

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

		transactionProps.put("auth", Boolean.TRUE.toString()); //$NON-NLS-1$
		try (
			final OutputStream fos = new FileOutputStream(transactionFile);
		) {
			transactionProps.store(fos, null);
			fos.close();
		}

		response.sendRedirect(redirectOk);
	}

}
