package es.gob.log.register;

import java.io.IOException;
import java.util.Properties;

/**
 * Conector para el registro de servicios de log en un sistema.
 * @version 1.0, 18/06/2019.
 */
public interface LogServiceRegister {

	/**
	 * Configura el punto de entrada del servicio de registro.
	 *
	 * @param url URL del servicio de registro.
	 */
	void setServiceUrl(String url);

	/**
	 * Configura la informaci&oacute;n para la autenticaci&oacute;n en el servicio.
	 *
	 * @param authenticationInfo Informaci&oacute;n para la autenticaci&oacute;n en el servicio.
	 */
	void setAuthenticationInfo(Object authenticationInfo);

	/**
	 * Proporciona al conector todas las propiedades heredadas del servicio de registro.
	 *
	 * @param config Propiedades de configuraci&oacute;n heredadas del servicio de registro.
	 */
	void setConfig(Properties config);

	/**
	 * Registra el nodo en un servicio.
	 *
	 * @throws IOException Cuando ocurre alg&uacute;n error en la conexi&oacute;n con el servicio.
	 * @throws RegistrationException Cuando el servidor rechaza el registro del servicio.
	 */
	void registry()
			throws IOException, RegistrationException;
}
