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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Manejador para la conexi&oacute;n a la base de datos.
 */
public class DbManager {

	private static boolean configured = false;

	private static DataSource dataSource = null;

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * 
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo
	 *         conectar.
	 * @throws SQLException Cuando no se puede crear la conexi&oacute;n.
	 */
	public static Connection getConnection() throws SQLException {
		return getConnection(false);
	}

	/**
	 * Obtiene la conexi&oacute;n de base de datos.
	 * 
	 * @param autoCommit Indica si se debe hacer commit autom&aacute;tico tras cada
	 *                   operaci&oacute;n de inserci&oacute;n y borrado de entradas
	 *                   en base de datos.
	 * @return Conexi&oacute;n de base de datos o {@code null} si no se pudo
	 *         conectar.
	 * @throws SQLException Cuando no se puede crear la conexi&oacute;n.
	 */
	public static Connection getConnection(final boolean autoCommit) throws SQLException {
		if (dataSource == null) {
			try {
				initialize();
			} catch (final IOException e) {
				throw new SQLException("No se ha podido inicializar la conexion con la base de datos", e); //$NON-NLS-1$
			}
		}
		final Connection conn = dataSource.getConnection();
		conn.setAutoCommit(autoCommit);
		return conn;
	}

	/**
	 * Inicializa al completo el manejador de base de datos, leyendo el fichero
	 * de configuraci&oacute;n y recuperando la conexi&oacute;n.
	 * 
	 * @return Conexi&oacute;n de base de datos o {@code null} si se produce un
	 *         error.
	 * @throws IOException Cuando no se encuentra correctamente configurada la
	 *                     conexi&ioacute;n.
	 */
	private static void initialize() throws IOException {
		try {
			Context context = new InitialContext();
			dataSource = (DataSource) context.lookup(ConfigManager.getDatasourceJNDIName());

			checkConnection();
		} catch (final Exception e) {
			dataSource = null;
			configured = false;
			throw new IOException("No se ha podido verificar el correcto funcionamiento de la conexion", e);
		}

		configured = true;
	}

	private static void checkConnection() throws SQLException {

		// Intentamos obtener una conexion
		try (Connection conn = dataSource.getConnection();) {
			if (conn == null) {
				throw new SQLException("No se obtuvo conexion con la BD en un primer intento"); //$NON-NLS-1$
			}
		} catch (final Exception e) {
			// Si no pudimos obtener una conexion, realizamos un segundo intento
			try (Connection conn = dataSource.getConnection();) {
				if (conn == null) {
					throw new SQLException("No se pudo obtener la conexion con la BD en un segundo intento", e); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Indica si la conexi&oacute;n a base de datos esta conigurada y puede usarse.
	 * 
	 * @return {@code true} si la conexi&oacute;n a base de datos puede usarse.
	 *         {@code false} en caso contrario.
	 */
	public static boolean isConfigured() {
		return configured;
	}

	/**
	 * Libera los recursos de acceso a base de datos.
	 */
	public static void closeResources() {
		if (dataSource != null) {
			configured = false;
			dataSource = null;
		}
	}
}
