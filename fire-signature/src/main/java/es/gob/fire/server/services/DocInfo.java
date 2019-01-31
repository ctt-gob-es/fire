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

import java.io.Serializable;
import java.util.Properties;

import es.gob.afirma.core.signers.TriphaseData.TriSign;

/**
 * Clase para almacenar informaci&oacute;n del documento firmado.
 */
public class DocInfo implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 5411072283426781574L;

	private static final String PROPERTY_DOC_TITLE = "docTitle"; //$NON-NLS-1$

	private static final String PROPERTY_DOC_NAME = "docName"; //$NON-NLS-1$

	//private static final String PROPERTY_DOC_SIZE ="docSize"; //$NON-NLS-1$

	private String title = null;
	private String name = null;
	private long size = 0;


	/**
	 * Recupera el t&iacute;tulo del documento.
	 * @return T&iacute;tulo del documento.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Recupera el nombre del documento.
	 * @return Nombre del documento.
	 */
	public String getName() {
		return this.name;
	}

	private void setName(final String name) {
		this.name = name;
	}

	private void setTitle(final String title) {
		this.title = title;
	}

	public final long getSize() {
		return this.size;
	}

	public final void setSize(final long size) {
		this.size = size;
	}

	/**
	 * Extrae la informaci&oacute;n declarada del documento que se firma de
	 * la configuraci&oacute;n y la elimina de la misma.
	 * @param config Configuraci&oacute;n de firma.
	 * @return Informaci&oacute;n del documento.
	 */
	public static DocInfo extractDocInfo(final Properties config) {

		final DocInfo docInfo = new DocInfo();
		docInfo.setTitle(extractProperty(config, PROPERTY_DOC_TITLE));
		docInfo.setName(extractProperty(config, PROPERTY_DOC_NAME));

		return docInfo;
	}


	/**
	 * Extrae el valor de una propiedad de un properties y elimina la propiedad
	 * de ese properties.
	 * @param properties Conjunto de propiedades.
	 * @param property Nombre de la propiedad de la que obtener el valor.
	 * @return Valor de la propiedad o {@code null} si no estaba establecido.
	 */
	private static String extractProperty(final Properties properties, final String property) {
		String value = null;
		if (properties != null && properties.containsKey(property)) {
			value = properties.getProperty(property);
			properties.remove(property);
		}
		return value;
	}

	/**
	 * Extrae la informaci&oacute;n configurada sobre el documento que se va a firmar.
	 * @param preTrisSign Firma trif&aacute;sica parcial del documento.
	 * @return Informaci&oacute;n del documento.
	 */
	public static DocInfo extractDocInfo(final TriSign preTrisSign) {
		final DocInfo docInfo = new DocInfo();
		if (preTrisSign != null) {
			docInfo.setTitle(preTrisSign.getProperty(PROPERTY_DOC_TITLE));
			docInfo.setName(preTrisSign.getProperty(PROPERTY_DOC_NAME));
		}
		return docInfo;
	}

	/**
	 * Obtiene una firma ampliada con las propiedades correspondiente a la informaci&oacute;n
	 * del documento que firm&oacute;.
	 * @param sign Informaci&oacute;n de firma trif&aacute;sica.
	 * @param docInfo Informaci&oacute;n del documento que se firma.
	 */
	public static void addDocInfoToSign(final TriSign sign, final DocInfo docInfo) {
		if (sign == null || docInfo == null) {
			return;
		}

		if (docInfo.getTitle() != null) {
			sign.addProperty(PROPERTY_DOC_TITLE, docInfo.getTitle());
		}

		if (docInfo.getName() != null) {
			sign.addProperty(PROPERTY_DOC_NAME, docInfo.getName());
		}
	}

	/**
	 * Agrega a la informaci&oacute;n de una firma, la informaci&oacute;n del documento que firma.
	 * @param signConfig Configuraci&oacute;n de firma.
	 * @param docInfo Informaci&oacute;n del documento que se firma.
	 */
	public static void addDocInfoToSign(final Properties signConfig, final DocInfo docInfo) {
		if (signConfig == null || docInfo == null) {
			return;
		}

		if (docInfo.getTitle() != null) {
			signConfig.setProperty(PROPERTY_DOC_TITLE, docInfo.getTitle());
		}

		if (docInfo.getName() != null) {
			signConfig.setProperty(PROPERTY_DOC_NAME, docInfo.getName());
		}
	}
}
