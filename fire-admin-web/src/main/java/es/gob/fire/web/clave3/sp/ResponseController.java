/*
/*******************************************************************************
 * Copyright (C) 2024 Secretaria General de la Administracion Digital, Gobierno de Espana
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

package es.gob.fire.web.clave3.sp;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IUserService;
import es.gob.fire.service.ILoginService;
import es.gob.fire.web.config.VersionProperties;

/**
 * <p>Class that processes the SAML response that Proxy2 sends to the Kit.</p>
 * <b>Project:</b><p>Citizen identification and authentication platform.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 29/10/2025.
 */
@Controller
public class ResponseController {

    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger(ResponseController.class);

	@Autowired
	private IUserService iUserService;

	@Autowired
	private ILoginService iLoginService;

	@Autowired
    private VersionProperties versionProperties;

	/**
	 * Function that processes the SAML response that Proxy2 sends to the Kit.
	 * It recive the SAML response from de login requests and de logout requests.
	 * @param response
	 * @param model
	 * @return Class that processes the SAML response that Proxy2 sends to the Kit.
	 * @throws Exception
	 */
	@PostMapping("/ResponseClave")
	public String returnAction(@ModelAttribute final ResponseSAML response, final Model model) throws Exception {
		model.addAttribute("response", new ResponseSAML()); //$NON-NLS-1$

		LOGGER.info("response.relayState: " + response.getRelayState()); //$NON-NLS-1$
		LOGGER.info("response.SAMLResponse: " + response.getSAMLResponse()); //$NON-NLS-1$
		LOGGER.info("response.logoutResponse: " + response.getLogoutResponse()); //$NON-NLS-1$

		String returnUrl;

		// Si la respuesta incluye el SAML de autenticacion, procesamos el login de usuario
		if (response.getSAMLResponse() != null && !response.getSAMLResponse().trim().isEmpty()) {
			returnUrl = processLoginResponse(response, model);
		}
		// Si la respuesta incluye una solicitud de desconexion, anulamos el login del usuario
		else if (response.getLogoutResponse() != null && !response.getLogoutResponse().trim().isEmpty()) {
			returnUrl = processLogoutResponse(response, model);
		}
		// En caso contrario, se trata de una peticion invalida
		else {
			returnUrl = processInvalidResponse(response, model);
		}

		return returnUrl;
	}

	private static DocumentBuilderFactory DOC_BUILDER_FACTORY = null;

