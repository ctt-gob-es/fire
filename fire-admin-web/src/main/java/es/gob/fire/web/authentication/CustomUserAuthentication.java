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
 * <b>File:</b><p>es.gob.fire.web.authentication.CustomUserAuthentication.java.</p>
 * <b>Description:</b><p>Class that manages authentication through a spring custom authentication provider.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>22/01/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 22/01/2025.
 */
package es.gob.fire.web.authentication;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.UserLoggedDTO;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.permissions.Permissions;
import es.gob.fire.persistence.permissions.PermissionsChecker;
import es.gob.fire.persistence.service.IUserService;

/**
 * <p>Class that manages authentication through a spring custom authentication provider.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.2, 22/01/2025.
 */
@Component
public class CustomUserAuthentication implements AuthenticationProvider {

	/** The Constant LOG. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserAuthentication.class);

	/**
	 * Attribute that represents the default charset.
	 */
	private static final String DEFAULT_CHARSET = "utf-8"; //$NON-NLS-1$

	/**
	 * Attribute that represents the md algorithm.
	 */
	private static final String MD_ALGORITHM = "SHA-256"; //$NON-NLS-1$

	/**
	 * Constant attribute that represents the value of the administrator permission.
	 */
	private static final String ROLE_ADMIN_PERMISSON = "1"; //$NON-NLS-1$

	/**
	 * Attribute that represents the user service.
	 */
	@Autowired
	private IUserService userService;
	
	/**
	 * Attribute that represents the user logged in platform.
	 */
	@Autowired
	private UserLoggedDTO userLoggedDTO;
	
	/** The password encoder */
	@Bean
	public PasswordEncoder passwordEncoder() {
		final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
		return encoder;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

		Authentication auth = null;

		// Get credentials
		final String userName = authentication.getName();
		final String password = authentication.getCredentials().toString();
		// Search the user in database
		final User user = this.userService.getUserByUserName(userName);

		if (user != null) {

			if (!PermissionsChecker.hasPermission(user, Permissions.ACCESS)) {
				LOGGER.error("El usuario {} no tiene permisos de acceso", UtilsStringChar.removeBlanksFromString(userName)); //$NON-NLS-1$
				throw new InsufficientAuthenticationException("El usuario " + UtilsStringChar.removeBlanksFromString(userName) //$NON-NLS-1$
						+ " no tiene permisos de acceso"); //$NON-NLS-1$
			}

			// If password is OK
			if (password.equals(user.getPassword()) 
					|| passwordEncoder().matches(password, user.getPassword())
					|| checkAdminPassword(user.getUserName(), password, user.getPassword())) {
				final List<GrantedAuthority> grantedAuths = new ArrayList<>();
				// Asignamos los roles del usuario
				// TODO Hacerlo mediante un bucle
				grantedAuths.add(new SimpleGrantedAuthority(Constants.ROLE_ADMIN));
				auth = new UsernamePasswordAuthenticationToken(userName, password,
						/* getAuthorities(user.getRoles()) */grantedAuths);
				
				// Asignamos al bean de spring del usuario para usarlo en la app
				userLoggedDTO.setDni(user.getDni());
				userLoggedDTO.setEmail(user.getEmail());
				userLoggedDTO.setIdRol(user.getRol().getRolId());
				userLoggedDTO.setName(user.getName());
				userLoggedDTO.setPassword(user.getPassword());
				userLoggedDTO.setPhone(user.getPhone());
				userLoggedDTO.setRenovationCode(user.getRenovationCode());
				userLoggedDTO.setRenovationDate(user.getRenovationDate() == null ? null : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(user.getRenovationDate()));
				userLoggedDTO.setRestPassword(user.getRestPassword());
				userLoggedDTO.setRoot(user.getRoot());
				userLoggedDTO.setStartDate(user.getStartDate() == null ? null : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(user.getStartDate()));
				userLoggedDTO.setSurnames(user.getSurnames());
				userLoggedDTO.setUserId(user.getUserId());
				userLoggedDTO.setUserName(user.getUserName());
				userLoggedDTO.setFecUltimoAcceso(user.getFecUltimoAcceso() == null ? null : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(user.getFecUltimoAcceso()));
				
				// Actualizamos la fecha de último acceso
				user.setFecUltimoAcceso(Calendar.getInstance().getTime());
				userService.saveUser(user);
			} else {
				LOGGER.error("El usuario {} inserto una constrasena incorrecta", UtilsStringChar.removeBlanksFromString(userName)); //$NON-NLS-1$
				throw new BadCredentialsException(
						"Las credenciales introducidas no son correctas."); //$NON-NLS-1$
			}

		} else {
			LOGGER.error("El usuario {} no existe en el sistema", UtilsStringChar.removeBlanksFromString(userName)); //$NON-NLS-1$
			throw new UsernameNotFoundException(
					"Las credenciales introducidas no son correctas."); //$NON-NLS-1$
		}
		return auth;
	}

	/**
	 * Method that checks if the password belong to the user.
	 *@param userName
	 *            user id
	 * @param password
	 *            user password
	 * @param keyAdminB64
	 *            keyAdminB64key admin Base64
	 * @return {@code true} if the password is of the user, {@code false} an
	 *         other case.
	 */
	private static boolean checkAdminPassword(final String userName, final String password, final String keyAdminB64) {

		boolean result = false;
		final byte[] md;
		try {
			md = MessageDigest.getInstance(MD_ALGORITHM).digest(password.getBytes(DEFAULT_CHARSET));
			if (keyAdminB64 != null && keyAdminB64.equals(Base64.encode(md))) {
				result = true;
			}
			else {
				LOGGER.error("Se ha insertado una contrasena no valida para el usuario {}", UtilsStringChar.removeBlanksFromString(userName)); //$NON-NLS-1$
				result = false;
			}
		} catch (final NoSuchAlgorithmException nsae) {
			LOGGER.error("Error de configuracion en el servicio de administracion. Algoritmo de huella incorrecto", //$NON-NLS-1$
					nsae);
		} catch (final UnsupportedEncodingException uee) {
			LOGGER.error("Error de configuracion en el servicio de administracion. Codificacion incorrecta", uee); //$NON-NLS-1$
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * supports(java.lang.Class)
	 */
	@Override
	public boolean supports(final Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}