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

import es.gob.fire.signature.DocInfo;

/**
 * Documento a firmar dentro de un proceso de lote.
 */
public class BatchDocument {

	private final String id;

	private final byte[] data;

	private String result;

	private final SignBatchConfig config;

	private final DocInfo docInfo;

	/**
	 * Construye el documento.
	 * @param id Identificador del documento.
	 * @param data Contenido del documento.
	 */
	public BatchDocument(final String id, final byte[] data) {
		this.id = id;
		this.data = data;
		this.result = null;
		this.config = null;
		this.docInfo = null;
	}

	/**
	 * Construye el documento.
	 * @param id Identificador del documento.
	 * @param data Contenido del documento.
	 * @param config Configuraci&oacute;n de firma particular para este documento.
	 */
	public BatchDocument(final String id, final byte[] data, final SignBatchConfig config) {
		this.id = id;
		this.data = data;
		this.result = null;
		this.config = config;
		this.docInfo = null;
	}

	/**
	 * Construye el documento.
	 * @param id Identificador del documento.
	 * @param data Contenido del documento.
	 * @param config Configuraci&oacute;n de firma particular para este documento.
	 * @param docInfo Informaci&oacute;n del documento.
	 */
	public BatchDocument(final String id, final byte[] data, final SignBatchConfig config, final DocInfo docInfo) {
		this.id = id;
		this.data = data;
		this.result = null;
		this.config = config;
		this.docInfo = docInfo;
	}

	/**
	 * Establece el resultado parcial de la firma de este documento.
	 * @param result Resultado parcial de la firma.
	 */
	public void setBatchResult(final String result) {
		this.result = result;
	}

	/**
	 * Recupera el identificador del documento.
	 * @return Identificador del documento.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Recupera el contenido del documento.
	 * @return Contenido del documento.
	 */
	public byte[] getData() {
		return this.data;
	}

	/**
	 * Resultado parcial de la operaci&oacute;n de firma.
	 * @return Indicador de un error producido durante la firma de los datos,
	 * o {@code null} si no se ha producido ning&uacute;n error.
	 */
	public String getResult() {
		return this.result;
	}

	/**
	 * Recupera la configuraci&oacute;n de firma particular para este documento.
	 * @return Configuraci&oacute;n de firma particular o {@code null} si no la tiene.
	 */
	public SignBatchConfig getConfig() {
		return this.config;
	}

	/**
	 * Recupera la informaci&oacute;n disponible del documento.
	 * @return Informaci&oacute;n del documenot o {@code null} si no la tiene.
	 */
	public DocInfo getDocInfo() {
		return this.docInfo;
	}
}
