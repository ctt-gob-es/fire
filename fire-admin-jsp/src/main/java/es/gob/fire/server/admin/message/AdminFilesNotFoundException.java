/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.message;

/**
 * @author mario
 * Excepci&oacute;n que salta cuando falta un fichero de configuraci&oacute;n en el m&oacute;dulo
 * de administraci&oacute;n.
 */
public class AdminFilesNotFoundException extends Exception{

	private static final long serialVersionUID = 1L;
	private static String fileName;
	protected AdminFilesNotFoundException() {
        super();
    }

	public AdminFilesNotFoundException(final String msg, final String filename) {
		super(msg);
		fileName = filename;
    }

	public AdminFilesNotFoundException(final String msg, final String filename, final Throwable cause) {
		super(msg, cause);
		fileName = filename;
    }

	/**
	 * @return El nombre del fichero que no se ha encontrado en el sistema.
	 */
	public static String getFileName(){
		return AdminFilesNotFoundException.fileName;
	}

}
