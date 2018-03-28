package es.gob.log.consumer;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Clase para la lectura de entradas de logs. Permite obtener, en base a un patr&oacute;n,
 * diversos valores del log, como la hora o el nivel de log.
 */
class LogRegistryReader {

	private static final Logger LOGGER = Logger.getLogger(LogRegistryReader.class.getName());

	private final LogRegistryParser registryParser;

	private LogReader logReader = null;

	/**
	 * Construye el parse de entradas de registro.
	 * @param logInfo Informaci&oacute;n del fichero de log.
	 */
	public LogRegistryReader(final LogInfo logInfo) {
		this.registryParser = new LogRegistryParser(logInfo);
	}

	/**
	 * Carga el lector de logs para poder
	 * @param reader Lector de logs.
	 * @throws IOException Cuando no se puede leer del log.
	 */
	public void loadReader(final LogReader reader) throws IOException {

		this.logReader = reader;

		// Leemos una linea del fichero para dejarla cargada
		try {
			this.logReader.readLine();
		} catch (final IOException e) {
			throw new IOException("Error en la carga del lector de logs", e); //$NON-NLS-1$
		}
	}

	/**
	 * Lee un registro de un log.
	 * @return Registro de log.
	 * @throws IOException Cuando ocurre un error durante la lectura del registro.
	 */
	public LogRegistry readRegistry() throws IOException {

		// Si hay contenido en el log, comenzamos a procesarlo
		LogRegistry registry = null;
		if (this.logReader.getCurrentLine() != null) {
			try {
				// Procesamos la linea y se deja cargada la siguiente
				registry = this.registryParser.parse(this.logReader);
			}
			catch (final InvalidRegistryFormatException e) {
				LOGGER.warning("Error al procesar una entrada del fichero de log: " + e); //$NON-NLS-1$
				registry = e.getRegistry();
			}
		}
		return registry;
	}
}
