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
 * Error en el cliente al ejecutar la operaci&oacute;n del servidor central.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public class HttpOperationException extends Exception {

    private static final long serialVersionUID = -5667976435415689416L;

    /**
     * /** Crea la excepci&oacute;n en la operaci&oacute;n del servidor central.
     * @param msg Mensaje de error.
     */
    public HttpOperationException(final String msg) {
        super(msg);
    }

    /**
     * /** Crea la excepci&oacute;n en la operaci&oacute;n del servidor central.
     * @param e Causa del error.
     */
    protected HttpOperationException(final Throwable e) {
        super(e);
    }

    /**
     * /** Crea la excepci&oacute;n en la operaci&oacute;n del servidor central.
     * @param msg Mensaje del error.
     * @param e Causa del error.
     */
    public HttpOperationException(final String msg, final Throwable e) {
        super(msg, e);
    }
}
