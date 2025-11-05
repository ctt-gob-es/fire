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
 * @version 1.5, 10/03/2025.
 */
package es.gob.fire.web.controller;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ibm.icu.util.Calendar;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsDate;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.crypto.cades.verifier.CAdESAnalizer;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.ThreadInfoDataSecureDTO;
import es.gob.fire.persistence.entity.ControlAccess;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IUserService;
import es.gob.fire.service.ILoginService;
import es.gob.fire.service.impl.LoginService;
import es.gob.fire.web.clave.sp.request.RequestClave;
import es.gob.fire.web.config.VersionProperties;
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
 * @version 1.5, 10/03/2025.
 */
@Controller
public class LoginController {

	private static final String PARAM_SIGNATUREB64 = "signatureBase64";

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(LoginController.class);

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
	 * Attribute that represents the service object for accessing the repository.
	 */
	@Autowired
	private IUserService iUserService;

	/**
	 * Attribute that represents the DTO for current user session.
	 */
	@Autowired
	private ThreadInfoDataSecureDTO threadInfoDataSecureDTO;

	@Autowired
    private VersionProperties versionProperties;

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
    public String login(final HttpServletRequest request, final Model model) {
        final HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            final AuthenticationException ex = (AuthenticationException) session
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
    public String loginWithClave(final Model model, final HttpServletRequest request) {
    	LOGGER.info(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG011));
    	String samlRequestB64;
    	final Date currentDate = Calendar.getInstance().getTime(); // Obtenemos la fecha actual
    	final String ipUser = request.getRemoteAddr(); // Obtenemos la ip del cliente que realiza la peticion

    	try {
    		// Construimos la peticion SAML en codificada en B64
			samlRequestB64 = RequestClave.constructRequestSAML();
		} catch (final WebAdminException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "login.html";
		}

    	// Comprobaremos si necesitamos activar el certificado de contingencia
    	final StringBuilder activateMsg = new StringBuilder();
    	if(activateCertificateContingency(model, currentDate, ipUser, activateMsg)) {
    		LOGGER.info(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG012));
    		final String randomStringLogin = UtilsStringChar.getRandomStringToLogin();
    		this.threadInfoDataSecureDTO.setRandomStringLogin(randomStringLogin);
    		this.threadInfoDataSecureDTO.setLimitSignGen(new SimpleDateFormat(UtilsDate.FORMAT_DATE_TIME_STANDARD).format(Calendar.getInstance().getTime()));
    		model.addAttribute(LoginService.PARAM_RANDOM_STRING_LOGIN, randomStringLogin);
    		model.addAttribute("errorMessage", activateMsg);
    		model.addAttribute("accessByCertificate", true);
			return "login.html";
    	}

    	// Registraremos la peticion en la tabla de control de acceso
    	final ControlAccess controlAccess = new ControlAccess();
    	controlAccess.setIp(ipUser);
    	controlAccess.setStartDateAccess(currentDate);
    	this.iLoginService.saveControlAccess(controlAccess);

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
	private boolean activateCertificateContingency(final Model model, final Date currentDate, final String ipUser, final StringBuilder activateMsg) {

		// Obtenemos todos los controles de accesos ordenados por fecha mas antigua
    	final List<ControlAccess> listControlAccess = this.iLoginService
    	        .obtainAllControlAccess()
    	        .stream()
    	        .filter(p -> p.getIp().equals(ipUser))
    	        .sorted(Comparator.comparing(ControlAccess::getStartDateAccess)) // Ordenamos por fecha más antigua
    	        .collect(Collectors.toList());

		// 1.- Comprobaremos si la plataforma de clave esta disponible
    	if(!this.iLoginService.isPasarelaAvailable()) {
    		activateMsg.append(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009));
    		return true;
    	}

