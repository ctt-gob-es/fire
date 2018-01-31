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

/** Excepci&oacute;n que refleja un error en la carga o inicializaci&oacute;n de la
 * clase conectora de un proveedor. */
public final class FIReConnectorFactoryException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Construye la excepci&oacute;n con un mensaje espec&iacute;fico y la causa
     * que origin&oacute; el error.
     * @param msg Mensaje de error.
     * @param cause Causa del error.
     */
    public FIReConnectorFactoryException(final String msg, final Exception cause) {
        super(msg, cause);
    }

    /**
     * Construye la excepci&oacute;n con un mensaje espec&iacute;fico del error.
     * @param msg Mensaje de error.
     */
    public FIReConnectorFactoryException(final String msg) {
        super(msg);
    }

    /**
     * Construye la excepci&oacute;n con la causa que origin&oacute; el error.

     * @param cause Causa del error.
     */
    public FIReConnectorFactoryException(final Exception cause) {
        super(cause);
    }
}
