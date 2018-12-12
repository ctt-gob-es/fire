package es.gob.log.consumer.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
 * Cliente para la consulta de logs en un servidor remoto.
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
	 * @throws IOException Cuando ocurre un error en la conexi&oacute;n o comunicaci&oacute;n
	 * con el servidor de logs.
	 * @throws SecurityException Cuando se rechaza la conexi&oacute;n con el servidor.
	 */
	public void init(final String url, final String keyB64) throws IOException, SecurityException {

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

		if(response.statusCode == 200 && response.getContent().length > 0) {
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
		if (response.statusCode == 200 && response.getContent().length > 0) {
			boolean logged = false;
			try (final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()));) {
				final JsonObject loginReponse = reader.readObject();
				logged = loginReponse.getBoolean(ResponseParams.PARAM_RESULT);
			}
			if (!logged) {
				throw new SecurityException("El servidor nego el acceso al usuario"); //$NON-NLS-1$
			}
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
	public String echo(final String serverUrl) throws IOException  {
		final StringWriter result = new StringWriter();
		if (serverUrl == null) {
			throw new NullPointerException("No se ha indicado la URL de redireccion"); //$NON-NLS-1$
		}

		final StringBuilder requestUrl = new StringBuilder(serverUrl)
				.append("?op=").append(ServiceOperations.ECHO.ordinal()); //$NON-NLS-1$

		HttpResponse response;
			response = this.conn.readUrl(requestUrl.toString(), HttpManager.UrlHttpMethod.GET);

			if(response.statusCode == 200) {

				final byte[] resEcho = response.getContent();
				final String res = new String(resEcho, getCharsetContent());
				final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
				final JsonArrayBuilder data = Json.createArrayBuilder();
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message", res)); //$NON-NLS-1$
				jsonObj.add("Ok",data ); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
				jw.writeObject(jsonObj.build());
				jw.close();

			}
			else {

				//final byte[] resEcho = response.getContent();
				final String res = new String("No se ha podido conectar a la ruta indicada."); //$NON-NLS-1$
				//result.append(res);
				final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
				final JsonArrayBuilder data = Json.createArrayBuilder();
				data.add(Json.createObjectBuilder()
					.add("Code",response.statusCode) //$NON-NLS-1$
					.add("Message", res)); //$NON-NLS-1$
				jsonObj.add("Error", data); //$NON-NLS-1$
				try(final JsonWriter jw = Json.createWriter(result);){
					jw.writeObject(jsonObj.build());
					jw.close();
				}
			}


		if(result.getBuffer().length() > 0) {
			return result.toString();
		}
		return null;
	}

	/**
	 * Obtenemos un listado de ficheros del servidor central. La respuesta se transmite en
	 * forma de estructura JSON.<br>
	 * En caso de &eacute;xito:<br>
	 * {"FileList":[{"Name":"nombre_ficghero.log","Date":datetime long format,"Size":bytes long format},etc...]}<br>
	 * En caso de error:<br>
	 * {"Error":[{"Code":204,"Message":"No se ha podido obtener la lista de ficheros log."}]}
	 * @return Cadena de bytes con formato JSON.
	 * @throws IOException Cuando se produce un error durante la conexi&oacute;n.
	 */
	public byte[] getLogFiles() throws IOException {
		final StringWriter result = new StringWriter();
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
					.append("?op=").append(ServiceOperations.GET_LOG_FILES.ordinal() ); //$NON-NLS-1$
		final HttpResponse response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

		if (response.statusCode == 200) {
			try(final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()));){
				final JsonObject listFilesReponse = reader.readObject();
				reader.close();
				result.write(listFilesReponse.toString());
			}
		}
		else {
			final byte[] resLogfiles = response.getContent();
			final String res = new String(resLogfiles, getCharsetContent());
			result.append(res);

			final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
			final JsonArrayBuilder data = Json.createArrayBuilder();
			data.add(Json.createObjectBuilder()
				.add("Code", response.statusCode) //$NON-NLS-1$
				.add("Message", getLegibleErrorMessage(result.toString()))); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			try(final JsonWriter jw = Json.createWriter(result);){
				jw.writeObject(jsonObj.build());
				jw.close();
			}
		}
		if (result.getBuffer().length() > 0) {
			return result.toString().getBytes();
		}
		return null;
	}

	/**
	 * Funci&oacute;n que se encarga de abrir el fichero log indicado como par&aacute;metro.
	 * Al mismo tiempo busca el fichero de configuraci&oacute;n loginfo, que pertenezca a dicho
	 * fichero log y obtiene los datos indicados para manejar el fichero adecuadamente. El
	 * resultado se devueve en forma
	 * @param filename Nombre del fichero de log que se desea abrir.
	 * @return Cadena de bytes con formato JSON. En caso de exito por ejemplo:{"LogInfo":[{"Charset":"UTF-8","Levels":"INFORMACI&Oacute;N,ADVERTENCIA,GRAVE","Date":"true","Time":"true","DateTimeFormat":"MMM dd, yyyy hh:mm:ss a"}]}
	 * En caso de error:{"Error":[{"Code":204,"Message":"No se ha podido abrir el fichero: filename"}]}
	 *@throws IOException Cuando no se pueda conectar con el servicio.
	 */
	public byte[] openFile(final String filename) throws IOException {
		final StringWriter result = new StringWriter();
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.OPEN_FILE.ordinal()).append("&fname=").append(filename); //$NON-NLS-1$ //$NON-NLS-2$
		HttpResponse response;
		response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		if (response.statusCode == 200) {
			try(final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()));){
				final JsonObject openFileReponse = reader.readObject();
				reader.close();
				final JsonArray jsonarr =   openFileReponse.getJsonArray("LogInfo"); //$NON-NLS-1$
				for(int i = 0; i < jsonarr.size(); i++) {
					final JsonObject obj = jsonarr.getJsonObject(i);
					if(obj.get("Charset")!=null) { //$NON-NLS-1$
						final String charsetName = obj.get("Charset").toString().replace("\"", "");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setCharsetContent(Charset.forName(charsetName));
					}
				}
				result.write(openFileReponse.toString());
			}
		}
		else {

			final byte[] resOpenFile = response.getContent();
			final String res = getLegibleErrorMessage(new String(resOpenFile, getCharsetContent()));
			//result.append(res);
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
			final JsonArrayBuilder data = Json.createArrayBuilder();
			data.add(Json.createObjectBuilder()
				.add("Code",response.statusCode) //$NON-NLS-1$
				.add("Message",  res)); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			try(final JsonWriter jw = Json.createWriter(result);){
				jw.writeObject(jsonObj.build());
				jw.close();
			}
		}

		if(result.getBuffer().length() > 0) {
			return result.toString().getBytes(getCharsetContent());
		}

		return null;
	}

	/**
	 *Funci&oacute;n que cierra el fichero y borra variables ( Channel, Reader, LogInfo y FilePosition ) de sesi&oacute;n en servidor de logs.
	 * @return Cadena de bytes con formato JSON. En caso de exito :{"OK":[{"Code":200,"Message":"Fichero cerrado correctamente"}]}
	 * En caso de error:{"Error":[{"Code":204,"Message":"Error al cerrar fichero log"}]}
	 * @throws IOException
	 */
	public byte[] closeFile() throws IOException {

		final StringWriter result = new StringWriter();
		try {
			final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
					.append("?op=").append(ServiceOperations.CLOSE_FILE.ordinal()); //$NON-NLS-1$

			final HttpResponse response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
			if( response.statusCode == 200) {
				final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
				final JsonArrayBuilder data = Json.createArrayBuilder();
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message", "Fichero cerrado correctamente")); //$NON-NLS-1$ //$NON-NLS-2$
				jsonObj.add("OK", data); //$NON-NLS-1$
				try(final JsonWriter jw = Json.createWriter(result);){
					jw.writeObject(jsonObj.build());
					jw.close();
				}

			}
			else {
				final byte[] resCloseFile = response.getContent();
				final String res = new String(resCloseFile, getCharsetContent());
				result.append(res);
				final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
				final JsonArrayBuilder data = Json.createArrayBuilder();
				data.add(Json.createObjectBuilder()
					.add("Code",response.statusCode) //$NON-NLS-1$
					.add("Message",  getLegibleErrorMessage(result.toString()))); //$NON-NLS-1$
				jsonObj.add("Error", data); //$NON-NLS-1$
				try(final JsonWriter jw = Json.createWriter(result);){
					jw.writeObject(jsonObj.build());
					jw.close();
				}
			}

		}
		catch (final Exception e) {
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
			final JsonArrayBuilder data = Json.createArrayBuilder();
			data.add(Json.createObjectBuilder()
					.add("Code",404) //$NON-NLS-1$
					.add("Message", "Error al cerrar fichero log:" + e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
			jsonObj.add("Error", data); //$NON-NLS-1$
			try(final JsonWriter jw = Json.createWriter(result);){
				jw.writeObject(jsonObj.build());
				jw.close();
			}
		}

		return result.toString().getBytes(getCharsetContent());
	}

	/**
	 * Obtiene del fichero las &uacute;ltimas l&iacute;neas del fichero.
	 * @param numLines N&uacute:mero de l&iacute;neas a obtener.
	 * @param filename Nombre del fichero.
	 * @return &Uacute;ltimas lineas del fichero en bytes.
	 */
	public  byte[] getLogTail(final int numLines, final String filename) {
		final StringWriter result = new StringWriter();
		final StringBuilder resultTail = new StringBuilder();
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
				final String res = new String(resTail, getCharsetContent());
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
				final byte[] resTail = response.getContent();
				final String res = new String(resTail, getCharsetContent());
				result.append(res);
				data.add(Json.createObjectBuilder()
					.add("Code",response.statusCode) //$NON-NLS-1$
					.add("Message",  getLegibleErrorMessage(result.toString()))); //$NON-NLS-1$
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

		if(result.getBuffer().length() == 0) {
			LOGGER.warning("No se obtuvo nada del log"); //$NON-NLS-1$
			return null;
		}
		return result.toString().getBytes(getCharsetContent());
	}

	/**
	 *
	 * @param numLines
	 * @param filename
	 * @return
	 */
	public byte[] getMoreLog(final int numLines, final String filename) {
		final StringWriter result = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringBuilder resultMore = new StringBuilder("");//$NON-NLS-1$
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.GET_MORE.ordinal()) //$NON-NLS-1$
				.append("&nlines=").append(numLines) //$NON-NLS-1$
				.append("&fname=").append(filename);  //$NON-NLS-1$
		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			if(response.statusCode == 200 && response.getContent().length > 0) {
				final byte[] resMore = response.getContent();
				final String res = new String(resMore, getCharsetContent());
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
				final byte[] resMore = response.getContent();
				final String res = new String(resMore, getCharsetContent());
				resultMore.append(res);
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message", getLegibleErrorMessage(resultMore.toString()))); //$NON-NLS-1$
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
			return result.toString().getBytes(getCharsetContent());
		}
		return null;
	}

	/**
	 * @param numLines
	 * @param startDate
	 * @param endDate
	 * @param level
	 * @param reset
	 * @return
	 */
	public byte[] getLogFiltered(final int numLines, final long startDate, final long endDate, final String level, final boolean reset) {

		final StringWriter result = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringBuilder resultFilter = new StringBuilder("");//$NON-NLS-1$
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.FILTER.ordinal()) //$NON-NLS-1$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.START_DATETIME).append("=").append(startDate)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.END_DATETIME).append("=").append(endDate)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LEVEL).append("=").append(level)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").append(reset); //$NON-NLS-1$ //$NON-NLS-2$
		HttpResponse response;
		try {

			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			if(response.statusCode == 200 && response.getContent().length > 0) {
				final byte[] resFilter = response.getContent();
				final String res = new String(resFilter, getCharsetContent());
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
				final byte[] resFilter = response.getContent();
				final String res = new String(resFilter, getCharsetContent());
				resultFilter.append(res);
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message", getLegibleErrorMessage(resultFilter.toString()))); //$NON-NLS-1$
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
			return result.toString().getBytes(getCharsetContent());
		}
		return null;
	}

	/**
	 * @param numLines
	 * @param text
	 * @param startDate
	 * @param reset
	 * @return
	 */
	public byte[] searchText(final int numLines, final String text, final String startDate, final boolean reset) {
		final StringWriter result = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringBuilder resultSearch = new StringBuilder("");//$NON-NLS-1$
		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.SEARCH_TEXT.ordinal()) //$NON-NLS-1$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.SEARCH_TEXT).append("=").append(text.replaceAll(" ", "%20"))//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				.append("&").append(ServiceParams.SEARCH_DATETIME).append("=").append(startDate) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").append(reset); //$NON-NLS-1$ //$NON-NLS-2$
		HttpResponse response = null;

		try {

			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

			if(response.statusCode == 200 && response.getContent().length > 0) {
				final byte[] resSearch = response.getContent();
				final String res = new String(resSearch, getCharsetContent());
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
				// Los mensaje de error son enviados directamente por el servidor, asi que no dependen
				// de la codificacion del fichero que se procesa. Siempre se usara UTF-8.
				final byte[] resSearch = response.getContent();
				final String res = new String(resSearch, StandardCharsets.UTF_8);
				resultSearch.append(res);
				data.add(Json.createObjectBuilder()
						.add("Code",response.statusCode) //$NON-NLS-1$
						.add("Message", getLegibleErrorMessage(resultSearch.toString()))); //$NON-NLS-1$
				jsonObj.add("Error",data ); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(result);
			    jw.writeObject(jsonObj.build());
			    jw.close();
			}

		}
		catch (final IOException e) {
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
			return result.toString().getBytes(getCharsetContent());
		}
		return null;
	}

	/**
	 * Descarga el fichero indicado.
	 * @param fileName Nombre del fichero.
	 * @param reset
	 * @param pathDownloadTemp
	 * @return
	 */
	public byte[] download(final String fileName, final boolean reset, final String pathDownloadTemp ) {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringBuilder resultDownload = new StringBuilder("");//$NON-NLS-1$

		final ByteArrayOutputStream result = new ByteArrayOutputStream();
		StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.DOWNLOAD.ordinal()) //$NON-NLS-1$
				.append("&").append(ServiceParams.LOG_FILE_NAME).append("=").append(fileName)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").append(reset); //$NON-NLS-1$ //$NON-NLS-2$;

		int status = 200;
		try (final FileOutputStream fos = new FileOutputStream(new File(pathDownloadTemp, fileName + ".zip"));)  { //$NON-NLS-1$
			int cont = 0;
			do {
				final HttpResponse response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
				cont ++;
				if (response.getContent().length > 0) {
					final byte[] fragment = response.getContent();
					fos.write(fragment);
				}
				if (cont > 0) {
					urlBuilder = new StringBuilder(this.serviceUrl)
					.append("?op=").append(ServiceOperations.DOWNLOAD.ordinal()) //$NON-NLS-1$
					.append("&").append(ServiceParams.LOG_FILE_NAME).append("=").append(fileName)//$NON-NLS-1$ //$NON-NLS-2$
					.append("&").append(ServiceParams.PARAM_RESET).append("=").append("false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$;
				}
				status = response.statusCode;
			} while(status == 206);
			fos.close();
		}
		catch (final IOException e) {
			resultDownload.append("Error la bajar el fichero"); //$NON-NLS-1$
			data.add(Json.createObjectBuilder()
					.add("Code",400) //$NON-NLS-1$
					.add("Message", resultDownload.toString())); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		final File ficherozip = new File(pathDownloadTemp + fileName + ".zip"); //$NON-NLS-1$
		if (ficherozip.exists()) {
			resultDownload.append(pathDownloadTemp + fileName + ".zip"); //$NON-NLS-1$
			data.add(Json.createObjectBuilder()
					.add("Code",200) //$NON-NLS-1$
					.add("Path", resultDownload.toString()) //$NON-NLS-1$
					.add("Message","Fichero " + fileName + ".zip, bajado correctamente")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			jsonObj.add("Ok", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		else {
			resultDownload.append("Error al guardar el fichero"); //$NON-NLS-1$
			data.add(Json.createObjectBuilder()
					.add("Code",400) //$NON-NLS-1$
					.add("Message", resultDownload.toString())); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(result);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		if(result.size() > 0) {
			return result.toByteArray();
		}
		return null;
	}

	/**
	 * Obtiene el juego de caracteres establecido para el fichero abierto.
	 * @return Juego de caracteres.
	 */
	public final Charset getCharsetContent() {
		return this.charsetContent;
	}
	private final void setCharsetContent(final Charset charsetContent) {
		this.charsetContent = charsetContent;
	}

	/**
	 * Obtiene un mensaje de error legible a partir del mensaje proporcionado por el servidor.
	 * @param message Mensaje de error.
	 * @return Mensaje de error legible.
	 */
	private static String getLegibleErrorMessage(final String message) {
		LOGGER.warning("Mensaje de error:\n" + message); //$NON-NLS-1$
		return message;
	}
}
