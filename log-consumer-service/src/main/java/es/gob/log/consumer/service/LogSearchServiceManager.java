package es.gob.log.consumer.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogErrors;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogSearchText;

public class LogSearchServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());
	private static LogErrors error = null;

	public final static byte[] process(final HttpServletRequest req) {

		byte[] result = null;
		/* Obtenemos los par&aacute;metros*/
		//final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final String text = req.getParameter(ServiceParams.SEARCH_TEXT);
		Long sdateTime = null;
		if(req.getParameter(ServiceParams.SEARCH_DATETIME) != null && !"".equals(req.getParameter(ServiceParams.SEARCH_DATETIME))) { //$NON-NLS-1$
			sdateTime = new Long( Long.parseLong(req.getParameter(ServiceParams.SEARCH_DATETIME)));
		}
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$

		try {
			if(filePosition != null && filePosition != Long.valueOf(0L)) {

				reader.load(filePosition.longValue());
			}
			final LogSearchText logSearch = new LogSearchText(info, reader);
			if(sdateTime == null || sdateTime.longValue() < 0L) {
				result = logSearch.searchText(Integer.parseInt(sNumLines) , text);
			}
			else {
				result = logSearch.searchText(Integer.parseInt(sNumLines) , text, sdateTime.longValue());
			}

			if(logSearch.getError() !=null && logSearch.getError().getMsgError() != null) {
					if(logSearch.getFilePosition() > 0L) {
						session.setAttribute("FilePosition",Long.valueOf(logSearch.getFilePosition())); //$NON-NLS-1$
//						reader.load(logSearch.getFilePosition());
//						session.setAttribute("Reader", reader); //$NON-NLS-1$
					}

					error = logSearch.getError();
					logSearch.getError().setMsgError(null);

				}
			else {
				session.setAttribute("FilePosition", Long.valueOf(0L)); //$NON-NLS-1$
				session.setAttribute("Reader", reader); //$NON-NLS-1$
			}


		} catch (final InvalidPatternException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero",e); //$NON-NLS-1$
			error = new LogErrors("No se ha podido leer el fichero",HttpServletResponse.SC_NOT_ACCEPTABLE); //$NON-NLS-1$
			result = error.getMsgError().getBytes(info.getCharset());
			return result;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero",e); //$NON-NLS-1$
			error = new LogErrors("No se ha podido leer el fichero",HttpServletResponse.SC_NOT_ACCEPTABLE); //$NON-NLS-1$
			result = error.getMsgError().getBytes(info.getCharset());
			return result;
		}

		if(error != null && error.getMsgError() != null && !"".equals(error.getMsgError()) ) { //$NON-NLS-1$
			result = error.getMsgError().getBytes(info.getCharset());
			error = null;
		}

		return result;
	}


	public static final LogErrors getError() {
		return error;
	}

}
