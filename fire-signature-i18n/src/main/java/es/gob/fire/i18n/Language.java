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
	 * Attribute that represents the locale specified in the configuration.
	 */
	private Locale currentLocale;

	/**
	 * Attribute that represents the url class loader for the messages files.
	 */
	private ClassLoader classLoaderMessages = null;

	/**
	 * Constant attribute that represents the string to identify the bundle name to the file related with fire signature module.
	 */
	private static final String BUNDLENAME_FIRESIGNATURE = "messages.signature.fire"; //$NON-NLS-1$

	/**
	 * Attribute that represents the properties for the locale for the core bundle messages for fire-signature.
	 */
	private ResourceBundle resFireSignatureBundle = null;

	/**
	 * Constructor method for the class Language.java.
	 */
	public Language(final Locale newLocale) {
		// Preparamos el URLClassLoader con el que se cargaran los mensajes de logs
		try {
			this.classLoaderMessages = Language.class.getClassLoader();
			this.currentLocale = newLocale;
			loadFireSignatureMessagesConfiguration();
		} catch (final RuntimeException e) {
			LOGGER.error(e);
		}
	}

	/**
	 * Method that loads the configured locale and the text messages for the fire signature module.
	 */
	private void loadFireSignatureMessagesConfiguration() {
		this.resFireSignatureBundle = ResourceBundle.getBundle(BUNDLENAME_FIRESIGNATURE, this.currentLocale, this.classLoaderMessages);
	}


	/**
	 * Gets the message with the key and values indicated as input parameters for fire-signature.
	 * @param key Key for obtain the message.
	 * @param values Values for insert in the message.
	 * @return String with the message well-formed.
	 */
	public String getFormatResFireSignature(final String key, final Object[ ] values) {
		return new MessageFormat(this.resFireSignatureBundle.getString(key), this.currentLocale).format(values);
	}


	/**
	 * Gets the message with the key indicated as input parameters for fire signature module.
	 * @param key Key for obtain the message.
	 * @return String with the message.
	 */
	public String getResFireSignature(final String key) {
		return this.resFireSignatureBundle.getString(key);
	}

}
