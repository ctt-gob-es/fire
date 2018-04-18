package es.gob.log.consumer.service;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public class LogSearchServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());
	private static Long position = new Long(0L);

	public final static byte[] process(final HttpServletRequest req) {

		final byte[] result = null;
		/* Obtenemos los par&aacute;metros*/
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final String text = req.getParameter(ServiceParams.SEARCH_TEXT);
		Long sdateTime = null;
		if(req.getParameter(ServiceParams.SEARCH_DATETIME)!=null && !"".equals(req.getParameter(ServiceParams.SEARCH_DATETIME))) { //$NON-NLS-1$
			sdateTime = new Long( Long.parseLong(req.getParameter(ServiceParams.SEARCH_DATETIME)));
		}


		//final LogSearchText logSearch = new LogSearchText(logInfo, path)
		if(sdateTime != null) {

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
