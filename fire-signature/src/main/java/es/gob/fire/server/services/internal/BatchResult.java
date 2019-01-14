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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.server.services.DocInfo;

/**
 * Resultado de un proceso de firma de lote.
 * @author Carlos Gamuci
 */
public class BatchResult implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 4852042946330169315L;

	/** Estado que indica que el documento no ha sido procesado en ning&uacute;n momento. */
	public static final String PENDING = "PENDING"; //$NON-NLS-1$
	/** Error que denota que un documento no se ha llegado a procesar. */
	public static final String NO_PROCESSED = "NO_PROCESSED"; //$NON-NLS-1$
	/** Error que denota que no se encontro un documento. */
	public static final String DATA_NOT_FOUND = "DATA_NOT_FOUND"; //$NON-NLS-1$
	/** Error que denota un problema durante la prefirma del documento. */
	public static final String PRESIGN_ERROR = "PRESIGN_ERROR"; //$NON-NLS-1$
	/** Error que denota un problema durante la postfirma del documento. */
	public static final String POSTSIGN_ERROR = "POSTSIGN_ERROR"; //$NON-NLS-1$
	/** Error que denota que no se pudo actualizar la firma al formato avanzado solicitado. */
	public static final String UPGRADE_ERROR = "UPGRADE_ERROR"; //$NON-NLS-1$
	/** Error que denota un problema durante el guardado de la firma. */
	public static final String ERROR_SAVING_DATA = "ERROR_SAVING_DATA"; //$NON-NLS-1$
	/** Error que denota que se configur&oacute; una operaci&oacute;n de firma no v&aacute;lida. */
	public static final String INVALID_SIGNATURE_OPERATION = "INVALID_SIGNATURE_OPERATION"; //$NON-NLS-1$
	/** Error que denota que se abort&oacute; la operaci&oacute;n (incluso despu&eacute;s de terminar) al detectar un error. */
	public static final String ABORTED = "ABORTED"; //$NON-NLS-1$
	/** Error que denota un problema al recuperar una firma generada. */
	public static final String ERROR_RECOVERING = "ERROR_RECOVERING"; //$NON-NLS-1$
	/** Estado que indica que la firma ya se ha recuperado. */
	public static final String RECOVERED = "RECOVERED"; //$NON-NLS-1$

	private static final String JSON_ATTR_PROVIDER_NAME = "prov"; //$NON-NLS-1$
	private static final String JSON_ATTR_BATCH_RESULT = "batch"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_ID = "id"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_OK = "ok"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_DETAILS = "dt"; //$NON-NLS-1$

	private final Map<String, BatchDocumentReference> results;

	private X509Certificate signingCertificate;

	private String providerName = null;

	/** Construye el objeto con el resultado del lote. */
	public BatchResult() {
		this.results = new HashMap<>();
		this.signingCertificate = null;
	}


	/**
	 * Devuelve el nombre de proveedor utilizado para la firma.
	 * @return Nombre de proveedor.
	 */
	public String getProviderName() {
		return this.providerName;
	}

	/**
	 * Establece el nombre del proveedor utilizado para la firma.
	 * @param provName Nombre del proveedor.
	 */
	public void setProviderName(final String provName) {
		this.providerName = provName;
	}

	/**
	 * Agrega un documento al lote de firma.
	 * @param docId Identificador del documento.
	 * @param dataReference Referencia a donde est&aacute; almacenado el documento.
	 * @param config Configuraci&oacute;n de firma para este documento particular.
	 * @param docInfo Informaci&oacute;n del documento.
	 */
	public void addDocument(final String docId, final String dataReference, final SignBatchConfig config, final DocInfo docInfo) {
		BatchDocumentReference docRef;
		if (docInfo != null && (docInfo.getName() != null || docInfo.getTitle() != null)) {
			docRef = new BatchDocumentReference(dataReference, config, docInfo);
		}
		else {
			docRef = new BatchDocumentReference(dataReference, config);
		}
		this.results.put(docId, docRef);

	}

	/**
	 * Recupera un iterador sobre el listado de identificadores de documentos.
	 * @return Iterador sobre los identificadores de documentos.
	 */
	public Iterator<String> iterator() {
		return this.results.keySet().iterator();
	}

	/**
	 * Obtiene el certificado utilizado para firmar.
	 * @return Certificado utilizado para firmar o {@code null} si no
	 * se ha firmado todav&iacute;a.
	 */
	public X509Certificate getSigningCertificate() {
		return this.signingCertificate;
	}

	/**
	 * Establece el certificado utilizado para firmar.
	 * @param cert Certificado utilizado para firmar.
	 */
	public void setSigningCertificate(final X509Certificate cert) {
		this.signingCertificate = cert;
	}

	/**
	 * Obtiene la referencia de un documeno para la carga de su contenido.
	 * @param docId Identificador del documento.
	 * @return Referencia para la carga del documento o {@code null} si no
	 * se encontr&oacute; el documento.
	 */
	public String getDocumentReference(final String docId) {
		final BatchDocumentReference docRef = this.results.get(docId);
		return docRef == null ? null : docRef.getDataReference();
	}

	/**
	 * Recupera la configuraci&oacute;n particular de una firma.
	 * @param docId Identificador del documento.
	 * @return Configuraci&oacute;n particular de firma para el documento
	 * indicado o {@code null} si no se encontr&oacute; el documento.
	 */
	public SignBatchConfig getSignConfig(final String docId) {
		final BatchDocumentReference docRef = this.results.get(docId);
		return docRef == null ? null : docRef.getSignConfig();
	}

	/**
	 * Recupera la informaci&oacute;n disponible del documento.
	 * @param docId Identificador del documento.
	 * @return Informaci&oacute;n del documento  indicado o {@code null}
	 * si no se encontr&oacute; el documento o no se tiene informaci&oacute;n.
	 */
	public DocInfo getDocInfo(final String docId) {
		final BatchDocumentReference docRef = this.results.get(docId);
		return docRef == null ? null : docRef.getDocInfo();
	}

	/**
	 * Establece que un documento del resultado se ha firmado correctamente.
	 * @param docId Identificador del documento del que se desea establecer el resultado.
	 */
	public void setSuccessResult(final String docId) {
		this.results.get(docId).setSuccessResult();
	}

	/**
	 * Establece que un documento del resultado no se ha firmado.
	 * @param docId Identificar del documento del que se desea establecer el resultado.
	 * @param error Detalle del error.
	 */
	public synchronized void setErrorResult(final String docId, final String error) {
		this.results.get(docId).setErrorResult(error);
	}

	/**
	 * Indica si la firma correspondiente a un documento del lote ha fallado.
	 * @param docId Identificador del documento.
	 * @return {@code true} en caso de que la firma fallase, {@code false} si
	 * la firma termin&oacute; correctamento o a&uacute;n no se ha iniciado el
	 * proceso de firma.
	 */
	public boolean isSignFailed(final String docId) {
		final BatchDocumentReference docRef = this.results.get(docId);
		if (docRef == null) {
			return false;
		}
		final String error = docRef.getDetails();
		return error != null && !PENDING.equals(error);
	}

	/**
	 * Devuelve el n&uacute;mero de documentos registrados para firmar.
	 * @return N&uacute;mero de documentos.
	 */
	public int documentsCount() {
		return this.results.size();
	}

	/**
	 * Comprueba si existe un documento en el lote con el identificador indicado.
	 * @param docId Identificador de documento.
	 * @return {@code true} si existe un documento con ese identificado en el lote,
	 * {@code false} en caso contrario.
	 */
	public boolean hasDocument(final String docId) {
		return this.results.containsKey(docId);
	}

	/**
	 * Compone un objeto JSON con la informaci&oacute;n y el resultado de
	 * la operaci&oacute;n de firma de lote.
	 * @return Resultado codificado en forma de JSON.
	 */
	public byte[] encode() {

		// Si no tenemos resultado, devolvemos un JSON con la informacion que se
		// dispone de la transaccion
		final JsonArrayBuilder resultBuilder = Json.createArrayBuilder();

		final Iterator<String> keys = this.results.keySet().iterator();
		while (keys.hasNext()) {
			final String id = keys.next();
			final BatchDocumentReference result = this.results.get(id);

			resultBuilder.add(
					Json.createObjectBuilder()
					.add(JSON_ATTR_DOC_ID, id)
					.add(JSON_ATTR_DOC_OK, result.isSigned())
					.add(JSON_ATTR_DOC_DETAILS, result.getDetails() != null ? result.getDetails() : "") //$NON-NLS-1$
					);
		}

		// Construimos la respuesta
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final JsonWriter json = Json.createWriter(baos);) {

			final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			jsonBuilder.add(JSON_ATTR_PROVIDER_NAME, this.providerName);
			jsonBuilder.add(JSON_ATTR_BATCH_RESULT, resultBuilder);

			json.writeObject(jsonBuilder.build());
		}

		return baos.toByteArray();
	}

	@Override
	public String toString() {
		return new String(encode());
	}

	private class BatchDocumentReference implements Serializable {

		/** Serial Id. */
		private static final long serialVersionUID = 8711273935320975833L;

		private final String dataReference;
		private boolean signed;
		private String details;
		private final SignBatchConfig config;
		private final DocInfo docInfo;

		public BatchDocumentReference(final String dataReference, final SignBatchConfig config) {
			this.dataReference = dataReference;
			this.signed= false;
			this.details = PENDING;
			this.config = config;
			this.docInfo = null;
		}

		public BatchDocumentReference(final String dataReference, final SignBatchConfig config, final DocInfo docInfo) {
			this.dataReference = dataReference;
			this.signed= false;
			this.details = PENDING;
			this.config = config;
			this.docInfo = docInfo;
		}

		public void setSuccessResult() {
			this.signed = true;
			this.details = null;
		}

		public void setErrorResult(final String error) {
			this.signed = false;
			this.details = error;
		}

		public String getDataReference() {
			return this.dataReference;
		}

		public boolean isSigned() {
			return this.signed;
		}

		public String getDetails() {
			return this.details;
		}

		public SignBatchConfig getSignConfig() {
			return this.config;
		}

		public DocInfo getDocInfo() {
			return this.docInfo;
		}
	}
}
