/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.test.webapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.BatchResult;
import es.gob.fire.client.ClientConfigFilesNotFoundException;
import es.gob.fire.client.HttpOperationException;
import es.gob.fire.client.InvalidTransactionException;
import es.gob.fire.client.TransactionResult;

/**
 * Clase de ayuda para la gesti&oacute;n de las peticiones de la operativa de firma de lotes.
 */
public class BatchHelper {

	private static Logger LOGGER = LoggerFactory.getLogger(BatchHelper.class);

	/**
	 * Recupera el resultado de la firma del lote.
	 * @param request Petici&oacute;n del resultado de la firma de lote.
	 * @return Listado con los resultados de cada firma individual.
	 * @throws HttpOperationException Cuando se produce algun error al recuperar el resultado.
	 * @throws IOException Cuando no se puede procesar el resultado.
	 * @throws InvalidTransactionException Cuando la transacci&oacute;n indicada no exista o este caducada.
	 */
	public static BatchResult recoverBatchResult(final HttpServletRequest request) throws IOException, HttpOperationException, InvalidTransactionException {

		// El identificador de aplicacion es propio de cada aplicacion. En esta de ejemplo,
    	// se lee del fichero de configuracion
    	final String appId = ConfigManager.getInstance().getAppId();

    	// Recuperamos de la sesion el ID de transaccion que guardamos al crear el lote
        final HttpSession session = request.getSession(false);
        final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
        if (transactionId == null || transactionId.isEmpty()) {
            // Cancelamos la sesion
            session.invalidate();
            throw new IllegalArgumentException(
                    "No se definieron todos los atributos de sesion necesarios"); //$NON-NLS-1$
        }

        final String userId = (String) session.getAttribute("user"); //$NON-NLS-1$

        final BatchResult batchResult;
        try {
        	batchResult = ConfigManager.getInstance().getFireClient(appId).recoverBatchResult(transactionId, userId);
        }
        catch (final ClientConfigFilesNotFoundException e) {
        	LOGGER.error("No se encontro el fichero de configuracion del componente cliente FIRe", e); //$NON-NLS-1$
        	throw new IOException("No se encontro el fichero de configuracion del componente cliente FIRe", e); //$NON-NLS-1$
		}

		return batchResult;
	}

	/**
	 * Recupera una de las firmas del lote.
	 * @param request Petici&oacute;n de recuperacion de la firma generada en un proceso de firma de lote.
	 * @return Listado con los resultados de cada firma individual.
	 * @throws HttpOperationException Cuando se produce algun error al recuperar el resultado.
	 * @throws IOException Cuando no se puede procesar el resultado.
	 * @throws InvalidTransactionException Cuando la transacci&oacute;n indicada no exista o este caducada.
	 */
	public static byte[] recoverBatchSign(final HttpServletRequest request) throws IOException, HttpOperationException, InvalidTransactionException {

		// El identificador de aplicacion es propio de cada aplicacion. En esta de ejemplo,
    	// se lee del fichero de configuracion
    	final String appId = ConfigManager.getInstance().getAppId();

    	// Recuperamos de la sesion el ID de transaccion que guardamos al crear el lote
        final HttpSession session = request.getSession(false);
        final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
        if (transactionId == null || transactionId.isEmpty()) {
            // Cancelamos la sesion
            session.invalidate();
            throw new IllegalArgumentException(
                    "No se definieron todos los atributos de sesion necesarios"); //$NON-NLS-1$
        }

        final String userId = (String) session.getAttribute("user"); //$NON-NLS-1$

		final String docId = request.getParameter("docid"); //$NON-NLS-1$
		if (docId == null) {
			// Cancelamos la sesion
            session.invalidate();
            throw new IllegalArgumentException(
                    "No se ha proporcionado un ID de documento"); //$NON-NLS-1$
		}

        final TransactionResult result;
        try {
        	result = ConfigManager.getInstance().getFireClient(appId).recoverBatchSign(transactionId, userId, docId);
        }
        catch (final ClientConfigFilesNotFoundException e) {
        	LOGGER.error("No se encontro el fichero de configuracion del componente cliente FIRe", e); //$NON-NLS-1$
        	throw new IOException("No se encontro el fichero de configuracion del componente cliente FIRe", e); //$NON-NLS-1$
		}

        return result.getResult();
	}
}