    	// 2.- Evaluaremos si para esta ip el usuario a intentando entrar mas de X veces en menos de X segundos
    	if(listControlAccess != null && !listControlAccess.isEmpty()) {
    		if(null != this.confCertNumberAttemps ) {
    			if(null != this.confCertIntervalContingency) {
    				if(listControlAccess.size() >= this.confCertNumberAttemps) {
            			final ControlAccess controlAccess = listControlAccess.get(NumberConstants.NUM0);
            			// Obtenemos a partir de la fecha actual y la fecha mas antigua para esta ip, la diferencia en milisegundos convertidos a segundos
            			final long secondsDifference  = (currentDate.getTime() - controlAccess.getStartDateAccess().getTime()) / NumberConstants.NUM1000;
            			// Evaluamos si supera el intervalo de X segundos de contingencia
            			if(secondsDifference >= this.confCertIntervalContingency) {
            				LOGGER.warn(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML007));
            				// Si es asi activamos el login con certificado por contigencia
            				activateMsg.append(Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009));
            				// Eliminamos intentos fallidos de acceso para todas las ip puesto que clave sigue estando operativo
            		    	this.iLoginService.deleteAllControlAccess();
            				return true;
            			}
            		}
    			} else {
    				LOGGER.warn(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML005));
    			}
    		} else {
    			LOGGER.warn(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML004));
    		}
    	}

    	return false;
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
	                                   final Model model, final HttpServletResponse response) {
	    X509Certificate certificate = null;
	    final AtomicReference<String> dniRef =  new AtomicReference<>("");
	    try {
	    	LOGGER.info(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML014));
	        // Decodificamos la firma en Base64
	        final byte[] signBase64Bytes = Base64.getDecoder().decode(signatureBase64.getBytes());

	        // Analizamos la firma con CAdESAnalizer y obtenemos el certificado del usuario
	        final CAdESAnalizer analizer = this.iLoginService.analizeSignWithCAdES(signBase64Bytes);

	        // Validamos si la firma es segura
	        this.iLoginService.validateIfSignSecure(analizer);

	        final List<X509Certificate> certs = analizer.getSigningCertificates();
	        certificate = certs.get(0);

	        // Verificamos vigencia del certificado
	        this.iLoginService.validatePeriodToCertUser(certificate);

	        // Cargamos el almacen de confianza
	        final KeyStore trustStoreUsers = this.iLoginService.loadTrustStoreUsers();

	        // Buscamos en el almacen de confianza algun certificado que tenga el mismo emisor
	        final X509Certificate issuerCert = this.iLoginService.validateIssuerWithTrustStoreUsers(certificate, trustStoreUsers);

	        // Verificamos la firma del certificado con la clave publica del emisor
	        this.iLoginService.verifyPublicKeyToCertUser(certificate, issuerCert);

	        // Obtenemos el dni a partir de un certificado valido
	        dniRef.set(this.iLoginService.obtainDNIfromCertUser(certificate));

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
	        final Authentication authentication = this.iLoginService.obtainAuthAndUpdateLastAccess(user);

	        // Si la autenticacion es exitosa, guardamos el resultado en el contexto de seguridad
	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        // Eliminamos intentos fallidos de acceso para todas las ip
	        this.iLoginService.deleteAllControlAccess();

//	        // Generamos una nueva cookie de sesion
//	        final Cookie cookie = new Cookie(WebSecurityConfig.SESSION_TRACKING_COOKIE_NAME,
//	        									this.iLoginService.generateCookieValue());
//	        cookie.setPath("/");
//	        cookie.setSecure(true);
//	        response.addCookie(cookie);

	        // Antes de ir al inicio limpiamos ThreadLocal para evitar memory leaks
	        this.threadInfoDataSecureDTO.clear();

	        model.addAttribute("appVersion", this.versionProperties.getProjectVersion());
	        model.addAttribute("copyrightYear", this.versionProperties.getCopyrightYear());

	        LOGGER.info(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.UD_LOG007, new Object[] {user.getName(), user.getDni(), Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG016)}));
	        return "redirect:/inicio";

	    } catch (final Exception e) {
	        String msgerror;

	        if (e instanceof CertificateException) {
	        	msgerror = e.getMessage();
	        } else if (e instanceof KeyStoreException) {
	        	msgerror = e.getMessage();
	        } else if (e instanceof TimeoutException) {
	        	msgerror = e.getMessage();
	        } else if (e instanceof BadCredentialsException) {
	        	LOGGER.error(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.UD_LOG008, new Object[] {dniRef.get()}));
	        	msgerror = e.getMessage();
	        } else {
	        	LOGGER.error("Error en la autenticacion con certificado; " + e);
	            msgerror = Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML016);
	        }

    		final String randomStringLogin = UtilsStringChar.getRandomStringToLogin();
    		this.threadInfoDataSecureDTO.setRandomStringLogin(randomStringLogin);
    		this.threadInfoDataSecureDTO.setLimitSignGen(new SimpleDateFormat(UtilsDate.FORMAT_DATE_TIME_STANDARD).format(Calendar.getInstance().getTime()));
    		model.addAttribute(LoginService.PARAM_RANDOM_STRING_LOGIN, randomStringLogin);
	        model.addAttribute("errorMessage", msgerror);
	        model.addAttribute("accessByCertificate", true);
	        return "login.html";
	    }
	}

}