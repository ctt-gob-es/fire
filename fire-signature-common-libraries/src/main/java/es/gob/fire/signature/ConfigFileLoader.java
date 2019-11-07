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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Clase para la carga de ficheros de configuraci&oacute;n.
 */
public class ConfigFileLoader {

	/** Variable de entorno que determina el directorio en el que buscar el fichero de configuraci&oacute;n. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "fire.config.path"; //$NON-NLS-1$

	/** Variable de entorno antigua que determinaba el directorio en el que buscar el fichero
	 * de configuraci&oacute;n. Se utiliza si no se ha establecido la nueva variable. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR_OLD = "clavefirma.config.path"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(ConfigFileLoader.class.getName());

	/**
	 * Carga un fichero de configuraci&oacute;n del directorio configurado
	 * o del classpath si no se configur&oacute;.
	 * @param configFilename Nombre del fichero de configuraci&oacute;n.
	 * @return Propiedades de fichero de configuraci&oacute:n.
	 * @throws IOException Cuando no se puede cargar el fichero de configuraci&oacute;n.
	 * @throws FileNotFoundException Cuando no se encuentra el fichero de configuraci&oacute;n.
	 */
	public static Properties loadConfigFile(final String configFilename) throws  IOException, FileNotFoundException {

		boolean loaded = false;
		final Properties config = new Properties();
		try {
			String configDirPath = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR);
			if (configDirPath == null) {
				configDirPath = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR_OLD);
			}
			if (configDirPath != null) {
				final File configDir = new File(configDirPath).getCanonicalFile();
				final File configFile = new File(configDir, configFilename).getCanonicalFile();
				// Comprobamos que se trate de un fichero sobre el que tengamos permisos y que no
				// nos hayamos salido del directorio de configuracion indicado
				if (configFile.isFile() && configFile.canRead() && configDir.equals(configFile.getParentFile())) {
					try (InputStream is = new FileInputStream(configFile);) {
						config.load(is);
					}
					loaded = true;
					LOGGER.info("Se cargo el fichero de configuracion " + configFile.getAbsolutePath()); //$NON-NLS-1$
				}
				else {
					LOGGER.warning(
							"El fichero " + configFilename + " no existe o no pudo leerse del directorio configurado en la variable " + //$NON-NLS-1$ //$NON-NLS-2$
									ENVIRONMENT_VAR_CONFIG_DIR + ". El fichero debia encontrase en el directorio " + configDir); //$NON-NLS-1$
				}
			}

			// Cargamos el fichero desde el classpath si no se cargo de otro sitio
			if (!loaded) {
				try (InputStream is = ConfigFileLoader.class.getResourceAsStream('/' + configFilename);) {
					if (is == null) {
						LOGGER.severe("No se ha encontrado el fichero de configuracion " + configFilename); //$NON-NLS-1$
						throw new FileNotFoundException("No se ha encontrado el fichero de configuracion " + configFilename); //$NON-NLS-1$
					}
					LOGGER.info("Se ha cargado desde el classpath el fichero de configuracion " + configFilename); //$NON-NLS-1$
					config.load(is);
				}
			}
		}
		catch(final FileNotFoundException e){
			throw e;
		}
		catch(final Exception e){
			throw new IOException("No se ha podido cargar el fichero de configuracion " + configFilename, e); //$NON-NLS-1$
		}

		return config;
	}
}
