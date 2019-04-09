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
 * No se ha podido conectar con el componente central.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class HttpNetworkException extends HttpOperationException {

    /** Serial Id. */
	private static final long serialVersionUID = 8512065980675189697L;

	HttpNetworkException() {
        super();
    }

    /**
     * Excepci&oacute;n que identifica un error de conexi&oacute;n con el componente central.
     * @param e Causa del error.
     */
    public HttpNetworkException(final Throwable e) {
        super("Error de conexion", e); //$NON-NLS-1$
    }

    /** Excepci&oacute;n que identifica un error de conexi&oacute;n con el componente central.
     * @param msg Mensaje de error. */
    HttpNetworkException(final String msg) {
    	super(msg);
    }

    /** Excepci&oacute;n que identifica un error de conexi&oacute;n con el componente central.
     * @param msg Mensaje de error.
     * @param e Error de origen. */
    public HttpNetworkException(final String msg, final Throwable e) {
    	super(msg, e);
    }
}
