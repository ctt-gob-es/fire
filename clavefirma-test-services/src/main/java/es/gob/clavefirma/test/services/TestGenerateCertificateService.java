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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.WeakRegistryException;

/**
 * Servicio para la solicitud de un nuevo certificado de pruebas.
 */
public class TestGenerateCertificateService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/** Clave para la propiedad de identificador del titular. */
	public static final String KEY_SUBJECTID ="subjectid"; //$NON-NLS-1$

	/** Clave para el par&aacute;metro de URL de redireccion en caso de &eacute;xito. */
	public static final String KEY_REDIRECT_OK = "urlok"; //$NON-NLS-1$

	/** Clave para el par&aacute;metro de URL de redireccion en caso de error. */
	public static final String KEY_REDIRECT_ERROR = "urlerror"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(TestGenerateCertificateService.class.getName());

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String subjectId = request.getParameter(KEY_SUBJECTID);
		final String redirectOkUrlB64UrlSafe = request.getParameter(KEY_REDIRECT_OK);
		final String redirectErrorUrlB64UrlSafe = request.getParameter(KEY_REDIRECT_ERROR);

		try {

			try {
				TestHelper.checkCanGenerateCert(subjectId);
			}
			catch (final InvalidUserException e) {
				LOGGER.log(Level.WARNING, "El usuario " + subjectId + " no existe", e); //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(CustomHttpErrors.HTTP_ERROR_UNKNOWN_USER);
				response.getWriter().write("El usuario no existe"); //$NON-NLS-1$
				response.flushBuffer();
				return;
			}
			catch (final FIReCertificateAvailableException e) {
				LOGGER.log(Level.WARNING, "El usuario " + subjectId + " ya tiene certificado de firma", e); //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(CustomHttpErrors.HTTP_ERROR_EXISTING_CERTS);
				response.getWriter().write("El usuario ya tiene certificado de firma"); //$NON-NLS-1$
				response.flushBuffer();
				return;
			}
			catch (final WeakRegistryException e) {
				LOGGER.log(Level.WARNING, "El usuario " + subjectId + " no puede generar certificados por haber hecho un registro debil", e); //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(CustomHttpErrors.HTTP_ERROR_WEAK_REGISTRY);
				response.getWriter().write("El usuario no puede generar certificados por haber hecho un registro debil"); //$NON-NLS-1$
				response.flushBuffer();
				return;
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "Error en la peticion", e); //$NON-NLS-1$
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Error en los parametros de la peticion"); //$NON-NLS-1$
				response.flushBuffer();
				return;
			}

			final Properties p = new Properties();
			p.put(KEY_SUBJECTID, subjectId);

			final String transactionId = UUID.randomUUID().toString();


			final File transactionFile = new File(TestHelper.getDataFolder(), transactionId);
			try (final OutputStream fos = new FileOutputStream(transactionFile)) {
				p.store(fos, ""); //$NON-NLS-1$
			}

			final String redirect = TestHelper.getCertificateRedirectionUrl(subjectId, transactionId, redirectOkUrlB64UrlSafe, redirectErrorUrlB64UrlSafe);

			final GenerateCertificateResult result = new GenerateCertificateResult(transactionId, redirect);
			response.getOutputStream().write(result.encodeResult(TestHelper.DEFAULT_ENCODING));
			response.flushBuffer();
		}
		catch (final Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("Error interno: " + e.getMessage()); //$NON-NLS-1$
			response.flushBuffer();

		}
	}
}
