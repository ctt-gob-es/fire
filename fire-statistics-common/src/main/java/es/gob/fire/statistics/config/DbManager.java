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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Manejador para la conexi&oacute;n a la base de datos.
 */
public class DbManager {

	private static final Logger LOGGER = Logger.getLogger(DbManager.class.getName());

	private HikariDataSource ds = null;

	private boolean initialized = false;

	private static DbManager instance = null;

	public static DbManager getInstance() {
		if (instance == null) {
			instance = new DbManager();
		}
		return instance;
	}

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
	 * @throws SQLException Cuando no se puede crear la conexi&oacute;n.
	 */
	public Connection getConnection() throws SQLException {
		return getConnection(false);
	}

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * @param autoCommit Indica si se debe hacer commit autom&aacute;tico tras cada
	 * operaci&oacute;n de inserci&oacute;n y borrado de entradas en base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
	 * @throws SQLException Cuando no se puede crear la conexi&oacute;n.
	 */
	public Connection getConnection(final boolean autoCommit) throws SQLException {
		if (this.ds == null || !this.ds.isRunning()) {
			throw new SQLException("El pool de conexiones no se encuentra en ejecucion"); //$NON-NLS-1$
		}
		final Connection conn = this.ds.getConnection();
		conn.setAutoCommit(autoCommit);
		return conn;
	}

	/**
	 * Inicializa al completo el manejador de base de datos con la configuraci&oacute;n
	 * proporcionada.
	 * @param jdbcDriver Nombre de la clase del controlador JDBC.
	 * @param dbConnectionString Cadena de conexi&oacute;n con la base de datos.
	 * @param username Nombre de usuario con el que conectarse a la base de datos.
	 * @param password Contrase&ntilde;a del usuario.
	 * @throws IOException Cuando ocurre un error durante la inicializaci&oacute;n.
	 */
	public void initialize(final String jdbcDriver, final String dbConnectionString, final String username, final String password) throws IOException {

		if (jdbcDriver == null) {
			LOGGER.log(
					Level.WARNING,
					"No se ha declarado la clase del driver JDBC de la BD en el fichero de configuracion"); //$NON-NLS-1$
			return;
		}

		if (dbConnectionString == null) {
			LOGGER.log(
					Level.WARNING,
					"No se ha declarado la cadena de conexion a la BD en el fichero de configuracion"); //$NON-NLS-1$
			return;
		}

		final HikariConfig config = loadConfig (jdbcDriver, dbConnectionString, username, password);

		this.ds = new HikariDataSource(config);

		if (!this.ds.isRunning()) {
			this.ds.close();
			this.ds = null;
			this.initialized = false;
			throw new IOException("El pool de conexiones con base de datos no se ejecuta correctamente"); //$NON-NLS-1$
		}

		this.initialized = true;
	}

	/**
	 * Inidica si el gestor est&aacute; inicializado.
	 * @return {@code true} si est&aacute; inicializado, {@code false} en caso contrario.
	 */
	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * Cierra el pool de conexiones.
	 */
	public void close() {
		if (this.ds != null) {
			this.ds.close();
			this.ds = null;
		}
		this.initialized = false;
	}

