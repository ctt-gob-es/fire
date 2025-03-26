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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;

/**
 * Servicio interno para registrar el error de cancelaci&oacute;n de la operaci&oacute;n y
 * redirigir a la pantalla correspondiente.
 */
public class CancelOperationService extends HttpServlet {

	/** SerialId. */
	private static final long serialVersionUID = 2777535691667138137L;

	private static final Logger LOGGER = Logger.getLogger(CancelOperationService.class.getName());

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) {

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		// Leemos los parametros de la peticion
		final RequestParameters params;
		try {
			params = RequestParameters.parseParameters(request, false);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error en la lectura de los parametros de entrada", e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.READING_PARAMETERS);
			return;
		}

		// Recuperamos el identificador de transaccion
		final String trId = params.getTransactionId();
		if (trId == null || trId.isEmpty()) {
			LOGGER.warning("No se ha proporcionado el identificador de transaccion"); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		final TransactionAuxParams trAux = new TransactionAuxParams(null, trId);
		final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de cancelacion")); //$NON-NLS-1$

		try {
			params.checkParameters(logF);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, logF.f("Error en la comprobacion de los parametros de entrada"), e); //$NON-NLS-1$
			Responser.sendError(response, FIReError.FORBIDDEN);
			return;
		}

		final String userRef = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
		String redirectErrorUrl = params.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		// Comprobamos que se hayan prorcionado los parametros indispensables
        if (userRef == null || redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
        	LOGGER.warning(logF.f("No se han proporcionado todos los parametros necesarios")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

        try {
        	redirectErrorUrl = URLDecoder.decode(redirectErrorUrl, StandardCharsets.UTF_8.name());
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se pudo deshacer el URL Encoding de la URL de redireccion: ") + e); //$NON-NLS-1$
		}

		final FireSession session = SessionCollector.getFireSessionOfuscated(trId, userRef, request.getSession(false), false, true, trAux);
    	if (session == null) {
        	LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado. Se redirige a la pagina proporcionada en la llamada")); //$NON-NLS-1$
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
    		return;
    	}

    	final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
    	logF.setAppId(appId);

		final TransactionConfig connConfig = (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			LOGGER.warning(logF.f("No se encontro en la sesion la URL de redireccion de la operacion. Se redirige a la proporcionada en la peticion")); //$NON-NLS-1$
			SessionCollector.removeSession(session, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
			return;
		}

		AlarmsManager.notify(Alarm.CANCELLED_OPERATION);

		ErrorManager.setErrorToSession(session, FIReError.OPERATION_CANCELED, trAux);

		Responser.redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response, trAux);

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de cancelacion")); //$NON-NLS-1$
	}
}
