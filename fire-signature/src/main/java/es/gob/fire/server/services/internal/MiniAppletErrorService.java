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

import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.Responser;

/**
 * Servicio para procesar los errores encontrados por el MiniApplet y los clientes nativos.
 */
public class MiniAppletErrorService extends HttpServlet {


	/** Serial Id. */
	private static final long serialVersionUID = 6742462095455032887L;

	private static final Logger LOGGER = Logger.getLogger(MiniAppletErrorService.class.getName());

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		// Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		final TransactionAuxParams trAux = new TransactionAuxParams(null, transactionId);
        final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de error de firma con certificado local")); //$NON-NLS-1$

        final String userRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);

        String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
        if (redirectErrorUrl == null || redirectErrorUrl.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado la URL de redireccion de error")); //$NON-NLS-1$
            SessionCollector.removeSession(transactionId, trAux);
            Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

        // Se recibe el tipo de error pero por ahora no hacemos nada con el
		final String errorMessage = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_MESSAGE);

		final FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, userRef, request.getSession(false), true, false, trAux);
		if (session == null) {
			LOGGER.warning(logF.f("La transaccion %1s no se ha inicializado o ha caducado", transactionId)); //$NON-NLS-1$
			SessionCollector.removeSession(transactionId, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
    		return;
        }

		final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		trAux.setAppId(appId);

        // Obtenenmos la configuracion del conector
        final TransactionConfig connConfig	= (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
        if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
        	LOGGER.severe(logF.f("No se encontro en la sesion la URL de redireccion de error para la operacion")); //$NON-NLS-1$
        	ErrorManager.setErrorToSession(session, FIReError.INTERNAL_ERROR, trAux);
        	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
        	return;
        }

        redirectErrorUrl = connConfig.getRedirectErrorUrl();

        LOGGER.severe(logF.f("Error localizado durante la firma con certificado local: " + errorMessage)); //$NON-NLS-1$

        // Establecemos el mensaje de error y redirigimos a la pagina de error
        ErrorManager.setErrorToSession(session, FIReError.SIGNING, true, errorMessage, trAux);
    	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de error de firma con certificado local")); //$NON-NLS-1$
	}
}
