/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature;


import java.util.logging.Level;

/** Nivel de registro que se firma py almacena para auditor&iacute;a.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class LoggingAuditableLevel extends Level {

	private static final long serialVersionUID = 7621623065480201425L;

	/** Nivel de registro que se firma py almacena para auditor&iacute;a. */
	public static final Level AUDITABLE = new LoggingAuditableLevel();

	private LoggingAuditableLevel() {
		super("AUDITABLE", Level.SEVERE.intValue() + 1); //$NON-NLS-1$
	}

}
