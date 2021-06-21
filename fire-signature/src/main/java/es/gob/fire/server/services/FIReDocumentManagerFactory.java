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
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.document.FIReDocumentManager;
import es.gob.fire.server.services.document.DefaultFIReDocumentManager;
import es.gob.fire.server.services.internal.AlarmsManager;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.signature.ConfigFileLoader;
import es.gob.fire.signature.ConfigManager;

/**
 * Factoria para la obtenci&oacute;n de los distintos gestores de documentos. Al
 * utilizar esta factor&iacute;a, que almacena y reutiliza las instancias de los
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
	 * indica ninguno, se devuelve una de la clase por defecto.
	 * @param appId Identificador de la aplicaci&oacute;n que solicita el gestor.
	 * @param trId Identificador de transacci&oacute;n (s&oacute;lo se usa para el log).
	 * @param docManagerName Nombre del gestor que se desea recuperar o {@code null}
	 * para obtener el por defecto.
	 * @return Gestor de documentos.
	 * @throws IllegalArgumentException Cuando se indica un gestor de documentos que no existe.
	 * @throws IOException Cuando no es posible cargar el gestor de documentos.
	 * @throws IllegalAccessException Cuando no se tenga permisos para acceder al gestor de
	 * documentos indicado.
	 */
	public static FIReDocumentManager newDocumentManager(final String appId, final String trId,
			final String docManagerName)
			throws IllegalArgumentException, IOException, IllegalAccessException {

		String managerName = docManagerName;
		if (managerName == null) {
			managerName = DEFAULT_DOCUMENT_MANAGER;
		}

		// Si se intenta cargar un gestor de documentos distinto al por defecto y no se tiene permiso para ello,
		// se devuelve un error
		if (!managerName.equals(DEFAULT_DOCUMENT_MANAGER) && !ConfigManager.isDocumentManagerAllowed(appId, managerName)) {
			throw new IllegalAccessException("La aplicacion no tiene habilitado el acceso al gestor de documentos: " + managerName); //$NON-NLS-1$
		}

		final LogTransactionFormatter logF = new LogTransactionFormatter(appId, trId);

		// Si no se tiene cargada ya la clase gestora, se carga ahora
		if (!docManagers.containsKey(managerName)) {

			final String docManagerClassName = ConfigManager.getDocumentManagerClassName(managerName);

			// Comprobamos que la clase este definida en el fichero de configuracion
			if (docManagerClassName == null) {
				// Si no hay definida en el fichero de configuracion una clase especifica en el fichero
				// de configuracion, cargamos la clase gestora original
				if (DEFAULT_DOCUMENT_MANAGER.equals(managerName)) {
					LOGGER.warning(logF.f("No se ha encontrado la clase gestora por defecto en el fichero de configuracion. Se establecera la clase original.")); //$NON-NLS-1$
					return registryDefaultDocumentManager();
				}
				throw new IllegalArgumentException("No hay definida una clase gestora de documentos con el nombre " + managerName); //$NON-NLS-1$
			}

			FIReDocumentManager docManager;
			try {
				docManager = (FIReDocumentManager) Class.forName(docManagerClassName).
						getConstructor().newInstance();
			}
			catch (final ClassNotFoundException e) {
				AlarmsManager.notify(Alarm.LIBRARY_NOT_FOUND, docManagerClassName);
				throw new IOException("No se encontro la clase gestora de documentos " + docManagerClassName, e); //$NON-NLS-1$
			}
			catch (final NoSuchMethodException | IllegalAccessException e) {
				throw new IOException("La clase gestora de documentos no tiene definido el constructor por defecto o este no es publico", e); //$NON-NLS-1$
			}
			catch (final Exception e) {
				throw new IOException("La clase gestora de documentos genero un error durante la construccion", e); //$NON-NLS-1$
			}

			// Si el gestor de documentos indica que requiere configuracion externa,
			// intentamos cargarla para inicializarlo
			Properties config = null;
			if (docManager.needConfiguration()) {
				final String configFilename = getDocManagerConfigFilename(managerName);
				try {
					config = ConfigFileLoader.loadConfigFile(configFilename);
				}
				catch (final FileNotFoundException e) {
					AlarmsManager.notify(Alarm.RESOURCE_NOT_FOUND, configFilename);
					LOGGER.warning(logF.f("No se encontro el fichero de configuracion '") + configFilename + //$NON-NLS-1$
							"'. Se cargara el gestor de documentos sin esta configuracion: " + e); //$NON-NLS-1$
				}
				catch (final Exception e) {
					LOGGER.warning(logF.f("No se pudo cargar el fichero de configuracion '") + configFilename + //$NON-NLS-1$
							"'. Se cargara el gestor de documentos sin esta configuracion: " + e); //$NON-NLS-1$
				}

				try {
					config = ConfigManager.mapEnvironmentVariables(config);
				} catch (final Exception e) {
					LOGGER.log(Level.WARNING,
							logF.f("No se pudieron mapear las variables de entorno en el fichero de configuracion: ") //$NON-NLS-1$
							+ configFilename, e);
				}

				// Desciframos las claves del fichero de configuracion si es necesario
				if (config != null && ConfigManager.hasDecipher()) {
					for (final String key : config.keySet().toArray(new String[config.size()])) {
						config.setProperty(key, ConfigManager.getDecipheredProperty(config, key, null));
					}
				}
			}

			try {
				docManager.init(config);
			}
			catch (final Exception e) {
				throw new IOException("La clase gestora de documentos '" + docManagerClassName + "' genero un error durante la inicializacion", e); //$NON-NLS-1$ //$NON-NLS-2$
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
