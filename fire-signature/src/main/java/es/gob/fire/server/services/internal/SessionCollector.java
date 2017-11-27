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

import es.gob.fire.server.services.internal.sessions.SessionsDAO;
import es.gob.fire.server.services.internal.sessions.SessionsDAOFactory;
import es.gob.fire.signature.ConfigManager;

/**
 * Gestiona las transacciones de firma de la aplicaciones almacenando los datos de cada
 * una en sesiones.
 */
public final class SessionCollector {

	/** N&uacute;mero de veces que se puden buscar sesiones antes de buscar aquellas que estan caducadas. */
	private static final int MAX_USE_TO_CLEANING = 250;

	private static final Logger LOGGER = Logger.getLogger(SessionCollector.class.getName());

    private static final Map<String, FireSession> sessions = new HashMap<>();

    private static int uses = 0;

    private static SessionsDAO dao;

    static {

    	final String daoType = ConfigManager.getSessionsDao();
    	if (daoType == null || daoType.isEmpty()) {
    		LOGGER.info("No se configuro un gestor de sesiones. " //$NON-NLS-1$
    				+ "El componente central solo funcionara sobre despliegues en un unico nodo."); //$NON-NLS-1$
    	}
    	else {
    		dao = SessionsDAOFactory.getInstance(daoType);
    		if (dao == null) {
    			LOGGER.warning("Se configuro un tipo de gestor de sesiones no reconocido: " + //$NON-NLS-1$
    					daoType + ". El funcionamiento del componente central fallara en despliegues sobre multiples nodos."); //$NON-NLS-1$
    		}
    		else {
    			LOGGER.info("Se configuro el gestor de sesiones: " + daoType); //$NON-NLS-1$
    		}
    	}
    }

    /**
     * Busca una sesion en el pool de sesiones para eliminarla.
     * @param id Identificador de la sesi&oacute;n.
     */
    public static void removeSession(final String id) {
    	if (id == null) {
    		return;
    	}

    	// Eliminamos la sesion de la memoria del servidor
    	final FireSession fireSession = sessions.get(id);
    	if (fireSession != null) {
    		removeAssociattedTempFiles(fireSession);
    		fireSession.invalidate();
    		sessions.remove(id);
    	}
    	TempFilesHelper.deleteTempData(id);

    	// Eliminamos la sesion del espacio compartido con el resto de nodos
		if (dao != null) {
			dao.removeSession(id);
		}
    }

    /**
     * Elimina por completo una nueva sesi&oacute;n
     * y sus ficheros temporales.
     * @param fireSession Sesi&oacute;n que hay que eliminar.
     */
    public static void removeSession(final FireSession fireSession) {
    	if (fireSession == null) {
    		return;
    	}

    	// Eliminamos los temporales
   		removeAssociattedTempFiles(fireSession);
   		TempFilesHelper.deleteTempData(fireSession.getTransactionId());

   		// Eliminamos la sesion de la memoria
   		sessions.remove(fireSession.getTransactionId());

    	// Eliminamos la sesion del espacio compartido con el resto de nodos
		if (dao != null) {
			dao.removeSession(fireSession.getTransactionId());
		}

		fireSession.invalidate();
    }

    /**
     * Elimina los ficheros temporales asociados a la sesi&oacute;n.
     * @param session Sesi&oacute;n de la que eliminar los ficheros temporales.
     */
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

