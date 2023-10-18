package es.gob.fire.client;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class ErrorResult {

	private final int code;
	private final String message;

	public ErrorResult(final int code, final String message) {
		this.code = code;
		this.message = message;
	}

	public static ErrorResult parse(final byte[] json) throws IllegalArgumentException {
		if (json == null) {
			throw new IllegalArgumentException(
					"El JSON de definicion no puede ser nulo"); //$NON-NLS-1$
		}
 
		
		JsonReader jsonReader = null;
		ErrorResult result;
		try {
			jsonReader = Json.createReader(new ByteArrayInputStream(json));
			final JsonObject jsonObject = jsonReader.readObject();
			result = parse(jsonObject);
		}
		catch (final IllegalArgumentException e) {
			throw e;
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("La respuesta del servidor no esta bien formada", e); //$NON-NLS-1$
		} finally {
			if (jsonReader != null) {
				jsonReader.close();
			}
		}

		jsonReader.close();

		return result;
	}

	public static ErrorResult parse(final JsonObject jsonObject) throws IllegalArgumentException {
		if (jsonObject == null) {
            throw new IllegalArgumentException(
                    "El JSON de definicion no puede ser nulo" //$NON-NLS-1$
            );
        }

        int code;
        try {
        	code = jsonObject.getInt("c"); //$NON-NLS-1$
        } catch (final Exception e) {
        	throw new IllegalArgumentException(
        			"Es obligatorio que el JSON contenga el codigo del error" //$NON-NLS-1$
        			);
        }

        String message;
        try {
        	message = jsonObject.getString("m"); //$NON-NLS-1$
        } catch (final Exception e) {
        	throw new IllegalArgumentException(
        			"Es obligatorio que el JSON contenga el mensaje del error" //$NON-NLS-1$
        			);
        }

        return new ErrorResult(code, message);
	}

	public int getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}
}
