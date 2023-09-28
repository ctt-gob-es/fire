/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.batch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.server.services.batch.ProcessResult.Result;

/** Lote de firmas electr&oacute;nicas */
public abstract class SignBatch {

	private static final String JSON_ELEMENT_ID = "id"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_DATAREFERENCE = "datareference"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_FORMAT = "format"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_ALGORITHM = "algorithm"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_SINGLESIGNS = "singlesigns"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_SUBOPERATION = "suboperation"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_STOPONERROR = "stoponerror"; //$NON-NLS-1$
	private static final String JSON_ELEMENT_EXTRAPARAMS = "extraparams"; //$NON-NLS-1$

	private static final String JSELEM_TD = "td"; //$NON-NLS-1$
	private static final String JSELEM_RESULTS = "results"; //$NON-NLS-1$
	private static final String JSELEM_ID = "id"; //$NON-NLS-1$
	private static final String JSELEM_RESULT = "result"; //$NON-NLS-1$
	private static final String JSELEM_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String JSELEM_FORMAT = "format"; //$NON-NLS-1$
	private static final String JSELEM_SIGNS = "signs"; //$NON-NLS-1$

	protected static final Logger LOGGER = Logger.getLogger(SignBatch.class.getName());

	/** Lista de firmas a procesar. */
	protected final List<SingleSign> signs;

	protected SingleSignConstants.SignAlgorithm algorithm = null;

	protected String id;

	protected String extraParams;

	protected SingleSignConstants.SignSubOperation subOperation = null;

	protected SingleSignConstants.SignFormat format = null;


	/** Indica si se debe parar al encontrar un error o por el contrario se debe continuar con el proceso. */
	protected boolean stopOnError = false;

	/**
	 * Ejecuta el preproceso de firma por lote.
	 * @param certChain Cadena de certificados del firmante.
	 * @return Resultados parciales y datos trif&aacute;sicos de pre-firma del lote.
	 * @throws BatchException Cuando hay errores irrecuperables en el preproceso.
	 */
	public abstract JsonObject doPreBatch(final X509Certificate[] certChain) throws BatchException;

	/**
	 * Ejecuta el postproceso de firma por lote.
	 * @param certChain Cadena de certificados del firmante.
	 * @param td Datos trif&aacute;sicos del preproceso.
	 *           Debe contener los datos de todas y cada una de las firmas del lote.
	 * @return Registro del resultado general del proceso por lote, en un JSON (<a href="../doc-files/resultlog-scheme.html">descripci&oacute;n
	 *         del formato</a>).
	 * @throws BatchException Cuando hay errores irrecuperables en el postproceso.
	 */
	public abstract String doPostBatch(final X509Certificate[] certChain,
                                       final TriphaseData td) throws BatchException;

	/**
	 * Crea un lote de firmas a partir de su definici&oacute;n JSON.
	 * @param json JSON de definici&oacute;n de lote de firmas (<a href="./doc-files/batch-scheme.html">descripci&oacute;n
	 *            del formato</a>).
	 * @throws IOException Si hay problemas en el tratamiento de datos en el an&aacute;lisis del JSON.
	 * @throws SecurityException Si se sobrepasa alguna de las limitaciones establecidas para el lote
	 * (n&ueacute;mero de documentos, tama&ntilde;o de las referencias, tama&ntilde;o de documento, etc.)
	 */
	protected SignBatch(final byte[] json) throws IOException, SecurityException {

		if (json == null || json.length < 1) {
			throw new IllegalArgumentException(
				"El JSON de definicion de lote de firmas no puede ser nulo ni vacio" //$NON-NLS-1$
			);
		}

		JsonObject jsonObject = null;
		try {
			try (ByteArrayInputStream bais = new ByteArrayInputStream(json);
				JsonReader reader = Json.createReader(bais)) {
				jsonObject = reader.readObject();
			}
		} catch (final JsonException e){
			LOGGER.severe("Error al parsear JSON: " + e); //$NON-NLS-1$
			throw new JsonException(
					"El JSON de definicion de lote de firmas no esta formado correctamente", e //$NON-NLS-1$
				);
		}

		this.id = UUID.randomUUID().toString();

		this.stopOnError = jsonObject.containsKey(JSON_ELEMENT_STOPONERROR) ?
				jsonObject.getBoolean(JSON_ELEMENT_STOPONERROR) : false;

		if (jsonObject.containsKey(JSON_ELEMENT_ALGORITHM)) {
			this.algorithm = SingleSignConstants.SignAlgorithm.getAlgorithm(
								jsonObject.getString(JSON_ELEMENT_ALGORITHM)
								);
		} else {
			this.algorithm = null;
		}

		if (jsonObject.containsKey(JSON_ELEMENT_FORMAT)) {
			this.format = SingleSignConstants.SignFormat.getFormat(
							jsonObject.getString(JSON_ELEMENT_FORMAT)
							);
		} else {
			this.format = null;
		}

		if (jsonObject.containsKey(JSON_ELEMENT_SUBOPERATION)) {
			this.subOperation = SingleSignConstants.SignSubOperation.getSubOperation(
									jsonObject.getString(JSON_ELEMENT_SUBOPERATION)
								);
		} else {
			this.subOperation = null;
		}

		if (jsonObject.containsKey(JSON_ELEMENT_EXTRAPARAMS)) {
			this.extraParams = jsonObject.getString(JSON_ELEMENT_EXTRAPARAMS);
		} else {
			this.extraParams = null;
		}

		this.signs = fillSingleSigns(jsonObject);
	}


