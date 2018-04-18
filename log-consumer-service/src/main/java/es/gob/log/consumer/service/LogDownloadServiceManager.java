package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogDownload;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;

public class LogDownloadServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogDownloadServiceManager.class.getName());

	public final static byte[] process(final HttpServletRequest req) {
		final byte[] result = null;
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final LogDownload download =  new LogDownload(logFileName, info.getCharset());
		final AsynchronousFileChannel chanel = (AsynchronousFileChannel)session.getAttribute("Channel"); //$NON-NLS-1$
		try {
			reader.load();

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//download.download(chanel);

		return result;
	}

}
