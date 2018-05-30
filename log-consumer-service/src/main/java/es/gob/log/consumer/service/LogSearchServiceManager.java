package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogErrors;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogSearchText;

public class LogSearchServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());
	private static LogErrors error = null;
	private static int status = HttpServletResponse.SC_OK;

	public final static byte[] process(final HttpServletRequest req) {

		byte[] result = null;
		/* Obtenemos los par&aacute;metros*/
		//final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final String text = req.getParameter(ServiceParams.SEARCH_TEXT);
		final boolean reset = Boolean.parseBoolean(req.getParameter(ServiceParams.PARAM_RESET));
		Long sdateTime = null;
		if(req.getParameter(ServiceParams.SEARCH_DATETIME) != null && !"".equals(req.getParameter(ServiceParams.SEARCH_DATETIME))) { //$NON-NLS-1$
			sdateTime = new Long( Long.parseLong(req.getParameter(ServiceParams.SEARCH_DATETIME)));
		}
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$

		try {

			if(reset || filePosition != null &&  filePosition.longValue() == 0L) {
				 reader.rewind() ;
			}

			final LogSearchText logSearch = new LogSearchText(info, reader);

			if(sdateTime == null || sdateTime.longValue() < 0L) {
				result = logSearch.searchText(Integer.parseInt(sNumLines) , text);
			}
			else {
				result = logSearch.searchText(Integer.parseInt(sNumLines) , text, sdateTime.longValue());
			}
			session.setAttribute("Reader", reader); //$NON-NLS-1$

//			if(logSearch.getError() != null && logSearch.getError().getMsgError() != null) {
//					error = logSearch.getError();
//					session.setAttribute("FilePosition", Long.valueOf(0L)); //$NON-NLS-1$
//			}
//			else {
				if(logSearch.getFilePosition() > 0L) {
					session.setAttribute("FilePosition",Long.valueOf(logSearch.getFilePosition())); //$NON-NLS-1$
				}
//			}
				//throw new InvalidPatternException("Prueba"); //$NON-NLS-1$
		} catch (final InvalidPatternException e) {
			LOGGER.log(Level.SEVERE,"El patrón indicado con la forma de los registros del log, no es válido."); //$NON-NLS-1$
			error = new LogErrors("El patrón indicado con la forma de los registros del log, no es válido.",HttpServletResponse.SC_NOT_ACCEPTABLE); //$NON-NLS-1$
			result = error.getMsgError().getBytes(StandardCharsets.UTF_8);
			return result;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero"); //$NON-NLS-1$
			error = new LogErrors("No se ha podido leer el fichero",HttpServletResponse.SC_NOT_ACCEPTABLE); //$NON-NLS-1$
			result = error.getMsgError().getBytes( StandardCharsets.UTF_8);
			return result;
		}
		catch (final InterruptedException e) {
			LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n buscar. "); //$NON-NLS-1$
			error = new LogErrors("Error al procesar la petici&oacute;n buscar texto.".concat(e.getMessage()),HttpServletResponse.SC_CONFLICT); //$NON-NLS-1$
			result = error.getMsgError().getBytes(info.getCharset());
		}
		catch (final ExecutionException e) {
			LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n buscar."); //$NON-NLS-1$
			error = new LogErrors("Error al procesar la petici&oacute;n buscar texto.",HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			result = error.getMsgError().getBytes(info.getCharset());
			return result;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero"); //$NON-NLS-1$
			error = new LogErrors("No se ha podido leer el fichero",HttpServletResponse.SC_NOT_ACCEPTABLE); //$NON-NLS-1$
			result = error.getMsgError().getBytes( StandardCharsets.UTF_8);
			return result;
		}

		return result;
	}


	public static final LogErrors getError() {
		return error;
	}


	public static final void setError(final LogErrors error) {
		LogSearchServiceManager.error = error;
	}


	public final static int getStatus() {
		return LogSearchServiceManager.status;
	}


	public final static void setStatus(final int status) {
		LogSearchServiceManager.status = status;
	}




}
