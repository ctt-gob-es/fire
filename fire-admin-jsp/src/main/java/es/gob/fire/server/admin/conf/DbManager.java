/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.conf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.admin.message.AdminFilesNotFoundException;


/**
 * Manejador para la conexi&oacute;n a la base de datos.
 */
public class DbManager {

	private static final Logger LOGGER = Logger.getLogger(DbManager.class.getName());

	private static Connection conn;

	/**
	 * Crea la conexi&oacute;n a trav&eacute;s del fichero de configuraci&oacute;n.
	 * @throws AdminFilesNotFoundException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	public static void initialize() throws AdminFilesNotFoundException {

		conn = null;

		ConfigManager.initialize();

		final String dbConnDriver = ConfigManager.getDbDriverString();
		if (dbConnDriver == null) {
			LOGGER.log(Level.SEVERE, "No se ha podido obtener el driver JDBC. Se aborta la creacion de la conexion."); //$NON-NLS-1$
			return;
		}

		final String dbConnString = ConfigManager.getDbConnectionString();
		if (dbConnString == null) {
			LOGGER.log(Level.SEVERE, "No se ha podido obtener la cadena de conexion a base de datos. Se aborta la creacion de la conexion."); //$NON-NLS-1$
			return;
		}

		// Iniciamos la conexion con la base de datos
		conn = initConnection(dbConnDriver, dbConnString);
	}

	/**
	 * Inicializa la conexi&oacute;n con la base de datos.
	 * @return Conexi&oacute;n con la base de datos o {@code null} si no se pudo conectar.
	 */
	private static Connection initConnection(final String dbConnDriver, final String dbConnString) {

		try {
			Class.forName(dbConnDriver).newInstance();

			// Conectamos con la base de datos
			return DriverManager.getConnection(dbConnString);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al crear la conexion con la base de datos", e); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Cierra la conexi&oacute;n con la base de datos.
	 * @throws SQLException Cuando ocurre un error al cerrar la conexi&oacute;n.
	 */
	public static void close() throws SQLException {
		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
	}

	/**
	 * Prepara una sentencia SQL para ser ejecutada.
	 * @param statement Sentencia SQL.
	 * @return Sentencia SQL.
	 * @throws SQLException Cuando se produce un error al preparar la sentencia.
	 */
	public static PreparedStatement prepareStatement(final String statement) throws SQLException {
		return getConnection().prepareStatement(statement);
	}

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
	 */
	private static Connection getConnection() {

		try {
			if (!conn.isValid(2)) {
				LOGGER.warning("La conexion con base de datos ha dejado de ser valida"); //$NON-NLS-1$
				conn = null;
			}
		} catch (final Exception e) {
			LOGGER.warning("La conexion con base de datos no es valida: " + e); //$NON-NLS-1$
			conn = null;
		}

		if (conn == null) {
			LOGGER.info("Se reinicia la conexion con base de datos"); //$NON-NLS-1$
			try {
				initialize();
			} catch (final AdminFilesNotFoundException e) {
				LOGGER.log(Level.SEVERE, "No se ha encontrado el fichero de configuracion del modulo", e); //$NON-NLS-1$
			}
		}

		return conn;
	}
}
