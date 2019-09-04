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
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.decipher.PropertyDecipher;

/** Manejador que gestiona la configuraci&oacute;n de la aplicaci&oacute;n. */
@WebListener
public final class ConfigManager implements ServletContextListener {

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

	private static final String PROP_DEBUG = "debug"; //$NON-NLS-1$

	private static final String PROP_UPGRADER_CLASSNAME = "signature.upgrader"; //$NON-NLS-1$

	private static final String DEFAULT_UPGRADER_CLASSNAME = "es.gob.fire.server.services.UpgraderAfirma"; //$NON-NLS-1$

	private static final String PROP_DB_DRIVER = "bbdd.driver"; //$NON-NLS-1$

	private static final String PROP_DB_CONNECTION = "bbdd.conn"; //$NON-NLS-1$

	private static final String PARAM_CIPHER_CLASS = "cipher.class"; //$NON-NLS-1$

	private static final String PROP_AFIRMA_ID = "afirma.appId"; //$NON-NLS-1$

	private static final String PROP_TEMP_DIR = "temp.dir"; //$NON-NLS-1$

	/** Propiedad utilizada para indicar los proveedores activos en el componente central. */
	private static final String PROP_PROVIDERS_LIST = "providers"; //$NON-NLS-1$

	/** Prefijo utilizado para las propiedades que determinan la clase principal de un proveedor. */
	private static final String PREFIX_PROP_PROVIDER = "provider."; //$NON-NLS-1$

	private static final String PROP_APP_ID = "default.appId"; //$NON-NLS-1$

	private static final String PROP_CERTIFICATE = "default.certificate"; //$NON-NLS-1$

	private static final String PROP_ALTERNATIVE_XMLDSIG = "signature.alternativeXmldsig"; //$NON-NLS-1$

	private static final String PROP_BATCH_MAX_DOCUMENTS = "batch.maxDocuments"; //$NON-NLS-1$

	private static final String PROP_FIRE_TEMP_TIMEOUT = "temp.fire.timeout"; //$NON-NLS-1$

	/** Segundos que, por defecto, tardan los ficheros temporales del proceso de firma de lote en caducar. */
	private static final int DEFAULT_FIRE_TEMP_TIMEOUT = 600; // 10 minutos

	private static final String PROP_CLIENTEAFIRMA_TEMP_TIMEOUT =  "temp.afirma.timeout"; //$NON-NLS-1$

	/** Segundos que, por defecto, tardan los ficheros temporales del cliente @firma en caducar. */
	private static final int DEFAULT_CLIENTEAFIRMA_TEMP_TIMEOUT = 600; // 10 minutos

	private static final String PROP_CLIENTEAFIRMA_FORCE_AUTOFIRMA = "clienteafirma.forceAutoFirma"; //$NON-NLS-1$

	private static final String PROP_CLIENTEAFIRMA_FORCE_NATIVE = "clienteafirma.forceNative"; //$NON-NLS-1$

	private static final String PROP_FIRE_PAGES_TITLE = "pages.title"; //$NON-NLS-1$

	private static final String PROP_FIRE_PAGES_LOGO_URL = "pages.logo"; //$NON-NLS-1$

	private static final String PROP_FIRE_PUBLIC_URL = "pages.public.url"; //$NON-NLS-1$

	private static final String PROP_DOCUMENT_MANAGER_PREFIX = "docmanager."; //$NON-NLS-1$

	private static final String PROP_SESSIONS_DAO = "sessions.dao"; //$NON-NLS-1$

	private static final String PROP_LOGS_DIR = "logs.dir"; //$NON-NLS-1$

	private static final String PROP_LOGS_ROLLING_POLICY = "logs.rollingPolicy"; //$NON-NLS-1$

	private static final String PROP_LOGS_LEVEL_FIRE = "logs.level.fire"; //$NON-NLS-1$

	private static final String PROP_LOGS_LEVEL_AFIRMA = "logs.level.afirma"; //$NON-NLS-1$

	private static final String PROP_LOGS_LEVEL_GENERAL = "logs.level"; //$NON-NLS-1$

	private static final String DEFAULT_LOGS_LEVEL = "WARNING"; //$NON-NLS-1$

