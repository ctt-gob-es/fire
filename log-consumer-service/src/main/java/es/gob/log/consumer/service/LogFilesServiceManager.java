package es.gob.log.consumer.service;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.log.consumer.LogFiles;

public class LogFilesServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogFilesServiceManager.class.getName());

	public final static byte[] process()  {
		byte[] result = null;
		final LogFiles logfile = new LogFiles();
		try {
			result = logfile.getLogFiles();
		} catch (final UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido obtener la lista de ficheros log.",e); //$NON-NLS-1$
		}
		return result;
	}

}
