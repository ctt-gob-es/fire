/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.connector;

/**
 * Error en la firma electr&oacute;nica en la nube.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class FIReSignatureException extends FIReConnectorException {

    private static final long serialVersionUID = -9045338848033350726L;

    /**
     * Construye una excepci&oacute;n de error en la firma electr&oacute;nica en
     * la nube.
     *
     * @param msg
     *            Mensaje de la excepci&oacute;n.
     * @param e
     *            Causa inicial de la excepci&oacute;n.
     */
    public FIReSignatureException(final String msg, final Throwable e) {
        super(msg, e);
    }

    /**
     * Construye una excepci&oacute;n de error en la firma electr&oacute;nica en
     * la nube.
     *
     * @param msg
     *            Mensaje de la excepci&oacute;n.
     */
    public FIReSignatureException(final String msg) {
        super(msg);
    }

}
