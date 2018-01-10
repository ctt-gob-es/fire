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
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manejador que gestiona la configuraci&oacute;n de la aplicaci&oacute;n.
 */
public class ConfigManager {

	private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

	private static final String PROP_DB_DRIVER = "bbdd.driver"; //$NON-NLS-1$

	private static final String PROP_DB_CONNECTION = "bbdd.conn"; //$NON-NLS-1$

	private static final String PROP_ANALYTICS_ID = "google.trackingId"; //$NON-NLS-1$

	private static final String PROP_AFIRMA_ID = "afirma.appId"; //$NON-NLS-1$

	private static final String PROP_TEMP_DIR = "temp.dir"; //$NON-NLS-1$

	private static final String PROP_BACKEND = "backendClassName"; //$NON-NLS-1$

	private static final String PROP_CLAVEFIRMA_PROVIDER_NAME = "clavefirma.providerName"; //$NON-NLS-1$

	private static final String PROP_TEST_ENDPOINT = "test.endpoint"; //$NON-NLS-1$

	private static final String PROP_TEST_SSL_KS = "test.ssl.keystore"; //$NON-NLS-1$

	private static final String PROP_TEST_SSL_KS_TYPE = "test.ssl.keystoreType"; //$NON-NLS-1$

	private static final String PROP_TEST_SSL_KS_PASS = "test.ssl.keystorePass"; //$NON-NLS-1$

	private static final String PROP_TEST_SSL_TS = "test.ssl.truststore"; //$NON-NLS-1$

	private static final String PROP_TEST_SSL_TS_TYPE = "test.ssl.truststoreType"; //$NON-NLS-1$

	private static final String PROP_TEST_SSL_TS_PASS = "test.ssl.truststorePass"; //$NON-NLS-1$

	private static final String PROP_APP_ID = "default.appId"; //$NON-NLS-1$

	private static final String PROP_CERTIFICATE = "default.certificate"; //$NON-NLS-1$

	private static final String PROP_ALTERNATIVE_XMLDSIG = "signature.alternativeXmldsig"; //$NON-NLS-1$

	private static final String PROP_BATCH_MAX_DOCUMENTS = "batch.maxDocuments"; //$NON-NLS-1$

	private static final String PROP_CLAVEFIRMA_TEMP_TIMEOUT = "temp.clavefirma.timeout"; //$NON-NLS-1$

	/** Segundos que, por defecto, tardan los ficheros temporales del proceso de firma de lote en caducar. */
	private static final int DEFAULT_CLAVEFIRMA_TEMP_TIMEOUT = 600; // 10 minutos

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

	private static final String USE_TSP = "usetsp"; //$NON-NLS-1$
	private static final String USE_BBDD = "usebbdd"; //$NON-NLS-1$
	private static final String PROP_CHECK_CERTIFICATE = "security.checkCertificate"; //$NON-NLS-1$
	private static final String PROP_CHECK_APPLICATION = "security.checkApplication"; //$NON-NLS-1$

	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String CONFIG_FILE = "config.properties"; //$NON-NLS-1$

	private static Properties config = null;

