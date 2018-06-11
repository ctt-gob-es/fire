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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.connector.FIReSignatureException;

/**
 * Servicio para la recuperacion de un certificado recien creado.
 */
public class TestRecoverCertificateService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(TestGenerateCertificateService.class.getName());

	/** Clave para la propiedad de certificado de firma. */
	private static final String KEY_CERTIFICATE = "certificate"; //$NON-NLS-1$

	/** Clave para la propiedad de identificador de la transaccion. */
	private static final String KEY_TRANSACTIONID = "transactionid"; //$NON-NLS-1$

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String transactionId = request.getParameter(KEY_TRANSACTIONID);

		final File transactionFile = new File(TestHelper.getDataFolder(), transactionId);
		if (!transactionFile.isFile() || !transactionFile.canRead()) {
			LOGGER.log(Level.WARNING, "La transaccion " + transactionId + " no existe o no es valida"); //$NON-NLS-1$ //$NON-NLS-2$
			final Exception e = new FIReSignatureException("La transaccion " + transactionId + " no existe o no es valida"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new ServletException(e);
		}

		final Properties p;
		try {
			final InputStream fis = new FileInputStream(transactionFile);
			p = new Properties();
			p.load(fis);
			fis.close();
		}
		catch(final IOException e) {
			LOGGER.log(Level.WARNING, "Error cargando la transaccion: " + e, e); //$NON-NLS-1$
			final Exception ex = new FIReSignatureException(
				"Error cargando la transaccion: " + e, e //$NON-NLS-1$
			);
			throw new ServletException(ex);
		}

		// Eliminamos el fichero de la transaccion
		transactionFile.delete();

		if (!Boolean.parseBoolean(p.getProperty("auth"))) { //$NON-NLS-1$
			LOGGER.log(Level.WARNING, "La transaccion " + transactionId + " no esta autorizada"); //$NON-NLS-1$ //$NON-NLS-2$
			final Exception ex = new FIReSignatureException("La transaccion " + transactionId + " no esta autorizada"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new ServletException(ex);
		}

		final byte[] certEncoded = Base64.decode(p.getProperty(KEY_CERTIFICATE));
		try {
			CertificateFactory.getInstance("X.509") //$NON-NLS-1$
				.generateCertificate(new ByteArrayInputStream(certEncoded));
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "La transaccion " + transactionId + " no contiene un certificado valido: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			final Exception ex = new FIReSignatureException(
				"La transaccion " + transactionId + " no contiene un certificado valido: " + e, e //$NON-NLS-1$ //$NON-NLS-2$
			);
			throw new ServletException(ex);
		}

		final StringBuilder result = buildJsonResult(true, certEncoded);

		response.getOutputStream().print(result.toString());
		response.getOutputStream().flush();
	}

	private static StringBuilder buildJsonResult(final boolean ok, final byte[] certEncoded) {
		return new StringBuilder()
		.append("{\n") //$NON-NLS-1$
		.append("\"result\":\"").append(ok ? "OK" : "KO").append("\",\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append("\"cert\":\"").append(Base64.encode(certEncoded)).append("\"") //$NON-NLS-1$ //$NON-NLS-2$
		.append("\n}"); //$NON-NLS-1$
	}
}
