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

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import es.gob.fire.server.services.ServiceUtil;

/**
 * Configuraci&oacute;n de firma particular para un documento de un lote.
 * @author Carlos Gamuci
 */
public class SignBatchConfig implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 8594180356291997801L;

	private String cryptoOperation;

	private String format;

	private Properties extraParams;

	private String upgrade;

	private Properties upgradeConfig;

	/**
	 * Recupera el identificador de la operaci&oacute;n criptogr&aacute;fica
	 * configurada (firma, cofirma,...).
	 * @return Identificador de operacion criptogr&aacute;fica (sign, cosign o countersign).
	 */
	public String getCryptoOperation() {
		return this.cryptoOperation;
	}

	/**
	 * Establece la operaci&oacute;n criptogr&aacute;fica a realizar (firma, cofirma,...).
	 * @param cryptoOperation Identificador de operacion criptogr&aacute;fica (sign,
	 * cosign o countersign).
	 */
	public void setCryptoOperation(final String cryptoOperation) {
		this.cryptoOperation = cryptoOperation;
	}

	/**
	 * Recupera el formato de firma.
	 * @return Formato de firma.
	 */
	public String getFormat() {
		return this.format;
	}

	/**
	 * Establece el formato de firma.
	 * @param format Formato de firma.
	 */
	public void setFormat(final String format) {
		this.format = format;
	}

	/**
	 * Recupera la configuraci&oacute;n del formato de firma.
	 * @return Configuraci&oacute;n del formato de firma en base 64.
	 */
	public Properties getExtraParams() {
		return this.extraParams;
	}

	/**
	 * Establece la configuraci&oacute;n del formato de firma.
	 * @param extraParamsB64 Configuraci&oacute;n del formato de firma en base 64.
	 * @throws IOException Los datos proporcionados no son un base 64 v&aacute;lido.
	 */
	public void setExtraParamsB64(final String extraParamsB64) throws IOException {
		if (extraParamsB64 != null) {
			this.extraParams = ServiceUtil.base642Properties(extraParamsB64);
		}
	}

	/**
	 * Establece la configuraci&oacute;n del formato de firma.
	 * @param extraParams Configuraci&oacute;n del formato de firma.
	 */
	public void setExtraParams(final Properties extraParams) {
		this.extraParams = extraParams;
	}

	/**
	 * Recupera el formato de actualizaci&oacute;n de la firma particular.
	 * @return Formato de actualizaci&oacute;n o {@code null} si no se estableci&oacute;.
	 */
	public String getUpgrade() {
		return this.upgrade;
	}

	/**
	 * Establece el formato de actualizaci&oacute;n de la firma particular.
	 * @param upgrade Nombre del formato avanzado al que hay que actualizar la firma.
	 */
	public void setUpgrade(final String upgrade) {
		this.upgrade = upgrade;
	}

	/**
	 * Recupera la configuraci&oacute;n adicional para la actualizaci&oacute;n de la firma.
	 * @return Configuraci&oacute;n para la actualizaci&oacute;n de la firma.
	 */
	public Properties getUpgradeConfig() {
		return this.upgradeConfig;
	}

	/**
	 * Establece la configuraci&oacute;n particular para la actualizaci&oacute;n de la firma.
	 * @param upgradeConfig Configuraci&oacute;n para la actualizaci&oacute;n de la firma.
	 */
	public void setUpgradeConfig(final Properties upgradeConfig) {
		this.upgradeConfig = upgradeConfig;
	}
}
