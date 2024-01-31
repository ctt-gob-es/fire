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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.FIReConnectorFactoryException;
import es.gob.fire.server.connector.FIReConnectorNetworkException;
import es.gob.fire.server.connector.FIReConnectorUnknownUserException;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.WeakRegistryException;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.Responser;

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
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		final String appId  = request.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String transactionId  = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectRef  = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		final boolean originForced = Boolean.parseBoolean(request.getParameter(ServiceParams.HTTP_PARAM_CERT_ORIGIN_FORCED));
		final String errorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		final TransactionAuxParams trAux = new TransactionAuxParams(appId, transactionId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de solicitud de certificado")); //$NON-NLS-1$

		// Comprobamos que se hayan proporcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
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

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

		FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true, trAux);
        if (session == null) {
			LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
			processError(FIReError.FORBIDDEN, errorUrl, request, response, trAux);
    		return;
        }

        // Si la operacion anterior no fue la solicitud de seleccion de certificado, forzamos a que se recargue por si faltan datos
    	if (SessionFlags.OP_CHOOSE != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSessionOfuscated(transactionId, subjectRef, request.getSession(false), false, true, trAux);
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
        				ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
        				ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl); //$NON-NLS-1$

        LOGGER.info(logF.f("Solicitamos generar un nuevo certificado de usuario")); //$NON-NLS-1$

        final GenerateCertificateResult gcr;
        try {
        	gcr = GenerateCertificateManager.generateCertificate(providerName, subjectId, requestCertConfig.getProperties());
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

        LOGGER.info(logF.f("Redirigimos a la URL de emision del certificado")); //$NON-NLS-1$

        Responser.redirectToExternalUrl(redirectUrl, request, response, trAux);

        LOGGER.fine(logF.f("Fin de la llamada al servicio publico de solicitud de certificado")); //$NON-NLS-1$
	}

    /**
     * Invalida la sesion del usuario y, si se ha indicado una URL de error, lo redirige a ella o devuelve el error en caso contrario.
     * @param error Tipo de error que se ha producido.
  

	 * 
	 * @param trAux Par&aacute;metro auxiliar de la transacci&oacute;n.
	 * 
	 * 
	 * @throws IOEx    eption Cuando no se puede redirigi
	 */      
	 rivate static vo  d processError(final FIReError error, final Str
	 	final HttpServletResponse response, final TransactionAuxPar
	 *                 ms trAux) {
	     
	 Invalidamos la sesion por seguridad
	 al
	 (httpSession != null) {
			pSession.invalidate();
		}
		if (url != null) {
			try {
				response.sendRedirect(url);
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, trAux.getLogFormatter().f("No se ha podido redirigir al usuario a la URL externa"), e); //$NON-NLS-1$
				Responser.sendError(response, FIReError.INTERNAL_ERROR);
			}
		}  Responser.sendError(response, error);
		}
						
    }

	/**
	 * Establece el mensaje interno de error y redirige a la p&aacute;gina de error interna
	 *
	por el usuario si se forz&oacute; el uso de un proveedor concreto.
	 * @param request Objeto con la petici&oacute;n realizada.
	 * @param response Objeto para la respuesta a la petici&oacute;n.
	 * @param session Sesi&oacute;n con los datos de la transacci&oacute;n.
	 * 
	 * @param operationError Error que debe declararse.
	 * 
	 * @param errorUrlRedirection URL externa de error indicada por la aplicaci&ooacute;n.
	 * 
	 * @param originF             rced {@code true} si se forz&oacute; al uso de un proveedor concreto.
	 */            
	private static vo             d redirectToErrorPage(final HttpS
	 *                            rvletRequest request, final HttpServletResponse response,
			final FireSession sess      on, final FIReError operationError,
			final String errorUrlRedirection, final boolean originForced, fi
	 *                            al TransactionAuxParams trAux) {
		ErrorManager.setError        oSession(session, operationError, originForc
	 *                            d, trAux);
    	if (originForced) {
    		Responser.redirectToExternalUrl(errorUrlRedirection, request, response, trAux);
    	}
    	else {
    		Responser.redirectToUrl(FirePages.PG_SIGNATURE_ERROR, request, response, 
		}
			
		 
			
		