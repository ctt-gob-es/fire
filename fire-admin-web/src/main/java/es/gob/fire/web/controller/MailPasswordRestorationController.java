/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * <b>File:</b><p>es.gob.fire.web.controller.MailPasswordRestorationController.java.</p>
 * <b>Description:</b><p>Class that manages the mail password restoration.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.web.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IUserService;
import es.gob.fire.web.authentication.CustomUserAuthentication;
import es.gob.fire.web.mail.MailSenderService;

/**
 * <p>
 * Class that manages the requests related to the mail password restoration.
 * </p>
 * <b>Project:</b>
 * <p>
 * Application for signing documents of @firma suite systems.
 * </p>
 * 
 * @version 1.0, 14/04/2020.
 */
@Controller
public class MailPasswordRestorationController {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(MailPasswordRestorationController.class);

	/**
	 * Attribute that represents the param code.
	 */
	public static final String PARAM_CODE = "code";

	/**
	 * Attribute that represents the user service.
	 */
	@Autowired
	private IUserService userService;

	/**
	 * Attribute that represents the mail sender service.
	 */
	@Autowired
	private MailSenderService mailSenderService;

	/**
	 * Attribute that represents the custom user authentication.
	 */
	@Autowired
	private CustomUserAuthentication customUserAuthentication;

	/**
	 * Method that restores password.
	 * 
	 * @param userNameOrLogin
	 *            user name or email of the user.
	 * @param request
	 *            request.
	 * @return String that represents the name of the view to forward.
	 * @param model
	 *            Holder object for model attributes.
	 * @throws MessagingException
	 *             messaging exception
	 * @throws IOException
	 *             IO exception
	 * @throws FileNotFoundException
	 *             file not found exception
	 * @throws AddressException
	 */
	@RequestMapping(value = "mailpasswordrestoration", method = RequestMethod.POST)
	public String mialRestorePassword(@RequestParam("userNameOrEmail") final String userNameOrEmail, 
			final HttpServletRequest request, final Model model) throws Exception {
		String result = "login.html";
		try {
			User user = userService.getUserByUserNameOrEmail(userNameOrEmail, userNameOrEmail);
			if (user != null /* && user.getEmail() != null */ /*
																 * user tiene
																 * permiso de
																 * adminitrador
																 */) {
				// Generamos el código de restauracion
				final String id = new String();
				final String renovationCode = buildRestorationCode(id);
				// Asociamos el código al ususario y la fecha actual
				user.setRenovationCode(renovationCode);
				user.setRenovationDate(new Date());
				userService.saveUser(user);

				// Construimos la URL para restaurar la contraseña
				// Para evitar errores recuperando el parámetro code,
				// parseamos los símbolos más antes de enviar la URL al usuario
				String renovationCodeURL = renovationCode.replace("+", "%2B");
				final String restorationUrl = getRestorationPageUrl(request, renovationCodeURL);

				// Enviamos el email
				mailSenderService.sendEmail(user, restorationUrl);
				model.addAttribute("mailsuccess", Boolean.TRUE);
				model.addAttribute("mailSuccessMessage", "El correo se ha enviado correctamente");

			} else {
				model.addAttribute("mailerror", Boolean.TRUE);
				model.addAttribute("mailErrorMessage", "El usuario indicado no se encuentra registrado en el sistema");
				result = "mailpasswordrestoration.html";
			}
		} catch (IOException | MessagingException e) {
			model.addAttribute("mailErrorMessage", "No ha sido posible enviar el correo");
			result = "mailpasswordrestoration.html";
		}
		return result;
	}

