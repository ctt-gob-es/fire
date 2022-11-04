/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.client.signprocess;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import es.gob.fire.client.Base64;

/**
 * Resultado de una operaci&oacute;n de carga de datos a firmar.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class LoadResult {

    private final String transactionId;
    private final String redirectUrl;
    private final TriphaseData triphaseData;

    /**
     * Crea el resultado de una operaci&oacute;n de carga de datos a firmar a
     * partir de su defici&oacute;n JSON.
     *
     * @param json
     *            Definici&oacute;n JSON del resultado de una operaci&oacute;n
     *            de carga de datos a firmar.
     * @throws IOException
     *             Si hay problemas analizando los datos de sesi&oacute;n
     *             trif&aacute;sica.
     */
    public LoadResult(final String json) throws IOException {
        if (json == null) {
            throw new IllegalArgumentException(
                    "El JSON de definicion no puede ser nulo" //$NON-NLS-1$
            );
        }
        final JsonReader jsonReader = Json
                .createReader(new ByteArrayInputStream(json.getBytes("utf-8"))); //$NON-NLS-1$
        final JsonObject jsonObject = jsonReader.readObject();
        final String id = jsonObject.getString("transacionid"); //$NON-NLS-1$
        final String redirect = jsonObject.getString("redirecturl"); //$NON-NLS-1$
        final String tDataXmlB64 = jsonObject.getString("triphasedata"); //$NON-NLS-1$
        jsonReader.close();

        if (id == null || "".equals(id)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "Es obligatorio que el JSON contenga el identificador de la transacci&oacute;n" //$NON-NLS-1$
            );
        }

        if (redirect == null || "".equals(redirect)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "Es obligatorio que el JSON contenga la URL a redireccionar al usuario para que se autentique" //$NON-NLS-1$
            );
        }

        if (tDataXmlB64 == null || "".equals(tDataXmlB64)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "Es obligatorio que el JSON contenga los datos de la sesion trifasica" //$NON-NLS-1$
            );
        }
        this.transactionId = id;
        this.redirectUrl = redirect;
        this.triphaseData = TriphaseData.parser(Base64.decode(tDataXmlB64));

    }

    /**
     * Crea el resultado de una operaci&oacute;n de carga de datos a firmar.
     *
     * @param id
     *            Identificador de la transacci&oacute;n de firma.
     * @param redirect
     *            URL a redireccionar al usuario para que se autentique.
     * @param td
     *            Datos de la sesi&oacute;n trif&aacute;sica.
     */
    public LoadResult(final String id, final String redirect, final TriphaseData td) {
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
        if (td == null) {
            throw new IllegalArgumentException(
                    "Los datos de la sesion trifasica no pueden ser nulos" //$NON-NLS-1$
            );
        }
        this.transactionId = id;
        this.redirectUrl = redirect;
        this.triphaseData = td;
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
     * Obtiene los datos de la sesi&oacute;n trif&aacute;sica.
     *
     * @return Datos de la sesi&oacute;n trif&aacute;sica.
     */
    public TriphaseData getTriphaseData() {
        return this.triphaseData;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{\n  \"transacionid\":\""); //$NON-NLS-1$
        sb.append(getTransactionId());
        sb.append("\",\n  \"redirecturl\":\""); //$NON-NLS-1$
        sb.append(getRedirectUrl());
        sb.append("\",\n  \"triphasedata\":\""); //$NON-NLS-1$
        sb.append(Base64.encode(getTriphaseData().toString().getBytes()));
        sb.append("\"\n}"); //$NON-NLS-1$
        return sb.toString();
    }

}
