/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.services.statistics.config;


import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.decipher.PropertyDecipher;

/**
 * Manejador que gestiona la configuraci&oacute;n de la aplicaci&oacute;n.
 */
public class ConfigManager {

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

	private static final String PROP_DB_DRIVER = "bbdd.driver"; //$NON-NLS-1$

	private static final String PROP_DB_CONNECTION = "bbdd.conn"; //$NON-NLS-1$

	private static final String PARAM_CIPHER_CLASS = "cipher.class"; //$NON-NLS-1$

	private static final String PROP_CHECK_CERTIFICATE = "security.checkCertificate"; //$NON-NLS-1$
	private static final String PROP_CHECK_APPLICATION = "security.checkApplication"; //$NON-NLS-1$


	/** Cadena utilizada para separar valores dentro de una propiedad. */
	private static final String VALUES_SEPARATOR = ","; //$NON-NLS-1$

	private static final String PREFIX_CIPHERED_TEXT = "{@ciphered:"; //$NON-NLS-1$
	private static final String SUFIX_CIPHERED_TEXT = "}"; //$NON-NLS-1$


	/** Ruta del directorio por defecto para el guardado de temporales (directorio temporal del sistema). */
	private static String DEFAULT_TMP_DIR;

	/** Nombre del fichero de configuraci&oacute;n. */
//	private static final String CONFIG_FILE = "config.properties"; //$NON-NLS-1$

	/**Configuraci&oacute;n de las estad&iacute;sticas*/
	private static final String CONFIG_STATISTICS ="statistics"; //$NON-NLS-1$

	/**Configuraci&oacute;n de la hora de comienzo de carga de estad&iacute;sticas*/
	private static final String CONFIG_STATISTICS_STARTTIME ="statistics.start_time"; //$NON-NLS-1$

	/**Configuraci&oacute;n del directorio donde se almacenan los ficheros de  estad&iacute;sticas*/
	private static final String CONFIG_STATISTICS_DIR ="statistics.dir"; //$NON-NLS-1$

	private static final String PATTERN_TIME = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$"; //$NON-NLS-1$

	private static Properties config = null;

	private static boolean initialized = false;

	private static PropertyDecipher decipherImpl = null;




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

