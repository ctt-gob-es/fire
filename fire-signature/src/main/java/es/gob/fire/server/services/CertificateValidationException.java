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
 * Excepci&oacute;n que identifica errores en la validaci&oacute;n del certificado de una petic&oacute;n.
 */
public class CertificateValidationException extends Exception {

	private static final long serialVersionUID = -7813116298451323222L;

	private final FIReError error;

	/**
	 * Construye la excepci&oacute;n.
	 * @param error Error que notificar a los usuarios.
	 * @param msg Mensaje descriptivo del error.
	 */
	public CertificateValidationException(final FIReError error, final String msg) {
		super(msg);
		this.error = error;
    }

	/**
	 * Construye la excepci&oacute;n.
	 * @param error Error que notificar a los usuarios.
	 * @param msg Mensaje descriptivo del error.
	 * @param cause Error que orig&oacute; la excepci&oacute;n.
	 */
	public CertificateValidationException(final FIReError error, final String msg, final Throwable cause) {
		super(msg, cause);
		this.error = error;
    }

	/**
	 * Recupera el error a notificar a los usuarios.
	 * @return Error.
	 */
	public FIReError getError() {
		return this.error;
	}


}
