/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.client;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.StringTokenizer;

import es.gob.fire.client.HttpsConnection;

/**
 * Clase para la configuracion de la conexi&oacute;n con el componente central.
 */
public class ConnectionManager {

	private static HttpsConnection conn = null;

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

		// Si ya esta inicializada la conexion, ignoramos la operacion
		if (conn != null) {
			return;
		}
		conn = HttpsConnection.getConnection(config, null);
	}

	/**
	 * Realiza una peticion HTTP a una URL usando el m&eacute;todo POST.
	 * @param url URL a la que se realiza la petici&oacute;n.
	 * @return Datos recuperados como resultado de la llamada.
	 * @throws IOException Cuando no es posible conectar con el servidor o leer su respuesta.
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
	 * Realiza una peticion HTTP a una URL usando el m&eacute;todo GET.
	 * @param url URL a la que se realiza la petici&oacute;n.
	 * @return Datos recuperados como resultado de la llamada.
	 * @throws IOException Cuando no es posible conectar con el servidor o leer su respuesta.
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
	 * @throws IOException Cuando ocurre un error durante la conexi&oacute;n/lectura o el
	 * servidor devuelve un error en la operaci&oacute;n.
	 */
	public static byte[] readUrl(final String url, final String urlParameters, final Method method) throws IOException {

		if (url == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		return conn.readUrl(url, urlParameters, method == Method.POST ?
				HttpsConnection.Method.POST : HttpsConnection.Method.GET);
	}

	/**
	 * M&eacute;todo HTTP soportados.
	 */
	public enum Method {
		/** M&eacute;todo HTTP para la recuperaci&oacute;n de datos remotos. */
		GET,
		/** M&eacute;todo HTTP para el env&iacute;o de datos remotos. */
		POST
	}
}
