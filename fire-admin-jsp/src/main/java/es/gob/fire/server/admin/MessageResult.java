/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin;

/**
 * Mensaje que notifica el resultado de una operaci&oacute;n.
 */
public class MessageResult {

	private boolean ok;

	private String message;

	/**
	 * Crea el mensaje resultado de la operaci&oacute;n.
	 * @param ok Resultado correcto.
	 * @param msg Mensaje.
	 */
	public MessageResult(final boolean ok, final String msg) {
		this.ok = ok;
		this.message = msg;
	}

	/**
	 * Si la operacion termin&oacute;n bien.
	 * @return {@code true} si la operaci&oacute;n finaliz&oacute; correctamente, {@code false} en caso contrario.
	 */
	public boolean isOk() {
		return this.ok;
	}

	/**
	 * Establece el resultado de una operaci&oacute;n.
	 * @param ok {@code true} Si la operaci&oacute;n finaliz&oacute; correctamente, {@code false} en caso contrario.
	 */
	public void setOk(final boolean ok) {
		this.ok = ok;
	}

	/**
	 * Recupera el mensaje que notifica el resultado de la operaci&oacute;n.
	 * @return Mensaje.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Establece el mensaje que notifica el resultado de la operaci&oacute;n.
	 * @param message Mensaje.
	 */
	public void setMessage(final String message) {
		this.message = message;
	}


}
