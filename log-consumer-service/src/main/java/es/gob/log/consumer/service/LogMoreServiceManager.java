package es.gob.log.consumer.service;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogMore;
import es.gob.log.consumer.LogReader;

public class LogMoreServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogMoreServiceManager.class.getName());
	private static boolean hasMore = false;

	public final static byte[] process(final HttpServletRequest req, final HttpServletResponse resp) throws IOException  {

		byte[] result = null;
		/* Obtenemos los par&aacute;metros*/
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final HttpSession session = req.getSession(true);
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final Long filePosition = (Long) session.getAttribute("FilePosition"); //$NON-NLS-1$
		final Long fileSize = (Long) session.getAttribute("FileSize"); //$NON-NLS-1$
		final AsynchronousFileChannel channel = (AsynchronousFileChannel)session.getAttribute("Channel"); //$NON-NLS-1$

		try {

			final int iNumLines = Integer.parseInt(sNumLines.trim());

			//Comprobamos que el fichero de log no se ha modificado en el trascurso de haber pulsado anteriormente la funci&oacute;n
			//Tail, y pueda haber m&aacute;s lineas, en ese caso se cierra el reader para cargarlo en la nueva posici&oacute;n
			if( channel.size() > fileSize.longValue() && reader.isEndFile()) {
				session.setAttribute("FileSize", new Long (channel.size())); //$NON-NLS-1$
				reader.setEndFile(false);
				if(filePosition.longValue() > 0L) {
					reader.reload(filePosition.longValue());
				}

			}
			//Si es el final de fichero se indica con un mensaje
			if(reader.isEndFile()) {
				LOGGER.log(Level.INFO,"No se han encontrado más ocurrencias en la  búsqueda"); //$NON-NLS-1$
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No existen más líneas en este momento para este fichero log");//$NON-NLS-1$
				result = new String("No existen m&aacute;s l&iacute;neas en este momento para este fichero log").getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
				session.setAttribute("Reader", reader); //$NON-NLS-1$
				return result;
			}

			final LogMore logMore = new LogMore();
//			logMore.setFilePosition(filePosition != null ? filePosition.longValue() : 0);
			result = logMore.getLogMore(iNumLines, reader);
			//Si es el final de fichero se indica con un mensaje
			 if (result != null && result.length <= 0) {
					LOGGER.log(Level.INFO,"No se han encontrado más ocurrencias en la  búsqueda"); //$NON-NLS-1$
					resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No existen más líneas en este momento para este fichero log");//$NON-NLS-1$
					result = new String("No existen m&aacute;s l&iacute;neas en este momento para este fichero log").getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
					session.setAttribute("Reader", reader); //$NON-NLS-1$
					return result;
			}

			session.setAttribute("FilePosition",new Long(reader.getFilePosition())); //$NON-NLS-1$
			session.setAttribute("Reader", reader); //$NON-NLS-1$


		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE,"No se ha podido leer el fichero",e); //$NON-NLS-1$
			String msg = "No se ha podido leer el fichero"; //$NON-NLS-1$
			if (reader.isEndFile()){
				 msg = "No existen más líneas en este momento para este fichero log"; //$NON-NLS-1$
			}
			//resp.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
			result = msg.getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8);

			return result;
		}
		catch (final NumberFormatException e) {
			LOGGER.log(Level.SEVERE,"No el parametro nlines no es un numero entero",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No el parametro nlines no es un numero entero");//$NON-NLS-1$
			result = "No el parametro nlines no es un numero entero".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$

			return result;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Error en servidor.",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error en la respuesta del servidor");//$NON-NLS-1$
			result = "\"Error en la respuesta del servidor".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$

			return result;

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
