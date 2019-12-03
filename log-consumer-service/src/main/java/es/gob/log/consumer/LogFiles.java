package es.gob.log.consumer;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFiles {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogFiles.class);

	/**
	 * M&eacute;todo de consulta de ficheros de logs. Obtiene el nombre de los ficheros,
	 * la fecha de la &uacute;ltima actualizaci&oacute;n en milisegundos y su tama&ntilde;o
	 * en bytes.
	 * @param logsDir Directorio con los ficheros de log.
	 * @param dirInfo Configuraci&oacute;n referente al directorio que se quiere listar.
	 * @return Array de bytes de un JSON con el listado de ficheros o con un mensaje de error.
	 */
	public static byte[] getLogFiles(final File logsDir, final LogDirInfo dirInfo) {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		if (logsDir == null || !logsDir.isDirectory()) {
			LOGGER.error("No se ha configurado un directorio de logs valido: " + logsDir); //$NON-NLS-1$
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

		final String[] patterns = dirInfo != null ? dirInfo.getHiddenPatterns() : null;
		final File[] files = logsDir.listFiles(new NegativeLogFilenameFilter(patterns));

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

		return writer.toString().getBytes(Charset.defaultCharset());
	}
}