	private List<SingleSign> fillSingleSigns(final JsonObject jsonObject) throws SecurityException {
		final ArrayList<SingleSign> singleSignsList = new ArrayList<>();
		final JsonArray singleSignsArray = jsonObject.getJsonArray(JSON_ELEMENT_SINGLESIGNS);

		if (singleSignsArray != null) {

			for (int i = 0 ; i < singleSignsArray.size() ; i++){

				final JsonObject jsonSingleSign = singleSignsArray.getJsonObject(i);
				final SingleSign singleSign = new SingleSign(jsonSingleSign.getString(JSON_ELEMENT_ID));

				// Cada nodo debe terner una referencia a los datos o el resultado de la operacion
				if (!jsonSingleSign.containsKey(JSON_ELEMENT_DATAREFERENCE) && !jsonSingleSign.containsKey(JSELEM_RESULT)) {
					throw new JsonException("La declaracion del lote no es valida. Todas las firmas deben declarar el atributo " //$NON-NLS-1$
							+ JSON_ELEMENT_DATAREFERENCE + " o " + JSELEM_RESULT); //$NON-NLS-1$
				}

				// Si tiene la referencia a los datos es que la firma aun no se ha completado
				// y tomamos los datos necesarios para hacerlo
				if (jsonSingleSign.containsKey(JSON_ELEMENT_DATAREFERENCE)) {

					final String dataReference = jsonSingleSign.getString(JSON_ELEMENT_DATAREFERENCE);

					singleSign.setDataRef(dataReference);

					singleSign.setProcessResult(null);

					singleSign.setFormat(jsonSingleSign.containsKey(JSON_ELEMENT_FORMAT)
							? SingleSignConstants.SignFormat.getFormat(jsonSingleSign.getString(JSON_ELEMENT_FORMAT))
									: this.format);

					singleSign.setSubOperation(jsonSingleSign.containsKey(JSON_ELEMENT_SUBOPERATION)
							? SingleSignConstants.SignSubOperation.getSubOperation(jsonSingleSign.getString(JSON_ELEMENT_SUBOPERATION))
									: this.subOperation);

					try {
						Properties signExtraParams;
						if (jsonSingleSign.containsKey(JSON_ELEMENT_EXTRAPARAMS)) {
							signExtraParams = AOUtil.base642Properties(jsonSingleSign.getString(JSON_ELEMENT_EXTRAPARAMS));
						} else {
							signExtraParams = AOUtil.base642Properties(this.extraParams);
						}
						singleSign.setExtraParams(signExtraParams);
					} catch (final Exception e) {
						throw new JsonException(
								"El objeto JSON no esta correctamente formado"); //$NON-NLS-1$
					}
				}
				// Si no esta la referencia a los datos, es que ya se ha obtenido un resultado
				else {
					final String result = jsonSingleSign.getString(JSELEM_RESULT);
					String description = null;
					if (jsonSingleSign.containsKey(JSELEM_DESCRIPTION)) {
						description = jsonSingleSign.getString(JSELEM_DESCRIPTION);
					}
					final ProcessResult processResult = new ProcessResult(Result.valueOf(result), description);
					singleSign.setProcessResult(processResult);
				}

				singleSignsList.add(singleSign);
			}
		}

		return singleSignsList;
	}


