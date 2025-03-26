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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import es.gob.fire.server.services.internal.ApplicationOperationConfig;
import es.gob.fire.server.services.internal.ApplicationsDAOFactory;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.server.services.internal.ServiceParams;
import es.gob.fire.signature.ConfigManager;

/**
 * Clase para la lectura y guardado de los par&aacute;metros de una
 * petici&oacute;n.
 */
public class RequestParameters {

	/** Longitud m&aacute;xima de los par&aacute;metros directamente extra&iacute;dos de la petici&oacute;n. */
	private static final int MAX_ID_LENGTH = 50;

	private final Map<String, String> params;

	private final long contentLength;

	private RequestParameters(final Map<String, String> params, final long contentLength) {
		this.params = params;
		this.contentLength = contentLength;
	}

	/**
	 * Carga los parametros de la petici&oacute;n.
	 * @param request Petici&oacute;n a un servlet.
	 * @return Conjunto de par&aacute;metros.
	 * @throws IOException Cuando ocurre un error en la lectura de los par&aacute;metros.
	 */
	public static RequestParameters parseParameters(final HttpServletRequest request, final boolean legacyNames) throws IOException {
		RequestParameters parameters;
		if ("GET".equals(request.getMethod()) || request.getParameterNames().hasMoreElements()) { //$NON-NLS-1$
			parameters = extractParametersFromUrl(request);
		} else {
			parameters = extractParametersFromBody(request);
		}

		// Si admitiamos los nombres antiguos, los sustituimos ahora por los nuevos
	    if (legacyNames) {
	    	parameters.updateParamNames();
	    }

		return parameters;
	}

	/**
	 * Actualiza en el conjunto de par&aacute;metros extra&iacute;dos los nombres antiguos
	 * permitidos en la petici&oacute;n por los nuevos.
	 * @param params Par&aacute;metros de la petici&oacute;n.
	 */
	private void updateParamNames() {
    	replaceParamKey(ServiceParams.OLD_HTTP_PARAM_APPLICATION_ID, ServiceParams.HTTP_PARAM_APPLICATION_ID);
    	replaceParamKey(ServiceParams.OLD_HTTP_PARAM_SUBJECT_ID, ServiceParams.HTTP_PARAM_SUBJECT_ID);
    }

	/**
	 * Reemplaza la clave/nombre de una propiedad.
	 * @param oldKey Clave antigua.
	 * @param newKey Clave nueva.
	 */
	private void replaceParamKey(final String oldKey, final String newKey) {
		if (this.params.containsKey(oldKey)) {
			this.params.put(newKey, this.params.get(oldKey));
			this.params.remove(oldKey);
		}
	}

	/**
	 * Recupera un par&aacute;metro de la petici&oacute;n. En caso de tener m&aacute;s de un valor
	 * asociado, devolver&aacute; el primero que encuentre.
	 * @param name Nombre del par&aacute;metro.
	 * @return Valor del par&aacute;metro o {@code null} si no existe.
	 */
	public String getParameter(final String name) {
		return this.params.get(name);
	}

	/**
	 * Establece el valor de un par&aacute;metro de la petici&oacute;n.
	 * @param name Nombre del par&aacute;metro.
	 * @param value Valor del par&aacute;metro.
	 */
	public void putParameter(final String name, final String value) {
		this.params.put(name, value);
	}

	/**
	 * Indica si se especific&oacute; un par&aacute;metro en la petici&oacute;n.
	 *
	 * @param name Nombre del par&aacute;metro.
	 * @return {@code true} si se indic&oacute; el par&aacute;metro,
	 *         {@code false} en caso contrario.
	 */
	public boolean containsKey(final String key) {
		return this.params.containsKey(key);
	}

	/**
	 * Obtiene de la petici&oacute;n el identificador de aplicaci&oacute;n.
	 * @return El identificador de aplicaci&oacute;n o {@code null} si no lo encuentra.
	 */
	public String getAppId() {
		final String appId = getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		return limitId(appId);
	}

	/**
	 * Obtiene de la petici&oacute;n el identificador de transacci&oacute;n.
	 * @param request Petici&oacute;n recibida en el servicio.
	 * @return El identificador de transacci&oacute;n o {@code null} si no lo encuentra.
	 */
	public String getTransactionId() {
		final String trId = getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		return limitId(trId);
	}

	/**
	 * Obtiene de la petici&oacute;n el identificador del usuario.
	 * @return El identificador de usuario o {@code null} si no lo encuentra.
	 */
	public String getSubjectId() {
		final String subjectId = getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		return limitId(subjectId);
	}

	/**
	 * Recorta el tama&ntilde;o del par&aacute;metro de entrada si excede el m&aacute;ximo
	 * permitido para los ID.
	 */
	private static String limitId(final String id) {
		String limitedId = id;
		if (limitedId != null && limitedId.length() > MAX_ID_LENGTH) {
			limitedId = limitedId.substring(0, MAX_ID_LENGTH);
		}
		return limitedId;
	}

	/**
	 * Comprueba los par&aacute;metros de la petici&oacute;n usando la configuracion por defecto.
	 * @throws IOException Cuando la petici&oacute;n no cumpla con las restricciones establecidas.
	 */
	public void checkParameters() throws IOException {
		checkParameters(null);
	}

