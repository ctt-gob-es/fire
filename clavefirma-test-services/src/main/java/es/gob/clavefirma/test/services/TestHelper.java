/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.test.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.fire.server.connector.FIReCertificateAvailableException;
import es.gob.fire.server.connector.FIReCertificateException;
import es.gob.fire.server.connector.WeakRegistryException;


class TestHelper {

	private static final Logger LOGGER = Logger.getLogger(TestHelper.class.getName());

	private static final String SYS_PROP_PREFIX = "${"; //$NON-NLS-1$

	private static final String ENV_PROP_PREFIX = "%{"; //$NON-NLS-1$

	private static final String PROP_SUFIX = "}"; //$NON-NLS-1$

		private static final String DEFAULT_ENCODING = "utf-8"; //$NON-NLS-1$

	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String CONFIG_FILE = "test-backend.properties" ; //$NON-NLS-1$

	/** Variable de entorno que determina el directorio en el que buscar el fichero de configuraci&oacute;n. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR = "fire.config.path"; //$NON-NLS-1$

	/** Variable de entorno antigua que determinaba el directorio en el que buscar el fichero
	 * de configuraci&oacute;n. Se utiliza si no se ha establecido la nueva variable. */
	private static final String ENVIRONMENT_VAR_CONFIG_DIR_OLD = "clavefirma.config.path"; //$NON-NLS-1$

	private static final String PROPERTY_TMP_DIR = "tmp_dir"; //$NON-NLS-1$
	private static final String PROPERTY_URL_SERVICE = "url_service"; //$NON-NLS-1$

	private static final String TEST_USER_PROPERTY_PASSWORD = "password"; //$NON-NLS-1$
	private static final String TEST_USER_PROPERTY_STATE = "state"; //$NON-NLS-1$

	private static final String STATE_NOCERT = "NOCERT"; //$NON-NLS-1$
	private static final String STATE_BLOCKED = "BLOCKED"; //$NON-NLS-1$
	private static final String STATE_WEAK_REGISTRY = "WEAK_REGISTRY"; //$NON-NLS-1$

	private static final String URL_BASE_SERVICE = "http://localhost:8080/clavefirma-test-services/"; //$NON-NLS-1$

	private static final String REDIRECT_URL_ID_TAG = "$$TID$$"; //$NON-NLS-1$
	private static final String REDIRECT_URL_OK_TAG = "$$ROK$$"; //$NON-NLS-1$
	private static final String REDIRECT_URL_KO_TAG = "$$RKO$$"; //$NON-NLS-1$
	private static final String REDIRECT_URL_INFODOCUMENTOS_TAG = "$$INFODOCUMENTOS$"; //$NON-NLS-1$
	private static final String REDIRECT_ID = "$$ID$$"; //$NON-NLS-1$

	private static final String URL_SIGN_PARAMETERS_TEMPLATE = "test_pages/TestAuth.jsp?transactionid=" + REDIRECT_URL_ID_TAG //$NON-NLS-1$
			+ "&redirectko=" + REDIRECT_URL_KO_TAG //$NON-NLS-1$
			+ "&redirectok=" + REDIRECT_URL_OK_TAG //$NON-NLS-1$
			+ "&id=" + REDIRECT_ID //$NON-NLS-1$
			+ "&infoDocumentos=" + REDIRECT_URL_INFODOCUMENTOS_TAG; //$NON-NLS-1$

	private static final String URL_CERT_PARAMETERS_TEMPLATE = "test_pages/TestCert.jsp?transactionid=" + REDIRECT_URL_ID_TAG //$NON-NLS-1$
			+ "&redirectko=" + REDIRECT_URL_KO_TAG //$NON-NLS-1$
			+ "&redirectok=" + REDIRECT_URL_OK_TAG //$NON-NLS-1$
			+ "&id=" + REDIRECT_ID; //$NON-NLS-1$

	private static String urlTemplateSign = null;

	private static String urlTemplateCert = null;

	private static File tempDir = null;

	private static Properties p = null;


	private TestHelper() {
		// No se puede instanciar
	}

