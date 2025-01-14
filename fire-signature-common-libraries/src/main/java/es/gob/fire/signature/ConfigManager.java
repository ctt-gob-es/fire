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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.decipher.PropertyDecipher;

/**
 * Manejador que gestiona la configuraci&oacute;n de la aplicaci&oacute;n.
 */
public class ConfigManager {

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

	private static final String SYS_PROP_PREFIX = "${"; //$NON-NLS-1$

	private static final String ENV_PROP_PREFIX = "%{"; //$NON-NLS-1$

	private static final String PROP_SUFIX = "}"; //$NON-NLS-1$

	private static final String PROP_DATASOURCE_JNDI_NAME = "datasource.jndi-name"; //$NON-NLS-1$

	private static final String PROP_DB_POOL_SIZE = "bbdd.pool.size"; //$NON-NLS-1$

	private static final int DEFAULT_DB_POOL_SIZE = 15;

	private static final String PARAM_CIPHER_CLASS = "cipher.class"; //$NON-NLS-1$

	private static final String PROP_TEMP_DIR = "temp.dir"; //$NON-NLS-1$

	/** Propiedad utilizada para indicar los proveedores activos en el componente central. */
	private static final String PROP_PROVIDERS_LIST = "providers"; //$NON-NLS-1$

	/** Prefijo utilizado para las propiedades que determinan la clase principal de un proveedor.
	 * Ejemplo: provider.NOMBRE_PROVEEDOR=es.gob.fire.MI_CLASE_CONECTORA*/
	private static final String PREFIX_PROP_PROVIDER = "provider."; //$NON-NLS-1$

	/** Sufijo utilizado junto prefijo y nombre de un proveedor para determinar si este se ha
	 * configurado como un proveedor seguro o no.
	 * Ejemplo: provider.NOMBRE_PROVEEDOR.secure=true */
	private static final String SUFIX_PROP_SECURE_PROVIDER = ".secure"; //$NON-NLS-1$

	/** Sufijo utilizado junto prefijo y nombre de un proveedor para determinar si se debe cargar
	 * un fichero externo con la informaci&oacute;n del proveedor en lugar de su fichero externo.
	 * Ejemplo: provider.NOMBRE_PROVEEDOR.info.file=/users/usuario/fire_config/proveedorinfo.properties */
	private static final String SUFIX_PROP_INFO_FILE_PROVIDER = ".info.file"; //$NON-NLS-1$

	/** Propiedad con la clase encargada de la validaci&oacute;n y actualizaci&oacute;n de las firmas. */
	private static final String PROP_VALIDATOR_CLASS = "validator.class"; //$NON-NLS-1$

	/**
	 * Sufijo utilizado para las propiedades que determinan si un proveedor es de confianza.
	 * Ejemplo: provider.NOMBRE_PROVEEDOR.trusted=true
	 */
	private static final String SUFIX_PROP_TRUSTED_PROVIDER = ".trusted"; //$NON-NLS-1$

	private static final String PROP_APP_ID = "default.appId"; //$NON-NLS-1$

	private static final String PROP_CERTIFICATE = "default.certificate"; //$NON-NLS-1$

	private static final String PROP_LOCAL_VERIFICATION_KEY = "local.verification.key"; //$NON-NLS-1$

	/** Tama&ntilde;o m&aacute;ximo permitido para un par&aacute;metro de la petici&oacute;n. */
	private static final String DEFAULT_PARAMS_MAX_SIZE = "8388608"; // 8 Mb //$NON-NLS-1$

	/** Propiedad con el tama&ntilde;o m&aacute;ximo permitido para una petici&oacute;n. */
	private static final String PROP_PARAMS_MAX_SIZE = "params.maxSize"; //$NON-NLS-1$

	/** Tama&ntilde;o m&aacute;ximo permitido para una petici&oacute;n. */
	private static final String DEFAULT_REQUEST_MAX_SIZE = "12582912"; // 12 Mb //$NON-NLS-1$

	/**
	 * Propiedad con el tama&ntilde;o m&aacute;ximo permitido para un par&aacute;metro
	 * de la petici&oacute;n.
	 */
	private static final String PROP_REQUEST_MAX_SIZE = "request.maxSize"; //$NON-NLS-1$

	private static final String PROP_BATCH_MAX_DOCUMENTS = "batch.maxDocuments"; //$NON-NLS-1$

	private static final String PROP_FIRE_TEMP_TIMEOUT = "temp.fire.timeout"; //$NON-NLS-1$

	/** Segundos que, por defecto, tardan los ficheros temporales del proceso de firma de lote en caducar. */
	private static final int DEFAULT_FIRE_TEMP_TIMEOUT = 600; // 10 minutos

	private static final String PROP_FIRE_PAGES_TITLE = "pages.title"; //$NON-NLS-1$

	private static final String PROP_FIRE_PAGES_LOGO_URL = "pages.logo"; //$NON-NLS-1$

	private static final String PROP_FIRE_PUBLIC_URL = "pages.public.url"; //$NON-NLS-1$

