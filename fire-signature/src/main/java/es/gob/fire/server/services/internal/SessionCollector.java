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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import es.gob.fire.server.services.LogUtils;
import es.gob.fire.server.services.internal.sessions.SessionException;
import es.gob.fire.server.services.internal.sessions.SessionsDAO;
import es.gob.fire.server.services.internal.sessions.SessionsDAOFactory;
import es.gob.fire.server.services.internal.sessions.TempDocumentsDAO;
import es.gob.fire.server.services.statistics.AuditConfig;
import es.gob.fire.server.services.statistics.AuditSignatureRecorder;
import es.gob.fire.server.services.statistics.AuditTransactionRecorder;
import es.gob.fire.server.services.statistics.SignatureRecorder;
import es.gob.fire.server.services.statistics.StatisticsConfig;
import es.gob.fire.server.services.statistics.TransactionRecorder;
import es.gob.fire.server.services.statistics.TransactionType;
import es.gob.fire.signature.ConfigManager;

/**
 * Gestiona las transacciones de firma de la aplicaciones almacenando los datos de cada
 * una en sesiones. Las sesiones pueden guardarse tanto en memoria como en un espacio
 * compartido por varias instancias de la aplicaci&oacute;n a trav&eacute;s de un DAO.
 * Esta clase gestiona autom&aacute;ticamente el borrado de sesiones caducadas en memoria y
 * en el espacio compartido. Los datos temporales de las sesiones caducadas solo se
 * eliminar&aacute;n a partir de las sesiones almacenadas en memoria, ya que, de hacerlo
 * para las sesiones del espacio compartido, se solicitar&iacute;a su borrado desde cada uno
 * de los nodos del sistema.
 */
public final class SessionCollector {

	private static final Logger LOGGER = Logger.getLogger(SessionCollector.class.getName());

	private static final String DEFAULT_EXPIRED_ERROR_TEXT = "La sesion ha caducado"; //$NON-NLS-1$

	static final TransactionRecorder TRANSLOGGER = TransactionRecorder.getInstance();
	static final AuditTransactionRecorder AUDITTRANSLOGGER = AuditTransactionRecorder.getInstance();
	static final SignatureRecorder SIGNLOGGER = SignatureRecorder.getInstance();
	static final AuditSignatureRecorder AUDITSIGNLOGGER = AuditSignatureRecorder.getInstance();

	private static Map<String, FireSession> sessions = null;

	/**
	 * &Uacute;timo proceso de limpieza que se ejecut&oacute;.
	 */
	private static Future<?> cleaningProcess = null;

	/**
	 * N&uacute;mero de veces que se puden guardar sesiones antes ejecutar el proceso para
	 * eliminar aquellas que estan caducadas.
	 */
	private static final int MAX_USE_TO_CLEANING = 500;

	/** Cadena de caracteres usados en la codificaci&oacute;n hexadecimal. */
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray(); //$NON-NLS-1$

	private static int uses = 0;

    private static SessionsDAO dao = null;

    private static SecureRandom random = null;

    private static boolean needRegistry = false;

    static {

    	sessions = new HashMap<>();

    	// Cargamos el DAO de sesiones compartidas
    	final String daoType = ConfigManager.getSessionsDao();
    	if (daoType == null || daoType.trim().isEmpty()) {
    		LOGGER.warning("No se configuro un gestor de sesiones. " //$NON-NLS-1$
    				+ "El componente central solo funcionara sobre despliegues en un unico nodo."); //$NON-NLS-1$
    	}
    	else {
    		dao = SessionsDAOFactory.getInstance(daoType);
    		if (dao == null) {
    			LOGGER.warning("Se configuro un tipo de gestor de sesiones no reconocido o no disponible: " + //$NON-NLS-1$
    					daoType + ". El funcionamiento del componente central fallara en despliegues sobre multiples nodos."); //$NON-NLS-1$
    		}
    		else {
    			LOGGER.info("Se configuro el gestor de sesiones: " + daoType); //$NON-NLS-1$
    		}
    	}

    	// Ejecutamos el proceso de borrado de sesiones caducadas (que no habra ninguna)
    	// y temporales
    	deleteExpiredSessions(new TransactionAuxParams());

    	// Inicializamos el generador de aleatorios
    	random = new SecureRandom();

    	needRegistry = StatisticsConfig.needRegistry() || AuditConfig.needRegistry();
    }