	protected static JsonObject buildSignResult(final String id, final Result result, final Throwable error) {
		final JsonObjectBuilder jsonResultBuilder = Json.createObjectBuilder();

		jsonResultBuilder.add(JSELEM_ID, id);
		jsonResultBuilder.add(JSELEM_RESULT, result.name());

		if (error != null) {
			jsonResultBuilder.add(JSELEM_DESCRIPTION, error.getMessage());
		}

		return jsonResultBuilder.build();
	}

	protected static JsonObject buildPreBatch(final String format, final JsonArray trisigns, final JsonArray errors) {
		final JsonObjectBuilder preBatch = Json.createObjectBuilder();
		if (trisigns != null && !trisigns.isEmpty()) {
			final JsonObjectBuilder triphaseInfoBuilder = Json.createObjectBuilder();
			triphaseInfoBuilder.add(JSELEM_FORMAT, format);
			triphaseInfoBuilder.add(JSELEM_SIGNS, trisigns);
			preBatch.add(JSELEM_TD, triphaseInfoBuilder.build());
		}
		if (errors != null && !errors.isEmpty()) {
			preBatch.add(JSELEM_RESULTS, errors);
		}
		
		return preBatch.build();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(
			"{\n\"stoponerror\":\"" //$NON-NLS-1$
		);
		sb.append(Boolean.toString(this.stopOnError));
		sb.append("\",\n\"format\":\""); //$NON-NLS-1$
		sb.append(this.format);
		sb.append("\",\n\"algorithm\":\""); //$NON-NLS-1$
		sb.append(this.algorithm);
		sb.append(",\n\"Id\":\""); //$NON-NLS-1$
		sb.append(this.id);
		sb.append("\",\n"); //$NON-NLS-1$
		sb.append("\n\"singlesigns\":[\n"); //$NON-NLS-1$
		for (int i = 0 ; i < this.signs.size() ; i++) {
			sb.append(this.signs.get(i).toString());
			if (this.signs.size()-1 != i) {
				sb.append(',');
			}
			sb.append('\n');
		}
		sb.append("]\n"); //$NON-NLS-1$
		sb.append("}\n"); //$NON-NLS-1$
		return sb.toString();
	}

	/**
	 * Indica si el proceso por lote debe detenerse cuando se encuentre un error.
	 * @param soe <code>true</code> si el proceso por lote debe detenerse cuando se encuentre un error,
	 *            <code>false</code> si se debe continuar con el siguiente elemento del lote cuando se
	 *            produzca un error.
	 */
	public void setStopOnError(final boolean soe) {
		this.stopOnError = soe;
	}

	/**
	 * Obtiene el <i>log</i> con el resultado del proceso del lote.
	 * @return <i>Log</i> en formato JSON con el resultado del proceso del lote.
	 * */
	protected String getResultLog() {
		// Iniciamos el log de retorno
		final StringBuilder ret = new StringBuilder("{\"signs\":["); //$NON-NLS-1$
		for (int i = 0; i < this.signs.size() ; i++) {
			ret.append(printProcessResult(this.signs.get(i).getProcessResult()));
			if (this.signs.size() - 1 != i) {
				ret.append(","); //$NON-NLS-1$
			}
		}
		ret.append("]}"); //$NON-NLS-1$
		return ret.toString();
	}

	public static String printProcessResult(final ProcessResult result) {
		String jsonText = "{\"id\":\"" + scapeText(result.getId()) + "\", \"result\":\"" + result.getResult() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (result.getDescription() != null) {
			jsonText += ", \"description\":\"" + scapeText(result.getDescription()) + "\"";	 //$NON-NLS-1$ //$NON-NLS-2$
		}
		jsonText += "}"; //$NON-NLS-1$
		return jsonText;
	}

	private static String scapeText(final String text) {
		return text == null ? null :
			text.replace("\\", "\\\\").replace("\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public String getExtraParams() {
		return this.extraParams;
	}

	public void setExtraParams(final String extraParams) {
		this.extraParams = extraParams;
	}

	String getId() {
		return this.id;
	}

	void setId(final String i) {
		if (i != null) {
			this.id = i;
		}
	}

	/**
	 * Obtiene el algoritmo de firma.
	 * @return Algoritmo de firma.
	 * */
	public SingleSignConstants.SignAlgorithm getSignAlgorithm() {
		return this.algorithm;
	}
}
