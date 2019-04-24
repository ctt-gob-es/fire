/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.fire.server.services.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.fire.signature.ConfigManager;

/** Configuraci&oacute;n para la gesti&oacute;n del almacenamiento temporal de ficheros en servidor. */
final class StorageConfig {

	/** <i>Log</i> para registrar las acciones del servicio. */
	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma");  //$NON-NLS-1$

	private static final String DEBUG_KEY = "debug"; //$NON-NLS-1$

	/** Modo de depuraci&oacute;n activo o no, en el que no se borran los ficheros en servidor ni se dan por caducados. */
	static final boolean DEBUG;

	static final String FILE_PREFIX = "ser_intermedio_"; //$NON-NLS-1$

	/** Fichero de configuraci&oacute;n. */
	private static final String DEFAULT_CFG_FILE = "/storagesvr.properties"; //$NON-NLS-1$

	private static final String MAX_SIZE_KEY = "maxFileSize"; //$NON-NLS-1$

	private static final int MAX_SIZE;

	static {
		final Properties config = new Properties();
		try (
			final InputStream is = StorageConfig.class.getResourceAsStream(DEFAULT_CFG_FILE);
		) {
			config.load(is);
			is.close();
		}
		catch (final IOException e) {
			LOGGER.severe(
				"No se ha podido cargar el fichero con las propiedades (" + DEFAULT_CFG_FILE + "), se usaran los valores por defecto: " + e //$NON-NLS-1$ //$NON-NLS-2$
			);
		}

		DEBUG = Boolean.parseBoolean(config.getProperty(DEBUG_KEY));
		if (DEBUG) {
			LOGGER.warning("Modo de depuracion activado, no se borraran los ficheros en servidor"); //$NON-NLS-1$
		}

		int maxSize;
		try {
			maxSize = config.containsKey(MAX_SIZE_KEY) ?
				Integer.parseInt(config.getProperty(MAX_SIZE_KEY)) :
					Integer.MAX_VALUE;
		}
		catch (final Exception e) {
			LOGGER.warning(
				"Tamano maximo de fichero invalido en el fichero de configuracion (" + DEFAULT_CFG_FILE + "), se usara " + Integer.MAX_VALUE + ": " + e //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			);
			maxSize = Integer.MAX_VALUE;
		}
		MAX_SIZE = maxSize;
	}

	private StorageConfig() {
		// No instanciable
	}

	static int getMaxDataSize() {
		return MAX_SIZE;
	}

	/** Elimina del directorio temporal todos los ficheros que hayan sobrepasado el tiempo m&aacute;ximo
	 * de vida configurado. */
	static void removeExpiredFiles() {
		final File tempDir = ConfigManager.getAfirmaTempDir();
		if (tempDir != null && tempDir.isDirectory()) {
			if (StorageConfig.DEBUG) {
				// No se limpia el directorio temporal por estar en modo depuracion
				return;
			}
			for (final File file : tempDir.listFiles()) {
				try {
					if (
						file.isFile() &&
						file.getName().startsWith(StorageConfig.FILE_PREFIX) && // Solo borramos lo nuestro
						isExpired(file, ConfigManager.getAfirmaTempsTimeout())
					) {
						file.delete();
					}
				}
				catch(final Exception e) {
					// Suponemos que el fichero ha sido eliminado por otro hilo
					LOGGER.warning(
						"No se ha podido eliminar el fichero '" + file.getAbsolutePath() + "', es probable que se elimine en otro hilo de ejecucion: " + e //$NON-NLS-1$ //$NON-NLS-2$
					);
				}
			}
		}
	}

	static boolean isExpired(final File file, final long expirationTimeLimit) {
		if (StorageConfig.DEBUG) {
			return false;
		}
		return System.currentTimeMillis() - file.lastModified() > expirationTimeLimit;
	}

}
