/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.test.webapp;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.TransactionResult;

/**
 * Servicio para la recuperaci&oacute;n del resultado de firma de un lote.
 */
public class DownloadBatchSignService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 2571572136476032759L;

	private static final Logger LOGGER = LoggerFactory.getLogger(DownloadBatchSignService.class);

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

		try {
			final HttpSession session = request.getSession(false);
			if ( session == null || session.getAttribute("user") == null) { //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

			// Obtenemos el id de transaccion
			final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
			if (transactionId == null) {
				LOGGER.error("No se ha encontrado id de transaccion iniciada"); //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

			// Obtenemos el id de transaccion
			final String docId = request.getParameter("docid"); //$NON-NLS-1$
			if (docId == null) {
				LOGGER.warn("No se ha proporcionado el identificador de documento"); //$NON-NLS-1$
		    	response.sendRedirect("ErrorPage.jsp?fatal=false&msg=" + URLEncoder.encode("No se ha proporcionado el identificador de documento", "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}

			// Recuperamos la firma del lote
			final TransactionResult result;
		    try {
		    	result = BatchHelper.recoverBatchSign(request);
		    }
		    catch (final Exception e) {
				LOGGER.error("Error al recuperar una firma del lote: {}", e.toString()); //$NON-NLS-1$
		    	response.sendRedirect("ErrorPage.jsp?fatal=false&msg=" + URLEncoder.encode(e.getMessage(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
		    	return;
		    }

		    // Comprobamos si hemos recibido un periodo de gracia (solo puede ocurrir al actualizar
		    // la firma a formatos longevos con @firma
	    	final SignatureInfo info = getSignatureInfo(result.getResult(), docId, session);

	    	response.setContentType(info.getMimetype());
	    	response.setHeader("Content-Disposition", "filename=\"" + info.getFilename() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    	response.getOutputStream().write(info.getSignature());
	    	response.getOutputStream().flush();

		    final Integer numSigsPendingToDownload = (Integer) session.getAttribute("numsigs"); //$NON-NLS-1$
		    if (numSigsPendingToDownload == null || numSigsPendingToDownload.intValue() <= 1) {
		    	session.invalidate();
		    } else {
		    	session.setAttribute("numsigs", Integer.valueOf(numSigsPendingToDownload.intValue() - 1)); //$NON-NLS-1$
		    }

		} catch (final Exception e) {
			LOGGER.error("Error interno del servicio: " + e); //$NON-NLS-1$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("Error interno del servicio: " + e.getMessage()); //$NON-NLS-1$
				response.flushBuffer();
			} catch (final IOException ioe) {
				LOGGER.error("Ha ocurrido un error al tratar de pasar el mensaje de error a la respuesta. Error: " + ioe); //$NON-NLS-1$
			}
		}
	}

	private static SignatureInfo getSignatureInfo(final byte[] result, final String docId, final HttpSession session) {

		final SignatureInfo signatureInfo = new SignatureInfo();
		signatureInfo.setSignature(result);

		String sufix = docId;
		if (docId.indexOf('.') > 0) {
			sufix = docId.substring(0, docId.lastIndexOf('.'));
		}
		final String filename = "firma_" + sufix; //$NON-NLS-1$
		final String signFormat = (String) session.getAttribute("format"); //$NON-NLS-1$
		signatureInfo.setFileInfo(filename, signFormat);

		return signatureInfo;
	}

}