			if (config.containsKey(PARAM_CIPHER_CLASS)) {
				final String decipherClassname = config.getProperty(PARAM_CIPHER_CLASS);
				if (decipherClassname != null && !decipherClassname.trim().isEmpty()) {
					try {
						final Class<?> decipherClass = Class.forName(decipherClassname);
						final Object decipher = decipherClass.newInstance();
						if (PropertyDecipher.class.isInstance(decipher)) {
							decipherImpl = (PropertyDecipher) decipher;
						}
					}
					catch (final Exception e) {
						LOGGER.log(Level.WARNING, "Se ha definido una clase de descifrado no valida", e); //$NON-NLS-1$
					}
				}
			}
		}
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
	 * Lanza una excepci&oacute;n en caso de que no encuentre el fichero de configuraci&oacute;n.
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
	 * Indica que la configuracion ya se comprob&oacute; y est&aacute; operativa.
	 * @return {@code true} cuando ya se ha cargado la configuraci&oacute;n y comprobado
	 * que es correcta.
	 */
	public static boolean isInitialized() {
		return initialized;
	}





	/**
	 * Devuelve la configuraci&oacute;n de las estad&iacute;sticas
	 * @return Dato num&eacute;rico de 0, 1 y 2
	 *  En caso de error, devolver&aacute; {@code null}.
	 */
	public static String getConfigStatistics() {
		int conf;
		try {
			conf = Integer.parseInt(getProperty(CONFIG_STATISTICS));
			if(conf > 2 || conf < 0) {
				conf = 0;
			}
		}
		catch (final NumberFormatException e) {
			conf = 0;
		}
		return String.valueOf(conf);
	}


	/**
	 * Devuelve la configuraci&oacute;n de la hora de la carga a la base de datos
	 * @return Dato hora con formato 00:00:00
	 *  En caso de no obtener un dato con formato correcto o nulo devolver&aacute; la cadena 00:00:00
	 */
	public static String getStatisticsStartTime() {
		 String time =  getProperty(CONFIG_STATISTICS_STARTTIME);
		 if (time == null || "".equals(time)) { //$NON-NLS-1$
			 time = "00:00:00";	 //$NON-NLS-1$
		 }
		 else if(!time.matches(PATTERN_TIME)) { //
			 time = "00:00:00";	 //$NON-NLS-1$
		}
		return time;
	}

	/**
	 * Devuelve el directorio configurado para el guardado de estad&iacute;sticas.
	 * @return Ruta del directorio de estad&iacute;sticas o, si no se configuro, el directorio
	 * temporal del sistema. En caso de error, devolver&aacute; {@code null}.
	 */
	public static String getStatisticsDir() {
		return getProperty(CONFIG_STATISTICS_DIR, DEFAULT_TMP_DIR);
	}



	/**
	 * Recupera si se debe realizar una autenticaci&oacute;n mediante certificado de las aplicaciones cliente.
	 * @return El valor del par&aacute;metro security.checkCertificate.
	 */
	public static boolean isCheckCertificateNeeded() {
		return 	Boolean.parseBoolean(getProperty(PROP_CHECK_CERTIFICATE, Boolean.TRUE.toString()));
	}

	/**
	 * Recupera si se debe realizar una validaci&oacute;n de la aplicaci&oacute;n en base de datos.
	 * @return El valor del par&aacute;metro security.checkApplication.
	 */
	public static boolean isCheckApplicationNeeded() {
		return 	Boolean.parseBoolean(getProperty(PROP_CHECK_APPLICATION, Boolean.TRUE.toString()));
	}



	/**
	 * Devuelve el fichero de configuraci&oacute;n.
	 * @return El fichero de configuraci&oacute;n.
	 */
	public static Properties getPropertyFile(){
		return config;
	}



	/**
	 * Indica si se ha configurado un objeto para el descifrado de propiedades
	 * en los ficheros de configuraci&oacute;n.
	 * @return {@code true} si se ha definido el objeto de descifrado. {@code false}
	 * en caso contrario.
	 */
	public static boolean hasDecipher() {
		return decipherImpl != null;
	}

	/**
	 * Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 */
	private static String getProperty(final String key) {
		return getProperty(key, null);
	}

	/**
	 * Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @param defaultValue Valor por defecto que devolver en caso de que la propiedad no exista
	 * o que se produzca un error al extraerla.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 */
	private static String getProperty(final String key, final String defaultValue) {
		return getDecipheredProperty(config, key, defaultValue);
	}

	/**
	 * Recupera una propiedad de un objeto de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param properties Objeto de configuraci&oacute;n del que obtener el valor.
	 * @param key Clave de la propiedad.
	 * @param defaultValue Valor por defecto que devolver en caso de que la propiedad no exista
	 * o que se produzca un error al extraerla.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 */
	public static String getDecipheredProperty(final Properties properties, final String key, final String defaultValue) {
		String value = properties.getProperty(key);
		if (decipherImpl != null && value != null) {
			while (isCiphered(value)) {
				try {
					value = decipherFragment(value);
				} catch (final IOException e) {
					if (defaultValue != null) {
						LOGGER.log(
								Level.WARNING,
								String.format(
										"Ocurrio un error al descifrar un fragmento de la propiedad %1s. Se usara el valor %2s", //$NON-NLS-1$
										key,
										defaultValue),
								e);
						value = defaultValue;
					}
					else {
						LOGGER.log(
								Level.WARNING,
								String.format(
										"Ocurrio un error al descifrar un fragmento de la propiedad %1s. Se usara el valor predefinido.", //$NON-NLS-1$
										key)
								);
					}
					break;
				}
			}
		}

		if (value == null) {
			value = defaultValue;
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

		if (text == null) {
			return false;
		}

		final int idx = text.indexOf(PREFIX_CIPHERED_TEXT);
		return idx != -1 && text.indexOf(SUFIX_CIPHERED_TEXT, idx + PREFIX_CIPHERED_TEXT.length()) != -1;
	}

	/**
	 * Texto cifrado del que descifrar un fragmento.
	 * @param text Texto con los marcadores que se&ntilde;alan que hay un fragmento cifrado.
	 * @return  Texto con un framento descifrado.
	 * @throws IOException Cuando ocurre un error al descifrar los datos.
	 */
	private static String decipherFragment(final String text) throws IOException {
		final int idx1 = text.indexOf(PREFIX_CIPHERED_TEXT);
		final int idx2 = text.indexOf(SUFIX_CIPHERED_TEXT, idx1);
		final String base64Text = text.substring(idx1 + PREFIX_CIPHERED_TEXT.length(), idx2).trim();

		return text.substring(0, idx1) +
				decipherImpl.decipher(Base64.decode(base64Text)) +
				text.substring(idx2 + SUFIX_CIPHERED_TEXT.length());
	}
}
