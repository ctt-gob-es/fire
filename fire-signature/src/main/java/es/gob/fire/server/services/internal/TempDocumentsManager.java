package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.server.services.internal.sessions.FileSystemTempDocumentsDAO;
import es.gob.fire.server.services.internal.sessions.TempDocumentsDAO;
import es.gob.fire.signature.ConfigManager;

public class TempDocumentsManager {

	private static final Logger LOGGER = Logger.getLogger(TempDocumentsManager.class.getName());

	private static final TempDocumentsDAO documentsDao;

	/**
	 * Ejecutor del ser servicio de limpieza de la cache.
	 */
	private static ExecutorService executorService = Executors.newSingleThreadExecutor();

	/**
	 * &Uacute;timo proceso de limpieza que se ejecut&oacute;.
	 */
	private static Future<?> cleaningProcess = null;

	/** N&uacute;mero de veces que se puden guardar datos antes ejecutar el proceso para
	 * eliminar aquellos que estan caducados. */
	private static final int MAX_USE_TO_CLEANING = 300;

	private static int uses = 0;

	static {
		TempDocumentsDAO associatedDao;
		final String documentsDaoClassname = ConfigManager.getTempDocumentsDao();
		if (documentsDaoClassname != null && !documentsDaoClassname.trim().isEmpty()) {
			try {
				associatedDao = (TempDocumentsDAO) Class.forName(documentsDaoClassname.trim()).getConstructor().newInstance();
			} catch (final Exception e) {
				LOGGER.severe("Error al cargar del gestor para la comparticion de temporales entre nodos: " + e); //$NON-NLS-1$
				AlarmsManager.notify(Alarm.LIBRARY_NOT_FOUND, documentsDaoClassname);
				associatedDao = null;
			}
		}
		else {
			associatedDao = SessionCollector.getAssociatedDocumentsDAO();
		}

		// Si no se puede obtener el DAO por otro medio, se usara el que guarda los documentos
		// en el sistema de ficheros
		documentsDao = associatedDao != null ?
			associatedDao :
			new FileSystemTempDocumentsDAO();

		LOGGER.info("Inicializacion del gestor de documentos temporales: " + documentsDao.getClass().getName()); //$NON-NLS-1$

		deleteExpiredDocuments();
	}

	public static boolean existDocument(final String id) throws IOException {
		return documentsDao.existDocument(id);
	}

	public static void storeDocument(final String id, final byte[] data, final boolean newDocument, final TransactionAuxParams trAux) throws IOException {

		//XXX: El segundo parametro LogTransactionFormatter deberia imprimir un identificador concreto no asociado a una transaccion
		// sino a una llamada independiente
		final LogTransactionFormatter logFormatter = trAux != null ? trAux.getLogFormatter() : new LogTransactionFormatter(id);

		documentsDao.storeDocument(id, data, newDocument, logFormatter);

		synchronized (documentsDao) {
			if (++uses >= MAX_USE_TO_CLEANING && (cleaningProcess == null || cleaningProcess.isDone())) {
				deleteExpiredDocuments();
				uses = 0;
			}
		}
	}

	public static byte[] retrieveDocument(final String id) throws IOException {
		uses++;
		return documentsDao.retrieveDocument(id);

	}

	public static void deleteDocument(final String id) throws IOException {
		documentsDao.deleteDocument(id);
	}

    /**
     * Lee el contenido de un documento guardado y despu&eacute;s lo elimina.
     * @param id Identificador del documento.
     * @return Contenido del documento.
     * @throws IOException Cuando no se encuentra el documento o no puede leerse.
     */
	public static byte[] retrieveAndDeleteDocument(final String id) throws IOException {
		return documentsDao.retrieveAndDeleteDocument(id);
	}

	/**
	 * Lanza un hilo para la eliminaci&oacute;n de los documentos temporales caducados.
	 */
	private static void deleteExpiredDocuments() {
		try {
			cleaningProcess = executorService.submit(new ExpiredDocumentsCleanerThread(documentsDao, ConfigManager.getTempsTimeout()));
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING,
					"Error al solicitar la eliminacion de los documentos temporales caducados", //$NON-NLS-1$
					e);
		}
	}

	/**
	 * Libera los recursos necesarios para la ejecuci&oacute;n del gestor.
	 */
	public static void release() {
		// Al destruir el servicio liberamos el pool de hilos
		if (executorService != null) {
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
					executorService.shutdownNow();
				}
			} catch (final InterruptedException e) {
				executorService.shutdownNow();
			}
		}
	}

	/**
	 * Hilo para la eliminaci&oacute;n de documentos temporales caducados.
	 */
	private static final class ExpiredDocumentsCleanerThread extends Thread {

		private static final Logger THREAD_LOGGER = Logger.getLogger(ExpiredDocumentsCleanerThread.class.getName());

		private final TempDocumentsDAO dao;

		private final long timeout;

		/**
		 * Construye el hilo para la eliminaci&oacute;n de documentos temporales caducados.
		 * @param dao Gestor de documentos temporales.
		 * @param tempTimeout Tiempo de caducidad en milisegundos de los ficheros temporales.
		 */
		public ExpiredDocumentsCleanerThread(final TempDocumentsDAO dao, final long tempTimeout) {
			this.dao = dao;
			this.timeout = tempTimeout;
		}

		@Override
		public void run() {
	    	try {
				this.dao.deleteExpiredDocuments(this.timeout);
			} catch (final IOException e) {
				THREAD_LOGGER.warning("Error durante la limpieza de documentos caducados: " + e); //$NON-NLS-1$
			}
		}
	}
}
