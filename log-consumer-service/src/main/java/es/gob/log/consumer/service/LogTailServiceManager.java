package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogTail;

/**
 *
 * @author Adolfo.Navarro
 *
 */
public class LogTailServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogTailServiceManager.class.getName());
	private static Long position = new Long(0L);
	/**
	 *
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public final static byte[] process(final HttpServletRequest req)  {

		final StringWriter result = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		/* Obtenemos los par&aacute;metros*/
		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final String sNumLines = req.getParameter(ServiceParams.NUM_LINES);
		final HttpSession session = req.getSession(true);

		/*Comprobanmos el valor del par&aacute;metro LOG_FILE_NAME */
		if(logFileName != null && !"".equals(logFileName)) { //$NON-NLS-1$

			/*Obtenemos la informaci&oacute;n del fichero de configuraci&oacute;n de logs*/
			final LogInfo info = (LogInfo)session.getAttribute("LogInfo"); //$NON-NLS-1$
			try {
				/* Obtenemos la ruta completa al fichero log*/
				final String path = ConfigManager.getInstance().getLogsDir().getCanonicalPath().toString().concat(File.separator).concat(logFileName);
				if(info != null) {
					final LogTail lTail = new LogTail(info,path);
					final int iNumLines = sNumLines.trim() != null  && !"".equals(sNumLines.trim()) ? Integer.parseInt(sNumLines.trim()) : 0; //$NON-NLS-1$
					result.write(lTail.getLogTail(iNumLines).toString());
					setPosition(lTail.getFilePosition());
				}
				else {
					data.add(Json.createObjectBuilder()
							.add("Code",HttpServletResponse.SC_PRECONDITION_FAILED) //$NON-NLS-1$
							.add("Message", "Es necesario abrir el fichero log anteriormente.")); //$NON-NLS-1$ //$NON-NLS-2$
					jsonObj.add("Error", data); //$NON-NLS-1$
					final JsonWriter jw = Json.createWriter(result);
			        jw.writeObject(jsonObj.build());
			        jw.close();
				}

			}
			catch (final NumberFormatException e) {
				LOGGER.log(Level.SEVERE,"El parametro nlines no es un numero entero",e); //$NON-NLS-1$
				data.add(Json.createObjectBuilder()
						.add("Code",HttpServletResponse.SC_BAD_REQUEST) //$NON-NLS-1$
						.add("Message", "El parametro número de líneas no es un numero entero.")); //$NON-NLS-1$ //$NON-NLS-2$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
		        jw.writeObject(jsonObj.build());
		        jw.close();
		        return result.toString().getBytes();
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE,"No se ha podido cargar el fichero de log.",e); //$NON-NLS-1$
				data.add(Json.createObjectBuilder()
						.add("Code",HttpServletResponse.SC_BAD_REQUEST) //$NON-NLS-1$
						.add("Message", "No se ha podido cargar el fichero de log.")); //$NON-NLS-1$ //$NON-NLS-2$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
		        jw.writeObject(jsonObj.build());
		        jw.close();
				return result.toString().getBytes();
			}

		}

		if(result.getBuffer().length() > 0) {
			return result.toString().getBytes();
		}

		data.add(Json.createObjectBuilder()
				.add("Code",HttpServletResponse.SC_BAD_REQUEST) //$NON-NLS-1$
				.add("Message", "Error al procesar la petición de leer las últimas líneas del fichero.")); //$NON-NLS-1$ //$NON-NLS-2$
		jsonObj.add("Error", data); //$NON-NLS-1$
		final JsonWriter jw = Json.createWriter(result);
        jw.writeObject(jsonObj.build());
        jw.close();
        LOGGER.log(Level.SEVERE,"Error al procesar la petición de leer las últimas líneas del fichero: result.getBuffer().length() <= 0"); //$NON-NLS-1$
        return result.toString().getBytes();

	}

	protected final static Long getPosition() {
		return LogTailServiceManager.position;
	}

	private final static void setPosition(final long position) {
		LogTailServiceManager.position = new Long (position);
	}



}
