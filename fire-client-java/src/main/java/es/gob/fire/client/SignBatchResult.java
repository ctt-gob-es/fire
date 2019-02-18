/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.client;

/** Resultado de cada firma particular dentro de un lote de firma. */
public final class SignBatchResult {

	private final boolean signed;
	private final String errotType;

	/** Crea el resultado de una firma de un lote indicando
	 * que esta finaliz&oacute; correctamente. */
	public SignBatchResult() {
		this.signed = true;
		this.errotType = null;
	}

	/** Crea el resultado de una firma de un lote indicando
	 * que esta no finaliz&oacute; correctamente.
	 * @param errorType Tipo de error de la firma. */
	public SignBatchResult(final String errorType) {
		this.signed = false;
		this.errotType = errorType;
	}

	/** Indica si se gener&oacute; la firma del documento.
	 * @return {@code true} si la firma se gener&oacute; correctamente,
	 * {@code false} en caso contrario. */
	public boolean isSigned() {
		return this.signed;
	}

	/** Motivo por el que la firma no finaliz&oacute; correctamente.
	 * @return Tipo de error registrado durante la firma. */
	public String getErrotType() {
		return this.errotType;
	}
}
