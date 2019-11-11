/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

/**
 * Resultado de una operaci&oacute;n de firma de lote.
 */
public class BatchResult extends HashMap<String, SignBatchResult> {

	/** Serial Id. */
	private static final long serialVersionUID = 1219447718358370365L;

	private static final String JSON_OBJECT = "batch"; //$NON-NLS-1$

	private static final String JSON_FIELD_PROVIDER_NAME = "prov"; //$NON-NLS-1$

	private static final String JSON_FIELD_SIGNING_CERT = "cert"; //$NON-NLS-1$

	private static final String JSON_FIELD_ID = "id"; //$NON-NLS-1$

	private static final String JSON_FIELD_OK = "ok"; //$NON-NLS-1$

	private static final String JSON_FIELD_DETAIL = "dt"; //$NON-NLS-1$

	private static final String JSON_FIELD_GRACE_PERIOD = "grace"; //$NON-NLS-1$

	private static final String JSON_FIELD_GP_ID = "id"; //$NON-NLS-1$

	private static final String JSON_FIELD_GP_DATE = "date"; //$NON-NLS-1$

	private static final String DEFAULT_CHARSET = "utf-8"; //$NON-NLS-1$

	private boolean error = false;

	private String providerName = null;

	private X509Certificate signingCert = null;

	private BatchResult() {
		// Impedimos la creacion directa de objetos
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
	 * Recupera el certificado utilizado para la firma.
	 * @return Certificado de firma.
	 */
	public X509Certificate getSigningCert() {
		return this.signingCert;
	}

	/**
	 * Establece el certificado utilizado para la firma.
	 * @param Certificado de firma.
	 */
	public void setSigningCert(final X509Certificate signingCert) {
		this.signingCert = signingCert;
	}

	/**
	 * Indica si alguna de las firmas del lote no finaliz&oacute; o
	 * finaliz&oacute; con errores.
	 * @return {@code true} si hubo firmas que no finalizaron correctamente,
	 * {@code false} en caso contrario.
	 */
	public boolean isError() {
		return this.error;
	}

	@Override
	public String toString() {

		final StringBuilder buffer = new StringBuilder();
		buffer.append("{\"batch\":["); //$NON-NLS-1$
		final Iterator<String> keys = keySet().iterator();
		while (keys.hasNext()) {
			final String id = keys.next();
			final SignBatchResult result = get(id);
			buffer.append("{\"id\": \"").append(id).append("\"") //$NON-NLS-1$ //$NON-NLS-2$
			.append(", \"ok\": \"").append(result.isSigned()).append("\"") //$NON-NLS-1$ //$NON-NLS-2$
			.append(", \"dt\": \"").append(result.getErrotType() != null ? result.getErrotType() : "").append("\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (result.getGracePeriod() != null) {
				buffer.append(", \"gp\": {\"id\":\"").append(result.getGracePeriod().getResponseId()) //$NON-NLS-1$
					.append("\", \"dt\":\"").append(result.getGracePeriod().getResolutionDate().getTime()) //$NON-NLS-1$
					.append("\"}"); //$NON-NLS-1$
			}
			buffer.append("}"); //$NON-NLS-1$
			if (keys.hasNext()) {
				buffer.append(","); //$NON-NLS-1$
			}
		}
		buffer.append("]}"); //$NON-NLS-1$

		return buffer.toString();
	}

	/**
	 * Parsea un JSON con el resultado de la firma de un lote.
	 * @param json JSON con el resultado de la firma del lote.
	 * @return Objeto con el listado de identificadores de los
	 * documentos y el resultado de firmarlos.
	 */
	public static BatchResult parse(final byte[] json) {

		final BatchResult result = new BatchResult();

		Reader reader;
		try {
			reader = new InputStreamReader(new ByteArrayInputStream(json), DEFAULT_CHARSET);
		}
		catch (final Exception e) {
			// Nunca deberia ocurrir
			throw new RuntimeException("Error al componer el objeto de lectura de los datos", e); //$NON-NLS-1$
		}

		final JsonReader jsonReader = Json.createReader(reader);
		final JsonObject mainObject = jsonReader.readObject();
		final JsonString providerName = mainObject.getJsonString(JSON_FIELD_PROVIDER_NAME);
		result.setProviderName(providerName.getChars().toString());

		if (mainObject.containsKey(JSON_FIELD_SIGNING_CERT)) {
			final JsonString signingCertB64 = mainObject.getJsonString(JSON_FIELD_SIGNING_CERT);
			if (signingCertB64 != null) {
				try {
					result.setSigningCert(decodeCertificate(signingCertB64.getChars().toString()));
				}
				catch (final Exception e) {
					jsonReader.close();
					throw new RuntimeException("El certificado de firma proporcionado no esta bien codificado", e); //$NON-NLS-1$
				}
			}
		}

		final JsonArray jsonArray = mainObject.getJsonArray(JSON_OBJECT);
		for (int i = 0; i < jsonArray.size(); i++) {
			final JsonObject jsonObject = jsonArray.getJsonObject(i);
			final String id = jsonObject.getString(JSON_FIELD_ID);
			final boolean ok = jsonObject.getBoolean(JSON_FIELD_OK);
			String dt = null;
			if (jsonObject.containsKey(JSON_FIELD_DETAIL)) {
				dt = jsonObject.getString(JSON_FIELD_DETAIL);
			}
			GracePeriodInfo gp = null;
			if (jsonObject.containsKey(JSON_FIELD_GRACE_PERIOD)) {
				final JsonObject gpObject = jsonObject.getJsonObject(JSON_FIELD_GRACE_PERIOD);
				final String gpId = gpObject.getString(JSON_FIELD_GP_ID);
				final long gpMillis = gpObject.getJsonNumber(JSON_FIELD_GP_DATE).longValue();
				gp = new GracePeriodInfo(gpId, new Date(gpMillis));
			}

			final SignBatchResult batchResult = gp != null ?
					new SignBatchResult(gp) : new SignBatchResult(ok, dt);
			result.put(id, batchResult);
			if (!ok) {
				result.error = true;
			}
		}
		jsonReader.close();

		return result;
	}

	/**
	 * Compone un certificado.
	 * @param certB64 Certificao en base 64.
	 * @return Certificado X509.
	 * @throws CertificateException Cuando la cadena indicada no se corresponde con un certificado.
	 * @throws IOException Cuando ocurre un error al leer el certificado.
	 */
	private static X509Certificate decodeCertificate(final String certB64) throws CertificateException, IOException {
		return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
				new ByteArrayInputStream(Base64.decode(certB64))
				);
	}
}
