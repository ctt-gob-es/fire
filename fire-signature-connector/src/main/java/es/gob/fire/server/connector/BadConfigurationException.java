/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.connector;

/**
 * Excepcion que identifica un error en la configuraci&oacute;n interna del servicio
 * (la establecida mediante ficheros de configuraci&oacute;n, por ejemplo).
 */
public class BadConfigurationException extends RuntimeException {

	/** Serial Id. */
	private static final long serialVersionUID = 2883534636440900361L;

	/**
	 * Se construye la excepci&oacute;n que indica un problema en la configuraci&oacute;n del
	 * servicio.
	 * @param cause Origen del error.
	 */
	public BadConfigurationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Se construye la excepci&oacute;n que indica un problema en la configuraci&oacute;n del
	 * servicio.
	 * @param msg Mensaje descriptivo del error.
	 */
	public BadConfigurationException(final String msg) {
		super(msg);
	}

	/**
	 * Se construye la excepci&oacute;n que indica un problema en la configuraci&oacute;n del
	 * servicio.
	 * @param msg Mensaje descriptivo del error.
	 * @param cause Origen del error.
	 */
	public BadConfigurationException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
