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
import es.gob.fire.server.services.statistics.SignatureLogger;


/**
 * Manejador encargado de la composici&oacute;n de las firmas, su actualizaci&oacute;n
 * de ser preciso, y la devoluci&oacute;n al cliente.
 */
public class RecoverSignResultManager {

	private static Logger LOGGER =  FireSignLogger.getFireSignLogger().getFireLogger().getLogger();
	//private static final Logger LOGGER = Logger.getLogger(RecoverSignResultManager.class.getName());
	private static final SignatureLogger SIGNLOGGER = SignatureLogger.getSignatureLogger();

	/**
	 * Finaliza un proceso de firma y devuelve el resultado del mismo.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverSignature(final RequestParameters params, final HttpServletResponse response)
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

        LOGGER.fine(String.format("TrId %1s: RecoverSignResultManager", transactionId)); //$NON-NLS-1$

        // Recuperamos el resto de parametros de la sesion
        FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, false);
        if (session == null) {
    		LOGGER.warning("La transaccion no se ha inicializado o ha caducado"); //$NON-NLS-1$
    		response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode());
    		return;
        }

        // Si la operacion anterior no fue una recuperacion de resultado de firma,
        // forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_RECOVER != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true);
		}

        // Comprobamos que no se haya declarado ya un error
        if (session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {
        	final String errType = session.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
        	final String errMessage = session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
        	SIGNLOGGER.log(session,false);
        	SessionCollector.removeSession(session);
        	LOGGER.warning("Ocurrio un error durante la operacion de firma de lote: " + errMessage); //$NON-NLS-1$
        	sendResult(
        			response,
        			new TransactionResult(
        					TransactionResult.RESULT_TYPE_BATCH,
        					Integer.parseInt(errType),
        					errMessage).encodeResult());
        	return;
        }

        // Recuperamos el resultado de la firma
        byte[] signResult;
        try {
        	signResult = TempFilesHelper.retrieveAndDeleteTempData(transactionId);

        }
        catch (final Exception e) {
        	LOGGER.warning("No se encuentra la firma generada: " + e); //$NON-NLS-1$
        	SIGNLOGGER.log(session, false);
        	SessionCollector.removeSession(session);
        	response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, "Ha caducado la sesion"); //$NON-NLS-1$
        	return;
        }
        // Se registra resultado de operacion firma
        SIGNLOGGER.log(session, true);

        // Ya no necesitaremos de nuevo la sesion, asi que la eliminamos del pool
        SessionCollector.removeSession(session);

        // Enviamos la firma electronica como resultado
        sendResult(response, signResult);
	}

	private static void sendResult(final HttpServletResponse response, final byte[] result) throws IOException {
		// El servicio devuelve el resultado de la operacion de firma.
        final OutputStream output = ((ServletResponse) response).getOutputStream();
        output.write(result);
        output.flush();
        output.close();
	}
}
