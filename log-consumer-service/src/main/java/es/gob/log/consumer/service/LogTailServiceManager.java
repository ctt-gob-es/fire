package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.LogTail;

/**
 *
 * @author Adolfo.Navarro
 *
 */
public class LogTailServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());
//	private static Long position = new Long(0L);
//	private static LogErrors error = null;
	/**
	 *
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public final static byte[] process(final HttpServletRequest req,  final HttpServletResponse resp) throws IOException  {

		byte[] result = null;
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		/* Obtenemos la sesio&oacute;n*/
		final HttpSession session = req.getSession(true);
		/*Obtenemos la informaci&oacute;n del fichero de configuraci&oacute;n de logs*/
		final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
		/* Obtenemos los par&aacute;metros*/
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final LogReader reader = (LogReader)session.getAttribute("Reader"); //$NON-NLS-1$
		final AsynchronousFileChannel channel_session = (AsynchronousFileChannel)session.getAttribute("Channel"); //$NON-NLS-1$
//		if(getError()!= null && getError().getMsgError() != null && !"".equals(getError().getMsgError())) { //$NON-NLS-1$
//			setError(null);
//		}

		/*Comprobanmos el valor del par&aacute;metro LOG_FILE_NAME */
		if(logFileName != null && !"".equals(logFileName)) { //$NON-NLS-1$

			try {
				/* Obtenemos la ruta completa al fichero log*/
				final String path = ConfigManager.getInstance().getLogsDir().getCanonicalPath().toString().concat(File.separator).concat(logFileName);
				if(info != null) {
					final LogTail lTail = new LogTail(info,path);
					final int iNumLines = sNumLines.trim() != null  && !"".equals(sNumLines.trim()) ? Integer.parseInt(sNumLines.trim()) : 0; //$NON-NLS-1$
					final String resTail = lTail.getLogTail(iNumLines);
					result = resTail.getBytes(info.getCharset());
					session.setAttribute("FilePosition",Long.valueOf(lTail.getFilePosition()));//$NON-NLS-1$
					session.setAttribute("FileSize", new Long (channel_session.size())); //$NON-NLS-1$
					reader.setEndFile(true);
					session.setAttribute("Reader",reader);//$NON-NLS-1$
				}
				else {
					LOGGER.log(Level.WARNING,"Es necesario abrir el fichero log anteriormente."); //$NON-NLS-1$
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Es necesario abrir el fichero log anteriormente"); //$NON-NLS-1$
					result = "Es necesario abrir el fichero log anteriormente.".getBytes(StandardCharsets.UTF_8 ); //$NON-NLS-1$
				}

			}
			catch (final NumberFormatException e) {
				LOGGER.log(Level.SEVERE,"El par&aacute;metro nlines no es un n&uacute;mero entero",e);  //$NON-NLS-1$
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "El parametro nlines no es un n&uacte;mero entero"); //$NON-NLS-1$
				result = "El parametro nlines no es un n&uacte;mero entero".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
				return result;

			} catch (final IOException e) {

				LOGGER.log(Level.SEVERE,"No se ha podido cargar el fichero de log.",e); //$NON-NLS-1$
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha podido cargar el fichero de log"); //$NON-NLS-1$
				result = "No se ha podido cargar el fichero de log".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
				return result;

			}

		}

		if(result != null && result.length > 0) {
			return result;
		}

		LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n de leer las &uacute;ltimas l&iacute;neas del fichero."); //$NON-NLS-1$
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error al procesar la petición de leer las últimas líneas del fichero"); //$NON-NLS-1$
//		error = new LogErrors("Error al procesar la petici&oacute;n de leer las &uacute;ltimas l&iacute;neas del fichero.",HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
		result = "Error al procesar la petición de leer las últimas líneas del fichero".getBytes(info != null ? info.getCharset() : StandardCharsets.UTF_8); //$NON-NLS-1$
		return result;

	}

}
