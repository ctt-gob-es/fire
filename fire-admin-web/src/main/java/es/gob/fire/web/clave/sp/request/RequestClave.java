/*
 * La plataforma TS@ es de libre distribución cuyo código fuente puede ser consultado
 * y descargado desde http://forja-ctt.administracionelectronica.gob.es
 *
 * Copyright 2019 Gobierno de España
 * Este fichero se distribuye bajo las licencias EUPL versión 1.1  y GPL versión 3, o superiores, según las
 * condiciones que figuran en el fichero 'LICENSE.txt' que se acompaña.  Si se   distribuyera este
 * fichero individualmente, deben incluirse aquí las condiciones expresadas allí.
 */

/**
 * <b>File:</b><p>es.gob.tsa.madmin.clave.sp.resquest.RequestClave.java.</p>
 * <b>Description:</b><p>Utility class for constructing SAML requests for Cl@ve. 
 * This class provides methods to generate SAML requests with the necessary configurations
 * and attributes, including destination URLs, provider names, and levels of assurance.</p>
 * <b>Project:</b><p>Time Stamping Authority.</p>
 * <b>Date:</b><p>24/05/2024.</p>
 * @author Gobierno de España.
 * @version 1.0, 24/05/2024.
 */
package es.gob.fire.web.clave.sp.request;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.gob.fire.web.clave.sp.utils.SPConfig;
import es.gob.fire.web.clave.sp.utils.SessionHolder;
import es.gob.fire.web.exception.WebAdminException;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.web.clave.sp.SpProtocolEngineFactory;
import es.gob.fire.web.clave.sp.utils.Constants;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IRequestMessageNoMetadata;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequestNoMetadata;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.engine.ProtocolEngineNoMetadataI;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.auth.engine.xml.opensaml.SecureRandomXmlIdGenerator;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * <p>
 * Utility class for constructing SAML requests for Cl@ve. 
 * This class provides methods to generate SAML requests with the necessary configurations
 * and attributes, including destination URLs, provider names, and levels of assurance.
 * </p>
 * <b>Project:</b>
 * <p>
 * Time Stamping Authority.
 * </p>
 *
 * @version 1.0, 24/05/2024.
 */
public class RequestClave {
	
	/**
	 * Attribute that represents the class logger.
	 */
	private static final Logger LOGGER = LogManager.getLogger(RequestClave.class);
	
	/**
     * The URL of the node service.
     */
    public static String nodeServiceUrl = "";
    
    /**
     * The relay state.
     */
    public static String relayState = ""; 
	
    /**
     * Constructs a SAML request for Cl@ve.
     * This method loads the Service Provider configurations, sets the necessary attributes for the SAML request,
     * generates the request message, and encodes it to Base64.
     * 
     * @return A Base64-encoded SAML request string.
     * @throws WebAdminException If an error occurs during the SAML request construction.
     */
	public static String constructRequestSAML() throws WebAdminException {
		
		Properties spConfig = SPConfig.loadSPConfigs();
		
		nodeServiceUrl = spConfig.getProperty(Constants.SERVICE_URL);
		relayState = SecureRandomXmlIdGenerator.INSTANCE.generateIdentifier(8); 
		
        String providerName = spConfig.getProperty(Constants.PROVIDER_NAME);
        String spApplication = spConfig.getProperty(Constants.SP_APLICATION);
        String returnUrl = spConfig.getProperty(Constants.SP_RETURN);
        String levelOfAssurance = spConfig.getProperty(Constants.EIDAS_LEVELOFASSURANCE);
		
        ImmutableAttributeMap.Builder reqAttrMapBuilder = new ImmutableAttributeMap.Builder();

		EidasAuthenticationRequestNoMetadata.Builder reqBuilder = new EidasAuthenticationRequestNoMetadata.Builder();
        reqBuilder.destination(nodeServiceUrl);
        reqBuilder.providerName(providerName);
        reqBuilder.requestedAttributes(reqAttrMapBuilder.build());
        reqBuilder.levelOfAssurance(levelOfAssurance);
        reqBuilder.levelOfAssuranceComparison(LevelOfAssuranceComparison.fromString("minimum").stringValue());
        reqBuilder.nameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        reqBuilder.binding(EidasSamlBinding.EMPTY.getName());
        reqBuilder.assertionConsumerServiceURL(returnUrl);
        reqBuilder.forceAuth(false);
        reqBuilder.spApplication(spApplication);
        
        IRequestMessageNoMetadata binaryRequestMessage = null;
        EidasAuthenticationRequestNoMetadata authRequest = null;
        try {
            reqBuilder.id(SAMLEngineUtils.generateNCName());
            authRequest = reqBuilder.build();
            ProtocolEngineNoMetadataI protocolEngine = SpProtocolEngineFactory.getSpProtocolEngine(Constants.SP_CONF, SPConfig.getConfigFilePath());
            binaryRequestMessage = protocolEngine.generateRequestMessage(authRequest, true);
        } catch (EIDASSAMLEngineException e) {
            LOGGER.error(e.getMessage(), e);
            throw new WebAdminException(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG001));
        }
        
        SessionHolder.sessionsSAML.put(authRequest.getId(), relayState);
        
        String samlRequestB64 = EidasStringUtil.encodeToBase64(binaryRequestMessage.getMessageBytes());
        
		return samlRequestB64;
	}
	
}
