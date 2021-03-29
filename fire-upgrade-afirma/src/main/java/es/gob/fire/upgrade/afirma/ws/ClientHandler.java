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
 * Class secures SOAP messages of @Firma requests.
 * @author Gobierno de Espana.
 */
package es.gob.fire.upgrade.afirma.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.saaj.SOAPElementImpl;
import org.apache.axis2.saaj.SOAPHeaderElementImpl;
import org.apache.axis2.saaj.TextImplEx;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * <p>Class secures SOAP messages of @Firma requests.</p>
 * <b>Project:</b><p>Library for the integration with the services of @Firma, eVisor and TS@.</p>
 * @version 1.4, 17/03/2020.
 */
class ClientHandler extends AbstractCommonHandler {

	/**
	 * Constant attribute that represents the handler name.
	 */
	private static final String HANDLER_NAME = "clientHandlerIntegra"; //$NON-NLS-1$

	/**
	 * Constant attribute that identifies UserNameToken authorization method.
	 */
	static final String USERNAMEOPTION = WSConstants.USERNAME_TOKEN_LN;

	/**
	 * Constant attribute that identifies BinarySecurityToken authorization method.
	 */
	static final String CERTIFICATEOPTION = WSConstants.BINARY_TOKEN_LN;

	/**
	 * Constant attribute that identifies none authorization method.
	 */
	static final String NONEOPTION = "none"; //$NON-NLS-1$

	/**
	 * Attribute that indicates the current authorization method.
	 */
	private String securityOption;

	/**
	 * Constructor method for the class ClientHandler.java.
	 * @param securityOpt Parameter that represents the authorization method.
	 * @throws WSServiceInvokerException If the method fails.
	 */
	ClientHandler(final String securityOpt) throws WSServiceInvokerException {
		this.handlerDesc.setName(HANDLER_NAME);
		this.handlerDesc.getRules().setPhaseLast(true);

		if (securityOpt == null || securityOpt.equals(NONEOPTION)) {
			this.securityOption = NONEOPTION;
		} else if (securityOpt.equals(USERNAMEOPTION)) {
			this.securityOption = USERNAMEOPTION;
		} else if (securityOpt.equals(CERTIFICATEOPTION)) {
			this.securityOption = CERTIFICATEOPTION;
		} else {
			throw new WSServiceInvokerException("Opcion de seguridad no valida: " + securityOpt ); //$NON-NLS-1$
		}
	}

	/**
	 * {@inheritDoc}
	 * @return
	 * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
	 */
	@Override
	public InvocationResponse invoke(final MessageContext msgContext) throws AxisFault {
		SOAPMessage secMsg;
		Document doc = null;

		secMsg = null;

		try {
			// Obtencion del documento XML que representa la peticion SOAP.
			doc = SAAJUtil.getDocumentFromSOAPEnvelope(msgContext.getEnvelope());
			// Securizacion de la peticion SOAP segun la opcion de seguridad
			// configurada
			if (this.securityOption.equals(USERNAMEOPTION)) {
				secMsg = this.createUserNameToken(doc);
			} else if (this.securityOption.equals(CERTIFICATEOPTION)) {
				secMsg = this.createBinarySecurityToken(doc);
			}

			if (!this.securityOption.equals(NONEOPTION)) {
				// Modificacion de la peticion SOAP...

				// Eliminamos el contenido del body e insertamos el nuevo body
				// generado.
				msgContext.getEnvelope().getBody().removeChildren();
				final SOAPBody body = msgContext.getEnvelope().getBody();
				updateSoapBody(body, secMsg.getSOAPBody());

				// Anadimos las cabeceras generadas.
				final Iterator<?> headers = secMsg.getSOAPHeader().getChildElements();
				while (headers.hasNext()) {
					msgContext.getEnvelope().getHeader().addChild(fromSOAPHeaderToOMElement((SOAPHeaderElementImpl) headers.next()));
				}
			}
		} catch (final Exception e) {
			throw AxisFault.makeFault(e);
		}
		return InvocationResponse.CONTINUE;
	}

	/**
	 * Method that transforms a SOAPHeader into a OMElement.
	 *
	 * @param headers SOAP header to transform.
	 * @return a new OMElement that represents the SOAP header.
	 */
	private static OMElement fromSOAPHeaderToOMElement(final SOAPHeaderElementImpl headers) {
		final OMFactory fac = OMAbstractFactory.getOMFactory();
		// Generamos los distintos elementos incluidos en el elemento principal.
		return parseElements(headers, fac);
	}

