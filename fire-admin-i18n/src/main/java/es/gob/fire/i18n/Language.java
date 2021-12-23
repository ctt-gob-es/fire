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
 * @version 1.1, 15/02/2019.
 */
package es.gob.fire.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * <p>Class responsible for managing the access to language resources.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 15/02/2019.
 */
/**
 * <p>Class .</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 15/04/2020.
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
	 * Constant attribute that represents the string to identify the bundle name to the file related with web admin logs.
	 */
	private static final String BUNDLENAME_WEBADMIN = "messages.webAdmin.fire";

	/**
	 * Constant attribute that represents the key for the configured locale for the platform.
	 */
	private static final String LANGUAGE = "LANGUAGE";

	/**
	 * Attribute that represents the properties for the locale for the core bundle messages.
	 */
	private static ResourceBundle resWebAdminBundle = null;



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
		resWebAdminBundle = ResourceBundle.getBundle(BUNDLENAME_WEBADMIN, currentLocale, classLoaderMessages);
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

}
