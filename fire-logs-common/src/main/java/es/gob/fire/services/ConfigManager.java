/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.services;


import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manejador que gestiona la configuraci&oacute;n de la aplicaci&oacute;n.
 */
public class ConfigManager {

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());





	private static final String PROP_LOGS_DIR = "logs.dir"; //$NON-NLS-1$
	private static final String PROP_LOGS_ROLLINGDATE = "logs.rollingDate"; //$NON-NLS-1$
	private static final String PROP_LOGS_PACKAGES = "logs.packages"; //$NON-NLS-1$


	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String CONFIG_FILE = "config_logger.properties"; //$NON-NLS-1$

	private static Properties config = null;

	/** N&uacute;mero que identifica cuando el valor de configuraci&oacute;n que indica que
	 * el n&uacute;mero de documentos no est&aacute; limitado. */
	public static final int UNLIMITED_NUM_DOCUMENTS = 0;

	/** Ruta del directorio por defecto para el guardado de temporales (directorio temporal del sistema). */
	private static String DEFAULT_TMP_DIR;

	static {
		try {
			DEFAULT_TMP_DIR = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			try {
				DEFAULT_TMP_DIR = File.createTempFile("tmp", null).getParentFile().getAbsolutePath(); //$NON-NLS-1$
			}
			catch (final Exception e1) {
				DEFAULT_TMP_DIR = null;
				LOGGER.warning(
					"No se ha podido cargar un directorio temporal por defecto, se debera configurar expresamente en el fichero de propiedades: "  + e1 //$NON-NLS-1$
				);
			}
		}
	}

	/**
	 * Carga el fichero de configuraci&oacute;n del m&oacute;dulo.
	 * @throws ConfigFilesException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	private static void loadConfig() throws  ConfigFilesException {

		if (config == null) {
			try {
				config = ConfigFileLoader.loadConfigFile(CONFIG_FILE);
			}
			catch (final Exception e) {
				LOGGER.severe("No se pudo cargar el fichero de configuracion " + CONFIG_FILE); //$NON-NLS-1$
				throw new ConfigFilesException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
			}
		}
	}



	/**
	 * Lanza una excepci&oacute;n en caso de que no encuentre el fichero de configuraci&oacute;n.
	 * @throws ConfigFilesException Si no encuentra el fichero login.properties.
	 */
	public static void checkInitialized() throws ConfigFilesException {

		loadConfig();

		if (config == null) {
			LOGGER.severe("No se ha encontrado el fichero de configuracion de la conexion"); //$NON-NLS-1$
			throw new ConfigFilesException("No se ha encontrado el fichero de configuracion de la conexion", CONFIG_FILE); //$NON-NLS-1$
		}


	}



	/**
	 * Devuelve el directorio configurado para el guardado de temporales.
	 * @return Ruta del directorio temporal o, si no se configuro, el directorio
	 * temporal del sistema. En caso de error, devolver&aacute; {@code null}.
	 */
	public static String getLogsDir() {
		return config.getProperty(PROP_LOGS_DIR, DEFAULT_TMP_DIR);
	}

	public static String getRollingDate() {
		return config.getProperty(PROP_LOGS_ROLLINGDATE);
	}
	/**
	 * Devuelve el fichero de configuraci&oacute;n.
	 * @return El fichero de configuraci&oacute;n.
	 */
	public static Properties getPropertyFile(){
		return config;
	}


	public static String getPackages() {
		return config.getProperty(PROP_LOGS_PACKAGES);
	}

}
