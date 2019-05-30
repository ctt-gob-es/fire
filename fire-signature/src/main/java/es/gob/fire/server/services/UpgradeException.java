/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

import java.io.IOException;

/** Excepci&oacute;n para indicar que ocurri&oacute; un error durante la
 * actualizaci&oacute;n de una firma electr&oacute;nica. */
public final class UpgradeException extends IOException {

	/** Serial Id. */
	private static final long serialVersionUID = -3470452142461150016L;

	/** Construye la excepci&oacute;n describiendo el motivo del error.
	 * @param msg Mensaje de error. */
	UpgradeException(final String msg) {
		super(msg);
    }

	/** Construye la excepci&oacute;n describiendo el motivo del error.
	 * @param msg Mensaje de error.
	 * @param cause Causa del error. */
	UpgradeException(final String msg, final Throwable cause) {
		super(msg, cause);
    }

	/** Construye la excepci&oacute;n describiendo el motivo del error.
	 * @param cause Causa del error. */
	public UpgradeException(final Exception cause) {
		super(cause);
	}


}
