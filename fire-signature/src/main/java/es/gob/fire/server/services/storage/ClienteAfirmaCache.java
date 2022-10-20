package es.gob.fire.server.services.storage;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import es.gob.fire.signature.ConfigManager;

/**
 * Cach&eacute; para el guardado en memoria de los datos intercambiados entre FIRe y el Cliente @firma.
 * @author carlos.gamuci
 */
public class ClienteAfirmaCache {

	/** Mapa en el que se almacenar&aacute;n los datos. */
	private static Map<String, CachedResponse> cache = new HashMap<>();

	/**
	 * Tiempo a partir del cual se considerar&aacute;n caducados los datos almacenados en
	 * cach&eacute;.
	 */
	private static final long EXPIRATION_PERIOD = ConfigManager.getTempsTimeout();

	/**
	 * N&uacute;mero de guardados en cach&eacute; que se realizan entre cada una de
	 * las ejecuciones de la operaci&oacute;n de limpieza de cach&eacute;.
	 */
	private static final int DEFAULT_USE_COUNTER = 3000;

	/**
	 * N&ueacute;mero de guardados pendientes antes de la ejecuci&oacute;n del hilo de limpieza.
	 */
	private static int useCounter = DEFAULT_USE_COUNTER;

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

		// Reducimos el contador de usos del cliente. Al llegar a 0,
		// se lanza el hilo de limpieza
		synchronized (cache) {
			if (--useCounter <= 0) {
				useCounter = DEFAULT_USE_COUNTER;
				new CacheCleanerThread(cache).start();
			}
		}
	}

	/**
	 * Datos en cach&eacute;.
	 */
	private static class CachedResponse {
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
			super.run();

			final Date currentDate = new Date();

			final Iterator<String> idsIt = this.cacheToClean.keySet().iterator();
			while (idsIt.hasNext()) {
				final String id = idsIt.next();
				final CachedResponse response = this.cacheToClean.get(id);
				if (response != null && response.isExpired(currentDate)) {
					this.cacheToClean.remove(id);
				}
			}
		}
	}
}

