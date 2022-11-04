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
 * El usuario realiz&oacute; un registro d&eacute;bil y no puede tener certificados
 * de firma.
 */
public final class HttpWeakRegistryException extends HttpOperationException {

	/** Serial ID. */
	private static final long serialVersionUID = -6238299869930902680L;

	/**
     * Indica que un usuario no puede tener certificados de firma.
     * @param code C&oacute;digo de error.
     * @param msg Mensaje de error.
     */
	public HttpWeakRegistryException(final int code, final String msg) {
		super(code, msg);
	}

	/**
	 * Indica que un usuario no puede tener certificados de firma.
	 * @param e Excepci&oacute;n que caus&oacute; el error.
	 */
    public HttpWeakRegistryException(final Throwable e) {
        super("El usuario realizo un registro debil y no puede tener certificados de firma", e); //$NON-NLS-1$
    }

    /**
	 * Indica que un usuario no puede tener certificados de firma.
	 * @param msg Mensaje de error.
	 * @param e Excepci&oacute;n que caus&oacute; el error.
	 */
    public HttpWeakRegistryException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
