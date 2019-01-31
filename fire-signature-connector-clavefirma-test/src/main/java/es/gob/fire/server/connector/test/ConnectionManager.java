/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.connector.test;

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
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.http.HttpError;


/**
 * Clase para la configuracion de la conexi&oacute;n con el componente central.
 */
public final class ConnectionManager {

    private static final String KEYSTORE_PROPERTY = "javax.net.ssl.keyStore"; //$NON-NLS-1$

    private static final String KEYSTORE_PASS_PROPERTY = "javax.net.ssl.keyStorePassword"; //$NON-NLS-1$

    private static final String KEYSTORE_TYPE_PROPERTY = "javax.net.ssl.keyStoreType"; //$NON-NLS-1$

    private static final String TRUSTSTORE_PROPERTY = "javax.net.ssl.trustStore"; //$NON-NLS-1$

    private static final String TRUSTSTORE_PASS_PROPERTY = "javax.net.ssl.trustStorePassword"; //$NON-NLS-1$

    private static final String TRUSTSTORE_TYPE_PROPERTY = "javax.net.ssl.trustStoreType"; //$NON-NLS-1$

    private static final String ACCEPT_ALL_CERTS = "all"; //$NON-NLS-1$

	private static SSLContext ctx = null;

	/**
	 * M&eacute;todo HTTP soportados.
	 */
	public enum Method {
		/** Identifica el m&eacute;todo GET para las comunicaciones HTTP/S. */
		GET,
		/** Identifica el m&eacute;todo POST para las comunicaciones HTTP/S. */
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

	private ConnectionManager() {
		// No se permite instanciar la clase
	}

	/**
	 * Configura la conexi&oacute;n con el componente central.
	 * @param config Opciones de configuraci&oacute;n.
	 * @throws IllegalArgumentException Cuando se configura un fichero de almac&eacute;n que no existe.
	 * @throws GeneralSecurityException Cuando se produce un error en la configuraci&oacute;n de la conexi&oacute;n.
	 * @throws IOException Cuando se produce un error en la conexi&oacute;n con el servidor remoto.
	 */
	public static void configureConnection(final Properties config) throws IllegalArgumentException, GeneralSecurityException, IOException {

		// Si ya esta inicializado el contexto, ignoramos la operacion
		if (ctx != null) {
			return;
		}

        final String keyStore = config.getProperty(KEYSTORE_PROPERTY);
        if (keyStore != null && !keyStore.isEmpty()) {
        	final File ksFile = new File(keyStore);
        	if (!ksFile.exists() || !ksFile.isFile() || !ksFile.canRead()) {
        		throw new IllegalArgumentException(
        				"El almacen de certificados de autenticacion SSL no existe o no puede leerse" //$NON-NLS-1$
        		);
        	}
        }

        final String ksType = config.getProperty(KEYSTORE_TYPE_PROPERTY);

        char[] ksPassword = null;
        if (config.containsKey(KEYSTORE_PASS_PROPERTY)) {
        	ksPassword = config.getProperty(KEYSTORE_PASS_PROPERTY).toCharArray();
        }

        if (keyStore != null && ksPassword == null) {
        	throw new IllegalArgumentException("No se ha indicado la clave del almacen SSL"); //$NON-NLS-1$
        }

        KeyStore ks = null;
        if (keyStore != null && ksPassword != null) {
        	ks = KeyStore.getInstance(ksType != null ? ksType : KeyStore.getDefaultType());
        	try (
    			final FileInputStream ksFis = new FileInputStream(new File(keyStore));
			) {
        		ks.load(ksFis, ksPassword);
        	}
        }

        KeyStore ts = null;
        boolean acceptAllCert = false;
        final String trustStore = config.getProperty(TRUSTSTORE_PROPERTY);
        if (trustStore != null) {
        	if (ACCEPT_ALL_CERTS.equalsIgnoreCase(trustStore)) {
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

                final String tsPassword = config.getProperty(TRUSTSTORE_PASS_PROPERTY);
                if (tsPassword == null) {
                	throw new IllegalArgumentException("No se ha indicado la clave del almacen de confianza SSL"); //$NON-NLS-1$
                }

                ts = KeyStore.getInstance(tsType);
                try (
            		final FileInputStream tsFis = new FileInputStream(new File(trustStore));
        		) {
                	ts.load(tsFis, tsPassword.toCharArray());
                }
        	}
        }

        initContext(ks, ksPassword, ts, acceptAllCert);
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
	private static void initContext(final KeyStore ks, final char[] ksPassword,
			final KeyStore ts, final boolean acceptAllCerts) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {

		KeyManager[] kms = null;
		if (ks != null && ksPassword != null) {
			final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, ksPassword);
			kms = kmf.getKeyManagers();
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

		ctx = SSLContext.getInstance("TLS"); //$NON-NLS-1$
		ctx.init(kms, tsManager, null);
	}

	private static HttpURLConnection getConnection(final URL url) throws IOException {

		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con instanceof HttpsURLConnection) {

			if (ctx != null) {
				HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
			}
			HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
			if (ctx != null) {
				((HttpsURLConnection) con).setSSLSocketFactory(ctx.getSocketFactory());
			}

			((HttpsURLConnection) con).setHostnameVerifier((hostname, session) -> true);
		}

		return con;
	}

