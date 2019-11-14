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
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	/** Par&aacute;metro JSON con el certificado utilizado para firmar. */
	private static final String JSON_ATTR_SIGNING_CERT = "cert"; //$NON-NLS-1$

	/** Prefijo de la respuesta JSON con el formato al que se actualiza la firma. */
	private static final String JSON_ATTR_UPGRADE = "upgrade"; //$NON-NLS-1$

	/** Par&aacute;metro JSON con la informaci&oacute;n del periodo de gracia. */
	private static final String JSON_ATTR_GRACE_PERIOD = "grace"; //$NON-NLS-1$

	/** Par&aacute;metro JSON con el identificador con el que recuperar la firma. */
	private static final String JSON_ATTR_GRACE_PERIOD_ID = "id"; //$NON-NLS-1$

	/** Par&aacute;metro JSON con la fecha a la que se debe recuperar la firma en milisegundos. */
	private static final String JSON_ATTR_GRACE_PERIOD_DATE = "date"; //$NON-NLS-1$

	/** Par&aacute;metro JSON con el estado de la operaci&oacute;n. */
	private static final String JSON_ATTR_STATE = "state"; //$NON-NLS-1$

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

	/** Especifica que la transacci&oacute;n aun no ha finalizado y se deber&aacute; pedir el resultamos m&aacute;s adelante. */
	public static final int STATE_PENDING = 1;

	/** Especifica que la transacci&oacute;n ha finalizado pero que el resultado puede
	 * diferir de lo solicitado por la aplicaci&oacute;n. Por ejemplo, puede haberse
	 * solicitado una firma ES-A y recibirse una ES-T. */
	public static final int STATE_PARTIAL = 2;

	private int state = STATE_ERROR;

	private final int resultType;

	private int errorCode = 0;

	private String errorMessage = null;

	private String providerName = null;

	private X509Certificate signingCert = null;

	private String upgradeFormat = null;

	private byte[] result = null;

	private GracePeriodInfo gracePeriod = null;

	public TransactionResult(final int resultType) {
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
	 * @return Estado de la transacci&oacute;n.
	 */
	public int getState() {
		return this.state;
	}

	/**
	 * Devuelve el estado de la transacci&oacute; (si termin&oacute; correctamente o no).
	 * @param state Estado de la transacci&oacute;n.
	 */
	public void setState(final int state) {
		this.state = state;
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
	 * Devuelve el identificador del formato de firma longevo.
	 * @return Formato de firma longevo.
	 */
	public String getUpgradeFormat() {
		return this.upgradeFormat;
	}

	/**
	 * Establece el identificador del formato de firma longevo.
	 * @param upgradeFormat Formato de firma longevo.
	 */
	public void setUpgradeFormat(final String upgradeFormat) {
		this.upgradeFormat = upgradeFormat;
	}

	/**
	 * Recupera la informaci&opacute;n del periodo de gracia necesario para recuperar la firma.
	 * @return Informaci&oacute;n del periodo de gracia o {@code null} si no lo hay.
	 */
	public GracePeriodInfo getGracePeriod() {
		return this.gracePeriod;
	}

	/**
	 * Establece la informaci&oacute;n del periodo de gracia necesario para recuperar la firma.
	 * @param Informaci&oacute;n del periodo de gracia.
	 */
	public void setGracePeriod(final GracePeriodInfo gracePeriod) {
		this.gracePeriod = gracePeriod;
		if (this.gracePeriod != null) {
			this.state = STATE_PENDING;
		}
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
		resultBuilder.add(JSON_ATTR_STATE, this.state);
		if (this.errorMessage != null) {
			resultBuilder.add(JSON_ATTR_ERROR_MSG, this.errorMessage);
			resultBuilder.add(JSON_ATTR_ERROR_CODE, this.errorCode);
		}
		if (this.providerName != null) {
			resultBuilder.add(JSON_ATTR_PROVIDER_NAME, this.providerName);
		}
		if (this.signingCert != null) {
			try {
				resultBuilder.add(JSON_ATTR_SIGNING_CERT, encodeCertificate(this.signingCert));
			} catch (final CertificateEncodingException e) {
				// Error al codificar el certificado, no se devolvera certificado en ese caso
				Logger.getLogger(BatchResult.class.getName()).log(
						Level.WARNING,
						"Error al codificar el certificado de firma", //$NON-NLS-1$
						e);
			}
		}
		if (this.upgradeFormat != null) {
			resultBuilder.add(JSON_ATTR_UPGRADE, this.upgradeFormat);
		}
		if (this.gracePeriod != null) {
			try {
				final JsonObjectBuilder gracePeriodBuilder = Json.createObjectBuilder();
				gracePeriodBuilder.add(JSON_ATTR_GRACE_PERIOD_ID, this.gracePeriod.getResponseId());
				gracePeriodBuilder.add(JSON_ATTR_GRACE_PERIOD_DATE, this.gracePeriod.getResolutionDate().getTime());
				resultBuilder.add(JSON_ATTR_GRACE_PERIOD, gracePeriodBuilder);
			} catch (final Exception e) {
				// Error al codificar el certificado, no se devolvera certificado en ese caso
				Logger.getLogger(BatchResult.class.getName()).log(
						Level.WARNING,
						"Error al codificar el certificado de firma", //$NON-NLS-1$
						e);
			}
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
	 * datos obtenidos en los servicios para recuperaci&oacute;n de datos de FIRe.
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
			try {
				final JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(result));
				final JsonObject json = jsonReader.readObject();
				final JsonObject resultObject = json.getJsonObject(JSON_ATTR_RESULT);
				if (resultObject.containsKey(JSON_ATTR_ERROR_CODE)) {
					opResult.errorCode = resultObject.getInt(JSON_ATTR_ERROR_CODE);
				}
				if (resultObject.containsKey(JSON_ATTR_STATE)) {
					opResult.state = resultObject.getInt(JSON_ATTR_STATE);
				}
				if (resultObject.containsKey(JSON_ATTR_ERROR_MSG)) {
					opResult.errorMessage = resultObject.getString(JSON_ATTR_ERROR_MSG);
				}
				if (resultObject.containsKey(JSON_ATTR_PROVIDER_NAME)) {
					opResult.providerName = resultObject.getString(JSON_ATTR_PROVIDER_NAME);
				}
				if (resultObject.containsKey(JSON_ATTR_SIGNING_CERT)) {
					try {
						opResult.signingCert = decodeCertificate(resultObject.getString(JSON_ATTR_SIGNING_CERT));
					}
					catch (final Exception e) {
						// Error al codificar el certificado, no se devolvera certificado en ese caso
						Logger.getLogger(TransactionResult.class.getName()).log(
								Level.WARNING,
								"Error al decodificar el certificado de firma", //$NON-NLS-1$
								e);
					}
				}
				if (resultObject.containsKey(JSON_ATTR_UPGRADE)) {
					opResult.upgradeFormat = resultObject.getString(JSON_ATTR_UPGRADE);
				}
				if (resultObject.containsKey(JSON_ATTR_GRACE_PERIOD)) {
					final JsonObject gracePeriodObject = resultObject.getJsonObject(JSON_ATTR_GRACE_PERIOD);
					Date gracePeriodDate = null;
					if (gracePeriodObject.containsKey(JSON_ATTR_GRACE_PERIOD_DATE)) {
						try {
							gracePeriodDate = new Date(gracePeriodObject.getJsonNumber(JSON_ATTR_GRACE_PERIOD_DATE).longValue());
						}
						catch (final Exception e) {
							opResult.state = STATE_ERROR;
							opResult.errorCode = 0;
							opResult.errorMessage = "Se solicito la espera de un periodo de gracia y se proporciono en un formato no valido"; //$NON-NLS-1$
						}
					}
					opResult.gracePeriod = new GracePeriodInfo(
							gracePeriodObject.getString(JSON_ATTR_GRACE_PERIOD_ID),
							gracePeriodDate);
				}
				jsonReader.close();
			}
			catch (final Exception e) {
				opResult.state = STATE_ERROR;
				opResult.errorCode = 0;
				opResult.errorMessage = "El formato de la respuesta del servidor no es valido"; //$NON-NLS-1$

			}
		}
		// Si no, habremos recibido directamente el resultado.
		else {
			opResult.result = result;
		}

		return opResult;
	}

	/**
	 * Codifica un certificado en forma de una cadena en base 64.
	 * @param cert Certificado a codificar.
	 * @return Certificado codificado.
	 * @throws CertificateEncodingException Cuando ocurre un error al codificar el certificado.
	 */
	private static String encodeCertificate(final X509Certificate cert) throws CertificateEncodingException {
		return Base64.encode(cert.getEncoded());
	}

	/**
	 * Construye el objeto certificado.
	 * @param certB64 Texto en base64 con el certificado codificado.
	 * @return Certificado.
	 * @throws CertificateException Cuando el base 64 no se correspond&iacute;a con un certificado.
	 * @throws IOException Cuando ocurre un error al leer el base 64.
	 */
	private static X509Certificate decodeCertificate(final String certB64) throws CertificateException, IOException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
                new ByteArrayInputStream(Base64.decode(certB64)));
	}
}
