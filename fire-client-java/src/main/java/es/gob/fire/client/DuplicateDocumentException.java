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

/**
 * Excepci&oacute;n que denota que se ha intentado agregar un documento a un lote con un identificador
 * que ya se agreg&oacute; anteriormente.
 * @author Carlos Gamuci
 */
public class DuplicateDocumentException extends HttpOperationException {

	/** Serial Id. */
	private static final long serialVersionUID = 3123749200845358933L;

	/**
     * Indica que se ha intentado agregar a un lote un documento con un c&oacute;digo repetido.
     * @param code C&oacute;digo de error.
     * @param msg Mensaje de error.
     */
	public DuplicateDocumentException(final int code, final String msg) {
		super(code, msg);
	}

	/** Construye la excepci&oacute;n con su descripci&oacute;n y la causa.
	 * @param msg Mensaje de error.
	 * @param cause Causa de la excepci&oacute;n. */
	public DuplicateDocumentException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	/** Construye la excepci&oacute;n indicando la causa.
	 * @param cause Causa de la excepci&oacute;n. */
	public DuplicateDocumentException(final Throwable cause) {
		super(cause);
	}
}
