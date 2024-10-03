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
import java.security.cert.CertificateEncodingException;

import javax.servlet.http.Cookie;
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
public class RecoverSignService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 2571572136476032759L;

	private static final Logger LOGGER = LoggerFactory.getLogger(RecoverSignService.class);

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

		LOGGER.info("Se intenta recuperar el resultado de exito de la firma"); //$NON-NLS-1$

    	final Cookie[] cookies = request.getCookies();
    	LOGGER.info("Cookies recibidas: " + (cookies == null ? "null" : Integer.toString(cookies.length))); //$NON-NLS-1$ //$NON-NLS-2$
    	if (cookies != null) {
    		for (final Cookie cookie : cookies) {
    			LOGGER.info("Cookie: " + cookie.getName());
    		}
    	}

		try {
			final HttpSession session = request.getSession(false);
			if ( session == null || session.getAttribute("user") == null) { //$NON-NLS-1$
				LOGGER.error(session == null ? "No se ha encontrado sesion" : "No habia registrado un usuario en la sesion"); //$NON-NLS-1$ //$NON-NLS-2$
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

			// Recuperamos el resultado de la operacion
			TransactionResult result;
		    try {
		    	result = SignHelper.recoverSignResult(request);
		    }
		    catch (final Exception e) {
		    	LOGGER.error("Error al recuperar la firma: {}", e.toString()); //$NON-NLS-1$
				session.setAttribute("message", "Ocurrio un error al recuperar la firma: " + e); //$NON-NLS-1$ //$NON-NLS-2$
		    	return;
		    }

		    // Primero, obtenemos del resultado e imprimimos en el log varios datos unicamente como muestra
		    // de que los hemos recibido
		    try {
		    	String cert = null;
		    	if (result.getSigningCert() != null) {
		    		cert = Base64.encode(result.getSigningCert().getEncoded());
		    	}
				LOGGER.info("Certificado de firma: {}", cert); //$NON-NLS-1$
			} catch (final CertificateEncodingException e) {
				LOGGER.error("No se pudo decodificar el certificado de firma: {}", e); //$NON-NLS-1$
			}
		    LOGGER.info("Estado: " + result.getState()); //$NON-NLS-1$
		    LOGGER.info("Nombre de proveedor: " + result.getProviderName()); //$NON-NLS-1$
		    LOGGER.info("Formato de actualizacion: " + result.getUpgradeFormat()); //$NON-NLS-1$


		    // Comprobamos si hemos recibido un periodo de gracia (solo puede ocurrir al actualizar
		    // la firma a formatos longevos con @firma
		    if (result.getGracePeriod() != null) {
		    	final String message = "ID Periodo de gracia: " + result.getGracePeriod().getResponseId() //$NON-NLS-1$
		    			+ "\nFecha estimada: " + result.getGracePeriod().getResolutionDate(); //$NON-NLS-1$
		    	session.setAttribute("message", message); //$NON-NLS-1$
		    }
		    // Si no, recuperamos la firma
		    else {
		    	final SignatureInfo info = getSignatureInfo(result.getResult(), session);

		    	session.setAttribute("resultstate", Boolean.TRUE); //$NON-NLS-1$
		    	session.setAttribute("sig", info.getSignature()); //$NON-NLS-1$
		    	session.setAttribute("sigfilename", info.getFilename()); //$NON-NLS-1$
		    	session.setAttribute("sigmimetype", info.getMimetype()); //$NON-NLS-1$
		    }

		    // Redirigimos a la pagina en la que mostrar el resultado
		    request.getRequestDispatcher("/RecoverSign.jsp").forward(request, response); //$NON-NLS-1$

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

	private static SignatureInfo getSignatureInfo(final byte[] result, final HttpSession session) {

		final SignatureInfo signatureInfo = new SignatureInfo();
		signatureInfo.setSignature(result);

		final String signFormat = (String) session.getAttribute("format"); //$NON-NLS-1$
		signatureInfo.setFileInfo("firma", signFormat); //$NON-NLS-1$

		return signatureInfo;
	}
}
