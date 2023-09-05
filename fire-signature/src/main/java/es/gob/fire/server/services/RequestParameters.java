/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import es.gob.fire.signature.ConfigManager;

/**
 * Clase para la lectura y guardado de los par&aacute;metros de una petici&oacute;n.
 */
public class RequestParameters extends HashMap<String, String> {

	/** Serial Id. */
	private static final long serialVersionUID = 2008278392123140503L;

	private RequestParameters() {
		super();
	}

	/**
	 * Recupera un par&aacute;metro de la petici&oacute;n.
	 * @param name Nombre del par&aacute;metro.
	 * @return Valor del par&aacute;metro o {@code null} si no existe.
	 */
	public String getParameter(final String name) {
		return get(name);
	}

	/**
	 * Indica si se especific&oacute; un par&aacute;metro en la petici&oacute;n.
	 * @param name Nombre del par&aacute;metro.
	 * @return {@code true} si se indic&oacute; el par&aacute;metro,
	 * {@code false} en caso contrario.
	 */
	public boolean containsParameter(final String name) {
		return containsKey(name);
	}

	/**
	 * Parsea una petici&oacute;n al servicio.
	 * @param request Petici&oacute;n al servicio.
	 * @return Objetos extra&iacute;dos de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error en la lectura de la petici&oacute;n
	 * o uno de sus par&aacute;metros.
	 * @throws IllegalArgumentException Si la peticion no esta bien formada.
	 */
	public static RequestParameters extractParameters(final HttpServletRequest request) throws IOException {

		final RequestParameters params = new RequestParameters();

		final long requestMaxSize = ConfigManager.getRequestMaxSize();
		if (requestMaxSize != ConfigManager.UNLIMITED_MAX_SIZE && request.getContentLengthLong() > requestMaxSize) {
			throw new IOException("La peticion excede el tamano maximo configurado. Tamano declarado en la peticion: " + request.getContentLength()); //$NON-NLS-1$
		}

		if ("GET".equals(request.getMethod())) { //$NON-NLS-1$
			extractParametersFromUrl(request, params);
		}
		else {
			extractParametersFromBody(request, params, ConfigManager.getParamMaxSize(), requestMaxSize);
		}

		return params;
	}


	private static void extractParametersFromUrl(final HttpServletRequest request, final RequestParameters params) {

		final Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			final String name = names.nextElement();
			params.put(name, request.getParameter(name));
		}
	}

	private static void extractParametersFromBody(final HttpServletRequest request, final RequestParameters params, final int paramsMaxSize, final long requestMaxSize) throws IOException {
		final char[] block = new char[1048576];
		final StringBuilder buffer = new StringBuilder(1048576);

		long totalSize = 0;

		int n = 0;
		request.setCharacterEncoding("utf-8"); //$NON-NLS-1$
		try (final BufferedReader reader = request.getReader(); ) {
			while ((n = reader.read(block, 0, block.length)) > 0) {

				// Comprobamos que la peticion en conjunto no exceda el tamano maximo configurado
				totalSize += n;
				if (requestMaxSize != ConfigManager.UNLIMITED_MAX_SIZE && checkLimit(totalSize, requestMaxSize)) {
					throw new IOException("La peticion excede el tamano maximo configurado. Tamano leido: " + totalSize); //$NON-NLS-1$
				}

				int startParamsIdx = 0;
				for (int i = 0; i < n; i++) {
					if (block[i] == '&') {
						if (i > 0) {

							// Comprobamos que el tamano no exceda el maximo configurado
							if (paramsMaxSize != ConfigManager.UNLIMITED_MAX_SIZE && checkLimit(i - startParamsIdx, paramsMaxSize)) {
								throw new IOException("Se envio un parametro al servicio que excedia el tamano maximo permitido: " + paramsMaxSize); //$NON-NLS-1$
							}
							// Agregamos al buffer el parametro que ya tenemos leido
							buffer.append(Arrays.copyOfRange(block, startParamsIdx, i));
						}

						saveParam(params, buffer);
						startParamsIdx = i + 1;
					}
				}

				// Comprobamos que el parametro almacenado en el buffer mas lo nuevo no exceda el
				// tamano maximo configurado

				if (paramsMaxSize != ConfigManager.UNLIMITED_MAX_SIZE && checkLimit(buffer.length() + n - startParamsIdx, paramsMaxSize)) {
					throw new IOException("Se envio un parametro al servicio que excedia el tamano maximo permitido: " + paramsMaxSize); //$NON-NLS-1$
				}
				// Agregamos al buffer el inicio de parametro que ya tenemos leido
				buffer.append(Arrays.copyOfRange(block, startParamsIdx, n));
			}
			saveParam(params, buffer);
		}
	}

	/**
	 * Hace un calculo aproximado de si un tama&ntilde;o de datos Base 64 exceder&iacute;a un
	 * tama&ntilde;o de datos expresado en bytes.
	 * @param base64Size Numero de caracteres en base 64.
	 * @param maxBytesSize Tama&ntilde;o m&aacute;ximo en bytes.
	 * @return {@code true} si el los datos excederian el tama&ntilde; indicado, {@code false} en
	 * caso contrario.
	 */
	private static boolean checkLimit(final long base64Size, final long maxBytesSize) {
		return base64Size * 0.75 > maxBytesSize;
	}

	/**
	 * Guarda en el mapa de par&aacute;metros aquel que se encuentra en {@code param} y luego vacia este buffer.
	 * @param params Mapa en el que almacenar el nuevo parm&aacute;metro.
	 * @param param Buffer con el nuevo par&aacute;metro.
	 */
	private static void saveParam(final HashMap<String, String> params, final StringBuilder param) {

		if (param.length() == 0) {
			return;
		}

		final int sep = param.indexOf("="); //$NON-NLS-1$
		if (sep == -1) {
			throw new IllegalArgumentException("La peticion no esta bien formada"); //$NON-NLS-1$
		}
		params.put(param.substring(0, sep), param.substring(sep + 1));
		param.setLength(0);
	}

	/**
	 * Reemplaza la clave/nombre de una propiedad.
	 * @param oldKey Clave antigua.
	 * @param newKey Clave nueva.
	 */
	void replaceParamKey(final String oldKey, final String newKey) {
		if (containsKey(oldKey)) {
			put(newKey, get(oldKey));
			remove(oldKey);
		}
	}
}
