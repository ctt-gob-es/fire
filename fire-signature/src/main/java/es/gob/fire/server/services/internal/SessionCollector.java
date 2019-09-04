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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.internal.sessions.SessionsDAO;
import es.gob.fire.server.services.internal.sessions.SessionsDAOFactory;
import es.gob.fire.signature.ConfigManager;

/** Gestiona las transacciones de firma de la aplicaciones almacenando los datos de cada
 * una en sesiones. Las sesiones pueden guardarse tanto en memoria como en un espacio
 * compartido por varias instancias de la aplicaci&oacute;n a trav&eacute;s de un DAO.
 * Esta clase gestiona autom&aacute;ticamente el borrado de sesiones caducadas en memoria y
 * en el espacio compartido. Los datos temporales de las sesiones caducadas solo se
 * eliminar&aacute;n a partir de las sesiones almacenadas en memoria, ya que, de hacerlo
 * para las sesiones del espacio compartido, se solicitar&iacute;a su borrado desde cada uno
 * de los nodos del sistema. */
public final class SessionCollector {

	/** N&uacute;mero de veces que se puden buscar sesiones antes de buscar aquellas que estan caducadas. */
	private static final int MAX_USE_TO_CLEANING = 250;

	private static final Logger LOGGER = Logger.getLogger(SessionCollector.class.getName());

	private static final Map<String, FireSession> SESSIONS = new HashMap<>();

	private static int uses = 0;

    private static final SessionsDAO DAO;

    static {

    	// Ejecutamos el proceso de borrado de sesiones caducadas (que no habra ninguna)
    	// y temporales
    	cleanExpiredSessions();

    	// Cargamos el DAO de sesiones compartidas
    	final String daoType = ConfigManager.getSessionsDao();
    	if (daoType == null || daoType.isEmpty()) {
    		LOGGER.warning(
				"No se configuro un gestor de sesiones, el componente central solo funcionara sobre despliegues en un unico nodo" //$NON-NLS-1$
			);
    		DAO = null;
    	}
    	else {
    		DAO = SessionsDAOFactory.getInstance(daoType);
    		if (DAO == null) {
    			LOGGER.warning("Se configuro un tipo de gestor de sesiones no reconocido ('" + //$NON-NLS-1$
					daoType + "'), el funcionamiento del componente central fallara en despliegues sobre multiples nodos."); //$NON-NLS-1$
    		}
    		else {
    			LOGGER.info("Se configuro el gestor de sesiones: " + daoType); //$NON-NLS-1$
    		}
    	}
    }

    /** Crea un nuevo objeto de sesi&oacute;n en el que almacenar los datos de
     * una transacci&oacute;n.
     * @param httpSession Sesi&oacute;n web.
     * @return Datos de sesi&oacute;n. */
    public static FireSession createFireSession(final HttpSession httpSession) {

    	final String transactionId = generateTransactionId();
    	final FireSession fireSession = FireSession.newSession(transactionId, httpSession);

    	fireSession.updateExpirationTime(ConfigManager.getTempsTimeout());

    	SESSIONS.put(transactionId, fireSession);

    	if (DAO != null) {
			DAO.saveSession(fireSession);
		}

    	LOGGER.fine("Se crea la transaccion " + transactionId); //$NON-NLS-1$

    	return fireSession;
    }

