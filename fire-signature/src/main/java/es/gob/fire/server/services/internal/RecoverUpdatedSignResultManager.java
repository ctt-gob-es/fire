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
 * Manejador encargado de recuperar la firma actualizada, previamente recuperada de la
 * plataforma de actualizaci&oacute;n de firmas.
 */
public class RecoverUpdatedSignResultManager {

	private static final Logger LOGGER = Logger.getLogger(RecoverUpdatedSignResultManager.class.getName());

	/**
	 * Obtiene de la plataforma de actualizaci&oacute;n la firma actualizada.
	 * @param params Par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @param response Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando se produce un error de lectura o env&iacute;o de datos.
	 */
	public static void recoverSignature(final RequestParameters params, final TransactionAuxParams trAux, final HttpServletResponse response)
			throws IOException {

		// Recogemos los parametros proporcionados en la peticion
		final String docId = params.getParameter(ServiceParams.HTTP_PARAM_DOCUMENT_ID);

		final LogTransactionFormatter logF = trAux.getLogFormatter();

        // Comprobamos que se hayan prorcionado los parametros indispensables
        if (docId == null || docId.isEmpty()) {
        	LOGGER.warning(logF.f("No se ha proporcionado el ID devuelto por la plataforma para la recuperacion de la firma")); //$NON-NLS-1$
        	Responser.sendError(response, FIReError.PARAMETER_DOCUMENT_ID_NEEDED);
            return;
        }

		LOGGER.fine(logF.f("Peticion bien formada")); //$NON-NLS-1$

		// Recuperamos el resultado de la firma
		LOGGER.fine(logF.f("Se carga el resultado de la operacion del almacen temporal")); //$NON-NLS-1$
		byte[] signResult;
		try {
			signResult = TempDocumentsManager.retrieveAndDeleteDocument(docId);
		}
		catch (final Exception e) {
			LOGGER.warning(logF.f("No se encuentra la firma actualizada en el almacen temporal. Puede hacer caducado la sesion: " + e)); //$NON-NLS-1$
			Responser.sendError(response, FIReError.INVALID_TRANSACTION);
			return;
		}

		LOGGER.info(logF.f("Se devuelve la firma actualizada")); //$NON-NLS-1$

		// Enviamos la firma electronica como resultado
		Responser.sendResult(response, signResult);
	}
}
