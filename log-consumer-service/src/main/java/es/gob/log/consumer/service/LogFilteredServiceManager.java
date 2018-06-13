package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.Criteria;
import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogFilter;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;

public class LogFilteredServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogFilteredServiceManager.class.getName());
	private static boolean hasMore = false;


	public final static byte[] process(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {


		byte[] result = null;
		final Criteria crit = new Criteria();
		// Obtenemos los datos enviados al servicio.
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);

		if(req.getParameter(ServiceParams.START_DATETIME) != null && !"".equals(req.getParameter(ServiceParams.START_DATETIME))) { //$NON-NLS-1$
			final Long start_dateTime = new Long( Long.parseLong(req.getParameter(ServiceParams.START_DATETIME)));
			if(start_dateTime.longValue() > 0L) {
				crit.setStartDate(start_dateTime.longValue());
			}
		}
		if(req.getParameter(ServiceParams.END_DATETIME) != null && !"".equals(req.getParameter(ServiceParams.END_DATETIME))) { //$NON-NLS-1$
			final Long end_datetime = new Long( Long.parseLong(req.getParameter(ServiceParams.END_DATETIME)));
			if(end_datetime.longValue() > 0L) {
				crit.setEndDate(end_datetime.longValue());
			}
		}
		if(req.getParameter(ServiceParams.LEVEL) != null && !"".equals(req.getParameter(ServiceParams.LEVEL))){ //$NON-NLS-1$
			final String level = req.getParameter(ServiceParams.LEVEL);
			crit.setLevel(Integer.parseInt(level));
		}

		final boolean reset = Boolean.parseBoolean(req.getParameter(ServiceParams.PARAM_RESET));

		//Obtenemos los datos guardados de sesion
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$

		try {
			if(reset || filePosition != null &&  filePosition.longValue() == 0L) {
				reader.close();
				reader.load();
				reader.setEndFile(false);
			}

			final LogFilter filter = new LogFilter(info);
			filter.load(reader);
			filter.setCriteria(crit);
			result = filter.filter(Integer.parseInt(sNumLines));
			if(filter.canHasMore()) {
				session.setAttribute("FilePosition", new Long(filter.getFilePosition())); //$NON-NLS-1$
			}
			if( result != null && result.length <= 0) {
				session.setAttribute("Reader", reader); //$NON-NLS-1$
				LOGGER.log(Level.INFO,"No se han encontrado m&aacute;s ocurrencias en el filtrado"); //$NON-NLS-1$
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se han encontrado más ocurrencias en el filtrado"); //$NON-NLS-1$
				result = new String("No se han encontrado m&aacute;s ocurrencias en el filtrado").getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
				return result;
			}

//			if(!reader.isEndFile() && result != null && result.length <= 0) {
//				session.setAttribute("Reader", reader); //$NON-NLS-1$
//				LOGGER.log(Level.INFO,"No se han encontrado m&aacute;s ocurrencias en el filtrado"); //$NON-NLS-1$
//				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se han encontrado más ocurrencias en el filtrado"); //$NON-NLS-1$
//				result = new String("No se han encontrado m&aacute;s ocurrencias en el filtrado").getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
//				return result;
//			}
			session.setAttribute("Reader", reader); //$NON-NLS-1$

		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha podido leer el fichero"); //$NON-NLS-1$
			result = "No se ha podido leer el fichero".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8);//$NON-NLS-1$
			return result;
		} catch (final InterruptedException e) {
			LOGGER.log(Level.SEVERE,"Error en la operación de filtrado",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error en la operación de filtrado"); //$NON-NLS-1$
			result = "Error en la operación de filtrado".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8);//$NON-NLS-1$
			return result;
		} catch (final ExecutionException e) {
			LOGGER.log(Level.SEVERE,"Error en la operación de filtrado",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error en la operación de filtrado"); //$NON-NLS-1$
			result ="Error en la operación de filtrado".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8);//$NON-NLS-1$
			return result;
		} catch (final InvalidPatternException e) {
			LOGGER.log(Level.SEVERE,"Error en la operación de filtrado, patron incorrecto",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error en la operación de filtrado, patron incorrecto"); //$NON-NLS-1$
			result = "Error en la operación de filtrado, patron incorrecto".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
			return result;
		}

		return result;
	}

	public static final boolean isHasMore() {
		return hasMore;
	}

	private static final void setHasMore(final boolean hasMore) {
		LogFilteredServiceManager.hasMore = hasMore;
	}
}
