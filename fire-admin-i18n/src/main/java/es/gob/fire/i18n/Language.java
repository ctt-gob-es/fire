/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * @author Gobierno de España.
 * @version 1.1, 15/02/2019.
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
	private static URLClassLoader urlClassLoaderMessages = null;
	
	/**
	 * Constant attribute that represents the property key server.config.dir. 
	 */
	public static final String PROP_SERVER_CONFIG_DIR = "server.config.dir";
	
	/**
	 * Constant attribute that represents the name of messages directory inside configuration directory.
	 */
	private static final String MESSAGES_DIRECTORY = "messages";

	/**
	 * Constant attribute that represents the string to identify the the bundle name for the file with the application language.
	 */
	private static final String BUNDLENAME_LANGUAGE = "Language";
	
	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with web admin logs.
	 */
	private static final String BUNDLENAME_WEBADMIN = "webAdmin.fire";

	/**
	 * Constant attribute that represents the key for the configured locale for the platform.
	 */
	private static final String LANGUAGE = "LANGUAGE";
	
	/**
	 * Attribute that represents the properties for the locale for the core bundle messages.
	 */
	private static ResourceBundle resWebAdminBundle = null;

	static {
		// Preparamos el URLClassLoader que hará referencia
		// al directorio de los mensajes de logs dentro de la configuración.
		try {
			final File configDirFile = new File(System.getProperty(PROP_SERVER_CONFIG_DIR) + File.separator + MESSAGES_DIRECTORY);
			urlClassLoaderMessages = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
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
		ResourceBundle resLocale = ResourceBundle.getBundle(BUNDLENAME_LANGUAGE, Locale.getDefault(), urlClassLoaderMessages);
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
			String[ ] localeSplit = propLocale.split("_");
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
		// Se cargan los mensajes del módulo de administración web.
		resWebAdminBundle = ResourceBundle.getBundle(BUNDLENAME_WEBADMIN, currentLocale, urlClassLoaderMessages);
	}
	
	/**
	 * Gets the message with the key and values indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @param values Values for insert in the message.
	 * @return String with the message well-formed.
	 */
	public static String getFormatResWebMonitoriza(String key, Object[ ] values) {
		return new MessageFormat(resWebAdminBundle.getString(key), currentLocale).format(values);
	}

	/**
	 * Gets the message with the key indicated as input parameters.
	 * @param key Key for obtain the message.
	 * @return String with the message.
	 */
	public static String getResWebFire(String key) {
		return resWebAdminBundle.getString(key);
	}
	
}
