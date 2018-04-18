package es.gob.log.consumer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.FragmentedFileReader;
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
		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$
		if(filePosition != null) {
			setPosition(filePosition.longValue());
		}


		if(logFileName != null && !"".equals(logFileName)) { //$NON-NLS-1$
			/* Obtenemos la ruta completa al fichero log*/
			final String path = ConfigManager.getInstance().getLogsDir().toString().concat(logFileName);
			/*Obtenemos la informaci&oacute;n del fichero de configuraci&oacute;n de logs*/
			//TODO Pte ver si no existe si se obtiene los valores por defecto
			LogInfo info;
			try {
				try (FileInputStream fis = new FileInputStream(new File (path).getCanonicalFile())) {
					info = new LogInfo();
					info.load(fis);

					final int iNumLines = Integer.parseInt(sNumLines.trim());
					final File logFile = new File("C:/LOGS/fire/logging_api.log").getCanonicalFile(); //$NON-NLS-1$

					try (final AsynchronousFileChannel channel = AsynchronousFileChannel.open(logFile.toPath(),
						StandardOpenOption.READ);) {
						final LogReader reader = new FragmentedFileReader(channel, info.getCharset());
						reader.load(getPosition().longValue());
						final LogMore logMore = new LogMore(info);
						result = logMore.getLogMore(iNumLines,reader);
						setPosition(logMore.getFilePosition());
					}
					catch (final IOException | InvalidPatternException e) {
						e.printStackTrace();
					}
				}
				catch (final FileNotFoundException e1) {
					LOGGER.log(Level.SEVERE,"No se ha podido cargar el fichero de log ",e1); //$NON-NLS-1$
				} catch (final IOException e1) {
					LOGGER.log(Level.SEVERE,"No se ha podido cargar el fichero de log ",e1); //$NON-NLS-1$
				}
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
