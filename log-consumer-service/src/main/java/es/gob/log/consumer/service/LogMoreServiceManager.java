package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogMore;
import es.gob.log.consumer.LogReader;

/**
 * Clase que atiende las peticiones de obtenci&oacute;n de m&aacute;s registros de un
 * fichero de log.
 */
public class LogMoreServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogMoreServiceManager.class.getName());

	/**
	 * Procesa la petici&oacute;n de obtenci&oacute;n de m&aacute;s registros de log.
	 * @param req Petici&oacute;n HTTP.
	 * @return Bytes de los nuevos registros recuperados.
	 * @throws IOException Cuando ocurre un errpor durante la lectura.
	 * @throws NoResultException Cuando no se encuentran nuevos registros.
	 */
	public final static byte[] process(final HttpServletRequest req) throws IOException, NoResultException  {

		byte[] result = null;

		// Obtenemos los parametros de la peticion
		final String numLines = req.getParameter(ServiceParams.NUM_LINES);
		int iNumLines;
		try {
			iNumLines = Integer.parseInt(numLines.trim());
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("No se ha proporcionado un numero de lineas valido", e); //$NON-NLS-1$
		}

		final HttpSession session = req.getSession(false);
		final LogReader reader = (LogReader) session.getAttribute("Reader"); //$NON-NLS-1$
		final Long fileSize = (Long) session.getAttribute("FileSize"); //$NON-NLS-1$
		Long filePosition = (Long) session.getAttribute("FilePosition");//$NON-NLS-1$
		final AsynchronousFileChannel channel = (AsynchronousFileChannel) session.getAttribute("Channel"); //$NON-NLS-1$

		try {
			// Comprobamos que el fichero de log no se ha modificado en el trascurso de haber pulsado
			// anteriormente la funcion Tail, y pueda haber mas lineas, en ese caso se cierra el
			// reader para cargarlo en la nueva posicion
			if (channel.size() > fileSize.longValue() && reader.isEndFile()) {
				session.setAttribute("FileSize", new Long (channel.size())); //$NON-NLS-1$
				reader.setEndFile(false);

				if (filePosition != null && filePosition.longValue() > 0L) {

					if (reader.getFilePosition() > filePosition.longValue()) {
							reader.reload(reader.getFilePosition());
					}
					// La posicion del reader debe ser la posicion marcada en el fichero
					else {
						reader.reload(filePosition.longValue());
					}
					// Reset de la posicion de sesion de tail
					filePosition = new Long(0L);
					session.setAttribute("FilePosition", filePosition); //$NON-NLS-1$
				}
				else if (reader.getFilePosition() > 0L) {
					reader.reload(reader.getFilePosition());
				}
			}

			// Si es el final de fichero, se indica con un mensaje
			if (reader.isEndFile()) {
				session.setAttribute("Reader", reader); //$NON-NLS-1$
				throw new NoResultException("No existen mas lineas en este momento"); //$NON-NLS-1$
			}

			result = LogMore.getLogMore(iNumLines, reader);

			session.setAttribute("Reader", reader); //$NON-NLS-1$
		}
		catch (final NoResultException e) {
			LOGGER.log(Level.INFO, "No se han encontrado mas resultados: " + e); //$NON-NLS-1$
			throw e;
		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido leer el fichero", e); //$NON-NLS-1$
			if (reader.isEndFile()){
				throw new NoResultException("No existen mas lineas en este momento", e); //$NON-NLS-1$
			}
			throw new NoResultException("No se ha podido leer el fichero", e);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error desconocido al procesar la peticion de obtener mas lineas", e); //$NON-NLS-1$
			throw new IOException("Error desconocido al procesar la peticion de obtener mas lineas", e);//$NON-NLS-1$
		}

		// Si no se han obtenido resultados, se indica con un mensaje
		if (result == null || result.length <= 0) {
			throw new NoResultException("No se han encontrado mas lineas en el fichero"); //$NON-NLS-1$
		}

		return result;
	}
}
