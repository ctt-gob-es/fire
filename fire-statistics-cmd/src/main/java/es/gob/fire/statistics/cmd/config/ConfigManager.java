/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.statistics.cmd.config;


import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manejador que gestiona la configuraci&oacute;n de la aplicaci&oacute;n.
 */
public class ConfigManager {

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

	private static final String PROP_DB_DRIVER = "bbdd.driver"; //$NON-NLS-1$

	private static final String PROP_DB_CONNECTION = "bbdd.conn"; //$NON-NLS-1$

	/** Configuraci&oacute;n de la pol&iacute;tica de volcado de datos estad&iacute;sticos*/
	private static final String PROP_STATISTICS_POLICY ="statistics.policy"; //$NON-NLS-1$

	/** Configuraci&oacute;n del directorio de volcado de datosestad&iacute;sticos. */
	private static final String PROP_STATISTICS_DIR = "statistics.dir"; //$NON-NLS-1$

	private static final String PREFIX_CIPHERED_TEXT = "{@ciphered:"; //$NON-NLS-1$

	private static Properties config = null;

	private static boolean initialized = false;

	/**
	 * Lanza una excepci&oacute;n en caso de que no encuentre el fichero de configuraci&oacute;n.
	 * @param configFile Nombre del fichero de configuraci&oacute;n.
	 * @throws ConfigFilesException Si no encuentra el fichero indicado como parametro .properties.
	 */
	public static void checkConfiguration(final String configFile) throws ConfigFilesException {

		initialized = false;

		loadConfig(configFile);

		if (config == null) {
			LOGGER.severe("No se ha encontrado el fichero de configuracion de la conexion"); //$NON-NLS-1$
			throw new ConfigFilesException("No se ha encontrado el fichero de configuracion de la conexion",configFile ); //$NON-NLS-1$ CONFIG_FILE
		}

		initialized = true;
	}

	/**
	 * Carga el fichero de configuraci&oacute;n del m&oacute;dulo.
	 * @throws ConfigFilesException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	private static void loadConfig(final String configFile) throws  ConfigFilesException {

		if (config == null) {
			try {
				config = ConfigFileLoader.loadConfigFile(configFile);//CONFIG_FILE
			}
			catch (final Exception e) {
				LOGGER.severe("No se pudo cargar el fichero de configuracion " + configFile ); //$NON-NLS-1$ CONFIG_FILE
				throw new ConfigFilesException("No se pudo cargar el fichero de configuracion " + configFile , configFile , e); //$NON-NLS-1$ CONFIG_FILE
			}
		}
	}

	/**
	 * Indica que la configuracion ya se comprob&oacute; y est&aacute; operativa.
	 * @return {@code true} cuando ya se ha cargado la configuraci&oacute;n y comprobado
	 * que es correcta.
	 */
	public static boolean isInitialized() {
		return initialized;
	}


	/**
	 * Recupera la clase del driver JDBC para el acceso a la base de datos.
	 * @return Clase de conexi&oacute;n.
	 */
	public static String getJdbcDriverString() {
		return getProperty(PROP_DB_DRIVER);
	}

	/**
	 * Recupera la cadena de conexi&oacute;n con la base de datos.
	 * @return Cadena de conexi&oacute;n con la base de datos.
	 */
	public static String getDataBaseConnectionString() {
		return getProperty(PROP_DB_CONNECTION);
	}

	/**
	 * Devuelve el identificador num&eacute;rico de la pol&iacute;tica de firma configurada.
	 * En caso de error, devuelve -1.
	 * @return Dato num&eacute;rico.
	 */
	public static int getStatisticsPolicy() {
		int policy;
		try {
			policy = Integer.parseInt(getProperty(PROP_STATISTICS_POLICY));
		}
		catch (final NumberFormatException e) {
			policy = -1;
		}
		return policy;
	}

	/**
	 * Devuelve la ruta configurada del directorio en el que almacenar los datos para la generaci&oacute;n de estad&iacute;sticas.
	 * @return Ruta del directorio o {@code null} si no est&aacute; definida.
	 */
	public static String getStatisticsDir() {
		 return getProperty(PROP_STATISTICS_DIR);
	}

	/**
	 * Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 * @throws IllegalStateException Si se encuentra que el valor de la propiedad indicada esta cifrado.
	 */
	private static String getProperty(final String key) throws IllegalStateException {
		return getProperty(key, null);
	}

	/**
	 * Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @param defaultValue Valor por defecto que devolver en caso de que la propiedad no exista
	 * o que se produzca un error al extraerla.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 * @throws IllegalStateException Si se encuentra que el valor de la propiedad indicada esta cifrado.
	 */
	private static String getProperty(final String key, final String defaultValue) throws IllegalStateException {
		final String value = config.getProperty(key, defaultValue);
		if (isCiphered(value)) {
			throw new IllegalStateException("No se pueden cargar propiedades cifradas del fichero de configuracion: " + key); //$NON-NLS-1$
		}
		return value;
	}

	/**
	 * Comprueba si una cadena de texto tiene alg&uacute;n fragmento cifrado.
	 * @param text Cadena de texto.
	 * @return {@code true} si la cadena contiene fragmentos cifrados. {@code false},
	 * en caso contrario.
	 */
	private static boolean isCiphered(final String text) {
		return text != null && text.indexOf(PREFIX_CIPHERED_TEXT) != -1;
	}

}