	private static final String PROP_HTTP_CERT_ATTR = "http.cert.attr"; //$NON-NLS-1$

	private static final String DEFAULT_HTTP_CERT_ATTR = "x-clientcert"; //$NON-NLS-1$

	/** Configuraci&oacute;n de la pol&iacute;tica de volcado de datos estad&iacute;sticos*/
	private static final String PROP_STATISTICS_POLICY ="statistics.policy"; //$NON-NLS-1$

	/** Configuraci&oacute;n de la hora del volcado a base de datos si la pol&iacute;tica lo permite). */
	private static final String PROP_STATISTICS_DUMPTIME = "statistics.dumptime"; //$NON-NLS-1$

	/** Configuraci&oacute;n del directorio de volcado de datosestad&iacute;sticos. */
	private static final String PROP_STATISTICS_DIR = "statistics.dir"; //$NON-NLS-1$

	private static final String USE_TSP = "usetsp"; //$NON-NLS-1$
	private static final String PROP_CHECK_CERTIFICATE = "security.checkCertificate"; //$NON-NLS-1$
	private static final String PROP_CHECK_APPLICATION = "security.checkApplication"; //$NON-NLS-1$

	private static final String PROVIDER_LOCAL = "local"; //$NON-NLS-1$

	/** Cadena utilizada para separar valores dentro de una propiedad. */
	private static final String VALUES_SEPARATOR = ","; //$NON-NLS-1$

	private static final String PREFIX_CIPHERED_TEXT = "{@ciphered:"; //$NON-NLS-1$
	private static final String SUFIX_CIPHERED_TEXT = "}"; //$NON-NLS-1$

	/** N&uacute;mero que identifica cuando el valor de configuraci&oacute;n que indica que
	 * el n&uacute;mero de documentos no est&aacute; limitado. */
	public static final int UNLIMITED_NUM_DOCUMENTS = 0;

	/** Ruta del directorio por defecto para el guardado de temporales (directorio temporal del sistema). */
	private static final String DEFAULT_TMP_DIR;

	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String CONFIG_FILE = "config.properties"; //$NON-NLS-1$

	private static final String PATTERN_TIME = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$"; //$NON-NLS-1$

	private static final Properties CONFIG;

	private static PropertyDecipher decipherImpl = null;


	static {

		String tmpDir;
		try {
			tmpDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.warning(
				"No se ha podido determinar el directorio temporal a partir de la variable java 'java.io.tmpdir': "  + e //$NON-NLS-1$
			);
			try {
				tmpDir = File.createTempFile("tmp", null).getParentFile().getAbsolutePath(); //$NON-NLS-1$
			}
			catch (final Exception e1) {
				tmpDir = null;
				LOGGER.warning(
					"No se ha podido cargar un directorio temporal por defecto, se debera configurar expresamente en el fichero de propiedades: "  + e1 //$NON-NLS-1$
				);
			}
		}
		DEFAULT_TMP_DIR = tmpDir;

		try {
			CONFIG = ConfigFileLoader.loadConfigFile(CONFIG_FILE);
		}
		catch (final Exception e) {
			LOGGER.severe("No se pudo cargar el fichero de configuracion " + CONFIG_FILE); //$NON-NLS-1$
			throw new IllegalStateException(
				new ConfigFilesException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e) //$NON-NLS-1$
			);
		}

