/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature;

/**
 * Cuando no ha sido posible establecer la conecion con la base de datos.
 */
public class DBConnectionException extends Exception {

	/** Serial ID. */
	private static final long serialVersionUID = 1057696375310201414L;

	/**
	 * Error de conexi&oacute;n con la base de datos.
	 * @param msg Mensaje descriptivo del error.
	 */
	public DBConnectionException(final String msg) {
		super(msg);
	}

	/**
	 * Error de conexi&oacute;n con la base de datos.
	 * @param msg Mensaje descriptivo del error.
	 * @param cause Causa del error.
	 */
	public DBConnectionException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
