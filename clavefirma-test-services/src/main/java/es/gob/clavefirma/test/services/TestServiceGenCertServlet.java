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
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;

/** Servicio simulado de autorizaci&oacute;n de firma por Clave.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestServiceGenCertServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PROP_SUBJECTID = "subjectid"; //$NON-NLS-1$

	private static final String KEY_AUTH = "auth"; //$NON-NLS-1$

	private static final String KEY_CERTIFICATE = "certificate"; //$NON-NLS-1$

	private static final String ERROR_PAGE = "test_pages/ErrorTransaction.html"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(TestServiceGenCertServlet.class.getName());

	/** @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response) */
	@Override
	protected void service(final HttpServletRequest request,
			               final HttpServletResponse response) throws ServletException,
			                                                          IOException {

		LOGGER.info("Solicitud de recuperacion de certificado desde la web de prueba"); //$NON-NLS-1$

		final String redirectKo = request.getParameter("redirectko"); //$NON-NLS-1$
		if (redirectKo == null || "".equals(redirectKo.trim())) { //$NON-NLS-1$
			LOGGER.severe("No se ha proporcionado URL de retorno de error"); //$NON-NLS-1$
			response.sendRedirect(ERROR_PAGE);
			return;
		}

		final String redirectOk = request.getParameter("redirectok"); //$NON-NLS-1$
		if (redirectOk == null || "".equals(redirectOk.trim())) { //$NON-NLS-1$
			LOGGER.severe("No se ha proporcionado URL de retorno de exito"); //$NON-NLS-1$
			response.sendRedirect(redirectKo);
			return;
		}

		final String transaction = request.getParameter("transactionid"); //$NON-NLS-1$
		if (transaction == null || "".equals(transaction.trim())) { //$NON-NLS-1$
			LOGGER.warning("No se ha recibido el identificador de transaccion: " + "' no existe o no es valida"); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendRedirect(redirectKo);
			return;
		}
		final File transactionFile = TestHelper.getCanonicalFile(TestHelper.getDataFolder(), transaction.trim());
		if (!transactionFile.exists() || !transactionFile.canRead()) {
			LOGGER.warning("La transaccion '" + transaction + "' no existe o no es valida"); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendRedirect(redirectKo);
			return;
		}

		// Ignoramos los datos de la transaccion en cuestion y usamos un usuario con un certificado
		// que hara las veces de nuevo certificado

		final Properties transactionProps = new Properties();
		try (FileInputStream fis = new FileInputStream(transactionFile)) {
			transactionProps.load(fis);
		}
		final String subjectId = transactionProps.getProperty(PROP_SUBJECTID);

		// Extraemos el certificado
		byte[] certEncoded;
		try {
			final KeyStore ks = TestHelper.getKeyStore(subjectId, false);
			certEncoded = ((X509Certificate) ks.getCertificate(ks.aliases().nextElement())).getEncoded();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo extraer el certificado del almacen del usuario", e); //$NON-NLS-1$
			response.sendRedirect(redirectKo);
			return;
		}

		transactionProps.setProperty(KEY_CERTIFICATE, Base64.encode(certEncoded));
		transactionProps.setProperty(KEY_AUTH, Boolean.TRUE.toString());

		try (final OutputStream fos = new FileOutputStream(transactionFile)) {
			transactionProps.store(fos, null);
		}

		response.sendRedirect(redirectOk);
	}

}