		if (CONFIG.containsKey(PARAM_CIPHER_CLASS)) {
			final String decipherClassname = CONFIG.getProperty(PARAM_CIPHER_CLASS);
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

	/** Indica si el programa est&aacute; configurado en modo depuraci&oacute;n.
	 * @return <code>true</code> si el programa est&aacute; configurado en modo depuraci&oacute;n,
	 *         <code>false</code> en caso contrario. */
	public static boolean isDebug() {
		return Boolean.parseBoolean(CONFIG.getProperty(PROP_DEBUG));
	}

	/** Obtiene el nombre de la clase encargada de la mejora de firmas.
	 * @return Nombre de la clase encargada de la mejora de firmas.
	 *         Si no hay ninguna definida en la configuraci&oacute;n, devuelve la clase por defecto. */
	public static String getUpgraderClassName() {
		return CONFIG.getProperty(PROP_UPGRADER_CLASSNAME, DEFAULT_UPGRADER_CLASSNAME);
	}

	/** Devuelve el listado de nombres de los proveedores configurados.
	 * En caso de que no se haya definido ninguno, este listado
	 * estar&aacute; vac&iacute;o.
	 * @return Listado de proveedores configurados. */
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

	/** Recupera el nombre de la clase de conexi&oacute;n de un proveedor.
	 * @param name Nombre del proveedor.
	 * @return Clase de conexi&oacute;n del proveedor. */
	public static String getProviderClass(final String name) {
		return getProperty(PREFIX_PROP_PROVIDER + name);
	}

	/**
	 * Recupera la clase del driver JDBC para el acceso a la base de datos.
	 * @return Clase de conexi&oacute;n.
	 */
	public static String getJdbcDriverString() {
		final String driver = getProperty(PROP_DB_DRIVER);
		if (driver == null) {
			LOGGER.warning(
					String.format("No se ha declarado la clase del driver JDBC de la BD en el fichero de configuracion. " //$NON-NLS-1$
							+ "Asegurese de habilitar las propiedades %1s y %2s como alternativa", PROP_APP_ID, PROP_CERTIFICATE)); //$NON-NLS-1$
		}
		return driver;
	}

	/**
	 * Recupera la cadena de conexi&oacute;n con la base de datos.
	 * @return Cadena de conexi&oacute;n con la base de datos.
	 */
	public static String getDataBaseConnectionString() {
		final String dbConnection = getProperty(PROP_DB_CONNECTION);
		if (dbConnection == null) {
			LOGGER.warning(
					String.format("No se ha declarado la cadena de conexion a la BD en el fichero de configuracion. " //$NON-NLS-1$
							+ "Asegurese de habilitar las propiedades %1s y %2s como alternativa", PROP_APP_ID, PROP_CERTIFICATE)); //$NON-NLS-1$
		}
		return dbConnection;
	}

	/**
	 * Recupera la cadena de conexi&oacute;n con la base de datos.
	 * @return Cadena de conexi&oacute;n con la base de datos.
	 */
	public static String getAfirmaAplicationId() {
		return getProperty(PROP_AFIRMA_ID);
	}

	/** Indica si la compatibilidad con XALAN/XERCES est&aacute; habilitada.
	 * @return {@code true} si la compatibilidad con XALAN/XERCES est&aacute; habilitada,
	 * {@code false} en caso contrario. */
	public static boolean isAlternativeXmlDSigActive() {
		return Boolean.parseBoolean(getProperty(PROP_ALTERNATIVE_XMLDSIG));
	}

	/** Recupera el numero m&aacute;ximo de documentos que se pueden agregar a un lote de firma.
	 * Si se devuelve el valor {@code #UNLIMITED_NUM_DOCUMENTS} se debe considerar que
	 * no hay l&iacute;mite al n&uacute;mero de documentos de un lote.
	 * @return N&uacute;mero de documentos que se pueden agregar a un lote. */
	public static int getBatchMaxDocuments() {
		try {
			return Integer.parseInt(getProperty(PROP_BATCH_MAX_DOCUMENTS));
		}
		catch (final Exception e) {
			LOGGER.warning(
				"Se encontro un valor invalido para la propiedad '" + //$NON-NLS-1$
					PROP_BATCH_MAX_DOCUMENTS +
						"' del fichero de configuracion. No se establecera limite al numero de ficheros: " + e); //$NON-NLS-1$
			return UNLIMITED_NUM_DOCUMENTS;
		}
	}

	/** Lanza una excepci&oacute;n en caso de que no encuentre el fichero de configuraci&oacute;n.
	 * @throws ConfigFilesException Si no encuentra el fichero config.properties. */
	private static void checkConfiguration() throws ConfigFilesException {

		final ProviderElement[] providers = getProviders();
		if (providers == null) {
			LOGGER.severe(
				"Debe declararse al menos un proveedor mediante la propiedad " + PROP_PROVIDERS_LIST //$NON-NLS-1$
			);
			throw new ConfigFilesException(
				"Debe declararse al menos un proveedor con la propiedad " + PROP_PROVIDERS_LIST, CONFIG_FILE //$NON-NLS-1$
			);
		}

		try {
			checkProviders(providers);
		}
		catch (final Exception e) {
			LOGGER.severe("Error en la configuracion de los proveedores: " + e.getMessage()); //$NON-NLS-1$
			throw new ConfigFilesException("Error en la configuracion de los proveedores: " + e.getMessage(), CONFIG_FILE, e); //$NON-NLS-1$
		}

		if (isCheckApplicationNeeded()) {
			if (getDataBaseConnectionString() == null && getAppId() == null) {
				LOGGER.severe("No se ha configurado el acceso a la base de datos ni el campo " + PROP_APP_ID + " para la verificacion del identificador de aplicacion"); //$NON-NLS-1$ //$NON-NLS-2$
				throw new ConfigFilesException("No se ha configurado el acceso a la base de datos ni el campo " + PROP_APP_ID + " para la verificacion del identificador de aplicacion", CONFIG_FILE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (isCheckCertificateNeeded()) {
			if (getDataBaseConnectionString() == null && getCert() == null) {
				LOGGER.severe("No se ha configurado el acceso a la base de datos ni el campo " + PROP_CERTIFICATE + " para la verificacion del certificado de auteticacion"); //$NON-NLS-1$ //$NON-NLS-2$
				throw new ConfigFilesException("No se ha configurado el acceso a la base de datos ni el campo " + PROP_CERTIFICATE + " para la verificacion del certificado de auteticacion", CONFIG_FILE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

	}

	/** Devuelve el certificado del fichero de propiedades en caso de que no se encuentre la
	 * cadena conexi&oacute;n a la base de datos.
	 * @return el certificado. */
	public static String getCert(){
		return getProperty(PROP_CERTIFICATE);
	}

	/** Devuelve el identificador de la aplicaci&oacute;n en caso de que no se encuentre la
	 * cadena conexi&oacute;n a la base de datos.
	 * @return identificador de la aplicaci&oacute;n. */
	public static String getAppId(){
		return getProperty(PROP_APP_ID);
	}

	/** Devuelve el directorio configurado para el guardado de temporales.
	 * @return Ruta del directorio temporal o, si no se configuro, el directorio
	 * temporal del sistema. En caso de error, devolver&aacute; {@code null}. */
	public static String getTempDir() {
		return getProperty(PROP_TEMP_DIR, DEFAULT_TMP_DIR);
	}


	/** Devuelve el identificador num&eacute;rico de la pol&iacute;tica de firma configurada.
	 * En caso de error, devuelve -1.
	 * @return Dato num&eacute;rico. */
	public static int getStatisticsPolicy() {
		int policy;
		try {
			policy = Integer.parseInt(getProperty(PROP_STATISTICS_POLICY));
		}
		catch (final NumberFormatException e) {
			LOGGER.warning("No se ha podido leer el identificador de politica (" + getProperty(PROP_STATISTICS_POLICY) + "):" + e); //$NON-NLS-1$ //$NON-NLS-2$
			policy = -1;
		}
		return policy;
	}

	/** Devuelve la hora a la que deber&iacute;n volcarse los datos estad&iacute;sticos a base de datos. En caso de no
	 * encontrarse configurada una hora con el formato hh:mm:ss se devolver&aacute; 00:00:00.
	 * @return Hora con formato <i>hh:mm:ss</i>. */
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
	public static String getStatisticsDir() {
		 return getProperty(PROP_STATISTICS_DIR);
	}


	/**
	 * Obtiene el directorio temporal para el almacenamiento de temporales del Cliente @firma.
	 * Este ser&aacute; un subdirectorio dentro del directorio temporal configurado o, en su defecto,
	 * del firectorio temporal del sistema.
	 * @return Directorio temporal.
	 * @throws IllegalStateException Cuando no se encuentra o no se tiene permisos sobre el directorio temporal.
	 */
	public static File getAfirmaTempDir() {

		final File baseTmpDir = new File(getTempDir());
		if (!baseTmpDir.exists() || !baseTmpDir.canRead() || !baseTmpDir.canWrite()) {
			LOGGER.warning("El directorio temporal base no existe o no tiene permisos de lectura/escritura"); //$NON-NLS-1$
			throw new IllegalStateException("No se ha podido definir un directorio temporal"); //$NON-NLS-1$
		}

		final File tmpDir = new File(baseTmpDir, "afirma"); //$NON-NLS-1$
		if (!tmpDir.exists()) {
			tmpDir.mkdir();
		}
		if (!tmpDir.exists() || !tmpDir.canRead() || !tmpDir.canWrite()) {
			throw new IllegalStateException("No se ha podido definir un directorio temporal para el intercambio del Cliente @firma"); //$NON-NLS-1$
		}
		return tmpDir;
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

	private static void checkProviders(final ProviderElement[] providers) {
		final List<String> wrongProviders = new ArrayList<>(providers.length);
		for (final ProviderElement provider : providers) {
			final String providerName = provider.getName();
			if (!PROVIDER_LOCAL.equalsIgnoreCase(providerName) &&
					!CONFIG.containsKey(PREFIX_PROP_PROVIDER + providerName)) {
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
			throw new NullPointerException("No se ha definido la clase conectora de los proveedores: " + errorMsg.toString()); //$NON-NLS-1$
		}
	}

	/**
	 * Recupera valor del par&aacute;metro de sello de tiempo.
	 * @return El par&aacute;metro usetsp.
	 */
	public static boolean existUseTsp(){
		return 	Boolean.parseBoolean(getProperty(USE_TSP, Boolean.FALSE.toString()));
	}

	/** Recupera el tiempo en milisegundos que debe transcurrir antes de considerar caducados los
	 * ficheros temporales almacenados durante un proceso de firma de lote. Si no se encuentra
	 * configurado un valor, se usara el valor por defecto.
	 * @return N&uacute;mero de milisegundos que como m&iacute;nimo se almacenar&aacute;n los
	 * ficheros temporales. */
	public static long getTempsTimeout() {
		try {
			return Long.parseLong(getProperty(PROP_FIRE_TEMP_TIMEOUT, Integer.toString(DEFAULT_FIRE_TEMP_TIMEOUT)))
					* 1000;
		}
		catch (final Exception e) {
			LOGGER.warning(
				"Tiempo de expiracion invalido en la propiedad '" + PROP_FIRE_TEMP_TIMEOUT + //$NON-NLS-1$
					"' del fichero de configuracion. Se usaran " + DEFAULT_FIRE_TEMP_TIMEOUT + //$NON-NLS-1$
						"segundos: " + e); //$NON-NLS-1$
			return DEFAULT_FIRE_TEMP_TIMEOUT * 1000;
		}
	}

	/** Recupera el tiempo en milisegundos que puede almacenarse un fichero de intercambio del Cliente Afirma
	 * antes de considerarse caducado. El tipo se carga de la configuraci&oacute;n en donde se indica
	 * en segundo.
	 * @return Tiempo m&aacute;ximo en milisegundos que puede tardarse en recoger un fichero antes de que
	 * caduque. */
	public static long getAfirmaTempsTimeout() {
		try {
			return Long.parseLong(getProperty(PROP_CLIENTEAFIRMA_TEMP_TIMEOUT, Integer.toString(DEFAULT_CLIENTEAFIRMA_TEMP_TIMEOUT)))
					* 1000;
		}
		catch (final Exception e) {
			LOGGER.warning(
				"Tiempo de expiracion invalido en la propiedad '" + PROP_CLIENTEAFIRMA_TEMP_TIMEOUT + //$NON-NLS-1$
					"' del fichero de configuracion. Se usaran " + DEFAULT_CLIENTEAFIRMA_TEMP_TIMEOUT + //$NON-NLS-1$
						"segundos: " + e //$NON-NLS-1$
			);
			return DEFAULT_CLIENTEAFIRMA_TEMP_TIMEOUT * 1000;
		}
	}

	/**
	 * Recupera de la configuraci&oacute;n si debe forzarse el uso de una aplicaci&oacute;n
	 * externa distinta al MiniApplet para la firma.
	 * @return {@code true} si debe forzarse el uso de una aplicaci&oacute;n externa,
	 * {@code false} en caso contrario.
	 */
	public static boolean getClienteAfirmaForceAutoFirma() {
			return Boolean.parseBoolean(getProperty(PROP_CLIENTEAFIRMA_FORCE_AUTOFIRMA));
	}

	/** Recupera de la configuraci&oacute;n si debe forzarse el uso de la version nativa de AutoFirma,
	 * no la version WebStart.
	 * @return {@code false} si se configur&oacute; el uso de AutoFirma WebStart (valor "false"),
	 *         {@code true} en caso contrario. */
	public static boolean getClienteAfirmaForceNative() {
		return !Boolean.FALSE.toString().equalsIgnoreCase(getProperty(PROP_CLIENTEAFIRMA_FORCE_NATIVE));
	}

	/** Recupera el t&iacute;tulo a asignar a las p&aacute;ginas web del componente central.
	 * @return T&iacute;tulo configurado para las paginas o cadena vac&iacute;a si no se
	 *         especific&oacute; uno. */
	public static String getPagesTitle() {
		return getProperty(PROP_FIRE_PAGES_TITLE, ""); //$NON-NLS-1$
	}

	/** Recupera la URL configurada de la imagen de logo que se debe mostrar en
	 * las p&aacute;ginas web del componente central.
	 * @return URL completa de la imagen de logo o cadena vac&iacute;a si no se ha configurado. */
	public static String getPagesLogoUrl() {
		return getProperty(PROP_FIRE_PAGES_LOGO_URL, ""); //$NON-NLS-1$
	}

	/** Recupera la clase DocumentManager con la que obtener los datos a firmar y
	 * guardar la firma.
	 * @param docManager Identificador del DocumentManager.
	 * @return Nombre cualificado de la clase. */
	public static String getDocumentManagerClassName(final String docManager) {
		return getProperty(PROP_DOCUMENT_MANAGER_PREFIX + docManager, ""); //$NON-NLS-1$
	}

	/** Recupera el identificador del gestor para la compartici&oacute;n de sesiones, necesario
	 * para permitir el uso de varios nodos balanceados con el componente central.
	 * @return Instancia del gestor para la compartici&oacute;n de sesiones o {@code null}
	 * si no se ha podido recuperar o no se ha configurado. */
	public static String getSessionsDao() {
		return getProperty(PROP_SESSIONS_DAO);
	}

	/** Recupera la URL de la parte p&uacute;blica del componente central.
	  * @return URL de la parte p&uacute;blica del componente central o {@code null}
	  * si no se ha podido recuperar o no se ha configurado. */
	 public static String getPublicContextUrl() {
	 	return getProperty(PROP_FIRE_PUBLIC_URL);
	 }

	 /** Recupera el directorio en el que almacenar los ficheros de log.
	  * @return Directorio de los ficheros de log o {@code null} si no se configur&oacute;. */
	 public static String getLogsDir() {
		 return getProperty(PROP_LOGS_DIR);
	 }

	 /** Recupera la pol&iacute;tica de rotado del fichero de log.
	  * @return Politica de rotado o {@code null} si no se configur&oacute;. */
	 public static String getLogsRollingPolicy() {
		 return getProperty(PROP_LOGS_ROLLING_POLICY);
	 }

	 /** Recupera el nivel general de log m&iacute;nimo que se debe mostrar.
	  * @return Nivel de log configurado o el nivel por defecto si no se configur&oacute; o
	  * se configur&oacute; un valor no valido. */
	 public static String getLogsLevel() {
		 return getProperty(PROP_LOGS_LEVEL_GENERAL, DEFAULT_LOGS_LEVEL);
	 }

	 /** Recupera el nivel m&iacute;nimo de los logs de FIRe que se deben mostrar.
	  * @return Nivel de log configurado o el nivel general si no se configur&oacute;. */
	 public static String getLogsLevelFire() {
		 return getProperty(PROP_LOGS_LEVEL_FIRE, getLogsLevel());
	 }

	 /** Recupera el nivel m&iacute;nimo de los logs del n&uacute;cleo de firma que se deben mostrar.
	  * @return Nivel de log configurado o el nivel general si no se configur&oacute;. */
	 public static String getLogsLevelAfirma() {
		 return getProperty(PROP_LOGS_LEVEL_AFIRMA, getLogsLevel());
	 }

	 /** Recupera el nombre del atributo de la cabecera de las peticiones HTTP
	  * en el que se transmite el certificado para la autenticaci&oacute;n
	  * cliente SSL.
	 * @return Nombre del atributo configurado. */
	public static String getHttpsCertAttributeHeader() {
		 return getProperty(PROP_HTTP_CERT_ATTR, DEFAULT_HTTP_CERT_ATTR);
	 }

	/** Indica si se ha configurado un objeto para el descifrado de propiedades
	 * en los ficheros de configuraci&oacute;n.
	 * @return {@code true} si se ha definido el objeto de descifrado. {@code false}
	 * en caso contrario. */
	public static boolean hasDecipher() {
		return decipherImpl != null;
	}

	/** Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida. */
	private static String getProperty(final String key) {
		return getProperty(key, null);
	}

	/** Recupera una propiedad del fichero de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param key Clave de la propiedad.
	 * @param defaultValue Valor por defecto que devolver en caso de que la propiedad no exista
	 *                     o que se produzca un error al extraerla.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida. */
	private static String getProperty(final String key, final String defaultValue) {
		return getDecipheredProperty(CONFIG, key, defaultValue);
	}

	/** Recupera una propiedad de un objeto de configuraci&oacute;n y devuelve su
	 * valor habi&eacute;ndolo descifrado si era necesario.
	 * @param properties Objeto de configuraci&oacute;n del que obtener el valor.
	 * @param key Clave de la propiedad.
	 * @param defaultValue Valor por defecto que devolver en caso de que la propiedad no exista
	 *                     o que se produzca un error al extraerla.
	 * @return Valor descifrado de la propiedad o {@code null} si la propiedad no estaba definida. */
	public static String getDecipheredProperty(final Properties properties, final String key, final String defaultValue) {
		String value = properties.getProperty(key);
		if (decipherImpl != null && value != null) {
			while (isCiphered(value)) {
				try {
					value = decipherFragment(value);
				}
				catch (final IOException e) {
					if (defaultValue != null) {
						LOGGER.log(
							Level.WARNING,
							String.format(
								"Ocurrio un error al descifrar un fragmento de la propiedad %1s. Se usara el valor %2s", //$NON-NLS-1$
								key,
								defaultValue
							),
							e
						);
						value = defaultValue;
					}
					else {
						LOGGER.log(
							Level.WARNING,
							String.format(
								"Ocurrio un error al descifrar un fragmento de la propiedad %1s. Se usara el valor predefinido", //$NON-NLS-1$
								key
							)
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

	/** Comprueba si una cadena de texto tiene alg&uacute;n fragmento cifrado.
	 * @param text Cadena de texto.
	 * @return {@code true} si la cadena contiene fragmentos cifrados. {@code false},
	 *         en caso contrario. */
	private static boolean isCiphered(final String text) {

		if (text == null) {
			return false;
		}

		final int idx = text.indexOf(PREFIX_CIPHERED_TEXT);
		return idx != -1 && text.indexOf(SUFIX_CIPHERED_TEXT, idx + PREFIX_CIPHERED_TEXT.length()) != -1;
	}

	/** Texto cifrado del que descifrar un fragmento.
	 * @param text Texto con los marcadores que se&ntilde;alan que hay un fragmento cifrado.
	 * @return  Texto con un framento descifrado.
	 * @throws IOException Cuando ocurre un error al descifrar los datos. */
	private static String decipherFragment(final String text) throws IOException {
		final int idx1 = text.indexOf(PREFIX_CIPHERED_TEXT);
		final int idx2 = text.indexOf(SUFIX_CIPHERED_TEXT, idx1);
		final String base64Text = text.substring(idx1 + PREFIX_CIPHERED_TEXT.length(), idx2).trim();

		return text.substring(0, idx1) +
			decipherImpl.decipher(Base64.decode(base64Text)) +
				text.substring(idx2 + SUFIX_CIPHERED_TEXT.length());
	}

    @Override
	public void contextInitialized(final ServletContextEvent event) {
    	try {
			ConfigManager.checkConfiguration();
		}
    	catch (final ConfigFilesException e) {
			throw new IllegalStateException(e);
		}
    	LOGGER.info("Cargada correctamente la configuracion de FIRe"); //$NON-NLS-1$
    }

}
