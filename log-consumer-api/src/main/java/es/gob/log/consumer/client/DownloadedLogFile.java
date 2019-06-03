package es.gob.log.consumer.client;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

/**
 * Fichero de log descargado.
 */
public class DownloadedLogFile {

	private final String path;

	private final LogError error;

	/**
	 * Crea el objeto con la informaci&oacute;n del fichero descargado.
	 * @param path Ruta local en la que se ha almacenado el fichero de log.
	 */
	public DownloadedLogFile(final String path) {
		this.path = path;
		this.error = null;
	}

	/**
	 * Crea el objeto con la informaci&oacute;n del error producido al descargar
	 * el fichero de log.
	 * @param error Error producido en la descarga del fichero.
	 */
	public DownloadedLogFile(final LogError error) {
		this.path = null;
		this.error = error;
	}

	/**
	 * Recupera la ruta local en la que se ha almacenado el fichero de log.
	 * @return Ruta local de guardado.
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Recupera la informaci&oacute;n del error detectado durante la operaci&oacute;n de
	 * descarga del log.
	 * @return Error en la descarga del log.
	 */
	public LogError getError() {
		return this.error;
	}

	/**
	 * Serializa el objeto en forma de JSON.
	 * @return JSON con los valores del objeto.
	 */
	public String toJSON() {

		if (this.error != null) {
			return this.error.toJson();
		}

		final JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
				.add("path", this.path); //$NON-NLS-1$

		final StringWriter resultWriter = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(resultWriter)) {
			jw.writeObject(objectBuilder.build());
		}
		return resultWriter.toString();
	}
}
