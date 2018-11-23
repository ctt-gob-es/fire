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

	private String fileName;

	protected AdminFilesNotFoundException() {
        super();
    }

	/**
	 * Informa de que no se encuentra un fichero de configuraci&oacute;n.
	 * @param msg Mensaje de error.
	 * @param filename Fichero que no se encontr&oacute;.
	 */
	public AdminFilesNotFoundException(final String msg, final String filename) {
		super(msg);
		this.fileName = filename;
    }

	/**
	 * Informa de que no se encuentra un fichero de configuraci&oacute;n.
	 * @param msg Mensaje de error.
	 * @param filename Fichero que no se encontr&oacute;.
	 * @param cause Causa origen del error.
	 */
	public AdminFilesNotFoundException(final String msg, final String filename, final Throwable cause) {
		super(msg, cause);
		this.fileName = filename;
    }

	/**
	 * @return El nombre del fichero que no se ha encontrado en el sistema.
	 */
	public String getFileName(){
		return this.fileName;
	}

}
