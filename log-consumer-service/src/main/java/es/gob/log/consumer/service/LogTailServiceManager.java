package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogTail;

/**
 *
 * @author Adolfo.Navarro
 *
 */
public class LogTailServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());

	/**
	 *
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public final static byte[] process(final HttpServletRequest req) throws IOException  {

		// Obtenemos la sesion
		final HttpSession session = req.getSession(false);

		// Obtenemos la informacion del fichero de configuracion de logs
		final LogInfo info = (LogInfo) session.getAttribute("LogInfo"); //$NON-NLS-1$
		if (info == null) {
			LOGGER.log(Level.WARNING, "Es necesario abrir el fichero log anteriormente"); //$NON-NLS-1$z
			throw new IllegalArgumentException("Es necesario abrir el fichero log anteriormente"); //$NON-NLS-1$
		}

		// Obtenemos los parametros
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);

		// Comprobamos el valor de los parametros
		if (logFileName == null || logFileName.isEmpty()) {
			LOGGER.log(Level.SEVERE, "No se ha proporcionado el nombre de fichero"); //$NON-NLS-1$
			throw new IllegalArgumentException("No se ha proporcionado el nombre de fichero"); //$NON-NLS-1$
		}
		int iNumLines;
		try {
			iNumLines = Integer.parseInt(sNumLines.trim());
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se ha proporcionado un numero de lineas valido", e);  //$NON-NLS-1$
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
			session.setAttribute("FilePosition", Long.valueOf(lTail.getFilePosition()));//$NON-NLS-1$

			final AsynchronousFileChannel channelSession = (AsynchronousFileChannel) session.getAttribute("Channel"); //$NON-NLS-1$
			session.setAttribute("FileSize", new Long (channelSession.size())); //$NON-NLS-1$

			final LogReader reader = (LogReader) session.getAttribute("Reader"); //$NON-NLS-1$
			reader.setEndFile(true);
			session.setAttribute("Reader", reader);//$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se ha podido cargar el fichero de log", e); //$NON-NLS-1$
			throw new IOException("No se ha podido cargar el fichero de log", e); //$NON-NLS-1$
		}

		return result;
	}
}