	/**
	 * Method that checks the renovation code and send to the user to restoration page.
	 * @param code code
	 * @param model model
	 * @return restoration page
	 */
	@GetMapping("/mailRestorePasswordUser")
	public String mailRestorePasswordUser(@RequestParam("code") String code, Model model, HttpSession session) {
		String result = "restorepassword.html";
		boolean error = Boolean.FALSE;
		User user = null;
		try {
			if (code == null) {
				LOGGER.warn("El código no ha sido encontrado o es nulo");
				model.addAttribute("restoreerror", Boolean.TRUE);
				model.addAttribute("restoreErrorMessage", "El código no ha sido encontrado o es nulo");
				result = "login.html";
			} else {
				user = userService.getUserByRenovationCode(code);
				if (user == null) {
					LOGGER.warn("El usuario no ha sido encontrado");
					error = Boolean.TRUE;
					model.addAttribute("restoreerror", Boolean.TRUE);
					model.addAttribute("restoreErrorMessage", "El usuario no ha sido encontrado");
					result = "login.html";
				}

				// Si el usuario tiene datos a nulo en base de datos se va fuera
				if (user.getRenovationCode() == null || user.getRenovationDate() == null && !error) {
					LOGGER.warn("El usuario no tenia registrada la informacion de restauración de contraseña");
					error = Boolean.TRUE;
					model.addAttribute("restoreerror", Boolean.TRUE);
					model.addAttribute("restoreErrorMessage",
							"El usuario no tenia registrada la información de restauracion de contraseña");
					result = "login.html";
				}

				// Comprobar que el usuario tiene asignado en BD el codigo
				if (!user.getRenovationCode().equals(code) && !error) {
					LOGGER.warn(
							"No se han proporcionado el identificador del usuario o no se han podido recuperar sus datos");
					error = Boolean.TRUE;
					model.addAttribute("restoreerror", Boolean.TRUE);
					model.addAttribute("restoreErrorMessage",
							"No se han proporcionado el identificador del usuario o no se han podido recuperar sus datos");
					result = "login.html";
				}

				// Si se ha excedido el tiempo de espera, no permitimos la
				// renovacion
				if (!error) {
					final long currentTime = new Date().getTime();
					final long renovationTime = user.getRenovationDate().getTime();
					final int expirationTime = mailSenderService.getMailPasswordExpiration() != null
							? Integer.parseInt(mailSenderService.getMailPasswordExpiration())
							: MailSenderService.DEFAULT_EXPIRED_TIME;

					if (currentTime > renovationTime + expirationTime) {
						LOGGER.warn("Se ha excedido el tiempo máximo de espera hasta la renovación de la contraseña");
						error = Boolean.TRUE;
						model.addAttribute("restoreerror", Boolean.TRUE);
						model.addAttribute("restoreErrorMessage",
								"Se ha excedido el tiempo máximo de espera hasta la renovación de la contraseña");
						result = "login.html";
					}
				}

				if (!error) {
					// Actualizar verdadero el tiempo de la nueva url
					user.setRestPassword(Boolean.TRUE);
					userService.saveUser(user);
					session.setAttribute("restoreUserId", user.getUserId());
					session.setAttribute("restoreUserCode", code);
					model.addAttribute("username", user.getUserName());
				}
			}

		} catch (Exception e) {
			LOGGER.error("Se ha producido un error reestableciendo la contraseña de usuario", e);
			model.addAttribute("restoreerror", Boolean.TRUE);
			model.addAttribute("restoreErrorMessage",
					"Se ha producido un error reestableciendo la contraseña de usuario");
			result = "login.html";
		}

		return result;
	}

