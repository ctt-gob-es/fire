/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.services;

/** Excepci&oacute;n que salta cuando falta un fichero de configuraci&oacute;n en el m&oacute;dulo
 * distribuido. */
public final class ConfigFilesException extends Exception{

	private static final long serialVersionUID = 1L;

	private static String fileName;
	private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

	protected ConfigFilesException() {
        super();
    }

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 * @param filename Nombre del fichero que falta en el sistema.
	 */
	public ConfigFilesException(final String msg, final String filename) {
		super(msg);
		fileName = filename;
    }

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 * @param filename Nombre del fichero que falta en el sistema.
	 * @param cause Motivo de la excepci&oacute;n.
	 */
	public ConfigFilesException(final String msg, final String filename, final Throwable cause) {
		super(msg, cause);
		fileName = filename;
    }

	/**
	 * @return El nombre del fichero que no se ha encontrado en el sistema.
	 */
	public static String getFileName(){
		return ConfigFilesException.fileName;
	}

	/**
	 * Devuelve el error HTTP de la excepci&oacute;n.
	 * @return El httpError.
	 */
	public static int getHttpError(){
		return HTTP_INTERNAL_SERVER_ERROR;
	}

}
