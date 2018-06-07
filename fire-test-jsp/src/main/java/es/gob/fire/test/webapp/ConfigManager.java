/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.test.webapp;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.fire.client.ClientConfigFilesNotFoundException;
import es.gob.fire.client.FireClient;


/**
 * Manejador de la configuraci&oacute;n de la aplicaci&oacute;n para la obtenci&oacute;n de
 * las propiedades configuradas.
 */
public class ConfigManager {

	private static final String CONFIG_FILE_PATH = "/test-app.properties"; //$NON-NLS-1$

	private static final String PROP_APP_ID = "appId"; //$NON-NLS-1$

	private static final String PROP_APP_NAME = "appName"; //$NON-NLS-1$

	private static final String PROP_URL_BASE = "urlbase"; //$NON-NLS-1$

	private static final String PROP_TMP_DIR = "tmpdir"; //$NON-NLS-1$

	private static final String PROP_PROCEDURE_NAME = "procedureName"; //$NON-NLS-1$

	private static final String PROP_CERTIFICATE_ORIGIN = "certOrigin"; //$NON-NLS-1$

	private static ConfigManager instance = null;

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

	/**
	 * Obtenemos una instancia del manejador de configuraci&oacute;n.
	 * @return Manejador de configuraci&oacute;n.
	 */
	public static ConfigManager getInstance() {
		if (instance == null) {
			instance = new ConfigManager();
		}
		return instance;
	}

	private final Properties config;

	private FireClient fireClient = null;

	private ConfigManager() {

		final InputStream is = ConfigManager.class.getResourceAsStream(CONFIG_FILE_PATH);

		this.config = new Properties();

		try {
			this.config.load(is);
		}
		catch (final Exception e) {
			LOGGER.warning("No se ha podido cargar la configuracion de la aplicacion: " + e); //$NON-NLS-1$
		}
		try {
			is.close();
		}
		catch (final Exception e) {
			LOGGER.warning("Error desconocido al cerrar el fichero de configuracion: " + e); //$NON-NLS-1$
		    // No se ha podido cerrar
		}
	}

	/**
	 * Recupera el identificador de la aplicaci&oacute;n.
	 * @return Identificador de la aplicaci&oacute;n.
	 */
	public String getAppId() {
		String appId = null;
		if (this.config != null) {
			appId = this.config.getProperty(PROP_APP_ID);
		}
		return appId;
	}

	/**
	 * Recupera el nombre de la aplicaci&oacute;n.
	 * @return Nombre de la aplicaci&oacute;n.
	 */
	public String getAppName() {
		String appName = null;
		if (this.config != null) {
			appName = this.config.getProperty(PROP_APP_NAME);
		}
		return appName;
	}

	/**
	 * Recupera el identificador de la aplicaci&oacute;n.
	 * @param relativeUrl Porci&oacute;n de la URL a la que adherir la URL base.
	 * @return Identificador de la aplicaci&oacute;n.
	 */
	public String addUrlBase(final String relativeUrl) {
		String urlBase = null;
		if (this.config != null) {
			urlBase = this.config.getProperty(PROP_URL_BASE);
		}
		if (urlBase == null) {
			return relativeUrl;
		}
		if (!urlBase.endsWith("/")) { //$NON-NLS-1$
			urlBase += "/"; //$NON-NLS-1$
		}
		return urlBase + relativeUrl;
	}

	/**
	 * Recupera el directorio temporal definido para la aplicaci&oacute;n.
	 * @return Ruta del directorio temporal o {@code null} si no est&aacute; definido.
	 */
	public String getTempDir() {
		String tmpDir = null;
		if (this.config != null) {
			tmpDir = this.config.getProperty(PROP_TMP_DIR);
		}
		return tmpDir;
	}

	/**
	 * Recupera el nombre de procedimiento asociado a la operaci&oacute;n
	 * que lleva a cabo la aplicaci&oacute;n. Si nuestra aplicaci&oacute;n
	 * tuviese varios procedimientos, cada uno tendr&oacute;a un nombre
	 * distinto.
	 * @return Nombre del procedimiento.
	 */
	public String getProcedureName() {
		String procName = null;
		if (this.config != null) {
			procName = this.config.getProperty(PROP_PROCEDURE_NAME);
		}
		return procName;
	}

	/**
	 * Recupera el origen configurado del certificado de firma.
	 * @return Origen del certificado de firma o {@code null} si
	 * no se ha predefinido.
	 */
	public String getCertOrigin() {
		String origin = null;
		if (this.config != null) {
			origin = this.config.getProperty(PROP_CERTIFICATE_ORIGIN);
		}
		return origin;
	}

	/**
	 * Recupera la instancia del componente distribuido de FIRe con el que hacer
	 * las llamadas al componente central.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @return Instancia del componente distribuido de FIRe.
	 * @throws ClientConfigFilesNotFoundException Si no se ha podido encontrar el
	 * fichero de configuraci&oacute;n de componente distribuido.
	 */
	public FireClient getFireClient(final String appId) throws ClientConfigFilesNotFoundException {
		if (this.fireClient == null) {
			this.fireClient = new FireClient(appId, new FakePasswordDecipher());
		}
		return this.fireClient;
	}
}
