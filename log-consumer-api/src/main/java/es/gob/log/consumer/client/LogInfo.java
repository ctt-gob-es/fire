package es.gob.log.consumer.client;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

/**
 * Informaci&oacute;n disponible sobre un fichero de log.
 */
public class LogInfo {

	private String charset;
	private String[] levels;
	private boolean date;
	private boolean time;
	private String dateTimeFormat;

	private LogError error;

	LogInfo() {
		// Solo permitimos la construccion dentro del paquete
	}

	public String getCharset() {
		return this.charset;
	}

	void setCharset(final String charset) {
		this.charset = charset;
	}

	public String[] getLevels() {
		return this.levels;
	}

	void setLevels(final String[] levels) {
		this.levels = levels;
	}

	public boolean isDate() {
		return this.date;
	}

	void setDate(final boolean date) {
		this.date = date;
	}

	public boolean isTime() {
		return this.time;
	}

	void setTime(final boolean time) {
		this.time = time;
	}

	public String getDateTimeFormat() {
		return this.dateTimeFormat;
	}

	void setDateTimeFormat(final String datetimeformat) {
		this.dateTimeFormat = datetimeformat;
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

		JsonArrayBuilder levelsBuilder = null;
		if (this.levels != null) {
			levelsBuilder = Json.createArrayBuilder();
			for (final String level : this.levels) {
				levelsBuilder.add(level);
			}
		}

		final JsonObjectBuilder logInfo = Json.createObjectBuilder()
				.add("charset", this.charset) //$NON-NLS-1$
				.add("date", this.date) //$NON-NLS-1$
				.add("time", this.time) //$NON-NLS-1$
				.add("dateTimeFormat", this.dateTimeFormat); //$NON-NLS-1$

		if (levelsBuilder != null) {
			logInfo.add("levels", levelsBuilder); //$NON-NLS-1$
		}

		final StringWriter resultWriter = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(resultWriter)) {
			jw.writeObject(logInfo.build());
		}
		return resultWriter.toString();
	}
}
