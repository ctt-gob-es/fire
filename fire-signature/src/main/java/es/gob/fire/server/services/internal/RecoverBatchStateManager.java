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

import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.statistics.FireSignLogger;


/**
 * Manejador que gestiona las peticiones para conocer el estado un lote de firma que se
 * encuentre actualmente en ejecuci&oacute;n.
 */
public class RecoverBatchStateManager {

	private static Logger LOGGER =  FireSignLogger.getFireSignLogger().getFireLogger().getLogger();
//	private static final Logger LOGGER = Logger.getLogger(RecoverBatchStateManager.class.getName());

	/**
	 * Recupera el porcentage, en forma de cadena, de avance de la firma del lote.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverState(final RequestParameters params, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        LOGGER.fine(String.format("TrId %1s: RecoverBatchStateManager", transactionId)); //$NON-NLS-1$

        // Recuperamos el resto de parametros de la sesion
        final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, false);

        // Si no se ha encontrado la session en el pool de sesiones vigentes, se
        // interpreta que estaba caducada
        if (session == null) {
    		LOGGER.warning("La transaccion no se ha inicializado o ha caducado"); //$NON-NLS-1$
    		response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode());
        	return;
        }

    	// Comprobamos si habia firmas que procesar y, en caso negativo, indicamos que se han procesado todas
    	final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
    	if (batchResult == null || batchResult.documentsCount() == 0) {
    		sendResult(response, "1".getBytes()); //$NON-NLS-1$
    		return;
		}

    	final int numOperations = batchResult.documentsCount();
    	int pending = numOperations;
    	synchronized (session) {
    		final Integer pendingInt = (Integer) session.getObject(ServiceParams.SESSION_PARAM_BATCH_PENDING_SIGNS);
    		if (pendingInt != null) {
        		pending = pendingInt.intValue();
        	}
    	}

    	final String progress = Float.toString(1 - (float) pending / numOperations);
    	sendResult(response, progress.getBytes());
    	return;
	}

	/**
	 * Envia el XML resultado de la operaci&oacute;n como respuesta del servicio.
	 * @param response Respuesta del servicio.
	 * @param result Resultado de la operaci&oacute;n.
	 * @throws IOException Cuando falla el env&iacute;o.
	 */
	private static void sendResult(final HttpServletResponse response, final byte[] result) throws IOException {
        final OutputStream output = ((ServletResponse) response).getOutputStream();
        output.write(new TransactionResult(TransactionResult.RESULT_TYPE_BATCH, result).encodeResult());
        output.flush();
        output.close();
	}
}
