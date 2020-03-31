package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogTail;

/**
 * Manejador encargado de obtener las &uacute;ltimas l&iacute;neas de un fichero de log.
 */
public class LogTailServiceManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogTailServiceManager.class);

	/**
	 * Procesa una petici&oacute;n de obtenci&oacute;n de las &uacute;ltimas l&iacute;neas de un log.
	 * @param req Petici&oacute;n HTTP de obtenci&oacute;n de l&iacute;neas.
	 * @return Contenido del log resultado de la obtenci&oacute;n.
	 * @throws IOException Cuando se produce un error durante la lectura o proceso del log.
	 */
	public final static byte[] process(final HttpServletRequest req) throws IOException  {

		// Obtenemos la sesion
		final HttpSession session = req.getSession(false);

		// Obtenemos la informacion del fichero de configuracion de logs
		final LogInfo info = (LogInfo) session.getAttribute(SessionParams.LOG_INFO);
		if (info == null) {
			LOGGER.warn("Es necesario abrir el fichero log anteriormente"); //$NON-NLS-1$z
			throw new IllegalArgumentException("Es necesario abrir el fichero log anteriormente"); //$NON-NLS-1$
		}

		// Obtenemos los parametros
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);

		// Comprobamos el valor de los parametros
		if (logFileName == null || logFileName.isEmpty()) {
			LOGGER.error("No se ha proporcionado el nombre de fichero"); //$NON-NLS-1$
			throw new IllegalArgumentException("No se ha proporcionado el nombre de fichero"); //$NON-NLS-1$
		}
		int iNumLines;
		try {
			iNumLines = Integer.parseInt(sNumLines.trim());
		}
		catch (final Exception e) {
			LOGGER.error("No se ha proporcionado un numero de lineas valido", e);  //$NON-NLS-1$
			throw new IllegalArgumentException("No se ha proporcionado un numero de lineas valido", e); //$NON-NLS-1$
		}

		byte[] result = null;
		try {
			// Obtenemos la ruta completa al fichero log
			final String path = new File(ConfigManager.getInstance().getLogsDir(), logFileName).getCanonicalPath();
			if (!path.startsWith(ConfigManager.getInstance().getLogsDir().getCanonicalPath())){
				throw new SecurityException("Se ha intentado acceder a una ruta fuera del directorio de logs: " + path); //$NON-NLS-1$
	        }


			final LogTail lTail = new LogTail(info, path);
			final StringBuilder resTail = lTail.getLogTail(iNumLines);
			result = resTail.toString().getBytes(info.getCharset());
			session.setAttribute(SessionParams.FILE_POSITION, Long.valueOf(lTail.getFilePosition()));

			final AsynchronousFileChannel channelSession =
					(AsynchronousFileChannel) session.getAttribute(SessionParams.FILE_CHANNEL);
			session.setAttribute(SessionParams.FILE_SIZE, new Long (channelSession.size()));

			final LogReader reader = (LogReader) session.getAttribute(SessionParams.FILE_READER);
			reader.setEndFile(true);
			session.setAttribute(SessionParams.FILE_READER, reader);
		}
		catch (final Exception e) {
			LOGGER.error("No se ha podido cargar el fichero de log", e); //$NON-NLS-1$
			throw new IOException("No se ha podido cargar el fichero de log", e); //$NON-NLS-1$
		}

		return result;
	}
}
