package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogDownload;


public class LogDownloadServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogDownloadServiceManager.class.getName());
	private static boolean hasMore = false;



	public final static byte[] process(final HttpServletRequest req, final HttpServletResponse resp, final String pathLogs) throws IOException {
		byte[] result = null;
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final File dataFile = new File(pathLogs.concat(File.separator).concat(logFileName));

		final HttpSession session = req.getSession(false);
		SeekableByteChannel channel = null;
		LogDownload download = null;
		try{

		Long fileDownloadPosition = new Long(0L);
		if((Long) session.getAttribute("FileDownloadPos") != null ) { //$NON-NLS-1$
			fileDownloadPosition = (Long)session.getAttribute("FileDownloadPos"); //$NON-NLS-1$
		}
		if((SeekableByteChannel)session.getAttribute("ChannelDownload") != null) { //$NON-NLS-1$
			channel = (SeekableByteChannel)session.getAttribute("ChannelDownload"); //$NON-NLS-1$
		}
		else {
			channel = FileChannel.open(dataFile.toPath() , StandardOpenOption.READ);
			session.setAttribute("ChannelDownload", channel); //$NON-NLS-1$
		}
		if( (LogDownload)session.getAttribute("Download") != null){ //$NON-NLS-1$
			download = (LogDownload)session.getAttribute("Download");//$NON-NLS-1$
		}
		else {
			download =  new LogDownload(logFileName, channel);
			session.setAttribute("Download", download); //$NON-NLS-1$
		}


			channel.position(fileDownloadPosition.longValue());
			result = download.download();
			setHasMore(download.hasMore());
			if(download.getPosition() > 0L) {
				session.setAttribute("FileDownloadPos", new Long(download.getPosition())); //$NON-NLS-1$
			}

		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Error al procesar la operación de bajada del fichero",e.getMessage()); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error al procesar la operación de bajada del fichero"); //$NON-NLS-1$
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