    /** Recupera una sesi&oacute;n activa con el identificador indicado de un usuario concreto.<br>
	 * La sesi&oacute;n se carga de las distintas fuentes disponibles, a menos que se indique
	 * que s&oacute;lo se obtenga si ya est&aacute; cargada en memoria. Este es un mecanismo
	 * de seguridad con el que poder evitar que un usuario logueado empiece a reclamar transacciones
	 * de otros usuarios.<br>
	 * Por orden de prioridad, los datos de sesi&oacute;n se recuperan de:
	 * <ol>
	 *  <li>La propia sesi&oacute;n HTTP.</li>
	 *  <li>La colecci&oacute;n de sesiones en memoria.</li>
	 *  <li>El gestor de sesiones entre m&uacute;ltiples nodos.</li>
	 * </ol>
	 * Mediante el par&aacute;metro {@code onlyLoaded} se puede indicar que s&oacute;lo se recupere
	 * la sesi&oacute;n si ya estaba cargada. Esto es &uacute;til cuando sabemos que la sesi&oacute;n
	 * ya est&aacute; cargada (posiblemente, porque se recuper&oacute; anteriormente dentro de la
	 * misma operaci&oacute;n) y no queremos abrir la puerta al resto de comprobaciones por seguridad.
	 * Mediante el par&aacute;metro {@code forceLoad} se puede forzar a que se carguen los datos de
	 * sesi&oacute;n del gestor de sesiones si se defini&oacute; este. Esto se puede utilizar para
	 * asegurarnos de que disponemos de los &uacute;ltimos datos de la transacci&oacute;n.
	 * Si tanto el par&aacute;metro {@code onlyLoaded} como {@code forceLoad} est&aacute;n activos,
	 * no se podr&aacute; recuperar la sesi&oacute;n.
	 * @param trId Identificador de transacci&oacute;n a recuperar.
	 * @param userId Identificador del usuario propietario de la transacci&oacute;n.
	 * @param session Sesi&oacute; actual.
	 * @param onlyLoaded Indica si solo se debe recuperar la transacci&oacute;n si ya estaba cargado en memoria.
	 * @param forceLoad Fuerza que, en caso de haberse definido un gestor de sesiones, se recargue la sesi&oacute;n de &eacute;l.
	 * @return Datos de sesi&oacute;n con la transacci&oacute;n deseada o {@code null} si no se encontr&oacute;
	 *         o establa caducada. */
	public static FireSession getFireSession(final String trId,
			                                 final String userId,
			                                 final HttpSession session,
			                                 final boolean onlyLoaded,
			                                 final boolean forceLoad) {
		if (trId == null) {
			return null;
		}

		// Comprobamos si necesitamos carga de memoria compartida, para saltarnos los
		// pasos previos (ver si ya esta cargado o si se carga de memoria)
		final boolean needForceLoad = canForceLoad(forceLoad);

		FireSession fireSession = null;

		// Comprobamos si los datos de la transaccion ya estan cargados
		if (!needForceLoad && session != null && session.getAttribute(trId) != null) {
			fireSession = findSessionFromCurrentSession(trId, session);

			if (fireSession != null) {
				LOGGER.fine("Sesion ya cargada"); //$NON-NLS-1$
			}
		}

		if (!onlyLoaded) {

			// Comprobamos si la transaccion esta en memoria
			if (fireSession == null && !needForceLoad) {
				fireSession = findSessionFromServerMemory(trId);
				if (fireSession != null && session != null) {
					fireSession.copySessionAttributes(session);
				}
				if (fireSession != null) {
					LOGGER.fine("Sesion cargada de memoria"); //$NON-NLS-1$
				}
			}

			// Comprobamos si la transaccion esta en almacenamiento persistente (para multiples nodos)
			if (fireSession == null) {
				fireSession = findSessionFromSharedMemory(trId, session);
				if (fireSession != null && session != null) {
					fireSession.copySessionAttributes(session);
				}
				if (fireSession != null) {
					LOGGER.fine("Sesion cargada de almacenamiento persistente"); //$NON-NLS-1$
				}
			}

			// Si hemos llegado al limite establecido de peticiones entre las cuales limpiar,
			// ejecutamos la limpieza
			synchronized (SESSIONS) {
				if (++uses > MAX_USE_TO_CLEANING) {
					cleanExpiredSessions();
					uses = 0;
				}
			}
		}

		// Comprobamos que los datos de la sesion se correspondan con los del usuario indicado
		if (fireSession != null) {
			if (userId != null) {
				if (!userId.equals(fireSession.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID))) {
					LOGGER.warning(
							String.format(
									"El usuario %s esta solicitando una transaccion que no le pertenece. Quizas alguien este intentando suplantar su identidad", //$NON-NLS-1$
									LogUtils.cleanText(userId)));
					return null;
				}
			}
			else if (DAO != null) {
				LOGGER.warning("Es obligatorio indicar un identificador de usuario cuando se configura un gestor de sesiones"); //$NON-NLS-1$
				return null;
			}
		}

