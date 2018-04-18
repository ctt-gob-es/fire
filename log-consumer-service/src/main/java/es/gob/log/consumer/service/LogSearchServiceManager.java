package es.gob.log.consumer.service;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogSearchText;

public class LogSearchServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());
	private static Long position = new Long(0L);

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
		if(filePosition != null) {
			setPosition(filePosition.longValue());
		}
		try {
			if(getPosition().longValue() != 0L ) {
				reader.load(getPosition().longValue());
			}
			final LogSearchText logSearch = new LogSearchText(info, reader);
			if(sdateTime == null) {
				result = logSearch.searchText(Integer.parseInt(sNumLines) , text);
			}
			else {
				result = logSearch.searchText(Integer.parseInt(sNumLines) , text, sdateTime.longValue());
			}

		} catch (final InvalidPatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return result;
	}


	protected final static Long getPosition() {
		return LogSearchServiceManager.position;
	}

	private final static void setPosition(final long position) {
		LogSearchServiceManager.position = new Long (position);
	}

}
