/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal.sessions;

import java.util.logging.Logger;

import es.gob.fire.server.services.statistics.FireSignLogger;

/**
 * Factor&iacute;a para la obtenci&oacute;n de gestores de sesiones que
 * posibiliten disponer de varios servidores sin memoria compartida entre los
 * que se compartan las sesiones con las transacciones de firma.
 */
public class SessionsDAOFactory {

	private static Logger LOGGER =  FireSignLogger.getFireSignLogger().getFireLogger().getLogger();
//	private static final Logger LOGGER = Logger.getLogger(SessionsDAOFactory.class.getName());

	private static final String DAO_FILESYSTEM = "filesystem"; //$NON-NLS-1$

	private static final String[][] DAOS = new String[][] {
			{DAO_FILESYSTEM, "es.gob.fire.server.services.internal.sessions.FileSystemSessionsDAO"}, //$NON-NLS-1$
	};

	/**
	 * Recupera una instancia de un gestor de sesiones para que se compartan entre
	 * los distintos nodos en los que se despliegue el componente central de FIRe.
	 * @param type Nombre del gestor.
	 * @return Gestor de sesiones.
	 */
	public static SessionsDAO getInstance(final String type) {

		SessionsDAO daoInstance = null;
		for (final String[] dao : DAOS){
			if (dao[0].equalsIgnoreCase(type)) {
				try {
					daoInstance = (SessionsDAO) Class.forName(dao[1]).getConstructor().newInstance();
				} catch (final Exception e) {
					LOGGER.severe("Error al cargar del gestor para la comparticion de sesiones entre nodos: " + e); //$NON-NLS-1$
				}
			}
		}
		return daoInstance;
	}
}
