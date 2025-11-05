package es.gob.fire.web.clave.sp.response;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.ibm.icu.util.Calendar;

import es.gob.fire.commons.utils.UtilsDate;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.ThreadInfoDataSecureDTO;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IUserService;
import es.gob.fire.service.ILoginService;
import es.gob.fire.service.impl.LoginService;
import es.gob.fire.web.clave.sp.SpProtocolEngineFactory;
import es.gob.fire.web.clave.sp.exception.ClaveException;
import es.gob.fire.web.clave.sp.utils.Constants;
import es.gob.fire.web.clave.sp.utils.SPConfig;
import es.gob.fire.web.clave.sp.utils.SessionHolder;
import es.gob.fire.web.config.VersionProperties;
import es.gob.fire.web.config.WebSecurityConfig;
import es.gob.fire.web.controller.LoginController;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationResponseNoMetadata;
import eu.eidas.auth.engine.ProtocolEngineNoMetadataI;
import eu.eidas.auth.engine.xml.opensaml.SecureRandomXmlIdGenerator;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

@Controller
public class ResponseClave {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(LoginController.class);

	@Autowired
	private IUserService iUserService;
	
	@Autowired
	private ILoginService iLoginService;
	
	@Autowired
	private ThreadInfoDataSecureDTO threadInfoDataSecure;
	
	@Autowired
    private VersionProperties versionProperties;
	
	@RequestMapping(value = "/ResponseClave", method = RequestMethod.POST)
    public String responseClave(HttpServletRequest request, HttpServletResponse response, final Model model) {
		AtomicReference<String> dniRef =  new AtomicReference<>("");
		try {
			LOGGER.info(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG013));
			// Obtenemos los parametros de la solicitud
	        String samlResponse = request.getParameter("SAMLResponse");
	    	String relayState = request.getParameter("RelayState");
	    	String remoteHost = request.getRemoteHost();
	    		
	    	Properties spConfig = SPConfig.loadSPConfigs();
	    		
	    	String claveReturnUrl = spConfig.getProperty(Constants.SP_RETURN);
	    	
	    	// Validaremos la respuesta devuelta por clave
	    	IAuthenticationResponseNoMetadata authnResponse = validateRespAndActivateCertContigency(claveReturnUrl, samlResponse, relayState, remoteHost);
	    	
	    	// Eliminamos intentos fallidos de acceso para todas las ip
	    	iLoginService.deleteAllControlAccess();
	    	
	    	PersonalInfoBean personalInfoBean = this.obtenerDatosUsuario(authnResponse);
	    	
	    	dniRef.set(personalInfoBean.getDni());
	    		
	    	// Buscamos al usuario en la base de datos
	        final Iterable<User> allUsers = this.iUserService.getAllUser();
	        final String dni = dniRef.get();

	        if (allUsers == null || dni == null) {
	            throw new BadCredentialsException(
	                Language.getFormatResWebAdminGeneral(IWebAdminGeneral.UD_LOG006, new Object[] { dni })
	            );
	        }

	        final User user = StreamSupport.stream(allUsers.spliterator(), false)
	            .filter(p -> dni.equals(p.getDni()))
	           	.findFirst()
	           	.orElseThrow(() -> new BadCredentialsException(
	                Language.getFormatResWebAdminGeneral(IWebAdminGeneral.UD_LOG006, new Object[] { dni })
	        ));
	    	
	        // Autenticamos el token utilizando el usuario consultado previamente
	        Authentication authentication = iLoginService.obtainAuthAndUpdateLastAccess(user);
	            
	        // Si la autenticacion es exitosa, guardamos el resultado en el contexto de seguridad
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        
	        // Generaremos una nueva cookie por cada inicio de sesion exitoso
//	        Cookie cookie = new Cookie(WebSecurityConfig.SESSION_TRACKING_COOKIE_NAME, iLoginService.generateCookieValue());
//	    	cookie.setPath("/");
//	    	cookie.setSecure(true);
//	    	response.addCookie(cookie);
	    	
	    	model.addAttribute("appVersion", versionProperties.getProjectVersion());
	        model.addAttribute("copyrightYear", versionProperties.getCopyrightYear());
	    	
	        // Informamos en la traza que el usuario X se ha logueado en la administracion
	        LOGGER.info(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.UD_LOG007, new Object[] {user.getName(), user.getDni(), Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG015)}));
	        return "redirect:/inicio";
		
		}catch (ClaveException e) {
			String randomStringLogin = UtilsStringChar.getRandomStringToLogin();
			threadInfoDataSecure.setRandomStringLogin(randomStringLogin);
			threadInfoDataSecure.setLimitSignGen(new SimpleDateFormat(UtilsDate.FORMAT_DATE_TIME_STANDARD).format(Calendar.getInstance().getTime()));
    		model.addAttribute(LoginService.PARAM_RANDOM_STRING_LOGIN, randomStringLogin);
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("accessByCertificate", true);
			return "login.html";
		}catch (BadCredentialsException e) {
			LOGGER.info(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.UD_LOG008, new Object[] {dniRef.get()}));
			model.addAttribute("errorMessage", e.getMessage());
			return "login.html";
		}catch (Exception e) {
			LOGGER.error(e);
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009));
			return "login.html";
		}
    }
	
	/**
     * Retrieves user data from the provided SAML response.
	 * @param authnResponse TODO
     * 
     * @return A {@link PersonalInfoBean} object containing the user's personal information.
     * @throws SecurityException If the SAML response is invalid or the relay state does not match.
     */
    public PersonalInfoBean obtenerDatosUsuario(IAuthenticationResponseNoMetadata authnResponse) {
		
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
     * Validates the SAML response and activates the contingency certificate if necessary.
     * The contingency certificate will be activated in the following cases:
     * - A timeout occurred while accessing the gateway.
     * - The SAML response is empty or null.
     * - An exception occurs during SAML validation.
     * - The response from Cl@ve contains a critical error requiring certificate activation.
     *
     * @param claveReturnUrl The return URL for Cl@ve authentication.
     * @param samlResponse  The SAML response received from the authentication provider.
     * @param relayState    The relay state for session validation.
     * @param remoteHost    The remote host requesting authentication.
     * @return An {@code IAuthenticationResponseNoMetadata} containing authentication details.
     * @throws ClaveException If validation fails or the contingency certificate needs activation.
     */
	private IAuthenticationResponseNoMetadata validateRespAndActivateCertContigency(String claveReturnUrl,
			String samlResponse, String relayState, String remoteHost) throws ClaveException {
		
		if (samlResponse == null || samlResponse.trim().isEmpty()) {
			throw new ClaveException(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009));
		}

		byte[] decSamlToken = EidasStringUtil.decodeBytesFromBase64(samlResponse);
		IAuthenticationResponseNoMetadata authnResponse = null;
		try {
			ProtocolEngineNoMetadataI protocolEngine = SpProtocolEngineFactory.getSpProtocolEngine(Constants.SP_CONF, SPConfig.getConfigFilePath());
			
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
			throw new ClaveException(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009));
		}
		
		if (authnResponse.isFailure()) {
			throw new ClaveException(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009));
		}
		
		// Eliminamos el valor de comprobacion de la sesion
		SessionHolder.sessionsSAML.remove(authnResponse.getInResponseToId());
		return authnResponse;
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
