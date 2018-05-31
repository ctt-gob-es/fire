package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogErrors;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogMore;
import es.gob.log.consumer.LogReader;

public class LogMoreServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogMoreServiceManager.class.getName());

	private static LogErrors error = null;

	private static int status = HttpServletResponse.SC_OK;

	public final static byte[] process(final HttpServletRequest req) {

		byte[] result = null;
		/* Obtenemos los par&aacute;metros*/
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$

		if(getError()!= null && getError().getMsgError() != null && !"".equals(getError().getMsgError())) { //$NON-NLS-1$
			setError(null);
		}
		if (getStatus() != HttpServletResponse.SC_OK) {
			setStatus(HttpServletResponse.SC_OK);
		}

		try {

			final int iNumLines = Integer.parseInt(sNumLines.trim());
			if(filePosition != null &&  filePosition.longValue() == 0L) {
				reader.load(filePosition.longValue());
			}

			 if (reader.isEndFile()) {
					LOGGER.log(Level.INFO,"No se han encontrado m&aacute;s ocurrencias en la  b&uacute;squeda"); //$NON-NLS-1$
					setStatus(HttpServletResponse.SC_ACCEPTED);
					result = new String("No existen m&aacute;s l&iacute;neas en este momento para este fichero log").getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
					session.setAttribute("Reader", reader); //$NON-NLS-1$
					return result;
			}

			final LogMore logMore = new LogMore();
			result = logMore.getLogMore(iNumLines,reader);

			session.setAttribute("FilePosition", new Long(logMore.getFilePosition())); //$NON-NLS-1$
			session.setAttribute("Reader", reader); //$NON-NLS-1$


		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero",e); //$NON-NLS-1$
			error = new LogErrors("No se ha podido leer el fichero",HttpServletResponse.SC_NOT_FOUND);//$NON-NLS-1$
			result = "No se ha podido leer el fichero".getBytes(info.getCharset()); //$NON-NLS-1$
			return result;
		}
		catch (final NumberFormatException e) {
			LOGGER.log(Level.SEVERE,"No el parametro nlines no es un numero entero",e); //$NON-NLS-1$
			error = new LogErrors("No el parametro nlines no es un numero entero",HttpServletResponse.SC_NOT_ACCEPTABLE); //$NON-NLS-1$
			result = error.getMsgError().getBytes(info.getCharset());
			return result;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Error en servidor.",e); //$NON-NLS-1$
			error = new LogErrors("Error en la respuesta del servidor.",HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			result = error.getMsgError().getBytes(info.getCharset());
			return result;
		}
		return result;


	}


	public static final LogErrors getError() {
		return error;
	}


	public static final void setError(final LogErrors error) {
		LogMoreServiceManager.error = error;
	}

	public final static int getStatus() {
		return LogMoreServiceManager.status;
	}

	public final static void setStatus(final int status) {
		LogMoreServiceManager.status = status;
	}





}
