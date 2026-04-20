/*
/*******************************************************************************
 * Copyright (C) 2024 Secretaría General de la Administración Digital, Gobierno de España
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
 * <b>File:</b><p>es.clave.SP2.Utils.java.</p>
 * <b>Description:</b><p>Class with shared utilities within the Kit.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * <b>Date:</b><p>29/10/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 29/10/2025.
 */
package es.gob.fire.web.clave3.sp.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.EncryptionConfiguration;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xmlsec.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.ContentReference;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.springframework.stereotype.Component;

/**
 * <p>Class with shared utilities within the Kit.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * @version 1.0, 29/10/2025.
 */
@Component
public final class Utils {

	/** Attribute that represents the object that manages the log of the class. */
	private static final Logger LOGGER = LogManager.getLogger(Utils.class);

	public static final String HTTPS_PROTOCOL = "https"; //$NON-NLS-1$

	/**
	 * Function to sign SAML requests going from Kit to Proxy2
	 *
	 * @param <T> signableObject Object to sign
	 * @param key Private key associated with the signing certificate
	 * @param certificate Signing certificate
	 * @param signatureAlgorithm Signature algorithm, set to 'signature.algorithm'
	 * @throws Exception
	 */
	public static <T extends SignableXMLObject> void signRequest(final T signableObject, final PrivateKey key, final X509Certificate certificate, final String signatureAlgorithm) throws Exception {

		try {
			final BasicX509Credential credential = new BasicX509Credential(certificate, key);

			final Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);

			// Establecer el credential (la clave privada y el certificado X509)
			signature.setSigningCredential(credential);
			signature.setSignatureAlgorithm(signatureAlgorithm);
			signature.setKeyInfo(getKeyInfo(credential));
			signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

			// Asignar la firma a la petición o el objeto firmable
			signableObject.setSignature(signature);

			final String digestAlgorithm = "http://www.w3.org/2001/04/xmlenc#sha512"; //$NON-NLS-1$

			final List<ContentReference> contentReferences = signature.getContentReferences();
			if (!contentReferences.isEmpty()) {
				((SAMLObjectContentReference) contentReferences.get(0)).setDigestAlgorithm(digestAlgorithm);
			} else {
				LOGGER.error("Unable to set DigestMethodAlgorithm - algorithm " + digestAlgorithm + " not set"); //$NON-NLS-1$
			}

			LOGGER.info("Marshall samlToken."); //$NON-NLS-1$
			XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signableObject).marshall(signableObject);

			LOGGER.info("Sign samlToken."); //$NON-NLS-1$
			Signer.signObject(signature);

		} catch (final MarshallingException e) {
			LOGGER.error("signRequest - Error al realizar el marshalling.", e); //$NON-NLS-1$
			throw e;

		} catch (final SignatureException e) {
			LOGGER.error("signRequest - Error al realizar la firma.", e); //$NON-NLS-1$
			throw e;
		}
	}

	private static KeyInfo getKeyInfo(final Credential credential) throws SecurityException {
		final EncryptionConfiguration secConfiguration = SecurityConfigurationSupport.getGlobalEncryptionConfiguration();
		final NamedKeyInfoGeneratorManager namedKeyInfoGeneratorManager = secConfiguration.getDataKeyInfoGeneratorManager();
		final KeyInfoGeneratorManager keyInfoGeneratorManager = namedKeyInfoGeneratorManager.getDefaultManager();
		final KeyInfoGeneratorFactory keyInfoGeneratorFactory = keyInfoGeneratorManager.getFactory(credential);
		final KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
		return keyInfoGenerator.generate(credential);
	}

	/**
	 * Function that checks if a URL uses the secure protocol
	 *
	 * @param urlService
	 * @return true or false
	 */
	public static boolean isSecureConnection(final String urlService) {
		return urlService.toLowerCase().startsWith(HTTPS_PROTOCOL);
	}

	/**
	 * Function that retrieves properties from a properties file
	 *
	 * @param propertiesFilePath Path and name of the properties file
	 * @return Properties
	 * @throws Exception
	 */
	public static Properties loadCertificatesProperties(final String propertiesFilePath) throws Exception {
		final Properties properties = new Properties();

		try {
			final File f = new File(propertiesFilePath);
			try (FileReader fileReader = new FileReader(f)) {
				properties.load(fileReader);
			}

		} catch (final FileNotFoundException fnfe) {
			LOGGER.error("El fichero '" + propertiesFilePath + "' no existe en la ruta '" + propertiesFilePath + "'. Se creara un fichero nuevo."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return properties;
	}

	/**
	 * Function to write data from a byte array to a file
	 *
	 * @param data Array of data bytes
	 * @param filename Path and name of the destination file
	 * @throws IOException
	 */
	public static void writeFile(final byte[ ] data, final String filename) throws IOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
				FileOutputStream fos = new FileOutputStream(new File(filename))) {

			final byte[ ] buffer = new byte[1024];
			int bytesReaded;
			while ((bytesReaded = bais.read(buffer)) >= 0) {
				fos.write(buffer, 0, bytesReaded);
			}

		} catch (final IOException e) {
			LOGGER.error("Error writing file", e); //$NON-NLS-1$
			throw e;
		}
	}
}
