/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espana
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <b>File:</b>
 * <p>
 * es.gob.fire.i18n.Language.java.
 * </p>
 * <b>Description:</b>
 * <p>
 * Class responsible for managing the access to language resources.
 * </p>
 * <b>Project:</b>
 * <p>
 * Application for signing documents of @firma suite systems.
 * </p>
 * <b>Date:</b>
 * <p>
 * 21/12/2017.
 * </p>
 *
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 02/02/2021.
 */
package es.gob.fire.i18n;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import es.gob.fire.i18n.utils.Logger;

/**
 * <p>Class responsible for managing the access to language resources.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.2, 02/02/2021.
 */
public final class Language {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(Language.class);

	/**
	 * Constructor method for the class Language.java.
	 */
	private Language() {
		super();
	}

	/**
	 * Attribute that represents the locale specified in the configuration.
	 */
	private static Locale currentLocale;

	/**
	 * Attribute that represents the url class loader for the messages files.
	 */
	private static ClassLoader classLoaderMessages = null;

	/**
	 * Constant attribute that represents the property key fire.config.path.
	 */
	private static final String PROP_SERVER_CONFIG_DIR = "fire.config.path";
	
	/**
	 * Constant attribute that represents the messages directory.
	 */
	private static final String MESSAGES_DIRECTORY = "messages";
	
	/**
	 * Constant attribute that represents the string to identify the the bundle name for the file with the application language.
	 */
	private static final String BUNDLENAME_LANGUAGE = "Language";

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with web admin logs.
	 */
	private static final String BUNDLENAME_WEBADMIN_FIRE = "webAdmin.fire";

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with web admin general logs.
	 */
	private static final String BUNDLENAME_WEBADMIN_GENERAL = "webAdmin.general";

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with web admin fire logs.
	 */
	private static final String BUNDLENAME_COMMONUTILS_FIRE = "commonsUtils.fire";

	/**
	 * Constant attribute that represents the key for the configured locale for the platform.
	 */
	private static final String LANGUAGE = "LANGUAGE";

	/**
	 * Attribute that represents the properties for the locale for the core bundle messages.
	 */
	private static ResourceBundle resWebAdminBundle = null;
	
	/**
	 * Attribute that represents the properties for the locale for the web admin general messages.
	 */
	private static ResourceBundle resWebAdminGeneral = null;
	
	/**
	 * Attribute that represents the properties for the locale for the core bundle messages.
	 */
	private static ResourceBundle resCommonsUtilsBundle = null;

	static {
		// Preparamos el URLClassLoader con el que se cargaran los mensajes de logs
		try {
			final File configDirFile = new File(createAbsolutePath(getServerConfigDir(), MESSAGES_DIRECTORY));
			classLoaderMessages = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {

				public URLClassLoader run() {
					try {
						return new URLClassLoader(new URL[ ] { configDirFile.toURI().toURL() });
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			});
			reloadMessagesConfiguration();
		} catch (RuntimeException e) {
			LOGGER.error(e);
		}
	}

	/**
	 * Method that loads the configured locale and reload the text messages.
	 */
	public static void reloadMessagesConfiguration() {
		boolean takeDefaultLocale = false;
		String propLocale = null;
		// Cargamos el recurso que determina el locale.
		final ResourceBundle resLocale = ResourceBundle.getBundle(BUNDLENAME_LANGUAGE, Locale.getDefault(), classLoaderMessages);
		if (resLocale == null) {
			takeDefaultLocale = true;
		} else {
			propLocale = resLocale.getString(LANGUAGE);
		}
		// Tratamos de inicializar el Locale.
		if (propLocale == null) {
			takeDefaultLocale = true;
		} else {
			propLocale = propLocale.trim();
			final String[ ] localeSplit = propLocale.split("_");
			if (localeSplit == null || localeSplit.length != 2) {
				takeDefaultLocale = true;
			} else {
				currentLocale = new Locale(localeSplit[0], localeSplit[1]);
			}
		}
		// Si hay que tomar el locale por defecto...
		if (takeDefaultLocale) {
			LOGGER.error("No property was obtained correctly determining the Locale for log messages. Will take the default locale.");
			currentLocale = Locale.getDefault();
		}
		// Se informa en el log del Locale selecccionado.
		LOGGER.info("Take the next locale for messages logs: " + currentLocale.toString());
		// Se cargan los mensajes del modulo de administracion web.
		resWebAdminBundle = ResourceBundle.getBundle(BUNDLENAME_WEBADMIN_FIRE, currentLocale, classLoaderMessages);
		resWebAdminGeneral = ResourceBundle.getBundle(BUNDLENAME_WEBADMIN_GENERAL, currentLocale, classLoaderMessages);	
		// Cargamos los mensajes del modulo de commons utils.
		resCommonsUtilsBundle = ResourceBundle.getBundle(BUNDLENAME_COMMONUTILS_FIRE, currentLocale, classLoaderMessages);
	}

	/**
	 * Method that returns the value of the system property fire.config.path.
	 * @return Value of the system property fire.config.path. Null if not exist.
	 */
	private static String getServerConfigDir() {
		return System.getProperty(PROP_SERVER_CONFIG_DIR);
	}
	
	/**
	 * Auxiliar method to create an absolute path to a file.
	 * @param pathDir Directory absolute path that contains the file.
	 * @param filename Name of the file.
	 * @return Absolute path of the file.
	 */
	private static String createAbsolutePath(String pathDir, String filename) {
		return pathDir + File.separator + filename;
	}
	
	/**
	 * Gets the message with the key and values indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @param values Values for insert in the message.
	 * @return String with the message well-formed.
	 */
	public static String getFormatResWebFire(final String key, final Object[ ] values) {
		return new MessageFormat(resWebAdminBundle.getString(key), currentLocale).format(values);
	}

	/**
	 * Gets the message with the key indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @return String with the message.
	 */
	public static String getResWebFire(final String key) {
		return resWebAdminBundle.getString(key);
	}

	/**
	 * Gets the message with the key and values indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @param values Values for insert in the message.
	 * @return String with the message well-formed.
	 */
	public static String getFormatResCommonsUtilsFire(String key, Object[ ] values) {
		return new MessageFormat(resCommonsUtilsBundle.getString(key), currentLocale).format(values);
	}

	/**
	 * Gets the message with the key indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @return String with the message.
	 */
	public static String getResCommonsUtilsFire(String key) {
		return resCommonsUtilsBundle.getString(key);
	}

	/**
	 * Method that gets the bundle message of the web admin general for a certain key.
	 * @param key Parameter that represents the key for obtain the message.
	 * @return The bundle message of the web admin general for certain key.
	 */
	public static String getResWebAdminGeneral(String key) {
		return resWebAdminGeneral.getString(key);
	}

	/**
	 * Method that gets the bundle message of the web admin general for a certain key and values indicated as input parameters.
	 * @param key Parameter that represents the key for obtain the message.
	 * @param values Parameter that represents the list of values for insert in the message.
	 * @return the bundle message of the web admin general for certain key and values indicated as input parameters.
	 */
	public static String getFormatResWebAdminGeneral(String key, Object[ ] values) {
		return new MessageFormat(resWebAdminGeneral.getString(key), currentLocale).format(values);
	}
	
}
