package es.gob.log.consumer.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogErrors;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogMore;
import es.gob.log.consumer.LogReader;

public class LogMoreServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogMoreServiceManager.class.getName());

	private static LogErrors error = null;


	public final static byte[] process(final HttpServletRequest req) {
		byte[] result = null;
		/* Obtenemos los par&aacute;metros*/
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$


		try {

			final int iNumLines = Integer.parseInt(sNumLines.trim());
			if(filePosition != null && filePosition != Long.valueOf(0L)) {
				reader.load(filePosition.longValue());
			}


			final LogMore logMore = new LogMore(info);
			result = logMore.getLogMore(iNumLines,reader);
			session.setAttribute("FilePosition", Long.valueOf(0L)); //$NON-NLS-1$
			session.setAttribute("Reader", reader); //$NON-NLS-1$

		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero",e); //$NON-NLS-1$
			error = new LogErrors("No se ha podido leer el fichero",HttpServletResponse.SC_NOT_ACCEPTABLE);//$NON-NLS-1$
			result = "No se ha podido leer el fichero".getBytes(info.getCharset()); //$NON-NLS-1$
			return result;
		}
		catch (final InvalidPatternException e) {
			LOGGER.log(Level.SEVERE,"El patrón indicado con la forma de los registros del log, no es válido",e); //$NON-NLS-1$
			error = new LogErrors("El patrón indicado con la forma de los registros del log, no es válido",HttpServletResponse.SC_NOT_ACCEPTABLE); //$NON-NLS-1$
			result = error.getMsgError().getBytes(info.getCharset());
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







}
