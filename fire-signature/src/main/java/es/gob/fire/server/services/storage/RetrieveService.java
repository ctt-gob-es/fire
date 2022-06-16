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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.internal.TempDocumentsManager;

/** Servicio de almacenamiento temporal de firmas. &Uacute;til para servir de intermediario en comunicaci&oacute;n
 * entre JavaScript y <i>Apps</i> m&oacute;viles nativas.
 * @author Tom&aacute;s Garc&iacute;a-;er&aacute;s */
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

		LOGGER.fine("== INICIO DE LA RECUPERACION =="); //$NON-NLS-1$

		final String operation = request.getParameter(PARAMETER_NAME_OPERATION);
		final String syntaxVersion = request.getParameter(PARAMETER_NAME_SYNTAX_VERSION);
		final String id = request.getParameter(PARAMETER_NAME_ID);
		response.setHeader("Access-Control-Allow-Origin", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		if (operation == null) {
			LOGGER.warning(ErrorManager.genError(ErrorManager.ERROR_MISSING_OPERATION_NAME));
			sendResult(response, ErrorManager.genError(ErrorManager.ERROR_MISSING_OPERATION_NAME));
			return;
		}
		if (syntaxVersion == null) {
			LOGGER.warning(ErrorManager.genError(ErrorManager.ERROR_MISSING_SYNTAX_VERSION));
			sendResult(response, ErrorManager.genError(ErrorManager.ERROR_MISSING_SYNTAX_VERSION));
			return;
		}
		if (id == null) {
			LOGGER.warning(ErrorManager.genError(ErrorManager.ERROR_MISSING_DATA_ID));
			sendResult(response, ErrorManager.genError(ErrorManager.ERROR_MISSING_DATA_ID));
			return;
		}
		if (!OPERATION_RETRIEVE.equalsIgnoreCase(operation)) {
			LOGGER.warning(ErrorManager.genError(ErrorManager.ERROR_UNSUPPORTED_OPERATION_NAME));
			sendResult(response, ErrorManager.genError(ErrorManager.ERROR_UNSUPPORTED_OPERATION_NAME));
			return;
		}

		retrieveSign(response, request, id);

		LOGGER.fine("== FIN DE LA RECUPERACION =="); //$NON-NLS-1$
	}

	/** Recupera la firma del servidor.
	 * @param response Respuesta a la petici&oacute;n.
	 * @param request Petici&oacute;n.
	 * @param id Identificador del documento a recuperar.
	 * @throws IOException Cuando ocurre un error al general la respuesta. */
	private static void retrieveSign(final HttpServletResponse response,
			final HttpServletRequest request, final String id) throws IOException {

		LOGGER.fine("Se solicita el documento con el identificador: " + LogUtils.cleanText(id)); //$NON-NLS-1$

		// Si el documento no existe, lo indicamos asi
		if (!TempDocumentsManager.existDocument(id)) {
			sendResult(response, ErrorManager.genError(ErrorManager.ERROR_INVALID_DATA_ID)  + " ('" + id + "')"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Recuperamos y eliminamos el documento
		byte[] data;
		try {
			data = TempDocumentsManager.retrieveAndDeleteDocument(id);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, ErrorManager.genError(ErrorManager.ERROR_INVALID_DATA), e);
			sendResult(response, ErrorManager.genError(ErrorManager.ERROR_INVALID_DATA));
			return;
		}
		sendResult(response, data);
	}

    private static void sendResult(final HttpServletResponse response, final String text)
    		throws IOException {
    	response.setContentType("text/plain"); //$NON-NLS-1$
		response.setCharacterEncoding("utf-8"); //$NON-NLS-1$
		try (PrintWriter writer = response.getWriter()) {
			writer.print(text);
			writer.flush();
		}
    }

    private static void sendResult(final HttpServletResponse response, final byte[] data)
    		throws IOException {
    	try (OutputStream os = response.getOutputStream()) {
    		os.write(data);
    		os.flush();
    	}
    }
}