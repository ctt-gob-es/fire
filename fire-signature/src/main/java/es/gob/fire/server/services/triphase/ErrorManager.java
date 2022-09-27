/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.triphase;

import java.util.Dictionary;
import java.util.Hashtable;

final class ErrorManager {

	public static final int MISSING_PARAM_OPERATION = 1;
	public static final int MISSING_PARAM_ID = 2;
	public static final int MISSING_PARAM_ALGORITHM = 3;
	public static final int MISSING_PARAM_FORMAT = 4;
	public static final int MISSING_PARAM_CERTIFICATE = 5;
	public static final int INVALID_FORMAT_EXTRAPARAMS = 6;
	public static final int INVALID_FORMAT_CERTIFICATE = 7;
	public static final int UNSUPPORTED_SIGNATURE_FORMAT = 8;
	public static final int ERROR_PRESIGNING = 9;
	public static final int ERROR_STORAGING_SIGNATURE = 10;
	public static final int UNSUPPORTED_TRIPHASE_OPERATION = 11;
	public static final int ERROR_POSTSIGNING = 12;
	public static final int MISSING_PARAM_CRYPTO_OPERATION = 13;
	public static final int ERROR_RETRIEVING_DOCUMENT = 14;
	public static final int INVALID_SESSION_DATA = 15;
	public static final int ERROR_GENERATING_PKCS1_HMAC = 16;
	public static final int ERROR_CHECKING_PKCS1_HMAC = 17;
	public static final int CONFIGURATION_NEEDED = 18;

	private static final Dictionary<Integer, String> errorMessages = new Hashtable<>();
	static {
		errorMessages.put(Integer.valueOf(MISSING_PARAM_OPERATION), "No se ha indicado la operacion a realizar"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(MISSING_PARAM_ID), "No se ha indicado el identificador del documento"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(MISSING_PARAM_ALGORITHM), "No se ha indicado el algoritmo de firma"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(MISSING_PARAM_FORMAT), "No se ha indicado el formato de firma"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(MISSING_PARAM_CERTIFICATE), "No se ha indicado el certificado de usuario"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(INVALID_FORMAT_EXTRAPARAMS), "El formato de los parametros adicionales suministrados es erroneo"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(INVALID_FORMAT_CERTIFICATE), "El certificado de usuario no esta en formato X.509"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(UNSUPPORTED_SIGNATURE_FORMAT), "Formato de firma no soportado"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(ERROR_PRESIGNING), "Error realizando la prefirma"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(ERROR_STORAGING_SIGNATURE), "Error en el almacen final del documento"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(UNSUPPORTED_TRIPHASE_OPERATION), "Operacion desconocida"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(ERROR_POSTSIGNING), "Error realizando la postfirma"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(MISSING_PARAM_CRYPTO_OPERATION), "No se indicado una sub-operacion valida a realizar (firma, cofirma,...)"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(ERROR_RETRIEVING_DOCUMENT), "Error al recuperar el documento"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(INVALID_SESSION_DATA), "El formato de los datos de sesion suministrados es erroneo"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(ERROR_GENERATING_PKCS1_HMAC), "No se ha podido generar el codigo de verificacion del PKCS#1 generado"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(ERROR_CHECKING_PKCS1_HMAC), "El PKCS#1 se ha generado con un certificado distinto al esperado"); //$NON-NLS-1$
		errorMessages.put(Integer.valueOf(CONFIGURATION_NEEDED), "Se requiere intervencion del usuario"); //$NON-NLS-1$
	}

	static String getErrorMessage(final int errNo) {
		return getErrorPrefix(errNo) + ": " + errorMessages.get(Integer.valueOf(errNo)); //$NON-NLS-1$
	}

	static String getErrorMessage(final int errNo, final String code) {
		return getErrorPrefix(errNo) + ":" + code + ": " + errorMessages.get(Integer.valueOf(errNo)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	static String getErrorPrefix(final int errNo) {
		return "ERR-" + Integer.toString(errNo); //$NON-NLS-1$
	}

}
