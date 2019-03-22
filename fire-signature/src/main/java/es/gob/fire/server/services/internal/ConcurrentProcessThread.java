/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.util.List;

/** Hilo preparado para la ejecuci&oacute;n de m&uacute;ltiples
 * instancias concurrentes y permitir deternerlos todos si alguno
 * falla en su ejecuci&oacute;n. */
public abstract class ConcurrentProcessThread extends Thread {

	private boolean failed;

	protected void setFailed(final boolean failed) {
		this.failed = failed;
	}

	/**
	 * Indica si la tarea finaliz&oacute; con errores.
	 * @return {@code true} si la tarea fall&oacute;, {@code false} en caso contrario.
	 */
	public boolean isFailed() {
		return this.failed;
	}

	/**
	 * Espera indefinidamente a que finalicen todos los hilos de un listado. Si se indica
	 * que se deben detener el proceso en caso de error, interrumpe todos los hilos.
	 * @param threads Listado de hilos en ejecuci&oacute;n a los que hay que esperar.
	 * @param stopOnError Si es {@code true} se detendr&aacute;n todos los hilos si uno
	 * de ellos se interrumpe. {@code false} en caso contrario.
	 */
	public static void waitThreads(final List<ConcurrentProcessThread> threads, final boolean stopOnError) {

        boolean stopThreads = false;
        int lives;
        do {
        	lives = 0;
        	for (final ConcurrentProcessThread t : threads) {
        		if (t.isAlive()) {
        			lives++;
        			if (stopThreads) {
        				t.interrupt();
        			}
        		}
        		else if (t.isFailed() || t.isInterrupted()) {
        			if (stopOnError) {
        				stopThreads = true;
        			}
        		}
        	}
        	// Si aun quedan hilos vivos, esperamos un poco antes
        	// de volver a consultarlos
        	if (lives > 0) {
        		try {
					Thread.sleep(2000);
        		}
        		catch (final InterruptedException e) {
        			// No hacemos nada
        		}
        	}
        } while (lives > 0);
	}

	/**
	 * Espera indefinidamente a que finalicen todos los hilos de un listado. Si se indica
	 * que se deben detener el proceso en caso de error, interrumpe todos los hilos.
	 * @param threads Listado de hilos en ejecuci&oacute;n a los que hay que esperar.
	 * @param stopOnError Si es {@code true} se detendr&aacute;n todos los hilos si uno
	 * de ellos se interrumpe. {@code false} en caso contrario.
	 * @param session Sesi&oacute;n en la que guardar el n&uacute;mero de hilos que
	 * a&uacute;n no han terminado.
	 * @param attrName Nombre del atributo de sesi&oacute;n con el que se guardar&aacute;.
	 */
	public static void waitThreads(final List<ConcurrentProcessThread> threads,
			                       final boolean stopOnError,
			                       final FireSession session,
			                       final String attrName) {

        boolean stopThreads = false;
        int lives;
        do {
        	lives = 0;
        	for (final ConcurrentProcessThread t : threads) {
        		if (t.isAlive()) {
        			lives++;
        			if (stopThreads) {
        				t.interrupt();
        			}
        		}
        		else if (t.isFailed() || t.isInterrupted()) {
        			if (stopOnError) {
        				stopThreads = true;
        			}
        		}
        	}

        	// Actualizamos en la sesion el numero de hilos que faltan por terminar
        	synchronized (session) {
        		session.setAttribute(attrName, new Integer(lives));
        	}

        	// Si aun quedan hilos vivos, esperamos un poco antes
        	// de volver a consultarlos
        	if (lives > 0) {
        		try {
					Thread.sleep(2000);
        		}
        		catch (final InterruptedException e) {
        			// No hacemos nada
        		}
        	}
        } while (lives > 0);
	}
}