	/**
	 * Method that update the current SOAP body with the new generated body.
	 * @param body Current SOAP body to update.
	 * @param soapBody new SOAP body.
	 */
	private static void updateSoapBody(final SOAPBody body, final javax.xml.soap.SOAPBody soapBody) {
		final OMFactory fac = OMAbstractFactory.getOMFactory();
		final NamedNodeMap attrs = soapBody.getAttributes();

		// anadimos los atributos
		for (int i = 0; i < attrs.getLength(); i++) {
			OMAttribute attr = null;
			attr = fac.createOMAttribute(attrs.item(i).getNodeName(), null, attrs.item(i).getNodeValue());
			body.addAttribute(attr);
		}

		final Iterator<?> it = soapBody.getChildElements();
		while (it.hasNext()) {
			body.addChild(parseElements((SOAPElementImpl<?>) it.next(), fac));
		}
	}

	/**
	 * Method that creates a request secured by UserNameToken.
	 * @param soapEnvelopeRequest Parameter that represents the unsecured request.
	 * @return the secured request.
	 * @throws TransformerException If an unrecoverable error occurs during the course of the transformation.
	 * @throws IOException If there is a problem in reading data from the input stream.
	 * @throws SOAPException If the message is invalid.
	 * @throws WSSecurityException If the method fails.
	 */
	private SOAPMessage createUserNameToken(final Document soapEnvelopeRequest) throws TransformerException, IOException, SOAPException, WSSecurityException {
		ByteArrayOutputStream baos;
		Document secSOAPReqDoc;
		DOMSource source;
		Element element;
		SOAPMessage res;
		StreamResult streamResult;
		String secSOAPReq;
		WSSecUsernameToken wsSecUsernameToken;
		WSSecHeader wsSecHeader;

		// Eliminamos el provider ApacheXMLDSig de la lista de provider para que
		// no haya conflictos con el nuestro.
		final Provider apacheXMLDSigProvider = Security.getProvider("ApacheXMLDSig"); //$NON-NLS-1$
		Security.removeProvider("ApacheXMLDSig"); //$NON-NLS-1$

		try {
			// Insercion del tag wsse:Security y userNameToken
			wsSecHeader = new WSSecHeader(null, false);
			wsSecUsernameToken = new WSSecUsernameToken();
			wsSecUsernameToken.setPasswordType(getPasswordType());
			wsSecUsernameToken.setUserInfo(getUserAlias(), getPassword());
			wsSecHeader.insertSecurityHeader(soapEnvelopeRequest);
			wsSecUsernameToken.prepare(soapEnvelopeRequest);
			// Anadimos una marca de tiempo indicando la fecha de creacion del tag
			wsSecUsernameToken.addCreated();
			wsSecUsernameToken.addNonce();
			// Modificacion de la peticion
			secSOAPReqDoc = wsSecUsernameToken.build(soapEnvelopeRequest, wsSecHeader);
			element = secSOAPReqDoc.getDocumentElement();

			// Transformacion del elemento DOM a String
			source = new DOMSource(element);
			baos = new ByteArrayOutputStream();
			streamResult = new StreamResult(baos);
			TransformerFactory.newInstance().newTransformer().transform(source, streamResult);
			secSOAPReq = new String(baos.toByteArray());

			// Creacion de un nuevo mensaje SOAP a partir del mensaje SOAP
			// securizado formado
			final MessageFactory mf = new org.apache.axis2.saaj.MessageFactoryImpl();
			res = mf.createMessage(null, new ByteArrayInputStream(secSOAPReq.getBytes()));

		} finally {
			// Restauramos el provider ApacheXMLDSig eliminado inicialmente.
			if (apacheXMLDSigProvider != null) {
				// Eliminamos de nuevo el provider por si se ha anadido otra
				// version durante la generacion de la peticion.
				Security.removeProvider("ApacheXMLDSig"); //$NON-NLS-1$
				// Anadimos el provider.
				Security.insertProviderAt(apacheXMLDSigProvider, 1);
			}
		}

		return res;
	}

