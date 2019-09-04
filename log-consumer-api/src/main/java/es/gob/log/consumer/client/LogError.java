package es.gob.log.consumer.client;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

/**
 * Informaci&oacute;n de error obtenida al detectarse un problema grave al consultar un fichero de log.
 */
public class LogError {

	public static final String EC_CONNECTION = "ERR001";

	public static final String EC_CONTROLLED = "ERR002";

	public static final String EC_UNKNOWN = "ERR003";

	public static final String EC_NO_MORE_LINES = "ERR004";

	private final String code;

	private final String message;

	public LogError(final String code, final String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	/**
	 * Serializa el objeto en forma de JSON.
	 * @return JSON con las propiedades del objeto.
	 */
	public String toJson() {
		final JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
				.add("code", this.code) //$NON-NLS-1$
				.add("message", this.message); //$NON-NLS-1$

		final StringWriter resultWriter = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(resultWriter)) {
			jw.writeObject(objectBuilder.build());
		}
		return resultWriter.toString();
	}
}
