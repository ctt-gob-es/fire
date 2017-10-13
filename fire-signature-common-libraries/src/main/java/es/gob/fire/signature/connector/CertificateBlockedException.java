/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature.connector;

/**
 * Excepci&oacute;n que describe el caso en el que un certificado est&aacute; bloqueado
 * y no puede usarse temporalmente.
 */
public class CertificateBlockedException extends FIReConnectorException {

	/** Serial Id. */
	private static final long serialVersionUID = -8612865318316942713L;

	/**
	 * Construye la excepci&oacute;n con un mensaje descritor.
	 * @param msg Descripci&oacute;n del problema.
	 */
	public CertificateBlockedException(final String msg) {
		super(msg);
	}

	/**
	 * Construye la excepci&oacute;n con un mensaje descritor y el motivo del problema.
	 * @param msg Descripci&oacute;n del problema.
	 * @param cause Origen del problema.
	 */
	public CertificateBlockedException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
