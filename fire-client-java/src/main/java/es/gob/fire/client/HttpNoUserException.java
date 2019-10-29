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
 * El usuario no est&aacute; dado de alta en el sistema.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class HttpNoUserException extends HttpOperationException {

    /** Serial Id. */
	private static final long serialVersionUID = -759772137388280743L;

    /**
     * El usuario no est&aacute; dado de alta en el sistema.
     * @param e Motivo del error.
     */
    public HttpNoUserException(final Throwable e) {
        super("Usuario no valido", e); //$NON-NLS-1$
    }

    /**
     * El usuario no est&aacute; dado de alta en el sistema.
     * @param msg Mensaje de error.
     * @param e Motivo del error.
     */
    public HttpNoUserException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
