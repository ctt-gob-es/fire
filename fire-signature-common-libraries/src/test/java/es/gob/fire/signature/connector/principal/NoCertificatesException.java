/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature.connector.principal;

/**
 * No se encontraron certificados.
 */
public class NoCertificatesException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = 2464905143989634556L;

	/**
	 * Crea un error indicando que no se encontraron certificados.
	 * @param msg Mensaje de error.
	 */
	public NoCertificatesException(final String msg) {
		super(msg);
	}
}
