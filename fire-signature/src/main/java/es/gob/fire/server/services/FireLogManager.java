package es.gob.fire.server.services;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import es.gob.fire.logs.handlers.PeriodicRotatingFileHandler;
import es.gob.fire.signature.ConfigManager;

/**
 * Gestor para la configuraci&oacute;n de los logs de FIRe.
 */
public class FireLogManager {

	private static final String LOG_FILENAME = "fire_signature.log"; //$NON-NLS-1$

	private static final String LOG_FIRE_NAME = "es.gob.fire"; //$NON-NLS-1$

	private static final String LOG_AFIRMA_NAME = "es.gob.afirma"; //$NON-NLS-1$

	/**
	 * Configura los logs del servicio.
	 * @throws IOException Cuando no es posible configurar la salida de los logs a un fichero externo.
	 */
	static void configureLogs() throws IOException {

		final File logsDir = ConfigManager.getLogsDir() != null ?
				new File (ConfigManager.getLogsDir()) :
				null;

		// Si se ha configurado un directorio de log, sacamos los logs a un fichero del mismo
		if (logsDir != null && logsDir.isDirectory() && logsDir.canWrite()) {

			// Nos aseguramos de que si ya se encontraba instalado el manejador de log en fichero, se desintale antes
			// de volverlo a instalar
			cleanLogHandlers();

			final PeriodicRotatingFileHandler handler = new PeriodicRotatingFileHandler(
				new File(logsDir, LOG_FILENAME).getAbsolutePath(),
				true
			);
			handler.setAutoFlush(true);
			handler.setFormatter(new SimpleFormatter());
			handler.setEnabled(true);
			handler.setLevel(Level.FINER);

			RollingPolicy policy;
			try {
				policy = RollingPolicy.valueOf(ConfigManager.getLogsRollingPolicy());
			}
			catch (final Exception e) {
				policy = RollingPolicy.DAY;
			}
			handler.setSuffix(policy.getSuffix());

			// Asignamos el logger a toda la jerarquia de logs
			Logger.getLogger("").addHandler(handler); //$NON-NLS-1$
		}

		// Establecemos el nivel para la jerarquia completa y para cada los paquetes especificos
		Logger.getLogger("").setLevel(parseLevel(ConfigManager.getLogsLevel())); //$NON-NLS-1$
		Logger.getLogger(LOG_FIRE_NAME).setLevel(parseLevel(ConfigManager.getLogsLevelFire()));
		Logger.getLogger(LOG_AFIRMA_NAME).setLevel(parseLevel(ConfigManager.getLogsLevelAfirma()));
	}

	/**
	 * Desinstala los manejadores de log en fichero que hubiese ya instalados.
	 */
	private static void cleanLogHandlers() {

		// Retiramos los logs a fichero si ya estaban instalados
		for (final Handler handler : Logger.getLogger("").getHandlers()) { //$NON-NLS-1$
			if (handler instanceof PeriodicRotatingFileHandler) {
				Logger.getLogger("").removeHandler(handler); //$NON-NLS-1$
			}
		}
	}


	private static Level parseLevel(final String levelName) {
		Level level;
		try {
			level = Level.parse(levelName);
		}
		catch (final Exception e) {
			level = Level.INFO;
		}
		return level;
	}

	/**
	 * Politica de rotado del fichero de logs.
	 */
	enum RollingPolicy {
		DAY(".yyyy-MM-dd"), //$NON-NLS-1$
		HOUR(".yyyy-MM-dd_hh"), //$NON-NLS-1$
		MINUTE(".yyyy-MM-dd_hh-mm"); //$NON-NLS-1$

		private final String suffix;

		private RollingPolicy(final String suffix) {
			this.suffix = suffix;
		}

		public String getSuffix() {
			return this.suffix;
		}
	}
}
