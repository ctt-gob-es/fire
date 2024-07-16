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
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.BatchResult;
import es.gob.fire.client.SignBatchResult;

/**
 * Servicio para la recuperaci&oacute;n del resultado de firma de un lote.
 */
public class RecoverBatchService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 2571572136476032759L;

	private static final Logger LOGGER = LoggerFactory.getLogger(RecoverBatchService.class);

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		try {
			final HttpSession session = request.getSession(false);
			if ( session == null || session.getAttribute("user") == null) { //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

			final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
			if (transactionId == null) {
				LOGGER.error("No se ha encontrado id de transaccion iniciada"); //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

		   BatchResult result = null;
		    try {
		    	result = BatchHelper.recoverBatchResult(request);
		    }
		    catch (final Exception e) {
				LOGGER.error("Error durante la operacion de recuperacion del lote", e); //$NON-NLS-1$
				final String msg = e.getMessage() != null ? e.getMessage() : e.toString();
		    	response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode(msg, "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
		    	return;
		    }

		    // Mostramos por consola los algunos datos referentes al lote
		    LOGGER.info("Proveedor usado: " + result.getProviderName()); //$NON-NLS-1$
		    try {
				LOGGER.info("Certificado de firma: " + (result.getSigningCert() != null ? //$NON-NLS-1$
						Base64.encode(result.getSigningCert().getEncoded()) : null));
			} catch (final CertificateEncodingException e) {
				LOGGER.error("No se pudo decodificar el certificado de firma: " + e); //$NON-NLS-1$
			}

		    // Almacenamos el numero de firma para saber cuantas podemos descargar
		    int numSigs = 0;
		    for (final SignBatchResult signResult : result.values().toArray(new SignBatchResult[0])) {
		    	if ((signResult.getErrotType() == null || signResult.getErrotType().isEmpty())
		    			&& signResult.getGracePeriod() == null) {
		    		numSigs++;
		    	}
		    }
		    session.setAttribute("numsigs", Integer.valueOf(numSigs)); //$NON-NLS-1$

		    try {
		    	response.getOutputStream().write(result.toString().getBytes(StandardCharsets.UTF_8));
			    response.flushBuffer();
			} catch (final IOException ioe) {
				LOGGER.error("Ha ocurrido un error al tratar de pasar el mensaje de error a la respuesta. Error: " + ioe); //$NON-NLS-1$
			}
		} catch (final Exception e) {
			LOGGER.error("Error interno: " + e); //$NON-NLS-1$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			try {
				response.getWriter().write("Error interno: " + e.getMessage()); //$NON-NLS-1$
				response.flushBuffer();
			} catch (final IOException ioe) {
				LOGGER.error("Ha ocurrido un error al tratar de pasar el mensaje de error a la respuesta. Error: " + ioe); //$NON-NLS-1$
			}
		}
	}
}