		return fireSession;
	}

	/** Comprueba si se puede forzar la carga de la sesi&oacute;n de memoria compartida.
	 * @param forceLoad {@code true} si se debe intentar cargar la sesi&oacute;n de
	 *                  memoria compartida, {@code false} en caso contrario.
	 * @return {@code true} si es posible cargar la sesi&oacute;n de memoria compartida. */
	private static boolean canForceLoad(final boolean forceLoad) {
		return forceLoad && DAO != null;
	}

	/** Recupera una sesi&oacute;n con los datos de una transacci&oacute;n desde una sesi&oacute;n web.
	 * @param id Identificador de sesi&oacute;n.
	 * @param httpSession Sesi&oacute;n web.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     *         caducada. */
    private static FireSession findSessionFromCurrentSession(final String id, final HttpSession httpSession) {

    	final FireSession fireSession = FireSession.loadFireSession(id, httpSession);
		if (fireSession != null) {
			if (fireSession.isExpired()) {
				httpSession.removeAttribute(id);
				removeSession(fireSession);
				return null;
			}
		}

		return fireSession;
	}

	/** Permite recuperar una sesi&oacute;n previamente a&ntilde;adida al pool.
     * @param id Identificador de la sesi&oacute;n dado de cara al cliente.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     *         caducada. */
    private static FireSession findSessionFromServerMemory(final String id) {

    	final FireSession fireSession = SESSIONS.get(id);
    	if (fireSession != null) {
    		if (fireSession.isExpired()) {
    			removeSession(fireSession);
    			return null;
    		}
    	}

    	return fireSession;
    }

	/** Permite recuperar una sesi&oacute;n almacenada en la memoria compartida, probablemente
     * porque se inicio en otro servidor.
     * @param id Identificador de la sesi&oacute;n dado de cara al cliente.
     * @param session Sesi&oacute;n web en la que se va a cargar la sesi&oacute;n.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     *         caducada. */
	private static FireSession findSessionFromSharedMemory(final String id, final HttpSession session) {

		FireSession fireSession = null;
		if (DAO != null) {
			fireSession = DAO.recoverSession(id, session);
			if (fireSession != null) {
				if (fireSession.isExpired()) {
					fireSession.invalidate();
					DAO.removeSession(id);
					return null;
				}
			}
		}
		return fireSession;
	}

    /** Busca una sesion en el pool de sesiones para eliminarla junto con sus datos temporales.
     * Si se establecio tambien un DAO de sesiones compartidas, se elimina tambi&eacute;n del mismo.
     * @param id Identificador de la sesi&oacute;n. */
    public static void removeSession(final String id) {
    	if (id == null) {
    		return;
    	}

    	// Buscamos la sesion en la memoria del servidor
    	final FireSession fireSession = SESSIONS.get(id);

    	// Eliminamos los datos de la session (si los encontramos) y la propia session
    	if (fireSession != null) {
    		removeAssociattedTempFiles(fireSession);
    		fireSession.invalidate();
    		SESSIONS.remove(id);
    	}
    	TempFilesHelper.deleteTempData(id);

    	// Eliminamos la sesion del espacio compartido con el resto de nodos
		if (DAO != null) {
			DAO.removeSession(id);
		}

		LOGGER.fine("Se elimina la transaccion " + LogUtils.cleanText(id)); //$NON-NLS-1$
    }

    /** Elimina por completo una sesi&oacute;n y sus ficheros temporales.
     * @param fireSession Sesi&oacute;n que hay que eliminar. */
    public static void removeSession(final FireSession fireSession) {
    	if (fireSession == null) {
    		return;
    	}

    	// Eliminamos los temporales
   		removeAssociattedTempFiles(fireSession);
   		TempFilesHelper.deleteTempData(fireSession.getTransactionId());

   		// Eliminamos la sesion de la memoria
   		SESSIONS.remove(fireSession.getTransactionId());

    	// Eliminamos la sesion del espacio compartido con el resto de nodos
		if (DAO != null) {
			DAO.removeSession(fireSession.getTransactionId());
		}

		fireSession.invalidate();

		LOGGER.fine("Se elimina la transaccion " + fireSession.getTransactionId()); //$NON-NLS-1$
    }

    /** Elimina los ficheros temporales asociados a la sesi&oacute;n.
     * @param session Sesi&oacute;n de la que eliminar los ficheros temporales. */
    private static void removeAssociattedTempFiles(final FireSession session) {

    	if (session.containsAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT)) {
    		final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
    		if (batchResult != null) {
    			final Iterator<String> it = batchResult.iterator();
    			while (it.hasNext()) {
    				TempFilesHelper.deleteTempData(batchResult.getDocumentReference(it.next()));
    			}
    		}
    	}
    }

    /** Elimina los datos de sesi&oacute;n (salvo mensajes de error, el indicador sobre si
     * en alg&uacute;n momento se cedi&oacute; el control de la transacci&oacute;n en
     * cuesti&oacute;n y el identificador del usuario) y los ficheros temporales
     * asociados, pero no la propia sesion para permitir recuperar el mensaje de error.
     * @param fireSession Sesion que se desea limpiar. */
    static void cleanSession(final FireSession fireSession) {
    	if (fireSession == null) {
    		return;
    	}

    	// Eliminamos los temporales
   		removeAssociattedTempFiles(fireSession);
    	TempFilesHelper.deleteTempData(fireSession.getTransactionId());

    	// Eliminamos todos los datos de sesion menos los que indican errores
    	for (final String attr : fireSession.getAttributteNames()) {
    		if (!attr.equals(ServiceParams.SESSION_PARAM_ERROR_TYPE) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_ERROR_MESSAGE) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_CERT_ORIGIN) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_REDIRECTED) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_SUBJECT_ID)) {
    			fireSession.removeAttribute(attr);
    		}
    	}
    }

    /** Recorre el listado de sesiones registradas y elimina las que han sobrepasado
     * el periodo de validez. */
    private static void cleanExpiredSessions() {
        final ExpiredSessionCleanerThread t = new ExpiredSessionCleanerThread(
    		SESSIONS.keySet().toArray(new String[SESSIONS.size()]),
    		SESSIONS,
    		ConfigManager.getTempsTimeout()
		);
        t.start();
    }

	/** Crea un ID de transaccion que no se encuentra registrado en la sesi&oacute;n.
	 * @return ID de transacci&oacute;n. */
	private static String generateTransactionId() {

		// Definimos un identificador de sesion externo para usar como ID de transaccion
    	String transactionId;
    	do {
    		transactionId = UUID.randomUUID().toString();
    	} while (existTransaction(transactionId));

		return transactionId;
	}

	/** Indica si existe una transacci&oacute;n con un identificador concreto.
	 * @param trId Identificador del que se quiere comprobar la existencia.
	 * @return {@code true} si ya existe una transacci&oacute;n con ese identificador,
	 *         {@code false} en caso contrario. */
	private static boolean existTransaction(final String trId) {
		return SESSIONS.containsKey(trId) || DAO != null && DAO.existsSession(trId);
	}

	/** Guarda los datos de una transacci&oacute;n para permitir su futura recuperaci&oacute;n.
	 * @param session Datos de la transacci&oacute;n a almacenar. */
	public static void commit(final FireSession session) {

		// Cada vez que se actualiza la sesion, se actualiza su fecha de caducidad.
		// Consideramos que una sesion sobre la que unicamente se hacen consultas, no
		// deberia actualizarse indefinidamente
		session.updateExpirationTime(ConfigManager.getTempsTimeout());

		// Actualizamos la informacion de la sesion, que ya existira, de la relacion que se
		// guarda en memoria
		SESSIONS.put(session.getTransactionId(), session);

		// Actualizamos, si procede, la informacion de la sesion de la memoria compartida
		if (DAO != null) {
			DAO.saveSession(session);
		}
	}
}