	/**
	 * Realiza una peticion HTTP a una URL usando el m&eacute;todo POST.
	 * @param url URL a la que se realiza la petici&oacute;n.
	 * @return Datos recuperados como resultado de la llamada.
	 * @throws IOException Cuando ocurre un error en la comunicaci&oacute;n.
	 */
	public static byte[] readUrlByPost(final String url) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		String urlBase;
		String urlParameters;
		if (url.contains("?")) { //$NON-NLS-1$
			final StringTokenizer st = new StringTokenizer(url, "?"); //$NON-NLS-1$
			urlBase = st.nextToken();
			urlParameters = st.nextToken();
		}
		else {
			urlBase = url;
			urlParameters = null;
		}

		return readUrl(urlBase, urlParameters, Method.POST);
	}

	/**
	 * Realiza una peticion HTTP a una URL usando el m&eacute;todo POST.
	 * @param url URL a la que se realiza la petici&oacute;n.
	 * @param urlParameters Listado de par&aacute;metros en codificaci&oacute; URL que se deben pasar.
	 * @return Datos recuperados como resultado de la llamada.
	 * @throws IOException Cuando ocurre un error en la comunicaci&oacute;n.
	 */
	public static byte[] readUrlByPost(final String url, final String urlParameters) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		return readUrl(url, urlParameters, Method.POST);
	}

	/**
	 * Realiza una peticion HTTP a una URL usando el m&eacute;todo GET.
	 * @param url URL a la que se realiza la petici&oacute;n.
	 * @return Datos recuperados como resultado de la llamada.
	 * @throws IOException Cuando ocurre un error en la comunicaci&oacute;n.
	 */
	public static byte[] readUrlByGet(final String url) throws IOException {
		return readUrl(url, null, Method.GET);
	}

	/**
	 * Realiza una peticion HTTP a una URL.
	 * @param url URL a la que se realiza la petici&oacute;n.
	 * @param urlParameters Par&aacute;metros transmitidos en la llamada.
	 * @param method M&eacute;todo HTTP utilizado.
	 * @return Datos recuperados como resultado de la llamada.
	 */
	private static byte[] readUrl(final String url, final String urlParameters, final Method method) throws IOException {

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
			try (
				final OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream()
				);
			) {
				writer.write(urlParameters);
				writer.flush();
			}
		}

		conn.connect();
		final int resCode = conn.getResponseCode();
		final String statusCode = Integer.toString(resCode);
		if (statusCode.startsWith("4") || statusCode.startsWith("5")) { //$NON-NLS-1$ //$NON-NLS-2$
			throw new HttpError(resCode, "Error " + statusCode + " en la conexion a: " + url, url); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try (
			final InputStream is = conn.getInputStream();
		) {
			return AOUtil.getDataFromInputStream(is);
		}

	}
}
