/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.upgrade.afirma;

/**
 * Indica que no se ha encontrado el fichero de configuraci&oacute;n requerido.
 */
public class ConfigFileNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String filename;

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 */
	public ConfigFileNotFoundException(final String msg) {
		super(msg);
		this.filename = null;
    }

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 * @param filename Nombre del fichero que no se encuentra o no se puede cargar.
	 */
	public ConfigFileNotFoundException(final String msg, final String filename) {
		super(msg);
		this.filename = filename;
    }

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 * @param filename Nombre del fichero que no se encuentra o no se puede cargar.
	 * @param cause Motivo de la excepci&oacute;n.
	 */
	public ConfigFileNotFoundException(final String msg, final String filename, final Throwable cause) {
		super(msg, cause);
		this.filename = filename;
    }

	/**
	 * Recupera el nombre del fichero.
	 * @return Nombre del fichero.
	 */
	public String getFilename() {
		return this.filename;
	}
}
