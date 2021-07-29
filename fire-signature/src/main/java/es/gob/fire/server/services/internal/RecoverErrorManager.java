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
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.RequestParameters;


/**
 * Manejador encargado de la recuperaci&oacute;n del error obtenido durante el
 * proceso de una transacci&oacute;n.
 */
public class RecoverErrorManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverErrorManager.class.getName());

	/**
	 * Obtiene el error detectado durante la transacci&oacute;n.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverError(final RequestParameters params, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String appId = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

        // Comprobamos que se hayan proporcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

        // Recuperamos el resto de parametros de la sesion
        final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true);
        if (session == null) {
    		LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		sendResult(response, buildErrorResult(session, OperationError.INVALID_SESSION));
    		return;
        }

        // Comprobamos si se declaro un error o si este es desconocido
        if (!session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {

        	// Si no se declaro un error, pero sabemos que lo ultimo que se hizo es
        	// redirigir a la pasarela externa para autenticar al usuario, autorizar
        	// la firma o emitir nuevos certificados, se notifica como tal
        	if (session.containsAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_LOGIN)) {
            	LOGGER.warning(logF.f("Ocurrio un error desconocido despues de llamar a la pasarela del proveedor para autenticar al usuario")); //$NON-NLS-1$
            	final TransactionResult result = buildErrorResult(session, OperationError.EXTERNAL_SERVICE_ERROR_TO_LOGIN);
            	SessionCollector.removeSession(session);
            	sendResult(response, result);
        		return;
        	}
        	if (session.containsAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_SIGN)) {
            	LOGGER.warning(logF.f("Ocurrio un error desconocido despues de llamar a la pasarela del proveedor para autorizar la firma en la nube o emitir certificados")); //$NON-NLS-1$
            	final TransactionResult result = buildErrorResult(session, OperationError.EXTERNAL_SERVICE_ERROR_TO_SIGN);
            	SessionCollector.removeSession(session);
            	sendResult(response, result);
        		return;
        	}

        	LOGGER.warning(logF.f("No se ha notificado el tipo de error de la transaccion")); //$NON-NLS-1$
            final TransactionResult result = buildErrorResult(session, OperationError.UNDEFINED_ERROR);
        	SessionCollector.removeSession(session);
        	sendResult(response, result);
        	return;
        }

        LOGGER.info(logF.f("Se devuelve el error identificado")); //$NON-NLS-1$

        // Recuperamos la informacion de error y eliminamos la sesion
        final TransactionResult result = buildErrorResult(session);


        SessionCollector.removeSession(session);
    	sendResult(response, result);
	}

	private static TransactionResult buildErrorResult(final FireSession session, final OperationError error) {
		return buildErrorResult(session, error.getCode(), error.getMessage());
	}

	private static TransactionResult buildErrorResult(final FireSession session) {

        final String errorType = session.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
        final String errorMsg = session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);

		return buildErrorResult(session, Integer.parseInt(errorType), errorMsg);
	}

	private static TransactionResult buildErrorResult(final FireSession session, final int errorCode, final String errorMsg) {

		final TransactionResult tr = new TransactionResult(
				TransactionResult.RESULT_TYPE_ERROR,
				errorCode,
				errorMsg);

		if (session != null) {
			tr.setProviderName(session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN));
		}

		return tr;
	}

	private static void sendResult(final HttpServletResponse response, final TransactionResult result) throws IOException {
		// El servicio devuelve el resultado de la operacion de firma.
        final OutputStream output = ((ServletResponse) response).getOutputStream();
        output.write(result.encodeResult());
        output.flush();
        output.close();
	}
}
