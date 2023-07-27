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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


/**
 * Manejador para la conexi&oacute;n a la base de datos.
 */
public class DbManager {

	private static boolean configured = false;

	private static HikariDataSource ds = null;

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
	 * @throws SQLException Cuando no se puede crear la conexi&oacute;n.
	 */
	public static Connection getConnection() throws SQLException {
		return getConnection(false);
	}

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * @param autoCommit Indica si se debe hacer commit autom&aacute;tico tras cada
	 * operaci&oacute;n de inserci&oacute;n y borrado de entradas en base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo conectar.
	 * @throws SQLException Cuando no se puede crear la conexi&oacute;n.
	 */
	public static Connection getConnection(final boolean autoCommit) throws SQLException {
		if (ds == null) {
			try {
				initialize();
			} catch (final IOException e) {
				throw new SQLException("No se ha podido inicializar la conexion con la base de datos", e); //$NON-NLS-1$
			}
		}
		final Connection conn = ds.getConnection();
		conn.setAutoCommit(autoCommit);
		return conn;
	}


	/**
	 * Inicializa al completo el manejador de base de datos, leyendo el fichero
	 * de configuraci&oacute;n y recuperando la conexi&oacute;n.
	 * @return Conexi&oacute;n de base de datos o {@code null} si se produce un error.
	 * @throws IOException Cuando no se encuentra correctamente configurada la conexi&ioacute;n.
	 */
	private static void initialize() throws IOException {

		final HikariConfig config = loadConfig();

		ds = new HikariDataSource(config);

		try {
			checkConnection();
		}
		catch (final Exception e) {
			ds.close();
			ds = null;
			configured = false;
			throw new IOException("No se ha podido verificar el correcto funcionamiento de la conexion", e); //$NON-NLS-1$
		}

		configured = true;
	}

	/**
	 * Carga la configuraci&oacute;n para inicializar el pool de conexiones.
	 * @return Configuracion del pool de conexiones.
	 * @throws IOException Cuando no se encuentran valores necesarios para configurar la conexi&oacute;n.
	 */
	private static HikariConfig loadConfig() throws IOException {

		final HikariConfig config = new HikariConfig();

		final String driverClassname = ConfigManager.getJdbcDriverString();
		if (driverClassname == null) {
			throw new IOException("No se ha declarado el driver JDBC en el fichero de configuracion"); //$NON-NLS-1$
		}
		config.setDriverClassName(driverClassname);

		// Se configura la cadena de conexion si se define. Si no, se configuran
		// los valores por separado
		final String dbConnString = ConfigManager.getDataBaseConnectionString();
		if (dbConnString != null) {
			config.setJdbcUrl(dbConnString);
		}
		else {
			final String host = ConfigManager.getDataBaseHost();
			final String port = ConfigManager.getDataBasePort();
			final String databaseName = ConfigManager.getDataBaseName();

			if (host != null && port != null && databaseName != null) {
				config.addDataSourceProperty("serverName", host); //$NON-NLS-1$
				config.addDataSourceProperty("portNumber", port); //$NON-NLS-1$
				config.addDataSourceProperty("databaseName", databaseName); //$NON-NLS-1$
			}
			else {
				throw new IOException("No se ha declarado la cadena de conexion ni los parametros independientes para la conexion con la base de datos"); //$NON-NLS-1$
			}
		}

		final String username = ConfigManager.getDataBaseUsername();
		if (username != null) {
			config.addDataSourceProperty("user", username); //$NON-NLS-1$
		}

		final String password = ConfigManager.getDataBasePassword();
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


	private static void checkConnection() throws SQLException {

		// Intentamos obtener una conexion
		try (Connection conn = ds.getConnection();) {
			if (conn == null) {
				throw new SQLException("No se obtuvo conexion con la BD en un primer intento"); //$NON-NLS-1$
			}
		}
		catch (final Exception e) {
			// Si no pudimos obtener una conexion, pero se definio un driver en lugar de un datasource,
			// probamos a establecer este (no se hizo antes por recomendacion de HikariPC).
			final String driverClassname = ConfigManager.getJdbcDriverString();
			if (driverClassname != null) {
				// Intentamos obtener una conexion
				try (Connection conn = ds.getConnection();) {
					if (conn == null) {
						throw new SQLException("No se pudo obtener la conexion con la BD en un segundo intento", e); //$NON-NLS-1$
					}
				}
				catch (final Exception e2) {
					throw new SQLException("No se obtuvo conexion con la BD", e2); //$NON-NLS-1$
				}
			}
			throw new SQLException("No se pudo obtener conexion con la BD", e); //$NON-NLS-1$
		}


	}

	/**
	 * Indica si la conexi&oacute;n a base de datos esta conigurada y puede usarse.
	 * @return {@code true} si la conexi&oacute;n a base de datos puede usarse.
	 * {@code false} en caso contrario.
	 */
	public static boolean isConfigured() {
		return configured;
	}

	/**
	 * Libera los recursos de acceso a base de datos.
	 */
	public static void closeResources() {
		if (ds != null) {
			configured = false;
			ds.close();
		}
	}
}
