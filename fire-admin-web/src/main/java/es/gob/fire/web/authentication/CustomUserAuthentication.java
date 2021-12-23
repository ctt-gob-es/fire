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
 * @version 1.0, 22/01/2021.
 */
package es.gob.fire.web.authentication;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.service.IUserService;

/**
 * <p>Class that manages authentication through a spring custom authentication provider.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 22/01/2021.
 */
@Component
public class CustomUserAuthentication implements AuthenticationProvider {

	/** The Constant LOG. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserAuthentication.class);

	/**
	 * Attribute that represents the default charset.
	 */
	private static final String DEFAULT_CHARSET = "utf-8";

	/**
	 * Attribute that represents the md algorithm.
	 */
	private static final String MD_ALGORITHM = "SHA-256";

	/**
	 * Constant attribute that represents the value of the administrator permission.
	 */
	private static final String ROLE_ADMIN_PERMISSON = "1";

	/**
	 * Attribute that represents the user service.
	 */
	@Autowired
	private IUserService userService;

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

			if (!this.userService.isAdminRol(user.getRol().getRolId())) {
				throw new InsufficientAuthenticationException("El usuario " + UtilsStringChar.removeBlanksFromString(userName)
						+ " no tiene permisos de acceso");
			}

			// If password is OK
			if (passwordEncoder().matches(password, user.getPassword())
					|| checkAdminPassword(password, user.getPassword())) {
				final List<GrantedAuthority> grantedAuths = new ArrayList<>();
				// Asignamos los roles del usuario
				// TODO Hacerlo mediante un bucle
				grantedAuths.add(new SimpleGrantedAuthority(Constants.ROLE_ADMIN));
				auth = new UsernamePasswordAuthenticationToken(userName, password,
						/* getAuthorities(user.getRoles()) */grantedAuths);
			} else {
				throw new BadCredentialsException(
						"Las credenciales introducidas no son correctas.");
			}

		} else {
			throw new UsernameNotFoundException(
					"Las credenciales introducidas no son correctas.");
		}
		return auth;
	}

	/**
	 * Method that checks if the password belong to the user.
	 *
	 * @param password
	 *            user password
	 * @param keyAdminB64
	 *            keyAdminB64key admin Base64
	 * @return {@code true} if the password is of the user, {@code false} an
	 *         other case.
	 */
	private static boolean checkAdminPassword(final String password, final String keyAdminB64) {

		boolean result = Boolean.FALSE;
		final byte[] md;
		try {
			md = MessageDigest.getInstance(MD_ALGORITHM).digest(password.getBytes(DEFAULT_CHARSET));
			result = Boolean.TRUE;
			if (keyAdminB64 == null || !keyAdminB64.equals(Base64.encode(md))) {
				LOGGER.error("Se ha insertado una contrasena de administrador no valida"); //$NON-NLS-1$
				result = false;
			}
		} catch (final NoSuchAlgorithmException nsae) {
			LOGGER.error("Error de configuracion en el servicio de administracion. Algoritmo de huella incorrecto", //$NON-NLS-1$
					nsae);
			return false;
		} catch (final UnsupportedEncodingException uee) {
			LOGGER.error("Error de configuracion en el servicio de administracion. Codificacion incorrecta", uee); //$NON-NLS-1$
			return false;
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