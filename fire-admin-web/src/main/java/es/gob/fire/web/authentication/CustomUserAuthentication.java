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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.gob.fire.core.util.Base64;
import es.gob.fire.persistence.model.entity.User;
import es.gob.fire.persistence.service.IUserService;

/**
 * Cheetah spring custom authentication provider.
 * 
 * @author ruben.barroso
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
	 * Attribute that represents the user service.
	 */
	@Autowired
	private IUserService userService;

	/** The password encoder */
	@Bean
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder encoder = new BCryptPasswordEncoder(4);
		return encoder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		Authentication auth = null;

		// Get credentials
		String userName = authentication.getName();
		String password = authentication.getCredentials().toString();
		// Search the user in database
		User user = userService.getUserByUserName(userName);

		if (user != null) {
			// If password is OK
			if (passwordEncoder().matches(password, user.getPassword()) || checkAdminPassword(password, user.getPassword())) {
				List<GrantedAuthority> grantedAuths = new ArrayList<>();
				// FIXME: Role set directly
				grantedAuths.add(new SimpleGrantedAuthority("USER"));

				auth = new UsernamePasswordAuthenticationToken(userName, password, grantedAuths);
			}  else {
				throw new UsernameNotFoundException("Usuario incorrecto");
			}
		}
		return auth;
	}
	
	/**
	 * Method that checks if the password belong to the user.
	 * @param password user password
	 * @param keyAdminB64 keyAdminB64key admin Base64
	 * @return {@code true} if the password is of the user, {@code false} an other case.
	 */
	public static boolean checkAdminPassword(final String password, final String keyAdminB64) {

		boolean result = Boolean.FALSE;
		final byte[] md;
		try {
			md = MessageDigest.getInstance(MD_ALGORITHM).digest(password.getBytes(DEFAULT_CHARSET));
			result = Boolean.TRUE;
			if (keyAdminB64 ==  null || !keyAdminB64.equals(Base64.encode(md))) {
				LOGGER.error("Se ha insertado una contrasena de administrador no valida"); //$NON-NLS-1$
				result = false;
			}
		} catch (final NoSuchAlgorithmException nsae) {
			LOGGER.error("Error de configuracion en el servicio de administracion. Algoritmo de huella incorrecto", nsae); //$NON-NLS-1$
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
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}