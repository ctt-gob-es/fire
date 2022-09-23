package es.gob.fire.server.services.batch;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;

public class TriphaseDataParser {

	/**
	 * Carga datos de firma trif&aacute;sica.
	 * @param json Estructura de datos con la informacion de firma trif&aacute;sica.
	 * @return Informaci&oacute;n de firma trif&aacute;sica.
	 */
	public static TriphaseData parseFromJSON(final byte[] json) {

		JsonObject jsonObject = null;

        try (final JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(json));) {
        	jsonObject = jsonReader.readObject();
        }

		return parseFromJSON(jsonObject);
	}

	/**
	 * Carga datos de firma trif&aacute;sica.
	 * @param jsonObject Objeto JSON con los datos a transformar.
	 * @return Informaci&oacute;n de firma trif&aacute;sica.
	 */
	public static TriphaseData parseFromJSON(final JsonObject jsonObject) {
		JsonArray signsArray = null;
		if (jsonObject.containsKey("signs")) { //$NON-NLS-1$
			signsArray = jsonObject.getJsonArray("signs"); //$NON-NLS-1$
		}

		String format = null;
		if (jsonObject.containsKey("format")) { //$NON-NLS-1$
			format = jsonObject.getString("format"); //$NON-NLS-1$
		}

		final List<TriSign> triSigns = new ArrayList<>();

		if (signsArray != null) {
			for (int i = 0 ; i < signsArray.size() ; i++) {
				final JsonObject sign = signsArray.getJsonObject(i);
				final JsonArray signInfo = sign.getJsonArray("signinfo"); //$NON-NLS-1$

				for (int j = 0; j < signInfo.size(); j++) {
					final String id = signInfo.getJsonObject(j).getString("id"); //$NON-NLS-1$
					final JsonObject params = signInfo.getJsonObject(j).getJsonObject("params"); //$NON-NLS-1$

					triSigns.add(new TriSign(parseParamsJSON(params), id));
				}
			}
		} else {
			final JsonArray signInfoArray = jsonObject.getJsonArray("signinfo"); //$NON-NLS-1$
			for (int i = 0 ; i < signInfoArray.size() ; i++) {
				final String id = signInfoArray.getJsonObject(i).getString("id"); //$NON-NLS-1$
				final JsonObject params = signInfoArray.getJsonObject(i).getJsonObject("params"); //$NON-NLS-1$
				triSigns.add(new TriSign(parseParamsJSON(params),id));
			}
		}
		return new TriphaseData(triSigns,format);
	}

	/**
	 * Mapea los par&aacute;metros de las firmas.
	 * @param params par&aacute;metros a parsear.
	 * @return par&aacute;metros mapeados.
	 */
	private static Map<String, String> parseParamsJSON(final JsonObject params){

		final Map<String, String> paramsResult = new ConcurrentHashMap<>();

		for (final String key : params.keySet()) {
			final String value = params.getString(key);
			paramsResult.put(key, value);
		}

		return paramsResult;
	}

	/**
	 * Genera un JSON con la descripci&oacute;n del mensaje trif&aacute;sico.
	 * @param td objeto con los datos a generar.
	 * @return JSON con la descripci&oacute;n.
	 * */
	public static JsonObject triphaseDataToJson(final TriphaseData td) {

		final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add("format", td.getFormat() != null ? td.getFormat() : ""); //$NON-NLS-1$ //$NON-NLS-2$

		final JsonArrayBuilder signInfosArrayBuilder = Json.createArrayBuilder();

		final Iterator<TriSign> firmasIt = td.getTriSigns().iterator();
		while (firmasIt.hasNext()) {
			final TriSign signConfig = firmasIt.next();

			final JsonObjectBuilder signInfoObjectBuilder = Json.createObjectBuilder();

			// Agrefamos el identificador
			if (signConfig.getId() != null) {
				signInfoObjectBuilder.add("id", signConfig.getId()); //$NON-NLS-1$
			}

			// Agregamos los parametros de la firma trifasica
			final JsonObjectBuilder paramsObjectBuilder = Json.createObjectBuilder();
			final Iterator<String> firmaIt = signConfig.getDict().keySet().iterator();
			while (firmaIt.hasNext()) {
				final String p = firmaIt.next();
				paramsObjectBuilder.add(p, signConfig.getProperty(p));
			}
			signInfoObjectBuilder.add("params", paramsObjectBuilder.build()); //$NON-NLS-1$

			signInfosArrayBuilder.add(signInfoObjectBuilder);
		}

		jsonObjectBuilder.add("signinfo", signInfosArrayBuilder.build()); //$NON-NLS-1$

		return jsonObjectBuilder.build();
	}

}
