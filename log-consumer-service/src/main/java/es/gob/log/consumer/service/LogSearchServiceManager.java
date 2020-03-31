package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogSearchText;

/**
 * Manejador encargado de realizar b&uacute;squedas de texto en los ficheros de log.
 */
public class LogSearchServiceManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogTailServiceManager.class);

	/**
	 * Procesa una petici&oacute;n de b&uacute;squeda de texto.
	 * @param req Petici&oacute;n HTTP de b&uacute;squeda de texto.
	 * @return Contenido del log resultado de la b&uacute;squeda.
	 * @throws IOException Cuando se produce un error durante la lectura o proceso del log.
	 * @throws NoResultException Cuando no se encuentra el texto b&uacute;scado en el log.
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
		final LogInfo info = (LogInfo) session.getAttribute(SessionParams.LOG_INFO);
		final LogReader reader = (LogReader) session.getAttribute(SessionParams.FILE_READER);
		final Long fileSize = (Long) session.getAttribute(SessionParams.FILE_SIZE);
		final AsynchronousFileChannel channel = (AsynchronousFileChannel) session.getAttribute(SessionParams.FILE_CHANNEL);
		Long filePosition = (Long) session.getAttribute(SessionParams.FILE_POSITION);

		byte[] result = null;

		try {
			if (reset) {
				reader.close();
				reader.load();
				// Reset de la posicion de sesion de tail
				if (filePosition != null && filePosition.longValue() > 0L) {
					filePosition = new Long(0L);
					session.setAttribute(SessionParams.FILE_POSITION, filePosition);
				}
			}

			if (channel.size() > fileSize.longValue() && reader.isEndFile()) {
				session.setAttribute(SessionParams.FILE_SIZE, new Long (channel.size()));
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

			session.setAttribute(SessionParams.FILE_READER, reader);

			if (result == null) {
				throw new NoResultException("No se han encontrado mas ocurrencias en la busqueda"); //$NON-NLS-1$
			}

		} catch (final NoResultException e) {
			LOGGER.info("No se han obtenido resultados: " + e.getMessage()); //$NON-NLS-1$
			throw e;
		} catch (final InvalidPatternException e) {
			LOGGER.error("El patron indicado con la forma de los registros del log, no es valido" , e); //$NON-NLS-1$
			throw new NoResultException("El patron indicado con la forma de los registros del log, no es valido", e); //$NON-NLS-1$
		} catch (final IOException e) {
			LOGGER.error("No se ha podido leer el fichero", e); //$NON-NLS-1$
			throw new NoResultException("No se ha podido leer el fichero", e); //$NON-NLS-1$
		} catch (final Exception e) {
			LOGGER.error("Error desconocido al procesar la peticion busqueda", e); //$NON-NLS-1$
			throw new NoResultException("Error desconocido al procesar la peticion busqueda", e); //$NON-NLS-1$
		}

		return result;
	}
}
