package es.gob.fire.server.services;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Clase para la autenticaci&oacute;n de la conexi&oacute;n en el proxy de red por medio del
 * usuario y la contrase&ntilde;a configurado con las propiedades de entorno "http.proxyUser"
 * y "http.proxyPassword".
 */
public class NetworkAuthenticator extends Authenticator {

	private static final String PROP_SUFIX_PROXY_HOST = ".proxyHost"; //$NON-NLS-1$
	private static final String PROP_SUFIX_PROXY_PORT = ".proxyPort"; //$NON-NLS-1$
	private static final String PROP_PROXY_USER = "http.proxyUser"; //$NON-NLS-1$
	private static final String PROP_PROXY_PWD = "http.proxyPassword"; //$NON-NLS-1$

	@Override
	protected RequestorType getRequestorType() {
		return RequestorType.PROXY;
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {

		if (getRequestorType() == RequestorType.PROXY) {
			final String authUser = System.getProperty(PROP_PROXY_USER);
			final String authPassword = System.getProperty(PROP_PROXY_PWD);

			if (authUser != null && authPassword != null) {
				final String prot = getRequestingProtocol().toLowerCase();
				final String host = System.getProperty(prot + PROP_SUFIX_PROXY_HOST, ""); //$NON-NLS-1$
				final String port = System.getProperty(prot + PROP_SUFIX_PROXY_PORT, "0"); //$NON-NLS-1$

				if (getRequestingHost().equalsIgnoreCase(host) && getRequestingPort() == parseInt(port)) {
					return new PasswordAuthentication(authUser, authPassword.toCharArray());
				}
			}
		}
		return null;
	}

	/**
	 * Parsea una cadena para obtener un entero. Si falla, devuelve 0.
	 * @param portText Texto con el entero.
	 * @return Entero proporcionado en forma de cadena o 0 si no era una cadena v&aacute;lida.
	 */
	private static int parseInt(final String portText) {
		try {
			return Integer.parseInt(portText);
		}
		catch (final Exception e) {
			return 0;
		}
	}

	/**
	 * Configura el uso de este autenticador en los proxy de red.
	 */
	public static void configure() {
		final String authUser = System.getProperty(PROP_PROXY_USER);
		final String authPassword = System.getProperty(PROP_PROXY_PWD);

		if (authUser != null && authPassword != null) {
			Authenticator.setDefault(new NetworkAuthenticator());
		}
	}
}
