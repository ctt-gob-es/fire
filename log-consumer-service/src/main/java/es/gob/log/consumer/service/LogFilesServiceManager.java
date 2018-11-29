package es.gob.log.consumer.service;


import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.log.consumer.LogFiles;

public class LogFilesServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogFilesServiceManager.class.getName());

	public final static byte[] process(final File pathLogs)  {
		LOGGER.log(Level.INFO, "Iniciado proceso de listar ficheros log"); //$NON-NLS-1$
		byte[] result = null;
		final LogFiles logfile = new LogFiles();
		result = logfile.getLogFiles(pathLogs);
		return result;
	}

}
