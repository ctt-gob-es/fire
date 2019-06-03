package es.gob.log.consumer.client;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

/**
 * Resultado de una operaci&oacute;n con el fichero de log. Consiste en un mensaje o, si ocurriese un error,
 * la informaci&oacute;n de error.
 */
public class LogResult {

	private String message;

	private LogError error;

	LogResult() {
		// Solo permitimos la construccion dentro del paquete
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public LogError getError() {
		return this.error;
	}

	public void setError(final LogError error) {
		this.error = error;
	}

	/**
	 * Serializa el objeto en forma de JSON.
	 * @return JSON con los valores del objeto.
	 */
	public String toJson() {

		if (this.error != null) {
			return this.error.toJson();
		}

		final JsonObjectBuilder logData = Json.createObjectBuilder()
				.add("message", this.message); //$NON-NLS-1$

		final StringWriter resultWriter = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(resultWriter)) {
			jw.writeObject(logData.build());
		}
		return resultWriter.toString();
	}
}
