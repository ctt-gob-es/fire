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
 * Resultado de una iniciar un proceso de lote contra Cl@ve Firma.
 */
public final class CreateBatchResult {

    private final String transactionId;

    /**
     * Crea el resultado de una operaci&oacute;n de creaci&oacute;n de lote para
     * la firma con Clave Firma.
     *
     * @param id
     *            Identificador de la transacci&oacute;n de creaci&oacute;n de lote.
     */
    public CreateBatchResult(final String id) {
        if (id == null || "".equals(id)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "El identificador de la transacci&oacute;n de creacion de lote no puede ser nulo" //$NON-NLS-1$
            );
        }
        this.transactionId = id;
    }


    /**
     * Crea el resultado de una operaci&oacute;n de creaci&oacute;n de lote para
     * la firma con Clave Firma.
     *
     * @param json
     *            Definici&oacute;n JSON del resultado de una operaci&oacute;n
     *            de creaci&oacute;n de lote a firmar.
     * @return Resultado de la creacion del lote.
     */
    public static CreateBatchResult parse(final byte[] json) {
        if (json == null) {
            throw new IllegalArgumentException(
                    "El JSON de definicion no puede ser nulo" //$NON-NLS-1$
            );
        }
        final JsonReader jsonReader = Json
                .createReader(new ByteArrayInputStream(json));
        final JsonObject jsonObject = jsonReader.readObject();
        final String id = jsonObject.getString("transactionid"); //$NON-NLS-1$
        jsonReader.close();

        if (id == null || "".equals(id)) { //$NON-NLS-1$
            throw new IllegalArgumentException(
                    "Es obligatorio que el JSON contenga el identificador de la transacci&oacute;n" //$NON-NLS-1$
            );
        }

        return new CreateBatchResult(id);
    }
    /**
     * Obtiene el identificador de la transacci&oacute;n de creaci&oacute;n de lote.
     *
     * @return Identificador de la transacci&oacute;n de creaci&oacute;n de lote.
     */
    public String getTransactionId() {
        return this.transactionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{\n  \"transactionid\":\""); //$NON-NLS-1$
        sb.append(getTransactionId());
        sb.append("\"\n}"); //$NON-NLS-1$
        return sb.toString();
    }

}