	/**
	 * Method that creates a request secured by BinarySecurityToken.
	 * @param soapEnvelopeRequest Parameter that represents the unsecured request.
	 * @return the secured request.
	 * @throws TransformerException If an unrecoverable error occurs during the course of the transformation.
	 * @throws IOException If there is a problem in reading data from the input stream.
	 * @throws SOAPException May be thrown if the message is invalid.
	 * @throws WSSecurityException If the method fails.
	 */
	private SOAPMessage createBinarySecurityToken(final Document soapEnvelopeRequest) throws TransformerException, IOException, SOAPException, WSSecurityException {

		SOAPMessage res;

		// Eliminamos el provider ApacheXMLDSig de la lista de provider para que
		// no haya conflictos con el nuestro.
		final Provider apacheXMLDSigProvider = Security.getProvider("ApacheXMLDSig"); //$NON-NLS-1$
		Security.removeProvider("ApacheXMLDSig"); //$NON-NLS-1$

		try {
			// Insercion del tag wsse:Security y BinarySecurityToken
			final WSSecHeader wsSecHeader = new WSSecHeader(null, false);
			final WSSecSignature wsSecSignature = new WSSecSignature();
			final Crypto crypto = getCryptoInstance();

			// Indicacion para que inserte el tag BinarySecurityToken
			wsSecSignature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
			wsSecSignature.setUserInfo(getUserAlias(), getPassword());
			wsSecHeader.insertSecurityHeader(soapEnvelopeRequest);
			wsSecSignature.prepare(soapEnvelopeRequest, crypto, wsSecHeader);

			// Modificacion y firma de la peticion
			final Document secSOAPReqDoc = wsSecSignature.build(soapEnvelopeRequest, crypto, wsSecHeader);
			final Element element = secSOAPReqDoc.getDocumentElement();

			// Transformacion del elemento DOM a String
			final DOMSource source = new DOMSource(element);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final StreamResult streamResult = new StreamResult(baos);
			TransformerFactory.newInstance().newTransformer().transform(source, streamResult);
			final String secSOAPReq = new String(baos.toByteArray());

			// Creacion de un nuevo mensaje SOAP a partir del mensaje SOAP
			// securizado formado
			final MessageFactory mf = new org.apache.axis2.saaj.MessageFactoryImpl();
			res = mf.createMessage(null, new ByteArrayInputStream(secSOAPReq.getBytes()));

		} finally {
			// Eliminamos de nuevo el provider por si se ha anadido otra
			// version durante la generacion de la peticion.
			Security.removeProvider("ApacheXMLDSig"); //$NON-NLS-1$

			// Restauramos el provider ApacheXMLDSig eliminado inicialmente.
			if (apacheXMLDSigProvider != null) {
				// Anadimos el provider.
				Security.insertProviderAt(apacheXMLDSigProvider, 1);
			}
		}
		return res;
	}

	/**
     * Method that transform a given set of child elements into a OMElement.
     *
     * @param sh Child to transform.
     * @param fac Object Model factory.
     * @return a new OMElement object that represents the set of child elements.
     */
	public static OMElement parseElements(final SOAPElementImpl<?> sh, final OMFactory fac) {

		// Creamos el namespace.
		final OMNamespace nsMain = fac.createOMNamespace(sh.getNamespaceURI(), sh.getPrefix());

		// Creamos el elemento principal.
		final OMElement mainElem = fac.createOMElement(sh.getElementQName().getLocalPart(), nsMain);

		// Añadimos los atributos.
		final NamedNodeMap attrs = sh.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			OMAttribute attr = null;
			attr = fac.createOMAttribute(attrs.item(i).getNodeName(), null, attrs.item(i).getNodeValue());
			if (attr.getLocalName().split(":")[0].equals("xmlns") && mainElem.getNamespace().getPrefix().equals(attr.getLocalName().split(":")[1])) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				continue;
			}
			mainElem.addAttribute(attr);
		}

		// Recorremos los hijos.
		final Iterator<?> elements = sh.getChildElements();
		while (elements.hasNext()) {
			final Object o = elements.next();
			if (o instanceof TextImplEx) {
				mainElem.setText(((TextImplEx) o).getNodeValue());
			} else {
				mainElem.addChild(parseElements((SOAPElementImpl<?>) o, fac));
			}
		}
		return mainElem;
	}
}
