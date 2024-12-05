/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.Properties;

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

/**
 * Clase de conexi&oacute;n mediante SSL.
 */
public class HttpsConnection {

	private static final String KEYSTORE_PROPERTY = "javax.net.ssl.keyStore"; //$NON-NLS-1$

    private static final String KEYSTORE_PASS_PROPERTY = "javax.net.ssl.keyStorePassword"; //$NON-NLS-1$

    private static final String KEYSTORE_ALIAS_PROPERTY = "javax.net.ssl.certAlias"; //$NON-NLS-1$

    private static final String KEYSTORE_TYPE_PROPERTY = "javax.net.ssl.keyStoreType"; //$NON-NLS-1$

    private static final String TRUSTSTORE_PROPERTY = "javax.net.ssl.trustStore"; //$NON-NLS-1$

    private static final String TRUSTSTORE_PASS_PROPERTY = "javax.net.ssl.trustStorePassword"; //$NON-NLS-1$

    private static final String TRUSTSTORE_TYPE_PROPERTY = "javax.net.ssl.trustStoreType"; //$NON-NLS-1$

    private static final String VERIFY_HOSTNAMES_PROPERTY = "verify.hostnames";  //$NON-NLS-1$

    protected static final String ACCEPT_ALL_CERTS_VALUE = "all"; //$NON-NLS-1$

    protected static final String ACCEPT_DEFAULT_CERTS_VALUE = "default"; //$NON-NLS-1$

