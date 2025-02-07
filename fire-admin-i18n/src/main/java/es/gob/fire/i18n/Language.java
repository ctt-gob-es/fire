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
 * Application for signing documents of FIRe suite systems.
 * </p>
 * <b>Date:</b>
 * <p>
 * 21/12/2017.
 * </p>
 *
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.4, 07/02/2025.
 */
package es.gob.fire.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import es.gob.fire.i18n.utils.Logger;

/**
 * <p>Class responsible for managing the access to language resources.</p>
 * <b>Project:</b><p>Application for signing documents of FIRe suite systems.</p>
 * @version 1.4, 07/02/2025.
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
	 * Constant attribute that represents the string to identify the the bundle name for the file with the application language.
	 */
	private static final String BUNDLENAME_LANGUAGE = "messages.Language";

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with web admin fire logs.
	 */
	private static final String BUNDLENAME_WEBADMIN_FIRE = "messages.webAdmin.fire";

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with web admin general logs.
	 */
	private static final String BUNDLENAME_WEBADMIN_GENERAL = "messages.webAdmin.general";

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with common utils fire logs.
	 */
	private static final String BUNDLENAME_COMMONUTILS_FIRE = "messages.commonsUtils.fire";

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with persistence general logs.
	 */
	private static final String BUNDLENAME_PERSISTENCE_GENERAL = "messages.persistence.general";

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with persistence constants.
	 */
	private static final String BUNDLENAME_PERSISTENCE_CONSTANTS = "messages.persistence.constants";
	
	/**
	 * Attribute that represents the location of file that contains the general messages quartz module.
	 */
	private static final String CONTENT_QUARTZ_GENERAL_PATH = "messages.fire-quartz.general";
	
	/**
	 * Attribute that represents the location of file that contains the TSL messages core module.
	 */
	private static final String CONTENT_CORE_TASKS_PATH = "messages.core.tasks";
	
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

	/**
	 * Attribute that represents the properties for the locale for the core bundle messages.
	 */
	private static ResourceBundle resPersistenceGeneral = null;

	/**
	 * Attribute that represents the properties for the locale for the core bundle constants.
	 */
	private static ResourceBundle resPersistenceBundleConstants = null;
	
	/**
	 * Attribute that represents the resource bundle with the general messages for the quartz module.
	 */
	private static ResourceBundle quartzGeneral = null;
	
	/**
	 * Attribute that represents the resource bundle with the general messages for the tasks.
	 */
	private static ResourceBundle resCoreTasks = null;
	
	static {
		// Preparamos el URLClassLoader con el que se cargaran los mensajes de logs
		try {
			classLoaderMessages = Language.class.getClassLoader();
			reloadMessagesConfiguration();
		} catch (final RuntimeException e) {
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
		// Se cargan las constantes del módulo de persistencia.
		resPersistenceGeneral = ResourceBundle.getBundle(BUNDLENAME_PERSISTENCE_GENERAL, currentLocale, classLoaderMessages);
		resPersistenceBundleConstants = ResourceBundle.getBundle(BUNDLENAME_PERSISTENCE_CONSTANTS, currentLocale, classLoaderMessages);
		// Se cargan los mensajes del modulo general de quartz
		quartzGeneral = ResourceBundle.getBundle(CONTENT_QUARTZ_GENERAL_PATH, currentLocale, classLoaderMessages);
		// Se cargan los mensajes del modulo del core
		resCoreTasks = ResourceBundle.getBundle(CONTENT_CORE_TASKS_PATH, currentLocale, classLoaderMessages);
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

	/**
	 * Method that gets the bundle message of the persistence general for a certain key.
	 * @param key Parameter that represents the key for obtain the message.
	 * @return The bundle message of the web admin general for certain key.
	 */
	public static String getResPersistenceGeneral(String key) {
		return resPersistenceGeneral.getString(key);
	}

	/**
	 * Method that gets the bundle message of the persistence general for a certain key and values indicated as input parameters.
	 * @param key Parameter that represents the key for obtain the message.
	 * @param values Parameter that represents the list of values for insert in the message.
	 * @return the bundle message of the web admin general for certain key and values indicated as input parameters.
	 */
	public static String getFormatResPersistenceGeneral(String key, Object[ ] values) {
		return new MessageFormat(resPersistenceGeneral.getString(key), currentLocale).format(values);
	}

	/**
	 * Gets the message with the key and values indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @param values Values for insert in the message.
	 * @return String with the message well-formed.
	 */
	public static String getFormatResPersistenceConstants(String key, Object[ ] values) {
		return new MessageFormat(resPersistenceBundleConstants.getString(key), currentLocale).format(values);
	}

	/**
	 * Gets the message with the key indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @return String with the message.
	 */
	public static String getResPersistenceConstants(String key) {
		return resPersistenceBundleConstants.getString(key);
	}
	
	/**
	 * Gets the general message (quartz module) with the key and values indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @param values Values for insert in the message.
	 * @return String with the message well-formed.
	 */
	public static String getFormatResQuartzGeneral(final String key, final Object... values) {
		return new MessageFormat(quartzGeneral.getString(key), currentLocale).format(values);
	}

	/**
	 * Gets the general message (quartz module) with the key indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @return String with the message.
	 */
	public static String getResQuartzGeneral(final String key) {
		return quartzGeneral.getString(key);
	}
	
	/**
	 * Gets the Task message (core module) with the key and values indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @param values Values for insert in the message.
	 * @return String with the message well-formed.
	 */
	public static String getFormatResCoreTasks(final String key, final Object... values) {
		return new MessageFormat(resCoreTasks.getString(key), currentLocale).format(values);
	}

	/**
	 * Gets the Task message (core module) with the key indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @return String with the message.
	 */
	public static String getResCoreTask(final String key) {
		return resCoreTasks.getString(key);
	}
}
