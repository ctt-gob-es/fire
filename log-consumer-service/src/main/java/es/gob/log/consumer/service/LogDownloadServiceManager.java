package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogConstants;
import es.gob.log.consumer.LogDownload;


public class LogDownloadServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogDownloadServiceManager.class.getName());
	private static boolean hasMore = false;

	public final static byte[] process(final HttpServletRequest req) {
		byte[] result = null;
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final HttpSession session = req.getSession(true);
		//final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final File dataFile = new File(LogConstants.DIR_FILE_LOG.concat("\\").concat(logFileName)); //$NON-NLS-1$

		try(final SeekableByteChannel channel = FileChannel.open(dataFile.toPath() , StandardOpenOption.READ);) {
			final LogDownload download =  new LogDownload(logFileName,channel);
			result = download.download();
			setHasMore(download.hasMore());
			channel.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public static final boolean isHasMore() {
		return hasMore;
	}

	private static final void setHasMore(final boolean hasMore) {
		LogDownloadServiceManager.hasMore = hasMore;
	}


}
