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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Clase para la carga de ficheros de configuraci&oacute;n.
 */
public class FileLoader {


	private static final Logger LOGGER = Logger.getLogger(FileLoader.class.getName());

	/**
	 * Carga un fichero de configuraci&oacute;n del directorio configurado
	 * o del classpath si no se configur&oacute;.
	 * @param configFilename Nombre del fichero de configuraci&oacute;n.
	 * @return Propiedades de fichero de configuraci&oacute:n.
	 * @throws IOException Cuando no se puede cargar el fichero de configuraci&oacute;n.
	 * @throws FileNotFoundException Cuando no se encuentra el fichero de configuraci&oacute;n.
	 */
	public static Properties loadConfigFile(final String configFilename) throws  IOException, FileNotFoundException {

		final boolean loaded = false;
		final Properties config = new Properties();
		try {

			// Cargamos el fichero desde el classpath si no se cargo de otro sitio

			try (InputStream is = FileLoader.class.getResourceAsStream('/' + configFilename);) {
					if (is == null) {
						throw new FileNotFoundException();
					}
					config.load(is);
			}
		}
		catch(final FileNotFoundException e){
			throw e;
		}
		catch(final Exception e){
			throw new IOException("No se ha podido cargar el fichero de configuracion: " + configFilename, e); //$NON-NLS-1$
		}
		return config;
	}
}
