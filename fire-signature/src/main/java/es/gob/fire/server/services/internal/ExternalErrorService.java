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
 * Servicio para procesar los errores enviados desde servicios externos de validaci&oacute;n o firma.
 */
public class ExternalErrorService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = 6742462095455032887L;

	private static final Logger LOGGER = Logger.getLogger(ExternalErrorService.class.getName());

	private static final String EXT_ERR_UNKOWN_ERROR = "00"; //$NON-NLS-1$
	private static final String EXT_ERR_AUTENTICATION_ERROR = "01"; //$NON-NLS-1$
	private static final String EXT_ERR_UNKNOWN_USER = "02"; //$NON-NLS-1$
	private static final String EXT_ERR_BLOCKED_USER = "03"; //$NON-NLS-1$
	private static final String EXT_ERR_SIGNING_ERROR = "11"; //$NON-NLS-1$
	private static final String EXT_ERR_INVALID_OPERATION = "12"; //$NON-NLS-1$
	private static final String EXT_ERR_UNSUPPORTED_ALGORITHM = "13"; //$NON-NLS-1$


	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) {

		final String transactionId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String userRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);

		// Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()
        		|| userRef == null || userRef.isEmpty()) {
        	LOGGER.warning("No se han proporcionado los parametros necesarios"); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.FORBIDDEN);
            return;
        }

		// No se guardaran los resultados en cache
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

		final TransactionAuxParams trAux = new TransactionAuxParams(null, transactionId);
        final LogTransactionFormatter logF = trAux.getLogFormatter();

		LOGGER.fine(logF.f("Inicio de la llamada al servicio publico de error tras la redireccion a un servicio externo")); //$NON-NLS-1$

		final String redirectErrorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);

		final FireSession session = SessionCollector.getFireSessionOfuscated(transactionId, userRef, request.getSession(false), false, true, trAux);
		if (session == null) {
        	LOGGER.warning(logF.f("La transaccion %1s no se ha inicializado o ha caducado. Se redirige a la pagina proporcionada en la llamada", transactionId)); //$NON-NLS-1$
        	SessionCollector.removeSession(transactionId, trAux);
        	Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
    		return;
        }

		final String appId = session.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
		trAux.setAppId(appId);

        // Comprobamos si el error deja o no alternativas para seleccionar otro proveedor
        final boolean originForced = Boolean.parseBoolean(session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED));


        // Obtenenmos la configuracion del conector
        final TransactionConfig connConfig	= (TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
        if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
        	LOGGER.severe(logF.f("No se encontro en la sesion la URL de redireccion de error para la operacion")); //$NON-NLS-1$
			SessionCollector.removeSession(session, trAux);
			Responser.redirectToExternalUrl(redirectErrorUrl, request, response, trAux);
        	return;
        }
        // Se recibe el tipo de error pero por ahora no hacemos nada con el
		final String errorCode = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_TYPE);
        LOGGER.severe(logF.f("Error notificado por el servicio externo: " + errorCode)); //$NON-NLS-1$

        // Traducimos el codigo de error del servicio a uno de los tipos de error
        // permitidos que pueden notificar los servicios externos, o al generico si
        // no se notifico o si no era de los soportados
       	final FIReError error = translateError(errorCode);
        LOGGER.info(logF.f("Error traducido: " + errorCode)); //$NON-NLS-1$

        // Establecemos el mensaje de error y redirigimos a la pagina de error
        ErrorManager.setErrorToSession(session, error, originForced, trAux);
		redirectToErrorPage(originForced, connConfig, request, response, trAux);

		LOGGER.fine(logF.f("Fin de la llamada al servicio publico de error de firma con certificado local")); //$NON-NLS-1$
	}

	/**
	 * Traduce entre los errores que puede notificar el servicio externo y los errores
	 * reconocidos por FIRe.
	 * @param errorCode C&oacute;digo de error externo.
	 * @return Error soportado por FIRe.
	 */
	private static FIReError translateError(final String errorCode) {

		FIReError error;

		if (errorCode == null) {
			return FIReError.EXTERNAL_SERVICE_ERROR;
		}

		switch (errorCode) {
		case EXT_ERR_UNKOWN_ERROR:
			error = FIReError.EXTERNAL_SERVICE_ERROR;
			break;
		case EXT_ERR_AUTENTICATION_ERROR:
			error = FIReError.UNAUTHORIZED;
			break;
		case EXT_ERR_UNKNOWN_USER:
			error = FIReError.UNKNOWN_USER;
			break;
		case EXT_ERR_BLOCKED_USER:
			error = FIReError.CERTIFICATE_BLOCKED;
			break;
		case EXT_ERR_SIGNING_ERROR:
			error = FIReError.SIGNING;
			break;
		case EXT_ERR_INVALID_OPERATION:
			error = FIReError.CERTIFICATE_WEAK_REGISTRY;
			break;
		case EXT_ERR_UNSUPPORTED_ALGORITHM:
			error = FIReError.PARAMETER_SIGNATURE_PARAMS_INVALID;
			break;
		default:
			error = FIReError.EXTERNAL_SERVICE_ERROR;
			break;
		}

		return error;
	}

	/**
	 * Redirige a una p&aacute;gina de error. La p&aacute;gina sera de de error de
	 * firma, si existe la posibilidad de
	 * que se pueda reintentar la operaci&oacute;n, o la p&aacute;gina de error
	 * proporcionada por el usuario.
	 *
	 * @param originForced Indica si era obligatorio el uso de un proveedor de firma
	 *                     concreto.
	 * @param connConfig   Configuraci&oacute;n de la transacci&oacute;n.
	 * @param request      Objeto de petici&oacute;n al servlet.
	 * @param response     Objeto de respuesta del servlet.
	 * @param trAux        Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 *
	 */
	private static void redirectToErrorPage(final boolean originForced, final TransactionConfig connConfig,
			final HttpServletRequest request, final HttpServletResponse response, final TransactionAuxParams trAux) {
		if (originForced) {
			Responser.redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response, trAux);
		} else {
			try {
				request.getRequestDispatcher(FirePages.PG_SIGNATURE_ERROR).forward(request, response);
			} catch (final Exception e) {
				Responser.redirectToExternalUrl(connConfig.getRedirectErrorUrl(), request, response, trAux);
			}
		}
	}

}
