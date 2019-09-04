package es.gob.log.consumer.client;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class ResponseParser {

	public static LogInfo parseLogInfo(final byte[] json) {

		final LogInfo logInfo = new LogInfo();

		try (final JsonReader reader = Json.createReader(new ByteArrayInputStream(json))) {
			final JsonObject openFileReponse = reader.readObject();
			final JsonArray jsonArr = openFileReponse.getJsonArray("LogInfo"); //$NON-NLS-1$
			if (jsonArr != null && jsonArr.size() >= 1) {
				final JsonObject logInfoJson = jsonArr.getJsonObject(0);

				// Si se ha definido un error, lo parseamos e ignoramos el resto
				if (logInfoJson.containsKey("error")) {
					final JsonObject errorObject = logInfoJson.getJsonObject("error"); //$NON-NLS-1$
					if (errorObject != null) {
						logInfo.setError(new LogError(
						                              errorObject.getString("code"), //$NON-NLS-1$
						                              errorObject.getString("message"))); //$NON-NLS-1$
					}
				}
				else {
					logInfo.setCharset(logInfoJson.getString("Charset")); //$NON-NLS-1$
					logInfo.setDate(Boolean.parseBoolean(logInfoJson.getString("Date"))); //$NON-NLS-1$
					logInfo.setTime(Boolean.parseBoolean(logInfoJson.getString("Time"))); //$NON-NLS-1$
					logInfo.setDateTimeFormat(logInfoJson.getString("DateTimeFormat")); //$NON-NLS-1$
					logInfo.setLevels(logInfoJson.getString("Levels").split(",")); //$NON-NLS-1$
				}
			}
		}

		return logInfo;
	}
}
