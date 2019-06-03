package es.gob.log.consumer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletResponse;

public class LogFiles {

	private static final String FILE_EXT_LOGINFO = LogConstants.FILE_EXT_LOGINFO;
	private static final String FILE_EXT_LCK = LogConstants.FILE_EXT_LCK;

	private static final Logger LOGGER = Logger.getLogger(LogFiles.class.getName());

	/**
	 * M&eacute;todo de consulta de ficheros de logs. Obtiene el nombre de los ficheros,
	 *  la fecha de la &uacute;ltima actualizaci&oacute;n en milisegundos y su tama&ntilde;o
	 *  en bytes.
	 * @return Array de bytes de un JSON con el listado de ficheros o con un mensaje de error.
	 */
	public static byte[] getLogFiles(final File logsDir) {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		if (logsDir == null || !logsDir.isDirectory()) {
			LOGGER.log(Level.SEVERE, "No se ha configurado un directorio de logs valido: " + logsDir); //$NON-NLS-1$
			final JsonArrayBuilder error = Json.createArrayBuilder();
			error.add(Json.createObjectBuilder()
					.add("code",HttpServletResponse.SC_BAD_REQUEST) //$NON-NLS-1$
					.add("message", "No se ha podido obtener la lista de ficheros log.")); //$NON-NLS-1$//$NON-NLS-2$
			jsonObj.add("error", error); //$NON-NLS-1$

			final StringWriter writer = new StringWriter();
			try (final JsonWriter jw = Json.createWriter(writer)) {
				jw.writeObject(jsonObj.build());
			}
			return writer.toString().getBytes(Charset.defaultCharset());
		}


		byte[] result = null;
		final File[] files = logsDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				if (name.lastIndexOf(".") == -1) { //$NON-NLS-1$
					return false;
				}

				final int dot = name.lastIndexOf('.');
				final String ext = name.substring(dot);
				return !ext.equalsIgnoreCase(FILE_EXT_LOGINFO) &&
						!ext.equalsIgnoreCase(FILE_EXT_LCK);
			}
		});

		// Devolvemos el listado de ficheros encontrados
		final JsonArrayBuilder data = Json.createArrayBuilder();
		for (final File logFile : files) {
			data.add(Json.createObjectBuilder()
					.add("name", logFile.getName()) //$NON-NLS-1$
					.add("date", logFile.lastModified()) //$NON-NLS-1$
					.add("size", logFile.length()) //$NON-NLS-1$
					);
		}
		jsonObj.add("fileList", data); //$NON-NLS-1$
		final StringWriter writer = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(writer)) {
			jw.writeObject(jsonObj.build());
		}
		result = writer.toString().getBytes(Charset.defaultCharset());

		return result;
	}
}
