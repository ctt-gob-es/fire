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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Manejador para la conexi&oacute;n a la base de datos.
 */
public class DbManager {

	private static final Logger LOGGER = Logger.getLogger(DbManager.class.getName());

	private static String dbConnDriver = null;

	private static String dbConnString = null;

	static {
		initialize();
	}

	/**
	 * Inicializa al completo el manejador de base de datos, leyendo el fichero
	 * de configuraci&oacute;n y recuperando la conexi&oacute;n.
	 * @return Conexi&oacute;n de base de datos o null si se produce un error.
	 */
	private static void initialize() {

		// Cargamos la cadena del driver JDBC
		dbConnDriver = ConfigManager.getJdbcDriverString();
		if (dbConnDriver == null) {
			LOGGER.log(
					Level.WARNING,
					"No se ha podido recuperar la clase del driver JDBC a la BD. No se realizara la conexion"); //$NON-NLS-1$
		}

		// Instanciamos el driver JDBC
		try {
			Class.forName(dbConnDriver).getConstructor().newInstance();
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al instanciar el driver de base de datos", e); //$NON-NLS-1$
		}

		// Cargamos la cadena de conexion a base de datos
		dbConnString = ConfigManager.getDataBaseConnectionString();
		if (dbConnString == null) {
			LOGGER.log(
					Level.WARNING,
					"No se ha podido recuperar la cadena de conexion a la BD. No se realizara la conexion"); //$NON-NLS-1$
		}
	}

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
	 * @throws SQLException Cuando no se puede crear la conexi&oacute;n.
	 */
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(dbConnString);
	}

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
	 * @throws SQLException Cuando no se puede crear la conexi&oacute;n.
	 */
	public static Connection getConnection(final boolean autocommit) throws SQLException {
		final Connection conn = DriverManager.getConnection(dbConnString);
		conn.setAutoCommit(autocommit);
		return conn;
	}

	/**
	 * Indica si la conexi&oacute;n a base de datos esta conigurada y puede usarse.
	 * @return {@code true} si la conexi&oacute;n a base de datos puede usarse.
	 * {@code false} en caso contrario.
	 */
	public static boolean isConfigured() {
		return dbConnDriver != null && dbConnString != null;
	}
}
