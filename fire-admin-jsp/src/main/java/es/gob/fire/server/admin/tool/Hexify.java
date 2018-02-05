/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.tool;

/**
 * Permite la codificaci&oacute;n de binarios en hexadecimal.
 */
public class Hexify {

    /** Equivalencias de hexadecimal a texto por la posici&oacute;n del vector.
     * Para ser usado en <code>hexify()</code> */
    private static final char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /** Convierte un vector de octetos en una cadena de caracteres que contiene
     * la representaci&oacute;n hexadecimal. Copiado directamente de
     * opencard.core.util.HexString
     * @param abyte0
     *        Vector de octetos que deseamos representar textualmente
     * @param separator
     *        Indica si han o no de separarse los octetos con un
     *        gui&oacute;n y en l&iacute;neas de 16
     * @return Representaci&oacute;n textual del vector de octetos de entrada */
    public static String hexify(final byte abyte0[], final String separator) {
        if (abyte0 == null) {
            return "null"; //$NON-NLS-1$
        }

        final StringBuffer stringbuffer = new StringBuffer(256);
        for (int j = 0; j < abyte0.length; j++) {
            if (separator != null && j > 0) {
                stringbuffer.append(separator);
            }
            stringbuffer.append(HEX_CHARS[abyte0[j] >> 4 & 0xf]);
            stringbuffer.append(HEX_CHARS[abyte0[j] & 0xf]);
        }
        return stringbuffer.toString();
    }
}
