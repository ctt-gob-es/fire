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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;


/**
 * Servlet para la recuperaci&oacute;n de un certificado reci&eacute;n expedido.
 */
public class RecoverNewCertificateService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 4541230456038147211L;

	private static final Logger LOGGER = Logger.getLogger(RecoverNewCertificateService.class.getName());

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		final LogTransactionFormatter logF = new LogTransactionFormatter(null, transactionId);

		if (transactionId == null || transactionId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado el identificador de usuario")); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(), false, false);
		if (session == null) {
			LOGGER.severe(logF.f("No existe sesion vigente asociada a la transaccion")); //$NON-NLS-1$
			if (redirectErrorUrl != null) {
				response.sendRedirect(redirectErrorUrl);
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
			return;
		}

		// Si la operacion anterior no fue la solicitud de generacion de un certificado, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_GEN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true);
		}

		final String generateTrId = session.getString(ServiceParams.SESSION_PARAM_GENERATE_TRANSACTION_ID);
		final String providerName = session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
	    final TransactionConfig connConfig =
	    		(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		String errorUrl = null;
		if (connConfig != null && connConfig.isDefinedRedirectErrorUrl()) {
			errorUrl = connConfig.getRedirectErrorUrl();
		}

		byte[] certEncoded = null;
	    try {
	    	certEncoded = RecoverCertificateManager.recoverCertificate(
	    			providerName,
	    			generateTrId,
	    			connConfig.getProperties()
	    	);
	    }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", providerName), e); //$NON-NLS-1$
        	if (errorUrl != null) {
            	response.sendRedirect(errorUrl);
            } else {
            	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        	return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
        	AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
        	if (errorUrl != null) {
            	response.sendRedirect(errorUrl);
            } else {
	            response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
	                    "No se ha podido conectar con el sistema"); //$NON-NLS-1$
            }
	         return;
        }
        catch (final Exception e) {
            LOGGER.log(Level.WARNING, logF.f("Error al recuperar el nuevo certificado"), e); //$NON-NLS-1$
            if (errorUrl != null) {
            	response.sendRedirect(errorUrl);
            } else {
            	response.sendError(
            			HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            			"No se ha podido obtener el certificado generado"); //$NON-NLS-1$
            }
            return;
        }

	    // Componemos el certificado y lo almacenamos en la sesion
	    final CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
        }
        catch (final Exception e) {
            LOGGER.severe(logF.f("No se pudo cargar la factoria de certificados")); //$NON-NLS-1$
            if (errorUrl != null) {
	    		response.sendRedirect(errorUrl);
	    	} else {
	    		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	    	}
            return;
        }
        X509Certificate cert;
        try {
        	cert = (X509Certificate) cf.generateCertificate(
        			new ByteArrayInputStream(certEncoded)
        			);
        }
        catch (final CertificateException e) {
            LOGGER.severe(logF.f("Error cargando el certificado del usuario")); //$NON-NLS-1$
            if (errorUrl != null) {
	    		response.sendRedirect(errorUrl);
	    	} else {
	    		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	    	}
            return;
        }

        // Adjuntamos los certificados a la sesion para que los reciba el JSP y configuramos
        // que la ultima operacion valida fue la de firma, ya que ha terminado la generacion
        // del nuevo certificado
        session.setAttribute(transactionId + "-certs", new X509Certificate[] { cert }); //$NON-NLS-1$
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_SIGN);
        SessionCollector.commit(session);

        try {
			request.getRequestDispatcher(FirePages.PG_CHOOSE_CERTIFICATE).forward(request, response);
			return;
		}
		catch (final ServletException e) {
			LOGGER.warning(logF.f("No se pudo continuar hasta la pagina de seleccion de certificado. Se redirigira al usuario a esa pagina")); //$NON-NLS-1$
			response.sendRedirect( FirePages.PG_CHOOSE_CERTIFICATE + "?" + ServiceParams.HTTP_PARAM_TRANSACTION_ID + //$NON-NLS-1$
					"=" + request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID)); //$NON-NLS-1$
		}
	}
}
