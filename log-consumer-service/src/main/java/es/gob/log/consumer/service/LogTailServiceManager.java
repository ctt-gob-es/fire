package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogTail;

/**
 *
 * @author Adolfo.Navarro
 *
 */
public class LogTailServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());
	private static Long position = new Long(0L);
	/**
	 *
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public final static byte[] process(final HttpServletRequest req)  {

		byte[] result = null;
		/* Obtenemos los par&aacute;metros*/
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final HttpSession session = req.getSession(true);

		/*Comprobanmos el valor del par&aacute;metro LOG_FILE_NAME */
		if(logFileName != null && !"".equals(logFileName)) { //$NON-NLS-1$

			/*Obtenemos la informaci&oacute;n del fichero de configuraci&oacute;n de logs*/
			final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
			try {
				/* Obtenemos la ruta completa al fichero log*/
				final String path = ConfigManager.getInstance().getLogsDir().getCanonicalPath().toString().concat(File.separator).concat(logFileName);
				if(info != null) {
					final LogTail lTail = new LogTail(info,path);
					final int iNumLines = Integer.parseInt(sNumLines.trim());
					result = lTail.getLogTail(iNumLines);
					setPosition(lTail.getFilePosition());
				}

			}
			catch (final NumberFormatException e) {
				LOGGER.log(Level.SEVERE,"No el parametro nlines no es un numero entero",e); //$NON-NLS-1$
			} catch (final IOException e) {

				LOGGER.log(Level.SEVERE,"No se ha podido cargar el fichero de log ",e); //$NON-NLS-1$
			}

		}
		return result;
	}

	protected final static Long getPosition() {
		return LogTailServiceManager.position;
	}

	private final static void setPosition(final long position) {
		LogTailServiceManager.position = new Long (position);
	}



}
