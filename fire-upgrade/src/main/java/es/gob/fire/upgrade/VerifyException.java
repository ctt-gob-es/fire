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
 * Error relacionado con validaci&oacute;n de certificados.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public class VerifyException extends Exception {

    private static final long serialVersionUID = -3257579473377289361L;

    VerifyException(final String msg, final Throwable e) {
        super(msg, e);
    }

    protected VerifyException(final String desc) {
        super(desc);
    }

}
