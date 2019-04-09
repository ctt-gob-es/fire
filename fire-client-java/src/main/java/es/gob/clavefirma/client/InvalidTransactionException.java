/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.client;

/**
 * Excepci&oacute;n que se&ntilde;ala que se ha intentado acceder a los datos de una transaccion
 * inexistente o caducada.
 * @author Carlos Gamuci
 */
public class InvalidTransactionException extends HttpOperationException {

	/** Serial Id. */
	private static final long serialVersionUID = 3011445782702735307L;

	/** Construye la excepci&oacute;n. */
	public InvalidTransactionException() {
		super();
	}

	/** Construye la excepci&oacute;n indicando un mensaje.
	 * @param msg Mensaje descriptivo del error. */
	public InvalidTransactionException(final String msg) {
		super(msg);
	}

	/** Construye la excepci&oacute;n indicando la causa.
	 * @param cause Causa de la excepci&oacute;n. */
	public InvalidTransactionException(final Throwable cause) {
		super(cause);
	}

	/** Construye la excepci&oacute;n con su descripci&oacute;n y la causa.
	 * @param msg Mensaje de error.
	 * @param cause Causa de la excepci&oacute;n. */
	public InvalidTransactionException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
