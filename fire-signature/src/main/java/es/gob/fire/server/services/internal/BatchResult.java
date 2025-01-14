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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.afirma.core.misc.Base64;
import es.gob.fire.server.connector.OperationResult;
import es.gob.fire.server.services.DocInfo;
import es.gob.fire.upgrade.GracePeriodInfo;

/**
 * Resultado de un proceso de firma de lote.
 * @author Carlos Gamuci
 */
public class BatchResult extends OperationResult implements Serializable {

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
	/** Error que denota que la firma generada es inv&aacute;lida. */
	public static final String INVALID_SIGNATURE = "INVALID_SIGNATURE"; //$NON-NLS-1$
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
	/** Estado que indica que es necesario esperar un periodo de gracia para recuperar la firma. */
	public static final String GRACE_PERIOD = "GRACE_PERIOD"; //$NON-NLS-1$

	public static final int WITHOUT_ERRORS = 0;
	public static final int ANY_FAILED = 1;
	public static final int ALL_FAILED = 2;

	private static final String JSON_ATTR_PROVIDER_NAME = "prov"; //$NON-NLS-1$
	private static final String JSON_ATTR_SIGNING_CERT = "cert"; //$NON-NLS-1$
	private static final String JSON_ATTR_BATCH_RESULT = "batch"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_ID = "id"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_OK = "ok"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_DETAILS = "dt"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_ERROR_MESSAGE = "errorm"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_GRACE_PERIOD = "grace"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_GP_ID = "id"; //$NON-NLS-1$
	private static final String JSON_ATTR_DOC_GP_DATE = "date"; //$NON-NLS-1$

	private final Map<String, BatchDocumentReference> results;

	private String providerName = null;

	private X509Certificate signingCertificate = null;