	/**
	 * Method that restores password.
	 * @param restoreUserId user id
	 * @param restoreUserCode code
	 * @param username user name
	 * @param newPassword new password
	 * @param repeatNewPassword repeat new password
	 * @return String that represents the name of the view to forward.
	 * @param model Holder object for model attributes.
	 * @throws Exception exception
	 */
	@RequestMapping(value = "restorepassword", method = RequestMethod.POST)
	public String restorePassword(@RequestParam("restoreUserId") final Long restoreUserId, @RequestParam("restoreUserCode") final String restoreUserCode,
			@RequestParam("username") final String username, @RequestParam("newPassword") final String newPassword,
			@RequestParam("repeatNewPassword") final String repeatNewPassword, final HttpServletRequest request,
			final Model model) throws Exception {
		String result = "login.html";
		try {
			boolean error = Boolean.FALSE;
			User user = userService.getUserByUserName(username);

			// Comprobamos que el usuario es el que realmente nos ha realizado
			// la peticion
			if (user == null || !user.getUserId().equals(restoreUserId)) {
				LOGGER.warn("El id de usuario es distinto al utilizado en la sesión");
				error = Boolean.TRUE;
				model.addAttribute("restorepassworderror", Boolean.TRUE);
				model.addAttribute("restorePasswordErrorMessage",
						"Se ha excedido el tiempo máximo de espera hasta la renovación de la contraseña");
			}

			// Comprobamos que el código del usuario es el que realmente nos ha
			// realizado la peticion
			if (restoreUserCode == null || !user.getRenovationCode().equals(restoreUserCode) && !error) {
				LOGGER.warn("El usuario no ha sido encontrado o es nulo");
				error = Boolean.TRUE;
				model.addAttribute("restorepassworderror", Boolean.TRUE);
				model.addAttribute("restorePasswordErrorMessage", "El usuario no ha sido encontrado o es nulo");
			}

			if (!error) {
				// Comprobamos que el enlace no ha caducado
				final int expirationTime = mailSenderService.getMailPasswordExpiration() != null
						? Integer.parseInt(mailSenderService.getMailPasswordExpiration())
						: MailSenderService.DEFAULT_EXPIRED_TIME;

				if (new Date().getTime() > user.getRenovationDate().getTime() + expirationTime) {
					LOGGER.warn("Se ha excedido el tiempo máximo de espera hasta la renovación de la contraseña");
					error = Boolean.TRUE;
					model.addAttribute("restorepassworderror", Boolean.TRUE);
					model.addAttribute("restorePasswordErrorMessage",
							"Se ha excedido el tiempo máximo de espera hasta la renovación de la contraseña");
				}
			}

			// Comprobamos que las passwords insertadas sean iguales
			if (newPassword != null && repeatNewPassword != null && !newPassword.equals(repeatNewPassword) && !error) {
				LOGGER.warn("Clave nueva y repetir clave nueva, deben ser iguales");
				error = Boolean.TRUE;
				model.addAttribute("restorepassworderror", Boolean.TRUE);
				model.addAttribute("restorePasswordErrorMessage",
						"Clave nueva y repetir clave nueva, deben ser iguales");
				result = "restorepassword.html";
			}

			if (!error) {
				// Codificamos la nueva clave y actualizamos el usuario
				String passwordEnconde = customUserAuthentication.passwordEncoder().encode(newPassword);
				user.setPassword(passwordEnconde);
				userService.saveUser(user);
				// Todo OK
				model.addAttribute("restorepasswordsuccess", Boolean.TRUE);
			}
			
		} catch (Exception e) {
			LOGGER.error("No ha sido posible restaurar la clave del usuario", e);
			model.addAttribute("restorepassworderror", Boolean.TRUE);
			model.addAttribute("restorePasswordErrorMessage",
					"No ha sido posible restaurar la clave del usuario");
		}
		return result;
	}

	/**
	 * Construye un codigo de restauracion de contrase&ntilde;a.
	 * 
	 * @param id
	 *            Identificador del usuario que solicita la restauraci&oacute;n.
	 * @return C&oacute;digo de restauraci&oacute;n en base64.
	 */
	private static String buildRestorationCode(final String id) {
		final UUID uuid = UUID.randomUUID();
		final byte[] code = uuid.toString().getBytes();

		final SecureRandom random = new SecureRandom();
		final byte[] salt = new byte[16];
		random.nextBytes(salt);

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.error("Algoritmo de cifrado no soportado: " + e);
			throw new RuntimeException("Error grave: Algoritmo interno de cifrado no soportado", e);
		}
		md.update(salt);
		md.update(id.getBytes());
		md.update(code);

		return Base64.encode(md.digest());
	}

	/**
	 * Method that build the restoration URL.
	 * 
	 * @param request
	 *            request
	 * @param renovationCode
	 *            renewal code to restoration password
	 * @return String with the restoration URL
	 */
	private static String getRestorationPageUrl(final HttpServletRequest req, final String renovationCode) {
		return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
				+ req.getRequestURI().substring(0, req.getRequestURI().indexOf('/', 1)) + "/mailRestorePasswordUser"
				+ "?" + PARAM_CODE + "=" + renovationCode;
	}

}