	/** N&uacute;mero que identifica cuando el valor de configuraci&oacute;n que indica que
	 * el n&uacute;mero de documentos no est&aacute; limitado. */
	public static final int UNLIMITED_NUM_DOCUMENTS = 0;

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
	 * @throws ConfigFilesException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	private static void loadConfig() throws  ConfigFilesException {

		if (config == null) {
			try {
				config = ConfigFileLoader.loadConfigFile(CONFIG_FILE);
			}
			catch (final Exception e) {
				LOGGER.severe("No se pudo cargar el fichero de configuracion " + CONFIG_FILE); //$NON-NLS-1$
				throw new ConfigFilesException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, CONFIG_FILE, e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Recupera el tracking Id de Google Analytics.
	 * @return Tracking Id de Google Analytics.
	 */
	public static String getGoogleAnalyticsTrackingId() {
		return config.getProperty(PROP_ANALYTICS_ID);
	}

	/**
	 * Recupera la clase del driver JDBC para el acceso a la base de datos.
	 * @return Clase de conexi&oacute;n.
	 */
	public static String getJdbcDriverString() {
		return config.getProperty(PROP_DB_DRIVER);
	}

	/**
	 * Recupera la cadena de conexi&oacute;n con la base de datos.
	 * @return Cadena de conexi&oacute;n con la base de datos.
	 */
	public static String getDataBaseConnectionString() {
		return config.getProperty(PROP_DB_CONNECTION);
	}

	/**
	 * Recupera la cadena de conexi&oacute;n con la base de datos.
	 * @return Cadena de conexi&oacute;n con la base de datos.
	 */
	public static String getAfirmaAplicationId() {
		return config.getProperty(PROP_AFIRMA_ID);
	}

	/**
	 * Indica si la compatibilidad con XALAN/XERCES est&aacute; habilitada.
	 * @return {@code true} si la compatibilidad con XALAN/XERCES est&aacute; habilitada,
	 * {@code false} en caso contrario.
	 */
	public static boolean isAlternativeXmlDSigActive() {
		return Boolean.parseBoolean(config.getProperty(PROP_ALTERNATIVE_XMLDSIG));
	}

	/**
	 * Recupera el numero maximo de documentos que se pueden agregar a un lote de firma.
	 * Si se devuelve el valor {@code #UNLIMITED_NUM_DOCUMENTS} se debe considerar que
	 * no hay l&iacute;mite al n&uacute;mero de documentos de un lote.
	 * @return N&uacute;mero de documentos que se pueden agregar a un lote.
	 */
	public static int getBatchMaxDocuments() {
		try {
			return Integer.parseInt(config.getProperty(PROP_BATCH_MAX_DOCUMENTS));
		}
		catch (final Exception e) {
			LOGGER.warning("Se encontro un valor invalido para la propiedad '" + //$NON-NLS-1$
					PROP_BATCH_MAX_DOCUMENTS +
					"' del fichero de configuracion. No se establecera limite al numero de ficheros."); //$NON-NLS-1$
			return UNLIMITED_NUM_DOCUMENTS;
		}
	}

	/**
	 * Recupera el tiempo que debe transcurrir antes de considerar caducados los ficheros
	 * temporales almacenados durante un proceso de firma de lote. Si no se encuentra
	 * configurado un valor, se usara el valor por defecto.
	 * @return N&uacute;mero de milisegundos que como m&iacute;nimo se almacenar&aacute;n los
	 * ficheros temporales.
	 */
	public static int getTempsTimeout() {
		try {
			return Integer.parseInt(config.getProperty(PROP_CLAVEFIRMA_TEMP_TIMEOUT, Integer.toString(DEFAULT_CLAVEFIRMA_TEMP_TIMEOUT))) * 1000;
		}
		catch (final Exception e) {
			LOGGER.warning("Se encontro un valor invalido para la propiedad '" + //$NON-NLS-1$
					PROP_CLAVEFIRMA_TEMP_TIMEOUT +
					"' del fichero de configuracion"); //$NON-NLS-1$
			return DEFAULT_CLAVEFIRMA_TEMP_TIMEOUT * 1000;
		}
	}

	/**
	 * Recupera el servicio de back end de la aplicaci&oacute;n.
	 * @return Servicio de back end.
	 */
	public static String getBackEndService(){
		return config.getProperty(PROP_BACKEND);
	}

	/**
	 * Recupera el servicio de back end de la aplicaci&oacute;n.
	 * @param defaultService URL del servicio por defecto.
	 * @return Servicio de back end.
	 */
	public static String getBackEndService(final String defaultService){
		return config.getProperty(PROP_BACKEND, defaultService);
	}

	/**
	 * Lanza una excepci&oacute;n en caso de que no encuentre el fichero de configuraci&oacute;n.
	 * @throws ConfigFilesException Si no encuentra el fichero login.properties.
	 */
	public static void checkInitialized() throws ConfigFilesException {

		loadConfig();

		if (config == null) {
			LOGGER.severe("No se ha encontrado el fichero de configuracion de la conexion"); //$NON-NLS-1$
			throw new ConfigFilesException("No se ha encontrado el fichero de configuracion de la conexion", CONFIG_FILE); //$NON-NLS-1$
		}

		if (getBackEndService() == null ) {
			LOGGER.severe("El campo " + PROP_BACKEND + " es obligatorio"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new ConfigFilesException("El campo " + PROP_BACKEND + " es obligatorio", CONFIG_FILE); //$NON-NLS-1$ //$NON-NLS-2$
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

	/**
	 * Devuelve el certificado del fichero de propiedades en caso de que no se encuentre la cadena conexi&oacute;n a la base de datos
	 * @return el certificado.
	 */
	public static String getCert(){
		return config.getProperty(PROP_CERTIFICATE);
	}

	/**
	 * Devuelve el identificador de la aplicaci&oacute;n en caso de que se encuentre la cadena conexi&oacute;n a la base de datos
	 * @return identificador de la aplicaci&oacute;n.
	 */
	public static String getAppId(){
		return config.getProperty(PROP_APP_ID);
	}

	/**
	 * Devuelve el identificador de proveedor con el que se autentica el componente
	 * central frente a Cl@ve Firma.
	 * @return Identificador de proveedor.
	 */
	public static String getClaveFirmaProviderName() {
		return config.getProperty(PROP_CLAVEFIRMA_PROVIDER_NAME);
	}

	/**
	 * Devuelve la URL base del servicio de pruebas.
	 * @return URL base del servicio de pruebas.
	 */
	public static String getTestServiceUrlBase(){
		return config.getProperty(PROP_TEST_ENDPOINT);
	}

	/**
	 * Devuelve la ruta del almacen de claves para la autenticacion SSL
	 * contra el servicio remoto de test.
	 * @return Ruta del almac&eacute;n de claves.
	 */
	public static String getTestSslKeyStore() {
		return config.getProperty(PROP_TEST_SSL_KS);
	}

	/**
	 * Devuelve el tipo del almacen de claves para la autenticacion SSL
	 * contra el servicio remoto de test.
	 * @return Tipo del almac&eacute;n de claves.
	 */
	public static String getTestSslKeyStoreType() {
		return config.getProperty(PROP_TEST_SSL_KS_TYPE);
	}

	/**
	 * Devuelve la contrase&ntilde;a del almacen de claves para la
	 * autenticacion SSL contra el servicio remoto de test.
	 * @return Contrase&ntilde;a del almac&eacute;n de claves.
	 */
	public static String getTestSslKeyStorePass() {
		return config.getProperty(PROP_TEST_SSL_KS_PASS);
	}

	/**
	 * Devuelve la ruta del almacen de certificados de confianza
	 * para la autenticacion SSL contra el servicio remoto de test.
	 * @return Ruta del almac&eacute;n de confianza.
	 */
	public static String getTestSslTrustStore() {
		return config.getProperty(PROP_TEST_SSL_TS);
	}

	/**
	 * Devuelve el tipo del almacen de claves para la autenticacion SSL
	 * contra el servicio remoto de test.
	 * @return Tipo del almac&eacute;n de claves.
	 */
	public static String getTestSslTrustStoreType() {
		return config.getProperty(PROP_TEST_SSL_TS_TYPE);
	}

	/**
	 * Devuelve la contrase&ntilde;a del almacen de claves para la
	 * autenticacion SSL contra el servicio remoto de test.
	 * @return Contrase&ntilde;a del almac&eacute;n de claves.
	 */
	public static String getTestSslTrustStorePass() {
		return config.getProperty(PROP_TEST_SSL_TS_PASS);
	}

	/**
	 * Devuelve el directorio configurado para el guardado de temporales.
	 * @return Ruta del directorio temporal o, si no se configuro, el directorio
	 * temporal del sistema. En caso de error, devolver&aacute; {@code null}.
	 */
	public static String getTempDir() {
		return config.getProperty(PROP_TEMP_DIR, DEFAULT_TMP_DIR);
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
		return 	Boolean.parseBoolean(config.getProperty(PROP_CHECK_CERTIFICATE, Boolean.TRUE.toString()));
	}

	/**
	 * Recupera si se debe realizar una validaci&oacute;n de la aplicaci&oacute;n en base de datos.
	 * @return El valor del par&aacute;metro security.checkApplication.
	 */
	public static boolean isCheckApplicationNeeded() {
		return 	Boolean.parseBoolean(config.getProperty(PROP_CHECK_APPLICATION, Boolean.TRUE.toString()));
	}

	/**
	 * Devuelve el fichero de configuraci&oacute;n.
	 * @return El fichero de configuraci&oacute;n.
	 */
	public static Properties getPropertyFile(){
		return config;
	}

	/**
	 * Recupera valor del par&aacute;metro de sello de tiempo.
	 * @return El par&aacute;metro usetsp.
	 */
	public static boolean existUseTsp(){
		return 	Boolean.parseBoolean(config.getProperty(USE_TSP, Boolean.FALSE.toString()));
	}

	/**
	 * Recupera si el log se guarda en base de datos
	 * @return El par&aacute;metro usebbdd.
	 */
	public static boolean isUsedLoggingBd(){
		return 	Boolean.parseBoolean(config.getProperty(USE_BBDD, Boolean.FALSE.toString()));
	}

	/**
	 * Recupera el tiempo en segundos que puede almacenarse un fichero de intercambio del Cliente Afirma
	 * antes de considerarse caducado.
	 * @return Tiempo m&aacute;ximo en segundos que puede tardarse en recoger un fichero antes de que
	 * caduque.
	 */
	public static int getAfirmaTempsTimeout() {
		try {
			return config.containsKey(PROP_CLIENTEAFIRMA_TEMP_TIMEOUT) ?
					Integer.parseInt(config.getProperty(PROP_CLIENTEAFIRMA_TEMP_TIMEOUT)) : DEFAULT_CLIENTEAFIRMA_TEMP_TIMEOUT;
		} catch (final Exception e) {
			LOGGER.warning("Tiempo de expiracion invalido en el fichero de configuracion, se usara " + DEFAULT_CLIENTEAFIRMA_TEMP_TIMEOUT); //$NON-NLS-1$
			return DEFAULT_CLIENTEAFIRMA_TEMP_TIMEOUT;
		}
	}

	/**
	 * Recupera de la configuraci&oacute;n si debe forzarse el uso de una aplicaci&oacute;n
	 * externa distinta al MiniApplet para la firma.
	 * @return {@code true} si debe forzarse el uso de una aplicaci&oacute;n externa,
	 * {@code false} en caso contrario.
	 */
	public static boolean getClienteAfirmaForceAutoFirma() {
			return Boolean.parseBoolean(config.getProperty(PROP_CLIENTEAFIRMA_FORCE_AUTOFIRMA));
	}

	/**
	 * Recupera de la configuraci&oacute;n si debe forzarse el uso de la version nativa de AutoFirma,
	 * no la version WebStart.
	 * @return {@code false} si se configur&oacute; el uso de AutoFirma WebStart (valor "false"),
	 * {@code true} en caso contrario.
	 */
	public static boolean getClienteAfirmaForceNative() {
		return !Boolean.FALSE.toString().equalsIgnoreCase(config.getProperty(PROP_CLIENTEAFIRMA_FORCE_NATIVE));
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

		return config.getProperty(PROP_FIRE_PAGES_TITLE, ""); //$NON-NLS-1$
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

		return config.getProperty(PROP_FIRE_PAGES_LOGO_URL);
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

		return config.getProperty(PROP_DOCUMENT_MANAGER_PREFIX + docManager);
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

		return config.getProperty(PROP_SESSIONS_DAO);
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

	 	return config.getProperty(PROP_FIRE_PUBLIC_URL);
	 }

}
