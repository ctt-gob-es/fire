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

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.signature.ConfigManager;


/**
 * Servlet para la recuperaci&oacute;n de un certificado reci&eacute;n expedido.
 */
public class RecoverNewCertificateService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 4541230456038147211L;

	private static final Logger LOGGER = Logger.getLogger(RecoverNewCertificateService.class.getName());

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		RequestParameters params;
		try {
			params = RequestParameters.extractParameters(request);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}
		
		final String trId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		String errorUrl = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		if (trId == null || trId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		final TransactionAuxParams trAux = new TransactionAuxParams(null, LogUtils.limitText(trId));
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		if (subjectRef == null || subjectRef.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la referencia de usuario")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

        // Cargamos los datos de la transaccion
		final FireSession session = loadSession(trId, subjectRef, request, trAux);
		if (session == null) {
        	Responser.sendError(response, FIReError.INVALID_TRANSACTION);
        	return;
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
	    			connConfig.getProperties(),
	    			logF
	    	);
	    }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido cargar el conector del proveedor de firma: %1s", LogUtils.cleanText(providerName)), e); //$NON-NLS-1$
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

		final boolean skipSelection = connConfig.isAppSkipCertSelection() != null
				? connConfig.isAppSkipCertSelection().booleanValue()
						: ConfigManager.isSkipCertSelection();

		String redirectUrl;

		// Si se ha indicado seleccionar automaticamente el certificado, lo establecemos
		if (skipSelection) {
			request.setAttribute(ServiceParams.HTTP_ATTR_CERT, Base64.encode(certEncoded, true));
			redirectUrl = ServiceNames.PUBLIC_SERVICE_PRESIGN;
		}
		// Si no, lo agregamos como listado para permitir la seleccion
		else {
	        session.setAttribute(trId + "-certs", new X509Certificate[] { cert }); //$NON-NLS-1$
			redirectUrl = FirePages.PG_CHOOSE_CERTIFICATE;
		}

		// Configuramos que la ultima operacion valida fue la de firma, ya que ha terminado
		// la generacion del nuevo certificado
		session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_CHOOSE);

		// Guardamos en la sesion compartida los datos agregados hasta ahora
		session.saveIntoHttpSession(request.getSession());
		SessionCollector.commit(session, trAux);

        Responser.redirectToUrl(redirectUrl, request, response, trAux);
	}

    private static FireSession loadSession(final String transactionId, final String subjectRef, final HttpServletRequest request,
			final TransactionAuxParams trAux) {

    	FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(), false, ConfigManager.isSessionSharingForced(), trAux);
		if (session == null && ConfigManager.isSessionSharingForced()) {
        	LOGGER.warning(trAux.getLogFormatter().f("La transaccion %1s no se ha inicializado o ha caducado", LogUtils.cleanText(transactionId))); //$NON-NLS-1$
			return null;
		}

		// Si la operacion anterior no fue de solicitud de firma, forzamos a que se
		// recargue por si faltan datos
		if (session == null || SessionFlags.OP_GEN != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			LOGGER.info(trAux.getLogFormatter().f("No se encontro la sesion o no estaba actualizada. Forzamos la carga")); //$NON-NLS-1$
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef,
					request.getSession(false), false, true, trAux);
		}

		return session;
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
    			ErrorManager.setErrorToSession(fireSession, error, false, trAux);
    		}
    		Responser.redirectToExternalUrl(url, request, response, trAux);
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
