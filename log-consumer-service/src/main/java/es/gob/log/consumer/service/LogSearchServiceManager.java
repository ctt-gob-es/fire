package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogSearchText;

public class LogSearchServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());

	/**
	 * @param req
	 * @return
	 * @throws IOException
	 * @throws NoResultException
	 */
	public final static byte[] process(final HttpServletRequest req) throws IOException, NoResultException {

		// Obtenemos los parametros
		final String numLinesString = req.getParameter(ServiceParams.NUM_LINES);
		final String text = req.getParameter(ServiceParams.SEARCH_TEXT);
		final boolean reset = Boolean.parseBoolean(req.getParameter(ServiceParams.PARAM_RESET));
		final String startDateTimeString = req.getParameter(ServiceParams.SEARCH_DATETIME);

		final int numLines;
		try {
			numLines = Integer.parseInt(numLinesString);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Numero de lineas con formato no valido", e); //$NON-NLS-1$
		}

		if (text == null || text.trim().isEmpty()) {
			throw new IllegalArgumentException("No se ha indicado el texto a buscar"); //$NON-NLS-1$
		}

		long sdateTime = 0L;
		if (startDateTimeString != null && !startDateTimeString.trim().isEmpty()) {
			try {
				sdateTime = Long.parseLong(startDateTimeString);
			} catch (final Exception e) {
				throw new IllegalArgumentException("Fecha de inicio con formato no valido", e); //$NON-NLS-1$
			}
		}

		final HttpSession session = req.getSession(false);
		final LogInfo info = (LogInfo) session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader) session.getAttribute("Reader"); //$NON-NLS-1$
		final Long fileSize = (Long) session.getAttribute("FileSize");  //$NON-NLS-1$
		final AsynchronousFileChannel channel = (AsynchronousFileChannel) session.getAttribute("Channel"); //$NON-NLS-1$
		Long filePosition = (Long) session.getAttribute("FilePosition");//$NON-NLS-1$

		byte[] result = null;

		try {
			if (reset) {
				reader.close();
				reader.load();
				// Reset de la posicion de sesion de tail
				if (filePosition != null && filePosition.longValue() > 0L) {
					filePosition = new Long(0L);
					session.setAttribute("FilePosition", filePosition); //$NON-NLS-1$
				}
			}

			if (channel.size() > fileSize.longValue() && reader.isEndFile()) {
				session.setAttribute("FileSize", new Long (channel.size())); //$NON-NLS-1$
				if (reader.getFilePosition() > 0L) {
					reader.reload(reader.getFilePosition());
				}
			}

			final LogSearchText logSearch = new LogSearchText(info);

			if (sdateTime < 0L) {
				result = logSearch.searchText(numLines, text, reader);
			}
			else {
				result = logSearch.searchText(numLines, text, sdateTime, reader);
			}

			session.setAttribute("Reader", reader); //$NON-NLS-1$

			if (result == null) {
				throw new NoResultException("No se han encontrado mas ocurrencias en la busqueda"); //$NON-NLS-1$
			}

		} catch (final NoResultException e) {
			LOGGER.log(Level.INFO, "No se han obtenido resultados: " + e.getMessage()); //$NON-NLS-1$
			throw e;
		} catch (final InvalidPatternException e) {
			LOGGER.log(Level.SEVERE, "El patron indicado con la forma de los registros del log, no es valido" , e); //$NON-NLS-1$
			throw new NoResultException("El patron indicado con la forma de los registros del log, no es valido", e); //$NON-NLS-1$
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido leer el fichero", e); //$NON-NLS-1$
			throw new NoResultException("No se ha podido leer el fichero", e); //$NON-NLS-1$
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error desconocido al procesar la peticion busqueda", e); //$NON-NLS-1$
			throw new NoResultException("Error desconocido al procesar la peticion busqueda", e); //$NON-NLS-1$
		}

		return result;
	}
}
