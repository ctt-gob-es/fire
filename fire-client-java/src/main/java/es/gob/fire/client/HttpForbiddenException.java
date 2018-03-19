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
 * Se rechaza el acceso del usuario.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class HttpForbiddenException extends HttpOperationException {

    /** Serial Id. */
	private static final long serialVersionUID = -3001968868698178809L;

	HttpForbiddenException() {
        super();
    }

    /**
     * Se rechaza el acceso del usuario al sistema.
     * @param e Motivo del error.
     */
    public HttpForbiddenException(final Throwable e) {
        super("Acceso no permitido", e); //$NON-NLS-1$
    }

    /**
     * Se rechaza el acceso del usuario al sistema.
     * @param msg Mensaje de error.
     * @param e Motivo del error.
     */
    public HttpForbiddenException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
