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

		// Establecemos una configuracion especifica para Oracle con la que establecemos como
		// comprobar que la conexion sigue siendo valida
		if (jdbcDriver.contains("Oracle")) { //$NON-NLS-1$
			config.setConnectionTestQuery("SELECT 1 FROM DUAL"); //$NON-NLS-1$
		}

		// Milisegundos de espera hasta que se de una conexion
		config.setConnectionTimeout(10000);
		// Numero maximo de conexiones que mantendra el pool simultaneamente
		config.setMaximumPoolSize(50);
		// Milisegundos maximos que permanecera abierta una conexion
		config.setMaxLifetime(1800000);
//		// Milisegundos entre los que se comprobara que la conexion sigue abierta
//		config.setKeepaliveTime(600000);
		config.setPoolName("FIRe-Statistics"); //$NON-NLS-1$

		return config;
	}
}
