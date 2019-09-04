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
import java.sql.PreparedStatement;
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

	private static Connection conn = null;
	static {
		conn = initialize();
	}

	/**
	 * Inicializa al completo el manejador de base de datos, leyendo el fichero
	 * de configuraci&oacute;n y recuperando la conexi&oacute;n.
	 * @return Conexi&oacute;n de base de datos o null si se produce un error.
	 */
	private static Connection initialize() {
		// Cargamos el driver JDBC
		try {
			dbConnDriver = ConfigManager.getJdbcDriverString();
			if (dbConnDriver == null) {
				LOGGER.log(
						Level.WARNING,
						"No se ha podido recuperar la clase del driver JDBC a la BD. No se realizara la conexion"); //$NON-NLS-1$
				return null;
			}

			dbConnString = ConfigManager.getDataBaseConnectionString();
			if (dbConnString == null) {
				LOGGER.log(
						Level.WARNING,
						"No se ha podido recuperar la cadena de conexion a la BD. No se realizara la conexion"); //$NON-NLS-1$
				return null;
			}

		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al crear la conexion con la base de datos", e); //$NON-NLS-1$
			return null;
		}

		return initConnection();
	}

	/**
	 * Inicializa la conexi&oacute;n de base de datos para la configuraci&oacute;n de base de datos
	 * previamente cargada.
	 * @return Conexi&oacute;n de base de datos o null si se produce un error.
	 */
	private static Connection initConnection() {

		try {
			Class.forName(dbConnDriver).newInstance();

			return DriverManager.getConnection(dbConnString);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al crear la conexion con la base de datos", e); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
	 */
	private static Connection getConnection() {

		try {
			if (conn != null && !conn.isValid(2)) {
				LOGGER.warning("La conexion con base de datos ha dejado de ser valida"); //$NON-NLS-1$
				conn = null;
			}
		} catch (final SQLException e) {
			LOGGER.warning("La conexion con base de datos no es valida: " + e); //$NON-NLS-1$
			conn = null;
		}

		if (conn == null) {
			LOGGER.info("Se reinicia la conexion con base de datos, volviendo a cargar el fichero de configuracion"); //$NON-NLS-1$
			conn = initialize();
		}

		return conn;
	}

	/**
	 * Realiza a acci&oacute;n de commit desde la &uacute;ltima acci&oacute;n de commit o rollback anterior
	 * @throws SQLException
	 */
	public static void runCommit() throws SQLException {
		getConnection().commit();
	}
	/**
	 * Deshace todos los cambios de la actual transacci&oacute;n
	 * @throws SQLException
	 */
	public static void runRollBack() throws SQLException {
		getConnection().rollback();
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
	 * @throws DBConnectionException Cuando no se ha podido inicializar la conexion con base de datos.
	 */
	public static PreparedStatement prepareStatement(final String statement) throws SQLException, DBConnectionException {
		final Connection c = getConnection();
		if (c == null)  {
			throw new DBConnectionException("No se ha encontrado una conexion abierta contra la base de datos"); //$NON-NLS-1$
		}

		return c.prepareStatement(statement);
	}

	/**
	 * Prepara una sentencia SQL para ser ejecutada.
	 * @param statement Sentencia SQL.
	 * @param autoCommit true or false
	 * @return Sentencia SQL.
	 * @throws SQLException Cuando se produce un error al preparar la sentencia.
	 * @throws DBConnectionException Cuando no se ha podido inicializar la conexion con base de datos.
	 */
	public static PreparedStatement prepareStatement(final String statement, final boolean autoCommit) throws SQLException, DBConnectionException {
		final Connection c = getConnection();
		if (c == null)  {
			throw new DBConnectionException("No se ha encontrado una conexion abierta contra la base de datos"); //$NON-NLS-1$
		}
		c.setAutoCommit(autoCommit);
		return c.prepareStatement(statement);
	}


	/**
	 * Indica si la conexi&oacute;n a base de datos esta conigurada y puede usarse.
	 * @return {@code true} si la conexi&oacute;n a base de datos puede usarse.
	 * {@code false} en caso contrario.
	 */
	public static boolean isConfigured() {
		return conn != null;
	}
}
