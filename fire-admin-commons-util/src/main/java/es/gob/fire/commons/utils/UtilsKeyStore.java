/* 
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
 * <b>File:</b><p>es.gob.fire.core.util.UtilsKeyStore.java.</p>
 * <b>Description:</b><p>Utility class that provides functionality for managing key stores.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>15/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/04/2020.
 */
package es.gob.fire.commons.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.log4j.Logger;

import es.gob.fire.i18n.IWebLogMessages;
import es.gob.fire.i18n.Language;

/** 
 * <p>Utility class that provides functionality for managing key stores.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 15/04/2020.
 */
public final class UtilsKeyStore {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(UtilsKeyStore.class);

	/**
	 * Constructor method for the class UtilsKeyStore.java. 
	 */
	private UtilsKeyStore() {

	}

	/**
	 * Method that loads the trust store.
	 * @param pathSslTruststore the trust store path
	 * @param typeSslTruststore the trust store type
	 * @param passSslTruststore the trust store pass
	 * @return the key store
	 */
	public static KeyStore loadSslTruststore(final String pathSslTruststore, final String typeSslTruststore, final String passSslTruststore) {
		String msgError = null;
		KeyStore cer = null;
		try {
			InputStream inputStreamKs = new FileInputStream(pathSslTruststore);
			// Accedemos al almacén de confianza SSL
			msgError = Language.getResWebFire(IWebLogMessages.ERRORWEB029);
			cer = KeyStore.getInstance(typeSslTruststore);
			cer.load(inputStreamKs, passSslTruststore.toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException
				| CertificateException | IOException e) {
			LOGGER.error(msgError, e.getCause());
		}
		return cer;
	}

}
