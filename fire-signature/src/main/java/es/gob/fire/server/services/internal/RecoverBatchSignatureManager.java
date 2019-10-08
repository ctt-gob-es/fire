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
import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.HttpCustomErrors;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;


/**
 * Manejador que gestiona las peticiones para la recuperaci&oacute;n de la firma de un documento
 * concreto dentro de un lote de firma.
 */
public class RecoverBatchSignatureManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverBatchSignatureManager.class.getName());
	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();

	/**
	 * Devuelve el resultado de una firma concreta de un lote. Si es necesario, actualiza la firma.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverSignature(final RequestParameters params, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String appId = params.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String docId = params.getParameter(ServiceParams.HTTP_PARAM_DOCUMENT_ID);

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (docId == null || docId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID del documento")); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

        // Recuperamos el resto de parametros de la sesion
        FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, false);
        if (session == null) {
    		LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode());
    		return;
        }

		// Si la operacion anterior no fue la recuperacion del resultado del lote, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_RECOVER != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true);
		}

        // Comprobamos que previamente se haya recuperado el resultado global del lote
        if (!Boolean.parseBoolean(session.getString(ServiceParams.SESSION_PARAM_BATCH_SIGNED))) {
        	LOGGER.severe(logF.f("Se ha solicitado recuperar una firma de un lote antes que el resultado de un lote")); //$NON-NLS-1$
        	response.sendError(HttpCustomErrors.BATCH_NO_SIGNED.getErrorCode(),
        			HttpCustomErrors.BATCH_NO_SIGNED.getErrorDescription());
        	return;
        }

        // Comprobamos que no se haya declarado ya un error
        if (session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {
        	final String errType = session.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
        	final String errMessage = session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
        	SessionCollector.removeSession(session);
        	LOGGER.warning(logF.f("Ocurrio un error durante la operacion de firma de lote: " + errMessage)); //$NON-NLS-1$
        	sendResult(
        			response,
        			new TransactionResult(
        					TransactionResult.RESULT_TYPE_BATCH_SIGN,
        					Integer.parseInt(errType),
        					errMessage).encodeResult());
        	return;
        }

		LOGGER.info(logF.f("Comprobamos el estado de la firma")); //$NON-NLS-1$

        // Obtenemos el resultado de firma del lote
        final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
        if (batchResult == null || batchResult.documentsCount() == 0) {
            LOGGER.severe(logF.f("No se han encontrado registrados los documentos del lote")); //$NON-NLS-1$
        	TRANSLOGGER.register(session, false);
        	SessionCollector.removeSession(session);
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			buildErrorMessage(OperationError.INVALID_STATE));
        	return;
        }

        // Si fallo la operacion de firma o la firma ya se recupero (momento en el
        // que se marca como erroneo el resultado), se notifica un error en la operacion
        if (batchResult.isSignFailed(docId)) {
            LOGGER.severe(logF.f("El documento solicitado ya se recupero o no se firmo correctamente")); //$NON-NLS-1$
        	SIGNLOGGER.register(session, false, docId);
        	response.sendError(HttpCustomErrors.BATCH_DOCUMENT_FAILED.getErrorCode(),
        			HttpCustomErrors.BATCH_DOCUMENT_FAILED.getErrorDescription());
        	return;
        }

        final String docFilename = batchResult.getDocumentReference(docId);
        if (docFilename == null) {
            LOGGER.severe(logF.f("El documento solicitado no estaba en el lote de firma")); //$NON-NLS-1$
        	SIGNLOGGER.register(session, false, docId);
        	response.sendError(HttpCustomErrors.INVALID_BATCH_DOCUMENT.getErrorCode(),
        			HttpCustomErrors.INVALID_BATCH_DOCUMENT.getErrorDescription());
        	return;
        }

        // Recuperamos el resultado de la firma
        LOGGER.info(logF.f("Se carga el resultado de la firma")); //$NON-NLS-1$
        byte[] signature;
        try {
        	signature = TempFilesHelper.retrieveAndDeleteTempData(docFilename);
        }
        catch (final Exception e) {
        	LOGGER.severe(logF.f("No se encuentra el resultado de la firma del documento: " + e)); //$NON-NLS-1$
        	batchResult.setErrorResult(docId, BatchResult.ERROR_RECOVERING);
        	session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
        	SIGNLOGGER.register(session, false, docId);
        	SessionCollector.commit(session);
        	response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
        			"No se encuentra el resultado de la firma del documento. Es posible que haya caducado o que ya se hubiese recuperado."); //$NON-NLS-1$
        	return;
        }

        // Actualizamos el estado para que quede registrado que no se puede volver a recuperar
        batchResult.setErrorResult(docId, BatchResult.RECOVERED);

        SIGNLOGGER.register(session, true, docId);

        // Revisamos si queda alguna firma valida sin recuperar, en cuyo caso,
        // eliminamos la sesion. Si no, actualizamos el estado
        if (isAllProcessed(batchResult)) {
        	SessionCollector.removeSession(session);
        }
        else {
        	session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
        	SessionCollector.commit(session);
        }

        LOGGER.info(logF.f("Se devuelve el resultado de la firma")); //$NON-NLS-1$

        sendResult(response, signature);
	}

	private static String buildErrorMessage(final OperationError error) {
		return buildErrorMessage(Integer.toString(error.getCode()), error.getMessage());
	}

	private static String buildErrorMessage(final String code, final String message) {
		return "ERR-" + code + ":" + message; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void sendResult(final HttpServletResponse response, final byte[] result) throws IOException {
        final OutputStream output = ((ServletResponse) response).getOutputStream();
        output.write(result);
        output.flush();
        output.close();
	}

	/**
	 * Comprueba si todas las firmas de un lote son err&oacute;neas o ya est&aacute;n procesadas.
	 * @param batchResult Informaci&oacute;n del lote.
	 * @return {@code true} si no quedan firmas v&aacute;lidas pendientes de recuperar, {@code false}
	 * en caso contrario.
	 */
	private static boolean isAllProcessed(final BatchResult batchResult) {
        boolean recovered = true;
        final Iterator<String> it = batchResult.iterator();
        while (it.hasNext() && recovered) {
        	if (!batchResult.isSignFailed(it.next())) {
        		recovered = false;
        	}
        }
        return recovered;
	}
}
