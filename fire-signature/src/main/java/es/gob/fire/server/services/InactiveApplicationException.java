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
 * Excepci&oacute;n que se&ntilde;ala que se ha intentado acceder con identificador de aplicaci&oacute;n
 * deshabilitada.
 */
public class InactiveApplicationException extends Exception {

	/** Serial ID. */
	private static final long serialVersionUID = -8109638837975867461L;

	/**
	 * Acceso con identificador de aplicaci&oacute;n deshabilitada.
	 * @param msg Mensaje descriptivo del error.
	 */
	public InactiveApplicationException(final String msg) {
		super(msg);
	}

	/**
	 * Acceso con identificador de aplicaci&oacute;n deshabilitada.
	 * @param msg Mensaje descriptivo del error.
	 * @param cause Causa del error.
	 */
	public InactiveApplicationException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