    /**
     * Crea un nuevo objeto de sesi&oacute;n para los datos de una
     * transacci&oacute;n. La sesi&oacute;n ya contar&aacute; con el
     * identificador y la referencia del usuario, pero debe llamarse a
     * {@link #commit(FireSession, TransactionAuxParams)} para guardar la sesi&oacute;n.
     * @param subjectId Identificador del usuario que crea la sesi&oacute;n.
     * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
     * @return Datos de sesi&oacute;n.
     */
    public static FireSession createFireSession(final String subjectId, final TransactionAuxParams trAux) {

    	final String transactionId = generateTransactionId();
    	final String subjectRef;
    	if (transactionId != null) {
    		subjectRef = generateSubjectRef(transactionId, subjectId);
    	} else {
    		LOGGER.warning("No se pudo generar la variable subjectRef puesto que transactionId es nulo"); //$NON-NLS-1$
    		subjectRef = null;
    	}

    	final FireSession fireSession = FireSession.newSession(transactionId);
    	fireSession.setAttribute(ServiceParams.SESSION_PARAM_SUBJECT_ID, subjectId);
    	if (subjectRef != null) {
    		fireSession.setAttribute(ServiceParams.SESSION_PARAM_SUBJECT_REF, subjectRef);
    	}

   		LOGGER.fine(trAux.getLogFormatter().fTr(transactionId, "Se crea la transaccion " + transactionId)); //$NON-NLS-1$

    	return fireSession;
    }

	/**
	 * Recupera una sesi&oacute;n activa con el identificador indicado de un usuario concreto.<br>
	 * La sesi&oacute;n se carga de las distintas fuentes disponibles, a menos que se indique
	 * que s&oacute;lo se obtenga si ya est&aacute; cargada en memoria. Este es un mecanismo
	 * de seguridad con el que poder evitar que un usuario logueado empiece a reclamar transacciones
	 * de otros usuarios.<br>
	 * Por orden de prioridad, los datos de sesi&oacute;n se recuperan de:
	 * <ol>
	 * <li>La propia sesi&oacute;n HTTP.</li>
	 * <li>La colecci&oacute;n de sesiones en memoria.</li>
	 * <li>El gestor de sesiones entre m&uacute;ltiples nodos.</li>
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
	 * @param onlyLoaded Indica si solo se debe recuperar la transacci&oacute;n si ya estaba cargado
	 * en memoria.
	 * @param forceLoad Fuerza que, en caso de haberse definido un gestor de sesiones, se recargue la
	 * sesi&oacute;n de &eacute;l.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @return Datos de sesi&oacute;n con la transacci&oacute;n deseada o {@code null} si no se
	 * encontr&oacute; o establa caducada.
	 */
    public static FireSession getFireSession(final String trId, final String userId,
    		final HttpSession session, final boolean onlyLoaded, final boolean forceLoad,
    		final TransactionAuxParams trAux) {
    	return getSession(trId, userId, session, onlyLoaded, forceLoad, false, trAux);
    }