	/**
	 * Comprueba los par&aacute;metros de la petici&oacute;n usando la configuracion por defecto.
	 * @param logF Formateador de logs.
	 * @throws IOException Cuando la petici&oacute;n no cumpla con las restricciones establecidas.
	 */
	public void checkParameters(final LogTransactionFormatter logF) throws IOException {
		checkParameters(null, logF);
	}

	/**
	 * Comprueba los par&aacute;metros de la petici&oacute;n usando la configuraci&oacute;n especifica para una
	 * aplicaci&oacute;n, o la gen&eacute;rica si no la tiene.
	 * @param appId Identificador de aplicaci&oacute;n para establecer las restricciones apropiadas
	 * para ella. Si es {@code null}, se usar&aacute; la configuraci&oacute;n general.
	 * @param logF Formateador de logs.
	 * @throws IOException Cuando la petici&oacute;n no cumpla con las restricciones establecidas.
	 */
	public void checkParameters(final String appId, final LogTransactionFormatter logF) throws IOException {

		final ApplicationOperationConfig config = ApplicationsDAOFactory.getApplicationsDAO().getOperationConfig(appId, logF);

		// Comprobamos el tamano total de la peticion
		final long requestMaxSize = config.getRequestMaxSize();
		if (requestMaxSize != ConfigManager.UNLIMITED_MAX_SIZE && this.contentLength > requestMaxSize) {
			throw new IOException("La peticion excede el tamano maximo configurado. Tamano de la peticion: " //$NON-NLS-1$
					+ this.contentLength);
		}

		// Comprobamos si alguno de los valores excede el tamano maximo permitido para un unico parametro
		final long paramsMaxSize = config.getParamsMaxSize();
		this.params.forEach((k, v) -> {
			if (paramsMaxSize != ConfigManager.UNLIMITED_MAX_SIZE && checkLimit(v.length(), paramsMaxSize)) {
				throw new RuntimeException(
						"Se envia un parametro que excede el tamano maximo permitido: " + paramsMaxSize); //$NON-NLS-1$
			}
		});
	}

	private static RequestParameters extractParametersFromUrl(final HttpServletRequest request) {

		long totalSize = 0;
		final Map<String, String> parameters = new HashMap<>();
		final Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			final String name = names.nextElement();
			final String value = request.getParameter(name);
			parameters.put(name, value);
			totalSize += name.length() + value.length();
		}
		return new RequestParameters(parameters, totalSize);
	}

	private static boolean isMultipartRequest(final HttpServletRequest request) {
		System.out.println("Content-Length: " + request.getContentLengthLong());
		final String contentType = request.getHeader("Content-Type");
		return contentType != null && contentType.contains("multipart/form-data");
	}

	/**
	 * Leemos los parametros del cuerpo del mensaje. Debemos hacerlo de esta manera porque en cuanto la petici&oacute;n
	 * excede cierto tama&ntilde;o los m&eacute;todos comunes de obtener los par&aacute;metros de la petici&oacute;n dejan
	 * de funcionar.
	 * @param request Petici&oacute;n recibida en el servlet.
	 * @throws IOException Cuando ocurre un error al leer los par&aacute;metros.
	 */
	private static RequestParameters extractParametersFromBody(final HttpServletRequest request) throws IOException {
		final char[] block = new char[1048576];
		final StringBuilder buffer = new StringBuilder(block.length);

		long totalSize = 0;
		final Map<String, String> parameters = new HashMap<>();

		int n = 0;
		request.setCharacterEncoding("utf-8"); //$NON-NLS-1$
		try (final BufferedReader reader = request.getReader(); ) {
			while ((n = reader.read(block, 0, block.length)) > 0) {

				totalSize += n;

				int startParamsIdx = 0;
				for (int i = 0; i < n; i++) {
					if (block[i] == '&') {
						if (i > 0) {

							// Agregamos al buffer el parametro que ya tenemos leido
							buffer.append(Arrays.copyOfRange(block, startParamsIdx, i));
						}

						saveParam(parameters, buffer);
						startParamsIdx = i + 1;
					}
				}

				// Agregamos al buffer el inicio de parametro que ya tenemos leido
				buffer.append(Arrays.copyOfRange(block, startParamsIdx, n));
			}
			saveParam(parameters, buffer);
		}

		return new RequestParameters(parameters, totalSize);
	}

	/**
	 * Guarda en el mapa de par&aacute;metros aquel que se encuentra en
	 * {@code param} y luego vacia este buffer.
	 *
	 * @param params
	 *            Mapa en el que almacenar el nuevo parm&aacute;metro.
	 * @param param
	 *            Buffer con el nuevo par&aacute;metro.
	 */
	private static void saveParam(final Map<String, String> params, final StringBuilder param) {

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
	 * Hace un calculo aproximado de si un tama&ntilde;o de datos Base 64
	 * exceder&iacute;a un tama&ntilde;o de datos expresado en bytes.
	 *
	 * @param base64Size N&uacute;mero de caracteres en base 64.
	 * @param maxBytesSize Tama&ntilde;o m&aacute;ximo en bytes.
	 * @return {@code true} si el los datos excederian el tama&ntilde; indicado,
	 *         {@code false} en caso contrario.
	 */
	private static boolean checkLimit(final long base64Size, final long maxBytesSize) {
		return base64Size * 0.75 > maxBytesSize;
	}

}
