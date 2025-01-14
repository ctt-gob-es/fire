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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.services.FIReError;
import es.gob.fire.server.services.RequestParameters;
import es.gob.fire.server.services.Responser;
import es.gob.fire.server.services.statistics.AuditTransactionRecorder;
import es.gob.fire.server.services.statistics.TransactionRecorder;

/**
 * Manejador que gestiona las peticiones para iniciar el proceso de firma de un lote.
 */
public class SignBatchManager {

	private static final Logger LOGGER = Logger.getLogger(SignBatchManager.class.getName());
	private static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();
	private static final AuditTransactionRecorder AUDITTRANSLOGGER = AuditTransactionRecorder.getInstance();

	/**
     * Inicia el proceso de firma de un lote.
	 * @param request Petici&oacute;n de firma del lote.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @param response Respuesta con el resultado del inicio de firma del lote.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
     */
	public static void signBatch(final HttpServletRequest request, final RequestParameters params,
			final TransactionAuxParams trAux, final HttpServletResponse response) throws IOException {

		// Recogemos los parametros proporcionados en la peticion
    	final String transactionId = params.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
    	final String subjectId = params.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		final String stopOnError = params.getParameter(ServiceParams.HTTP_PARAM_BATCH_STOP_ON_ERROR);

		final LogTransactionFormatter logF = trAux.getLogFormatter();

		// Comprobamos que se hayan prorcionado los parametros indispensables
    	if (transactionId == null || transactionId.isEmpty()) {
    		LOGGER.warning(logF.f("No se ha proporcionado el identificador de transaccion")); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.PARAMETER_TRANSACTION_ID_NEEDED);
    		return;
    	}

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

		// Recuperamos los datos de sesion de la transaccion, lo que tambien comprueba que
		// esa sesion corresponda a ese usuario
    	final FireSession session = SessionCollector.getFireSession(transactionId, subjectId, request.getSession(false), false, true, trAux);
    	if (session == null) {
    		LOGGER.warning(logF.f("La transaccion no se ha inicializado o ha caducado")); //$NON-NLS-1$
    		Responser.sendError(response, FIReError.INVALID_TRANSACTION);
    		return;
    	}

    	final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
    	if (batchResult == null || batchResult.documentsCount() == 0) {
    		final String errorMessage = "Se ha pedido firmar un lote sin documentos. Se aborta la operacion."; //$NON-NLS-1$
    		LOGGER.warning(logF.f(errorMessage));
    		TRANSLOGGER.register(session, false);
    		AUDITTRANSLOGGER.register(session, false, errorMessage);
        	SessionCollector.removeSession(session, trAux);
        	Responser.sendError(response, FIReError.BATCH_NO_DOCUMENTS);
    		return;
    	}

        final TransactionConfig connConfig =
        		(TransactionConfig) session.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		if (connConfig == null || !connConfig.isDefinedRedirectErrorUrl()) {
			final String errorMessage = "No se proporcionaron las URL de redireccion para la operacion"; //$NON-NLS-1$
			LOGGER.warning(logF.f(errorMessage));
			TRANSLOGGER.register(session, false);
			AUDITTRANSLOGGER.register(session, false, errorMessage);
			Responser.sendError(response, FIReError.INTERNAL_ERROR);
			return;
		}

        // Configuramos en la sesion si se debe detener el proceso de error cuando se encuentre uno
        // para tenerlo en cuenta en este paso y los siguientes
        session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_STOP_ON_ERROR, stopOnError);
        session.setAttribute(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION, SessionFlags.OP_SIGN);
        session.setAttribute(ServiceParams.SESSION_PARAM_BATCH_SIGNED, Boolean.TRUE);

        // Guardamos los datos de la transaccion en la coleccion de sesiones y en la sesion del propio servidor
        session.saveIntoHttpSession(request.getSession());
        SessionCollector.commit(session, trAux);

		// Obtenemos la referencia al usuario de la sesion
        final String subjectRef = session.getString(ServiceParams.SESSION_PARAM_SUBJECT_REF);

        // Componemos los parametros del servicio
		final String redirectUrl = ServiceNames.PUBLIC_SERVICE_ENTRY_POINT
    			+ "?" +  ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + transactionId //$NON-NLS-1$ //$NON-NLS-2$
    			+ "&" + ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef; //$NON-NLS-1$ //$NON-NLS-2$;

		// Obtenemos la URL de las paginas web de FIRe (parte publica). Si no se define,
		// se calcula en base a la URL actual
		final String redirectUrlBase = PublicContext.getPublicContext(request);

		// Devolvemos al usuario el ID de la transaccion y la pagina a la que debe dirigir al usuario
        final SignOperationResult result = new SignOperationResult(
        		transactionId,
        		redirectUrlBase + redirectUrl);

        LOGGER.info(logF.f("Devolvemos la URL de redireccion con el ID de transaccion")); //$NON-NLS-1$

        Responser.sendResult(response, result);
	}
}
