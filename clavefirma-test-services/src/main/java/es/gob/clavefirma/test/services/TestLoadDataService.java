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

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.connector.LoadResult;
import es.gob.fire.server.connector.TriphaseData;

/**
 * Servlet implementation class TestLoadData
 */
public class TestLoadDataService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(TestLoadDataService.class.getName());

	/** Clave para la propiedad de datos trif&aacute;sicos. */
	public static final String KEY_TRIPHASEDATA = "triphasedata"; //$NON-NLS-1$

	/** Clave para la propiedad de identificador del titular. */
	public static final String KEY_SUBJECTID ="subjectid"; //$NON-NLS-1$

	/** Clave para la propiedad de algoritmo de firma. */
	public static final String KEY_ALGORITHM = "algorithm"; //$NON-NLS-1$

	/** Clave para la propiedad de certificado de firma. */
	public static final String KEY_CERTIFICATE = "certificate"; //$NON-NLS-1$

	/** Clave para el par&aacute;metro de URL de redireccion en caso de &eacute;xito. */
	public static final String KEY_REDIRECT_OK = "urlok"; //$NON-NLS-1$

	/** Clave para el par&aacute;metro de URL de redireccion en caso de error. */
	public static final String KEY_REDIRECT_ERROR = "urlerror"; //$NON-NLS-1$

	/** Clave para indicar que el certificado de usuario no existe y se debe cargar el "nuevo" certificado. */
	public static final String KEY_NEWCERT = "newcert"; //$NON-NLS-1$

	/** Clave para indicar la informaci&oacute;n de los documentos */
	public static final String KEY_INFODOCUMENTOS = "infoDocumentos"; //$NON-NLS-1$


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        final String subjectId = request.getParameter(KEY_SUBJECTID);
		final String algorithm = request.getParameter(KEY_ALGORITHM);
		final String certB64UrlSafe = request.getParameter(KEY_CERTIFICATE);
		final String triDataB64UrlSafe = request.getParameter(KEY_TRIPHASEDATA);
		final String redirectOkUrlB64UrlSafe = request.getParameter(KEY_REDIRECT_OK);
		final String redirectErrorUrlB64UrlSafe = request.getParameter(KEY_REDIRECT_ERROR);
		final String infoDocumentosB64 = request.getParameter(KEY_INFODOCUMENTOS);

		try{
			try {
				TestHelper.doSubjectExist(subjectId);
			}
			catch (final InvalidUserException e) {
				LOGGER.log(Level.WARNING, "El usuario " + subjectId + " no existe", e); //$NON-NLS-1$ //$NON-NLS-2$
				response.sendError(CustomHttpErrors.HTTP_ERROR_UNKNOWN_USER, "El usuario no existe"); //$NON-NLS-1$
				response.flushBuffer();
				return;
			}
	
			final Properties p = new Properties();
			p.put(KEY_TRIPHASEDATA, triDataB64UrlSafe.replace('-', '+').replace('_', '/'));
			p.put(KEY_ALGORITHM, algorithm != null ? algorithm : "SHA1withRSA"); //$NON-NLS-1$
			p.put(KEY_SUBJECTID, subjectId);
			p.put(KEY_CERTIFICATE, certB64UrlSafe.replace('-', '+').replace('_', '/'));
	
			if (!TestHelper.subjectKeyStoreExist(subjectId)) {
				p.put(KEY_NEWCERT, Boolean.TRUE.toString());
			}
	
			final String transactionId = UUID.randomUUID().toString();
			final File transactionFile = new File(TestHelper.getDataFolder(), transactionId);
			try (final OutputStream fos = new FileOutputStream(transactionFile)) {
				p.store(fos, ""); //$NON-NLS-1$
			}
	
			final String redirectUrl = TestHelper.getSignRedirectionUrl(subjectId, transactionId, redirectOkUrlB64UrlSafe, redirectErrorUrlB64UrlSafe, infoDocumentosB64);
	
			final LoadResult result = new LoadResult(
				transactionId,
				redirectUrl,
				TriphaseData.parser(Base64.decode(triDataB64UrlSafe, true))
			);
	
			response.setCharacterEncoding(TestHelper.DEFAULT_ENCODING.displayName());
			response.getOutputStream().write(result.encodeResult(TestHelper.DEFAULT_ENCODING));
			response.flushBuffer();
		}
		catch (final Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("Error interno: " + e.getMessage()); //$NON-NLS-1$
				response.flushBuffer();
			} catch (IOException ioe) {
				LOGGER.warning("Ha ocurrido un error al tratar de pasar el mensaje de error a la respuesta. Error: " + ioe);
			}
		}
	}

}