	private static final String PROP_ALARMS_NOTIFIER = "alarms.notifier"; //$NON-NLS-1$

	private static final String PROP_DOCUMENT_MANAGER_PREFIX = "docmanager."; //$NON-NLS-1$

	private static final String PROP_DOCMANAGER_REQUESTOR_PARTICLE = ".requestor"; //$NON-NLS-1$

	private static final String PROP_DOCMANAGER_REQ_VALID_SUFIX = PROP_DOCMANAGER_REQUESTOR_PARTICLE + ".valid"; //$NON-NLS-1$

	private static final String PROP_DOCMANAGER_REQ_INVALID_SUFIX = PROP_DOCMANAGER_REQUESTOR_PARTICLE + ".invalid"; //$NON-NLS-1$

	private static final String PROP_SESSIONS_DAO = "sessions.dao"; //$NON-NLS-1$

	private static final String PROP_SESSIONS_FORCE_SHARING = "sessions.sharing.forced"; //$NON-NLS-1$

	private static final String PROP_TEMP_DOCUMENTS_DAO = "sessions.documents.dao"; //$NON-NLS-1$

	private static final String PROP_LOGS_DIR = "logs.dir"; //$NON-NLS-1$

	private static final String PROP_LOGS_ROLLING_POLICY = "logs.rollingPolicy"; //$NON-NLS-1$

	private static final String PROP_LOGS_LEVEL_FIRE = "logs.level.fire"; //$NON-NLS-1$

	private static final String PROP_LOGS_LEVEL_AFIRMA = "logs.level.afirma"; //$NON-NLS-1$

	private static final String PROP_LOGS_LEVEL_GENERAL = "logs.level"; //$NON-NLS-1$

	private static final String DEFAULT_FIRE_LOGS_LEVEL = Level.INFO.getName();
	private static final String DEFAULT_AFIRMA_LOGS_LEVEL = Level.WARNING.getName();
	private static final String DEFAULT_GENERAL_LOGS_LEVEL = Level.OFF.getName();

	private static final String PROP_SKIP_CERT_SELECTION = "skipCertSelection"; //$NON-NLS-1$

	private static final String PROP_HTTP_CERT_ATTR = "http.cert.attr"; //$NON-NLS-1$

	private static final String DEFAULT_HTTP_CERT_ATTR = "x-clientcert"; //$NON-NLS-1$

	/** Configuraci&oacute;n de la pol&iacute;tica de volcado de datos estad&iacute;sticos*/
	private static final String PROP_STATISTICS_POLICY ="statistics.policy"; //$NON-NLS-1$

	/** Configuraci&oacute;n de la hora del volcado a base de datos si la pol&iacute;tica lo permite). */
	private static final String PROP_STATISTICS_DUMPTIME = "statistics.dumptime"; //$NON-NLS-1$

	/** Configuraci&oacute;n del directorio de volcado de datosestad&iacute;sticos. */
	private static final String PROP_STATISTICS_DIR = "statistics.dir"; //$NON-NLS-1$

	/** Configuraci&oacute;n de la pol&iacute;tica de volcado de datos estad&iacute;sticos. */
	private static final String PROP_AUDIT_POLICY ="audit.policy"; //$NON-NLS-1$

	/** Configuraci&oacute;n de la hora de borrado de los datos de auditoria de base de datos. */
	private static final String PROP_AUDIT_DELETE_TIME ="audit.deletetime"; //$NON-NLS-1$

	/** Configuraci&oacute;n del directorio de volcado de datosestad&iacute;sticos. */
	private static final String PROP_AUDIT_DIR = "audit.dir"; //$NON-NLS-1$

	private static final String USE_TSP = "usetsp"; //$NON-NLS-1$
	private static final String PROP_CHECK_CERTIFICATE = "security.checkCertificate"; //$NON-NLS-1$
	private static final String PROP_CHECK_APPLICATION = "security.checkApplication"; //$NON-NLS-1$

	private static final String PROVIDER_LOCAL = "local"; //$NON-NLS-1$

	/** Cadena utilizada para separar valores dentro de una propiedad. */
	private static final String VALUES_SEPARATOR = ","; //$NON-NLS-1$

	private static final String PREFIX_CIPHERED_TEXT = "{@ciphered:"; //$NON-NLS-1$
	private static final String SUFIX_CIPHERED_TEXT = "}"; //$NON-NLS-1$

	/**
	 * N&uacute;mero que identifica cuando el valor de configuraci&oacute;n que indica que
	 * el n&uacute;mero de documentos no est&aacute; limitado.
	 */
	public static final int UNLIMITED_NUM_DOCUMENTS = 0;
	/**
	 * N&uacute;mero que identifica cuando el valor de configuraci&oacute;n que indica el
	 * tama&ntilde;o m&aacute;ximo el n&uacute;mero de documentos no est&aacute; limitado.
	 */
	public static final int UNLIMITED_MAX_SIZE = 0;

