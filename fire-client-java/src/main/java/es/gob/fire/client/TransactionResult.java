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
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

/**
 * Resultado de una transacci&oacute;n.
 */
public class TransactionResult {

	/** Codificaci&oacute;n de caracters por defecto. */
	public static final String DEFAULT_CHARSET = "utf-8"; //$NON-NLS-1$

	/** Prefijo de la respuesta JSON que engloba los detalles de la operaci&oacute;n. */
	private static final String JSON_ATTR_RESULT = "result"; //$NON-NLS-1$

	/** Par&aacute;metro JSON con el c&oacute;digo del error. */
	private static final String JSON_ATTR_ERROR_CODE = "ercod"; //$NON-NLS-1$

	/** Par&aacute;metro JSON con el mensaje del error. */
	private static final String JSON_ATTR_ERROR_MSG = "ermsg"; //$NON-NLS-1$

	/** Par&aacute;metro JSON con el c&oacute;digo del error. */
	private static final String JSON_ATTR_PROVIDER_NAME = "prov"; //$NON-NLS-1$

	/** Cadena de inicio de una estructura JSON compatible. */
	private static final String JSON_RESULT_PREFIX = "{\"" + JSON_ATTR_RESULT + "\":"; //$NON-NLS-1$ //$NON-NLS-2$

	/** Resultado de operaci&oacute;n de firma/multifica individual. */
	public static final int RESULT_TYPE_SIGN = 11;

	/** Resultado de una operaci&oacute;n de lote. */
	public static final int RESULT_TYPE_BATCH = 12;

	/** Resultado de una operaci&oacute;n de recogida de firma de un lote. */
	public static final int RESULT_TYPE_BATCH_SIGN = 13;

	/** Resultado de una operaci&oacute;n de generaci&oacute;n de certificado. */
	public static final int RESULT_TYPE_GENERATE_CERTIFICATE = 14;

	/** Resultado de error producido por cualquier otro tipo de operaci&oacute;n. */
	public static final int RESULT_TYPE_ERROR = 15;

	/** Especifica que la transacci&oacute;n finaliz&oacute; correctamente. */
	public static final int STATE_OK = 0;

	/** Especifica que la transacci&oacute;n no pudo finalizar debido a un error. */
	public static final int STATE_ERROR = -1;

	private int state = STATE_ERROR;

	private final int resultType;

	private int errorCode = 0;

	private String errorMessage = null;

	private String providerName = null;

	private byte[] result = null;

	private TransactionResult(final int resultType) {
		this.resultType = resultType;
	}

