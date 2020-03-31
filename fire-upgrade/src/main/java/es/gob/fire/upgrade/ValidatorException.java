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
 * Error relacionado con la configuraci&oacute;n y uso de la plataforma de validaci&oacute;n
 * y mejora de firma.
 */
public class ValidatorException extends Exception {

    /** N&uacute;mero de serie. */
	private static final long serialVersionUID = -3615642866749920497L;

	/** Construye la excepci&oacute;n.
	 * @param msg Descripci&oacute;n del error.
	 * @param cause Motivo por el que se crea la excepci&oacute;n. */
	public ValidatorException(final String msg, final Throwable cause) {
	    super(msg, cause);
    }

	/** Construye la excepci&oacute;n.
	 * @param cause Motivo por el que se crea la excepci&oacute;n. */
	public ValidatorException(final Throwable cause) {
	    super(cause);
    }

	/** Construye la excepci&oacute;n.
	 * @param msg Descripci&oacute;n del error. */
	public ValidatorException(final String msg) {
	    super(msg);
    }
}
