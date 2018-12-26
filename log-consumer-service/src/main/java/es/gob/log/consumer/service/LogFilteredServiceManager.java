package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.Criteria;
import es.gob.log.consumer.InvalidPatternException;
import es.gob.log.consumer.LogFilter;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;

/**
 * Clase para la gesti&oacute;n de las peticiones de registros de logs filtrados.
 */
public class LogFilteredServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogFilteredServiceManager.class.getName());

	private static final int DEFAULT_NUM_LINES = 50;

	/**
	 * Atiende una petici&oacute;n de obtenci&oacute;n de registros de log filtrados.
	 * @param req Petici&oacute;n HTTP.
	 * @return Bytes de las l&iacute;neas recuperadas.
	 * @throws NoResultException Cuando no se han encontrado m&aacute;s registros de log.
	 * @throws IOException Cuando ocurre un error durante la lectura del log.
	 */
	public final static byte[] process(final HttpServletRequest req) throws NoResultException, IOException {

		// Obtenemos los datos enviados al servicio.
		final String numLinesString = req.getParameter(ServiceParams.NUM_LINES);
		final String startDateTimeString = req.getParameter(ServiceParams.START_DATETIME);
		final String endDateTimeString = req.getParameter(ServiceParams.END_DATETIME);
		final String levelString = req.getParameter(ServiceParams.LEVEL);

		final Criteria crit = new Criteria();

		int numLines = DEFAULT_NUM_LINES;
		if (numLinesString != null && !numLinesString.isEmpty()) {
			try {
				numLines = Integer.parseInt(numLinesString.trim());
			} catch (final Exception e) {
				throw new IllegalArgumentException("Numero de lineas con formato no valido", e); //$NON-NLS-1$
			}
		}

		if (startDateTimeString != null && !startDateTimeString.trim().isEmpty()) {
			try {
				final long startDateTime = Long.parseLong(startDateTimeString.trim());
				if (startDateTime > 0L) {
					crit.setStartDate(startDateTime);
				}
			} catch (final Exception e) {
				throw new IllegalArgumentException("Fecha de inicio con formato no valido", e); //$NON-NLS-1$
			}
		}
		if (endDateTimeString != null && !endDateTimeString.trim().isEmpty()) {
			try {
				final long endDatetime = Long.parseLong(endDateTimeString.trim());
				if (endDatetime > 0L) {
					crit.setEndDate(endDatetime);
				}
			} catch (final Exception e) {
				throw new IllegalArgumentException("Fecha de fin con formato no valido", e); //$NON-NLS-1$
			}
		}
		if (levelString != null && !levelString.trim().isEmpty()){
			try {
				crit.setLevel(Integer.parseInt(levelString.trim()));
			}
			catch (final Exception e) {
				throw new IllegalArgumentException("Nivel de log con formato no valido", e); //$NON-NLS-1$
			}
		}

		final boolean reset = Boolean.parseBoolean(req.getParameter(ServiceParams.PARAM_RESET));

		// Obtenemos los datos guardados de sesion
		final HttpSession session = req.getSession(false);
		final LogInfo info = (LogInfo) session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader) session.getAttribute("Reader"); //$NON-NLS-1$
		final Long fileSize = (Long) session.getAttribute("FileSize");  //$NON-NLS-1$
		final AsynchronousFileChannel channel = (AsynchronousFileChannel) session.getAttribute("Channel"); //$NON-NLS-1$
		Long filePosition = (Long) session.getAttribute("FilePosition");//$NON-NLS-1$

		byte[] result = null;
		try {

			final LogFilter filter = new LogFilter(info);

			// Se carga el registro en el caso de ser la primera vez que entra al proceso
			if (reset) {
				reader.close();
				reader.load();
				//Reset de la posicion de sesion de tail
				if (filePosition != null && filePosition.longValue() > 0L) {
					filePosition = new Long(0L);
					session.setAttribute("FilePosition", filePosition); //$NON-NLS-1$
				}
			}

			// Se recarga el registro en caso de que el tamanno del fichero haya aumentado
			if (channel.size() > fileSize.longValue() && reader.isEndFile()) {
				session.setAttribute("FileSize", new Long (channel.size())); //$NON-NLS-1$
				if(reader.getFilePosition() > 0L) {
					reader.reload(reader.getFilePosition());
				}
			}

			filter.loadReaderToFilter(reader);
			filter.setCriteria(crit);
			result = filter.filter(numLines);

			if (result != null && result.length <= 0) {
				session.setAttribute("Reader", reader); //$NON-NLS-1$
				LOGGER.log(Level.INFO,"No se han encontrado mas ocurrencias en el filtrado"); //$NON-NLS-1$
				throw new NoResultException("No se han encontrado mas ocurrencias en el filtrado"); //$NON-NLS-1$
			}

			session.setAttribute("Reader", reader); //$NON-NLS-1$

		} catch (final NoResultException e) {
			LOGGER.log(Level.INFO, "No se han encontrado mas resultados: " + e); //$NON-NLS-1$
			throw e;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido leer el fichero", e); //$NON-NLS-1$
			throw new IOException("No se ha podido leer el fichero", e); //$NON-NLS-1$
		} catch (final InvalidPatternException e) {
			LOGGER.log(Level.SEVERE, "Error en la operacion de filtrado, patron incorrecto", e); //$NON-NLS-1$
			throw new IOException("Error en la operacion de filtrado, patron incorrecto", e); //$NON-NLS-1$
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error desconocido en la operacion de filtrado", e); //$NON-NLS-1$
			throw new IOException("Error desconocido en la operacion de filtrado", e); //$NON-NLS-1$
		}

		return result;
	}
}
