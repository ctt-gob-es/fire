// Copyright (C) 2012-13 MINHAP, Gobierno de Espana
// This program is licensed and may be used, modified and redistributed under the terms
// of the European Public License (EUPL), either version 1.1 or (at your
// option) any later version as soon as they are approved by the European Commission.
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and
// more details.
// You should have received a copy of the EUPL1.1 license
// along with this program; if not, you may find it at
// http://joinup.ec.europa.eu/software/page/eupl/licence-eupl

/**
 * Class that represents handler used to verify the signature response.
 * @author Gobierno de Espa&ntilde;a.
 */
package es.gob.fire.upgrade.afirma.ws;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.xml.crypto.dsig.XMLSignature;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <p>Class that represents handler used to verify the signature response.</p>
 */
public class ResponseHandler extends AbstractCommonHandler {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ResponseHandler.class.getName());

	/** Etiqueta de los nodos firma de los XML firmados. */
    private static final String XML_SIGNATURE_TAG = "Signature"; //$NON-NLS-1$

	/**
	 * Constant attribute that represents the handler name.
	 */
	private static final String HANDLER_NAME = "responseHandlerWs"; //$NON-NLS-1$

	/**
	 * Constructor method for the class.
	 */
	public ResponseHandler() {
		this.handlerDesc.setName(HANDLER_NAME);
		this.handlerDesc.getRules().setPhaseLast(true);
	}

	/**
	 * Constructor method for the class ResponseHandler.java.
	 * @param signingCertStorePath Ruta del almac&eacute;n de confianza.
	 * @param signingCertStorePass Contrase&ntilde;a del almac&eacute;n de confianza.
	 * @param signingCertStoreType Tipo de almac&eacute;n de confianza.
	 * @param signingCertAlias Alias del certificado con el que debe estar firmada la respuesta.
	 */
	public ResponseHandler(final String signingCertStorePath, final String signingCertStorePass, final String signingCertStoreType, final String signingCertAlias) {
		this.handlerDesc.setName(HANDLER_NAME);
		this.handlerDesc.getRules().setPhaseLast(true);
		setKeystore(signingCertStorePath);
		setKeystorePass(signingCertStorePass);
		setKeystoreType(signingCertStoreType);
		setUserAlias(signingCertAlias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InvocationResponse invoke(final MessageContext msgContext) throws AxisFault {

		Document doc = null;
		try {
			// Obtencion del documento XML que representa la peticion SOAP.
			doc = SAAJUtil.getDocumentFromSOAPEnvelope(msgContext.getEnvelope());

			// Obtenemos el objeto signature.
			Element sigElement = null;
			final NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, XML_SIGNATURE_TAG);
			if (nl.getLength() > 0) {
				sigElement = (Element) nl.item(0);
				// creamos un manejador de la firma (para validarlo) a partir
				// del xml de la firma.
				org.apache.xml.security.Init.init();
				final org.apache.xml.security.signature.XMLSignature signature = new org.apache.xml.security.signature.XMLSignature(sigElement, ""); //$NON-NLS-1$


				// Registramos cual debe ser el atributo ID de los nodos de este XML
				// para que durante la validacion se pueda identificar estos nodos
				IdRegister.registerElements(doc.getDocumentElement());

				// Obtenemos el certificado con el que debe haberse firmado la respuesta
				final X509Certificate signingCert = loadSigningCert();
				if (signingCert != null) {
					if (signature.checkSignatureValue(signingCert)) {
						LOGGER.fine("La firma de la respuesta es valida"); //$NON-NLS-1$
					} else {
						throw new AxisFault("La firma de la respuesta del servicio web de @Firma no es valida segun certificado: " //$NON-NLS-1$
								+ signingCert.getSubjectDN() + "  - Numero de serie: " + signingCert.getSerialNumber()); //$NON-NLS-1$
					}
				}
			} else {
				throw new AxisFault("No se localiza el nodo de firma en la respuesta del servicio"); //$NON-NLS-1$
			}
		} catch (final Exception e) {
			throw AxisFault.makeFault(e);
		}
		return InvocationResponse.CONTINUE;
	}

	private X509Certificate loadSigningCert() throws AxisFault {

		X509Certificate cert;
		try (InputStream is = new FileInputStream(getKeystore());) {
			final KeyStore ks = KeyStore.getInstance(getKeystoreType());
			ks.load(is, getKeystorePass().toCharArray());
			cert = (X509Certificate) ks.getCertificate(getUserAlias());
			if (cert == null) {
				throw new KeyStoreException("No se ha encontrado en el almacen el certificado con el que debia firmarse la respuesta"); //$NON-NLS-1$
			}
		}
		catch (final Exception e) {
			throw new AxisFault("Error al recuperar el certificado con el que debia firmarse la respuesta", e); //$NON-NLS-1$
		}
		return cert;
	}
}