    /**
	 * Recupera una sesi&oacute;n activa con el identificador indicado de un usuario concreto,
	 * del que, en lugar de su identificador, se pasa un c&oacute;digo asociado al mismo.<br>
	 * La sesi&oacute;n se carga de las distintas fuentes disponibles, a menos que se indique
	 * que s&oacute;lo se obtenga si ya est&aacute; cargada en memoria. Este es un mecanismo
	 * de seguridad con el que poder evitar que un usuario logueado empiece a reclamar transacciones
	 * de otros usuarios.<br>
	 * Por orden de prioridad, los datos de sesi&oacute;n se recuperan de:
	 * <ol>
	 * <li>La propia sesi&oacute;n HTTP.</li>
	 * <li>La colecci&oacute;n de sesiones en memoria.</li>
	 * <li>El gestor de sesiones entre m&uacute;ltiples nodos.</li>
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
	 * @param userRef C&oacute;digo asociado al usuario propietario de la transacci&oacute;n.
	 * @param session Sesi&oacute; actual.
	 * @param onlyLoaded Indica si solo se debe recuperar la transacci&oacute;n si ya estaba cargado
	 * en memoria.
	 * @param forceLoad Fuerza que, en caso de haberse definido un gestor de sesiones, se recargue la
	 * sesi&oacute;n de &eacute;l.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @return Datos de sesi&oacute;n con la transacci&oacute;n deseada o {@code null} si no se
	 * encontr&oacute; o establa caducada.
	 */
    public static FireSession getFireSessionOfuscated(final String trId, final String userRef,
    		final HttpSession session, final boolean onlyLoaded, final boolean forceLoad,
    		final TransactionAuxParams trAux) {
    	return getSession(trId, userRef, session, onlyLoaded, forceLoad, true, trAux);
    }

    /**
	 * Recupera una sesi&oacute;n activa con el identificador indicado de un usuario concreto.<br>
	 * La sesi&oacute;n se carga de las distintas fuentes disponibles, a menos que se indique
	 * que s&oacute;lo se obtenga si ya est&aacute; cargada en memoria. Este es un mecanismo
	 * de seguridad con el que poder evitar que un usuario logueado empiece a reclamar transacciones
	 * de otros usuarios.<br>
	 * Por orden de prioridad, los datos de sesi&oacute;n se recuperan de:
	 * <ol>
	 * <li>La propia sesi&oacute;n HTTP.</li>
	 * <li>La colecci&oacute;n de sesiones en memoria.</li>
	 * <li>El gestor de sesiones entre m&uacute;ltiples nodos.</li>
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
	 * @param userRef Identificador o c&oacute;digo asociado al usuario propietario de la transacci&oacute;n.
	 * @param session Sesi&oacute; actual.
	 * @param onlyLoaded Indica si solo se debe recuperar la transacci&oacute;n si ya estaba cargado en memoria.
	 * @param forceLoad Fuerza que, en caso de haberse definido un gestor de sesiones, se recargue la sesi&oacute;n de &eacute;l.
	 * @param ofuscated Indica si deber&aacute; trabajarse con el identificador ofuscado del usuario para evitar
	 * que se tranmita en la medida de lo posible y almacene en los logs el identificador (DNI) del usuario.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 * @return Datos de sesi&oacute;n con la transacci&oacute;n deseada o {@code null} si no se encontr&oacute;
	 * o establa caducada.
	 */
	private static FireSession getSession(final String trId, final String userRef,
			final HttpSession session, final boolean onlyLoaded, final boolean forceLoad,
			final boolean ofuscated, final TransactionAuxParams trAux) {

		if (trId == null || userRef == null) {
			return null;
		}

		// Comprobamos si necesitamos carga de memoria compartida, para saltarnos los
		// pasos previos (ver si ya esta cargado o si se carga de memoria)
		final boolean needForceLoad = canForceLoad(forceLoad);

		FireSession fireSession = null;

		// Comprobamos si los datos de la transaccion ya estan cargados
		if (!needForceLoad && session != null && session.getAttribute(trId) != null) {
			fireSession = findSessionFromCurrentSession(trId, session, trAux);

			if (fireSession != null) {
				LOGGER.info(trAux.getLogFormatter().fTr(trId, "Sesion ya cargada")); //$NON-NLS-1$
			}
		}

		if (!onlyLoaded) {

			// Comprobamos si la transaccion esta en memoria
			if (fireSession == null && !needForceLoad) {
				fireSession = findSessionFromServerMemory(trId, trAux);
				if (fireSession != null && session != null) {
					fireSession.saveIntoHttpSession(session);
				}
				if (fireSession != null) {
					LOGGER.info(trAux.getLogFormatter().fTr(trId, "Sesion cargada de memoria")); //$NON-NLS-1$
				}
			}

			// Comprobamos si la transaccion esta en almacenamiento persistente (para multiples nodos)
			if (fireSession == null) {
				fireSession = findSessionFromSharedMemory(trId, session, trAux);
				if (fireSession != null && session != null) {
					fireSession.saveIntoHttpSession(session);
				}
				if (fireSession != null) {
					LOGGER.info(trAux.getLogFormatter().fTr(trId, "Sesion cargada de almacenamiento persistente")); //$NON-NLS-1$
				}
			}
		}

		if (onlyLoaded && fireSession == null) {
			LOGGER.warning(trAux.getLogFormatter().fTr(trId, "Se esperaba que la sesion estuviese en memoria y no fue asi")); //$NON-NLS-1$
		}

		// Comprobamos que los datos de la sesion se correspondan con los del usuario indicado
		if (fireSession != null) {
			final String savedRef = ofuscated
				? fireSession.getString(ServiceParams.SESSION_PARAM_SUBJECT_REF)
				: fireSession.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);

			if (!userRef.equals(savedRef)) {
				LOGGER.warning(trAux.getLogFormatter().fTr(
					trId,
					"Se esta intentando reclamar la transaccion para un usuario al que no le pertenece. Quizas alguien este intentando suplantar su identidad de " //$NON-NLS-1$
					+ LogUtils.cleanText(userRef)));
				return null;
			}
		}

