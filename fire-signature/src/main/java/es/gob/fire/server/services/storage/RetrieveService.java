/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.signature.ConfigManager;

/** Servicio de almacenamiento temporal de datos.
 * &Uacute;til para servir de intermediario en comunicaci&oacute;n
 * entre JavaScript y aplicaciones nativas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class RetrieveService extends HttpServlet {

	private static final long serialVersionUID = -3272368448371213403L;

	/** Log para registrar las acciones del servicio. */
	private static final Logger LOGGER = Logger.getLogger(RetrieveService.class.getName());

	/** Nombre del par&aacute;metro con la operaci&oacute;n realizada. */
	private static final String PARAMETER_NAME_OPERATION = "op"; //$NON-NLS-1$

	/** Nombre del par&aacute;metro con el identificador del fichero temporal. */
	private static final String PARAMETER_NAME_ID = "id"; //$NON-NLS-1$

	/** Nombre del par&aacute;metro con la versi&oacute;n de la sintaxis de petici&oacute; utilizada. */
	private static final String PARAMETER_NAME_SYNTAX_VERSION = "v"; //$NON-NLS-1$

	private static final String OPERATION_RETRIEVE = "get"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String operation = request.getParameter(PARAMETER_NAME_OPERATION);
		final String syntaxVersion = request.getParameter(PARAMETER_NAME_SYNTAX_VERSION);
		response.setHeader("Access-Control-Allow-Origin", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		response.setContentType("text/plain"); //$NON-NLS-1$
		response.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		try (
			final PrintWriter out = response.getWriter();
		) {
			if (operation == null) {
				LOGGER.warning(ErrorManager.genError(ErrorManager.ERROR_MISSING_OPERATION_NAME));
				out.println(ErrorManager.genError(ErrorManager.ERROR_MISSING_OPERATION_NAME));
				out.flush();
				return;
			}
			if (syntaxVersion == null) {
				LOGGER.warning(ErrorManager.genError(ErrorManager.ERROR_MISSING_SYNTAX_VERSION));
				out.println(ErrorManager.genError(ErrorManager.ERROR_MISSING_SYNTAX_VERSION));
				out.flush();
				return;
			}

			if (OPERATION_RETRIEVE.equalsIgnoreCase(operation)) {
				retrieveSign(out, request);
			}
			else {
				LOGGER.warning(ErrorManager.genError(ErrorManager.ERROR_UNSUPPORTED_OPERATION_NAME));
				out.println(ErrorManager.genError(ErrorManager.ERROR_UNSUPPORTED_OPERATION_NAME));
			}
			out.flush();
		}

		// Antes de salir revisamos todos los ficheros y eliminamos los caducados.
		StorageConfig.removeExpiredFiles();
	}

	/** Recupera los datos del servidor.
	 * @param out Respuesta a la petici&oacute;n.
	 * @param request Petici&oacute;n.
	 * @throws IOException Cuando ocurre un error al general la respuesta. */
	private static void retrieveSign(final PrintWriter out,
			                         final HttpServletRequest request) throws IOException {

		final String id = request.getParameter(PARAMETER_NAME_ID);
		if (id == null) {
			LOGGER.warning(ErrorManager.genError(ErrorManager.ERROR_MISSING_DATA_ID));
			out.println(ErrorManager.genError(ErrorManager.ERROR_MISSING_DATA_ID));
			return;
		}

		LOGGER.fine("Se solicita el fichero con el identificador: " + id); //$NON-NLS-1$

		final File inFile = new File(ConfigManager.getAfirmaTempDir(), StorageConfig.FILE_PREFIX + id);

		// No hacemos distincion si el archivo no existe, no es un fichero, no puede leerse o ha caducado
		// para evitar que un atacante conozca su situacion. Lo borramos despues de usarlo
		if (!inFile.isFile() || !inFile.canRead() || StorageConfig.isExpired(inFile, ConfigManager.getAfirmaTempsTimeout())) {

			if (!inFile.exists()) {
				LOGGER.warning("El fichero con el identificador '" + id + "' no existe: " + inFile.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (!inFile.isFile()) {
				LOGGER.warning("El archivo con el identificador '" + id + "' no es un fichero: " + inFile.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (!inFile.canRead()) {
				LOGGER.warning("El fichero con el identificador '" + id + "' no tiene permisos de lectura: " + inFile.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				LOGGER.warning("El fichero con el identificador '" + id + "' esta caducado: " + inFile.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			out.println(
				ErrorManager.genError(ErrorManager.ERROR_INVALID_DATA_ID)  + " ('" + id + "')" //$NON-NLS-1$ //$NON-NLS-2$
			);
			// Que el fichero sea de tipo fichero, implica que existe
			if (inFile.isFile()) {
				if (!StorageConfig.DEBUG) {
					inFile.delete();
				}
			}
		}
		else {
			try (
				final InputStream fis = new FileInputStream(inFile);
			) {
				out.println(new String(getDataFromInputStream(fis)));
				fis.close();
				LOGGER.fine("Se recupera el fichero: " + inFile.getName()); //$NON-NLS-1$
			}
			catch (final IOException e) {
				LOGGER.severe("Error recupendo el fichero '" + inFile.getAbsolutePath() + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
				out.println(ErrorManager.genError(ErrorManager.ERROR_INVALID_DATA));
				return;
			}
			if (!StorageConfig.DEBUG) {
				inFile.delete();
			}
		}
	}

	private static final int BUFFER_SIZE = 4096;

	/** Lee un flujo de datos de entrada y los recupera en forma de array de
     * octetos. Este m&eacute;todo consume pero no cierra el flujo de datos de
     * entrada.
     * @param input Flujo de donde se toman los datos.
     * @return Los datos obtenidos del flujo.
     * @throws IOException Cuando ocurre un problema durante la lectura. */
    private static byte[] getDataFromInputStream(final InputStream input) throws IOException {
        if (input == null) {
            return new byte[0];
        }
        int nBytes = 0;
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((nBytes = input.read(buffer)) != -1) {
            baos.write(buffer, 0, nBytes);
        }
        return baos.toByteArray();
    }
}