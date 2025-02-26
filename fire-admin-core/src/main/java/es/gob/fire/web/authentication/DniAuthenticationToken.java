package es.gob.fire.web.authentication;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class DniAuthenticationToken extends AbstractAuthenticationToken {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6336340094412147890L;
	
	private final String dni;

    public DniAuthenticationToken(String dni) {
        super(null); // No requiere credenciales
        this.dni = dni;
        setAuthenticated(false);
    }

    public DniAuthenticationToken(String dni, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.dni = dni;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null; // No hay contraseña
    }

    @Override
    public Object getPrincipal() {
        return dni; // El DNI actúa como identificador
    }
}