	private static void initDocumentBuilderFactory() throws ParserConfigurationException {
		if (DOC_BUILDER_FACTORY == null) {
			try {
				DOC_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
				DOC_BUILDER_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$

				// Declaramos las propiedades de seguridad para evitar ataques XXE. En caso de que
				// el parser no soporte alguna de estas propiedades, se captura la excepcion y se
				// continua con el proceso, aunque se muestra un mensaje de advertencia en el log
				final String[] securityAttrs = {
						"http://javax.xml.XMLConstants/property/accessExternalDTD", //$NON-NLS-1$
						"http://javax.xml.XMLConstants/property/accessExternalSchema", //$NON-NLS-1$
						"http://javax.xml.XMLConstants/property/accessExternalStylesheet" }; //$NON-NLS-1$
				for (final String attr : securityAttrs) {
					try {
						DOC_BUILDER_FACTORY.setAttribute(attr, ""); //$NON-NLS-1$
					} catch (final IllegalArgumentException e) {
						LOGGER.warn("No se ha podido establecer la propiedad de seguridad " + attr + " en el DocumentBuilderFactory: " + e); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				DOC_BUILDER_FACTORY.setNamespaceAware(true);
			}
			catch (final ParserConfigurationException e) {
				DOC_BUILDER_FACTORY = null;
				throw e;
			}
		}
	}

	/**
	 * Procesa una respuesta de login.
	 * @param response Respuesta recibida de Clave.
	 * @param model Modelo en el que registrar los datos.
	 * @return Plantilla de retorno.
	 */
	private String processLoginResponse(final ResponseSAML response, final Model model) {

		// Se decodifica la respuesta SAML recibida en Base64
		final byte[ ] decodedBytes = Base64.getDecoder().decode(response.getSAMLResponse());

		try {
			// Se inicializa la configuracion de OpenSaml
			InitializationService.initialize();

			// Se inicializa la factoria para el procesado del XML de respuesta
			initDocumentBuilderFactory();
		}
		catch (final Exception e) {
			LOGGER.error("No se pudo inicializar el servicio para el procesado de la respuesta de Clave", e); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG017)); //$NON-NLS-1$
			return "login"; //$NON-NLS-1$
		}

		Element element;
		try {
			final DocumentBuilder builder = DOC_BUILDER_FACTORY.newDocumentBuilder();
			final Document document = builder.parse(new ByteArrayInputStream(decodedBytes));
			element = document.getDocumentElement();
		} catch (final Exception e) {
			LOGGER.error("Ocurrio un error al procesar el XML de la respuesta de Clave", e); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG004)); //$NON-NLS-1$
			return "login"; //$NON-NLS-1$
		}

		// Se lleva a cabo la conversion del documento XML en el objeto SAML Response
		XMLObject xmlObject;
		try {
			final UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			final Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			xmlObject = unmarshaller.unmarshall(element);
			if (!(xmlObject instanceof Response)) {
				throw new IllegalArgumentException("El XML recibido no se corresponde con una respuesta SAML"); //$NON-NLS-1$
			}
		}
		catch (final Exception e) {
			LOGGER.error("La respuesta de Clave no es valida", e); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009)); //$NON-NLS-1$
			return "login"; //$NON-NLS-1$
		}

		final Response samlResponse = (Response) xmlObject;
		// Se obtiene la informacion del RelayState desde la respuesta
		// SAML para verificar que coincide con el que genero durante el
		// envio para asi verificar su integridad.
		final String prevRelayState = SessionHolder.sessionsSAML.get(samlResponse.getInResponseTo());
		SessionHolder.sessionsSAML.clear();
		// Si no coincide el RelayState se detiene el proceso y se
		// muestra un mensaje de error al usuario.
		if (prevRelayState == null || !prevRelayState.equals(response.getRelayState())) {
			LOGGER.error("Ha habido un error de verificacion del RelayState para la respuesta de tipo samlResponse recibida."); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG003)); //$NON-NLS-1$
			return "login"; //$NON-NLS-1$
		}

		// Comprobamos si la respuesta de autenticacion desde Pasarela no es exitosa
		if (!samlResponse.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS)) {
			String errorMessage = "Se ha producido un error no controlado durante la peticion de autenticacion"; //$NON-NLS-1$
			if (samlResponse.getStatus() != null && samlResponse.getStatus().getStatusMessage() != null) {
				errorMessage = samlResponse.getStatus().getStatusMessage().getMessage();
			}
			LOGGER.error("Error de autenticacion devuelto por Pasarela: " + errorMessage); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG005)); //$NON-NLS-1$
			return "login"; //$NON-NLS-1$
		}

		// Eliminamos intentos fallidos de acceso para todas las ip
		this.iLoginService.deleteAllControlAccess();

		// Obtenemos la informacion del usuario
		final PersonalInfoBean personalInfoBean = obtenerDatosUsuario(samlResponse);

		// Buscamos al usuario en la base de datos
		final Iterable<User> allUsers = this.iUserService.getAllUser();
		final String dni = personalInfoBean.getDni();

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

		model.addAttribute("appVersion", this.versionProperties.getProjectVersion()); //$NON-NLS-1$
		model.addAttribute("copyrightYear", this.versionProperties.getCopyrightYear()); //$NON-NLS-1$

		return "inicio"; //$NON-NLS-1$
	}

	/**
     * Retrieves user data from the provided SAML response.
	 * @param samlResponse SAML response.
     * @return A {@link PersonalInfoBean} object containing the user's personal information.
     * @throws SecurityException If the SAML response is invalid or the relay state does not match.
     */
    public static PersonalInfoBean obtenerDatosUsuario(final Response samlResponse) {

    	// Autenticacion correcta
    	// Se recorre el objeto Response para extraer toda la informacion
    	// recibida desde pasarela
    	final Map<String, String> map = new HashMap<>();
    	for (final Assertion assertion: samlResponse.getAssertions()) {
    		assertion.getAttributeStatements().forEach(attrStatement -> {
    			attrStatement.getAttributes().forEach(attribute -> {
    				final List<XMLObject> values = attribute.getAttributeValues();
    				if (values != null && !values.isEmpty()) {
    					map.put(attribute.getFriendlyName(), values.iterator().next().getDOM().getTextContent());
    				}
    			});
    		});
    	}

    	// Generamos un token unico asociado al usuario
		final String infoToken = SecureRandomXmlIdGenerator.generateIdentifier(8);

		// Recogemos la informacion del usuario que nos resulta de interes
    	final PersonalInfoBean personalInfo = new PersonalInfoBean();
    	personalInfo.setNombre(map.get("FirstName")); //$NON-NLS-1$
    	personalInfo.setApellidos(map.get("FamilyName")); //$NON-NLS-1$
    	personalInfo.setDni(map.get("PersonIdentifier")); //$NON-NLS-1$
    	personalInfo.setInfoToken(infoToken);

    	return personalInfo;
	}

	/**
	 * Procesa una respuesta de logout.
	 * @param response Respuesta recibida de Clave.
	 * @param model Modelo en el que registrar los datos.
	 * @return Plantilla de retorno.
	 */
	private static String processLogoutResponse(final ResponseSAML response, final Model model) {

		// Se decodifica la respuesta SAML recibida en Base64
		final byte[ ] decodedBytes = Base64.getDecoder().decode(response.getLogoutResponse());

		try {
			// Se inicializa la configuracion de OpenSaml
			InitializationService.initialize();

			// Se inicializa la factoria para el procesado del XML de respuesta
			initDocumentBuilderFactory();
		}
		catch (final Exception e) {
			LOGGER.error("No se pudo inicializar el servicio para el procesado de la respuesta de logout de Clave", e); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG017)); //$NON-NLS-1$
			return "login"; //$NON-NLS-1$
		}

		Element element;
		try {
			final DocumentBuilder builder = DOC_BUILDER_FACTORY.newDocumentBuilder();
			final Document document = builder.parse(new ByteArrayInputStream(decodedBytes));
			element = document.getDocumentElement();
		} catch (final Exception e) {
			LOGGER.error("Ocurrio un error al procesar el XML de la respuesta de logout de Clave", e); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG004)); //$NON-NLS-1$
			return "login"; //$NON-NLS-1$
		}

		// Se lleva a cabo la conversion del documento XML en el objeto SAML LogoutResponse
		XMLObject xmlObject;
		try {
			final UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			final Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			xmlObject = unmarshaller.unmarshall(element);
			if (!(xmlObject instanceof LogoutResponse)) {
				throw new IllegalArgumentException("El XML recibido no se corresponde con una respuesta de logout de Clave"); //$NON-NLS-1$
			}
		}
		catch (final Exception e) {
			LOGGER.error("La respuesta de logout de Clave no es valida", e); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG009)); //$NON-NLS-1$
			return "login"; //$NON-NLS-1$
		}

		final LogoutResponse logoutResponse = (LogoutResponse) xmlObject;
		// Se obtiene la informacion del RelayState desde la respuesta
		// SAML para verificar que coincide con el que genero durante el
		// envio para asi verificar su integridad.
		final String prevRelayState = SessionHolder.sessionsSAML.get(logoutResponse.getInResponseTo());
		SessionHolder.sessionsSAML.clear();
		// Si no coincide el RelayState se detiene el proceso y se
		// muestra un mensaje de error al usuario.
		if (prevRelayState == null || !prevRelayState.equals(response.getRelayState())) {
			LOGGER.error("Ha habido un error de verificacion del RelayState para la respuesta de tipo logoutResponse recibida"); //$NON-NLS-1$
			model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG003)); //$NON-NLS-1$
		}

		// Se redirecciona al usuario a la pantalla de login
		return "login"; //$NON-NLS-1$
	}

	/**
	 * Procesa una respuesta err&oacute;nea.
	 * @param response Respuesta recibida de Clave.
	 * @param model Modelo en el que registrar los datos.
	 * @return Plantilla de retorno.
	 */
	private static String processInvalidResponse(final ResponseSAML response, final Model model) {
		LOGGER.error("Se ha recibido una respuesta vacia"); //$NON-NLS-1$
		model.addAttribute("errorMessage", Language.getResWebAdminGeneral(IWebAdminGeneral.UD_LOG002)); //$NON-NLS-1$
		return "login"; //$NON-NLS-1$
	}
}
