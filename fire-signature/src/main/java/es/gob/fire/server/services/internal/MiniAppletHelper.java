/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Properties;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.services.ServiceUtil;

/**
 * Clase de utilidad para la construcci&oacute;n de un XML de definicion de lote
 * de firma para el cliente @firma.
 */
public class MiniAppletHelper {

	private static final String XML_ATTRIBUTE_ID = "Id"; //$NON-NLS-1$

	private static final String XML_ELEMENT_DATASOURCE = "datasource"; //$NON-NLS-1$
	private static final String XML_ELEMENT_FORMAT = "format"; //$NON-NLS-1$
	private static final String XML_ELEMENT_SUBOPERATION = "suboperation"; //$NON-NLS-1$
	private static final String XML_ELEMENT_EXTRAPARAMS = "extraparams"; //$NON-NLS-1$

	/** Nombre del par&aacute;metro para la configuraci&oacute;n del formato avanzado
	 * al que actualizar las firmas en el proceso de firma trif&aacute;sico. */
	public static final String PARAM_UPGRADE_FORMAT = "upgradeFormat"; //$NON-NLS-1$

	private static final String AFIRMA_EXTRAPARAM_FILTER = "filter"; //$NON-NLS-1$
	private static final String AFIRMA_EXTRAPARAM_FILTERS = "filters"; //$NON-NLS-1$
	private static final String AFIRMA_EXTRAPARAM_ORDER_FILTERS_PREFIX = "filters."; //$NON-NLS-1$
	public static final String AFIRMA_EXTRAPARAM_HEADLESS = "headless"; //$NON-NLS-1$

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * Crea el fichero de firma de lote para su uso por parte del Cliente @firma.
	 * @param stopOnError Indica si se debe detener el proceso de firma al encontrar un error.
	 * @param algorithm Algoritmo de firma que se usar&aacute; en todas las firmas del lote.
	 * @param defaultConfig Configuraci&oacute;n de firma por defecto.
	 * @param batchResult Listado de resultados parciales de los documentos del lote.
	 * @return XML de definici&oacute;n del lote codificado en base 64.
	 */
	public static String createBatchXml(final boolean stopOnError, final String algorithm, final SignBatchConfig defaultConfig, final BatchResult batchResult) {
		final StringBuilder sb = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<signbatch stoponerror=\"" //$NON-NLS-1$
			);
			sb.append(stopOnError);
			sb.append("\" algorithm=\""); //$NON-NLS-1$
			sb.append(algorithm);
			sb.append("\">\n"); //$NON-NLS-1$
			final Iterator<String> it = batchResult.iterator();
			while (it.hasNext()) {
				final String docId = it.next();
				final String dataReference = batchResult.getDocumentReference(docId);
				final SignBatchConfig signConfig = batchResult.getSignConfig(docId);
				sb.append(
						getSingleSignXml(
								docId,
								dataReference,
								signConfig != null ? signConfig : defaultConfig)
				);
			}

			sb.append("</signbatch>\n"); //$NON-NLS-1$
			return Base64.encode(sb.toString().getBytes(DEFAULT_CHARSET));
	}

	private static String getSingleSignXml(final String docId, final String dataReference, final SignBatchConfig signConfig) {
		final StringBuilder sb = new StringBuilder()
				.append(" <singlesign ") //$NON-NLS-1$
				.append(XML_ATTRIBUTE_ID).append("=\"").append(docId).append("\">\n  <") //$NON-NLS-1$ //$NON-NLS-2$
				.append(XML_ELEMENT_DATASOURCE).append(">").append(dataReference) //$NON-NLS-1$
				.append("</").append(XML_ELEMENT_DATASOURCE).append(">\n  <") //$NON-NLS-1$ //$NON-NLS-2$
				.append(XML_ELEMENT_FORMAT).append(">").append(signConfig.getFormat()) //$NON-NLS-1$
				.append("</").append(XML_ELEMENT_FORMAT).append(">\n  <") //$NON-NLS-1$ //$NON-NLS-2$
				.append(XML_ELEMENT_SUBOPERATION).append(">").append(signConfig.getCryptoOperation()) //$NON-NLS-1$
				.append("</").append(XML_ELEMENT_SUBOPERATION).append(">\n  <"); //$NON-NLS-1$ //$NON-NLS-2$

		// Para que la configuracion de actualizacion de la firma llegue al DocumentManager del
		// servidor trifasico, la metemos dentro de los extraParams que se le enviaran
		final Properties extraParams = signConfig.getExtraParams();
		if (signConfig.getUpgrade() != null && !signConfig.getUpgrade().isEmpty()) {
			extraParams.setProperty(PARAM_UPGRADE_FORMAT, signConfig.getUpgrade());
		}

		sb.append(XML_ELEMENT_EXTRAPARAMS).append(">").append(ServiceUtil.properties2Base64(extraParams)) //$NON-NLS-1$
			.append("</").append(XML_ELEMENT_EXTRAPARAMS).append(">\n </singlesign>"); //$NON-NLS-1$ //$NON-NLS-2$

		return sb.toString();
	}

	/**
	 * Extrae de las propiedades de configuraci&oacute;n del Cliente @firma las
	 * propiedades correspondientes a los filtros de certificados.
	 * @param extraParams Propiedades de configuracion. Se modifica el objeto de entrada
	 * eliminando las propiedades de los certificados.
	 * @return Cadena de texto con las claves y propiedades concatenadas para la
	 * configruaci&oacute;n de los filtros de certificados.
	 */
	static String extractCertFiltersParams(final Properties extraParams) {
		final StringBuilder filters = new StringBuilder();
		for (final String k : extraParams.keySet().toArray(new String[extraParams.size()])) {
			if (k.equals(AFIRMA_EXTRAPARAM_FILTER) ||
					k.equals(AFIRMA_EXTRAPARAM_FILTERS) ||
					k.startsWith(AFIRMA_EXTRAPARAM_ORDER_FILTERS_PREFIX) ||
					k.equals(AFIRMA_EXTRAPARAM_HEADLESS)) {
				if (filters.length() != 0) {
					filters.append("\\n"); //$NON-NLS-1$
				}
				filters.append(k).append("=").append(extraParams.getProperty(k)); //$NON-NLS-1$
				extraParams.remove(k);
			}
		}
		return filters.length() != 0 ? filters.toString() : null;
	}
}
