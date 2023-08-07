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

import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.services.internal.AlarmsManager;

/**
 * Factor&iacute;a para la obtenci&oacute;n de gestores de sesiones que
 * posibiliten disponer de varios servidores sin memoria compartida entre los
 * que se compartan las sesiones con las transacciones de firma.
 */
public class SessionsDAOFactory {

	private static final Logger LOGGER = Logger.getLogger(SessionsDAOFactory.class.getName());

	private static final String DAO_FILESYSTEM = "filesystem"; //$NON-NLS-1$

	private static final String DAO_FILESYSTEM_CLASSNAME = "es.gob.fire.server.services.internal.sessions.FileSystemSessionsDAO"; //$NON-NLS-1$


	/**
	 * Recupera una instancia de un gestor de sesiones para que se compartan entre
	 * los distintos nodos en los que se despliegue el componente central de FIRe.
	 * @param classname Nombre de la clase gestora.
	 * @return Gestor de sesiones.
	 */
	public static SessionsDAO getInstance(final String classname) {

		SessionsDAO daoInstance = null;

		// Se mantiene por retrocompatibilidad el que se pueda usar el nombre
		// "filesystem" para configurar el gestor para la comparticion a traves
		// de un directorio temporal comun
		final String daoClassname = DAO_FILESYSTEM.equalsIgnoreCase(classname) ?
				DAO_FILESYSTEM_CLASSNAME :
					classname;

		// Cargamos la clase
		try {
			daoInstance = (SessionsDAO) Class.forName(daoClassname).getConstructor().newInstance();
		} catch (final Throwable e) {
			LOGGER.log(Level.SEVERE, "Error al cargar el gestor para la comparticion de sesiones entre nodos", e); //$NON-NLS-1$
			AlarmsManager.notify(Alarm.LIBRARY_NOT_FOUND, daoClassname);
		}
		return daoInstance;
	}
}
