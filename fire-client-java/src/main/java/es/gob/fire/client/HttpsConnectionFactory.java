package es.gob.fire.client;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsConnectionFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpsConnectionFactory.class);

	/**
	 * Obtiene una conexi&oacute;n Http/Https.
	 * @param config Opciones de configuraci&oacute;n.
	 * @param decipher Descifrador encargado de descifrar las contrase&ntilde;as de
	 * los almacenes de claves y certificados de confianza.
	 * @return Devuelve la conexi&oacute;n configurada.
	 * @throws IllegalArgumentException Cuando se configura un fichero de almac&eacute;n que no existe.
	 * @throws GeneralSecurityException Cuando se produce un error en la configuraci&oacute;n de la conexi&oacute;n.
	 * @throws IOException Cuando se produce un error en la conexi&oacute;n con el servidor remoto.
	 */
	public static HttpsConnection getConnection(final Properties config, final PasswordDecipher decipher) throws IllegalArgumentException,
																		GeneralSecurityException,
																		IOException {
		final HttpsConnection conn = new HttpsConnection();
		LOGGER.info("Se usara el gestor de conexiones SSL por defecto de Java"); //$NON-NLS-1$

		conn.init(config, decipher);

		return conn;
	}
}
