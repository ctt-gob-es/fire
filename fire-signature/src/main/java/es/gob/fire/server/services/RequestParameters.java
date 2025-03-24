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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

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
public class RequestParameters extends HashMap<String, String> {

	/** Serial Id. */
	private static final long serialVersionUID = 2008278392123140503L;

	/** Longitud m&aacute;xima de los par&aacute;metros directamente extra&iacute;dos de la petici&oacute;n. */
	private static final int MAX_ID_LENGTH = 50;


	private RequestParameters() {
		super();
	}

	/**
	 * Recupera un par&aacute;metro de la petici&oacute;n.
	 *
	 * @param name
	 *            Nombre del par&aacute;metro.
	 * @return Valor del par&aacute;metro o {@code null} si no existe.
	 */
	public String getParameter(final String name) {
		return get(name);
	}

	/**
	 * Indica si se especific&oacute; un par&aacute;metro en la petici&oacute;n.
	 *
	 * @param name Nombre del par&aacute;metro.
	 * @return {@code true} si se indic&oacute; el par&aacute;metro,
	 *         {@code false} en caso contrario.
	 */
	public boolean containsParameter(final String name) {
		return containsKey(name);
	}

	/**
	 * Obtiene de la petici&oacute;n el identificador de aplicaci&oacute;n.
	 * @param request Petici&oacute;n recibida en el servicio.
	 * @param legacy Indica si el identificador podr&iacute;a venir con el nombre antiguo en la
	 * petici&oacute;n, en cuyo caso se le dar&iacute;a prioridad.
	 * @return El identificador de aplicaci&oacute;n o {@code null} si no lo encuentra.
	 */
	public static String getAppId(final HttpServletRequest request, final boolean legacy) {
		String appId = null;
		if (legacy) {
			appId = request.getParameter(ServiceParams.OLD_HTTP_PARAM_APPLICATION_ID);
		}
		if (appId == null) {
			appId = request.getParameter(ServiceParams.HTTP_PARAM_APPLICATION_ID);
		}
		return limitId(appId);
	}

	/**
	 * Obtiene de la petici&oacute;n el identificador de transacci&oacute;n.
	 * @param request Petici&oacute;n recibida en el servicio.
	 * @return El identificador de transacci&oacute;n o {@code null} si no lo encuentra.
	 */
	public static String getTransactionId(final HttpServletRequest request) {
		final String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
		return limitId(trId);
	}