		return fireSession;
	}

	/**
	 * Comprueba si se puede forzar la carga de la sesi&oacute;n de memoria compartida.
	 * @param forceLoad {@code true} si se debe intentar cargar la sesi&oacute;n de
	 * memoria compartida, {@code false} en caso contrario.
	 * @return {@code true} si es posible cargar la sesi&oacute;n de memoria compartida.
	 */
	private static boolean canForceLoad(final boolean forceLoad) {
		return forceLoad && dao != null;
	}

	/**
	 * Recupera una sesi&oacute;n con los datos de una transacci&oacute;n desde una sesi&oacute;n web.
	 * @param id Identificador de sesi&oacute;n.
	 * @param httpSession Sesi&oacute;n web.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     * caducada.
	 */
    private static FireSession findSessionFromCurrentSession(final String id, final HttpSession httpSession, final TransactionAuxParams trAux) {

    	final FireSession fireSession = FireSession.loadFireSession(id, httpSession);
		if (fireSession != null && fireSession.isExpired()) {
			httpSession.removeAttribute(id);
			removeSession(fireSession, trAux);
			return null;
		}

		return fireSession;
	}

	/**
     * Permite recuperar una sesi&oacute;n previamente a&ntilde;adida al pool.
     * @param id Identificador de la sesi&oacute;n dado de cara al cliente.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     * caducada.
     */
    private static FireSession findSessionFromServerMemory(final String id, final TransactionAuxParams trAux) {

    	final FireSession fireSession = sessions.get(id);
    	if (fireSession != null && fireSession.isExpired()) {
			removeSession(fireSession, trAux);
			return null;
		}

    	return fireSession;
    }

	/**
     * Permite recuperar una sesi&oacute;n almacenada en la memoria compartida, probablemente
     * porque se inicio en otro servidor.
     * @param id Identificador de la sesi&oacute;n dado de cara al cliente.
     * @param session Sesi&oacute;n web en la que se va a cargar la sesi&oacute;n.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
     * @return Sesi&oacute;n preestablecida o {@code null} si no se encontr&oacute; o estaba
     * caducada.
     */
	private static FireSession findSessionFromSharedMemory(final String id, final HttpSession session, final TransactionAuxParams trAux) {

		FireSession fireSession = null;
		if (dao != null) {
			fireSession = dao.recoverSession(id, session);
			if (fireSession != null && fireSession.isExpired()) {
				removeSession(fireSession, trAux);
				return null;
			}
		}
		return fireSession;
	}

    /**
     * Elimina por completo una sesi&oacute;n y sus ficheros temporales.
     * @param fireSession Sesi&oacute;n que hay que eliminar.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
     */
    public static void removeSession(final FireSession fireSession, final TransactionAuxParams trAux) {
    	if (fireSession == null) {
    		return;
    	}

    	// Comprobamos si la sesion estaba caducada
    	final boolean expired = fireSession.isExpired();

    	// Eliminamos los temporales
   		removeAssociattedTempFiles(fireSession, trAux);
   		try {
			TempDocumentsManager.deleteDocument(fireSession.getTransactionId());
		} catch (final IOException e) {
			LOGGER.warning(trAux.getLogFormatter().fTr(fireSession.getTransactionId(), "No se pudo eliminar el documento de la transaccion: " + e)); //$NON-NLS-1$
		}

   		// Eliminamos la sesion de la memoria
   		synchronized (sessions) {
   			sessions.remove(fireSession.getTransactionId());
   		}

    	// Eliminamos la sesion del espacio compartido con el resto de nodos.
   		// Ademas, si somos los primeros en eliminarla (cualquier intento futuro no la
   		// encontrara en el almacenamiento compartido) y la sesion estaba caducada,
   		// registramos el error. En cualquier otro caso, interpretaremos que ya se
   		// registro la caducidad
   		if (dao == null) {
   			if (expired && needRegistry) {
   				registryExpirationErrors(fireSession);
   			}
   		} else {
   			final boolean removed = removeSessionFromDao(fireSession);
   			if (expired && removed && needRegistry) {
   				registryExpirationErrors(fireSession);
   			}
   		}

		fireSession.invalidate();

    	LOGGER.fine(trAux.getLogFormatter().fTr(fireSession.getTransactionId(), "Se elimina la transaccion")); //$NON-NLS-1$
    }

    /**
     * Registra en las estad&iacute;sticas y la auditor&iacute;a una transacci&oacute;n y sus firmas
     * como caducadas.
     * @param fireSession Informaci&oacute;n de la transacci&oacute;n.
     */
    private static void registryExpirationErrors(final FireSession fireSession) {

    	// Si se trata de una operacion de firma, marcamos que tanto la transaccion como la firma caducaron
    	final TransactionType op = (TransactionType) fireSession.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE);
    	if (op == TransactionType.SIGN) {
    		AUDITSIGNLOGGER.register(fireSession, false, null, DEFAULT_EXPIRED_ERROR_TEXT);
    		SIGNLOGGER.register(fireSession, false, null);
    		TRANSLOGGER.register(fireSession, false);
    		AUDITTRANSLOGGER.register(fireSession, false, DEFAULT_EXPIRED_ERROR_TEXT);
    	}
    	// Si se trata de una operacion de lote
    	else if (op == TransactionType.BATCH) {
    		// Si el lote no se recupero (en cuyo caso se habria indicado que finalizo correctamente),
    		// se marca como fallido
    		if (!Boolean.TRUE.equals(fireSession.getObject(ServiceParams.SESSION_PARAM_BATCH_RECOVERED))) {
				TRANSLOGGER.register(fireSession, false);
    			AUDITTRANSLOGGER.register(fireSession, false, DEFAULT_EXPIRED_ERROR_TEXT);
    		}
    		// Se registran como erroneas las firmas del lote no recuperadas que quedasen pendientes
			registryBatchSignErrors(fireSession);
    	}
    	// Si es cualquier otro tipo de transaccion, se marca como caducada
    	else {
    		TRANSLOGGER.register(fireSession, false);
    		AUDITTRANSLOGGER.register(fireSession, false, DEFAULT_EXPIRED_ERROR_TEXT);
    	}
    }

    /**
     * Registra como caducadas en las estad&iacute;sticas y la auditor&iacute;a las firmas de un lote
     * que no estuviesen ya marcadas como err&oacute;neas o recuperadas.
     * @param fireSession Informaci&oacute;n de la transacci&oacute;n.
     */
	private static void registryBatchSignErrors(final FireSession fireSession) {

		final BatchResult batchResult = (BatchResult) fireSession.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
		final Iterator<String> it = batchResult.iterator();
		while (it.hasNext()) {
			final String docId = it.next();
			if (!batchResult.isSignRecovered(docId) && !batchResult.isSignFailed(docId)) {
				AUDITSIGNLOGGER.register(fireSession, false, null, DEFAULT_EXPIRED_ERROR_TEXT);
	    		SIGNLOGGER.register(fireSession, false, null);
			}
		}
	}

	private static boolean removeSessionFromDao(final FireSession fireSession) {
		return dao.deleteSession(fireSession.getTransactionId());
    }

    /**
     * Elimina los ficheros temporales asociados a la sesi&oacute;n.
     * @param session Sesi&oacute;n de la que eliminar los ficheros temporales.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
     */
    private static void removeAssociattedTempFiles(final FireSession session, final TransactionAuxParams trAux) {

    	if (session.containsAttribute(ServiceParams.SESSION_PARAM_BATCH_RESULT)) {
    		final BatchResult batchResult = (BatchResult) session.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
    		if (batchResult != null) {
    			final Iterator<String> it = batchResult.iterator();
    			while (it.hasNext()) {
    				final String docId = it.next();
    				try {
						TempDocumentsManager.deleteDocument(batchResult.getDocumentReference(docId));
					} catch (final IOException e) {
						LOGGER.warning(trAux.getLogFormatter().fTr(
								session.getTransactionId(),
								"No se pudo eliminar de la transaccion el documento " + docId + ": " + e)); //$NON-NLS-1$ //$NON-NLS-2$
					}
    			}
    		}
    	}
    }

    /**
     * Elimina los datos de sesi&oacute;n (salvo mensajes de error, el indicador sobre si
     * en alg&uacute;n momento se cedi&oacute; el control de la transacci&oacute;n en
     * cuesti&oacute;n y el identificador del usuario) y los ficheros temporales
     * asociados, pero no la propia sesion para permitir recuperar el mensaje de error.
     * @param fireSession Sesion que se desea limpiar.
     */
    static void cleanSession(final FireSession fireSession, final TransactionAuxParams trAux) {
    	if (fireSession == null) {
    		return;
    	}

    	// Eliminamos los temporales
    	removeAssociattedTempFiles(fireSession, trAux);
    	try {
    		TempDocumentsManager.deleteDocument(fireSession.getTransactionId());
    	} catch (final IOException e) {
    		LOGGER.warning(trAux.getLogFormatter().fTr(
    				fireSession.getTransactionId(),
    				"No se pudo eliminar el documento de la transaccion: " + e)); //$NON-NLS-1$
    	}

    	// Eliminamos todos los datos de sesion menos los que ayudan a identificar errores
    	for (final String attr : fireSession.getAttributteNames()) {
    		if (!attr.equals(ServiceParams.SESSION_PARAM_ERROR_TYPE) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_ERROR_MESSAGE) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_CERT_ORIGIN) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_REDIRECTED_LOGIN) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_REDIRECTED_SIGN) &&
    				!attr.equals(ServiceParams.SESSION_PARAM_SUBJECT_ID)) {
    			fireSession.removeAttribute(attr);
    		}
    	}
    }

	/**
	 * Crea un ID de transaccion que no se encuentra registrado en la sesi&oacute;n.
	 * @return ID de transacci&oacute;n.
	 */
	private static String generateTransactionId() {

		// Definimos un identificador de sesion externo para usar como ID de transaccion
    	String transactionId = null;
    	try {
    		do {
    			transactionId = UUID.randomUUID().toString();
    		} while (existTransaction(transactionId));
    	}
    	catch (final Exception e) {
    		LOGGER.warning(
    				"No se crea una sesion compartida. La sesion solo estara disponible en local con el ID: " + //$NON-NLS-1$
    						transactionId);
    	}

		return transactionId;
	}


	/**
	 * Genera un c&oacute;digo de referencia al usuario. Este codigo se utilizar&aacute;
	 * para referirnos al usuario sin necesidad de hacer uso de su identificador. El
	 * c&oacute;digo sirve para una transacci&oacute;n concreta.
	 * @param transactionId Identificador de transacci&oacute;n.
	 * @param subjectId Identificador de usuario.
	 * @return C&oacute;digo de referncia del usuario para esta transacci&oacute;n.
	 */
    private static String generateSubjectRef(final String transactionId, final String subjectId) {

    	MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD2"); //$NON-NLS-1$
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.warning("No se puede iniciar el generador de hashes para la creacion de referencias de usuario: " + e); //$NON-NLS-1$
			return bytesToHex(subjectId.getBytes());
		}
    	md.update(transactionId.getBytes());
    	md.update(generateRandomBytes());
    	md.update(subjectId.getBytes());

    	return bytesToHex(md.digest());
	}

    /**
     * Genera un array con 8 bytes aleatorios.
     * @return
     */
    private static byte[] generateRandomBytes() {
    	final byte[] randomBytes = new byte[16];
    	random.nextBytes(randomBytes);
    	return randomBytes;
    }

    /**
     * Codifica un binario a hexadecimal.
     * @param bytes Binario a codificar.
     * @return Cadena hexadecimal.
     */
    private static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

	/**
	 * Indica si existe una transacci&oacute;n con un identificador concreto.
	 * @param trId Identificador del que se quiere comprobar la existencia.
	 * @return {@code true} si ya existe una transacci&oacute;n con ese identificador,
	 * {@code false} en caso contrario.
	 * @throws SessionException Cuando no se haya podido comprobar la existencia de la sesi&oacute;n.
	 */
	private static boolean existTransaction(final String trId) throws SessionException {
		return sessions.containsKey(trId) || dao != null && dao.existsSession(trId);
	}

	/**
	 * Guarda los datos de una transacci&oacute;n para permitir su futura recuperaci&oacute;n.
	 * Si es la primera vez que se guarda la sesi&oacute;n, se deber&iacute;a usar el m&eacute;todo
	 * {@link #commit(FireSession, boolean, TransactionAuxParams)}.
	 * @param session Datos de la transacci&oacute;n a almacenar.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	public static void commit(final FireSession session, final TransactionAuxParams trAux) {
		commit(session, false, trAux);
	}

	/**
	 * Guarda los datos de una transacci&oacute;n para permitir su futura recuperaci&oacute;n.
	 * @param session Datos de la transacci&oacute;n a almacenar.
	 * @param creation Indica si la sesi&oacute;n se est&aacute; guardado por primera vez.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
	 */
	public static void commit(final FireSession session, final boolean creation,
			final TransactionAuxParams trAux) {

		// Cada vez que se actualiza la sesion, se actualiza su fecha de caducidad.
		// Consideramos que una sesion sobre la que unicamente se hacen consultas, no
		// deberia actualizarse indefinidamente
		session.updateExpirationTime();

		// Actualizamos la informacion de la sesion, que ya existira, de la relacion que se
		// guarda en memoria
		synchronized (sessions) {
			sessions.put(session.getTransactionId(), session);
		}

		// Actualizamos, si procede, la informacion de la sesion de la memoria compartida
		if (dao != null) {
			dao.saveSession(session, creation);
		}

		// Si hemos llegado al limite establecido de peticiones entre las cuales limpiar,
		// ejecutamos la limpieza
		synchronized (sessions) {
			if (++uses > MAX_USE_TO_CLEANING && (cleaningProcess == null || cleaningProcess.isDone())) {
				deleteExpiredSessions(trAux);
				uses = 0;
			}
		}
	}

	/**
	 * Obtiene el gestor de ficheros temporales asociado al gestor de sesiones compartidas
	 * establecido.
	 * @return Gestor de ficheros temporales o {@code null} si no hay gestor de sesiones
	 * o si este no tiene ninguno asociado.
	 */
	public static TempDocumentsDAO getAssociatedDocumentsDAO() {
		return dao != null ? dao.getAssociatedDocumentsDAO() : null;
	}


    /**
     * Recorre el listado de sesiones registradas y elimina las que han sobrepasado
     * el periodo de validez.
	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
     */
	// TODO: Esta funcion de borrado no registra en las estadisticas/auditoria
	// el error en las sesiones que se encuentren caducadas. Habria que plantear
	// si merece la pena hacerlo, ya que requeriria cargar en memoria la sesion
	// desde disco/base de datos solo para registrarla
	private static void deleteExpiredSessions(final TransactionAuxParams trAux) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		String[] sessionsIds;
		synchronized (sessions) {
			sessionsIds = sessions.keySet().toArray(new String[sessions.size()]);
		}

		try {
			cleaningProcess = executorService.submit(new ExpiredSessionCleanerThread(
					sessionsIds,
					sessions,
					dao,
					ConfigManager.getTempsTimeout(),
					trAux));
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING,
					trAux.getLogFormatter().f("Error en la ejecucion del proceso de borrado de seiones expiradas"), //$NON-NLS-1$
					e);
		}
		finally {
			// Al destruir el servicio liberamos el pool de hilos
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
					executorService.shutdownNow();
				}
			} catch (final InterruptedException e) {
				executorService.shutdownNow();
			}
			executorService = null;
		}
	}

	public static void release() {
		if (cleaningProcess != null) {
			cleaningProcess.cancel(true);
			cleaningProcess = null;
		}
		synchronized (sessions) {
			sessions.clear();
		}
		sessions = null;

	    random = null;
	}

    /**
     * Hilo para la eliminaci&oacute;n de sesiones y datos caducados.
     */
    private static final class ExpiredSessionCleanerThread extends Thread {

    	private static Logger THREAD_LOGGER = Logger.getLogger(ExpiredSessionCleanerThread.class.getName());

    	private final String[] ids;
    	private final Map<String, FireSession> sessionsMap;
    	private final SessionsDAO sessionsDao;
    	private final long timeout;
    	private final TransactionAuxParams trAux;

    	/**
    	 * Construye el objeto para la eliminaci&oacute;n de sesiones caducadas.
    	 * @param ids Identificadores de las sesiones que se tienen que evaluar.
    	 * @param sessions Mapa con todas las sesiones.
    	 * @param dao Objeto para la carga de sesiones.
    	 * @param tempTimeout Tiempo de caducidad en milisegundos de los ficheros temporales.
    	 * @param trAux Informaci&oacute;n auxiliar de la transacci&oacute;n.
    	 */
    	public ExpiredSessionCleanerThread(final String[] ids,
    			final Map<String, FireSession> sessions,
    			final SessionsDAO dao,
    			final long tempTimeout,
        		final TransactionAuxParams trAux) {
    		this.ids = ids;
    		this.sessionsMap = sessions;
    		this.sessionsDao = dao;
    		this.timeout = tempTimeout;
    		this.trAux = trAux;
    	}

    	@Override
    	public void run() {

    		FireSession session;
    		final long currentTime = new Date().getTime();

    		// Eliminamos la sesiones caducadas y sus datos asociados
        	for (final String id : this.ids) {
        		session = this.sessionsMap.get(id);
        		if (session != null && currentTime > session.getExpirationTime()) {
        			// Borramos la sesion
        			SessionCollector.removeSession(session, this.trAux);
        		}
        	}

        	// Eliminamos las sesiones caducadas en almacenamiento compartido si se definio

        	// TODO: Este hilo no registra en las estadisticas y la auditoria los
        	// de las transacciones que no estuviesen en memoria pero se encuentren
        	// en el almacen compartido. Habria que valorar si merece la pena, ya que
        	// requeriria cargarlas solo para marcarlas como erroneas
        	if (this.sessionsDao != null) {
        		try {
        			this.sessionsDao.deleteExpiredSessions(this.timeout);
        		}
        		catch (final Exception e) {
        			THREAD_LOGGER.warning("Error al eliminar las sesiones caducadas: " + e); //$NON-NLS-1$
    			}
        	}
    	}
    }
}
