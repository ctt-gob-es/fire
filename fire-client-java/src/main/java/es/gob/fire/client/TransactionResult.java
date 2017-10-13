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

import java.io.UnsupportedEncodingException;

/**
 * Resultado de una transacci&oacute;n.
 */
public class TransactionResult {

	/** Prefijo que antecede al c&oacute;digo de error cuando este se produjo durante la
	 * operaci&oacute;n. */
	private static final String ERROR_PREFIX = "ERR-"; //$NON-NLS-1$

	/** Sufijo que se indica a continuaci&oacute;n de un c&oacute;digo de error . */
	private static final char ERROR_SUFIX = ':';

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
	 * Devuelve los datos obtenidos como resultado cuando la transacci&oacute;n ha
	 * finalizado correctamente.
	 * @return Resultado de la operaci&oacute;n.
	 */
	public byte[] getResult() {
		return this.result;
	}

	/**
	 * Obtiene la cadena de bytes resultado de la transacci&oacute;n. Este resultado es
	 * susceptible de parsearse mediante el m&eacute;todo {@link #parse(int, byte[])}.
	 * @return Resultado de la operaci&oacute;n.
	 */
	public byte[] encodeResult() {

		// Resultado OK
		if (this.state == STATE_OK) {
			return this.result;
		}

		// Error
		try {
			return (ERROR_PREFIX + this.errorCode + ERROR_SUFIX + this.errorMessage).getBytes("utf-8"); //$NON-NLS-1$
		}
		catch (final Exception e) {
			return (ERROR_PREFIX + this.errorCode + ERROR_SUFIX + this.errorMessage).getBytes();
		}
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

		// Comprobamos si se ha producido un error no recuperable
		if (result.length > 6 &&
				ERROR_PREFIX.equals(new String(new byte[] { result[0], result[1], result[2], result[3] }))) {

			// Comprobamos los primeros caracteres para corroborar que se trata de un error
			for (int i = 5; i < Math.min(result.length, 11) && opResult.state == STATE_OK; i++) {
				if (result[i] == ERROR_SUFIX) {
					opResult.state = STATE_ERROR;
				}
			}
		}

		// En caso de error, habremos recibido el codigo y el mensaje
		if (opResult.state == STATE_ERROR) {
			String error;
			try {
				error = new String(result, "utf-8"); //$NON-NLS-1$
			} catch (final UnsupportedEncodingException e) {
				error = new String(result);
			}
			opResult.errorCode = Integer.parseInt(error.substring(ERROR_PREFIX.length(), error.indexOf(ERROR_SUFIX)));
			opResult.errorMessage = error.substring(error.indexOf(ERROR_SUFIX) + 1);
		}
		// En caso de exito habremos recibido directamente el resultado.
		else {
			opResult.result = result;
		}

		return opResult;
	}
}