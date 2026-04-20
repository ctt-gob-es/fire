package es.gob.fire.web.clave3.sp;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.opensaml.core.xml.Namespace;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.w3c.dom.Element;

import es.gob.fire.web.clave3.sp.util.Utils;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

public class RequestBuilder {

	private static final String NAME_ID_POLICY = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"; //$NON-NLS-1$

	private static final Boolean FORCE_CHECK = Boolean.TRUE;

	private static final String SIGNATURE_ALGORITHM = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"; //$NON-NLS-1$

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger(RequestBuilder.class);


	public static RequestSAML buildLoginRequest(final ClaveConfig config) throws Exception {

		// Se instancian los objetos que almacenaran la informacion con la
        // que construir la peticion SAML
        final AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        final NameIDPolicy nameIDPolicy = new NameIDPolicyBuilder().buildObject();
        // Se inicializa el atributo que identifica el nameIDPolicy.
        nameIDPolicy.setAllowCreate(Boolean.TRUE);
        nameIDPolicy.setFormat(NAME_ID_POLICY);

        // Definicion de namespaces por defecto asociados al xml (SAML)
        authnRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("urn:oasis:names:tc:SAML:2.0:assertion", "saml2")); //$NON-NLS-1$ //$NON-NLS-2$
        authnRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds")); //$NON-NLS-1$ //$NON-NLS-2$
        authnRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("urn:oasis:names:tc:SAML:2.0:protocol", "saml2p")); //$NON-NLS-1$ //$NON-NLS-2$
        authnRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("http://eidas.europa.eu/saml-extensions", "eidas")); //$NON-NLS-1$ //$NON-NLS-2$
        authnRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("http://eidas.europa.eu/attributes/naturalperson", "eidas-natural")); //$NON-NLS-1$ //$NON-NLS-2$

        // A continuacion se establecen los valores propios de la peticion
        // SAML. Para más informacion sobre los atributos:
        // https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf
        // Atributo opcional que informa al receptor que el usuario permite
        // el consentimiento del envío de la peticion SAML. Por defecto:
        // Unspecified.
        authnRequest.setConsent(RequestAbstractType.UNSPECIFIED_CONSENT);
        // Define la URL de respuesta con la que deberá de comunicarse
        // Pasarela (Proxy2) cuando finalice el ciclo de autenticacion.
        authnRequest.setAssertionConsumerServiceURL(config.getReturnUrl());
        // Define la URL del entorno de Pasarela (Proxy2) a la que se está
        // enviando la peticion SAML.
        authnRequest.setDestination(config.getServiceUrl());
        // Define si se quiere forzar una nueva autenticacion en Pasarela
        // (Proxy2).
        authnRequest.setForceAuthn(FORCE_CHECK);
        // Número aleatorio que identifica la peticion SAML.
        authnRequest.setID(SecureRandomXmlIdGenerator.generateIdentifier());
        authnRequest.setIsPassive(Boolean.FALSE);
        authnRequest.setIssueInstant(DateTime.now());
        // Define la informacion del SP que realiza la peticion de
        // autenticacion en Pasarela (Proxy2)
        authnRequest.setProviderName(config.getProviderName() + ";" + config.getApplicationName()); //$NON-NLS-1$
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setNameIDPolicy(nameIDPolicy);

        // Define el atributo que establece el contexto de la autenticacion.
        final RequestedAuthnContext authContext = new RequestedAuthnContextBuilder().buildObject();
        authContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
        final AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
        final AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder.buildObject();
        // Define el nivel de seguridad que aplica en esta peticion SAML
        // (Level of Assurance (LoA))
        authnContextClassRef.setAuthnContextClassRef(config.getEidasAsseguranceLevel());
        authContext.getAuthnContextClassRefs().add(authnContextClassRef);

        // Define los atributos que se enviaran en la peticion SAML
        final List<EidasAttribute> requestEidasAttributes = new ArrayList<>();

        requestEidasAttributes.add(EidasAttribute.RELAY_STATE);
        authnRequest.setExtensions(buildExtensions(requestEidasAttributes));
        authnRequest.setRequestedAuthnContext(authContext);

        // Se inicializa la configuracion de la firma de la peticion SAML a
        // partir de los valores establecidos en el fichero
        // de configuracion de Clave
        final KeyStore ks = KeyStore.getInstance(config.getKeyStoreType());
        try (FileInputStream fis = new FileInputStream(config.getKeyStorePath())) {
        	final String keystorePassword = config.getKeyStorePass() != null ? config.getKeyStorePass() : ""; //$NON-NLS-1$
            ks.load(fis, keystorePassword.toCharArray());
        }

        final String alias = config.getKeyAlias();
        final String keyPassword = config.getKeyPass() != null ? config.getKeyPass() : ""; //$NON-NLS-1$
        final KeyStore.PrivateKeyEntry pkEntry = (PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(keyPassword.toCharArray()));
        final PrivateKey pk = pkEntry.getPrivateKey();
        final Certificate certificate = pkEntry.getCertificate();

        // Se realiza la firma de la peticion SAML construida
        Utils.signRequest(authnRequest, pk, (X509Certificate) certificate, SIGNATURE_ALGORITHM);
        final Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnRequest);
        out.marshall(authnRequest);

        // Se lleva a cabo la logica para convertir la peticion SAML
        // construida con OpenSaml en un documento XML para codificarlo
        // posteriormente en Base64 como lo espera recibir Pasarela (Proxy2)
        final Element elementDOM = authnRequest.getDOM();
        final String xmlString = SerializeSupport.nodeToString(elementDOM);

        // Preparamos los datos de la peticion
        final RequestSAML requestSaml = new RequestSAML();

        // XML de la peticion codificado en Base 64
        final String requestEncoded = Base64.getEncoder().encodeToString(xmlString.getBytes());
        requestSaml.setSAMLRequest(requestEncoded);

        // Identificador RelayState que permitira su validacion en la respuesta desde Pasarela
        final String relayState = SecureRandomXmlIdGenerator.generateIdentifier(8);
        requestSaml.setRelayState(relayState);

    	// Se asocia el identificados de la peticion con le relayState de cara a validar
        // posteriormente la respuesta

    	final String authId = authnRequest.getID();
    	SessionHolder.sessionsSAML.put(authId, relayState);

        return requestSaml;
	}

	private static Extensions buildExtensions(final List<EidasAttribute> eidasAttributes) throws Exception {
        final Extensions extensions = buildSAMLObject(Extensions.class);

        final XSAny requestedAttributes = new XSAnyBuilder().buildObject("http://eidas.europa.eu/saml-extensions", "RequestedAttributes", "eidas"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        addEidasAttributes(eidasAttributes, requestedAttributes);
        extensions.getUnknownXMLObjects().add(requestedAttributes);

        return extensions;
    }

    public static <T> T buildSAMLObject(final Class<T> clazz) throws Exception {
        T object = null;

        try {
            final XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
            final QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null); //$NON-NLS-1$
            final Object rawObject = builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
            if (!clazz.isInstance(rawObject)) {
                throw new ClassCastException("Failed to cast the SAML object to the expected type " + clazz.getName()); //$NON-NLS-1$
            }
			object = clazz.cast(rawObject);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.error("buildSAMLObject - Error al crear el SAML object", e); //$NON-NLS-1$
            throw e;
        }

        return object;
    }

    private static void addEidasAttributes(final List<EidasAttribute> eidasAttributes, final XSAny requestedAttributes) throws Exception {
        if (eidasAttributes == null) {
            return;
        }
        for (final EidasAttribute attribute: eidasAttributes) {
            addEidasAttribute(requestedAttributes, attribute);
        }
    }

    private static void addEidasAttribute(final XSAny requestedAttributes, final EidasAttribute eidasAttribute) throws Exception {
        try {
            requestedAttributes.getUnknownXMLObjects().add(buildRequestedAttribute(eidasAttribute));
        } catch (final Exception e) {
            LOGGER.error("addEidasAttribute - Error al realizar el marshalling.", e); //$NON-NLS-1$
            throw e;
        }
    }

    private static XSAny buildRequestedAttribute(final EidasAttribute eidasAttribute) {
        final XSAny requestedAttribute = new XSAnyBuilder().buildObject("http://eidas.europa.eu/saml-extensions", "RequestedAttribute", "eidas"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        requestedAttribute.getUnknownAttributes().put(new QName("FriendlyName"), eidasAttribute.getFriendlyName()); //$NON-NLS-1$
        requestedAttribute.getUnknownAttributes().put(new QName("Name"), eidasAttribute.getName()); //$NON-NLS-1$
        requestedAttribute.getUnknownAttributes().put(new QName("NameFormat"), "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"); //$NON-NLS-1$ //$NON-NLS-2$
        requestedAttribute.getUnknownAttributes().put(new QName("isRequired"), eidasAttribute.isRequired() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        if (eidasAttribute.equals(EidasAttribute.RELAY_STATE)) {
            final XSAny attributeValue = new XSAnyBuilder().buildObject("http://eidas.europa.eu/saml-extensions", "AttributeValue", "eidas"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            attributeValue.setTextContent(SecureRandomXmlIdGenerator.generateIdentifier(8));
            attributeValue.getUnknownAttributes().put(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi"), "eidas-natural:PersonIdentifierType"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            requestedAttribute.getUnknownXMLObjects().add(attributeValue);
        }

        return requestedAttribute;
    }

    public static RequestSAML buildLogoutRequest(final ClaveConfig config) throws Exception {

    	// Se instancian los objetos que almacenarán la información con la
        // que construir la petición SAML
        final LogoutRequest logoutRequest = new LogoutRequestBuilder().buildObject();

        // Define la URL de respuesta con la que deberá de comunicarse
        // Pasarela (Proxy2) cuando finalice el ciclo de desconexión.
        final Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(config.getReturnUrl());

        // Se inicializa el atributo que identifica el nameIDPolicy.
        final NameID nameID = new NameIDBuilder().buildObject();
        nameID.setFormat(NameIDType.UNSPECIFIED);
        nameID.setSPNameQualifier(config.getReturnUrl());
        nameID.setValue(config.getProviderName());

        // Definición de namespaces por defecto asociados al xml (SAML)
        logoutRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("urn:oasis:names:tc:SAML:2.0:assertion", "saml2")); //$NON-NLS-1$ //$NON-NLS-2$
        logoutRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("http://www.w3.org/2000/09/xmldsig#", "ds")); //$NON-NLS-1$ //$NON-NLS-2$
        logoutRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("urn:oasis:names:tc:SAML:2.0:protocol", "saml2p")); //$NON-NLS-1$ //$NON-NLS-2$
        logoutRequest.getNamespaceManager().registerNamespaceDeclaration(new Namespace("http://eidas.europa.eu/saml-extensions", "eidas")); //$NON-NLS-1$ //$NON-NLS-2$

        // A continuación se establecen los valores propios de la petición
        // SAML de desconexión. Para más información sobre los atributos:
        // https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf
        logoutRequest.setIssueInstant(DateTime.now());
        logoutRequest.setDestination(config.getServiceUrl());
        logoutRequest.setID(SecureRandomXmlIdGenerator.generateIdentifier());
        logoutRequest.setIssuer(issuer);
        logoutRequest.setNameID(nameID);
        logoutRequest.setVersion(SAMLVersion.VERSION_20);
        logoutRequest.setReason("urn:oasis:names:tc:SAML:2.0:logout:user"); //$NON-NLS-1$
        logoutRequest.setNotOnOrAfter(DateTime.now().plusSeconds(300));

        // Se inicializa la configuración de la firma de la petición SAML de
        // desconexión a partir de los valores establecidos en el fichero
        // de configuracion de Clave
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream fis = new FileInputStream(config.getKeyStorePath())) {
        	final String keystorePassword = config.getKeyStorePass() != null ? config.getKeyStorePass() : ""; //$NON-NLS-1$
            ks.load(fis, keystorePassword.toCharArray());
        }

        final String alias = config.getKeyAlias();
        final String keyPassword = config.getKeyPass() != null ? config.getKeyPass() : ""; //$NON-NLS-1$
        final KeyStore.PrivateKeyEntry pkEntry = (PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(keyPassword.toCharArray()));
        final PrivateKey pk = pkEntry.getPrivateKey();
        final Certificate certificate = pkEntry.getCertificate();

        // Se realiza la firma de la petición SAML de desconexión construida
        Utils.signRequest(logoutRequest, pk, (X509Certificate) certificate, SIGNATURE_ALGORITHM);
        final Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(logoutRequest);
        out.marshall(logoutRequest);

        // Se lleva a cabo la lógica para convertir la petición SAML de
        // desconexión construida con OpenSaml en un documento XML para
        // codificarlo posteriormente en Base64 como lo espera recibir
        // Pasarela (Proxy2)
        final Element elementDOM = logoutRequest.getDOM();
        final String xmlString = SerializeSupport.nodeToString(elementDOM);

        // Preparamos los datos de la peticion
        final RequestSAML requestSaml = new RequestSAML();

        // XML de la peticion codificado en Base 64
        final String requestEncoded = Base64.getEncoder().encodeToString(xmlString.getBytes());
        requestSaml.setLogoutRequest(requestEncoded);

        // Identificador RelayState que permitira su validacion en la respuesta desde Pasarela
        final String relayState = SecureRandomXmlIdGenerator.generateIdentifier(8);
        requestSaml.setRelayState(relayState);


        // URL del servicio
        requestSaml.setUrl(config.getServiceUrl());

    	// Se asocia el identificados de la peticion con le relayState de cara a validar
        // posteriormente la respuesta
    	final String authId = logoutRequest.getID();
    	SessionHolder.sessionsSAML.put(authId, relayState);

        return requestSaml;
    }
}
