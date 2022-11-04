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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Resultado de una operaci&oacute;n de carga de datos a firmar.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class SignOperationResult {

	private final String transactionId;
    private final String redirectUrl;

    /**
     * Crea el resultado de una operaci&oacute;n de carga de datos a firmar.
     * @param id
     *            Identificador de la transacci&oacute;n de firma.
     * @param redirect
     *            URL a redireccionar al usuario para que se autentique.
     */
    public SignOperationResult(final String id, final String redirect) {
        if (id == null || "".equals(id)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador de la transacci&oacute;n de firma no puede ser nulo" //$NON-NLS-1$
            );
        }
        if (redirect == null || "".equals(redirect)) { //$NON-NLS-1$
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
     * Obtiene el resultado de una operaci&oacute;n de carga de datos a firmar a
     * partir de su defici&oacute;n JSON.
     *
     * @param json
     *            Definici&oacute;n JSON del resultado de una operaci&oacute;n
     *            de carga de datos a firmar.
     * @return Resultado de la llamada a una operaci&oacute;n de firma.
     */
    static SignOperationResult parse(final byte[] json) {
        if (json == null) {
            throw new IllegalArgumentException(
                    "El JSON de definicion no puede ser nulo" //$NON-NLS-1$
            );
        }
        final JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(json));
        final JsonObject jsonObject = jsonReader.readObject();

        final String id = jsonObject.getString("transactionid"); //$NON-NLS-1$
        final String redirect = jsonObject.getString("redirecturl"); //$NON-NLS-1$
        jsonReader.close();

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException(
                    "Es obligatorio que el JSON contenga el identificador de la transacci&oacute;n" //$NON-NLS-1$
            );
        }

        if (redirect == null || redirect.isEmpty()) {
            throw new IllegalArgumentException(
                    "Es obligatorio que el JSON contenga la URL a redireccionar al usuario para que se autentique" //$NON-NLS-1$
            );
        }

        return new SignOperationResult(id, redirect);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{\n  \"transactionid\":\""); //$NON-NLS-1$
        sb.append(getTransactionId());
        sb.append("\",\n  \"redirecturl\":\""); //$NON-NLS-1$
        sb.append(getRedirectUrl());
        sb.append("\"\n}"); //$NON-NLS-1$
        return sb.toString();
    }
}
