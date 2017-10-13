/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.test.services;

/**
 * Cuando se ha introducido un usuario no dado de alta en el sistema.
 * @author Carlos Gamuci
 */
public class InvalidUserException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -7527928048792741875L;

	/**
	 * Crea la excepci&oacute;n con un mensaje descriptivo.
	 * @param msg Descripci&oacute;n del error.
	 */
	public InvalidUserException(final String msg) {
		super(msg);
	}

	/**
	 * Crea la excepci&oacute;n con un mensaje descriptivo.
	 * @param msg Descripci&oacute;n del error.
	 * @param cause Origen del error.
	 */
	public InvalidUserException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