	private static final String CONTENT_TYPE_SEPARATOR = ";"; //$NON-NLS-1$
	private static final String CHARSET_PREFIX = "charset="; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsConnection.class);

	/**
	 * M&eacute;todo HTTP soportados.
	 */
	public enum Method {
		/** M&eacute;todo HTTP para la recuperaci&oacute;n de datos remotos. */
		GET,
		/** M&eacute;todo HTTP para el env&iacute;o de datos remotos. */
		POST
	}

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

	private static boolean urlHandlerLoaded = false;
	private static Constructor<?> jsseUrlHandlerConstructor = null;

	private SSLContext ctx;

	private Properties config = null;
	private PasswordDecipher decipher = null;

	/**
	 * Inicializa el objeto para las conexiones SSL.
	 * @param sslConfig Opciones de configuraci&oacute;n.
	 * @param passwordDecipher Descifrador encargado de descifrar las contrase&ntilde;as de
	 * los almacenes de claves y certificados de confianza.
	 * @throws IllegalArgumentException Cuando se configura un fichero de almac&eacute;n que no existe.
	 * @throws GeneralSecurityException Cuando se produce un error en la configuraci&oacute;n de la conexi&oacute;n.
	 * @throws IOException Cuando se produce un error en la conexi&oacute;n con el servidor remoto.
	 */
	public void init(final Properties sslConfig, final PasswordDecipher passwordDecipher)
			throws IllegalArgumentException, GeneralSecurityException, IOException {

		this.config = new Properties();
		this.config.putAll(sslConfig);
		this.decipher = passwordDecipher;

		// Inicializamos el KeyStore
		KeyStore ks = null;
		KeyStorePassword ksPassword = null;
		String ksAlias = null;
        final String keyStore = getKeyStorePath();

        if (keyStore != null) {
        	final File ksFile = new File(keyStore);
        	if (!ksFile.isFile() || !ksFile.canRead()) {
        		throw new IllegalArgumentException(
        				"El almacen de certificados de autenticacion SSL no existe o no puede leerse: " + ksFile.getAbsolutePath() //$NON-NLS-1$
        		);
        	}

        	final String ksType = getKeyStoreType();

        	final String ksPasswordText = getKeyStorePass();
        	if (ksPasswordText == null) {
        		throw new IllegalArgumentException("No se ha indicado la clave del almacen SSL"); //$NON-NLS-1$
        	}
        	ksPassword = new KeyStorePassword(ksPasswordText, this.decipher);

        	ksAlias = getKeyStoreAlias();

        	ks = KeyStore.getInstance(ksType != null ? ksType : KeyStore.getDefaultType());

        	FileInputStream ksFis = null;
        	try {
        		ksFis = new FileInputStream(ksFile);
        		ks.load(ksFis, ksPassword.getPassword());
        	} finally {
        		if (ksFis != null) {
        			ksFis.close();
        		}
        	}
        }

        // Inicializamos el TrustStore
        KeyStore ts = null;
        boolean acceptAllCert = false;
        final String trustStore = getTrustStorePath();

        if (trustStore != null && !ACCEPT_DEFAULT_CERTS_VALUE.equalsIgnoreCase(trustStore)) {
        	if (ACCEPT_ALL_CERTS_VALUE.equalsIgnoreCase(trustStore)) {
        		acceptAllCert = true;
        	}
        	else {
        		final File tsFile = new File(trustStore);
            	if (!tsFile.exists() || !tsFile.isFile() || !tsFile.canRead()) {
            		throw new IllegalArgumentException(
            				"El almacen de certificados de confianza SSL no existe o no puede leerse" //$NON-NLS-1$
            		);
            	}

            	final String tsType = getTrustStoreType();

                final String tsPasswordText = getTrustStorePass();
                if (tsPasswordText == null) {
                	throw new IllegalArgumentException("No se ha indicado la clave del almacen de confianza SSL"); //$NON-NLS-1$
                }

                ts = KeyStore.getInstance(tsType);
                FileInputStream tsFis = null;
                try {
                	tsFis = new FileInputStream(new File(trustStore));
                	ts.load(tsFis, new KeyStorePassword(tsPasswordText, this.decipher).getPassword());
                } finally {
                	if (tsFis != null) {
                		tsFis.close();
                	}
                }                
        	}
        }

        // Inicializamos el contexto SSL
        if (ks != null || ts != null || acceptAllCert) {
        	initContext(ks, ksPassword != null ? ksPassword.getPassword() : null, ksAlias, ts, acceptAllCert);
        }
	}

	/**
	 * Inicializa el contexto para las conexiones HTTPS.
	 * @param ks Almac&eqacute;n de claves de autenticaci&oacute;n cliente.
	 * @param ksPassword Contrase&ntilde:a del almac&eacute;n de claves.
	 * @param ts TrustStore de certificados de confianza o {@code null} para la
	 * configuraci&oacute;n por defecto.
	 * @param acceptAllCerts Indica si se debe desactivar la validaci&oacute;n de
	 * los certificados SSL servidor.
	 * @throws NoSuchAlgorithmException Tipo de almac&eacute;n no soportado.
	 * @throws UnrecoverableKeyException  Cuando no se puede acceder al almac&eacute;n
	 * de claves.
	 * @throws KeyStoreException Cuando no se pueden inicializar el KeyStore o TrustStore.
	 * @throws KeyManagementException Cuando no se puede inicializar el contexto SSL.
	 */
	protected void initContext(
			final KeyStore ks,
			final char[] ksPassword,
			final String ksAlias,
			final KeyStore ts,
			final boolean acceptAllCerts) throws
												NoSuchAlgorithmException,
												UnrecoverableKeyException,
												KeyStoreException,
												KeyManagementException {

		KeyManager[] keyManagers = null;
		if (ks != null) {

			String selectedAlias;
			if (ksAlias == null || ksAlias.isEmpty()) {
				selectedAlias = ks.aliases().nextElement();
			}
			else {
				selectedAlias = ksAlias;
			}


			if (selectedAlias != null && !selectedAlias.isEmpty()) {
				try {
					keyManagers = new KeyManager[]{ new MultiCertKeyManager(ks, ksPassword, selectedAlias) };
				} catch (final Exception e) {
					LOGGER.warn("No se pudo inicializar el almacen con los datos proporcionados. Se usara el mecanismo por defecto"); //$NON-NLS-1$
					keyManagers = null;
				}
			}

			// Si todavia no se ha inicializado, lo hacemos con el mecanismo por defecto
			if (keyManagers == null) {
				final KeyManagerFactory kmf =
						KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

				kmf.init(ks, ksPassword);

				keyManagers = kmf.getKeyManagers();
			}
		}

		TrustManager[] tsManager = null;
		if (ts != null) {
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(
					TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);
			tsManager = tmf.getTrustManagers();
		}
		else if (acceptAllCerts) {
			tsManager = DUMMY_TRUST_MANAGER;
		}

		this.ctx = SSLContext.getInstance("TLSv1.2"); //$NON-NLS-1$
		this.ctx.init(keyManagers, tsManager, null);
	}

	/**
	 * Realiza una peticion HTTP a una URL.
	 * @param url URL a la que se realiza la petici&oacute;n.
	 * @param urlParameters Par&aacute;metros transmitidos en la llamada.
	 * @param method M&eacute;todo HTTP utilizado.
	 * @return Respuesta con el resultado de la llamada.
	 * @throws IOException Cuando ocurre un error durante la conexi&oacute;n/lectura.
	 */
	public HttpResponse sendRequest(final String url, final String urlParameters, final Method method) throws IOException {

		if (url == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		final URL uri = createURL(url);
		final HttpURLConnection conn = getConnection(uri);

		conn.setRequestMethod(method.toString());

		conn.addRequestProperty("Accept", "*/*"); //$NON-NLS-1$ //$NON-NLS-2$
		conn.addRequestProperty("Connection", "keep-alive"); //$NON-NLS-1$ //$NON-NLS-2$
		conn.addRequestProperty("Host", uri.getHost()); //$NON-NLS-1$
		conn.addRequestProperty("Origin", uri.getProtocol() +  "://" + uri.getHost()); //$NON-NLS-1$ //$NON-NLS-2$

		if (urlParameters != null) {
			conn.setDoOutput(true);
			final OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream()
					);
			writer.write(urlParameters);
			writer.flush();
			writer.close();
		}

		conn.connect();

		final int statusCode = conn.getResponseCode();
		final String mimeType = getMimeType(conn.getContentType());

		final Charset charset = getCharset(conn);

		final byte[] data;
		if (statusCode == 200) {
			final InputStream is = conn.getInputStream();
			data = Utils.getDataFromInputStream(is);
			is.close();
		}
		else {
			final InputStream is = conn.getErrorStream();
			data = Utils.getDataFromInputStream(is);
			is.close();
		}

		conn.disconnect();

		return new HttpResponse(statusCode, mimeType, charset, data);
	}



	private static String getMimeType(final String contentType) {

		if (contentType == null) {
			return null;
		}

		final int contentTypeLimit = contentType.indexOf(CONTENT_TYPE_SEPARATOR);
		if (contentTypeLimit != -1) {
			return contentType.substring(0, contentTypeLimit).trim();
		}
		return contentType.trim();
	}


	private static Charset getCharset(final HttpURLConnection connection) {

		final String contentType = connection.getContentType();
		final String contentEncoding = connection.getContentEncoding();
		if (contentType == null && contentEncoding == null) {
			return null;
		}

		Charset charset = null;
		try {
			if (contentType != null && contentType.toLowerCase(Locale.ENGLISH).contains(CHARSET_PREFIX)) {
				final int charsetIdx = contentType.toLowerCase(Locale.ENGLISH).indexOf(CHARSET_PREFIX) + CHARSET_PREFIX.length();
				final int limit = contentType.indexOf(CONTENT_TYPE_SEPARATOR, charsetIdx);
				if (limit != -1) {
					charset = Charset.forName(contentType.substring(charsetIdx, limit).trim());
				} else {
					charset = Charset.forName(contentType.substring(charsetIdx).trim());
				}
			}
			else if (contentEncoding != null) {
				charset = Charset.forName(contentEncoding.trim());
			}
		} catch (final Exception e) {
			LOGGER.error("No se pudo obtener el set de caracteres. Excepcion: " + e);
			charset = null;
		}

		return charset;
	}

	/**
	 * Realiza una peticion HTTP a una URL.
	 * @param url URL a la que se realiza la petici&oacute;n.
	 * @param urlParameters Par&aacute;metros transmitidos en la llamada.
	 * @param method M&eacute;todo HTTP utilizado.
	 * @return Datos recuperados como resultado de la llamada.
	 * @throws IOException Cuando ocurre un error durante la conexi&oacute;n/lectura o el
	 * servidor devuelve un error en la operaci&oacute;n.
	 */
	public byte[] readUrl(final String url, final String urlParameters, final Method method) throws IOException {

		if (url == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		final URL uri = createURL(url);
		final HttpURLConnection conn = getConnection(uri);

		conn.setRequestMethod(method.toString());

		conn.addRequestProperty("Accept", "*/*"); //$NON-NLS-1$ //$NON-NLS-2$
		conn.addRequestProperty("Connection", "keep-alive"); //$NON-NLS-1$ //$NON-NLS-2$
		conn.addRequestProperty("Host", uri.getHost()); //$NON-NLS-1$
		conn.addRequestProperty("Origin", uri.getProtocol() +  "://" + uri.getHost()); //$NON-NLS-1$ //$NON-NLS-2$

		if (urlParameters != null) {
			conn.setDoOutput(true);
			final OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream()
					);
			writer.write(urlParameters);
			writer.flush();
			writer.close();
		}

		conn.connect();

		final int resCode = conn.getResponseCode();
		final String statusCode = Integer.toString(resCode);

		if (statusCode.startsWith("4") || statusCode.startsWith("5")) { //$NON-NLS-1$ //$NON-NLS-2$
			throw new HttpError(resCode, conn.getResponseMessage() != null ?
					conn.getResponseMessage() : "Error: StatusCode: " + statusCode, uri.getHost()); //$NON-NLS-1$
		}

		final InputStream is = conn.getInputStream();
		final byte[] data = Utils.getDataFromInputStream(is);
		is.close();

		conn.disconnect();

		return data;
	}

	private static URL createURL(final String url) throws MalformedURLException {

		// Intentamos forzar el Handler de URL de JSSE para mantener un comportamiento homogeneo.
		// Si no, segun la configuracion del servidor de aplicaciones, puede que se utilice una
		// configuracion para las conexiones SSL distinta a la establecida
		if (!urlHandlerLoaded) {
			urlHandlerLoaded = true;
			try {
				final Class<?> handlerClass = Class.forName("sun.net.www.protocol.https.Handler", false, HttpsConnection.class.getClassLoader()); //$NON-NLS-1$
				jsseUrlHandlerConstructor = handlerClass.getDeclaredConstructor();
				jsseUrlHandlerConstructor.setAccessible(true);
			}
			catch (final Exception e) {
				jsseUrlHandlerConstructor = null;
			}
		}

		// Utilizamos el manejador de JSSE si se pudo cargar o el por defecto si no
		URL uri;
		if (jsseUrlHandlerConstructor != null) {
			try {
				final Object handler = jsseUrlHandlerConstructor.newInstance();
				uri = new URL(null, url, (URLStreamHandler) handler);
			}
			catch (final Exception e) {
				uri = new URL(url);
			}
		}
		else {
			uri = new URL(url);
		}

		return uri;
	}

	protected HttpURLConnection getConnection(final URL url) throws IOException {
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (this.ctx != null && con instanceof HttpsURLConnection) {
			final HttpsURLConnection cons = (HttpsURLConnection) con;
			cons.setSSLSocketFactory(this.ctx.getSocketFactory());
			cons.setHostnameVerifier(getHostnameVerifier(cons.getHostnameVerifier()));
		}
		return con;
	}


	private HostnameVerifier getHostnameVerifier(final HostnameVerifier verifier) {
		return new FireHostnameVerifier(verifier, isVerifyHostNames());
	}

	/**
	 * Verificador de nombre de host que utiliza el verificador por defecto o acepta
	 * cualquier nombre de host seg&uacute;n la configuraci&oacute;n establecida.
	 */
	static class FireHostnameVerifier implements HostnameVerifier {

		private final HostnameVerifier defaultVerifier;
		private final boolean verifyHostnames;

		public FireHostnameVerifier(final HostnameVerifier defaultVerifier, final boolean verifyHostnames) {
			this.defaultVerifier = defaultVerifier;
			this.verifyHostnames = verifyHostnames;
		}

		@Override
		public boolean verify(final String hostname, final SSLSession session) {

			if (this.verifyHostnames) {
				return this.defaultVerifier.verify(hostname, session);
			}

			return true;
		}
	}

	public String getKeyStorePath() {
		return this.config.getProperty(KEYSTORE_PROPERTY);
	}

	public String getKeyStorePass() {
		return this.config.getProperty(KEYSTORE_PASS_PROPERTY);
	}

	public String getKeyStoreType() {
		return this.config.getProperty(KEYSTORE_TYPE_PROPERTY, KeyStore.getDefaultType());
	}

	public String getKeyStoreAlias() {
		return this.config.getProperty(KEYSTORE_ALIAS_PROPERTY);
	}

	public String getTrustStorePath() {
		return this.config.getProperty(TRUSTSTORE_PROPERTY);
	}

	public String getTrustStorePass() {
		return this.config.getProperty(TRUSTSTORE_PASS_PROPERTY);
	}

	public String getTrustStoreType() {
		return this.config.getProperty(TRUSTSTORE_TYPE_PROPERTY, KeyStore.getDefaultType());
	}

	public boolean isVerifyHostNames() {
		// Se verificara el nombre de host salvo que se indique expresamente el valor "false"
		final String verifiedValue = this.config.getProperty(VERIFY_HOSTNAMES_PROPERTY);
		return !Boolean.FALSE.toString().equalsIgnoreCase(verifiedValue);
	}
}