	/** Ruta del directorio por defecto para el guardado de temporales (directorio temporal del sistema). */
	private static String DEFAULT_TMP_DIR;

	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String CONFIG_FILE = "fire_config.properties"; //$NON-NLS-1$

	/**
	 * Antiguo nombre del fichero de configuraci&oacute;n.
	 * @deprecated Usar {@link #CONFIG_FILE}.
	 */
	@Deprecated
	private static final String CONFIG_FILE_OLD = "config.properties"; //$NON-NLS-1$

	private static final String PATTERN_TIME = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$"; //$NON-NLS-1$

	private static Properties config = null;

	private static boolean initialized = false;

	private static PropertyDecipher decipherImpl = null;


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
	 * @throws ConfigFilesException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	private static void loadConfig() throws  ConfigFilesException {

		String configFilename;
		if (config == null) {
			try {
				config = ConfigFileLoader.loadConfigFile(CONFIG_FILE);
				configFilename = CONFIG_FILE;
			}
			catch (final Exception e) {
				try {
					config = ConfigFileLoader.loadConfigFile(CONFIG_FILE_OLD);
					configFilename = CONFIG_FILE_OLD;
				}
				catch (final Exception e2) {
					throw new ConfigFilesException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
				}
			}

			LOGGER.info("Se carga la configuracion de FIRe a traves del fichero " + configFilename); //$NON-NLS-1$

			try {
				config = mapEnvironmentVariables(config);
			}
			catch (final Exception e) {
				LOGGER.severe("No se pudieron mapear las variables de entorno del fichero de configuracion " + configFilename); //$NON-NLS-1$
			}

			// Comprobamos el valor establecido para la clase de cifrado
			if (config != null) {
				if (config.containsKey(PARAM_CIPHER_CLASS)) {
					final String decipherClassname = config.getProperty(PARAM_CIPHER_CLASS);
					if (decipherClassname != null && !decipherClassname.trim().isEmpty()) {
						try {
							final Class<?> decipherClass = Class.forName(decipherClassname);
							final Object decipher = decipherClass.getConstructor().newInstance();
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

			// Comprobamos si se establecieron las propiedades para la conexion con la base de
			// datos y advertimos de las consecuencias en caso contrario
			if (getProperty(PROP_DATASOURCE_JNDI_NAME) == null) {
				LOGGER.warning("No se ha declarado el nombre del datasource encargado de la conexión a base de datos." //$NON-NLS-1$
						+ String.format("Asegurese de habilitar las propiedades %1s y %2s como alternativa", //$NON-NLS-1$
								PROP_APP_ID, PROP_CERTIFICATE));
			}
		}
	}


	/**
	 * Devuelve el listado de nombres de los proveedores configurados.
	 * En caso de que no se haya definido ninguno, este listado
	 * estar&aacute; vac&iacute;o.
	 * @return Listado de proveedores configurados.
	 */
	public static ProviderElement[] getProviders() {
		final String providers = getProperty(PROP_PROVIDERS_LIST);
		if (providers == null) {
			return new ProviderElement[0];
		}

		final List<ProviderElement> providersList = new ArrayList<>();
		final String[] providersTempList = providers.split(VALUES_SEPARATOR);
		for (final String provider : providersTempList) {
			if (provider != null && !provider.trim().isEmpty()) {
				final ProviderElement prov = new ProviderElement(provider);
				if (!providersList.contains(prov)) {
					providersList.add(prov);
				}
			}
		}
		return providersList.toArray(new ProviderElement[providersList.size()]);
	}

	/**
	 * Recupera el nombre de la clase de conexi&oacute;n de un proveedor.
	 * @param name Nombre del proveedor.
	 * @return Clase de conexi&oacute;n del proveedor.
	 */
	public static String getProviderClass(final String name) {
		return getProperty(PREFIX_PROP_PROVIDER + name);
	}

	/**
	 * Recupera el fichero externo de la conexi&oacute;n de un proveedor.
	 * @param name Nombre del proveedor.
	 * @return fichero externo o null si no est&aacute; definido
	 */
	public static String getProviderInfoFile(final String name) {
		// El proveedor local no tiene configuracion externa
		if (PROVIDER_LOCAL.equalsIgnoreCase(name)) {
			return null;
		}
		return getProperty(PREFIX_PROP_PROVIDER + name + SUFIX_PROP_INFO_FILE_PROVIDER);
	}

	/**
	 * Identifica si se ha configurado un proveedor como seguro y, por lo tanto, no es
	 * posible que las firmas realizadas con &eacute;n no sean v&aacute;lidas.
	 * @param name Nombre del proveedor.
	 * @return {@code true} si el proveedor se configur&oacute; como de confianza, {@code false}
	 * si es el proveedor local, si no se configur&oacute; o si se configur&oacute; expresamente
	 * que no lo es.
	 */
	public static boolean isSecureProvider(final String name) {
		// El proveedor local nunca sera seguro
		if (PROVIDER_LOCAL.equalsIgnoreCase(name)) {
			return false;
		}
		return Boolean.parseBoolean(getProperty(PREFIX_PROP_PROVIDER + name + SUFIX_PROP_SECURE_PROVIDER));
	}

	/**
	 * Indica si un proveedor esta configurado como proveedor de confianza.
	 * No debe usarse con el proveedor "local".
	 * @param name Nombre del proveedor.
	 * @return {@code true} si el proveedor es de confianza y no es necesario validar sus firmas,
	 * {@code false} en caso contrario.
	 */
	public static boolean isTrusted(final String name) {
		return Boolean.parseBoolean(getProperty(PREFIX_PROP_PROVIDER + name + SUFIX_PROP_TRUSTED_PROVIDER));
	}

	/**
	 * Recupera el nombre de la clase para la validaci&oacute;n y mejora de firmas.
	 * @return Clase de conexi&oacute;n del proveedor.
	 */
	public static String getValidatorClass() {
		return getProperty(PROP_VALIDATOR_CLASS);
	}

	/**
	 * Recupera el nombre de la clase para la validaci&oacute;n y mejora de firmas.
	 * @param required Establece si es obligatorio que haya un valor definido.
	 * @return Clase de conexi&oacute;n del proveedor.
	 * @throws InvalidConfigurationException Cuando sea obligatorio qe haya un valor definido
	 * y no se encuentre.
	 */
	public static String getValidatorClass(final boolean required) throws InvalidConfigurationException {
		return getProperty(PROP_VALIDATOR_CLASS, required);
	}

	/**
	 * Recupera el nombre del datasource para el acceso a la base de datos.
	 * @return nombre del datasource.
	 */
	public static String getDatasourceJNDIName(){
		final String value = getProperty(PROP_DATASOURCE_JNDI_NAME);
		return value != null && !value.isEmpty() ? value : null;
	}

	/**
	 * Recupera el texto para la composici&oacute;n de la clave HMac para la
	 * verificaci&oacute;n de del PKCS#1 de la firma.
	 * @return Cadena a partir de la que componer la clave HMac.
	 */
	public static String getHMacKey() {
		final String value = getProperty(PROP_LOCAL_VERIFICATION_KEY);
		return value != null && !value.isEmpty() ? value : null;
	}

	/**
	 * Recupera el tama&ntilde;o m&aacute;ximo que puede tener un par&aacute;metro enviado en la
	 * petici&oacute;n. Si se devuelve el valor {@code #UNLIMITED_MAX_SIZE} se
	 * debe considerar que no hay l&iacute;mite al n&uacute;mero de documentos de un
	 * lote.
	 * @return Tama&ntilde;o m&aacute;ximo de un par&aacute;metro enviado al servicio.
	 */
	public static int getParamMaxSize() {
		try {
			return Integer.parseInt(getProperty(PROP_PARAMS_MAX_SIZE, DEFAULT_PARAMS_MAX_SIZE));
		}
		catch (final Exception e) {
			LOGGER.warning("Se encontro un valor invalido para la propiedad '" + //$NON-NLS-1$
					PROP_PARAMS_MAX_SIZE +
					"' del fichero de configuracion. Se establecera el valor " + DEFAULT_PARAMS_MAX_SIZE); //$NON-NLS-1$
			return Integer.parseInt(DEFAULT_PARAMS_MAX_SIZE);
		}
	}



	public static long getRequestMaxSize() {
		try {
			return Long.parseLong(getProperty(PROP_REQUEST_MAX_SIZE, DEFAULT_REQUEST_MAX_SIZE));
		}
		catch (final Exception e) {
			LOGGER.warning("Se encontro un valor invalido para la propiedad '" + //$NON-NLS-1$
					PROP_REQUEST_MAX_SIZE +
					"' del fichero de configuracion. Se establecera el valor " + DEFAULT_REQUEST_MAX_SIZE); //$NON-NLS-1$
			return Long.parseLong(DEFAULT_REQUEST_MAX_SIZE);
		}
	}

	/**
	 * Recupera el n&uacute;mero m&aacute;ximo de documentos que se pueden agregar a
	 * un lote de firma. Si se devuelve el valor {@code #UNLIMITED_NUM_DOCUMENTS} se
	 * debe considerar que no hay l&iacute;mite al n&uacute;mero de documentos de un
	 * lote.
	 * @return N&uacute;mero de documentos que se pueden agregar a un lote.
	 */
	public static int getBatchMaxDocuments() {
		try {
			return Integer.parseInt(getProperty(PROP_BATCH_MAX_DOCUMENTS));
		}
		catch (final Exception e) {
			LOGGER.warning("Se encontro un valor invalido para la propiedad '" + //$NON-NLS-1$
					PROP_BATCH_MAX_DOCUMENTS +
					"' del fichero de configuracion. No se establecera limite al numero de ficheros."); //$NON-NLS-1$
			return UNLIMITED_NUM_DOCUMENTS;
		}
	}

	/**
	 * Lanza una excepci&oacute;n en caso de que no encuentre el fichero de configuraci&oacute;n o
	 * no se encuentren propiedades obligatorias.
	 * @throws ConfigFilesException Si no encuentra el fichero config.properties.
	 * @throws InvalidConfigurationException Si hay una propiedad mal configurada.
	 */
	public static void checkConfiguration() throws ConfigFilesException, InvalidConfigurationException {

		initialized = false;

		loadConfig();

		if (config == null) {
			throw new ConfigFilesException("No se ha encontrado el fichero de configuracion del componente central", CONFIG_FILE); //$NON-NLS-1$
		}

		final ProviderElement[] providers = getProviders();
		if (providers == null) {
			throw new InvalidConfigurationException("Debe declararse al menos un proveedor con la propiedad " + PROP_PROVIDERS_LIST, PROP_PROVIDERS_LIST, CONFIG_FILE); //$NON-NLS-1$
		}

		checkProviders(providers);

		if (isCheckApplicationNeeded() && getDatasourceJNDIName() == null && getAppId() == null) {
			throw new InvalidConfigurationException("No se ha configurado el acceso a la base de datos en la propiedad " + PROP_DATASOURCE_JNDI_NAME //$NON-NLS-1$
					+ ", ni tampoco la propiedad " + PROP_APP_ID //$NON-NLS-1$
					+ " para la verificacion del identificador de aplicacion", //$NON-NLS-1$
					PROP_DATASOURCE_JNDI_NAME, CONFIG_FILE);
		}
		if (isCheckCertificateNeeded() && getDatasourceJNDIName() == null && getCert() == null) {
			throw new InvalidConfigurationException("No se ha configurado el acceso a la base de datos ni el campo " + PROP_CERTIFICATE //$NON-NLS-1$
					+ " para la verificacion del certificado de auteticacion", PROP_DATASOURCE_JNDI_NAME, CONFIG_FILE); //$NON-NLS-1$
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
	 * Devuelve el certificado del fichero de propiedades en caso de que no se encuentre la
	 * cadena conexi&oacute;n a la base de datos.
	 * @return el certificado.
	 */
	public static String getCert(){
		return getProperty(PROP_CERTIFICATE);
	}

	/**
	 * Devuelve el identificador de la aplicaci&oacute;n en caso de que no se encuentre la
	 * cadena conexi&oacute;n a la base de datos.
	 * @return identificador de la aplicaci&oacute;n.
	 */
	public static String getAppId(){
		return getProperty(PROP_APP_ID);
	}

	/**
	 * Devuelve el directorio configurado para el guardado de temporales.
	 * @return Ruta del directorio temporal o, si no se configuro, el directorio
	 * temporal del sistema. En caso de error, devolver&aacute; {@code null}.
	 */
	public static String getTempDir() {

		if (config == null) {
			try {
				loadConfig();
			} catch (final ConfigFilesException e) {
				LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				return null;
			}
		}

		return getProperty(PROP_TEMP_DIR, DEFAULT_TMP_DIR);
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
	 * Devuelve la hora a la que deber&iacute;n volcarse los datos estad&iacute;sticos a base de datos. En caso de no
	 * encontrarse configurada una hora con el formato hh:mm:ss se devolver&aacute; 00:00:00.
	 * @return Hora con formato hh:mm:ss
	 *
	 */
	public static String getStatisticsDumpTime() {
		 String time =  getProperty(PROP_STATISTICS_DUMPTIME);
		 if (time == null || !time.matches(PATTERN_TIME)) {
			 time = "00:00:00";	 //$NON-NLS-1$
		 }
		return time;
	}

	/**
	 * Devuelve la ruta configurada del directorio en el que almacenar los datos para la generaci&oacute;n de estad&iacute;sticas.
	 * @return Ruta del directorio o {@code null} si no est&aacute; definida.
	 */
	public static String getAuditDir() {
		 return getProperty(PROP_AUDIT_DIR);
	}

	/**
	 * Devuelve el identificador num&eacute;rico de la pol&iacute;tica de firma configurada.
	 * En caso de error, devuelve -1.
	 * @return Dato num&eacute;rico.
	 */
	public static int getAuditPolicy() {
		int policy;
		try {
			policy = Integer.parseInt(getProperty(PROP_AUDIT_POLICY));
		}
		catch (final NumberFormatException e) {
			policy = -1;
		}
		return policy;
	}

	/**
	 * Devuelve la hora a la que se debe realizar el borrado de los datos de auditoria de base de
	 * datos o {@code null} si no
	 * @return Hora con formato hh:mm:ss o {@code null} si no se establece.
	 */
	public static String getAuditDeleteTime() {
		 return getProperty(PROP_AUDIT_DELETE_TIME);
	}

	/**
	 * Devuelve la ruta configurada del directorio en el que almacenar los datos para la generaci&oacute;n de estad&iacute;sticas.
	 * @return Ruta del directorio o {@code null} si no est&aacute; definida.
	 */
	public static String getStatisticsDir() {
		 return getProperty(PROP_STATISTICS_DIR);
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

	private static void checkProviders(final ProviderElement[] providers) throws InvalidConfigurationException {
		final List<String> wrongProviders = new ArrayList<>(providers.length);
		for (final ProviderElement provider : providers) {
			final String providerName = provider.getName();
			if (!PROVIDER_LOCAL.equalsIgnoreCase(providerName) &&
					!config.containsKey(PREFIX_PROP_PROVIDER + providerName)) {
				wrongProviders.add(providerName);
			}
		}
		if (!wrongProviders.isEmpty()) {
			final StringBuilder errorMsg = new StringBuilder();
			for (final String providerName : wrongProviders) {
				if (errorMsg.length() != 0) {
					errorMsg.append(", "); //$NON-NLS-1$
				}
				errorMsg.append(providerName);
			}
			throw new InvalidConfigurationException("No se ha definido la clase conectora de los proveedores: " + errorMsg.toString(), errorMsg.toString(), CONFIG_FILE); //$NON-NLS-1$
		}
	}

	/**
	 * Recupera valor del par&aacute;metro de sello de tiempo.
	 * @return El par&aacute;metro usetsp.
	 */
	public static boolean existUseTsp(){
		return 	Boolean.parseBoolean(getProperty(USE_TSP, Boolean.FALSE.toString()));
	}

	/**
	 * Recupera el tiempo en milisegundos que debe transcurrir antes de considerar caducados los
	 * ficheros temporales almacenados durante un proceso de firma. Si no se encuentra configurado
	 * un valor, se usara el valor por defecto.
	 * @return N&uacute;mero de milisegundos que como m&iacute;nimo se almacenar&aacute;n los
	 * ficheros temporales.
	 */
	public static long getTempsTimeout() {

		if (config == null) {
			try {
				loadConfig();
			} catch (final ConfigFilesException e) {
				LOGGER.warning("No se puede cargar el fichero de configuracion del componente central. Se usaran " //$NON-NLS-1$
						+ DEFAULT_FIRE_TEMP_TIMEOUT + " segundos: " + e); //$NON-NLS-1$
				return (long) DEFAULT_FIRE_TEMP_TIMEOUT * 1000;
			}
		}

		try {
			return Long.parseLong(getProperty(PROP_FIRE_TEMP_TIMEOUT, Integer.toString(DEFAULT_FIRE_TEMP_TIMEOUT)))
					* 1000;
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Tiempo de expiracion invalido en la propiedad '" + PROP_FIRE_TEMP_TIMEOUT + //$NON-NLS-1$
					"' del fichero de configuracion. Se usaran " + DEFAULT_FIRE_TEMP_TIMEOUT + //$NON-NLS-1$
					" segundos: " + e); //$NON-NLS-1$
			return (long) DEFAULT_FIRE_TEMP_TIMEOUT * 1000;
		}
	}

	/**
	 * Recupera el t&iacute;tulo a asignar a las p&aacute;ginas web del componente central.
	 * @return T&iacute;tulo configurado para las paginas o cadena vac&iacute;a si no se
	 * especific&oacute; uno.
	 */
	public static String getPagesTitle() {
		if (config == null) {
			try {
				loadConfig();
			} catch (final ConfigFilesException e) {
				LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			}
		}

		return getProperty(PROP_FIRE_PAGES_TITLE, ""); //$NON-NLS-1$
	}

	/**
	 * Recupera la URL configurada de la imagen de logo que se debe mostrar en
	 * las p&aacute;ginas web del componente central.
	 * @return URL completa de la imagen de logo o cadena vac&iacute;a si no se ha configurado.
	 */
	public static String getPagesLogoUrl() {
		if (config == null) {
			try {
				loadConfig();
			} catch (final ConfigFilesException e) {
				LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			}
		}

		return getProperty(PROP_FIRE_PAGES_LOGO_URL);
	}

	/**
	 * Recupera la clase {@code AlarmNotifier}
	 * con la que notificar las alarmas identificadas.
	 * @return Nombre cualificado de la clase.
	 */
	public static String getAlarmsNotifierClassName() {

		if (config == null) {
			try {
				loadConfig();
			} catch (final ConfigFilesException e) {
				LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			}
		}

		return getProperty(PROP_ALARMS_NOTIFIER);
	}

	/**
	 * Recupera la clase DocumentManager con la que obtener los datos a firmar y
	 * guardar la firma.
	 * @param docManager Identificador del DocumentManager.
	 * @return Nombre cualificado de la clase.
	 */
	public static String getDocumentManagerClassName(final String docManager) {

		if (config == null) {
			try {
				loadConfig();
			} catch (final ConfigFilesException e) {
				LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			}
		}

		return getProperty(PROP_DOCUMENT_MANAGER_PREFIX + docManager);
	}

	/**
	 * Indica si una aplicaci&oacute;n tiene permisos para el uso de un gestor de documentos concreto.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @param docManager Identificador del gestor de documentos.
	 * @return {@code true} si tiene permiso, {@code false} en caso contrario.
	 */
	public static boolean isDocumentManagerAllowed(final String appId, final String docManager) {

		// Si hay un listado de aplicaciones validas, comprobamos que la indicada este entre ellas
		final String validsList = getProperty(PROP_DOCUMENT_MANAGER_PREFIX + docManager + PROP_DOCMANAGER_REQ_VALID_SUFIX);
		if (validsList != null) {
			return Arrays.asList(validsList.split(VALUES_SEPARATOR)).contains(appId);
		}

		// Si hay un listado de aplicacion invalidas, comprobamos que la indicada no este entre ellas
		final String invalidsList = getProperty(PROP_DOCUMENT_MANAGER_PREFIX + docManager + PROP_DOCMANAGER_REQ_INVALID_SUFIX);
		if (invalidsList != null) {
			return !Arrays.asList(invalidsList.split(VALUES_SEPARATOR)).contains(appId);
		}

		// Si no se ha restringido el uso, se permite directamente
		return true;
	}

	/**
	 * Recupera el identificador del gestor para la compartici&oacute;n de sesiones, necesario
	 * para permitir el uso de varios nodos balanceados con el componente central.
	 * @return Instancia del gestor para la compartici&oacute;n de sesiones o {@code null}
	 * si no se ha podido recuperar o no se ha configurado.
	 */
	public static String getSessionsDao() {

		if (config == null) {
			try {
				loadConfig();
			} catch (final ConfigFilesException e) {
				LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				return null;
			}
		}

		return getProperty(PROP_SESSIONS_DAO);
	}

	/**
	 * Indica si se debe forzar la carga de sesiones compartidas. Esta opci&oacute;
	 * solo se debe habilitar si se sufren problemas de carga de las sesiones al
	 * realizarse un despliegue en alta disponibilidad.
	 * @return {@code true} si se les debe dar soporte, {@code false} en caso contrario.
	 */
	public static boolean isSessionSharingForced() {
		return Boolean.parseBoolean(getProperty(PROP_SESSIONS_FORCE_SHARING));
	}

	/**
	 * Recupera el identificador del gestor para la compartici&oacute;n de ficheros temporales,
	 * necesario para permitir el uso de varios nodos balanceados con el componente central.
	 * @return Instancia del gestor para la compartici&oacute;n de ficheros temporales o
	 * {@code null} si no se ha podido recuperar o no se ha configurado.
	 */
	public static String getTempDocumentsDao() {
		if (config == null) {
			try {
				loadConfig();
			} catch (final ConfigFilesException e) {
				LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				return null;
			}
		}

		return getProperty(PROP_TEMP_DOCUMENTS_DAO);
	}

	/**
	  * Recupera la URL de la parte p&uacute;blica del componente central.
	  * @return URL de la parte p&uacute;blica del componente central o {@code null}
	  * si no se ha podido recuperar o no se ha configurado.
	  */
	 public static String getPublicContextUrl() {

	 	if (config == null) {
	 		try {
	 			loadConfig();
	 		} catch (final ConfigFilesException e) {
	 			LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
	 			return null;
	 		}
	 	}

	 	return getProperty(PROP_FIRE_PUBLIC_URL);
	 }

	 /**
	  * Recupera el directorio en el que almacenar los ficheros de log.
	  * @return Directorio de los ficheros de log o {@code null} si no se configur&oacute;.
	  */
	 public static String getLogsDir() {

		 if (config == null) {
			 try {
				 loadConfig();
			 } catch (final ConfigFilesException e) {
				 LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				 return null;
			 }
		 }

		 return getProperty(PROP_LOGS_DIR);
	 }

	 /**
	  * Recupera la pol&iacute;tica de rotado del fichero de log.
	  * @return Politica de rotado o {@code null} si no se configur&oacute;.
	  */
	 public static String getLogsRollingPolicy() {
		 return getProperty(PROP_LOGS_ROLLING_POLICY);
	 }

	 /**
	  * Recupera el nivel general de log m&iacute;nimo que se debe mostrar.
	  * @return Nivel de log configurado o el nivel por defecto si no se configur&oacute; o
	  * se configur&oacute; un valor no valido.
	  */
	 public static String getLogsLevel() {
		 return getProperty(PROP_LOGS_LEVEL_GENERAL, DEFAULT_GENERAL_LOGS_LEVEL);
	 }

	 /**
	  * Recupera el nivel m&iacute;nimo de los logs de FIRe que se deben mostrar.
	  * @return Nivel de log configurado o el nivel general si no se configur&oacute;.
	  */
	 public static String getLogsLevelFire() {
		 return getProperty(PROP_LOGS_LEVEL_FIRE, DEFAULT_FIRE_LOGS_LEVEL);
	 }

	 /**
	  * Recupera el nivel m&iacute;nimo de los logs del n&uacute;cleo de firma que se deben mostrar.
	  * @return Nivel de log configurado o el nivel general si no se configur&oacute;.
	  */
	 public static String getLogsLevelAfirma() {
		 return getProperty(PROP_LOGS_LEVEL_AFIRMA, DEFAULT_AFIRMA_LOGS_LEVEL);
	 }

	 /**
	  * Recupera el nombre del atributo de la cabecera de las peticiones HTTP
	  * en el que se transmite el certificado para la autenticaci&oacute;n
	  * cliente SSL.
	 * @return Nombre del atributo configurado.
	 */
	public static String getHttpsCertAttributeHeader() {

		 if (config == null) {
			 try {
				 loadConfig();
			 } catch (final ConfigFilesException e) {
				 LOGGER.warning("No se puede cargar el fichero de configuracion del componente central: " + e); //$NON-NLS-1$
				 return null;
			 }
		 }
		 return getProperty(PROP_HTTP_CERT_ATTR, DEFAULT_HTTP_CERT_ATTR);
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
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 * @throws InvalidConfigurationException Cuando la propiedad era obligatoria y no se
	 * indic&oacute; encontr&oacute;.
	 */
	private static String getProperty(final String key, final boolean required) throws InvalidConfigurationException {
		return getProperty(key, null, required);
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
		try {
			return getProperty(key, defaultValue, false);
		} catch (final InvalidConfigurationException e) {
			// No puede producirse nunca
			return null;
		}
	}

	/**
	 * Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @param defaultValue Valor por defecto que devolver en caso de que la propiedad no exista
	 * o que se produzca un error al extraerla.
	 * @param required Establece si es obligatoria la existencia de la propiedad.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida.
	 * @throws InvalidConfigurationException Cuando la propiedad era obligatoria y no se
	 * indic&oacute; encontr&oacute;.
	 */
	private static String getProperty(final String key, final String defaultValue, final boolean required) throws InvalidConfigurationException {
		final String value = getDecipheredProperty(config, key, defaultValue);
		if (required && (value == null || value.isEmpty())) {
			throw new InvalidConfigurationException(key, CONFIG_FILE);
		}
		return value;
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
					LOGGER.log(Level.WARNING,
							String.format(
									"Ocurrio un error al descifrar un fragmento de la propiedad %1s. Se usara el valor por defecto proporcionado: %2", //$NON-NLS-1$
									key,
									defaultValue),
							e);
					value = null;
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
			mappedProperties.setProperty(k, mapProperties(prop.getProperty(k)));
		}
		return mappedProperties;
	}

	/**
	 * Mapea las propiedades del sistema que haya en el texto que se referencien de
	 * la forma ${propiedad} y las variables de entorno referenciadas como %{propiedad}.
	 * @param text Texto en el que se pueden encontrar las referencias a las propiedades.
	 * @return Text de entrada con las propiedades sustituidas por sus valores correspondientes.
	 * Si no se encuentra la propiedad definida, no se modificar&aacute;.
	 */
	private static String mapProperties(final String text) {

		if (text == null) {
			return null;
		}

		int pos = -1;
		int pos2 = 0;
		String mappedText = text;

		// Mapeamos las variables de sistema
		while ((pos = mappedText.indexOf(SYS_PROP_PREFIX, pos + 1)) > -1 && pos2 > -1) {
			pos2 = mappedText.indexOf(PROP_SUFIX, pos + SYS_PROP_PREFIX.length());
			if (pos2 > pos) {
				final String prop = mappedText.substring(pos + SYS_PROP_PREFIX.length(), pos2);
				final String value = System.getProperty(prop, null);
				if (value != null) {
					mappedText = mappedText.replace(SYS_PROP_PREFIX + prop + PROP_SUFIX, value);
				}
			}
		}

		// Mapeamos las variables de entorno
		pos = -1;
		pos2 = 0;
		while ((pos = mappedText.indexOf(ENV_PROP_PREFIX, pos + 1)) > -1 && pos2 > -1) {
			pos2 = mappedText.indexOf(PROP_SUFIX, pos + ENV_PROP_PREFIX.length());
			if (pos2 > pos) {
				final String prop = mappedText.substring(pos + ENV_PROP_PREFIX.length(), pos2);
				final String value = System.getenv(prop);
				if (value != null) {
					mappedText = mappedText.replace(ENV_PROP_PREFIX + prop + PROP_SUFIX, value);
				}
			}
		}

		return mappedText;
	}

	/**
	 * Recupera valor del par&aacute;metro que indica si se debe omitir la
	 * pantalla de selecci&oacute;n de certificados.
	 * @return Indica si se debe omitir la pantalla de selecci&oacute;n de certificados.
	 */
	public static boolean isSkipCertSelection(){
		return 	Boolean.parseBoolean(getProperty(PROP_SKIP_CERT_SELECTION, Boolean.FALSE.toString()));
	}

	public static int getDataBasePoolSize() {

		int poolSize;
		try {
			poolSize = Integer.parseInt(getProperty(PROP_DB_POOL_SIZE));
		}
		catch (final Exception e) {
			poolSize = -1;
		}

		if (poolSize < 1) {
			LOGGER.warning("No se ha indicado un tamano valido para el pool de base de datos. Se usara el por defecto: " + DEFAULT_DB_POOL_SIZE); //$NON-NLS-1$
			poolSize = DEFAULT_DB_POOL_SIZE;
		}

		return poolSize;
	}
}