	private static Properties initConfig() {

		if (p != null) {
			return p;
		}

		try {
			p = loadConfig();
		}
		catch(final Exception e) {
			LOGGER.warning(
				"No se ha podido cargar la configuracion del servicio de pruebas, se usaran los valores por defecto: " + e //$NON-NLS-1$
			);
		}
		String tmpDir = p.getProperty(PROPERTY_TMP_DIR);
		if (tmpDir == null || tmpDir.length() == 0) {
			tmpDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		}
		final File f = new File(tmpDir);
		if (f.isDirectory() && f.canWrite() && f.canRead()) {
			tempDir = f;
			LOGGER.info("Directorio para ficheros temporales: " + f.getAbsolutePath()); //$NON-NLS-1$
		}
		else {
			throw new IllegalStateException(
				"No se puede usar el directorio para ficheros temporales: " + f //$NON-NLS-1$
			);
		}


		String urlBase = p.getProperty(PROPERTY_URL_SERVICE);
		if (urlBase == null || urlBase.length() == 0) {
			urlBase = URL_BASE_SERVICE;
		}
		if (!urlBase.endsWith("/")) { //$NON-NLS-1$
			urlBase += "/"; //$NON-NLS-1$
		}

		// URL del servicio de confirmacion de firma
		urlTemplateSign = urlBase + URL_SIGN_PARAMETERS_TEMPLATE;
		LOGGER.info("Plantilla de la URL del servicio de firma de pruebas: " + urlTemplateSign); //$NON-NLS-1$

		// URL del servicio de generacion de certificados
		urlTemplateCert = urlBase + URL_CERT_PARAMETERS_TEMPLATE;
		LOGGER.info("Plantilla de la URL del servicio de generacion de certificados de pruebas: " + urlTemplateCert); //$NON-NLS-1$

		return p;
	}

	/**
	 * Carga el fichero de configuraci&oacute;n del m&oacute;dulo o lo devuelve directamente si ya
	 * tuviese cargado.
	 * @return Propiedades de fichero de configuraci&oacute:n.
	 * @throws IOException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	public static Properties loadConfig() throws  IOException {

		final Properties config = new Properties();
		try {
			String configDirPath = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR);
			if (configDirPath == null) {
				configDirPath = System.getProperty(ENVIRONMENT_VAR_CONFIG_DIR_OLD);
			}

			boolean loaded = false;
			if (configDirPath != null) {
				final File configFile = new File(configDirPath, CONFIG_FILE).getCanonicalFile();
				if (!configFile.isFile() || !configFile.canRead()) {
					LOGGER.warning(
							"No se encontro el fichero " + CONFIG_FILE + " en el directorio configurado en la variable " + //$NON-NLS-1$ //$NON-NLS-2$
									ENVIRONMENT_VAR_CONFIG_DIR + ": " + configDirPath + //$NON-NLS-1$
									"\nSe buscara en el CLASSPATH."); //$NON-NLS-1$
				}
				else {
					try (InputStream is = new FileInputStream(configFile)) {
						config.load(is);
					}
					loaded = true;
				}
			}

			if (!loaded) {
				try (InputStream is = TestHelper.class.getResourceAsStream('/' + CONFIG_FILE)) {
					config.load(is);
				}
			}
		}
		catch(final NullPointerException e){
			LOGGER.severe("No se ha encontrado el fichero de configuracion: " + e); //$NON-NLS-1$
			throw new IOException("No se ha encontrado el fichero de propiedades " + CONFIG_FILE, e); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.severe("No se pudo cargar el fichero de configuracion " + CONFIG_FILE); //$NON-NLS-1$
			throw new IOException("No se pudo cargar el fichero de configuracion " + CONFIG_FILE, e); //$NON-NLS-1$
		}

		// Expandimos las propiedades configuradas con los parametros proporcionados
		// en el arranque del servidor
		for (final String key : config.keySet().toArray(new String[0])) {
			config.setProperty(key, mapProperties(config.getProperty(key)));
		}

		return config;
	}

	static File getDataFolder() {
		if (p == null) {
			initConfig();
		}
		return tempDir;
	}

	/**
	 * Recupera el almacen de claves del usuario.
	 * @param subjectId Identificador del usuario.
	 * @param needCheck Indica si debe verificarse la disponibilidad del certificado del usuario.
	 * @return Almac&eacute;n del usuario.
	 * @throws FIReCertificateException Cuando el usuario no tiene certificados.
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws InvalidUserException Cuando el usuario no exista.
	 * @throws BlockedCertificateException Cuando el usuario no tenga certificados activos y s&iacute; bloqueados.
	 * @throws WeakRegistryException Cuando el usuario realiz&oacute; un registro d&eacute;bil y no puede tener certificados de firma.
	 */
	static KeyStore getKeyStore(final String subjectId, final boolean needCheck) throws  FIReCertificateException,
																		KeyStoreException,
																		NoSuchAlgorithmException,
																		CertificateException,
																		IOException,
																		InvalidUserException,
																		BlockedCertificateException,
																		WeakRegistryException {

		if (needCheck) {
			checkSubject(subjectId);
		}

		// Eliminamos el proveedor de BouncyCastle en caso de estar instaladas
		Security.removeProvider("BC"); //$NON-NLS-1$

		final KeyStore ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
		ks.load(
				TestGetCertificateService.class.getResourceAsStream("/testservice/"+ subjectId + ".p12"), //$NON-NLS-1$ //$NON-NLS-2$
				getSubjectPassword(subjectId).toCharArray()
				);
		return ks;
	}

