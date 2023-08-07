/* Copyright (C) 2023 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 03/08/2023
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Gestor con los m&eacute;todos para la espera de un pool de tareas.
 */
public class TasksPoolManager {

	/**
	 * Espera indefinidamente a que finalicen todos las tareas de un listado. Si se indica
	 * que se deben detener el proceso en caso de error, interrumpe todas las tareas.
	 * @param tasks Listado de tareas en ejecuci&oacute;n a las que hay que esperar.
	 * @param stopOnError Si es {@code true} se detendr&aacute;n todas las tareas si una
	 * de ellas se interrumpe. {@code false} en caso contrario.
	 * @param session Sesi&oacute;n en la que guardar el n&uacute;mero de hilos que
	 * a&uacute;n no han terminado.
	 * @param attrName Nombre del atributo de sesi&oacute;n con el que se guardar&aacute;.
	 */
	public static void waitTasks(final List<Future<String>> tasks, final boolean stopOnError, final FireSession session, final String attrName) {

        boolean stopTasks = false;
        int lives;
        do {
        	lives = 0;
        	for (final Future<String> t : tasks) {
        		// Si no termino, lo contamos como tarea viva
        		if (!t.isDone()) {
        			lives++;
        			if (stopTasks) {
        				t.cancel(true);
        			}
        		}
        		// Si termino, comprobamos si debemos para en caso de error,
        		// en cuyo caso comprobamos si fallo
        		else if (stopOnError) {
        			try {
        				stopTasks = t.isCancelled() || t.get() == null;
        			}
        			catch (final Exception e) {
        				stopTasks = true;
					}
        		}
        	}

        	// Actualizamos en la sesion el numero de hilos que faltan por terminar
        	if (session != null && attrName != null) {
        		synchronized (session) {
        			session.setAttribute(attrName, Integer.valueOf(lives));
        		}
        	}

        	// Si aun quedan tareas vivas, esperamos un poco antes
        	// de volver a consultarlas
        	if (lives > 0) {
        		try {
					Thread.sleep(2000);
        		} catch (final InterruptedException e) {
        			// No hacemos nada
        		}
        	}
        } while (lives > 0);
	}
}
