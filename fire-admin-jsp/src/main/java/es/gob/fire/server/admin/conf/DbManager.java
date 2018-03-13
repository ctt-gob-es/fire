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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.admin.message.AdminFilesNotFoundException;


/**
 * Manejador para la conexi&oacute;n a la base de datos.
 */
public class DbManager {

	private static final Logger LOGGER = Logger.getLogger(DbManager.class.getName());

	private static final String PARAM_DB_DRIVER = "bbdd.driver"; //$NON-NLS-1$
	private static final String PARAM_DB_CONN = "bbdd.conn"; //$NON-NLS-1$
	private static final String CONFIG_FILE = "admin_config.properties";//$NON-NLS-1$

	/** Variable de entorno que determina el directorio en el que buscar el fichero de configuraci&oacute;n. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "fire.config.path"; //$NON-NLS-1$

	/** Variable de entorno antigua que determinaba el directorio en el que buscar el fichero
	 * de configuraci&oacute;n. Se utiliza si no se ha establecido la nueva variable. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR_OLD = "clavefirma.config.path"; //$NON-NLS-1$

	private static String dbConnDriver = null;

	private static String dbConnString = null;

	private static Connection conn;

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
			conn = initConnection();
		}

		return conn;
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
	 * Crea la conexi&oacute;n a trav&eacute;s del fichero de configuraci&oacute;n.
	 * @throws AdminFilesNotFoundException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	public static void initialize() throws AdminFilesNotFoundException {

		conn = null;

		final Properties dbConfig = loadConfig();

		dbConnDriver = dbConfig.getProperty(PARAM_DB_DRIVER);
		if (dbConnDriver == null) {
			LOGGER.log(
					Level.SEVERE,
					"No se ha declarado la clase del driver JDBC ('" + PARAM_DB_DRIVER + //$NON-NLS-1$
					"') a la BD en el fichero de configuracion"); //$NON-NLS-1$
			return;
		}

		dbConnString = dbConfig.getProperty(PARAM_DB_CONN);
		if (dbConnString == null) {
			LOGGER.log(
					Level.SEVERE,
					"No se ha declarado la cadena de conexion ('" + PARAM_DB_CONN + //$NON-NLS-1$
					"') a la BD en el fichero de configuracion"); //$NON-NLS-1$
			return;
		}

		// Iniciamos la conexion con la base de datos
		conn = initConnection();
	}

	/**
	 * Inicializa la conexi&oacute;n con la base de datos.
	 * @return Conexi&oacute;n con la base de datos o {@code null} si no se pudo conectar.
	 */
	private static Connection initConnection() {

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
	 * Prepara una sentencia SQL para ser ejecutada.
	 * @param statement Sentencia SQL.
	 * @return Sentencia SQL.
	 * @throws SQLException Cuando se produce un error al preparar la sentencia.
	 */
	public static PreparedStatement prepareStatement(final String statement) throws SQLException {
		return getConnection().prepareStatement(statement);
	}

	/**
	 * Carga el fichero de configuraci&oacute;n del m&oacute;dulo.
	 * @return Propiedades de fichero de configuraci&oacute:n.
	 * @throws AdminFilesNotFoundException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	private static Properties loadConfig() throws AdminFilesNotFoundException {

		InputStream is = null;
		final Properties dbConfig = new Properties();
		try {
			String configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR);
			if (configDir == null) {
				configDir = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR_OLD);
			}
			if (configDir != null) {
				final File configFile = new File(configDir, CONFIG_FILE).getCanonicalFile();
				if (!configFile.isFile() || !configFile.canRead()) {
					LOGGER.warning(
							"No se encontro el fichero " + CONFIG_FILE + " en el directorio configurado en la variable " + //$NON-NLS-1$ //$NON-NLS-2$
									ENVIRONMENT_VAR_CONFIG_DIR + ": " + configFile.getAbsolutePath() + //$NON-NLS-1$
									"\nSe buscara en el CLASSPATH."); //$NON-NLS-1$
				}
				else {
					is = new FileInputStream(configFile);
				}
			}

			if (is == null) {
				is = DbManager.class.getResourceAsStream('/' + CONFIG_FILE);
			}

			dbConfig.load(is);
			is.close();
		}
		catch(final NullPointerException e){
			LOGGER.severe("No se ha encontrado el fichero de configuracion de la base de datos: " + e); //$NON-NLS-1$
			if (is != null) {
				try { is.close(); } catch (final Exception ex) { ex.getStackTrace();/* No hacemos nada */ }
			}
			throw new AdminFilesNotFoundException("No se ha encontrado el fichero de propiedades" + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo cargar el fichero de configuracion del modulo de administracion", e); //$NON-NLS-1$
			if (is != null) {
				try { is.close(); } catch (final Exception ex) { ex.getStackTrace();/* No hacemos nada */ }
			}
			throw new AdminFilesNotFoundException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
		}
		finally {
			if (is != null) {
				try { is.close(); } catch (final Exception ex) {ex.getStackTrace(); /* No hacemos nada */ }
			}
		}
		return dbConfig;
	}
}
