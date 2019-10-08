/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade.afirma;

import es.gob.fire.upgrade.ValidatorException;

/**
 * Error relacionado con el acceso a los servicios Web de la Plataforma Afirma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class PlatformWsException extends ValidatorException {

    private static final long serialVersionUID = -8027555586203516787L;

    /**
     * Construye la excepci&oacute;n.
     * @param msg Mensaje descriptivo del error.3
     *
     * @param cause Causa del error.
     */
    PlatformWsException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Construye la excepci&oacute;n.
     * @param cause Causa del error.
     */
    PlatformWsException(final Throwable cause) {
        super(cause);
    }

    /**
     * Construye la excepci&oacute;n.
     * @param msg Mensaje descriptivo del error.
     */
    PlatformWsException(final String msg) {
        super(msg);
    }

}