    /**
     * Elimina los datos de sesion (salvo mensajes de error) y los ficheros temporales
     * asociados, pero no la propia sesion para permitir recuperar el mensaje de error.
     * @param fireSession Sesion que se desea limpiar.
     */
    public static void cleanSession(final FireSession fireSession) {
    	if (fireSession == null) {
    		return;
    	}

    	// Eliminamos los temporales
   		removeAssociattedTempFiles(fireSession);
    	TempFilesHelper.deleteTempData(fireSession.getTransactionId());

    	// Eliminamos todos los datos de sesion menos los que indican errores
    	for (final String attr : fireSession.getAtributteNames()) {
    		if (!attr.equals(ServiceParams.SESSION_PARAM_ERROR_TYPE) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_ERROR_MESSAGE) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_SUBJECT_ID)) {
    			fireSession.removeAttribute(attr);
    		}
    	}
    }

    /**
     * Recorre el listado de sesiones registradas y elimina las que han sobrepasado
     * el periodo de validez.
     */
    private static void cleanExpiredSessions() {
        final ExpiredSessionCleanerThread t = new ExpiredSessionCleanerThread(
        		sessions.keySet().toArray(new String[sessions.size()]), sessions);
        t.start();
    }

	/**
	 * Crea un ID de transaccion que no se encuentra registrado en la sesi&oacute;n.
	 * @return ID de transacci&oacute;n.
	 */
	private static String generateTransactionId() {

		// Definimos un identificador de sesion externo para usar como ID de transaccion
    	String transactionId;
    	do {
    		transactionId = UUID.randomUUID().toString();
    	} while (existTransaction(transactionId));

		return transactionId;
	}

	/**
	 * Indica si existe una transacci&oacute;n con un identificador concreto.
	 * @param trId Identificador del que se quiere comprobar la existencia.
	 * @return {@code true} si ya existe una transacci&oacute;n con ese identificador,
	 * {@code false} en caso contrario.
	 */
	private static boolean existTransaction(final String trId) {
		return sessions.containsKey(trId) || dao != null && dao.existsSession(trId);
	}

    /**
     * Crea un nuevo objeto de sesi&oacute;n en el que almacenar los datos de
     * una transacci&oacute;n.
     * @param httpSession Sesi&oacute;n web.
     * @return Datos de sesi&oacute;n.
     */
    public static FireSession createFireSession(final HttpSession httpSession) {

    	final String transactionId = generateTransactionId();
    	final FireSession fireSession = FireSession.newSession(transactionId, httpSession);

    	fireSession.updateExpirationTime(ConfigManager.getTempsTimeout());

    	sessions.put(transactionId, fireSession);

    	if (dao != null) {
			dao.saveSession(fireSession);
		}

    	return fireSession;
    }

    /**
	 * Recupera una sesi&oacute;n activa con el identificador indicado de un usuario concreto.<br/>
	 * La sesi&oacute;n se carga de las distintas fuentes disponibles, a menos que se indique
	 * que s&oacute;lo se obtenga si ya est&aacute; cargada en memoria. Este es un mecanismo
	 * de seguridad con el que poder evitar que un usuario logueado empiece a reclamar transacciones
	 * de otros usuarios.
	 * @param trId Identificador de transacci&oacute;n a recuperar.
	 * @param userId Identificador del usuario propietario de la transacci&oacute;n.
	 * @param session Sesi&oacute; actual.
	 * @param onlyLoaded Indica si solo se debe recuperar la transacci&oacute;n si ya estaba cargado en memoria.
	 * @return Datos de sesi&oacute;n con la transacci&oacute;n deseada o {@code null} si no se encontr&oacute;
	 * o establa caducada.
	 */
	public static FireSession getFireSession(final String trId, final String userId, final HttpSession session, final boolean onlyLoaded) {

		if (trId == null) {
			return null;
		}

		FireSession fireSession = null;

		// Comprobamos si los datos de la transaccion ya estan cargados
		if (session != null && session.getAttribute(trId) != null) {
			fireSession = findSessionFromCurrentSession(trId, session);
		}

		if (!onlyLoaded) {

			// Comprobamos si la transaccion esta en almacenamiento persistente (para multiples nodos)
			if (fireSession == null) {
				fireSession = findSessionFromSharedMemory(trId, session);
				if (fireSession != null && session != null) {
					fireSession.copySessionAttributes(session);
				}
			}

			// Comprobamos si la transaccion esta en memoria
			if (fireSession == null) {
				fireSession = findSessionFromServerMemory(trId);
				if (fireSession != null && session != null) {
					fireSession.copySessionAttributes(session);
				}
			}

			// Si hemos llegado al limite establecido de peticiones entre las cuales limpiar,
			// ejecutamos la limpieza
			synchronized (sessions) {
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
									userId));
					return null;
				}
			}
			else if (dao != null) {
				LOGGER.warning("Es obligatorio indicar un identificador de usuario cuando se configura un gestor de sesiones"); //$NON-NLS-1$
				return null;
			}
		}

		return fireSession;
	}

	/**
	 * Recupera una sesi&oacute;n con los datos de una transacci&oacute;n desde una sesi&oacute;n web.
	 * @param id Identificador de sesi&oacute;n.
	 * @param httpSession Sesi&oacute;n web.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     * caducada.
	 */
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

	/**
     * Permite recuperar una sesi&oacute;n previamente a&ntilde;adida al pool.
     * @param id Identificador de la sesi&oacute;n dado de cara al cliente.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     * caducada.
     */
    private static FireSession findSessionFromServerMemory(final String id) {

    	final FireSession fireSession = sessions.get(id);
    	if (fireSession != null) {
    		if (fireSession.isExpired()) {
    			removeSession(id);
    			return null;
    		}
    	}

    	return fireSession;
    }

	/**
     * Permite recuperar una sesi&oacute;n almacenada en la memoria compartida, probablemente
     * porque se inicio en otro servidor.
     * @param id Identificador de la sesi&oacute;n dado de cara al cliente.
     * @param session Sesi&oacute;n web en la que se va a cargar la sesi&oacute;n.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     * caducada.
     */
	private static FireSession findSessionFromSharedMemory(final String id, final HttpSession session) {

		FireSession fireSession = null;
		if (dao != null) {
			fireSession = dao.recoverSession(id, session);
			if (fireSession != null) {
				if (fireSession.isExpired()) {
					fireSession.invalidate();
					removeSession(id);
					return null;
				}
			}
		}
		return fireSession;
	}

	/**
	 * Guarda los datos de una transacci&oacute;n para permitir su futura recuperaci&oacute;n.
	 * @param session Datos de la transacci&oacute;n a almacenar.
	 */
	public static void commit(final FireSession session) {

		// Cada vez que se actualiza la sesion, se actualiza su fecha de caducidad.
		// Consideramos que una sesion sobre la que unicamente se hacen consultas, no
		// deberia actualizarse indefinidamente
		session.updateExpirationTime(ConfigManager.getTempsTimeout());

		if (dao != null) {
			dao.saveSession(session);
		}
	}
}
