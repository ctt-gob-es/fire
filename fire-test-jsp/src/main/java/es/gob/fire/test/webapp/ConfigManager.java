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

import java.io.FileNotFoundException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.client.ClientConfigFilesNotFoundException;
import es.gob.fire.client.FireClient;


/**
 * Manejador de la configuraci&oacute;n de la aplicaci&oacute;n para la obtenci&oacute;n de
 * las propiedades configuradas.
 */
public class ConfigManager {

	private static final String CONFIG_FILE = "test-app.properties"; //$NON-NLS-1$

	private static final String PROP_APP_ID = "appId"; //$NON-NLS-1$

	private static final String PROP_APP_NAME = "appName"; //$NON-NLS-1$

	private static final String PROP_URL_BASE = "urlbase"; //$NON-NLS-1$

	private static final String PROP_PROCEDURE_NAME = "procedureName"; //$NON-NLS-1$

	private static final String PROP_CERTIFICATE_ORIGIN = "certOrigin"; //$NON-NLS-1$

	private static final String PROP_APP_SKIP_CERT_SELECTION = "app.skipcertselection"; //$NON-NLS-1$

	private static ConfigManager instance = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

	private static final String SYS_PROP_PREFIX = "${"; //$NON-NLS-1$

	private static final String SYS_PROP_SUFIX = "}"; //$NON-NLS-1$

	private Properties config = null;

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


	private FireClient fireClient = null;

	private ConfigManager() {

		try {
			this.config = ConfigFileLoader.loadConfigFile(CONFIG_FILE);
		}
		catch (final FileNotFoundException e) {
			LOGGER.error("No se ha encontrado el fichero de configuracion de la conexion", e); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.error("No se pudo cargar el fichero de configuracion {}", CONFIG_FILE, e); //$NON-NLS-1$
		}

		this.config = mapEnvironmentVariables(this.config);
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
	 * Recupera valor del par&aacute;metro que indica si se debe omitir la pantalla de seleccion de certificados
	 * @return El par&aacute;metro skipcertselection.
	 */
	public boolean isAppSkipCertSelection() {
		boolean skipCertSelection = false;
		if (this.config != null) {
			skipCertSelection = Boolean.parseBoolean(this.config.getProperty(PROP_APP_SKIP_CERT_SELECTION));
		}
		return skipCertSelection;
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

	/**
	 * Sustituye las variables contenidas en los valores del objeto de propiedades por
	 * los valores establecidos a trav&eacute;s de variables de entorno. Las variables
	 * se declaran antecediendola con la part&iacute;cula "${" y cerrando con "}". Por
	 * ejemplo: <code>${variable}</code>.
	 * <br>Las variables a las que no se les asignen valor se quedan tal cual.
	 * @param prop Objeto de propiedades.
	 * @return Un nuevo objeto de propiedades con las variables reemplazadas.
	 */
	public static Properties mapEnvironmentVariables(final Properties prop) {

		if (prop == null) {
			return null;
		}

		final Properties mappedProperties = new Properties();
		for (final String k : prop.keySet().toArray(new String[0])) {
			mappedProperties.setProperty(k, mapSystemProperties(prop.getProperty(k)));
		}
		return mappedProperties;
	}

	/**
	 * Mapea las propiedades del sistema que haya en el texto que se referencien de
	 * la forma: ${propiedad}
	 * @param text Texto en el que se pueden encontrar las referencias a las propiedades
	 * del sistema.
	 * @return Cadena con las part&iacute;culas traducidas a los valores indicados como propiedades
	 * del sistema. Si no se encuentra la propiedad definida, no se modificar&aacute;.
	 */
	private static String mapSystemProperties(final String text) {

		if (text == null) {
			return null;
		}

		int pos = -1;
		int pos2 = 0;
		String mappedText = text;
		while ((pos = mappedText.indexOf(SYS_PROP_PREFIX, pos + 1)) > -1 && pos2 > -1) {
			pos2 = mappedText.indexOf(SYS_PROP_SUFIX, pos + SYS_PROP_PREFIX.length());
			if (pos2 > pos) {
				final String prop = mappedText.substring(pos + SYS_PROP_PREFIX.length(), pos2);
				final String value = System.getProperty(prop, null);
				if (value != null) {
					mappedText = mappedText.replace(SYS_PROP_PREFIX + prop + SYS_PROP_SUFIX, value);
				}
			}
		}
		return mappedText;
	}
}
