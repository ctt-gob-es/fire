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
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;


/**
 * Manejador encargado de recuperar el resultado de la operaci&oacute;n de firma (tipo de operacion, si termino bien o mal,
 * etc.). La propia firma no va incluiso en el resultado.
 */
public class RecoverSignResultManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverSignResultManager.class.getName());
	private static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();

	/**
	 * Obtiene el resultado del proceso de firma.
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

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, transactionId);

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (transactionId == null || transactionId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID de transaccion")); //$NON-NLS-1$
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

        // Si la operacion anterior no fue una recuperacion de resultado de firma,
        // forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_RECOVER != session.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			session = SessionCollector.getFireSession(transactionId, subjectId, null, false, true);
		}

        // Comprobamos que no se haya declarado ya un error
        if (session.containsAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE)) {
        	final String errType = session.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
        	final String errMessage = session.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
        	SIGNLOGGER.register(session, false, null);
        	TRANSLOGGER.register(session, false);
        	SessionCollector.removeSession(session);
        	LOGGER.warning(logF.f("Ocurrio un error durante la operacion de firma de lote: " + errMessage)); //$NON-NLS-1$
        	sendResult(
        			response,
        			new TransactionResult(
        					TransactionResult.RESULT_TYPE_SIGN,
        					Integer.parseInt(errType),
        					errMessage).encodeResult());
        	return;
        }

        // Recuperamos el resultado de la firma
		LOGGER.info(logF.f("Se carga el resultado de la operacion del almacen temporal")); //$NON-NLS-1$
        byte[] signResult;
        try {
        	signResult = TempDocumentsManager.retrieveDocument(transactionId);
        }
        catch (final Exception e) {
        	LOGGER.warning(logF.f("No se encuentra el resultado de la operacion: " + e)); //$NON-NLS-1$
        	SIGNLOGGER.register(session, false, null);
        	TRANSLOGGER.register(session, false);
        	SessionCollector.removeSession(session);
        	response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, "Ha caducado la sesion"); //$NON-NLS-1$
        	return;
        }

        // Se registra resultado de operacion firma
        SIGNLOGGER.register(session, true, null);
        TRANSLOGGER.register(session, true);

        // Ya no necesitaremos de nuevo la sesion, asi que la eliminamos del pool
        SessionCollector.removeSession(session);

        LOGGER.info(logF.f("Se devuelve el resultado de la operacion")); //$NON-NLS-1$

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
