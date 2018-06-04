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
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogSearchText;

public class LogSearchServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());
//	private static LogErrors error = null;
//	private static int status = HttpServletResponse.SC_OK;

	public final static byte[] process(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

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
			if(logSearch.getFilePosition() > 0L) {
				session.setAttribute("FilePosition",Long.valueOf(logSearch.getFilePosition())); //$NON-NLS-1$
			}

			if(result == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se han encontrado más ocurrencias en la búsqueda"); //$NON-NLS-1$
				result = "No se han encontrado más ocurrencias en la búsqueda".getBytes( info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
			}

		} catch (final InvalidPatternException e) {
			LOGGER.log(Level.SEVERE,"El patr&oacute;n indicado con la forma de los registros del log, no es v&aacute;lido.",e.getMessage()); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "El patrón indicado con la forma de los registros del log, no es válido."); //$NON-NLS-1$
			result = "El patrón indicado con la forma de los registros del log, no es válido.".getBytes( info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
			return result;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero",e.getMessage()); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha podido leer el fichero"); //$NON-NLS-1$
			result = "No se ha podido leer el fichero".getBytes( info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
			return result;
		}
		catch (final InterruptedException e) {
			LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n buscar. ",e.getMessage()); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error al procesar la petición buscar."); //$NON-NLS-1$
			result ="Error al procesar la petición buscar.".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
		}
		catch (final ExecutionException e) {
			LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n buscar.",e.getMessage()); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error al procesar la petición buscar."); //$NON-NLS-1$
			result = "Error al procesar la petición buscar.".getBytes( info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
			return result;

		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero",e.getMessage()); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error al procesar la petición buscar."); //$NON-NLS-1$
			result = "Error al procesar la petición buscar.".getBytes( info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
			return result;
		}

		return result;
	}


//	public static final LogErrors getError() {
//		return error;
//	}
//
//
//	public static final void setError(final LogErrors error) {
//		LogSearchServiceManager.error = error;
//	}
//
//
//	public final static int getStatus() {
//		return LogSearchServiceManager.status;
//	}
//
//
//	public final static void setStatus(final int status) {
//		LogSearchServiceManager.status = status;
//	}




}