	public static String getSubjectId(final HttpServletRequest request, final boolean legacy) {
		String subjectId = null;
		if (legacy) {
			subjectId = request.getParameter(ServiceParams.OLD_HTTP_PARAM_SUBJECT_ID);
		}
		if (subjectId == null) {
			subjectId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
		}
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
	 * Parsea una petici&oacute;n al servicio usando la configuracion por defecto.
	 *
	 * @param request Petici&oacute;n al servicio.
	 * @return Conjunto de par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error en la lectura de la petici&oacute;n o uno de sus
	 * par&aacute;metros.
	 */
	public static RequestParameters extractParameters(final HttpServletRequest request) throws IOException {
		return extractParameters(request, null);
	}

	/**
	 * Parsea una petici&oacute;n al servicio usando la configuracion por defecto.
	 *
	 * @param request Petici&oacute;n al servicio.
	 * @return Conjunto de par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @param logF Formateador de logs.
	 * @throws IOException Cuando ocurre un error en la lectura de la petici&oacute;n o uno de sus
	 * par&aacute;metros.
	 */
	public static RequestParameters extractParameters(final HttpServletRequest request, final LogTransactionFormatter logF) throws IOException {
		return extractParameters(request, null, logF);
	}

	/**
	 * Parsea una petici&oacute;n al servicio usando la configuraci&oacute;n especifica para una
	 * aplicaci&oacute;n, o la gen&eacute;rica si no la tiene.
	 *
	 * @param request Petici&oacute;n al servicio.
	 * @param appId Identificador de aplicaci&oacute;n para establecer las restricciones apropiadas
	 * para ella. Si es {@code null}, se usar&aacute; la configuraci&oacute;n general.
	 * @param logF Formateador de logs.
	 * @return Conjunto de par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error en la lectura de la petici&oacute;n o uno de sus
	 * par&aacute;metros.
	 */
	public static RequestParameters extractParameters(final HttpServletRequest request, final String appId, final LogTransactionFormatter logF) throws IOException {
		return extractParameters(request, appId, false, logF);
	}

	/**
	 * Parsea una petici&oacute;n al servicio usando la configuraci&oacute;n especifica para una
	 * aplicaci&oacute;n, o la gen&eacute;rica si no la tiene.
	 *
	 * @param request Petici&oacute;n al servicio.
	 * @param appId Identificador de aplicaci&oacute;n para establecer las restricciones apropiadas
	 * para ella. Si es {@code null}, se usar&aacute; la configuraci&oacute;n general.
	 * @param legacyNames Indica si entre los par&acute;metros pueden aparecer los identificadores
	 * con el nombre antiguo y si debe d&aacute;rseles prioridad.
	 * @param logF Formateador de logs.
	 * @return Conjunto de par&aacute;metros extra&iacute;dos de la petici&oacute;n.
	 * @throws IOException Cuando ocurre un error en la lectura de la petici&oacute;n o uno de sus
	 * par&aacute;metros.
	 */
	public static RequestParameters extractParameters(final HttpServletRequest request, final String appId, final boolean legacyNames, final LogTransactionFormatter logF) throws IOException {
		final RequestParameters params = new RequestParameters();

		final ApplicationOperationConfig config = ApplicationsDAOFactory.getApplicationsDAO().getOperationConfig(appId, logF);

		final long requestMaxSize = config.getRequestMaxSize();
		if (requestMaxSize != ConfigManager.UNLIMITED_MAX_SIZE && request.getContentLengthLong() > requestMaxSize) {
			throw new IOException("La peticion excede el tamano maximo configurado. Tamano declarado en la peticion: " //$NON-NLS-1$
					+ request.getContentLength());
		}

		if ("GET".equals(request.getMethod())) { //$NON-NLS-1$
			extractParametersFromUrl(request, params);
		} else {
			extractParametersFromBody(request, params, config.getParamsMaxSize(), requestMaxSize);
		}

		// Si admitiamos los nombres antiguos, los sustituimos ahora por los nuevos
	    if (legacyNames) {
	    	updateParamNames(params);
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

	private static void extractParametersFromBody(final HttpServletRequest request, final RequestParameters params,
			final int paramsMaxSize, final long requestMaxSize) throws IOException {

		request.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		// Obtener parametros de la peticion sin consumir el InputStream
		request.getParameterMap().forEach((key, values) -> {
			if (values.length > 0) {
				final String value = values[0];

				// Comprobamos si el valor excede el tamano maximo permitido
				if (paramsMaxSize != ConfigManager.UNLIMITED_MAX_SIZE
						&& checkLimit(value.length(), paramsMaxSize)) {
					throw new RuntimeException(
							"Se envia un parametro que excede el tamano maximo permitido: " + paramsMaxSize); //$NON-NLS-1$
				}

				// Guardar el parametro
				params.put(key, value);
			}
		});

		// Comprobar el tamano total de la peticion
		final long totalSize = request.getContentLengthLong();
		if (requestMaxSize != ConfigManager.UNLIMITED_MAX_SIZE && checkLimit(totalSize, requestMaxSize)) {
			throw new IOException("La peticion excede el tamano maximo configurado. Tamano leido: " + totalSize); //$NON-NLS-1$
		}
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

	/**
	 * Actualiza en el conjunto de par&aacute;metros extra&iacute;dos los nombres antiguos
	 * permitidos en la petici&oacute;n por los nuevos.
	 * @param params Par&aacute;metros de la petici&oacute;n.
	 */
	private static void updateParamNames(final RequestParameters params) {
    	params.replaceParamKey(ServiceParams.OLD_HTTP_PARAM_APPLICATION_ID, ServiceParams.HTTP_PARAM_APPLICATION_ID);
    	params.replaceParamKey(ServiceParams.OLD_HTTP_PARAM_SUBJECT_ID, ServiceParams.HTTP_PARAM_SUBJECT_ID);
    }

//	/**
//	 * Guarda en el mapa de par&aacute;metros aquel que se encuentra en
//	 *
//	 * {@code param} y luego vacia este buffer.
//	 * @param params Mapa en el que almacenar el nuevo parm&aacute;metro.
//	 * @param param Buffer con el nuevo par&aacute;metro.
//	 */
//	private static void saveParam(final HashMap<String, String> params, final StringBuilder param) {
//
//		if (param.length() == 0) {
//			return;
//		}
//
//		final int sep = param.indexOf("="); //$NON-NLS-1$
//		if (sep == -1) {
//			throw new IllegalArgumentException("La peticion no esta bien formada"); //$NON-NLS-1$
//		}
//		params.put(param.substring(0, sep), param.substring(sep + 1));
//		param.setLength(0);
//	}

	/**
	 * Reemplaza la clave/nombre de una propiedad.
	 *
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
