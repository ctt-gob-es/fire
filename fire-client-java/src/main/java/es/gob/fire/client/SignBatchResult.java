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

/**
 * Resultado de cada firma particular dentro de un lote de firma.
 */
public class SignBatchResult {

	private final boolean signed;
	private final String errorType;
	private final GracePeriodInfo gracePeriod;

	/**
	 * Crea el resultado de una firma de un lote indicando
	 * que esta finaliz&oacute; correctamente.
	 */
	SignBatchResult() {
		this.signed = true;
		this.errorType = null;
		this.gracePeriod = null;
	}

	/**
	 * Crea el resultado de una firma de un lote indicando
	 * que esta finaliz&oacute; correctamente, pero que hay
	 * que esperar un periodo de gracia antes de recogerla.
	 */
	SignBatchResult(final GracePeriodInfo gracePeriod) {
		this.signed = true;
		this.errorType = null;
		this.gracePeriod = gracePeriod;
	}

	/**
	 * Crea el resultado de una firma de un lote indicando
	 * que esta no finaliz&oacute; correctamente.
	 * @param detail Detalle del resultado (tipo de error o estado final).
	 */
	SignBatchResult(final boolean signed, final String detail) {
		this.signed = signed;
		this.errorType = detail;
		this.gracePeriod = null;
	}

	/**
	 * Indica si se gener&oacute; la firma del documento.
	 * @return {@code true} si la firma se gener&oacute; correctamente,
	 * {@code false} en caso contrario.
	 */
	public boolean isSigned() {
		return this.signed;
	}

	/**
	 * Detalle del error producido si la firma no se gener&oacute; correctamente.
	 * @return C&oacute;digo de error producido.
	 */
	public String getErrotType() {
		return this.errorType;
	}

	/**
	 * Obtiene el periodo de gracia establecido.
	 * @return Informaci&oacute;n del periodo de gracia o {@code null}
	 * si no se estableci&oacute;.
	 */
	public GracePeriodInfo getGracePeriod() {
		return this.gracePeriod;
	}
}
