/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.batch;

/** Error en el proceso de firma por lotes.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class BatchException extends Exception {

	private static final long serialVersionUID = 1L;

	BatchException(final String msg, final Throwable e) {
		super(msg, e);
	}

}
