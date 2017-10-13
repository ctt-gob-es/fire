/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.client;

/**
 * @author mario
 * Excepci&oacute;n que salta cuando falta un fichero de configuraci&oacute;n del
 * componente distribuido.
 */
public class ClientConfigFilesNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	private static String fileName;
	protected ClientConfigFilesNotFoundException() {
        super();
    }

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 * @param filename Nombre del fichero que falta en el sistema.
	 */
	public ClientConfigFilesNotFoundException(final String msg, final String filename) {
		super(msg);
		fileName = filename;
    }

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 * @param filename Nombre del fichero que falta en el sistema.
	 * @param cause Error que caus&oacute; la excepci&oacute;n.
	 */
	public ClientConfigFilesNotFoundException(final String msg, final String filename, final Throwable cause) {
		super(msg, cause);
		fileName = filename;
    }

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 */
	public ClientConfigFilesNotFoundException(final String msg) {
		super(msg);
	}

	/**
	 * Constructor p&uacute;blico de la excepci&oacute;n.
	 * @param msg Mensaje de la excepci&oacute;n.
	 * @param cause Error que caus&oacute; la excepci&oacute;n.
	 */
	public ClientConfigFilesNotFoundException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @return El nombre del fichero que no se ha encontrado en el sistema.
	 */
	public static String getFileName(){
		return ClientConfigFilesNotFoundException.fileName;
	}

}
