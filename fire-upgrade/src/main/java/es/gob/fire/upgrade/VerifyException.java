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
 * Error relacionado con validaci&oacute;n de firmas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public class VerifyException extends Exception {

    /** Serial Id. */
	private static final long serialVersionUID = 5344165604415563447L;

	public VerifyException(final String msg, final Throwable e) {
        super(msg, e);
    }

    public VerifyException(final String desc) {
        super(desc);
    }
}
