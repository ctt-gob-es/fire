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
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
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

/**
 * Clase de conexi&oacute;n mediante SSL.
 */
public class HttpsConnection {

	private static final String KEYSTORE_PROPERTY = "javax.net.ssl.keyStore"; //$NON-NLS-1$

    private static final String KEYSTORE_PASS_PROPERTY = "javax.net.ssl.keyStorePassword"; //$NON-NLS-1$

    private static final String KEYSTORE_TYPE_PROPERTY = "javax.net.ssl.keyStoreType"; //$NON-NLS-1$

    private static final String TRUSTSTORE_PROPERTY = "javax.net.ssl.trustStore"; //$NON-NLS-1$

    private static final String TRUSTSTORE_PASS_PROPERTY = "javax.net.ssl.trustStorePassword"; //$NON-NLS-1$

    private static final String TRUSTSTORE_TYPE_PROPERTY = "javax.net.ssl.trustStoreType"; //$NON-NLS-1$

    private static final String ACCEPT_ALL_CERTS_VALUE = "all"; //$NON-NLS-1$

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

	private SSLContext ctx;


	private HttpsConnection() {
		// No hacemos nada
	}


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
		conn.configureConnection(config, decipher);

		return conn;
	}

	/**
	 * Configura la conexi&oacute;n con el componente central.
	 * @param config Opciones de configuraci&oacute;n.
	 * @param decipher Descifrador encargado de descifrar las contrase&ntilde;as de
	 * los almacenes de claves y certificados de confianza.
	 * @throws IllegalArgumentException Cuando se configura un fichero de almac&eacute;n que no existe.
	 * @throws GeneralSecurityException Cuando se produce un error en la configuraci&oacute;n de la conexi&oacute;n.
	 * @throws IOException Cuando se produce un error en la conexi&oacute;n con el servidor remoto.
	 */
	private void configureConnection(final Properties config, final PasswordDecipher decipher) throws IllegalArgumentException, GeneralSecurityException, IOException {

		// Inicializamos el KeyStore
		KeyStore ks = null;
		KeyStorePassword ksPassword = null;
        final String keyStore = config.getProperty(KEYSTORE_PROPERTY);
        if (keyStore != null) {
        	final File ksFile = new File(keyStore);
        	if (!ksFile.isFile() || !ksFile.canRead()) {
        		throw new IllegalArgumentException(
        				"El almacen de certificados de autenticacion SSL no existe o no puede leerse: " + ksFile.getAbsolutePath() //$NON-NLS-1$
        		);
        	}

        	final String ksType = config.getProperty(KEYSTORE_TYPE_PROPERTY);

        	final String ksPasswordText = config.getProperty(KEYSTORE_PASS_PROPERTY);
        	if (ksPasswordText == null) {
        		throw new IllegalArgumentException("No se ha indicado la clave del almacen SSL"); //$NON-NLS-1$
        	}
        	ksPassword = new KeyStorePassword(ksPasswordText, decipher);

        	ks = KeyStore.getInstance(ksType != null ? ksType : KeyStore.getDefaultType());

        	final FileInputStream ksFis = new FileInputStream(new File(keyStore));
        	ks.load(ksFis, ksPassword.getPassword());
        	ksFis.close();
        }

        // Inicializamos el TrustStore
        KeyStore ts = null;
        boolean acceptAllCert = false;
        final String trustStore = config.getProperty(TRUSTSTORE_PROPERTY);
        if (trustStore != null) {
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

            	final String tsType = config.getProperty(TRUSTSTORE_TYPE_PROPERTY, KeyStore.getDefaultType());

                final String tsPasswordText = config.getProperty(TRUSTSTORE_PASS_PROPERTY);
                if (tsPasswordText == null) {
                	throw new IllegalArgumentException("No se ha indicado la clave del almacen de confianza SSL"); //$NON-NLS-1$
                }

                ts = KeyStore.getInstance(tsType);
                final FileInputStream tsFis = new FileInputStream(new File(trustStore));
                ts.load(tsFis, new KeyStorePassword(tsPasswordText, decipher).getPassword());
                tsFis.close();
        	}
        }

        if (ks != null || ts != null || acceptAllCert) {
        	initContext(ks, ksPassword != null ? ksPassword.getPassword() : null, ts, acceptAllCert);
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
	private void initContext(
			final KeyStore ks,
			final char[] ksPassword,
			final KeyStore ts,
			final boolean acceptAllCerts) throws
												NoSuchAlgorithmException,
												UnrecoverableKeyException,
												KeyStoreException,
												KeyManagementException {

		KeyManager[] keyManagers = null;
		if (ks != null) {
			final KeyManagerFactory kmf =
					KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

			kmf.init(ks, ksPassword);

			keyManagers = kmf.getKeyManagers();
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

		this.ctx = SSLContext.getInstance("TLS"); //$NON-NLS-1$
		this.ctx.init(keyManagers, tsManager, null);
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

		final URL uri = new URL(url);

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
			throw new HttpError(resCode, conn.getResponseMessage(), uri.getHost());
		}

		final InputStream is = conn.getInputStream();
		final byte[] data = Utils.getDataFromInputStream(is);
		is.close();

		return data;
	}


	private HttpURLConnection getConnection(final URL url) throws IOException {

		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con instanceof HttpsURLConnection && this.ctx != null) {
			HttpsURLConnection.setDefaultSSLSocketFactory(this.ctx.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(final String hostname, final SSLSession session) {
					return true;
				}
			});
			((HttpsURLConnection) con).setSSLSocketFactory(this.ctx.getSocketFactory());
			((HttpsURLConnection) con).setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(final String hostname, final SSLSession session) {
					return true;
				}
			});
		}

		return con;
	}
}
