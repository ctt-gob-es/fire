/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package log.utils;


import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manejador que gestiona la configuraci&oacute;n de la aplicaci&oacute;n.
 */
public class LogInfoManager {

	private static final Logger LOGGER = Logger.getLogger(LogInfoManager.class.getName());
	private static final String PROP_CODE = "code"; //$NON-NLS-1$
	private static final String PROP_LEVELS = "levels"; //$NON-NLS-1$
	private static final String PROP_DATE_FORMAT = "dateFormat"; //$NON-NLS-1$
	private static final String PROP_LOG_PATTERN = "logPattern"; //$NON-NLS-1$

	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String LOGINFO_FILE = "fire_service.loginfo"; //$NON-NLS-1$

	private static Properties config = null;



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
	 * @throws FilesException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	public static void loadConfig(final String sFile) throws  FilesException {
		final String fileName = sFile != null ? sFile : LOGINFO_FILE;
		if (config == null) {
			try {
				config = FileLoader.loadConfigFile(fileName);
			}
			catch (final Exception e) {
				LOGGER.severe("No se pudo cargar el fichero de configuracion " + fileName ); //$NON-NLS-1$
				throw new FilesException("No se pudo cargar el fichero de configuracion " + fileName, fileName, e); //$NON-NLS-1$
			}
		}
	}


	/**
	 * Obtiene el c&oacute;digo del fichero log indicados en la propiedad "code" del fichero ".loginfo" indicado.
	 * @return
	 */
	public static String getCode() {
		return config.getProperty(PROP_CODE);
	}

	/**
	 * Obtiene los niveles de log indicados en la propiedad "levels" del fichero ".loginfo" indicado.
	 * @return
	 */
	public static String[] getLevels() {
		final String[] levels=config.getProperty(PROP_LEVELS).split(","); //$NON-NLS-1$
		return levels;
	}

	/**
	 * Obtiene el formato de la fecha del log, indicada en la propiedad "dateFormat" del fichero ".loginfo" indicado
	 * @return
	 */
	public static String getDateFormat() {
		return config.getProperty(PROP_DATE_FORMAT);
	}


	/**
	 * Obtiene el patr&oacute;n de la l&iacute;neas del log, indicada en la propiedad "logPattern" del fichero ".loginfo" indicado
	 * @return
	 */
	public static String getLogPattern() {
		return config.getProperty(PROP_LOG_PATTERN);
	}






	/**
	 * Devuelve el fichero de configuraci&oacute;n.
	 * @return El fichero de configuraci&oacute;n.
	 */
	public static Properties getPropertyFile(){
		return config;
	}


}
