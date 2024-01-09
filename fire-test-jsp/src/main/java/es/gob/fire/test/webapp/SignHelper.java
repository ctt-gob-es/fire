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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.LoggerFactory;

import es.gob.fire.client.ClientConfigFilesNotFoundException;
import es.gob.fire.client.HttpOperationException;
import es.gob.fire.client.InvalidTransactionException;
import es.gob.fire.client.TransactionResult;

/**
 * Realiza la llamada para la firma de datos.
 */
public class SignHelper {

    /**
     * Constructor privado para no permir la instanciaci&oacute;n
     */
    private SignHelper() {
        // no instanciable
    }

    /**
     * Completa el proceso de firma.
     *
     * @param request
     *            Petici&oacute;n en cuya sesi&oacute;n se ha guardado la
     *            configuraci&oacute;n de firma.
     * @return Firma electr&oacute;nica completa.
     * @throws IllegalArgumentException
     *             Cuando no se han establecido los par&aacute;metros de sesi&oacute;n
     *             necesarios.
     * @throws IOException
     *             Cuando ocurre un error en la comunicaci&oacute;n con el servidor central
     *             o no se encuentra la configuraci&oacute;n del componente cliente.
     * @throws HttpOperationException
     * 				Cuando ocurre un error en la composici&oacute;n y recuperaci&oacute;n
     * 				de la firma.
     * @throws InvalidTransactionException Cuando la transacci&oacute;n indicada no exista o este caducada.
     */
    public static TransactionResult recoverSignResult(final HttpServletRequest request)
            throws IllegalArgumentException, IOException, HttpOperationException, InvalidTransactionException {

    	// El identificador de aplicacion es propio de cada aplicacion. En esta de ejemplo,
    	// se lee del fichero de configuracion
    	final String appId = ConfigManager.getInstance().getAppId();

    	// Recuperamos de la sesion el ID de transaccion y el formato de actualizacion que guardamos
    	// antes de redirigir al usuario a las pantallas para la seleccion del certificado de firma
    	// y autenticacion (en SignatureService)
        final HttpSession session = request.getSession(false);
        final String transactionId = (String) session.getAttribute("transactionId"); //$NON-NLS-1$
        final String userId = (String) session.getAttribute("user"); //$NON-NLS-1$
        final String upgrade = (String) session.getAttribute("upgrade"); //$NON-NLS-1$

        if (transactionId == null || transactionId.isEmpty()) {
            // Cancelamos la sesion
            session.invalidate();
            throw new IllegalArgumentException(
                    "No se definieron todos los atributos de sesion necesarios"); //$NON-NLS-1$
        }

        final Properties config = new Properties();
        config.setProperty("updater.ignoreGracePeriod", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        //config.setProperty("updater.allowPartialUpgrade", "true"); //$NON-NLS-1$ //$NON-NLS-2$

        // Recuperamos la firma generada (actualizanda en caso de haberse solicitado)
        TransactionResult result;
        try {
        	result = ConfigManager.getInstance().getFireClient(appId).recoverSignResult(
        			transactionId,
        			userId,
        			upgrade,
        			config
        			);
		} catch (final ClientConfigFilesNotFoundException e) {
			LoggerFactory.getLogger(SignHelper.class).error(
					"No se encuentra el fichero de configuracion del componente distribuido de FIRe", e); //$NON-NLS-1$
			throw new IOException("No se encuentra el fichero de configuracion del cliente Cl@ve Firma", e); //$NON-NLS-1$
		}

        // Si el resultado obtenido es un error, lanzamos una excepcion para notificarlo
        if (result.getState() == TransactionResult.STATE_ERROR) {
        	throw new HttpOperationException(
        			"ERR-" + result.getErrorCode() + //$NON-NLS-1$
        			": " + result.getErrorMessage()); //$NON-NLS-1$
        }

        return  result;
    }
}
