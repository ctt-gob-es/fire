package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogMore;
import es.gob.log.consumer.LogReader;

public class LogMoreServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogMoreServiceManager.class.getName());
	private static boolean hasMore = false;

	/**
	 * @param req
	 * @return
	 * @throws IOException
	 * @throws NoResultException
	 */
	public final static byte[] process(final HttpServletRequest req) throws IOException, NoResultException  {

		byte[] result = null;

		/* Obtenemos los par&aacute;metros*/
		final String numLines = req.getParameter(ServiceParams.NUM_LINES);
		int iNumLines;
		try {
			iNumLines = Integer.parseInt(numLines.trim());
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("No se ha proporcionado un número de líneas válido", e); //$NON-NLS-1$
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

					if (reader.getFilePosition() > 0L && reader.getFilePosition() > filePosition.longValue()) {
							reader.reload(reader.getFilePosition());
						}
					//Si la posicion de Tail es superior a la posicion del reader entonces es que se ha realizado la funcion de tail
					//y la ultima posicion es la guardada en session con FilePosition
					else if(reader.getFilePosition() > 0L && reader.getFilePosition() < filePosition.longValue() ) {
						reader.reload(filePosition.longValue());
					}
					else {
						reader.reload(filePosition.longValue());
					}
					//Reset de la posicion de sesion de tail
					filePosition = new Long(0L);
					session.setAttribute("FilePosition", filePosition); //$NON-NLS-1$
				}
				else if(reader.getFilePosition() > 0L) {
					reader.reload(reader.getFilePosition());
				}
			}

			// Si es el final de fichero, se indica con un mensaje
			if (reader.isEndFile()) {
				session.setAttribute("Reader", reader); //$NON-NLS-1$
				throw new NoResultException("No existen más líneas en este momento");//$NON-NLS-1$
			}

			final LogMore logMore = new LogMore();
			result = logMore.getLogMore(iNumLines, reader);

			session.setAttribute("Reader", reader); //$NON-NLS-1$
		}
		catch (final NoResultException e) {
			LOGGER.log(Level.INFO, "No se han encontrado mas resultados: " + e); //$NON-NLS-1$
			throw e;
		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido leer el fichero",e); //$NON-NLS-1$
			String msg = "No se ha podido leer el fichero"; //$NON-NLS-1$
			if (reader.isEndFile()){
				 msg = "No existen más líneas en este momento"; //$NON-NLS-1$
			}
			throw new NoResultException(msg, e);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error desconocido al procesar la petición de obtener más líneas", e); //$NON-NLS-1$
			throw new NoResultException("Error desconocido al procesar la petición de obtener más líneas", e);//$NON-NLS-1$
		}

		// Si no se han obtenido resultados, se indica con un mensaje
		if (result != null && result.length <= 0) {
			LOGGER.log(Level.INFO, "No se han encontrado más líneas en el fichero"); //$NON-NLS-1$
			throw new NoResultException("No se han encontrado más líneas en el fichero"); //$NON-NLS-1$
		}

		return result;


	}

	public static final boolean isHasMore() {
		return hasMore;
	}

	private static final void setHasMore(final boolean hasMore) {
		LogMoreServiceManager.hasMore = hasMore;
	}

}
