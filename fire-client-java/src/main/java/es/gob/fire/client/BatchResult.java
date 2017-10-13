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
import java.util.HashMap;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Resultado de una operaci&oacute;n de firma de lote.
 */
public class BatchResult extends HashMap<String, SignBatchResult> {

	/** Serial Id. */
	private static final long serialVersionUID = 1219447718358370365L;

	private static final String JSON_OBJECT = "batch"; //$NON-NLS-1$

	private static final String JSON_FIELD_ID = "id"; //$NON-NLS-1$

	private static final String JSON_FIELD_OK = "ok"; //$NON-NLS-1$

	private static final String JSON_FIELD_DETAIL = "dt"; //$NON-NLS-1$

	private boolean error = false;

	private BatchResult() {
		// Impedimos la creacion directa de objetos
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

		final JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(json));
		final JsonObject mainObject = jsonReader.readObject();
		final JsonArray jsonArray = mainObject.getJsonArray(JSON_OBJECT);
		for (int i = 0; i < jsonArray.size(); i++) {
			final JsonObject jsonObject = jsonArray.getJsonObject(i);
			final String id = jsonObject.getString(JSON_FIELD_ID);
			final boolean ok = Boolean.parseBoolean(jsonObject.getString(JSON_FIELD_OK));
			String dt = null;
			if (jsonObject.containsKey(JSON_FIELD_DETAIL)) {
				dt = jsonObject.getString(JSON_FIELD_DETAIL);
			}

			result.put(id, ok ? new SignBatchResult() : new SignBatchResult(dt));
			if (!ok) {
				result.error = true;
			}
		}
		jsonReader.close();

		return result;
	}
}
