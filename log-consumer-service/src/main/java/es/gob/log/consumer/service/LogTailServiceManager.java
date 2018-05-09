package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogErrors;
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
	private static LogErrors error = null;
	/**
	 *
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public final static byte[] process(final HttpServletRequest req)  {

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


		/*Comprobanmos el valor del par&aacute;metro LOG_FILE_NAME */
		if(logFileName != null && !"".equals(logFileName)) { //$NON-NLS-1$


			try {
				/* Obtenemos la ruta completa al fichero log*/
				final String path = ConfigManager.getInstance().getLogsDir().getCanonicalPath().toString().concat(File.separator).concat(logFileName);
				if(info != null) {
//					String resTail = ""; //$NON-NLS-1$
					final LogTail lTail = new LogTail(info,path);
					final int iNumLines = sNumLines.trim() != null  && !"".equals(sNumLines.trim()) ? Integer.parseInt(sNumLines.trim()) : 0; //$NON-NLS-1$
					final String resTail = lTail.getLogTail(iNumLines);
//					String resultTail = "";//$NON-NLS-1$
//					for(int i = 0; i < resTail.length; i++){
//						resultTail += (char)resTail[i];
//				    }
					result = resTail.getBytes(info.getCharset());
					setPosition(lTail.getFilePosition());

				}
				else {
					LOGGER.log(Level.WARNING,"Es necesario abrir el fichero log anteriormente."); //$NON-NLS-1$
					error = new LogErrors();
					error.setNumError(HttpServletResponse.SC_PRECONDITION_FAILED);
					error.setMsgError("Es necesario abrir el fichero log anteriormente.");//$NON-NLS-1$
					result = error.getMsgError().getBytes(info.getCharset());
//					data.add(Json.createObjectBuilder()
//							.add("Code",HttpServletResponse.SC_PRECONDITION_FAILED) //$NON-NLS-1$
//							.add("Message", "Es necesario abrir el fichero log anteriormente.")); //$NON-NLS-1$ //$NON-NLS-2$
//					jsonObj.add("Error", data); //$NON-NLS-1$
//					final JsonWriter jw = Json.createWriter(result);
//			        jw.writeObject(jsonObj.build());
//			        jw.close();
				}

			}
			catch (final NumberFormatException e) {
				LOGGER.log(Level.SEVERE,"El par&aacute;metro nlines no es un n&uacute;mero entero",e); //$NON-NLS-1$

//
//				data.add(Json.createObjectBuilder()
//						.add("Code",HttpServletResponse.SC_NOT_ACCEPTABLE) //$NON-NLS-1$
//						.add("Message", "El parametro número de líneas no es un numero entero.")); //$NON-NLS-1$ //$NON-NLS-2$
//				jsonObj.add("Error", data); //$NON-NLS-1$
//				final JsonWriter jw = Json.createWriter(result);
//		        jw.writeObject(jsonObj.build());
//		        jw.close();
		        error = new LogErrors();
				error.setNumError(HttpServletResponse.SC_NOT_ACCEPTABLE);
				error.setMsgError("El parametro nlines no es un n&uacte;mero entero"); //$NON-NLS-1$
				result = error.getMsgError().getBytes(info.getCharset());
				return result;


			} catch (final IOException e) {

				LOGGER.log(Level.SEVERE,"No se ha podido cargar el fichero de log.",e); //$NON-NLS-1$
			    error = new LogErrors();
			    error.setNumError(HttpServletResponse.SC_BAD_REQUEST);
				error.setMsgError("No se ha podido cargar el fichero de log."); //$NON-NLS-1$
				result = error.getMsgError().getBytes(info.getCharset());
				return result;


//				data.add(Json.createObjectBuilder()
//						.add("Code",HttpServletResponse.SC_BAD_REQUEST) //$NON-NLS-1$
//						.add("Message", "No se ha podido cargar el fichero de log.")); //$NON-NLS-1$ //$NON-NLS-2$
//				jsonObj.add("Error", data); //$NON-NLS-1$
//				final JsonWriter jw = Json.createWriter(result);
//		        jw.writeObject(jsonObj.build());
//		        jw.close();

			}

		}

		if(result != null && result.length > 0) {
			return result;
		}

		LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n de leer las &uacute;ltimas l&iacute;neas del fichero."); //$NON-NLS-1$
		error = new LogErrors();
		error.setNumError(HttpServletResponse.SC_BAD_REQUEST);
		error.setMsgError("Error al procesar la petici&oacute;n de leer las &uacute;ltimas l&iacute;neas del fichero."); //$NON-NLS-1$
		result = error.getMsgError().getBytes(info.getCharset());
		return result;

//		data.add(Json.createObjectBuilder()
//				.add("Code",HttpServletResponse.SC_BAD_REQUEST) //$NON-NLS-1$
//				.add("Message", "Error al procesar la petición de leer las últimas líneas del fichero.")); //$NON-NLS-1$ //$NON-NLS-2$
//		jsonObj.add("Error", data); //$NON-NLS-1$
//		final JsonWriter jw = Json.createWriter(result);
//        jw.writeObject(jsonObj.build());
//        jw.close();
//        LOGGER.log(Level.SEVERE,"Error al procesar la petición de leer las últimas líneas del fichero."); //$NON-NLS-1$


	}

	protected final static Long getPosition() {
		return LogTailServiceManager.position;
	}

	private final static void setPosition(final long position) {
		LogTailServiceManager.position = new Long (position);
	}

	public static final LogErrors getError() {
		return error;
	}

}
