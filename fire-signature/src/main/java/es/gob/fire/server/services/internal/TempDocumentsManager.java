package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.services.internal.sessions.FileSystemTempDocumentsDAO;
import es.gob.fire.server.services.internal.sessions.TempDocumentsDAO;
import es.gob.fire.signature.ConfigManager;

public class TempDocumentsManager {

	private static final Logger LOGGER = Logger.getLogger(TempDocumentsManager.class.getName());

	private static final TempDocumentsDAO documentsDao;

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
				LOGGER.severe("Error al cargar del gestor para la comparticion de sesiones entre nodos: " + e); //$NON-NLS-1$
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

		LOGGER.info("Gestor de documentos temporales: " + documentsDao.getClass().getName()); //$NON-NLS-1$

		deleteExpiredDocuments();
	}

	public static boolean existDocument(final String id) throws IOException {
		return documentsDao.existDocument(id);
	}

	public static void storeDocument(final String id, final byte[] data, final boolean newDocument) throws IOException {
		documentsDao.storeDocument(id, data, newDocument);

		synchronized (documentsDao) {
			if (++uses >= MAX_USE_TO_CLEANING) {
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
			new ExpiredDocumentsCleanerThread(documentsDao, ConfigManager.getTempsTimeout()).start();
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING,
					"Error al solicitar la eliminacion de los documentos temporales caducados", //$NON-NLS-1$
					e);
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
