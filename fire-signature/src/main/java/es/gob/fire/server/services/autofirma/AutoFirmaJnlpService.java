/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.autofirma;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.signature.ConfigManager;

/**
 * Servicio para la obtenci&oaucte;n de un fichero de despliegue JNLP
 * al que se le proporciona un argumento proporcionado en la llamada.
 */
public class AutoFirmaJnlpService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String TEMPLATE_FILE = "autofirma.jnlp"; //$NON-NLS-1$

	private static final String PARAM_ARGUMENT = "arg"; //$NON-NLS-1$
	private static final String PARAM_OS_NAME = "os"; //$NON-NLS-1$

	private static final String OS_WINDOWS = "windows"; //$NON-NLS-1$
	private static final String OS_LINUX = "linux"; //$NON-NLS-1$
	private static final String OS_MAC = "mac"; //$NON-NLS-1$

	private static final String TEMPLATE_REPLACE_OS_JAR_REFERENCE = "%OSJAR%"; //$NON-NLS-1$
	private static final String TEMPLATE_REPLACE_OS_NAME = "%OSNAME%"; //$NON-NLS-1$
	private static final String TEMPLATE_REPLACE_CODEBASE = "%CODEBASE%"; //$NON-NLS-1$
	private static final String TEMPLATE_REPLACE_ARGUMENT = "%ARGUMENT%"; //$NON-NLS-1$

	private static final String TEMPLATE_NODE_REFERENCE = "<jar href=\"" + TEMPLATE_REPLACE_CODEBASE + //$NON-NLS-1$
			TEMPLATE_REPLACE_OS_NAME + ".jar\"/>"; //$NON-NLS-1$

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String arg = request.getParameter(PARAM_ARGUMENT);
		final String osName = request.getParameter(PARAM_OS_NAME);
		// Devolvemos al usuario la pagina a la que debe dirigir al usuario
		String publicCodeBase = ConfigManager.getPublicContextUrl();
		if (publicCodeBase == null || publicCodeBase.isEmpty()){
			publicCodeBase = request.getRequestURL().toString();
			publicCodeBase = publicCodeBase.substring(0, publicCodeBase.toString().lastIndexOf('/') + 1);
		}

		// Cargamos la plantilla del JNLP
		String template = loadJnlpTemplate();

		// Agregamos al JNLP los recursos propios del sistema operativo que nos hayan indicado
		if (OS_WINDOWS.equalsIgnoreCase(osName) ||
				OS_LINUX.equalsIgnoreCase(osName) ||
				OS_MAC.equalsIgnoreCase(osName)) {
			template = template.replace(TEMPLATE_REPLACE_OS_JAR_REFERENCE,
					TEMPLATE_NODE_REFERENCE.replace(TEMPLATE_REPLACE_OS_NAME, osName));
		} else {
			template = template.replace(TEMPLATE_REPLACE_OS_JAR_REFERENCE, ""); //$NON-NLS-1$
		}

		// Devolvemos el JNLP
		response.addHeader("Content-Type", "application/x-java-jnlp-file"); //$NON-NLS-1$ //$NON-NLS-2$
		response.getWriter().append(
				template
				.replace(TEMPLATE_REPLACE_CODEBASE, publicCodeBase)
				.replace(TEMPLATE_REPLACE_ARGUMENT, arg != null && !arg.isEmpty() ?
						"<argument>" + new String(Base64.decode(arg, true), DEFAULT_CHARSET) + "</argument>" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		response.flushBuffer();
	}

	/**
	 * Carga la plantilla del JNLP en la que sobreescribir el codebase del JAR WebStart
	 * y el argumento a proporcionarle.
	 * @return Plantilla JNLP.
	 * @throws IOException Cuando ocurre un error en la lectura.
	 */
	private static String loadJnlpTemplate() throws IOException {

		byte[] content;
		try (final InputStream templateIs = AutoFirmaJnlpService.class.getClassLoader().getResourceAsStream(TEMPLATE_FILE)) {
			content = readInputStream(templateIs);
		}

		return new String(content, DEFAULT_CHARSET);
	}

	/**
	 * Lee el contenido de un flujo de datos.
	 * @param is Flujo de datos de entrada.
	 * @return Contenido del flujo de datos.
	 * @throws IOException Cuando ocurre un error en la lectura.
	 */
	private static byte[] readInputStream(final InputStream is) throws IOException {

		int n;
		final byte[] buffer = new byte[2048];
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((n = is.read(buffer)) > 0) {
			baos.write(buffer, 0, n);
		}
		return baos.toByteArray();
	}
}