	/** Construye el objeto con el resultado del lote. */
	public BatchResult() {
		this.results = new HashMap<>();
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
	 * Agrega un documento al lote de firma.
	 * @param docId Identificador del documento.
	 * @param dataReference Referencia a donde est&aacute; almacenado el documento.
	 * @param config Configuraci&oacute;n de firma para este documento particular.
	 * @param docInfo Informaci&oacute;n del documento.
	 */
	public void addDocument(final String docId, final String dataReference, final SignBatchConfig config, final DocInfo docInfo) {
		BatchDocumentReference docRef;

		if (docInfo != null && (docInfo.getName() != null || docInfo.getTitle() != null || docInfo.getSize() > 0)) {
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
	 * Recupera el periodo de gracia establecido antes de recuperar la firma.
	 * @param docId Identificador del documento.
	 * @return Periodo de gracia solicitado para la recuperaci&oacute;n de la firma o {@code null}
	 * si no se estableci&oacute; un periodo de gr&aacute;cia.
	 */
	public GracePeriodInfo getGracePeriod(final String docId) {
		final BatchDocumentReference docRef = this.results.get(docId);
		return docRef == null ? null : docRef.getGracePeriod();
	}

	/**
	 * Establece que un documento del resultado se ha firmado correctamente.
	 * @param docId Identificador del documento del que se desea establecer el resultado.
	 */
	public void setSuccessResult(final String docId) {
		this.results.get(docId).setSuccessResult();
	}

	/**
	 * Establece que un documento del resultado se ha firmado correctamente, pero que
	 * hay que esperar para recuperar el resultado.
	 * @param docId Identificador del documento del que se desea establecer el resultado.
	 * @param gracePeriod Periodo de gracia que hay que esperar para recuperar la firma.
	 */
	public void setSuccessResult(final String docId, final GracePeriodInfo gracePeriod) {
		this.results.get(docId).setSuccessResult(gracePeriod);
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
	 * Indica si la firma correspondiente a un documento del lote se recuper&oacute;
	 * anteriormente.
	 * @param docId Identificador del documento.
	 * @return {@code true} en caso de que la firma ya se recuperase, {@code false}
	 * en caso contrario.
	 */
	public boolean isSignRecovered(final String docId) {
		final BatchDocumentReference docRef = this.results.get(docId);
		if (docRef == null) {
			return false;
		}
		final String error = docRef.getDetails();
		return RECOVERED.equals(error);
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
		return !docRef.isSigned() && !PENDING.equals(error);
	}

	/**
	 * Comprueba si el resultado contiene firmas err&oacute;neas. El valor de retorno
	 * indica si no hay firmas erroneas, si hay alguna o si todas fallaron.
	 * @return {@code 0} si ninguna firma ha fallado todavia, {code 1} si alguna fall&oacute;
	 * y {@code 2} si fallaron todas.
	 */
	public synchronized int hasErrors() {

		int errors = 0;
		for (final BatchDocumentReference ref : this.results.values()) {
			if (ref != null && !ref.isSigned() && !PENDING.equals(ref.getDetails())) {
				errors++;
			}
		}
		int result;
		if (errors == 0) {
			result = WITHOUT_ERRORS;
		} else if (errors < this.results.size()) {
			result = ANY_FAILED;
		} else {
			result = ALL_FAILED;
		}
        return result;
	}

	public synchronized void setErrorMessage(final String docId, final String errorMessage) {
		this.results.get(docId).setErrorMessage(errorMessage);
	}

	public String getErrorMessage(final String docId) {
		final BatchDocumentReference docRef = this.results.get(docId);
		if (docRef == null) {
			return null;
		}
		return docRef.getErrorMessage();
	}

	/**
	 * Indica si la firma requiere que se espere un periodo de gracia.
	 * @param docId Identificador del documento.
	 * @return {@code true} en caso de que la firma tenga asignado un periodo de gracia,
	 * {@code false} en caso contrario.
	 */
	public boolean needWaitGracePeriod(final String docId) {
		final BatchDocumentReference docRef = this.results.get(docId);
		if (docRef == null) {
			return false;
		}
		return docRef.getGracePeriod() != null;
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
	 * @param charset Juego de caracteres.
	 * @return Resultado codificado en forma de JSON.
	 */
	@Override
	public byte[] encodeResult(final Charset charset) {

		// Si no tenemos resultado, devolvemos un JSON con la informacion que se
		// dispone de la transaccion
		final JsonArrayBuilder resultBuilder = Json.createArrayBuilder();

		final Iterator<String> keys = this.results.keySet().iterator();
		while (keys.hasNext()) {
			final String id = keys.next();
			final BatchDocumentReference result = this.results.get(id);

			final JsonObjectBuilder docInfo = Json.createObjectBuilder()
			.add(JSON_ATTR_DOC_ID, id)
			.add(JSON_ATTR_DOC_OK, result.isSigned())
			.add(JSON_ATTR_DOC_DETAILS, result.getDetails() != null ? result.getDetails() : "") //$NON-NLS-1$
			.add(JSON_ATTR_DOC_ERROR_MESSAGE, result.getErrorMessage() != null ? result.getErrorMessage() : "");//$NON-NLS-1$

			// Si hay informacion del periodo de gracia, la agregamos
			if (result.getGracePeriod() != null) {
				final JsonObjectBuilder gracePeriod = Json.createObjectBuilder()
				.add(JSON_ATTR_DOC_GP_ID, result.getGracePeriod().getResponseId())
				.add(JSON_ATTR_DOC_GP_DATE, result.getGracePeriod().getResolutionDate().getTime());
				docInfo.add(JSON_ATTR_DOC_GRACE_PERIOD, gracePeriod);
			}

			resultBuilder.add(docInfo);
		}

		// Construimos la respuesta
		byte[] result = null;
		try (	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final Writer w = new OutputStreamWriter(baos, charset);
				final JsonWriter json = Json.createWriter(w);) {

			final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			jsonBuilder.add(JSON_ATTR_PROVIDER_NAME, this.providerName);
			if (this.signingCertificate != null) {
				try {
					jsonBuilder.add(JSON_ATTR_SIGNING_CERT, Base64.encode(this.signingCertificate.getEncoded()));
				} catch (final CertificateEncodingException e) {
					// Error al codificar el certificado, no se devolvera certificado en ese caso
					Logger.getLogger(BatchResult.class.getName()).log(
							Level.WARNING,
							"Error al codificar el certificado de firma", //$NON-NLS-1$
							e);
				}
			}
			jsonBuilder.add(JSON_ATTR_BATCH_RESULT, resultBuilder);

			json.writeObject(jsonBuilder.build());
			w.flush();

			result = baos.toByteArray();
		}
		catch (final IOException e) {
			// Error en el cierre de los flujos de datos, no deberia afectar al resultado
			Logger.getLogger(BatchResult.class.getName()).log(
					Level.WARNING,
					"Error en el cierre del flujo de datos al devolver el resultado del lote", //$NON-NLS-1$
					e);
		}
		return result;
	}

	private static class BatchDocumentReference implements Serializable {

		/** Serial Id. */
		private static final long serialVersionUID = 8711273935320975833L;

		private final String dataReference;
		private boolean signed;
		private String details;
		private String errorMessage;
		private final SignBatchConfig config;
		private final DocInfo docInfo;
		private GracePeriodInfo gracePeriod = null;

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

		public void setSuccessResult(final GracePeriodInfo gracePeriod) {
			this.signed = true;
			this.details = GRACE_PERIOD;
			this.gracePeriod = gracePeriod;
		}

		public void setErrorResult(final String error) {
			this.signed = false;
			this.details = error;
		}

		public void setErrorMessage(final String errorMessage) {
			this.errorMessage = errorMessage;
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

		public String getErrorMessage()	{
			return this.errorMessage;
		}

		public SignBatchConfig getSignConfig() {
			return this.config;
		}

		public DocInfo getDocInfo() {
			return this.docInfo;
		}

		public GracePeriodInfo getGracePeriod() {
			return this.gracePeriod;
		}
	}
}
