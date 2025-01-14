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

import java.util.Properties;

/**
 * Clase de utilidad para la construcci&oacute;n de un JSON de definicion de lote
 * de firma para el cliente @firma.
 */
public class MiniAppletHelper {

	/** Nombre del par&aacute;metro para la configuraci&oacute;n del formato avanzado
	 * al que actualizar las firmas en el proceso de firma trif&aacute;sico. */
	public static final String PARAM_UPGRADE_FORMAT = "upgradeFormat"; //$NON-NLS-1$

	private static final String AFIRMA_EXTRAPARAM_FILTER = "filter"; //$NON-NLS-1$
	private static final String AFIRMA_EXTRAPARAM_FILTERS = "filters"; //$NON-NLS-1$
	private static final String AFIRMA_EXTRAPARAM_ORDER_FILTERS_PREFIX = "filters."; //$NON-NLS-1$
	public static final String AFIRMA_EXTRAPARAM_HEADLESS = "headless"; //$NON-NLS-1$

	/**
	 * Extrae de las propiedades de configuraci&oacute;n del Cliente @firma las
	 * propiedades correspondientes a los filtros de certificados.
	 * @param extraParams Propiedades de configuracion. Se modifica el objeto de entrada
	 * eliminando las propiedades de los certificados.
	 * @return Cadena de texto con las claves y propiedades concatenadas para la
	 * configuraci&oacute;n de los filtros de certificados.
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
