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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import es.gob.fire.server.connector.OperationResult;

/**
 * Resultado de una operaci&oacute;n de carga de datos a firmar.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class SignOperationResult extends OperationResult {

    private final String transactionId;
    private final String redirectUrl;

    /**
     * Crea el resultado de una operaci&oacute;n de carga de datos a firmar a
     * partir de su defici&oacute;n JSON.
     * @param json
     *            Definici&oacute;n JSON del resultado de una operaci&oacute;n
     *            de carga de datos a firmar.
     * @throws IOException
     *             Si hay problemas analizando los datos de sesi&oacute;n
     *             trif&aacute;sica.
     */
    public SignOperationResult(final byte[] json) throws IOException {
        if (json == null) {
            throw new IllegalArgumentException(
                    "El JSON de definicion no puede ser nulo" //$NON-NLS-1$
            );
        }

        String id;
        String redirect;
        try (final JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(json));) {
        	final JsonObject jsonObject = jsonReader.readObject();
        	id = jsonObject.getString("transactionid"); //$NON-NLS-1$
        	redirect = jsonObject.getString("redirecturl"); //$NON-NLS-1$
        }

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException(
                    "Es obligatorio que el JSON contenga el identificador de la transaccion" //$NON-NLS-1$
            );
        }

        if (redirect == null || redirect.isEmpty()) {
            throw new IllegalArgumentException(
                    "Es obligatorio que el JSON contenga la URL a redireccionar al usuario para que se autentique" //$NON-NLS-1$
            );
        }

        this.transactionId = id;
        this.redirectUrl = redirect;
    }

    /**
     * Crea el resultado de una operaci&oacute;n de carga de datos a firmar.
     *
     * @param id
     *            Identificador de la transacci&oacute;n de firma.
     * @param redirect
     *            URL a redireccionar al usuario para que se autentique.
     */
    public SignOperationResult(final String id, final String redirect) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException(
                    "El identificador de la transacci&oacute;n de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (redirect == null || redirect.isEmpty()) {
            throw new IllegalArgumentException(
                    "La URL a redireccionar al usuario para que se autentique no puede ser nula" //$NON-NLS-1$
            );
        }
        this.transactionId = id;
        this.redirectUrl = redirect;
    }

    /**
     * Obtiene el identificador de la transacci&oacute;n de firma.
     *
     * @return Identificador de la transacci&oacute;n de firma.
     */
    public String getTransactionId() {
        return this.transactionId;
    }

    /**
     * Obtiene la URL a redireccionar al usuario para que se autentique.
     *
     * @return URL a redireccionar al usuario para que se autentique.
     */
    public String getRedirectUrl() {
        return this.redirectUrl;
    }

	/**
	 * Obtiene el resultado de la operaci&oacute;n de firma como documento JSON.
	 * @param charset Juego de caracteres.
	 * @return Resultado codificado en forma de JSON.
	 */
    @Override
    public byte[] encodeResult(final Charset charset) {
        final StringBuilder sb = new StringBuilder("{\n  \"transactionid\":\""); //$NON-NLS-1$
        sb.append(getTransactionId());
        sb.append("\",\n  \"redirecturl\":\""); //$NON-NLS-1$
        sb.append(getRedirectUrl());
        sb.append("\"\n}"); //$NON-NLS-1$
        return sb.toString().getBytes(charset);
    }
}
