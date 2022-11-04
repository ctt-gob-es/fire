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

import java.nio.charset.Charset;

import es.gob.fire.server.connector.OperationResult;

/**
 * Resultado de la operaci&oacute;n de creaci&oacute;n de un lote.
 */
public class CreateBatchResult  extends OperationResult {

	private final String transactionId;

	/**
	 * Crea un objeto con el resultado de crear un lote de firmas.
	 * @param transactionId Identificador de transacci&oacute;n asociada al lote.
	 */
	public CreateBatchResult(final String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * Obtiene el Id de la transacci&oacute;n asociada a la operaci&oacute;n
	 * de creaci&oacute;n de lote.
	 * @return Identificador de la transacci&oacute;n.
	 */
	public String getTransactionId() {
		return this.transactionId;
	}

	/**
	 * Crea un resultado de creaci&oacute;n de lote  a partir del resultado
	 * del servicio correspondiente.
	 * @param result Resultado del servicio de creaci&oacute;n de lote.
	 * @return Resultado de la creaci&oacute;n de un lote.
	 */
	public static CreateBatchResult parse(final String result) {
		return new CreateBatchResult(result);
	}

	/**
	 * Obtiene el resultado de la creaci&oacute;n del lote como documento JSON.
	 * @param charset Juego de caracteres.
	 * @return Resultado codificado en forma de JSON.
	 */
	@Override
	public byte[] encodeResult(final Charset charset) {
		return ("{\"transactionid\": \"" + this.transactionId + "\"}").getBytes(charset); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
