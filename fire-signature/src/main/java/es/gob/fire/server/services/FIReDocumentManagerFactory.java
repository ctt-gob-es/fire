/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.document.DefaultFIReDocumentManager;
import es.gob.fire.signature.ConfigFileLoader;
import es.gob.fire.signature.ConfigManager;

/**
 * Factoria para la obtenci&oacute;n de los distintos gestores de documentos. Al
 * utilizar esta factor&iaucte;a, que almacena y reutiliza las instancias de los
 * DocumentManager, conseguimos que no sea necesario inicializar un DocumentManager
 * por cada operaci&oacute;n de firma.
 */
public class FIReDocumentManagerFactory {

	private static final Logger LOGGER = Logger.getLogger(FIReDocumentManagerFactory.class.getName());

	/** Nombre del gestor de documentos precargado. */
	static final String DEFAULT_DOCUMENT_MANAGER = "default"; //$NON-NLS-1$

	private static final Map<String, FIReDocumentManager> docManagers;

	static {
		docManagers = new HashMap<>();
	}

	/**
	 * Obtiene una instancia de la clase de gesti&oacute;n de documentos. Si no se
	 * indica ninguno, se devuelve el por defecto.
	 * @param docManagerName Nombre del gestor que se desea recuperar o {@code null}
	 * para obtener el por defecto.
	 * @return Gestor de documentos.
	 * @throws IllegalArgumentException Cuando se indica un gestor de documentos que no existe.
	 * @throws IOException Cuando no es posible cargar el gestor de documentos.
	 */
	public static FIReDocumentManager newDocumentManager(final String docManagerName)
			throws IllegalArgumentException, IOException {

		String managerName = docManagerName;
		if (managerName == null) {
			managerName = DEFAULT_DOCUMENT_MANAGER;
		}

		if (!docManagers.containsKey(managerName)) {

			final String docManagerClassName = ConfigManager.getDocumentManagerClassName(managerName);

			// Comprobamos que la clase este definida en el fichero de configuracion
			if (docManagerClassName == null) {

				// Si no hay definida en el fichero de configuracion una clase especifica en el fichero
				// de configuracion, cargamos la clase gestora original
				if (DEFAULT_DOCUMENT_MANAGER.equals(managerName)) {
					LOGGER.warning("No se ha encontrado la clase gestora por defecto en el fichero de configuracion. Se establecera la clase original."); //$NON-NLS-1$
					return registryDefaultDocumentManager();
				}

				LOGGER.severe("No hay definida una clase gestora de documentos con el nombre " + managerName); //$NON-NLS-1$
				throw new IllegalArgumentException("No hay definida una clase gestora de documentos con el nombre " + managerName); //$NON-NLS-1$
			}

			FIReDocumentManager docManager;
			try {
				docManager = (FIReDocumentManager) Class.forName(docManagerClassName).
						getConstructor().newInstance();
			}
			catch (final ClassNotFoundException e) {
				LOGGER.severe("No se encontro la clase gestora de documentos " + docManagerClassName); //$NON-NLS-1$
				throw new IOException("No se encontro la clase gestora de documentos " + docManagerClassName, e); //$NON-NLS-1$
			}
			catch (final NoSuchMethodException | IllegalAccessException e) {
				LOGGER.severe("La clase gestora de documento no tiene definido el constructor por defecto o este no es publico: " + e); //$NON-NLS-1$
				throw new IOException("La clase gestora de documento no tiene definido el constructor por defecto o este no es publico", e); //$NON-NLS-1$
			}
			catch (final Exception e) {
				LOGGER.severe("La clase gestora de documentos genero un error durante la construccion: " + e); //$NON-NLS-1$
				throw new IOException("La clase gestora de documentos genero un error durante la construccion", e); //$NON-NLS-1$
			}

			// Salvo que sea el DocumentManager por defecto, se busca un fichero de configuracion
			// para su inicializacion. El DocumentManager por defecto no lo necesita.
			if (!DEFAULT_DOCUMENT_MANAGER.equals(managerName)) {
				Properties config = null;
				try {
					config = ConfigFileLoader.loadConfigFile(getDocManagerConfigFilename(managerName));
				}
				catch (final FileNotFoundException e) {
					LOGGER.warning("No se encontro el fichero de configuracion '" + getDocManagerConfigFilename(managerName) + //$NON-NLS-1$
							"'. Se cargara el gestor de documentos sin esta configuracion: " + e); //$NON-NLS-1$
				}
				catch (final Exception e) {
					LOGGER.warning("No se pudo cargar el fichero de configuracion '" + getDocManagerConfigFilename(managerName) + //$NON-NLS-1$
							"'. Se cargara el gestor de documentos sin esta configuracion: " + e); //$NON-NLS-1$
				}

				// Desciframos las claves del fichero de configuracion si es necesario
				if (config != null && ConfigManager.hasDecipher()) {
					for (final String key : config.keySet().toArray(new String[config.size()])) {
						config.setProperty(key, ConfigManager.getDecipheredProperty(config, key, null));
					}
				}

				try {
					docManager.init(config);
				}
				catch (final Exception e) {
					LOGGER.severe("La clase gestora de documentos " + docManagerClassName + " genero un error durante la inicializacion: " + e); //$NON-NLS-1$ //$NON-NLS-2$
					throw new IOException("La clase gestora de documentos genero un error durante la inicializacion", e); //$NON-NLS-1$
				}
			}
			docManagers.put(managerName, docManager);
		}

		return docManagers.get(managerName);
	}

	/**
	 * Registra y devuelve el gestor de documentos por defecto.
	 * @return Gestor de documentos por defecto.
	 */
	private static FIReDocumentManager registryDefaultDocumentManager() {
		docManagers.put(DEFAULT_DOCUMENT_MANAGER, new DefaultFIReDocumentManager());
		return docManagers.get(DEFAULT_DOCUMENT_MANAGER);
	}

	/**
	 * Obtiene el nombre de fichero de configuraci&oacute;n asociado
	 * un gestor de documentos.
	 * @param docManagerName Nombre del gestor de documento
	 * @return Nombre del fichero de configuraci&oacute;n.
	 */
	private static String getDocManagerConfigFilename(final String docManagerName) {
		return "docmanager." + docManagerName + ".properties"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Revela si una instancia de DocumentManager es el por defecto. Esto es importante
	 * porque el DocumentManager por defecto recibe como identificador del documento
	 * los propios datos que se deben procesar, lo cual puede implicar un gran volumen de
	 * datos.
	 * @param docManager Gestor de documentos que se desea evaluar.
	 * @return {@code true} si el gestor es el por defecto, false en caso contrario.
	 */
	public static boolean isDefaultDocumentManager(final FIReDocumentManager docManager) {
		return docManager instanceof DefaultFIReDocumentManager;
	}
}
