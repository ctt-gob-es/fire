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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.signature.ConfigManager;

/**
 * Servlet para la solicitud de expedici&oacute;n de un nuevo certificado.
 */
public final class RequestNewCertificateService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(RequestNewCertificateService.class.getName());

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) {

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
		
		final String appId  = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String trId  = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef  = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final boolean originForced = Boolean.parseBoolean(params.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED));
		final String redirectErrorUrl = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		final TransactionAuxParams trAux = new TransactionAuxParams(appId, LogUtils.limitText(trId));
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de solicitud de certificado")); //$NON-NLS-1$

		// Comprobamos que se hayan proporcionado los parametros indispensables
        if (trId == null || trId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el identificador de transaccion")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

		// Comprobamos del usuario
    	if (subjectRef == null || subjectRef.isEmpty()) {
            LOGGER.warning(logF.f("No se ha proporcionado la referencia de usuario")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

		if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
			LOGGER.warning(logF.f("No se ha proporcionado la URL de error")); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		String decodedErrorUrl;
		try {
			decodedErrorUrl = URLDecoder.decode(redirectErrorUrl, StandardCharsets.UTF_8.name());
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: ") + e); //$NON-NLS-1$
        	decodedErrorUrl = redirectErrorUrl;
		}

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

		// Cargamos los datos de sesion
		final FireSession session = loadSession(trId, subjectRef, request, trAux);
		if (session == null) {
       		LOGGER.warning(logF.f("Se redirige a la pagina proporcionada en la llamada")); //$NON-NLS-1$
       		Responser.redirectToExternalUrl(decodedErrorUrl, request, response, trAux);
			return;
		}

        final String providerName	= session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN);
        final String subjectId	= session.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);
		TransactionConfig connConfig = (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		if (connConfig == null) {
			connConfig = new TransactionConfig(new Properties());
		}
    	final String errorUrlRedirection = connConfig.getRedirectErrorUrl();

    	// Creamos una configuracion igual a la de firma para la generacion de certificado
    	// y establecemos que la URL de redireccion en caso de exito sea la de recuperacion
    	// del certificado generado
    	final String redirectUrlBase = PublicContext.getPublicContext(request);

        final TransactionConfig requestCertConfig = new TransactionConfig(connConfig);
        requestCertConfig.setRedirectSuccessUrl(
        		redirectUrlBase + ServiceNames.PUBLIC_SERVICE_RECOVER_NEW_CERT + "?" + //$NON-NLS-1$
        				ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef + "&" + //$NON-NLS-1$ //$NON-NLS-2$
        				ServiceParams.HTTP_PARAM_APPLICATION_ID + "=" + appId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
        				ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
        				ServiceParams.HTTP_PARAM_ERROR_URL + "=" + redirectErrorUrl); //$NON-NLS-1$

        LOGGER.info(logF.f("Solicitamos generar un nuevo certificado de usuario")); //$NON-NLS-1$

        final GenerateCertificateResult gcr;
        try {
        	gcr = GenerateCertificateManager.generateCertificate(providerName, subjectId, requestCertConfig.getProperties(), logF);
        }
        catch (final IllegalArgumentException e) {
        	LOGGER.warning(logF.f("No se ha proporcionado el identificador del usuario que solicita el certificado")); //$NON-NLS-1$
        	redirectToErrorPage(request, response, session, FIReError.INTERNAL_ERROR, errorUrlRedirection, originForced, trAux);
        	return;
        }
        catch (final FIReConnectorFactoryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("Error en la carga la configuracion del conector del proveedor de firma"), e); //$NON-NLS-1$
        	redirectToErrorPage(request, response, session, FIReError.INTERNAL_ERROR, errorUrlRedirection, originForced, trAux);
        	return;
        }
        catch (final FIReConnectorNetworkException e) {
        	LOGGER.log(Level.SEVERE, logF.f("No se ha podido conectar con el proveedor de firma en la nube"), e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.CONNECTION_SIGNATURE_PROVIDER, providerName);
			redirectToErrorPage(request, response, session, FIReError.PROVIDER_INACCESIBLE_SERVICE, errorUrlRedirection, originForced, trAux);
        	return;
        }
        catch (final FIReCertificateAvailableException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El usuario ya tiene un certificado del tipo indicado"), e); //$NON-NLS-1$
        	redirectToErrorPage(request, response, session, FIReError.CERTIFICATE_DUPLICATED, errorUrlRedirection, originForced, trAux);
        	return;
        }
        catch (final FIReCertificateException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El certificado obtenido no es valido"), e); //$NON-NLS-1$
        	redirectToErrorPage(request, response, session, FIReError.CERTIFICATE_ERROR, errorUrlRedirection, originForced, trAux);
        	return;
        }
        catch (final FIReConnectorUnknownUserException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El usuario no esta dado de alta en el sistema proveedor y no podra generarse un nuevo certificado"), e); //$NON-NLS-1$
        	redirectToErrorPage(request, response, session, FIReError.UNKNOWN_USER, errorUrlRedirection, originForced, trAux);
        	return;
        }
        catch (final WeakRegistryException e) {
        	LOGGER.log(Level.SEVERE, logF.f("El usuario realizo un registro debil y no puede tener certificados de firma"), e); //$NON-NLS-1$
        	redirectToErrorPage(request, response, session, FIReError.CERTIFICATE_WEAK_REGISTRY, errorUrlRedirection, originForced, trAux);
        	return;
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, logF.f("Error desconocido en la generacion del certificado"), e); //$NON-NLS-1$
        	redirectToErrorPage(request, response, session, FIReError.PROVIDER_ERROR, errorUrlRedirection, originForced, trAux);
        	return;
        }

        final String generateTransactionId = gcr.getTransactionId();
        final String redirectUrl = gcr.getRedirectUrl();

        session.setAttribute(ServiceParams.SESSION_PARAM_GENERATE_TRANSACTION_ID, generateTransactionId);
        session.setAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_SIGN, Boolean.TRUE);
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_GEN);

        SessionCollector.commit(session, trAux);
        session.saveIntoHttpSession(request.getSession());

        LOGGER.info(logF.f("Redirigimos a la URL de emision del certificado")); //$NON-NLS-1$

        Responser.redirectToExternalUrl(redirectUrl, request, response, trAux);

        LOGGER.fine(logF.f("Fin de la llamada al servicio publico de solicitud de certificado")); //$NON-NLS-1$
	}

	private static FireSession loadSession(final String transactionId, final String subjectRef, final HttpServletRequest request,
			final TransactionAuxParams trAux) {

		FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, ConfigManager.isSessionSharingForced(), trAux);
        if (session == null && ConfigManager.isSessionSharingForced()) {
        	LOGGER.warning(trAux.getLogFormatter().f("La transaccion %1s no se ha inicializado o ha caducado", LogUtils.cleanText(transactionId))); //$NON-NLS-1$
    		return null;
        }

        // Si la operacion anterior no fue la session de proveedor, forzamos a que se
     	// recargue por si faltan datos
     	if (session == null || SessionFlags.OP_CHOOSE != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
     		LOGGER.info(trAux.getLogFormatter().f("No se encontro la sesion o no estaba actualizada. Forzamos la carga")); //$NON-NLS-1$
    		session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true, trAux);
        }

		return session;
	}

	/**
	 * Establece el mensaje interno de error y redirige a la p&aacute;gina de error interna
	 * para que el usuario pueda seleccionar otro proveedor o a la pagina de error indicada
	 * por el usuario si se forz&oacute; el uso de un proveedor concreto.
	 * @param request Objeto con la petici&oacute;n realizada.
	 * @param response Objeto para la respuesta a la petici&oacute;n.
	 * @param session Sesi&oacute;n con los datos de la transacci&oacute;n.
	 * @param operationError Error que debe declararse.
	 * @param errorUrlRedirection URL externa de error indicada por la aplicaci&ooacute;n.
	 * @param originForced {@code true} si se forz&oacute; al uso de un proveedor concreto.
	 */
	private static void redirectToErrorPage(final HttpServletRequest request, final HttpServletResponse response,
			final FireSession session, final FIReError operationError,
			final String errorUrlRedirection, final boolean originForced, final TransactionAuxParams trAux) {
		ErrorManager.setErrorToSession(session, operationError, originForced, trAux);
    	if (originForced) {
    		Responser.redirectToExternalUrl(errorUrlRedirection, request, response, trAux);
    	}
    	else {
    		Responser.redirectToUrl(FirePages.PG_SIGNATURE_ERROR, request, response, trAux);
    	}
	}
}