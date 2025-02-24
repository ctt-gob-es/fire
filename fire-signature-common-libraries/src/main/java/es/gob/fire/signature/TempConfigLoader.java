package es.gob.fire.signature;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * Cargador de configuraci&oacute; que se asegura de recargarla
 * cada cierto tiempo.
 */
public abstract class TempConfigLoader extends ConfigLoader {

	private static final Logger LOGGER = Logger.getLogger(TempConfigLoader.class.getName());

	/**
	 * Indicador de si la configuracion esta cargada o no.
	 */
	private boolean loaded = false;

	/**
	 *  Tiempo de caducidad de la configuraci&oacute;n. Por defecto,
	 *  se serr&acute;n 5 minutos.
	 */
	private long timeout = 5 * 60 * 1000;	// 5 minutos

	@Override
	protected final synchronized void loadConfig() throws IOException, ConfigException {

		if (!this.loaded) {

			// Cargamos la configuracion
			Hashtable<Object, Object> newConfig;
			try {
				newConfig = loadConfiguration();
			}
			catch (final IOException e) {
				LOGGER.warning("Error al cargar la configuracion. Se mantendra la configuracion actual: " + e); //$NON-NLS-1$
				throw e;
			}
			catch (final ConfigException e) {
				LOGGER.warning("Se han encontrado errores en la configuracion. Se mantendra la configuracion actual: " + e); //$NON-NLS-1$
				throw e;
			}

			// Establecemos la nueva configuracion
			setConfig(newConfig);
			// Lanzamos el hilo para limitar la duracion de la configuracion
			new TimeoutThread(this, this.timeout).start();
			// Marcamos que la configuracion esta cargada
			this.loaded = true;
		}
	}

	/**
	 * Carga la configuraci&acute;n de duraci&oacute;n temporal.
	 * @throws IOException Cuando ocurre un error durante la carga de la configuraci&oacute;n.
	 * @
	 */
	public abstract Hashtable<Object, Object> loadConfiguration() throws IOException, ConfigException;

	/**
	 * Establece el tiempo que durar&aacute; vigente esta configuraci&oacute;n.
	 * @param millis N&uacute;mero de segundos
	 */
	protected void setTimeout(final long millis) {

		this.timeout = millis;
	}

	/**
	 * Elimina la configuraci&oacute;n actual forzando que la pr&oacute;xima
	 * vez quese solicite una propiedad se recargue.
	 */
	public synchronized final void reset() {
		this.loaded = false;
	}

	/**
	 * Hilo para el reinicio peri&oacute;dico de la configuraci&oacute;n.
	 */
	private static class TimeoutThread extends Thread {

		private static final Logger THREAD_LOGGER = Logger.getLogger(TimeoutThread.class.getName());

		private final TempConfigLoader loader;
		private final long millis;

		public TimeoutThread(final TempConfigLoader loader, final long millis) {
			this.loader = loader;
			this.millis = millis;
		}

		@Override
		public void run() {

			// Esperamos el tiempo designado
			try {
				Thread.sleep(this.millis);
			} catch (final InterruptedException e) {
				THREAD_LOGGER.warning("No se espera el tiempo de reinicio de configuracion"); //$NON-NLS-1$
			}

			// Reseteamos la configuracion
			if (this.loader != null) {
				this.loader.reset();
			}
		}
	}
}
