package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogOpen;
import es.gob.log.consumer.LogReader;

public class LogOpenServiceManager implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 3381831208944096820L;

	private static final Logger LOGGER = Logger.getLogger(LogOpenServiceManager.class.getName());

	/**
	 * Lanza el proceso de obtener los datos loginfo asociado al fichero log indicado por
	 * par&aacute;metro "fname" de HttpServletRequest (req)
	 * @param req Petici&oacute;n HTTP.

	 * @return  {@code null} Si el par&aacute;metro "fname" de HttpServletRequest (req) es nulo o vacio
	 * @throws IOException Cuando ocurre un error en la apertura.
	 * @throws IllegalArgumentException Cuando no se proporcionan los par&aacute;metros necesarios.
	 */
	public final static byte [] process(final HttpServletRequest req) throws IOException, IllegalArgumentException {

		final HttpSession session = req.getSession(false);

		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		if (logFileName == null || logFileName.isEmpty()) {



			LOGGER.log(Level.WARNING, "No se ha proporcionado el parametro con el nombre de fichero: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
			throw new IllegalArgumentException("No se ha proporcionado el parametro con el nombre de fichero: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$


		}

		final File logsDir = ConfigManager.getInstance().getLogsDir();
		final File logFile = new File(logsDir, logFileName);
		if (!logFile.getCanonicalPath().startsWith(logsDir.getCanonicalPath())){
			throw new SecurityException("Se ha intentado acceder a una ruta fuera del directorio de logs: " + logFile.getAbsolutePath()); //$NON-NLS-1$
        }

		// Comprobacion de seguridad de que no se pida un fichero fuera del directorio configurado
		try {
			if (!logFile.getCanonicalPath().startsWith(ConfigManager.getInstance().getLogsDir().getCanonicalPath())) {
				LOGGER.log(Level.SEVERE, "Intento cargarse un fichero de log de fuera del directorio configurado: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
				throw new IllegalArgumentException("Intento cargarse un fichero de log de fuera del directorio configurado: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
			}
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No pudo completarse la validacion de seguridad sobre la ruta del fichero: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
			throw new IOException("No pudo completarse la validacion de seguridad sobre la ruta del fichero: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
		}

		if (!logFile.isFile() || !logFile.canRead()) {
			LOGGER.log(Level.SEVERE, "El fichero de log indicado no se encontro o no pudo leerse: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
			throw new IOException("El fichero de log indicado no se encontro o no pudo leerse: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
		}

		byte[] logInfoJson;
		try {
			final LogOpen logOpen = new LogOpen(logFile);
			logInfoJson = logOpen.openFile();

			final LogInfo logInfo = logOpen.getLogInfo();
			session.setAttribute("LogInfo", logInfo); //$NON-NLS-1$

			final AsynchronousFileChannel logChannel = logOpen.getChannel();
			session.setAttribute("Channel", logChannel); //$NON-NLS-1$
			session.setAttribute("FileSize", new Long (logChannel.size())); //$NON-NLS-1$

			final LogReader logReader = logOpen.getReader();
			session.setAttribute("Reader", logReader);	 //$NON-NLS-1$
			session.setAttribute("FilePosition", new Long(0L)); //$NON-NLS-1$

		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Error al abrir el fichero de log " + logFile.getAbsolutePath(), e); //$NON-NLS-1$
			throw e;
		}

		return logInfoJson;
	}
}
