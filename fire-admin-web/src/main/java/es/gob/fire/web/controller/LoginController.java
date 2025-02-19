/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
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
 * <b>File:</b><p>es.gob.fire.web.controller.LoginController.java.</p>
 * <b>Description:</b><p> Class that enables and configures the security of the FIRe application.</p>
  * <b>Project:</b><p></p>
 * <b>Date:</b><p>1.0, 27/01/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.3, 19/02/2025.
 */
package es.gob.fire.web.controller;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ibm.icu.util.Calendar;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.crypto.cades.verifier.CAdESAnalizer;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.entity.ControlAccess;
import es.gob.fire.service.ILoginService;
import es.gob.fire.web.clave.sp.request.RequestClave;
import es.gob.fire.web.config.WebSecurityConfig;
import es.gob.fire.web.exception.WebAdminException;

/**
 * <p>
 * Class that manages the requests related to the Login.
 * </p>
 * <b>Project:</b>
 * <p>
 * 
 * </p>
 *
 * @version 1.3, 19/02/2025.
 */
@Controller
public class LoginController {

	private static final String PARAM_SIGNATUREB64 = "signatureBase64";

	/** The Constant LOG. */
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
	
	/**
	 * Attribute that represent a property configure in admin_config.properties
	 */
	@Value("${conf.cert.number.attemps}")
	private Long confCertNumberAttemps;

	/**
	 * Attribute that represent a property configure in admin_config.properties
	 */
	@Value("${conf.cert.interval.contingency}")
	private Long confCertIntervalContingency;
	
	/**
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private ILoginService iLoginService;
	
	/**
	 * Handles the login error by retrieving the authentication exception from the session and displaying 
	 * an error message on the login page.
	 * <p>This method is called when there is an authentication error during login. It retrieves the 
	 * error message stored in the session (if available) and adds it as an attribute to the model, which
	 * is then rendered on the login page.</p>
	 * 
	 * @param request the HttpServletRequest object containing the client request
	 * @param model the Model object to add attributes to be rendered in the view
	 * @return a string representing the view name to render, in this case, the login page ("login.html")
	 */
	@GetMapping("/login-error")
    public String login(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            AuthenticationException ex = (AuthenticationException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
        }
        model.addAttribute("errorMessage", errorMessage);
        return "login.html";
    }

	/**
     * Method that maps the root request for the application to the controller to the login view.  
     * @return String that represents the name of the view to forward.
     */
    @RequestMapping(value = "/loginClave", method = RequestMethod.POST)
    public String loginWithClave(final Model model, HttpServletRequest request, HttpSession httpSession) {
    	String samlRequestB64;
    	Date currentDate = Calendar.getInstance().getTime(); // Obtenemos la fecha actual
    	String ipUser = request.getRemoteAddr(); // Obtenemos la ip del cliente que realiza la peticion
    	
    	try {
    		// Construimos la petición SAML en codificada en B64
			samlRequestB64 = RequestClave.constructRequestSAML();
		} catch (WebAdminException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "login.html";
		}
    	
    	// Guardaremos en la sesion la ip con la que el usuario ha realizado la peticion
    	httpSession.setAttribute("ipUser", ipUser);
    	
    	// Comprobaremos si necesitamos activar el certificado de contingencia
    	StringBuilder activateMsg = new StringBuilder();
    	if(activateCertificateContingency(model, currentDate, ipUser, activateMsg)) {
    		model.addAttribute("errorMessage", activateMsg);
    		model.addAttribute("accessByCertificate", true);
			return "login.html";
    	}
    	
    	// Registraremos la peticion en la tabla de control de acceso
    	ControlAccess controlAccess = new ControlAccess();
    	controlAccess.setIp(ipUser);
    	controlAccess.setStartDateAccess(currentDate);
    	iLoginService.saveControlAccess(controlAccess);
        
    	model.addAttribute("samlRequest", samlRequestB64);
    	model.addAttribute("relayState", RequestClave.relayState);
    	model.addAttribute("nodeServiceUrl", RequestClave.nodeServiceUrl);
    	
        return "loginClave.html";
    }

    /**
     * Activates the certificate contingency if the platform is unavailable or if the user has made 
     * too many access attempts within a short time.
     * 
     * @param model the Model to add attributes for the view
     * @param currentDate the current date for time comparison
     * @param ipUser the IP address of the user
     * @param activateMsg a message indicating the reason for activating contingency mode
     * 
     * @return true if contingency is activated, false otherwise
     */
	private boolean activateCertificateContingency(final Model model, Date currentDate, String ipUser, StringBuilder activateMsg) {
		LOGGER.info(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML004));
		
		boolean activate = false;
		
