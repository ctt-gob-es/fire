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
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.Responser;


/**
 * Servlet para la recuperaci&oacute;n de un certificado reci&eacute;n expedido.
 */
public class RecoverNewCertificateService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 4541230456038147211L;

	private static final Logger LOGGER = Logger.getLogger(RecoverNewCertificateService.class.getName());

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		String errorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		if (transactionId == null || transactionId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		final TransactionAuxParams trAux = new TransactionAuxParams(null, transactionId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia de usuario")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(), false, false, trAux);
		if (session == null) {
			LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
			processError(FIReError.FORBIDDEN, session, errorUrl, request, response, trAux);
			return;
		}

		// Si la operacion anterior no fue la solicitud de generacion de un certificado, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_GEN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true, trAux);
		}

		final String generateTrId = session.getString(ServiceParams.SESSION_PARAM_GENERATE_TRANSACTION_ID);
		final String providerName = session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
	    final TransactionConfig connConfig = (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.warning(logF.f("No se encontro en la sesion la URL redireccion de error para la operacion")); //$NON-NLS-1$
			processError(FIReError.INTERNAL_ERROR, session, errorUrl, request, response, trAux);
			return;
		}

		// Usaremos la URL de error establecida en la sesion
		errorUrl = connConfig.getRedirectErrorUrl();

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
        	processError(FIReError.INTERNAL_ERROR, session, errorUrl, request, response, trAux);
        	return;
        }
	    catch (final FIReConnectorNetworkException e) {
	    	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
	    	AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
	    	processError(FIReError.PROVIDER_INACCESIBLE_SERVICE, session, errorUrl, request, response, trAux);
	    	return;
	    }
	    catch (final FIReCertificateException e) {
	    	LOGGER.log(Level.SEVERE, logF.f("Error en la generacion del certificado"), e); //$NON-NLS-1$
	    	processError(FIReError.CERTIFICATE_ERROR, session, errorUrl, request, response, trAux);
	    	return;
	    }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, logF.f("Error desconocido del proveedor de firma en la nube al recuperar un certificado"), e); //$NON-NLS-1$
            processError(FIReError.PROVIDER_ERROR, session, errorUrl, request, response, trAux);
            return;
        }

	    // Componemos el certificado y lo almacenamos en la sesion
	    final CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
        }
        catch (final Exception e) {
            LOGGER.severe(logF.f("No se pudo cargar la factoria de certificados")); //$NON-NLS-1$
            processError(FIReError.INTERNAL_ERROR, session, errorUrl, request, response, trAux);
            return;
        }
        X509Certificate cert;
        try {
        	cert = (X509Certificate) cf.generateCertificate(
        			new ByteArrayInputStream(certEncoded)
        			);
        }
        catch (final CertificateException e) {
            LOGGER.severe(logF.f("Error cargando el certificado del usuario proporcionado por el proveedor de firma en la nube")); //$NON-NLS-1$
            processError(FIReError.PROVIDER_ERROR, session, errorUrl, request, response, trAux);
            return;
        }

        // Adjuntamos los certificados a la sesion para que los reciba el JSP y configuramos
        // que la ultima operacion valida fue la de firma, ya que ha terminado la generacion
        // del nuevo certificado
        session.setAttribute(transactionId + "-certs", new X509Certificate[] { cert }); //$NON-NLS-1$
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_SIGN);
        SessionCollector.commit(session, trAux);

        try {
        	request.getRequestDispatcher(FirePages.PG_CHOOSE_CERTIFICATE).forward(request, response);
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido redirigir al usuario a la URL interna"), e); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.INTERNAL_ERROR);
		}
	}

    /**
     * Invalida la sesion del usuario y, si se ha indicado una URL de error, lo redirige a ella o devuelve el error en caso contrario.
     * @param error Tipo de error que se ha producido.
     * @param fireSession Sesi&oacute;n de la transacci&oacute;n.
     * @param url URL de error a la que redirigir al usuario.
     * @param request Objeto de petici&oacute;n realizada al servlet.
     * @param response Objeto de respuesta con el que realizar la redirecci&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
     */
    private static void processError(final FIReError error, final FireSession fireSession, final String url,
    		final HttpServletRequest request, final HttpServletResponse response, final TransactionAuxParams trAux) {

    	if (url != null) {
    		if (fireSession != null) {
    			SessionCollector.cleanSession(fireSession, trAux);
    		}
    		try {
    			response.sendRedirect(url);
    		}
    		catch (final Exception e) {
    			LOGGER.log(Level.SEVERE, trAux.getLogFormatter().f("No se ha podido redirigir al usuario a la URL externa"), e); //$NON-NLS-1$
    			Responser.sendError(response, FIReError.INTERNAL_ERROR);
    		}
    	}
    	else {
    		// Invalidamos la sesion entre el navegador y el componente central porque no se usara mas
    		final HttpSession httpSession = request.getSession(false);
    		if (httpSession != null) {
    			httpSession.invalidate();
    		}
    		Responser.sendError(response, error);
    	}
    }
}
