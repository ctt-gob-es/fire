package es.gob.log.consumer.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogMore;
import es.gob.log.consumer.LogReader;

public class LogMoreServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogMoreServiceManager.class.getName());
	private static Long position = new Long(0L);

	public final static byte[] process(final HttpServletRequest req) {
		byte[] result = null;
		/* Obtenemos los par&aacute;metros*/
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$

		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$
		if(filePosition != null) {
			setPosition(filePosition.longValue());
		}


		if(logFileName != null && !"".equals(logFileName)) { //$NON-NLS-1$

				try  {
					final int iNumLines = Integer.parseInt(sNumLines.trim());
					if(getPosition().longValue() != 0L ) {
						reader.load(getPosition().longValue());
					}
					final LogMore logMore = new LogMore(info);
					result = logMore.getLogMore(iNumLines,reader);
				}
				catch (final IOException | InvalidPatternException e) {
					e.printStackTrace();
				}
				catch (final NumberFormatException e) {
					LOGGER.log(Level.SEVERE,"No el parametro nlines no es un numero entero",e); //$NON-NLS-1$
				}

		}
		return result;
	}



	protected final static Long getPosition() {
		return LogMoreServiceManager.position;
	}

	private final static void setPosition(final long position) {
		LogMoreServiceManager.position = new Long (position);
	}

}
