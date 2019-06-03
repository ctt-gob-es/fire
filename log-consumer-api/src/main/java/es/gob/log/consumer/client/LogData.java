package es.gob.log.consumer.client;

import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

/**
 * Datos recuperados de un fichero de log.
 */
public class LogData {

	private byte[] log;

	private Charset charset;

	private LogError error;

	LogData() {
		// Solo permitimos la construccion dentro del paquete
	}

	public byte[] getLog() {
		return this.log;
	}

	public void setLog(final byte[] log) {
		this.log = log;
	}

	public Charset getCharset() {
		return this.charset;
	}

	public void setCharset(final Charset charset) {
		this.charset = charset;
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
	public String toJson(final Charset charset) {

		if (this.error != null) {
			return this.error.toJson();
		}

		final JsonObjectBuilder logData = Json.createObjectBuilder()
				.add("log", new String(this.log, charset)); //$NON-NLS-1$

		final StringWriter resultWriter = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(resultWriter)) {
			jw.writeObject(logData.build());
		}
		return resultWriter.toString();
	}
}
