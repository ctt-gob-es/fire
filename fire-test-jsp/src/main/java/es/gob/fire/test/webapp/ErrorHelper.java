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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.fire.client.ClientConfigFilesNotFoundException;
import es.gob.fire.client.HttpOperationException;
import es.gob.fire.client.InvalidTransactionException;
import es.gob.fire.client.TransactionResult;

/**
 * Clase de ayuda para obtener un error detectado durante una transacci&oacute;n.
 */
public class ErrorHelper {

    /**
     * Constructor privado para no permir la instanciaci&oacute;n
     */
    private ErrorHelper() {
        // no instanciable
    }

    /**
     * Recupera el error obtenido durante una transacci&oacute;n.
     *
     * @param request
     *            Petici&oacute;n en cuya sesion se ha guardado el error.
     * @return Mensaje de error.
     * @throws IllegalArgumentException
     *             Cuando la sesi&oacute;n o alguno de los par&aacute;ametros necesarios
     *             falta o est&aacute; vac&iacute;o.
     * @throws IOException
     *             Cuando ocurre un error al cargar el fichero de configuraci&oacute;n o
     *             durante la lectura del resultado..
     * @throws HttpOperationException
     * 				Cuando ocurre un error durante la operaci&oacute;n.
     * @throws InvalidTransactionException
     * 				Cuando la transacci&oacute;n no existe o caducado.
     */
    public static TransactionResult recoverErrorResult(final HttpServletRequest request)
            throws IllegalArgumentException, IOException, HttpOperationException, InvalidTransactionException {

    	// El identificador de aplicacion es propio de cada aplicacion. En esta de ejemplo,
    	// se lee del fichero de configuracion
    	final String appId = ConfigManager.getInstance().getAppId();

    	// Recuperamos de la sesion el ID de transaccion que obtuvimos y guardamos al iniciar
    	// la operaci&oacute;n
        final HttpSession session = request.getSession(false);
        final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
        final String subjectId = (String) session.getAttribute("user"); //$NON-NLS-1$

        if (transactionId == null || transactionId.isEmpty()) {
            // Cancelamos la sesion
            session.invalidate();
            throw new IllegalArgumentException(
                    "No se definieron todos los atributos de sesion necesarios"); //$NON-NLS-1$
        }

        // Recuperamos el error
        TransactionResult result;
        try {
        	result = ConfigManager.getInstance().getFireClient(appId).recoverErrorResult(transactionId, subjectId);
		} catch (final ClientConfigFilesNotFoundException e) {
			Logger.getLogger(ErrorHelper.class.getName()).log(
					Level.SEVERE, "No se encuentra el fichero de configuracion del cliente Cl@ve Firma", e); //$NON-NLS-1$
			throw new IOException("No se encuentra el fichero de configuracion del cliente Cl@ve Firma", e); //$NON-NLS-1$
		}
        return  result;
    }
}
