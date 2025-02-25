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
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;


/**
 * Manejador encargado de la recuperaci&oacute;n del error obtenido durante el
 * proceso de una transacci&oacute;n.
 */
public class RecoverErrorManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverErrorManager.class.getName());

	/**
	 * Obtiene el error detectado durante la transacci&oacute;n.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverError(final RequestParameters params, final TransactionAuxParams trAux,
			final HttpServletResponse response) throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);

		final LogTransactionFormatter logF = trAux.getLogFormatter();

        // Comprobamos que se hayan proporcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.PARAMETER_TRANSACTION_ID_NEEDED);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

        // Recuperamos el resto de parametros de la sesion
        final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true, trAux);

        // Cuando se ha pedido el error y no se ha encontrado la sesion, al
        // contrario que en el resto de operaciones, vamos a devolver el error
        // de transaccion invalida como resultado (sendResult) en lugar de como
        // error (sendError) para que se pueda procesar como una respuesta
        // valida, ya que se correspondera muy probablemente a una operacion
        // legitima pero en la que la sesion de usuario hay caducado
        if (session == null) {
    		LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		final TransactionResult result = buildErrorResult(session, FIReError.INVALID_TRANSACTION, trAux);
    		Responser.sendResult(response, result); // Usamos sendResult en lugar de sendError
    		return;
        }

        // Comprobamos si se declaro un error o si este es desconocido
        if (!session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {

        	// Si no se declaro un error, pero sabemos que lo ultimo que se hizo es
        	// redirigir a la pasarela externa para autenticar al usuario, autorizar
        	// la firma o emitir nuevos certificados, se notifica como tal
        	if (session.containsAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_LOGIN)) {
            	LOGGER.warning(logF.f("Ocurrio un error desconocido despues de llamar a la pasarela del proveedor para autenticar al usuario")); //$NON-NLS-1$
            	final TransactionResult result = buildErrorResult(session, FIReError.EXTERNAL_SERVICE_ERROR_TO_LOGIN, trAux);
            	SessionCollector.removeSession(session, trAux);
            	Responser.sendResult(response, result);
        		return;
        	}
        	if (session.containsAttribute(ServiceParams.SESSION_PARAM_REDIRECTED_SIGN)) {
            	LOGGER.warning(logF.f("Ocurrio un error desconocido despues de llamar a la pasarela del proveedor para autorizar la firma en la nube o emitir certificados")); //$NON-NLS-1$
            	final TransactionResult result = buildErrorResult(session, FIReError.EXTERNAL_SERVICE_ERROR_TO_SIGN, trAux);
            	SessionCollector.removeSession(session, trAux);
            	Responser.sendResult(response, result);
        		return;
        	}

        	LOGGER.severe(logF.f("Se ha producido un error del que no se ha establecido el tipo")); //$NON-NLS-1$
            final TransactionResult result = buildErrorResult(session, FIReError.INTERNAL_ERROR, trAux);
        	SessionCollector.removeSession(session, trAux);
        	Responser.sendResult(response, result);
        	return;
        }

        LOGGER.info(logF.f("Se devuelve el error identificado")); //$NON-NLS-1$

        // Recuperamos la informacion de error y eliminamos la sesion
        final TransactionResult result = buildErrorResult(session, trAux);


        SessionCollector.removeSession(session, trAux);
    	Responser.sendResult(response, result);
	}

	private static TransactionResult buildErrorResult(final FireSession session,
			final FIReError error, final TransactionAuxParams trAux) {
		return buildErrorResult(session, error.getCode(), error.getMessage(), trAux);
	}

	private static TransactionResult buildErrorResult(final FireSession session,
			final TransactionAuxParams trAux) {

        final String errorType = session.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
        final String errorMsg = session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);

		return buildErrorResult(session, Integer.parseInt(errorType), errorMsg, trAux);
	}

	private static TransactionResult buildErrorResult(final FireSession session, final int errorCode
			, final String errorMsg, final TransactionAuxParams trAux) {

		final TransactionResult tr = new TransactionResult(
				TransactionResult.RESULT_TYPE_ERROR,
				errorCode,
				errorMsg,
				trAux);

		if (session != null) {
			tr.setProviderName(session.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN));
		}

		return tr;
	}
}
