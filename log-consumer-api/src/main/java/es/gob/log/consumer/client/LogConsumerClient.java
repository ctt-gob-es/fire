package es.gob.log.consumer.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import es.gob.log.consumer.client.HttpManager.UrlHttpMethod;

/**
 * Cliente para la consulta de logs.
 */
public class LogConsumerClient {

	private static final Logger LOGGER = Logger.getLogger(LogConsumerClient.class.getName());

	private String serviceUrl = null;

	private final HttpManager conn;

	private  Charset charsetContent = StandardCharsets.UTF_8;




	/**
	 * Construye el cliente para la consulta de logs.
	 */
	public LogConsumerClient() {
		this.conn = new HttpManager();
	}

	/**
	 * Inicializa el cliente para la conexi&oacute;n con un servicio de consulta
	 * de logs.
	 * @param url URL del servicio de consulta de logs.
	 * @param keyB64 Clave de inicio de sesion en base 64.
	 * @throws IOException
	 */
	public void init(final String url, final String keyB64) throws IOException {

		if (url == null) {
			throw new NullPointerException("La url del servicio no puede ser nula"); //$NON-NLS-1$
		}

		if (keyB64 == null) {
			throw new NullPointerException("La clave de inicio de sesion no puede ser nula"); //$NON-NLS-1$
		}

		if (this.serviceUrl != null) {
			try {
				closeFile();
			}
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "No se pudo cerrar el fichero abierto", e); //$NON-NLS-1$
			}
		}

		this.serviceUrl = url;



		// Solicitamos login
		StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.REQUEST_LOGIN.ordinal()); //$NON-NLS-1$

		HttpResponse response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

		// Procesamos la respuesta
		byte[] token = null;
		byte[] iv = null;

		try (final JsonReader reader = Json.createReader(
				new ByteArrayInputStream(response.getContent()));) {
			final JsonObject loginReponse = reader.readObject();
			final String tokenEncoded = loginReponse.getString(ResponseParams.PARAM_TOKEN);
			if (tokenEncoded != null) {
				token = Base64.decode(tokenEncoded);
			}
			final String ivEncoded = loginReponse.getString(ResponseParams.PARAM_IV);
			if (ivEncoded != null) {
				iv = Base64.decode(ivEncoded);
			}
		}

		if (token == null || iv == null) {
			throw new IOException("No se ha obtenido el token de autenticacion y la configuracion necesaria"); //$NON-NLS-1$
		}

		// Procesamos el token
		final byte[] cipherKey = Base64.decode(keyB64);
		byte[] cipheredToken;
		try {
			cipheredToken = Cipherer.cipher(token, cipherKey, iv);
		} catch (final GeneralSecurityException e) {
			throw new IOException("No se pudo negociar la sesion con el servidor", e); //$NON-NLS-1$
		}

		// Solicitamos validacion de login
		 urlBuilder = new StringBuilder(this.serviceUrl)
					.append("?op=").append(ServiceOperations.VALIDATE_LOGIN.ordinal()) //$NON-NLS-1$
					.append("&sc=").append(Base64.encode(cipheredToken, true)); //$NON-NLS-1$

		 response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.POST);

		// Procesamos la respuesta
		 boolean logged = false;
		 try (final JsonReader reader = Json.createReader(
					new ByteArrayInputStream(response.getContent()));) {
				final JsonObject loginReponse = reader.readObject();
				logged = loginReponse.getBoolean(ResponseParams.PARAM_RESULT);
			}

		 if (!logged) {
			 throw new IOException("El servidor nego el acceso al usuario"); //$NON-NLS-1$
		 }
	}

	/**
	 * Permite desactivar las comprobaciones que se realizan autom&aacute;ticamente
	 * sobre el certificado SSL servidor.
	 * @param disable {@code true} para desactitivar las comprobaciones de seguridad,
	 * {@code false} en caso contrario.
	 */
	public void setDisableSslChecks(final boolean disable) {
		this.conn.setDisabledSslChecks(disable);
	}

	/**
	 * Indica si se encuentran deshabilitadas las comprobaciones autom&aacute;aticas
	 * sobre los certificados SSL servidor.
	 * @return {@code true} si las comprobaciones de seguridad se encuentran desactivadas,
	 * {@code false} en caso contrario.
	 */
	public boolean isDisabledSslChecks() {
		return this.conn.isDisabledSslChecks();
	}

	/**
	 * Consulta la disponibilidad de un servicio de consulta de logs.
	 * @param serverUrl URL del servicio.
	 * @return Respuesta del servicio al saludo.
	 * @throws IOException Cuando no es posible contactar con el servicio.
	 */
	public String echo(final String serverUrl) throws IOException {

		if (serverUrl == null) {
			throw new NullPointerException("No se ha indicado la URL de redireccion"); //$NON-NLS-1$
		}

		final StringBuilder requestUrl = new StringBuilder(serverUrl)
				.append("?op=").append(ServiceOperations.ECHO.ordinal()); //$NON-NLS-1$

		HttpResponse result;
		try {
			result = this.conn.readUrl(requestUrl.toString(), HttpManager.UrlHttpMethod.GET);
		}
		catch (final IOException e) {
			throw new IOException("No se ha podido conectar con el servicio indicado", e);
		}

		final byte[] response = result.getContent();
		if (response == null) {
			throw new IOException("El servicio no respondio a la peticion realizada"); //$NON-NLS-1$
		}


		return new String(response);
	}

	/**
	 *
	 * @return
	 */
	public byte[] getLogFiles() {
		final StringWriter result = new StringWriter();
		try {

			final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
					.append("?op=").append(ServiceOperations.GET_LOG_FILES.ordinal() ); //$NON-NLS-1$
			final HttpResponse response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			if(response.statusCode == 200) {
				final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()));
				final JsonObject listFilesReponse = reader.readObject();
				reader.close();
				result.write(listFilesReponse.toString());
			}
			else {

				final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
				final JsonArrayBuilder data = Json.createArrayBuilder();
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message", "No existen ficheros con extension .log")); //$NON-NLS-1$ //$NON-NLS-2$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
		        jw.writeObject(jsonObj.build());
		        jw.close();
			}

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result.getBuffer().length() > 0) {
			return result.toString().getBytes();
		}
		return null;

	}

	/**
	 *
	 * @param filename
	 * @return
	 */
	public byte[] openFile(final String filename) {
		final StringWriter result = new StringWriter();
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.OPEN_FILE.ordinal()).append("&fname=").append(filename); //$NON-NLS-1$ //$NON-NLS-2$
		HttpResponse response;
		try {

			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
			if(response.statusCode == 200) {
				final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()));
				final JsonObject openFileReponse = reader.readObject();
				reader.close();
				final JsonArray jsonarr =   openFileReponse.getJsonArray("LogInfo"); //$NON-NLS-1$
				for(int i = 0; i < jsonarr.size(); i++) {
					final JsonObject obj = jsonarr.getJsonObject(i);
					if(obj.get("Charset")!=null) { //$NON-NLS-1$
						final String charsetName = obj.get("Charset").toString().replace("\"", "");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						this.setCharsetContent(Charset.forName(charsetName));
					}
				}

				result.write(openFileReponse.toString());

			}
			else {
				final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
				final JsonArrayBuilder data = Json.createArrayBuilder();
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message", "No existen ficheros con extension .log")); //$NON-NLS-1$ //$NON-NLS-2$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
		        jw.writeObject(jsonObj.build());
		        jw.close();
			}

			if(result.getBuffer().length() > 0) {
				return result.toString().getBytes(this.getCharsetContent());
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	public byte[] closeFile() throws IOException {
		final StringWriter result = new StringWriter();
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.CLOSE_FILE.ordinal()); //$NON-NLS-1$
		HttpResponse response;
		response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		if( response.statusCode == 200) {
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
			final JsonArrayBuilder data = Json.createArrayBuilder();
			data.add(Json.createObjectBuilder()
					.add("Code",response.statusCode) //$NON-NLS-1$
					.add("Message", "Fichero cerrado correctamente")); //$NON-NLS-1$ //$NON-NLS-2$
			jsonObj.add("OK", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
	        jw.writeObject(jsonObj.build());
	        jw.close();
		}
		else {
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
			final JsonArrayBuilder data = Json.createArrayBuilder();
			data.add(Json.createObjectBuilder()
					.add("Code",response.statusCode) //$NON-NLS-1$
					.add("Message", "Error al cerrar fichero log")); //$NON-NLS-1$ //$NON-NLS-2$
			jsonObj.add("Error", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
	        jw.writeObject(jsonObj.build());
	        jw.close();
		}
		if(result.getBuffer().length() > 0) {
			return result.toString().getBytes(this.getCharsetContent());
		}
		return null;
	}

	/**
	 *
	 * @param numLines
	 * @return
	 */
	public  byte[] getLogTail(final int numLines, final String filename) {
		final StringWriter result = new StringWriter();
		final StringBuilder resultTail =new StringBuilder("");//$NON-NLS-1$
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.TAIL.ordinal()) //$NON-NLS-1$
				.append("&nlines=").append(numLines)  //$NON-NLS-1$
				.append("&fname=").append(filename); //$NON-NLS-1$
		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			if(response.statusCode == 200 && response.getContent().length > 0) {
				final byte[] resTail = response.getContent();
				final String res = new String(resTail,this.getCharsetContent());
				resultTail.append(res);

				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Result", resultTail.toString())); //$NON-NLS-1$
				jsonObj.add("Tail",data ); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}
			else {
				resultTail.append("No se han podido obtener datos del fichero log."); //$NON-NLS-1$
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message", resultTail.toString())); //$NON-NLS-1$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}

		} catch (final IOException e) {
			resultTail.append("No se han podido obtener datos del fichero log."); //$NON-NLS-1$
			data.add(Json.createObjectBuilder()
					.add("Code",400) //$NON-NLS-1$
					.add("Message", resultTail.toString())); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		if(result.getBuffer().length() > 0) {
			return result.toString().getBytes(this.getCharsetContent());
		}
		return null;
	}

	public byte[] getMoreLog(final int numLines) {
		final StringWriter result = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringBuilder resultMore = new StringBuilder("");//$NON-NLS-1$
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.GET_MORE.ordinal()) //$NON-NLS-1$
				.append("&nlines=").append(numLines); //$NON-NLS-1$
		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			if(response.statusCode == 200 && response.getContent().length > 0) {
				final byte[] resMore = response.getContent();
				final String res = new String(resMore,this.getCharsetContent());
				resultMore.append(res);
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Result", resultMore.toString())); //$NON-NLS-1$
				jsonObj.add("More",data ); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}
			else {
				resultMore.append("No se han podido obtener datos del fichero log."); //$NON-NLS-1$
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message",resultMore.toString())); //$NON-NLS-1$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}

		} catch (final IOException e) {
			resultMore.append("No se han podido obtener datos del fichero log."); //$NON-NLS-1$
			data.add(Json.createObjectBuilder()
					.add("Code",400) //$NON-NLS-1$
					.add("Message", resultMore.toString())); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		if(result.getBuffer().length() > 0) {
			return result.toString().getBytes(this.getCharsetContent());
		}
		return null;
	}

	public byte[] getLogFiltered(final int numLines, final long startDate, final long endDate, final String level) {

		final StringWriter result = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringBuilder resultFilter = new StringBuilder("");//$NON-NLS-1$
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.FILTER.ordinal()) //$NON-NLS-1$
				.append("&".concat(ServiceParams.NUM_LINES).concat("=")).append(numLines)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&".concat(ServiceParams.START_DATETIME).concat("=")).append(startDate)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&".concat(ServiceParams.END_DATETIME).concat("=")).append(endDate)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&".concat(ServiceParams.LEVEL).concat("=")).append(level)//$NON-NLS-1$ //$NON-NLS-2$
				;
		HttpResponse response;
		try {

			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			if(response.statusCode == 200 && response.getContent().length > 0) {
				final byte[] resFilter = response.getContent();
				final String res = new String(resFilter,this.getCharsetContent());
				resultFilter.append(res);
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Result", resultFilter.toString())); //$NON-NLS-1$
				jsonObj.add("Filtered",data ); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}
			else {
				resultFilter.append("No se han podido obtener datos del fichero log."); //$NON-NLS-1$
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message",resultFilter.toString())); //$NON-NLS-1$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}

		} catch (final IOException e) {
			resultFilter.append("No se han podido obtener datos del fichero log."); //$NON-NLS-1$
			data.add(Json.createObjectBuilder()
					.add("Code",400) //$NON-NLS-1$
					.add("Message", resultFilter.toString())); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		if(result.getBuffer().length() > 0) {
			return result.toString().getBytes(this.getCharsetContent());
		}
		return null;
	}

	public byte[] searchText(final int numLines, final String text, final String startDate) {
		final StringWriter result = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringBuilder resultSearch = new StringBuilder("");//$NON-NLS-1$
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.SEARCH_TEXT.ordinal()) //$NON-NLS-1$
				.append("&".concat(ServiceParams.NUM_LINES).concat("=")).append(numLines)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&".concat(ServiceParams.SEARCH_TEXT).concat("=")).append(text.replaceAll(" ", "%20") )//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				.append("&".concat(ServiceParams.SEARCH_DATETIME).concat("=")).append(startDate); //$NON-NLS-1$ //$NON-NLS-2$
		HttpResponse response;
		try {

			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			if(response.statusCode == 200 && response.getContent().length > 0) {
				final byte[] resSearch = response.getContent();
				final String res = new String(resSearch,this.getCharsetContent());
				resultSearch.append(res);
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Result", resultSearch.toString())); //$NON-NLS-1$
				jsonObj.add("Search",data ); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}
			else {
				resultSearch.append("No se han podido obtener datos del fichero log."); //$NON-NLS-1$
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message",resultSearch.toString())); //$NON-NLS-1$
				jsonObj.add("Error", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}

		} catch (final IOException e) {
			resultSearch.append("No se han podido obtener datos del fichero log."); //$NON-NLS-1$
			data.add(Json.createObjectBuilder()
					.add("Code",400) //$NON-NLS-1$
					.add("Message", resultSearch.toString())); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		if(result.getBuffer().length() > 0) {
			return result.toString().getBytes(this.getCharsetContent());
		}
		return null;
	}

	public byte[] download(final String fileName) {
		final ByteArrayOutputStream result = new ByteArrayOutputStream();
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.DOWNLOAD.ordinal()) //$NON-NLS-1$
				.append("&".concat(ServiceParams.LOG_FILE_NAME).concat("=")).append(fileName);//$NON-NLS-1$ //$NON-NLS-2$
		HttpResponse response;
		//(FileOutputStream fos = new FileOutputStream("C:/Users/adolfo.navarro/Desktop/salida.zip"))
		try  {
			do {
				response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
				final byte[] fragment = response.getContent();
				result.write(fragment);
				//fos.write(fragment);
			}while(response.statusCode == 206);
		} catch (final IOException e) {

		}

		if(result.size() > 0) {
			return result.toByteArray();
		}
		return null;
	}

	public final Charset getCharsetContent() {
		return this.charsetContent;
	}
	private final void setCharsetContent(final Charset charsetContent) {
		this.charsetContent = charsetContent;
	}


}
