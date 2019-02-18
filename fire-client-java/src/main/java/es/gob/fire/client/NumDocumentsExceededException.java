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

/** Excepci&oacute;n que denota que se ha sobrepasado el n&uacute;mero m&aacute;ximo
 * de documentos. Normalmente, se referir&aacute; al n&uacute;mero de documentos
 * permitidos en un lote.
 * @author Carlos Gamuci. */
public final class NumDocumentsExceededException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = 3410961465765079806L;

	/** Construye la excepci&oacute;n. */
	public NumDocumentsExceededException() {
		super();
	}

	/** Construye la excepci&oacute;n con su descripci&ocute;n y la causa.
	 * @param msg Mensaje de error.
	 * @param cause Causa de la excepci&oacute;n. */
	public NumDocumentsExceededException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	/** Construye la excepci&oacute;n indicando la causa.
	 * @param cause Causa de la excepci&oacute;n. */
	public NumDocumentsExceededException(final Throwable cause) {
		super(cause);
	}
}
