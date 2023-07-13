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
 * Manejador que gestiona las peticiones para conocer el estado un lote de firma que se
 * encuentre actualmente en ejecuci&oacute;n.
 */
public class RecoverBatchStateManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverBatchStateManager.class.getName());

	private static final byte[] RESULT_COMPLETE_OPERATION = "1".getBytes(); //$NON-NLS-1$

	/**
	 * Recupera el porcentage, en forma de cadena, de avance de la firma del lote.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverState(final RequestParameters params, final TransactionAuxParams trAux, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);

		final LogTransactionFormatter logF = trAux.getLogFormatter();

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.PARAMETER_TRANSACTION_ID_NEEDED);
        	return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

        // Recuperamos el resto de parametros de la sesion
        final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, false, trAux);

        // Si no se ha encontrado la session en el pool de sesiones vigentes, se
        // interpreta que estaba caducada
        if (session == null) {
    		LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.INVALID_TRANSACTION);
        	return;
        }

    	// Comprobamos si habia firmas que procesar y, en caso negativo, indicamos que se han procesado todas
    	final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
    	if (batchResult == null || batchResult.documentsCount() == 0) {
    		Responser.sendResult(response, RESULT_COMPLETE_OPERATION);
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

		LOGGER.info(logF.f("Se devuelve el estado del lote")); //$NON-NLS-1$

    	final String progress = Float.toString(1 - (float) pending / numOperations);
    	Responser.sendResult(response, progress.getBytes());
	}
}
