/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.log.consumer.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Clase para la lectura y env&iacute;o de datos a URL remotas.
 * @author Carlos Gamuci.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
class HttpManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpManager.class);

	private static final String HTTPS = "https"; //$NON-NLS-1$

	private static final String KEYSTORE = "javax.net.ssl.keyStore"; //$NON-NLS-1$
	private static final String KEYSTORE_PASS = "javax.net.ssl.keyStorePassword"; //$NON-NLS-1$
	private static final String KEYSTORE_TYPE = "javax.net.ssl.keyStoreType"; //$NON-NLS-1$
	private static final String KEYSTORE_DEFAULT_TYPE = "JKS"; //$NON-NLS-1$
	private static final String KEYMANAGER_INSTANCE = "SunX509";//$NON-NLS-1$
	private static final String SSL_CONTEXT = "TLSv1.2";//$NON-NLS-1$

	private static final int BUFFER_SIZE = 4096;

	private static final TrustManager[] DUMMY_TRUST_MANAGER = new TrustManager[] {
		new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			@Override
			public void checkClientTrusted(final X509Certificate[] certs, final String authType) { /* No hacemos nada */ }
			@Override
			public void checkServerTrusted(final X509Certificate[] certs, final String authType) {  /* No hacemos nada */  }

		}
	};
	/**
	 * M&eacute;todo HTTP.
	 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
	 */
	public static enum UrlHttpMethod {
		/** GET. */
		GET,
		/** POST. */
		POST
	}

	/**
	 * Validador de nombres de dominio.
	 */
	private static class CustomHostnameVerifier implements HostnameVerifier {

		private final HostnameVerifier defaultVerifier;
		private final boolean verify;

		public CustomHostnameVerifier(final HostnameVerifier defaultVerifier, final boolean verify) {
			this.defaultVerifier = defaultVerifier;
			this.verify = verify;
		}

		@Override
		public boolean verify(final String hostname, final SSLSession session) {
			if (!this.verify) {
				return true;
			}
			return this.defaultVerifier.verify(hostname, session);
		}
	}

	/** Tiempo de espera por defecto para descartar una conexi&oacute;n HTTP. */
	public static final int DEFAULT_TIMEOUT = -1;

	private boolean disabledSslChecks = false;

	private boolean disabledHostnameVerifier = false;

	private TrustManager[] trustStoreManagers = null;

	static {
		final CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
	}

	protected HttpManager() {
		// Vacio y "protected"
	}

	/**
	 * Realiza una peticion  contra una URL.
	 * @param url Direcci&oacute; a la que realizar la petici&oacute;n.
	 * @param method M&eacute;todo HTTP que utilizar en la llamada.
	 * @return Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error al realizar la llamada.
	 */
	public HttpResponse readUrl(final String url, final UrlHttpMethod method) throws IOException {
		return readUrl(url, method, DEFAULT_TIMEOUT, null, null);
	}

	/**
	 * Realiza una peticion contra una URL.
	 * @param url Direcci&oacute; a la que realizar la petici&oacute;n.
	 * @param method M&eacute;todo HTTP que utilizar en la llamada.
	 * @param timeout Tiempo m&aacute;ximo de espera.
	 * @param contentType Tipo de contenido.
	 * @param accept Formato aceptado.
	 * @return Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error al realizar la llamada.
	 */
	public HttpResponse readUrl(final String url,
						  final UrlHttpMethod method,
						  final int timeout,
			              final String contentType,
			              final String accept
			              ) throws IOException {
		final Properties headers = new Properties();
		if (contentType != null) {
			headers.setProperty("Content-Type", contentType); //$NON-NLS-1$
		}
		if (accept != null) {
			headers.setProperty("Accept", accept); //$NON-NLS-1$
		}
		return readUrl(url, method, timeout, headers);
	}

	/**
	 * Realiza una peticion contra una URL.
	 * @param url Direcci&oacute; a la que realizar la petici&oacute;n.
	 * @param method M&eacute;todo HTTP que utilizar en la llamada.
	 * @param timeout Tiempo m&aacute;ximo de espera.
	 * @param requestProperties Cabeceras que incorporar a la petici&oacute;n.
	 * @return Respuesta de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error al realizar la llamada.
	 */
	public HttpResponse readUrl(final String url,
						  final UrlHttpMethod method,
						  final int timeout,
		                  final Properties requestProperties) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		String urlParameters = null;
		String request = null;
		if (UrlHttpMethod.POST.equals(method)) {
			final StringTokenizer st = new StringTokenizer(url, "?"); //$NON-NLS-1$
			request = st.nextToken();
			if (url.contains("?")) { //$NON-NLS-1$
				urlParameters = st.nextToken();
			}
		}

		final URL uri = new URL(request != null ? request : url);

		final HttpURLConnection conn = openConnection(uri);

		conn.setRequestMethod(method.name());

		if (timeout != DEFAULT_TIMEOUT) {
			conn.setConnectTimeout(timeout);
		}


		// Trabajamos las cabeceras, las por defecto y las que nos indiquen

		final Properties headers = new Properties();
		if (requestProperties != null) {
			headers.putAll(requestProperties);
		}

		if (!headers.containsKey("Accept")) { //$NON-NLS-1$
			conn.addRequestProperty(
				"Accept", //$NON-NLS-1$
				"*/*" //$NON-NLS-1$
			);
		}
		if (!headers.containsKey("Connection")) { //$NON-NLS-1$
			conn.addRequestProperty("Connection", "keep-alive"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!headers.containsKey("Host")) { //$NON-NLS-1$
			conn.addRequestProperty("Host", uri.getHost()); //$NON-NLS-1$
		}
		if (!headers.containsKey("Origin")) { //$NON-NLS-1$
			conn.addRequestProperty("Origin", uri.getProtocol() +  "://" + uri.getHost()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Ponemos el resto de las cabeceras
		for (final Map.Entry<?, ?> entry: headers.entrySet()) {
			conn.addRequestProperty(
				(String) entry.getKey(),
				(String) entry.getValue()
			);
		}

		if (urlParameters != null) {
			conn.setRequestProperty(
				"Content-Length", String.valueOf(urlParameters.getBytes(StandardCharsets.UTF_8).length) //$NON-NLS-1$
			);
			conn.setDoOutput(true);
			try (
				final OutputStream os = conn.getOutputStream();
			) {
				os.write(urlParameters.getBytes(StandardCharsets.UTF_8));
			}
		}

		conn.connect();
		final int resCode = conn.getResponseCode();

		final String statusCode = Integer.toString(resCode);
		final HttpResponse response = new HttpResponse(resCode);
		byte[] content = null;

		if (statusCode.startsWith("4") || statusCode.startsWith("5")) { //$NON-NLS-1$ //$NON-NLS-2$
			content = readStream(conn.getErrorStream());
		}
		else {
			try (final InputStream is = conn.getInputStream(); ) {
				content = readStream(is);
			}
		}

		response.setContent(content);

		return response;

	}

	/**
	 * Abre la conexi&oacute;n a la URL y establece la configuraci&oacute;n SSL
	 * y de proxy necesaria.
	 * @param url Direcci&oacute;n a la cual conectar.
	 * @return Conex&iacute;n.
	 * @throws IOException Cuando ocurre alg&uacute;n problema al abrir la
	 * conexi&oacute;n.
	 */
	private HttpURLConnection openConnection(final URL url) throws IOException {

		HttpURLConnection conn;

		// Abrimos la conexion
		if (isLocal(url)) {
			conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
		}
		else {
			conn = (HttpURLConnection) url.openConnection();
		}

		// Establecemos la seguridad HTTPS si corresponde
		if (url.getProtocol().equals(HTTPS)) {
			// Configuramos la conexion SSL
			try {
				configureSSLSecurity((HttpsURLConnection) conn);
			}
			catch(final Exception e) {
				LOGGER.warn("No se ha podido ajustar la confianza SSL, es posible que no se pueda completar la conexion: " + e); //$NON-NLS-1$
			}

			// Configuramos el verificador de nombre de dominio
			configureHostNameVerifier((HttpsURLConnection) conn);
		}

		return conn;
	}


	private static boolean isLocal(final URL url) {
		if (url == null) {
			throw new IllegalArgumentException("La URL no puede ser nula"); //$NON-NLS-1$
		}
		try {
			return InetAddress.getByName(url.getHost()).isLoopbackAddress();
		}
		catch (final Exception e) {
			LOGGER.warn("Error comprobando si una URL es el bucle local: " + e); //$NON-NLS-1$
			return false;
		}
	}

	private SSLContext sslContext = null;

	/** Configura el almac&eacute;n con el certificado SSL cliente de autenticaci&oacute;n y
	 * los certificados de confianza. En caso de haberse configurado que se aceptan todos los
	 * certificados, se deshabilitar&aacute; la comprobaci&oacute;n de confianza en el
	 * certificado SSL.
	 * @throws KeyManagementException Si hay problemas en la gesti&oacute;n de claves SSL.
	 * @throws NoSuchAlgorithmException Si el JRE no soporta alg&uacute;n algoritmo necesario.
	 * @throws KeyStoreException Si no se puede cargar el KeyStore SSL.
	 * @throws IOException Si hay errores en la carga del fichero KeyStore SSL.
	 * @throws CertificateException Si los certificados del KeyStore SSL son inv&aacute;lidos.
	 * @throws UnrecoverableKeyException Si una clave del KeyStore SSL es inv&aacute;lida. */
	private void configureSSLSecurity(final HttpsURLConnection conn) throws KeyManagementException,
	                                             NoSuchAlgorithmException,
	                                             KeyStoreException,
	                                             UnrecoverableKeyException,
	                                             CertificateException,
	                                             IOException {

		if (this.sslContext == null) {
			this.sslContext = initializeSSLContext();
		}

		conn.setSSLSocketFactory(this.sslContext.getSocketFactory());
		LOGGER.debug("Configurado el TrustManager a medida para las conexiones SSL"); //$NON-NLS-1$
	}

	private SSLContext initializeSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
		final SSLContext sc = SSLContext.getInstance(SSL_CONTEXT);
		KeyManager[] km;
		try {
			km = getKeyManager();
		}
		catch(final Exception e) {
			// En ocasiones, los servidores de aplicaciones establecen configuraciones de KeyStore
			// que no se pueden cargar aqui, y no es algo controlable por las aplicaciones
			LOGGER.error(
				"No ha sido posible obtener el KeyManager con el KeyStore '" + //$NON-NLS-1$
						System.getProperty(KEYSTORE) + "', se usara null", //$NON-NLS-1$
				e
			);
			km = null;
		}
		if (this.trustStoreManagers == null && this.disabledSslChecks) {
			this.trustStoreManagers = DUMMY_TRUST_MANAGER;
		}

		if (km != null || this.trustStoreManagers != null) {
			sc.init(
					km,
					this.trustStoreManagers,
					new java.security.SecureRandom()
					);
		}

		return sc;
	}

	private void configureHostNameVerifier(final HttpsURLConnection conn) {
		conn.setHostnameVerifier(
				new CustomHostnameVerifier(
						conn.getHostnameVerifier(),
						!this.disabledHostnameVerifier));
	}

	/** Devuelve un KeyManager a utilizar cuando se desea deshabilitar las comprobaciones de certificados en las conexiones SSL.
	 * @return KeyManager[] Se genera un KeyManager[] utilizando el keystore almacenado en las propiedades del sistema.
	 * @throws KeyStoreException Si no se puede cargar el KeyStore SSL.
	 * @throws NoSuchAlgorithmException Si el JRE no soporta alg&uacute;n algoritmo necesario.
	 * @throws CertificateException Si los certificados del KeyStore SSL son inv&aacute;lidos.
	 * @throws IOException Si hay errores en la carga del fichero KeyStore SSL.
	 * @throws UnrecoverableKeyException Si una clave del KeyStore SSL es inv&aacute;lida. */

	private static KeyManager[] getKeyManager() throws KeyStoreException,
	                                                   NoSuchAlgorithmException,
	                                                   CertificateException,
	                                                   IOException,
	                                                   UnrecoverableKeyException {
		final String keyStore = System.getProperty(KEYSTORE);
		final String keyStorePassword = System.getProperty(KEYSTORE_PASS);
		final String keyStoreType = System.getProperty(KEYSTORE_TYPE);
		if (keyStore == null || keyStore.isEmpty()) {
			return null;
		}
		final File f = new File(keyStore);
		if (!f.isFile() || !f.canRead()) {
			LOGGER.warn("El KeyStore SSL no existe o no es legible: " + f.getAbsolutePath()); //$NON-NLS-1$
			return null;
		}
		final KeyStore keystore = KeyStore.getInstance(
			keyStoreType != null && !keyStoreType.isEmpty() ? keyStoreType : KEYSTORE_DEFAULT_TYPE
		);
		try (
			final InputStream fis = new FileInputStream(f);
		) {
			keystore.load(
				fis,
				keyStorePassword != null ? keyStorePassword.toCharArray() : null
			);
		}
		final KeyManagerFactory keyFac = KeyManagerFactory.getInstance(KEYMANAGER_INSTANCE);
		keyFac.init(
			keystore,
			keyStorePassword != null ? keyStorePassword.toCharArray() : new char[0]
		);

		return keyFac.getKeyManagers();
	}


    /** Lee un flujo de datos de entrada y los recupera en forma de array de
     * bytes. Este m&eacute;todo consume pero no cierra el flujo de datos de
     * entrada.
     * @param input
     *        Flujo de donde se toman los datos.
     * @return Los datos obtenidos del flujo.
     * @throws IOException
     *         Cuando ocurre un problema durante la lectura */
    private static byte[] readStream(final InputStream input) throws IOException {
        if (input == null) {
            return new byte[0];
        }
        int nBytes;
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((nBytes = input.read(buffer)) != -1) {
            baos.write(buffer, 0, nBytes);
        }
        return baos.toByteArray();
    }

    /**
	 * Establece cu&aacute;l es el almac&eacute;n de confianza para las conexiones SSL.
	 * @param trustStore Almac&eacute;n de confianza ya inicializado.
	 */
	public void setTrustStore(final KeyStore trustStore) {

		TrustManagerFactory trustManagerFactory;
		try {
			trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);
		} catch (final Exception e) {
			LOGGER.warn("No se pudo inicializar el almacen de certificados de confianza para la conexiones SSL", e); //$NON-NLS-1$
			return;
		}
		this.trustStoreManagers = trustManagerFactory.getTrustManagers();
	}

	/**
	 * Permite desactivar la comprobaci&oacute;n de la confianza del
	 * certificado SSL servidor.
	 * @param disable {@code true} para desactivar la comprobaci&oacute;n de
	 * seguridad, {@code false} en caso contrario.
	 */
	public void setDisabledSslChecks(final boolean disable) {
		this.disabledSslChecks = disable;
	}

	/**
	 * Indica si est&aacute; desactivada la comprobaci&oacute;n de la confianza
	 * del certificado SSL servidor.
	 * @return {@code true} si se ha desactivado la comprobaci&oacute;n de
	 * seguridad, {@code false} en caso contrario.
	 */
	public boolean isDisabledSslChecks() {
		return this.disabledSslChecks;
	}

	/**
	 * Permite desactivar la verificaci&oacute;n del nombre de host del
	 * servidor SSL.
	 * @param disable {@code true} para desactivar la comprobaci&oacute;n de
	 * seguridad, {@code false} en caso contrario.
	 */
	public void setDisabledHostnameVerifier(final boolean disable) {
		this.disabledHostnameVerifier = disable;
	}

	/**
	 * Indica si est&aacute; desactivada la verificaci&oacute;n del nombre de
	 * host del servidor SSL.
	 * @param disable {@code true} para desactivar la comprobaci&oacute;n de
	 * seguridad, {@code false} en caso contrario.
	 */
	public boolean isDisabledHostnameVerifier() {
		return this.disabledHostnameVerifier;
	}
}