		// Obtenemos todos los controles de accesos ordenados por fecha mas antigua
    	List<ControlAccess> listControlAccess = iLoginService
    	        .obtainAllControlAccess()
    	        .stream()
    	        .filter(p -> p.getIp().equals(ipUser))
    	        .sorted(Comparator.comparing(ControlAccess::getStartDateAccess)) // Ordenamos por fecha más antigua
    	        .collect(Collectors.toList());
		
		// 1.- Comprobaremos si la plataforma de clave esta disponible
    	if(!iLoginService.isPasarelaAvailable()) {
    		activateMsg.append(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009));
    		activate = true;
    	}
    	
    	// 2.- Evaluaremos si para esta ip el usuario a intentando entrar mas de X veces en menos de X segundos
    	if(listControlAccess != null && !listControlAccess.isEmpty()) {
    		if(null != confCertNumberAttemps ) {
    			if(null != confCertIntervalContingency) {
    				if(listControlAccess.size() >= confCertNumberAttemps) {
            			ControlAccess controlAccess = listControlAccess.get(NumberConstants.NUM0);
            			// Obtenemos a partir de la fecha actual y la fecha mas antigua para esta ip, la diferencia en milisegundos convertidos a segundos
            			long secondsDifference  = (currentDate.getTime() - controlAccess.getStartDateAccess().getTime()) / NumberConstants.NUM1000; 
            			// Evaluamos si supera el intervalo de X segundos de contingencia
            			if(secondsDifference >= confCertIntervalContingency) {
            				// Si es asi activamos el login con certificado por contigencia
            				activateMsg.append(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML007));
            				activate = true;
            			}
            		}
    			} else {
    				LOGGER.warn(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML005));
    			}
    		} else {
    			LOGGER.warn(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML004));
    		}
    	}
    	
    	return activate;
	}
    
	/**
	 * Handles login using a certificate. It validates the certificate, generates a session cookie, 
	 * and performs login or error handling depending on certificate status.
	 * 
	 * @param signatureBase64 the base64 encoded signature of the user
	 * @param model the Model to add attributes for the view
	 * @param response the HttpServletResponse to add the cookie
	 * @return the view name after login attempt
	 */
	@RequestMapping(value = "/loginWithCertificate", method = RequestMethod.POST)
	public String loginWithCertificate(@RequestParam(PARAM_SIGNATUREB64) final String signatureBase64,
	                                   final Model model, HttpServletResponse response, HttpSession httpSession) {
	    
	    String passTrustStoreUsers = "changeit";
	    X509Certificate certificate = null;
	    KeyStore trustStoreUsers = null;

	    try {
	        // Decodificamos la firma en Base64
	        byte[] signBase64Bytes = Base64.getDecoder().decode(signatureBase64.getBytes());

	        // Analizamos la firma con CAdESAnalizer y obtenemos el certificado del usuario
	        CAdESAnalizer analizer = iLoginService.analizeSignWithCAdES(signBase64Bytes);
	        List<X509Certificate> certs = analizer.getSigningCertificates();
	        certificate = certs.get(0);
	       
	        // Verificamos vigencia del certificado
	        iLoginService.validatePeriodCertificate(certificate);
	        
	        // Cargamos el almacen de confianza
	        trustStoreUsers = iLoginService.loadTrustStoreUsers(passTrustStoreUsers, trustStoreUsers);
	        
	        // Buscamos el certificado del emisor en el TrustStore
	        X509Certificate issuerCert = iLoginService.validateIssuerWithTrustStore(certificate, trustStoreUsers);

	        // Verificamos la firma del certificado con la clave pública del emisor
	        iLoginService.verifyPublicKey(certificate, issuerCert);

	        // Eliminamos intentos fallidos de acceso para esta IP
	        String ipUser = (String) httpSession.getAttribute("ipUser");
	        iLoginService.deleteControlAccessByIp(ipUser);

	        // Generamos una nueva cookie de sesión
	        Cookie cookie = new Cookie(WebSecurityConfig.SESSION_TRACKING_COOKIE_NAME, iLoginService.generateCookieValue());
	        cookie.setPath("/");
	        cookie.setSecure(true);
	        response.addCookie(cookie);

	        return "inicio.html";

	    } catch (Exception e) {
	        String msgerror;

	        if (e instanceof CertificateException) {
	        	msgerror = e.getMessage();
	        } else if (e instanceof KeyStoreException) {
	        	msgerror = e.getMessage();
	        } else {
	            msgerror = e.getMessage();
	        }

	        model.addAttribute("errorMessage", msgerror);
	        model.addAttribute("accessByCertificate", true);
	        return "login.html";
	    }
	}
}