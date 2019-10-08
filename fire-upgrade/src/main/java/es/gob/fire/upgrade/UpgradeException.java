/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade;

/**
 * Error relacionado con la mejora de firmas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public class UpgradeException extends Exception {

    private static final long serialVersionUID = -3257579473377289361L;

    /**
     * Construye la excepci&oacute;n con su descripci&oacute;n y la causa del error.
     * @param msg Mensaje descriptivo del error.
     * @param cause Causa del error.
     */
    public UpgradeException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Construye la excepci&oacute;n con su descripci&oacute;n.
     * @param msg Mensaje descriptivo del error.
     */
    public UpgradeException(final String msg) {
        super(msg);
    }

}
