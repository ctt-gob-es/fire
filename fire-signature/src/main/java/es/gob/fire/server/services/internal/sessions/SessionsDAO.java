/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal.sessions;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import es.gob.fire.server.services.internal.FireSession;

/**
 * Interfaz que implementan los gestores para el guardado de sesiones y
 * su proceso compartido por varios nodos que no comparten objetos en memoria.
 */
public interface SessionsDAO {

	/**
	 * Guarda una sesi&oacute;n.
	 * @param session Sesi&oacute;n a guardar.
	 * @param firstSave Indica si la sesi&oacute;n se est&aacute; creando ({@code true})
	 * o si ya lo estaba ({@code false}).
	 */
	void saveSession(FireSession session, boolean firstSave);

	/**
	 * Comprueba la existencia de una sesi&oacute;n.
	 * @param id Identificador de la sesi&oacute;n.
	 * @return {@code true} si la sesi&oacute;n existe, {@code false} en caso contrario.
	 * @throws SessionException Cuando no se haya podido comprobar la existencia de la sesi&oacute;n.
	 */
	boolean existsSession(String id) throws SessionException;

	/**
	 * Recupera una sesi&oacute;n. Esto implica construir el propio objeto de sesi&oacute;n y
	 * renovar su periodo de validez.
	 * @param id Identificador de la sesi&oacute;n.
	 * @param session Sesi&oacute;n web en la que se debe sustentar la sesi&oacute;n.
	 * @return Sesi&oacute;n recuperada o {@code null} si no existe la sesi&oacute;n
	 * o no pudo cargarse.
	 */
	FireSession recoverSession(String id, HttpSession session);

	/**
	 * Elimina una sesi&oacute;n.
	 * @param id Identificador de la sesi&oacute;n.
	 */
	void deleteSession(String id);

	/**
	 * Elimina las sesiones caducadas.
	 * @param expirationTime Tiempo que puede haber trasncurrido desde su uso creaci&oacute;n/modificaci&oacute;n antes
	 * de considerarse caducada.
	 * @throws IOException Cuando ocurre alg&uacute;n error al borrar los ficheros.
	 */
	void deleteExpiredSessions(long expirationTime) throws IOException;

	/**
	 * Recupera el gestor de documentos temporales asociado a un gestor de sesiones.
	 * @return Gestor de documentos temporales o {@code null} si no tuviese uno asociado.
	 */
	TempDocumentsDAO getAssociatedDocumentsDAO();
}
