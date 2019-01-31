/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.signature.ConfigManager;

/**
 * Servlet implementation class RequestNewCertificateService
 */
public final class RequestNewCertificateService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(RequestNewCertificateService.class.getName());

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final String appId  = request.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String transactionId  = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId  = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final boolean originForced = Boolean.parseBoolean(request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED));

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

		// Comprobamos que se hayan proporcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.format("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		// Comprobamos del usuario
    	if (subjectId == null || subjectId.isEmpty()) {
            LOGGER.warning(logF.format("No se ha proporcionado el identificador del usuario que solicita el certificado")); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		LOGGER.fine(logF.format("Peticion bien formada")); //$NON-NLS-1$

		FireSession session = SessionCollector.getFireSession(transactionId, subjectId, request.getSession(false), true, false);
        if (session == null) {
    		LOGGER.warning(logF.format("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode());
    		return;
        }

        // Si la operacion anterior no fue la solicitud de una firma, forzamos a que se recargue por si faltan datos
        if (SessionFlags.OP_SIGN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
        	session = SessionCollector.getFireSession(transactionId, subjectId, request.getSession(false), false, true);
        }

        final String origin	= session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
		TransactionConfig connConfig =
				(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		if (connConfig == null) {
			connConfig = new TransactionConfig(new Properties());
		}
    	final String errorUrlRedirection = connConfig.getRedirectErrorUrl();

    	// Creamos una configuracion igual a la de firma para la generacion de certificado
    	// y establecemos que la URL de redireccion en caso de exito sea la de recuperacion
    	// del certificado generado
    	final String redirectUrlBase = getPublicContext(request.getRequestURL().toString());

        final TransactionConfig requestCertConfig = (TransactionConfig) connConfig.clone();
        requestCertConfig.setRedirectSuccessUrl(
        		redirectUrlBase + "ChooseNewCertificate.jsp?" + //$NON-NLS-1$
        				ServiceParams.HTTP_PARAM_SUBJECT_ID + "=" + subjectId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
        				ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId); //$NON-NLS-1$

        LOGGER.info(logF.format("Solicitamos generar un nuevo certificado de usuario")); //$NON-NLS-1$

        final GenerateCertificateResult gcr;
        try {
        	gcr = GenerateCertificateManager.generateCertificate(origin, subjectId, requestCertConfig.getProperties());
        }
        catch (final IllegalArgumentException e) {
        	LOGGER.warning(logF.format("No se ha proporcionado el identificador del usuario que solicita el certificado: " + e)); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
        			"No se ha proporcionado el identificador del usuario que solicita el certificado"); //$NON-NLS-1$
        	return;
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.format("Error en la configuracion del conector del proveedor de firma"), e); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			"Error en la configuracion del conector con el servicio de custodia: " + e //$NON-NLS-1$
        			);
        	return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.format("No se ha podido conectar con el sistema"), e); //$NON-NLS-1$
        	response.sendError(
        			HttpServletResponse.SC_REQUEST_TIMEOUT,
        			"No se ha podido conectar con el sistema: " + e); //$NON-NLS-1$
        	return;
        }
        catch (final FIReCertificateAvailableException e) {
        	LOGGER.log(Level.SEVERE, logF.format("El usuario ya tiene un certificado del tipo indicado"), e); //$NON-NLS-1$
        	response.sendError(
    				HttpCustomErrors.CERTIFICATE_AVAILABLE.getErrorCode(),
    				"El usuario ya tiene un certificado del tipo indicado: " + e); //$NON-NLS-1$
    		return;
        }
        catch (final FIReCertificateException e) {
        	LOGGER.log(Level.SEVERE, logF.format("Error en la generacion del certificado"), e); //$NON-NLS-1$
        	response.sendError(
        			HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			"Error en la generacion del certificado: " + e); //$NON-NLS-1$
        	return;
        }
        catch (final FIReConnectorUnknownUserException e) {
        	LOGGER.log(Level.SEVERE, logF.format("El usuario no esta dado de alta en el sistema"), e); //$NON-NLS-1$
        	response.sendError(
        			HttpCustomErrors.NO_USER.getErrorCode(),
        			"El usuario no esta dado de alta en el sistema: " + e); //$NON-NLS-1$
        	return;
        }
        catch (final WeakRegistryException e) {
        	LOGGER.log(Level.SEVERE, logF.format("El usuario realizo un registro debil y no puede tener certificados de firma"), e); //$NON-NLS-1$
        	if (originForced) {
        		response.sendRedirect(errorUrlRedirection);
        	}
        	else {
        		request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
        	}
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.format("Error desconocido en la generacion del certificado"), e); //$NON-NLS-1$
        	SessionCollector.removeSession(session);
        	response.sendError(
        			HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			"Error desconocido en la generacion del certificado: " + e); //$NON-NLS-1$
        	return;
        }

        final String generateTransactionId = gcr.getTransactionId();
        final String redirectUrl = gcr.getRedirectUrl();

        session.setAttribute(ServiceParams.SESSION_PARAM_GENERATE_TRANSACTION_ID, generateTransactionId);
        session.setAttribute(ServiceParams.SESSION_PARAM_REDIRECTED, Boolean.TRUE);
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_GEN);
        SessionCollector.commit(session);

        LOGGER.info(logF.format("Redirigimos a la URL de emision del certificado")); //$NON-NLS-1$

        response.sendRedirect(redirectUrl);
	}


	private static String getPublicContext(final String requestUrl) {
		String redirectUrlBase = ConfigManager.getPublicContextUrl();
		if ((redirectUrlBase == null || redirectUrlBase.isEmpty()) && requestUrl != null) {
			redirectUrlBase = requestUrl.substring(0, requestUrl.lastIndexOf('/'));
		}

		if (redirectUrlBase != null && !redirectUrlBase.endsWith("/public/")) { //$NON-NLS-1$
			if (redirectUrlBase.endsWith("/public")) { //$NON-NLS-1$
				redirectUrlBase += "/"; //$NON-NLS-1$
			}
			else {
				redirectUrlBase += "/public/"; //$NON-NLS-1$
			}
		}
		return redirectUrlBase;
	}
}
