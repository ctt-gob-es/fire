package es.gob.fire.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

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
		HttpsConnection conn;

//		final HttpURLConnection testConn = getTestConnection();
//		if (isJSSEConnection(testConn)) {
			conn = new HttpsConnection();
			LOGGER.info("Se usara el gestor de conexiones SSL por defecto de Java"); //$NON-NLS-1$
//		}
//		else if (isWebLogicConnection(testConn)) {
//			try {
//				conn = (HttpsConnection) Class.forName("es.gob.fire.client.weblogic.WebLogicHttpsConnection") //$NON-NLS-1$
//						.getConstructor().newInstance();
//				LOGGER.info("Se usara el gestor de conexiones SSL para servidores WebLogic"); //$NON-NLS-1$
//			} catch (final Throwable e) {
//				LOGGER.warn("Se ha detectado el contexto SSL de un servidor WebLogic, pero no se ha encontrado la dependencia para la compatibilidad con estos servidores (fire-client-weblogic)"); //$NON-NLS-1$
//				conn = new HttpsConnection();
//			}
//		}
//		else {
//			throw new IOException("No se ha identificado el tipo de conexion proporcionado por el servidor"); //$NON-NLS-1$
//		}

		conn.init(config, decipher);

		return conn;
	}

	/**
	 * Identifica si se pueden utilizar las conexiones SSL de JSSE.
	 * @param conn Conexi&oacute;n de ejemplo con la que determinar si se
	 * pueden usar las conexiones por defecto de Java.
	 * @return {@code true} si se puede utilizar una conexi&oacute;n de Java,
	 * {@code false} en caso contrario.
	 */
	private static boolean isJSSEConnection(final HttpURLConnection conn) {
		return conn instanceof HttpsURLConnection;
	}

	/**
	 * Identifica si es necesario utilizar connexiones adaptadas para WebLogic.
	 * @param conn Conexi&oacute;n de ejemplo con la que determinar si son
	 * necesarias las conexiones de WebLogic.
	 * @return {@code true} si se requiere utilizar una conexi&oacute;n para
	 * WebLogic, {@code false} en caso contrario.
	 */
	private static boolean isWebLogicConnection(final HttpURLConnection conn) {

		boolean usingWLSConnectionClass = false;
		try {
			final Class<?> wlsHttpsConnClass = Class.forName("weblogic.net.http.HttpsURLConnection", false, HttpsConnectionFactory.class.getClassLoader()); //$NON-NLS-1$
			if (wlsHttpsConnClass.isInstance(conn)) {
				usingWLSConnectionClass = true;
			}
		} catch (final Exception e) {
			usingWLSConnectionClass = false;
		}
		return usingWLSConnectionClass;
	}

	/**
	 * Crea una conexi&oacute;n HTTPS de prueba.
	 * @return Conexi&oacute;n HTTPS sin abrir.
	 */
	private static HttpURLConnection getTestConnection() {
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) new URL("https://127.0.0.1:443").openConnection(); //$NON-NLS-1$
		}
		catch (final Exception e) {
			conn = null;
		}
		return conn;
	}
}
