/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.commons.utils;

/** 
 * <b>File:</b><p>es.gob.fire.commons.utils.Utils.java.</p>
 * <b>Description:</b><p>Class with general utilities.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>21/06/2020.</p>
 * @version 1.1, 21/05/2021.
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * <p>Class with general utilities.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 21/05/2021.
 */
public class Utils {

    private static final int BUFFER_SIZE = 4096;
    
    /**
     * Simple regular expression for email validation.
     */
    private static final String regexValidEmail = "^(.+)@(.+)$"; 

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
        int nBytes;
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((nBytes = input.read(buffer)) != -1) {
            baos.write(buffer, 0, nBytes);
        }
        return baos.toByteArray();
    }

    /**
     * Obtiene la fecha formateada.
     * @param date Fecha a formatear.
     * @return Cadena de texto con la fecha.
     */
    public static String getStringDateFormat(final Date date) {
    	return DateFormat.getInstance().format(date);
    }
    
    /**
     * Method that validates if a String represents a valid email.
     * @param email The String to check
     * @return true if the String represents a valid email.
     */
    public static boolean isValidEmail(final String email) {    	
    	    	
    	Pattern pattern = Pattern.compile(regexValidEmail);
    	
    	Matcher matcher = pattern.matcher(email);
    	
    	return matcher.matches();
    	 
    }

}
