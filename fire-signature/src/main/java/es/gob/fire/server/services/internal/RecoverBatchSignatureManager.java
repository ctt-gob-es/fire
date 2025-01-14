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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.server.services.statistics.AuditSignatureRecorder;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.signature.ConfigManager;


/**
 * Manejador que gestiona las peticiones para la recuperaci&oacute;n de la firma de un documento
 * concreto dentro de un lote de firma.
 */
public class RecoverBatchSignatureManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverBatchSignatureManager.class.getName());
	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final AuditSignatureRecorder AUDITSIGNLOGGER = AuditSignatureRecorder.getInstance();

	/**
	 * Devuelve el resultado de una firma concreta de un lote. Si es necesario, actualiza la firma.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverSignature(final RequestParameters params, final TransactionAuxParams trAux, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String docId = params.getParameter(ServiceParams.HTTP_PARAM_DOCUMENT_ID);

		final LogTransactionFormatter logF = trAux.getLogFormatter();

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.PARAMETER_TRANSACTION_ID_NEEDED);
            return;
        }

        if (docId == null || docId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID del documento")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.PARAMETER_DOCUMENT_ID_NEEDED);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

        // Cargamos los datos de la transaccion
        final FireSession session = loadSession(transactionId, subjectId, trAux);
        if (session == null) {
        	Responser.sendError(response, FIReError.INVALID_TRANSACTION);
        	return;
        }

        // Comprobamos que previamente se haya recuperado el resultado global del lote
        if (!Boolean.TRUE.equals(session.getObject(ServiceParams.SESSION_PARAM_BATCH_RECOVERED))) {
        	LOGGER.severe(logF.f("Se ha solicitado recuperar una firma de un lote antes que el resultado de un lote")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.BATCH_NO_SIGNED);
        	return;
        }

        // Comprobamos que no se haya declarado ya un error
        if (session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {
        	final String errType = session.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
        	final String errMessage = session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
        	SessionCollector.removeSession(session, trAux);
        	LOGGER.warning(logF.f("Ocurrio un error durante la operacion de firma de lote: " + errMessage)); //$NON-NLS-1$
        	Responser.sendError(
        			response,
        			FIReError.BATCH_SIGNING,
        			new TransactionResult(
        					TransactionResult.RESULT_TYPE_BATCH_SIGN,
        					Integer.parseInt(errType),
        					errMessage,
        					trAux));
        	return;
        }

		LOGGER.fine(logF.f("Comprobamos el estado de la firma")); //$NON-NLS-1$

        // Obtenemos el resultado de firma del lote
        final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
        if (batchResult == null || batchResult.documentsCount() == 0) {
            LOGGER.severe(logF.f("No se han encontrado documentos registrados en el lote")); //$NON-NLS-1$
        	SessionCollector.removeSession(session, trAux);
        	Responser.sendError(response, FIReError.INTERNAL_ERROR);
        	return;
        }

        // Si la firma ya se recupero, se notifica un error en la operacion
        if (batchResult.isSignRecovered(docId)) {
            LOGGER.severe(logF.f("El documento solicitado del lote ya se recupero")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.BATCH_RECOVERED);
        	return;
        }

        // Si fallo la operacion de firma, se notifica un error en la operacion
        if (batchResult.isSignFailed(docId)) {
            LOGGER.severe(logF.f("El documento solicitado del lote no se firmo correctamente")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.BATCH_SIGNING);
        	return;
        }

        // Si para recuperar la firma hay que esperar un periodo de gracia, se notifica un error en la operacion
        if (batchResult.needWaitGracePeriod(docId)) {
            LOGGER.info(logF.f("El documento solicitado del lote no estara disponible hasta esperar un periodo de gracia")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.BATCH_DOCUMENT_GRACE_PERIOD);
        	return;
        }

        final String docFilename = batchResult.getDocumentReference(docId);
        if (docFilename == null) {
            LOGGER.severe(logF.f("El documento solicitado no estaba en el lote de firma")); //$NON-NLS-1$
            Responser.sendError(response, FIReError.BATCH_INVALID_DOCUMENT);
        	return;
        }

        // Recuperamos el resultado de la firma
        LOGGER.info(logF.f("Se carga el resultado de la firma")); //$NON-NLS-1$
        byte[] signature;
        try {
        	signature = TempDocumentsManager.retrieveAndDeleteDocument(docFilename);
        }
        catch (final Exception e) {
        	final String errorMessage = String.format("No se encuentra el resultado de la firma del documento: %1s. Puede haber caducado la sesion", LogUtils.cleanText(docId)); //$NON-NLS-1$
        	LOGGER.log(Level.SEVERE, logF.f(errorMessage), e);
        	batchResult.setErrorResult(docId, BatchResult.ERROR_RECOVERING);
        	batchResult.setErrorMessage(docId, errorMessage);
        	session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
        	SIGNLOGGER.register(session, false, docId);
        	AUDITSIGNLOGGER.register(session, false, docId, errorMessage);

        	SessionCollector.commit(session, trAux);
        	Responser.sendError(response, FIReError.INVALID_TRANSACTION);
        	return;
        }

        // Actualizamos el estado para que quede registrado que no se puede volver a recuperar
        batchResult.setErrorResult(docId, BatchResult.RECOVERED);

        SIGNLOGGER.register(session, true, docId);
        AUDITSIGNLOGGER.register(session, true, docId);

        // Revisamos si queda alguna firma valida sin recuperar, en cuyo caso,
        // eliminamos la sesion. Si no, actualizamos el estado
        if (isAllProcessed(batchResult)) {
        	SessionCollector.removeSession(session, trAux);
        }
        else {
        	session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT, batchResult);
        	SessionCollector.commit(session, trAux);
        }

        LOGGER.info(logF.f("Se devuelve el resultado de la firma")); //$NON-NLS-1$

        Responser.sendResult(response, signature);
	}

	private static FireSession loadSession(final String transactionId, final String subjectId, final TransactionAuxParams trAux) {

        FireSession session = SessionCollector.getFireSession(transactionId, subjectId, null, false, ConfigManager.isSessionSharingForced(), trAux);
        if (session == null && ConfigManager.isSessionSharingForced()) {
    		LOGGER.warning(trAux.getLogFormatter().f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		return null;
        }

		// Si no se ha encontrado la sesion o si la operacion anterior no fue la recuperacion
        // del resultado del lote, forzamos a que se recargue por si faltan datos
		if (session == null || SessionFlags.OP_RECOVER != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			LOGGER.info(trAux.getLogFormatter().f("No se encontro la sesion o no estaba actualizada. Forzamos la carga")); //$NON-NLS-1$
			session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true, trAux);
		}

		return session;
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
        	final String docId = it.next();
        	// El documento no estara procesado si no es un fallo (los que ya se hayan recuperado se consideran fallados)
        	// y si no esta pendiente de la espera de un periodo de gracia (que no se considera fallo)
        	if (!batchResult.isSignFailed(docId) && !batchResult.needWaitGracePeriod(docId)) {
        		recovered = false;
        	}
        }
        return recovered;
	}
}
