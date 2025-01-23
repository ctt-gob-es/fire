package es.gob.fire.web.clave.sp.response;

import java.util.Iterator;
import java.util.Properties;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IUserService;
import es.gob.fire.web.authentication.CustomUserAuthentication;
import es.gob.fire.web.clave.sp.SpProtocolEngineFactory;
import es.gob.fire.web.clave.sp.utils.Constants;
import es.gob.fire.web.clave.sp.utils.SPConfig;
import es.gob.fire.web.clave.sp.utils.SessionHolder;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationResponseNoMetadata;
import eu.eidas.auth.engine.ProtocolEngineNoMetadataI;
import eu.eidas.auth.engine.xml.opensaml.SecureRandomXmlIdGenerator;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

@Controller
public class ResponseClave {
	
	/** The Constant LOG. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseClave.class);
	
	@Autowired
	private IUserService iUserService;
	
	@Autowired
    private CustomUserAuthentication customUserAuthentication;
	
	@RequestMapping(value = "/ResponseClave", method = RequestMethod.POST)
    public String responseClave(HttpServletRequest request, final Model model) {
		try {
		
			// Obtenemos los parámetros de la solicitud si es necesario
	        String samlResponse = request.getParameter("SAMLResponse");
	    	String relayState = request.getParameter("RelayState");
	    	String remoteHost = request.getRemoteHost();
	    		
	    	Properties spConfig = SPConfig.loadSPConfigs();
	    		
	    	String claveReturnUrl = spConfig.getProperty(Constants.SP_RETURN);
	    		
	    	PersonalInfoBean personalInfoBean = this.obtenerDatosUsuario(claveReturnUrl, samlResponse, relayState, remoteHost);
	    		
	    	String dni = personalInfoBean.getDni();
	    		
	    	User user = StreamSupport.stream(iUserService.getAllUser().spliterator(), false) // Convierte el Iterable a Stream
	           	.filter(p -> p.getDni().equals(dni))
	           	.findFirst()
	           	.orElseThrow(() -> new BadCredentialsException(
	           			Language.getFormatResWebAdminGeneral(IWebAdminGeneral.UD_LOG006, new Object[] {dni})
	        ));
	
	    	// Creamos el token de autenticacion
	        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
	            
	        // Autenticamos el token utilizando CustomUserAuthentication que hereda AuthenticationManager
	        Authentication authResult = customUserAuthentication.authenticate(authentication);
	            
	        // Si la autenticación es exitosa, guardamos el resultado en el contexto de seguridad
	        SecurityContextHolder.getContext().setAuthentication(authResult);
	        
	        // Informamos en la traza que el usuario X se ha logueado en la administracion
	        LOGGER.info(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.UD_LOG007, new Object[] {user.getName()}));
	        return "inicio.html";
		}catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "login.html";
		}
    }
	
	/**
     * Retrieves user data from the provided SAML response.
     * 
     * @param claveReturnUrl The return URL of Cl@ve.
     * @param samlResponse The SAML response in Base64 format.
     * @param relayState The relay state associated with the SAML request.
     * @param remoteHost The remote host address.
     * @return A {@link PersonalInfoBean} object containing the user's personal information.
     * @throws SecurityException If the SAML response is invalid or the relay state does not match.
     */
    public PersonalInfoBean obtenerDatosUsuario(String claveReturnUrl, String samlResponse, String relayState, String remoteHost) {
		
		if (samlResponse == null || samlResponse.trim().isEmpty()) {
			throw new SecurityException(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG002));
		}

		byte[] decSamlToken = EidasStringUtil.decodeBytesFromBase64(samlResponse);
		IAuthenticationResponseNoMetadata authnResponse = null;
		try {
			ProtocolEngineNoMetadataI protocolEngine = SpProtocolEngineFactory.getSpProtocolEngine(Constants.SP_CONF);
			
			//validate SAML Token
			authnResponse = protocolEngine.unmarshallResponseAndValidate(decSamlToken, remoteHost, 0, 0, 
					claveReturnUrl);

			// Check session
			String prevRelayState = SessionHolder.sessionsSAML.get(authnResponse.getInResponseToId());
			if (prevRelayState == null || !prevRelayState.equals(relayState)) {
				throw new EIDASSAMLEngineException(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG003));
			}

		} catch (EIDASSAMLEngineException e) {
			LOGGER.error(e.getMessage());
			throw new SecurityException(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG004));
		}

		// Eliminamos el valor de comprobacion de la sesion
		SessionHolder.sessionsSAML.remove(authnResponse.getInResponseToId());
		
		if (authnResponse.isFailure()) {
			throw new SecurityException(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG005));
		}

		ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> attrMap
			= authnResponse.getAttributes().getAttributeMap();

		String infoToken = SecureRandomXmlIdGenerator.INSTANCE.generateIdentifier(8);
		
		PersonalInfoBean personalInfo = new PersonalInfoBean();
		personalInfo.setNombre(extractFromAttrMap("FirstName", attrMap));
		personalInfo.setApellidos(extractFromAttrMap("FamilyName", attrMap));
		personalInfo.setDni(extractFromAttrMap("PersonIdentifier", attrMap));
		personalInfo.setInfoToken(infoToken);
		
		return personalInfo;
	}
	
    /**
     * Extracts a value from the attribute map based on the provided friendly name.
     * 
     * @param friendlyName The friendly name of the attribute.
     * @param attrMap The map of attributes.
     * @return The value of the attribute as a String, or null if not found.
     */
    private static String extractFromAttrMap(String friendlyName, ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> attrMap) {
		Iterator<AttributeDefinition<?>> it = attrMap.keySet().iterator();
		while (it.hasNext()) {
			AttributeDefinition<?> k = it.next();
			if (friendlyName.equals(k.getFriendlyName())) {
				ImmutableSet<? extends AttributeValue<?>> valuesSet = attrMap.get(k);
				return !valuesSet.isEmpty()
						? (String) valuesSet.iterator().next().getValue()
						: null;
			}
		}
		return null;
	} 
}
