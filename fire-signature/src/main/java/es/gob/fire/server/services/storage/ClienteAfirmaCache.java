package es.gob.fire.server.services.storage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import es.gob.fire.signature.ConfigManager;

/**
 * Cach&eacute; para el guardado en memoria de los datos intercambiados entre FIRe y el Cliente @firma.
 * @author carlos.gamuci
 */
public class ClienteAfirmaCache {

	/** Mapa en el que se almacenar&aacute;n los datos. */
	private static Map<String, CachedResponse> cache = new HashMap<>();

	/**
	 * Ejecutor del ser servicio de limpieza de la cache.
	 */
	private static ExecutorService executorService = null;

	/**
	 * &Uacute;timo proceso de limpieza que se ejecut&oacute;.
	 */
	private static Future<?> cleaningProcess = null;

	/**
	 * N&uacute;mero de guardados en cach&eacute; que se realizan entre cada una de
	 * las ejecuciones de la operaci&oacute;n de limpieza de cach&eacute;. Cada ejecuci&oacute;n
	 * equivale a una transaccaci&oacute;n con certificado local.
	 */
	private static final int INITIAL_USE_COUNTER_VALUE = 500;

	/**
	 * N&ueacute;mero de guardados pendientes antes de la ejecuci&oacute;n del hilo de limpieza.
	 */
	private static int useCounter = INITIAL_USE_COUNTER_VALUE;

	/**
	 * Tiempo en milisegundos a partir del que es razonable ejecutar una nueva limpieza de la
	 * cach&eacute;.
	 */
	private static final int CLEANING_INTERVAL_MILLIS = 30 * 60 * 1000;  // 30 minutos

	/**
	 * Momento del tiempo en milisegundos a partir del que se podr&iacute;a realizar un nuevo
	 * proceso de limpieza.
	 */
	private static long newCleanupTargetMillis = System.currentTimeMillis();

	/**
	 * Recupera datos de la cach&eacute;.
	 * @param id Identificador del dato que se desea recuperar.
	 * @return El datos solicitado o {@code null} si no se encuentra disponible.
	 */
	public static byte[] recoverData(final String id) {

		final CachedResponse response = cache.get(id);
		if (response == null) {
			return null;
		}
		if (response.isExpired(new Date())) {
			cache.remove(id);
			return null;
		}
		return response.getData();
	}

	/**
	 * Almacena datos en cach&eacute;.
	 * @param id Identificador del dato que se desea almacenar.
	 * @param data Datos que se desean almacenar.
	 */
	public static void saveData(final String id, final byte[] data) {
		cache.put(id, new CachedResponse(data));

		// Contabilizamos el guardado en el contador de usos
		--useCounter;

		if (isCleanningNeeded()) {
			cleanCache();
		}
	}


	/**
	 * Identifica si es necesaria limpiar la cache. Ser&aacute; necesario limpiar si se han
	 * realizado m&aacute;s de un n&uacute;mero de operaciones determinado desde la &uacute;ltima
	 * limpieza o si se ha excedido un tiempo determinado desde la &uacute;ltima limpieza. No es
	 * necesario iniciar una limpieza si ya se esta ejecutando.
	 * @return {@code true} si es necesario limpiar, {@code false} en caso contrario.
	 */
	private static boolean isCleanningNeeded() {

		// Si se esta limpiando ahora mismo, no necesitamos limpieza
		if (cleaningProcess != null && !cleaningProcess.isDone()) {
			return false;
		}

		// Comprobamos si ya se han realizado los usos prefijado despues de la
		// ultima limpieza
		if (useCounter <= 0) {
			return true;
		}

		// Comprobamos si ha pasado un tiempo prefijado desde la ultima limpieza
		if (newCleanupTargetMillis <= System.currentTimeMillis()) {
			return true;
		}

		return false;
	}

	/**
	 * Inicia la tarea de limpieza de la cache
	 */
	private static void cleanCache() {
		useCounter = INITIAL_USE_COUNTER_VALUE;
		newCleanupTargetMillis = System.currentTimeMillis() + CLEANING_INTERVAL_MILLIS;
		if (executorService == null) {
			executorService = Executors.newSingleThreadExecutor();
		}
		cleaningProcess = executorService.submit(new CacheCleanerThread(cache));
	}

	/**
	 * Datos en cach&eacute;.
	 */
	private static class CachedResponse {

		/**
		 * Tiempo a partir del cual se considerar&aacute;n caducados los datos almacenados en
		 * cach&eacute;.
		 */
		private static final long EXPIRATION_PERIOD = ConfigManager.getTempsTimeout();

		private final Date expirationDate;
		private final byte[] data;

		/**
		 * Crea los datos en cach&eacute;.
		 * @param data Datos en cach&eacute;.
		 */
		public CachedResponse(final byte[] data) {
			this.expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_PERIOD);
			this.data = data;
		}

		/**
		 * Recupera los datos guardados en cach&eacute;.
		 * @return Datos guardados en cach&eacute;.
		 */
		public byte[] getData() {
			return this.data;
		}

		/**
		 * Comprueba si los datos est&aacute;n caducados.
		 * @param currentDate Fecha actual.
		 * @return {@code true} si los datos est&aacute;n caducados, {@code false} en caso
		 * contrario.
		 */
		public boolean isExpired(final Date currentDate) {
			return this.expirationDate.before(currentDate);

		}
	}

	/**
	 * Hilo para la limpieza peri&oacute;dica de la cach&eacute;.
	 */
	private static class CacheCleanerThread extends Thread {

		private final Map<String, CachedResponse> cacheToClean;

		public CacheCleanerThread(final Map<String, CachedResponse> cache) {
			this.cacheToClean = cache;
		}

		@Override
		public void run() {
			final Date currentDate = new Date();

			for (final String id : this.cacheToClean.keySet().toArray(new String[0])) {
				final CachedResponse response = this.cacheToClean.get(id);
				if (response != null && response.isExpired(currentDate)) {
					this.cacheToClean.remove(id);
				}
			}
		}
	}

	/**
	 * Libera los recursos de la cache.
	 */
	public static void release() {
		if (executorService != null) {
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
}

