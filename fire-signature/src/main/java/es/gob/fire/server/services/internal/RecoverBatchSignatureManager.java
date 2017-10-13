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


/**
 * Manejador que gestiona las peticiones para la recuperaci&oacute;n de la firma de un documento
 * concreto dentro de un lote de firma.
 */
public class RecoverBatchSignatureManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverBatchSignatureManager.class.getName());

	/**
	 * Devuelve el resultado de una firma concreta de un lote. Si es necesario, actualiza la firma.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverSignature(final RequestParameters params, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String docId = params.getParameter(ServiceParams.HTTP_PARAM_DOCUMENT_ID);

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID de transaccion"); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (docId == null || docId.isEmpty()) {
        	LOGGER.warning("No se ha proporcionado el ID del documento"); //$NON-NLS-1$
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Recuperamos el resto de parametros de la sesion
        final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false);

        // Si no se ha encontrado la session en el pool de sesiones vigentes, se
        // interpreta que estaba caducada
        if (session == null) {
    		LOGGER.warning("La transaccion no se ha inicializado o ha caducado"); //$NON-NLS-1$
    		response.sendError(HttpCustomErrors.INVALID_TRANSACTION.getErrorCode());
    		return;
        }

        // Comprobamos que previamente se haya recuperado el resultado global del lote
        if (!Boolean.parseBoolean(session.getString(ServiceParams.SESSION_PARAM_BATCH_SIGNED))) {
        	LOGGER.severe("Se ha solicitado recuperar una firma de un lote antes que el resultado de un lote"); //$NON-NLS-1$
        	response.sendError(HttpCustomErrors.BATCH_NO_SIGNED.getErrorCode(),
        			HttpCustomErrors.BATCH_NO_SIGNED.getErrorDescription());
        	return;
        }

        // Comprobamos que no se haya declarado ya un error
        if (session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {
        	SessionCollector.removeSession(session);
        	LOGGER.warning("Ocurrio un error durante la operacion de firma de lote: " + //$NON-NLS-1$
        			session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE));
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			buildErrorMessage(
        					session.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE),
        					session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE)));
        	return;
        }

        // Obtenemos el resultado de firma del lote
        final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
        if (batchResult == null || batchResult.documentsCount() == 0) {
            LOGGER.severe("No se han encontrado registrados los documentos del lote"); //$NON-NLS-1$
        	SessionCollector.removeSession(session);
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        			buildErrorMessage(OperationError.INVALID_STATE));
        	return;
        }

        // Si fallo la operacion de firma de ese documento, se notifica un error
        // en la operacion
        if (batchResult.isSignFailed(docId)) {
            LOGGER.severe("El documento solicitado no se firmo correctamente"); //$NON-NLS-1$
        	response.sendError(HttpCustomErrors.BATCH_DOCUMENT_FAILED.getErrorCode(),
        			HttpCustomErrors.BATCH_DOCUMENT_FAILED.getErrorDescription());
        	return;
        }

        final String docFilename = batchResult.getDocumentReference(docId);
        if (docFilename == null) {
            LOGGER.severe("El documento solicitado no estaba en el lote de firma"); //$NON-NLS-1$
        	response.sendError(HttpCustomErrors.INVALID_BATCH_DOCUMENT.getErrorCode(),
        			HttpCustomErrors.INVALID_BATCH_DOCUMENT.getErrorDescription());
        	return;
        }

        byte[] signature;
        try {
        	signature = TempFilesHelper.retrieveAndDeleteTempData(docFilename);
        }
        catch (final Exception e) {
        	LOGGER.severe("No se encuentra la firma del documento: " + e); //$NON-NLS-1$
        	batchResult.setErrorResult(docId, BatchResult.ERROR_RECOVERING);
        	session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
        	SessionCollector.commit(session);
        	response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
        			"No se encuentra la firma del documento. Es posible que haya caducado o que ya se hubiese recuperado."); //$NON-NLS-1$
        	return;
        }

        // Actualizamos el estado para que quede registrado que no se puede volver a recuperar
        batchResult.setErrorResult(docId, BatchResult.RECOVERED);

        // Revisamos si queda alguna firma valida sin recuperar, en cuyo caso,
        // eliminamos la sesion. Si no, actualizamos el estado
        if (isAllProcessed(batchResult)) {
        	SessionCollector.removeSession(session);
        }
        else {
        	session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
        	SessionCollector.commit(session);
        }

        // Enviamos la firma electronica como resultado
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
        output.write(new TransactionResult(TransactionResult.RESULT_TYPE_BATCH_SIGN, result).encodeResult());
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
