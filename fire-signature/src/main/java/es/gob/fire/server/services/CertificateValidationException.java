/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

/**
 * @author mario
 * Excepci&oacute;n para controlar los posibles errores en la autentificaci&oacute;n del cliente
 * mediante certificado.
 */
public class CertificateValidationException extends Exception {

	private static final long serialVersionUID = -7813116298451323222L;

	private final int httpError;

	/**
	 * Constructor con los siguientes par&acute;metros
	 * @param typeHttpError Codigo del error HTTP que ser&aacute; devuelto.
	 * @param msg Mensaje del error que ser&aacute; devuelto.
	 */
	CertificateValidationException(final int typeHttpError, final String msg) {
		super(msg);
		this.httpError = typeHttpError;
    }

	/**
	 * Constructor con los siguientes par&acute;metros
	 * @param typeHttpError Codigo del error HTTP que ser&aacute; devuelto.
	 * @param msg Mensaje del error que ser&aacute; devuelto.
	 * @param cause Error que orig&oacute; la excepci&oacute;n.
	 */
	CertificateValidationException(final int typeHttpError, final String msg, final Throwable cause) {
		super(msg, cause);
		this.httpError = typeHttpError;
    }

	/**
	 * @return El tipo de error HTTP a lanzar.
	 */
	public int getHttpError() {
		return this.httpError;
	}


}
