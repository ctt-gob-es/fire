/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.util.Date;
import java.util.Map;

/**
 * Hilo para la eliminaci&oacute;n de sesiones caducadas.
 */
public class ExpiredSessionCleanerThread extends Thread {

	private final String[] ids;
	private final Map<String, FireSession> sessions;

	/**
	 * Construye el objeto para la eliminaci&oacute;n de sesiones caducadas.
	 * @param ids Identificadores de las sesiones que se tienen que evaluar.
	 * @param sessions Mapa con todas las sesiones.
	 */
	public ExpiredSessionCleanerThread(final String[] ids, final Map<String, FireSession> sessions) {
		this.ids = ids;
		this.sessions = sessions;
	}

	@Override
	public void run() {

		FireSession session;
		final long currentTime = new Date().getTime();
    	for (final String id : this.ids) {
    		session = this.sessions.get(id);
    		if (session != null && currentTime > session.getExpirationTime()) {
    			SessionCollector.removeSession(session);
    		}
    	}
	}
}