	/**
	 * Recupera la contrase&ntilde;a declarada para el almacen de claves en cuesti&oacute;n.
	 * @param subjectId Identificador del usuario, nombre de su almacen de claves y el de su
	 * fichero de propiedades.
	 * @return Contrase&ntilde;a declarada en el fichero de propiedades del usuario.
	 * @throws IOException Cuando ocurre un error al leer el fichero del usuario.
	 * @throws InvalidUserException Cuando no se encuentra este fichero.
	 */
	static String getSubjectPassword(final String subjectId) throws IOException, InvalidUserException {

		Properties tempProperties;
		try (final InputStream is = doSubjectExist(subjectId)) {
			tempProperties = new Properties();
			tempProperties.load(is);
		}
		return tempProperties.getProperty(TEST_USER_PROPERTY_PASSWORD);
	}

	static InputStream doSubjectExist(final String subjectId) throws InvalidUserException {
		if (subjectId == null) {
			throw new IllegalArgumentException(
					"El identificador del titular no puede ser nulo" //$NON-NLS-1$
					);
		}
		final InputStream resIs = TestGetCertificateService.class.getResourceAsStream(
				"/testservice/"+ subjectId + ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
		if (resIs == null) {
			throw new InvalidUserException("El titular no existe: " + subjectId); //$NON-NLS-1$
		}
		return resIs;
	}

	/**
	 * Comprueba que el usuario en cuesti&oacute;n est&eacute; dado de alta en el sistema
	 * y disponga de certificados. Si se ejecuta normalmente, sin lanzar excepciones, el
	 * usuario ser&aacute; v&aacute;lido y tendr&aacute; certificados para usar.
	 * @param subjectId Identificador del usuario
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidUserException Cuando el usuario no exista.
	 * @throws BlockedCertificate Cuando el usuario tenga un certificado bloqueado.
	 * @throws FIReCertificateException Cuando el usuario no tenga certificados.
	 * @throws BlockedCertificateException Cuando el usuario no tenga certificados activos y s&iacute; bloqueados.
	 * @throws WeakRegistryException Cuando el usuario realiz&oacute; un registro d&eacute;bil y no puede tener certificados de firma.
	 */
	private static void checkSubject(final String subjectId) throws InvalidUserException, FIReCertificateException, BlockedCertificateException, WeakRegistryException {

		final Properties tempProperties = new Properties();
		try (final InputStream is = doSubjectExist(subjectId)) {
			tempProperties.load(is);
		}
		catch(final IOException e) {
			throw new InvalidUserException("No se ha podido cargar el fichero descriptor del usuario", e); //$NON-NLS-1$
		}

		if (STATE_NOCERT.equalsIgnoreCase(tempProperties.getProperty(TEST_USER_PROPERTY_STATE))) {
			throw new FIReCertificateException("El usuario no tiene certificados de firma"); //$NON-NLS-1$
		}
		if (STATE_BLOCKED.equalsIgnoreCase(tempProperties.getProperty(TEST_USER_PROPERTY_STATE))) {
			throw new BlockedCertificateException("El certificado de firma del usuario esta bloqueado"); //$NON-NLS-1$
		}
		if (STATE_WEAK_REGISTRY.equalsIgnoreCase(tempProperties.getProperty(TEST_USER_PROPERTY_STATE))) {
			throw new WeakRegistryException("El usuario realizo un registro debil y no puede tener certificados de firma"); //$NON-NLS-1$
		}
	}

	static boolean subjectKeyStoreExist(final String subjectId) {
		if (subjectId == null) {
			return false;
		}
		final File f;
		final URL subUrl = TestGetCertificateService.class.getResource("/testservice/"+ subjectId + ".p12"); //$NON-NLS-1$ //$NON-NLS-2$
		if (subUrl == null) {
			return false;
		}
		try {
			f = new File(subUrl.toURI());
		}
		catch (final Exception e) {
			return false;
		}

		return f.isFile() && f.canRead();
	}

	@SuppressWarnings("deprecation")
	static String getSignRedirectionUrl(final String subjectId, final String transactionId, final String okUrlB64, final String errorUrlB64, final String infoDocumentosB64) {

		String infoDocsB64 = infoDocumentosB64;
		if (infoDocsB64 == null) {
			infoDocsB64 = ""; //$NON-NLS-1$
		}

		try {
			return urlTemplateSign
					.replace(REDIRECT_URL_ID_TAG, transactionId)
					.replace(REDIRECT_URL_OK_TAG, okUrlB64)
					.replace(REDIRECT_URL_KO_TAG, errorUrlB64)
					.replace(REDIRECT_ID, URLEncoder.encode(subjectId, DEFAULT_ENCODING))
					.replace(REDIRECT_URL_INFODOCUMENTOS_TAG, infoDocsB64);
		} catch (final UnsupportedEncodingException e) {
			LOGGER.warning("No se soporta el encoding proporcionado para codificar la URL, se usara el por defecto: " + e); //$NON-NLS-1$
			return urlTemplateSign
					.replace(REDIRECT_URL_ID_TAG, transactionId)
					.replace(REDIRECT_URL_OK_TAG, okUrlB64)
					.replace(REDIRECT_URL_KO_TAG, errorUrlB64)
					.replace(REDIRECT_ID, URLEncoder.encode(subjectId))
					.replace(REDIRECT_URL_INFODOCUMENTOS_TAG, infoDocsB64);
		}
	}

	@SuppressWarnings("deprecation")
	static String getCertificateRedirectionUrl(final String subjectId, final String transactionId, final String okUrlB64, final String errorUrlB64) {
		try {
			return urlTemplateCert
					.replace(REDIRECT_URL_ID_TAG, transactionId)
					.replace(REDIRECT_URL_OK_TAG, okUrlB64)
					.replace(REDIRECT_URL_KO_TAG, errorUrlB64)
					.replace(REDIRECT_ID, URLEncoder.encode(subjectId, DEFAULT_ENCODING));
		} catch (final UnsupportedEncodingException e) {
			LOGGER.warning("No se soporta el encoding proporcionado para codificar la URL, se usara el por defecto: " + e); //$NON-NLS-1$
			return urlTemplateCert
					.replace(REDIRECT_URL_ID_TAG, transactionId)
					.replace(REDIRECT_URL_OK_TAG, okUrlB64)
					.replace(REDIRECT_URL_KO_TAG, errorUrlB64)
					.replace(REDIRECT_ID, URLEncoder.encode(subjectId));
		}
	}

	/**
	 * Comprueba que el usuario en cuestion pueda crear nuevos certificados.
	 * @param subjectId Identificador del usuario
	 * @throws InvalidUserException Cuando el usuario no exista.
	 * @throws FIReCertificateAvailableException Cuando ya existen certificados y no se pueden generar m&aacute;s.
	 * @throws WeakRegistryException Cuando el usuario no puede generar certificados por haber hecho un registro d&eacute;bil.
	 */
	static void checkCanGenerateCert(final String subjectId) throws InvalidUserException, FIReCertificateAvailableException, WeakRegistryException {

		final Properties tempProperties = new Properties();
		try (final InputStream is = doSubjectExist(subjectId)) {
			tempProperties.load(is);
		}
		catch(final IOException e) {
			throw new InvalidUserException("No se ha podido cargar el fichero descriptor del usuario", e); //$NON-NLS-1$
		}

		if (STATE_WEAK_REGISTRY.equalsIgnoreCase(tempProperties.getProperty(TEST_USER_PROPERTY_STATE))) {
			throw new WeakRegistryException("El usuario realizo un registro debil y no puede tener certificados de firma"); //$NON-NLS-1$
		}

		if (!STATE_NOCERT.equalsIgnoreCase(tempProperties.getProperty(TEST_USER_PROPERTY_STATE))) {
			throw new FIReCertificateAvailableException("El usuario ya tiene el numero maximo de certificados de firma"); //$NON-NLS-1$
		}
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
	 * Carga un fichero dentro de un directorio, asegur&aacute;ndose de que no se intenta
	 * ning&uacute;n ataque de tipo PathTransversal.
	 * @param path Ruta base segura desde la que cargar el fichero.
	 * @param name Ruta relativa desde el directorio base.
	 * @return Fichero al que dirig&iacute;a la ruta.
	 * @throws IOException Cuando se ha intentado acceder a un fichero fuera del directorio
	 * base o cuando no se puede asegurar que est&eacute; en el mismo.
	 */
	public static File getCanonicalFile(final String path, final String name) throws IOException {
		return getCanonicalFile(new File(path), name);
	}

	/**
	 * Carga un fichero dentro de un directorio, asegur&aacute;ndose de que no se intenta
	 * ning&uacute;n ataque de tipo PathTransversal.
	 * @param dirFile Directorio suguro desde la que cargar el fichero.
	 * @param name Ruta relativa desde el directorio base.
	 * @return Fichero al que dirig&iacute;a la ruta.
	 * @throws IOException Cuando se ha intentado acceder a un fichero fuera del directorio
	 * base o cuando no se puede asegurar que est&eacute; en el mismo.
	 */
	public static File getCanonicalFile(final File dirFile, final String name) throws IOException {

		final String dirPath = dirFile.getCanonicalPath();
		final File canonicalFile = new File(dirPath, name).getCanonicalFile();

		if (!canonicalFile.getAbsolutePath().startsWith(dirPath)) {
			throw new IOException("Se ha intentado acceder a un fichero fuera de un directorio seguro"); //$NON-NLS-1$
		}
		return canonicalFile;
	}
}