	/**
	 * Carga la configuraci&oacute;n para inicializar el pool de conexiones.
	 * @return Configuracion del pool de conexiones.
	 * @throws IOException Cuando no se encuentran valores necesarios para configurar la conexi&oacute;n.
	 */
	private static HikariConfig loadConfig(final String jdbcDriver, final String dbConnectionString, final String username, final String password) throws IOException {

		final HikariConfig config = new HikariConfig();

		config.setDriverClassName(jdbcDriver);
		config.setJdbcUrl(dbConnectionString);

		if (username != null) {
			config.addDataSourceProperty("user", username); //$NON-NLS-1$
		}

		if (password != null) {
			config.addDataSourceProperty("password", password); //$NON-NLS-1$
		}


		// Milisegundos de espera hasta que se de una conexion
		config.setConnectionTimeout(10000);
		// Numero maximo de conexiones que mantendra el pool simultaneamente
		config.setMaximumPoolSize(50);
		// Milisegundos maximos que permanecera abierta una conexion
		config.setMaxLifetime(1800000);
//		// Milisegundos entre los que se comprobara que la conexion sigue abierta
//		config.setKeepaliveTime(600000);
		config.setPoolName("FIRe"); //$NON-NLS-1$

		return config;
	}

//	/**
//	 * Inicializa la conexi&oacute;n de base de datos para la configuraci&oacute;n de base de datos
//	 * previamente cargada.
//	 * @return Conexi&oacute;n de base de datos o null si se produce un error.
//	 */
//	private static Connection initConnection() {
//
//		// Cargamos el driver JDBC
//		try {
//			final Class<?> driverClass = Class.forName(dbConnDriver, false, DbManager.class.getClassLoader());
//			if (!java.sql.Driver.class.isAssignableFrom(driverClass)) {
//				throw new IllegalArgumentException("La clase indicada como drive JDBC no es valida " + dbConnDriver); //$NON-NLS-1$
//			}
//			Class.forName(dbConnDriver).newInstance();
//
//			return DriverManager.getConnection(dbConnString);
//		}
//		catch (final Exception e) {
//			LOGGER.log(Level.SEVERE, "Error al crear la conexion con la base de datos", e); //$NON-NLS-1$
//			return null;
//		}
//	}
//
//
//	/**
//	 * Obtiene la conexi&oacute;n de base de datos.
//	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
//	 */
//	private static Connection getConnection() {
//
//		try {
//			if (conn != null && !conn.isValid(2)) {
//				LOGGER.warning("La conexion con base de datos ha dejado de ser valida y se reiniciara."); //$NON-NLS-1$
//				conn = null;
//			}
//		} catch (final SQLException e) {
//			LOGGER.warning("La conexion con base de datos no es valida y se reiniciara: " + e); //$NON-NLS-1$
//			conn = null;
//		}
//
//		if (conn == null) {
//			LOGGER.info("Se inicia la conexion con base de datos"); //$NON-NLS-1$
//			conn = initConnection();
//		}
//
//		return conn;
//	}

	/**
	 * Realiza a acci&oacute;n de commit desde la &uacute;ltima acci&oacute;n de commit o rollback anterior
	 * @throws SQLException Error al confirmar la transacci&oacute;n.
	 */
	public void runCommit() throws SQLException {
		getConnection().commit();
	}
	/**
	 * Deshace todos los cambios de la actual transacci&oacute;n.
	 * @throws SQLException Cuando ocurre alg&uacute;n error.
	 */
	public void runRollBack() throws SQLException {
		getConnection().rollback();
	}

//	/**
//	 * Cierra la conexi&oacute;n con la base de datos.
//	 * @throws SQLException Cuando ocurre un error al cerrar la conexi&oacute;n.
//	 */
//	public static void close() throws SQLException {
//		if (conn != null && !conn.isClosed()) {
//			conn.close();
//		}
//	}
//
//	/**
//	 * Prepara una sentencia SQL para ser ejecutada.
//	 * @param statement Sentencia SQL.
//	 * @return Sentencia SQL.
//	 * @throws SQLException Cuando se produce un error al preparar la sentencia.
//	 * @throws DBConnectionException Cuando no se ha podido inicializar la conexion con base de datos.
//	 */
//	public static PreparedStatement prepareStatement(final String statement) throws SQLException, DBConnectionException {
//		final Connection c = getConnection();
//		if (c == null)  {
//			throw new DBConnectionException("No se ha encontrado una conexion abierta contra la base de datos"); //$NON-NLS-1$
//		}
//
//		return c.prepareStatement(statement);
//	}
//
//	/**
//	 * Prepara una sentencia SQL para ser ejecutada.
//	 * @param statement Sentencia SQL.
//	 * @param autoCommit true or false
//	 * @return Sentencia SQL.
//	 * @throws SQLException Cuando se produce un error al preparar la sentencia.
//	 * @throws DBConnectionException Cuando no se ha podido inicializar la conexion con base de datos.
//	 */
//	public static PreparedStatement prepareStatement(final String statement, final boolean autoCommit) throws SQLException, DBConnectionException {
//		final Connection c = getConnection();
//		if (c == null)  {
//			throw new DBConnectionException("No se ha encontrado una conexion abierta contra la base de datos"); //$NON-NLS-1$
//		}
//		c.setAutoCommit(autoCommit);
//		return c.prepareStatement(statement);
//	}
//
//	/**
//	 * Indica si la conexi&oacute;n a base de datos esta conigurada y puede usarse.
//	 * @return {@code true} si la conexi&oacute;n a base de datos puede usarse.
//	 * {@code false} en caso contrario.
//	 */
//	public static boolean isConfigured() {
//		return conn != null;
//	}
}