	/**
	 * Crea el objeto que debe devolverse como resultado de una transacci&oacute;n cuando esta
	 * ha finalizado con errores.
	 * @param resultType Tipo de resultado.
	 * @param errorCode C&oacute;digo de error.
	 * @param errorMessage Mensaje de error.
	 */
	public TransactionResult(final int resultType, final int errorCode, final String errorMessage) {
		this.resultType = resultType;
		this.state = STATE_ERROR;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Crea el objeto que debe devolverse como resultado de una transacci&oacute;n cuando esta
	 * ha finalizado correctamente. Este objeto no tiene definido el resultado final.
	 * @param resultType Tipo de resultado.
	 * @param providerName Nombre del proveedor utilizado.
	 */
	public TransactionResult(final int resultType, final String providerName) {
		this.resultType = resultType;
		this.state = STATE_OK;
		this.providerName = providerName;
	}

	/**
	 * Crea el objeto que debe devolverse como resultado de una transacci&oacute;n cuando esta
	 * ha finalizado correctamente.
	 * @param resultType Tipo de resultado.
	 * @param result Datos resultantes de la operaci&oacute;n.
	 */
	public TransactionResult(final int resultType, final byte[] result) {
		this.resultType = resultType;
		this.state = STATE_OK;
		this.result = result;
	}

	/**
	 * Recupera el tipo de resultado almacenado en el objeto.
	 * @return Tipo de resultado.
	 */
	public int getResultType() {
		return this.resultType;
	}

	/**
	 * Devuelve el estado de la transacci&oacute; (si termin&oacute; correctamente o no).
	 * @return Estado de la transacci&oacute;n: {@link #STATE_OK} o {@link #STATE_ERROR}.
	 */
	public int getState() {
		return this.state;
	}

	/**
	 * Devuelve el c&oacute;digo asociado al error sufrido durante la transacci&oacute;n.
	 * @return C&oacute;digo de error.
	 */
	public int getErrorCode() {
		return this.errorCode;
	}

	/**
	 * Devuelve el mensaje asociado al error sufrido durante la transacci&oacute;n.
	 * @return Mensaje de error.
	 */
	public String getErrorMessage() {
		return this.errorMessage;
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
	 * @param providerName Nombre del proveedor.
	 */
	public void setProviderName(final String providerName) {
		this.providerName = providerName;
	}

	/**
	 * Devuelve los datos obtenidos como resultado cuando la transacci&oacute;n ha
	 * finalizado correctamente.
	 * @return Resultado de la operaci&oacute;n.
	 */
	public byte[] getResult() {
		return this.result;
	}

	/**
	 * Establece el resultado de la transacci&oacute;n.
	 * @param result
	 */
	public void setResult(final byte[] result) {
		this.result = result;
	}

	/**
	 * Obtiene el resultado de la transacci&oacute;n, que puede ser los bytes del resultado
	 * o un objeto JSON con la informaci&oacute;n del proceso si este no se obtuvo. Este
	 * resultado es susceptible de parsearse mediante el m&eacute;todo {@link #parse(int, byte[])}.
	 * @return Resultado de la operaci&oacute;n.
	 */
	public byte[] encodeResult() {

		// Si tenemos un resultado, lo devolvemos directamente
		if (this.result != null) {
			return this.result;
		}

		// Si no tenemos resultado, devolvemos un JSON con la informacion que se
		// dispone de la transaccion
		final JsonObjectBuilder resultBuilder = Json.createObjectBuilder();
		if (this.errorMessage != null) {
			resultBuilder.add(JSON_ATTR_ERROR_MSG, this.errorMessage);
			resultBuilder.add(JSON_ATTR_ERROR_CODE, this.errorCode);
		}
		if (this.providerName != null) {
			resultBuilder.add(JSON_ATTR_PROVIDER_NAME, this.providerName);
		}

		// Construimos la respuesta
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final JsonWriter json = Json.createWriter(baos);

		final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add(JSON_ATTR_RESULT, resultBuilder);

		json.writeObject(jsonBuilder.build());
		json.close();

		return baos.toByteArray();
	}


	/**
	 * Obtiene un objeto con el resultado de la transacci&oacute;n a partir de los
	 * datos obtenidos en los servicios para recuperaci&oacute;n de datos de Clave Firma.
	 * @param resultType Tipo de resultado.
	 * @param result Datos devueltos por la operacion de recuperacion de datos.
	 * @return Resultado de la operaci&oacute;n.
	 */
	public static TransactionResult parse(final int resultType, final byte[] result) {

		final TransactionResult opResult = new TransactionResult(resultType);

		opResult.state = STATE_OK;

		// Comprobamos el inicio de la respuesta para saber si recibimos la informacion
		// de la operacion o el binario resultante
		byte[] prefix = null;
		if (result != null && result.length > JSON_RESULT_PREFIX.length() + 2) {
			prefix = Arrays.copyOf(result, JSON_RESULT_PREFIX.length());
		}

		// Si los datos empiezan por un prefijo concreto, es la informacion de la operacion
		if (prefix != null && Arrays.equals(prefix, JSON_RESULT_PREFIX.getBytes())) {
			final JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(result));
			final JsonObject json = jsonReader.readObject();
			final JsonObject resultObject = json.getJsonObject(JSON_ATTR_RESULT);
			if (resultObject.containsKey(JSON_ATTR_ERROR_CODE)) {
				opResult.errorCode = resultObject.getInt(JSON_ATTR_ERROR_CODE);
			}
			if (resultObject.containsKey(JSON_ATTR_ERROR_MSG)) {
				opResult.errorMessage = resultObject.getString(JSON_ATTR_ERROR_MSG);
			}
			if (resultObject.containsKey(JSON_ATTR_PROVIDER_NAME)) {
				opResult.providerName = resultObject.getString(JSON_ATTR_PROVIDER_NAME);
			}
			jsonReader.close();
		}
		// Si no, habremos recibido directamente el resultado.
		else {
			opResult.result = result;
		}

		return opResult;
	}
}
