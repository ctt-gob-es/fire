package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogOpen;
import es.gob.log.consumer.LogReader;

public class LogOpenServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogOpenServiceManager.class.getName());
	private static  LogInfo linfo;
	private static LogReader reader;
	private static AsynchronousFileChannel channel;

	public final static byte [] process(final HttpServletRequest req)  {
		byte [] result = null;
		/* Obtenemos los par&aacute;metros*/
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		if(logFileName != null && !"".equals(logFileName)) { //$NON-NLS-1$
			/* Obtenemos la ruta completa al fichero log*/
			final String path = ConfigManager.getInstance().getLogsDir().toString().concat(logFileName);
			final LogOpen logOpen = new LogOpen(path);
			try {
				result = logOpen.openFile(logFileName);
				if(result != null) {
					setLinfo(logOpen.getLinfo());
					setChannel(logOpen.getChannel());
					setReader(logOpen.getReader());
				}
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result;
	}

	public final static  LogInfo getLinfo() {
		return LogOpenServiceManager.linfo;
	}

	public final static void setLinfo(final LogInfo linfo) {
		LogOpenServiceManager.linfo = linfo;
	}

	public final static LogReader getReader() {
		return LogOpenServiceManager.reader;
	}

	public final static void setReader(final LogReader reader) {
		LogOpenServiceManager.reader = reader;
	}

	public final static AsynchronousFileChannel getChannel() {
		return LogOpenServiceManager.channel;
	}

	public final static void setChannel(final AsynchronousFileChannel channel) {
		LogOpenServiceManager.channel = channel;
	}



}
