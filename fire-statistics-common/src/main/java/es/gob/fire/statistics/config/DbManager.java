/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.statistics.config;

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

	/**
	 * Inicializa al completo el manejador de base de datos, leyendo el fichero
	 * de configuraci&oacute;n y recuperando la conexi&oacute;n.
	 * @param jdbcDriver Nombre de la clase del controlador JDBC.
	 * @param dbConnectionString Cadena de conexi&oacute;n con la base de datos.
	 * @return Conexi&oacute;n de base de datos o null si se produce un error.
	 */
	public static Connection initialize(final String jdbcDriver, final String dbConnectionString) {

		dbConnDriver = jdbcDriver;
		if (dbConnDriver == null) {
			LOGGER.log(
					Level.WARNING,
					"No se ha declarado la clase del driver JDBC de la BD en el fichero de configuracion"); //$NON-NLS-1$
			return null;
		}

		dbConnString = dbConnectionString;
		if (dbConnString == null) {
			LOGGER.log(
					Level.WARNING,
					"No se ha declarado la cadena de conexion a la BD en el fichero de configuracion"); //$NON-NLS-1$
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

		// Cargamos el driver JDBC
		try {
			final Class<?> driverClass = Class.forName(dbConnDriver, false, DbManager.class.getClassLoader());
			if (!java.sql.Driver.class.isAssignableFrom(driverClass)) {
				throw new IllegalArgumentException("La clase indicada como drive JDBC no es valida " + dbConnDriver); //$NON-NLS-1$
			}
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
				LOGGER.warning("La conexion con base de datos ha dejado de ser valida y se reiniciara."); //$NON-NLS-1$
				conn = null;
			}
		} catch (final SQLException e) {
			LOGGER.warning("La conexion con base de datos no es valida y se reiniciara: " + e); //$NON-NLS-1$
			conn = null;
		}

		if (conn == null) {
			LOGGER.info("Se inicia la conexion con base de datos"); //$NON-NLS-1$
			conn = initConnection();
		}

		return conn;
	}

	/**
	 * Realiza a acci&oacute;n de commit desde la &uacute;ltima acci&oacute;n de commit o rollback anterior
	 * @throws SQLException Error al confirmar la transacci&oacute;n.
	 */
	public static void runCommit() throws SQLException {
		getConnection().commit();
	}
	/**
	 * Deshace todos los cambios de la actual transacci&oacute;n.
	 * @throws SQLException Cuando ocurre alg&uacute;n error.
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
