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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.LoggerFactory;

/**
 * M&eacute;todos de utilidad para la conversi&oacute;n de datos.
 */
public class Utils {

	private static final int BUFFER_SIZE = 4096;

    /**
     * Convierte un objeto de propiedades en una cadena Base64.
     *
     * @param p
     *            Objeto de propiedades a convertir.
     * @param urlSafe
     * 			  Indica si el base 64 debe ser URL Safe o no.
     * @return Base64 que descodificado es un fichero de propiedades en texto
     *         plano o cadena vac&iacute;a si el objeto era {@code null}.
     */
    public static String properties2Base64(final Properties p, final boolean urlSafe) {
        if (p == null) {
            return ""; //$NON-NLS-1$
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
        	p.store(baos, ""); //$NON-NLS-1$
        }
        catch (final Exception e) {
        	LoggerFactory.getLogger(Utils.class).error(
        			"No se ha podido generar el base 64 de un objeto de propiedades. No deberia ocurrir nunca", e); //$NON-NLS-1$
		}
        return Base64.encode(baos.toByteArray(), urlSafe);
    }

    /**
     * Transforma un Base64 normal en un Base64 URL SAFE.
     * @param base64UrlSafe Cadena Base64 URL Safe.
     * @return Cadena de texto Base64 normal.
     */
    public static String doBase64UrlSafe(final String base64UrlSafe) {
        return base64UrlSafe.replace('+', '-').replace('/', '_');
    }

    /** Lee un flujo de datos de entrada y los recupera en forma de array de
     * bytes. Este m&eacute;todo consume pero no cierra el flujo de datos de
     * entrada.
     * @param input
     *        Flujo de donde se toman los datos.
     * @return Los datos obtenidos del flujo.
     * @throws IOException
     *         Cuando ocurre un problema durante la lectura */
    public static byte[] getDataFromInputStream(final InputStream input) throws IOException {
        if (input == null) {
            return new byte[0];
        }
        int nBytes = 0;
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((nBytes = input.read(buffer)) != -1) {
            baos.write(buffer, 0, nBytes);
        }
        return baos.toByteArray();
    }
}
