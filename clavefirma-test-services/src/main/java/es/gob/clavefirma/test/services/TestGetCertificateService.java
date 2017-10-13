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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.signature.connector.FIReCertificateException;
import es.gob.fire.signature.connector.WeakRegistryException;

/**
 * Servlet implementation class TestGestCertificateService
 */
public class TestGetCertificateService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(TestGetCertificateService.class.getName());

	/** Clave para la propiedad de identificador del tutular. */
	public static final String KEY_SUBJECTID ="subjectid"; //$NON-NLS-1$

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String subjectId = request.getParameter(KEY_SUBJECTID);

		final KeyStore ks;
		try {
			ks = TestHelper.getKeyStore(subjectId);
		}
		catch (final KeyStoreException e) {
			LOGGER.log(Level.SEVERE, "Error obteniendo el KeyStore: " + e, e); //$NON-NLS-1$
			final Exception cause = new FIReCertificateException(
					"Error obteniendo el KeyStore: " + e, e //$NON-NLS-1$
				);
			throw new ServletException(cause);
		}
		catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, "Error obteniendo el KeyStore por algoritmo no soportado: " + e); //$NON-NLS-1$
			final Exception cause = new FIReCertificateException(
					"Error obteniendo el KeyStore por algoritmo no soportado: " + e, e //$NON-NLS-1$
				);
			throw new ServletException(cause);
		}
		catch (final CertificateException e) {
			LOGGER.log(Level.SEVERE, "Error obteniendo el KeyStore por certificado corrupto: " + e); //$NON-NLS-1$
			final Exception cause = new FIReCertificateException(
					"Error obteniendo el KeyStore por certificado corrupto: " + e, e //$NON-NLS-1$
				);
			throw new ServletException(cause);
		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Error obteniendo el KeyStore en la lectura de datos: " + e, e); //$NON-NLS-1$
			final Exception cause = new FIReCertificateException(
					"Error obteniendo el KeyStore en la lectura de datos: " + e, e //$NON-NLS-1$
				);
			throw new ServletException(cause);
		}
		catch (final FIReCertificateException e) {
			LOGGER.log(Level.WARNING, "El usuario " + subjectId + " no tiene certificados: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendError(CustomHttpErrors.HTTP_ERROR_NO_CERT, "El usuario no tiene certificados"); //$NON-NLS-1$
			response.flushBuffer();
			return;
		}
		catch (final InvalidUserException e) {
			LOGGER.log(Level.WARNING, "El usuario " + subjectId + " no existe", e); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendError(CustomHttpErrors.HTTP_ERROR_UNKNOWN_USER, "El usuario no existe"); //$NON-NLS-1$
			response.flushBuffer();
			return;
		}
		catch (final BlockedCertificateException e) {
			LOGGER.log(Level.WARNING, "El usuario " + subjectId + " tiene certificados bloqueados", e); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendError(CustomHttpErrors.HTTP_ERROR_BLOCKED_CERT, "El usuario tiene certificados bloqueados"); //$NON-NLS-1$
			response.flushBuffer();
			return;
		}
		catch (final WeakRegistryException e) {
			LOGGER.log(Level.WARNING, "El usuario " + subjectId + " realizo un registro debil y no puede tener certificados de firma", e); //$NON-NLS-1$ //$NON-NLS-2$
			response.sendError(CustomHttpErrors.HTTP_ERROR_WEAK_REGISTRY, "El usuario realizo un registro debil y no puede tener certificados de firma"); //$NON-NLS-1$
			response.flushBuffer();
			return;
		}

		Enumeration<String> aliases;
		try {
			aliases = ks.aliases();
		}
		catch (final KeyStoreException e) {
			LOGGER.log(Level.WARNING, "Error obteniendo los certificados del KeyStore: " + e, e); //$NON-NLS-1$
			final Exception cause = new FIReCertificateException(
					"Error obteniendo los certificados del KeyStore: " + e, e //$NON-NLS-1$
				);
			throw new ServletException(cause);
		}
		final List<X509Certificate> certs = new ArrayList<>();
		while(aliases.hasMoreElements()) {
			final String alias = aliases.nextElement();
			try {
				certs.add((X509Certificate) ks.getCertificate(alias));
			}
			catch (final KeyStoreException e) {
				LOGGER.log(Level.WARNING, "Error obteniendo los certificados del KeyStore: " + e, e); //$NON-NLS-1$
				final Exception cause = new FIReCertificateException(
						"Error obteniendo el certificado " + alias + " del KeyStore: " + e, e //$NON-NLS-1$ //$NON-NLS-2$
					);
				throw new ServletException(cause);
			}
		}

		final StringBuilder jsonResponse = new StringBuilder("["); //$NON-NLS-1$
		for (int i = 0; i < certs.size(); i++) {
			byte[] certEncoded;
			try {
				certEncoded = certs.get(i).getEncoded();
			}
			catch (final CertificateEncodingException e) {
				LOGGER.severe("No se pudo recuperar uno de los certificados del usuario: " + e); //$NON-NLS-1$
				continue;
			}
			jsonResponse.append("\"").append(Base64.encode(certEncoded)).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
			if (i < certs.size() - 1) {
				jsonResponse.append(","); //$NON-NLS-1$
			}
		}
		jsonResponse.append("]"); //$NON-NLS-1$
		response.getWriter().print(jsonResponse.toString());
		response.flushBuffer();
	}
}
