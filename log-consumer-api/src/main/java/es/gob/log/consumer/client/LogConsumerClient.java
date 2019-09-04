package es.gob.log.consumer.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
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
 * Cliente para la consulta de logs remotos.
 */
public class LogConsumerClient {

	private static final Logger LOGGER = Logger.getLogger(LogConsumerClient.class.getName());

	/** Status code devuelto en las conexiones HTTP para indicar &eacute;xito en la
	 * operaci&oacute;n. */
	private static final int STATUSCODE_OK = 200;

	/** Status code que indica que la respuesta es correcta pero que a&uacute;n quedan datos
	 * por enviar. */
	private static final int STATUSCODE_PARTIAL_CONTENT = 206;

	/** Status code que indica que no se encuentran m&aacute;s l&iacute;neas en el fichero. */
	private static final int STATUSCODE_NO_CONTENT = 204;

	/** Status code personalizado que proporciona el servidor de log cuando quiere notificar
	 * un error interno controlado. */
	private static final int STATUSCODE_CONTROLLED_ERROR = 220;

	private String serviceUrl = null;

	private final HttpManager conn;

	private Charset charsetContent = StandardCharsets.UTF_8;

	/**
	 * Construye el cliente para la consulta de logs.
	 * @param disableSslChecks Deshabilita las comprobaciones sobre el certificado
	 * SSL servidor.
	 */
	public LogConsumerClient() {
		this.conn = new HttpManager();

	}

	/**
	 * Configura el almace&acute;n de confianza para las conexiones SSL.
	 * @param trustStore Almac&eacute;n de confianza ya inicializado.
	 */
	public void setTrustStore(final KeyStore trustStore) {
		this.conn.setTrustStore(trustStore);
	}

	/**
	 * Inicializa el cliente para la conexi&oacute;n con un servicio de consulta
	 * de logs.
	 * @param serverUrl URL del servicio de consulta de logs.
	 * @param keyB64 Clave de inicio de sesion en base 64.
	 * @throws IOException Cuando no se ha podido iniciar la conexi&oacute;n con el servidor.
	 */
	public void init(final String serverUrl, final String keyB64) throws IOException {

		if (serverUrl == null) {
			throw new NullPointerException("No se indico la URL del servidor"); //$NON-NLS-1$
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

		this.serviceUrl = serverUrl;

		// Solicitamos login
		StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?op=").append(ServiceOperations.REQUEST_LOGIN.getId()); //$NON-NLS-1$

		HttpResponse response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);

		// Procesamos la respuesta
		byte[] token = null;
		byte[] iv = null;

		if (response.statusCode == STATUSCODE_OK && response.getContent().length > 0) {
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
				.append("?op=").append(ServiceOperations.VALIDATE_LOGIN.getId()) //$NON-NLS-1$
				.append("&sc=").append(Base64.encode(cipheredToken, true)); //$NON-NLS-1$

		response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.POST);

		// Procesamos la respuesta
		boolean logged = false;

		if (response.statusCode == STATUSCODE_OK && response.getContent().length > 0) {
			try (final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()));) {
				final JsonObject loginReponse = reader.readObject();
				logged = loginReponse.getBoolean(ResponseParams.PARAM_RESULT);
			}
		}

		if (!logged) {
			throw new IOException("El servidor nego el acceso al usuario"); //$NON-NLS-1$
		}
	}

	/**
	 * Cierra la conexi&oacute;n con un servicio de consulta de logs.
	 */
	public void closeConnection() {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION) //$NON-NLS-1$
				.append("=").append(ServiceOperations.CLOSE_CONNECTION.getId() ); //$NON-NLS-1$

		// Hacemos la peticion y no atendemos al resultado, ya que en caso de error igualmente la
		// sesion habria quedado invalidada
		try {
			this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
		}
	}

	/**
	 * Permite desactivar las comprobaciones que se realizan autom&aacute;ticamente
	 * sobre el certificado SSL servidor.
	 * @param disable {@code true} para desactivar las comprobaciones de seguridad,
	 * {@code false} en caso contrario.
	 */
	public void setDisableSslChecks(final boolean disable) {
		this.conn.setDisabledSslChecks(disable);
	}

	/**
	 * Indica si se encuentran deshabilitadas las comprobaciones autom&aacute;ticas
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
	 */
	public String echo(final String serverUrl) {

		if (serverUrl == null) {
			throw new NullPointerException("No se indico la URL del servidor"); //$NON-NLS-1$
		}

		final StringBuilder requestUrl = new StringBuilder(serverUrl)
				.append("?").append(ServiceParams.OPERATION) //$NON-NLS-1$
				.append("=").append(ServiceOperations.ECHO.getId()); //$NON-NLS-1$

		HttpResponse response;
		try {
			response = this.conn.readUrl(requestUrl.toString(), HttpManager.UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			return createErrorMessage("Error de conexion con el servidor", 0); //$NON-NLS-1$
		}

		String result;

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			final JsonArrayBuilder data = Json.createArrayBuilder()
					.add(Json.createObjectBuilder()
					.add("Code", response.statusCode) //$NON-NLS-1$
					.add("Message", new String(response.getContent(), getCharsetContent()))); //$NON-NLS-1$
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder()
					.add("Ok", data); //$NON-NLS-1$

			final StringWriter resultWriter = new StringWriter();
			try (final JsonWriter jw = Json.createWriter(resultWriter)) {
				jw.writeObject(jsonObj.build());
			}
			result = resultWriter.toString();
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al solicitar un eco: " + new String(response.getContent())); //$NON-NLS-1$
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			result = createErrorMessage(errorParser.getMessage(), errorParser.getStatus());
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result;
	}

	/**
	 * Obtenemos el listado de ficheros del directorio de logs del servidor al que nos hemos
	 * conectado. Se omiten del listado los ficheros loginfo y los de bloqueo.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"FileList":[{"Name":"nombre_ficghero.log","Date":datetime long format,"Size":bytes long format},etc...]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":204,"Message":"No se ha podido obtener la lista de ficheros log."}]}</code>
	 * @return Bytes del JSON con el resultado de la operaci&oacute;n.
	 */
	public byte[] getLogFiles() {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
					.append("?").append(ServiceParams.OPERATION) //$NON-NLS-1$
					.append("=").append(ServiceOperations.GET_LOG_FILES.getId() ); //$NON-NLS-1$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			return createErrorMessage("Error de conexion con el servidor", 0).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		String result;

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {
			final StringWriter resultWriter = new StringWriter();
			try (final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()))) {
				final JsonObject listFilesReponse = reader.readObject();
				resultWriter.write(listFilesReponse.toString());
			}
			result = resultWriter.toString();
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al listar los ficheros de log: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			result = createErrorMessage(errorParser.getMessage(), errorParser.getStatus());
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result.getBytes(getCharsetContent());
	}

	/**
	 * Abre el fichero de log indicado como par&aacute;metro y busca el fichero de
	 * configuraci&oacute;n loginfo que le corresponda. Obtiene los datos del fichero loginfo
	 * para manejar el fichero de log.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"LogInfo":[{"Charset":"UTF-8","Levels":"INFORMACI&Oacute;N,ADVERTENCIA,GRAVE","Date":"true","Time":"true","DateTimeFormat":"MMM dd, yyyy hh:mm:ss a"}]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":204,"Message":"No se ha podido abrir el fichero: filename"}]}</code>
	 * @param filename Nombre del fichero de log.
	 * @return Bytes del JSON con el resultado de la operaci&oacute;n.
	 */
	public LogInfo openFile(final String filename) {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.OPEN_FILE.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LOG_FILE_NAME).append("=").append(filename); //$NON-NLS-1$ //$NON-NLS-2$

		LogInfo logInfo;

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			logInfo = new LogInfo();
			logInfo.setError(new LogError(LogError.EC_CONNECTION, "Error de conexion con el servidor")); //$NON-NLS-1$
			return logInfo;
		}

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {
			logInfo = ResponseParser.parseLogInfo(response.getContent());
			if (logInfo.getCharset() != null) {
				setCharsetContent(Charset.forName(logInfo.getCharset()));
			}
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al abrir un fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			logInfo = new LogInfo();
			logInfo.setError(new LogError("LOG" + errorParser.getStatus(), errorParser.getMessage())); //$NON-NLS-1$
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			logInfo = new LogInfo();
			logInfo.setError(new LogError("HTTP" + response.statusCode, "Error devuelto por el servidor")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return logInfo;
	}

	/**
	 * Abre el fichero de log indicado como par&aacute;metro y busca el fichero de
	 * configuraci&oacute;n loginfo que le corresponda. Obtiene los datos del fichero loginfo
	 * para manejar el fichero de log.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"LogInfo":[{"Charset":"UTF-8","Levels":"INFORMACI&Oacute;N,ADVERTENCIA,GRAVE","Date":"true","Time":"true","DateTimeFormat":"MMM dd, yyyy hh:mm:ss a"}]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":204,"Message":"No se ha podido abrir el fichero: filename"}]}</code>
	 * @param filename Nombre del fichero de log.
	 * @return Bytes del JSON con el resultado de la operaci&oacute;n.
	 */
	public byte[] openFileJson(final String filename) {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.OPEN_FILE.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LOG_FILE_NAME).append("=").append(filename); //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			return createErrorMessage("Error de conexion con el servidor", 0).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		String result;

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			final StringWriter resultWriter = new StringWriter();
			try (final JsonReader reader = Json.createReader(new ByteArrayInputStream(response.getContent()))) {
				final JsonObject openFileReponse = reader.readObject();
				final JsonArray jsonarr =   openFileReponse.getJsonArray("LogInfo"); //$NON-NLS-1$
				for (int i = 0; i < jsonarr.size(); i++) {
					final JsonObject obj = jsonarr.getJsonObject(i);
					if (obj.get("Charset") != null) { //$NON-NLS-1$
						final String charsetName = obj.get("Charset").toString().replace("\"", "");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						setCharsetContent(Charset.forName(charsetName));
					}
				}
				resultWriter.write(openFileReponse.toString());
			}
			result = resultWriter.toString();
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al abrir un fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			result = createErrorMessage(errorParser.getMessage(), errorParser.getStatus());
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result.getBytes(getCharsetContent());
	}

	/**
	 * Funci&oacute;n que cierra el fichero de logs.<br>
	 * @return Resultado de la operaci&oacute;n.
	 */
	public LogResult closeFile() {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION) //$NON-NLS-1$
				.append("=").append(ServiceOperations.CLOSE_FILE.getId()); //$NON-NLS-1$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			final LogResult result = new LogResult();
			result.setError(new LogError(LogError.EC_CONNECTION, "Error de conexion con el servidor"));
			return result;
		}

		final LogResult result = new LogResult();

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {
			result.setMessage("Fichero cerrado correctamente");
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al cerrar un fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			result.setError(new LogError(LogError.EC_CONTROLLED, errorParser.getMessage()));
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result.setError(new LogError(LogError.EC_UNKNOWN, "No se ha podido conectar con el servicio indicado"));
		}

		return result;
	}

	/**
	 * Funci&oacute;n que cierra el fichero de logs y devuelve un JSON con el resultado.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"OK":[{"Code":200,"Message":"Fichero cerrado correctamente"}]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":204,"Message":"Error al cerrar fichero log"}]}</code>
	 * @return Bytes del JSON con el resultado de la operaci&oacute;n.
	 */
	public byte[] closeFileJson() {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION) //$NON-NLS-1$
				.append("=").append(ServiceOperations.CLOSE_FILE.getId()); //$NON-NLS-1$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			return createErrorMessage("Error de conexion con el servidor", 0).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		String result;

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			final JsonArrayBuilder data = Json.createArrayBuilder()
					.add(Json.createObjectBuilder()
					.add("Code", response.statusCode) //$NON-NLS-1$
					.add("Message", "Fichero cerrado correctamente")); //$NON-NLS-1$ //$NON-NLS-2$
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder()
					.add("OK", data); //$NON-NLS-1$

			final StringWriter resultWriter = new StringWriter();
			try (final JsonWriter jw = Json.createWriter(resultWriter)){
				jw.writeObject(jsonObj.build());
			}
			result = resultWriter.toString();
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al cerrar un fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			result = createErrorMessage(errorParser.getMessage(), errorParser.getStatus());
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result.getBytes(getCharsetContent());
	}

	/**
	 * Funci&oacute;n que obtiene del final del fichero el n&uacute;mero de l&iacute;neas
	 * indicadas por par&aacute;metro.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"Tail":[{"Code":200,"Result":"LOGS..."]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":CODIGO_ERROR,"Message":"MENSAJE_ERROR"}]}</code>
	 * @param numLines N&uacute;mero de l&iacute;neas a recuperar.
	 * @param filename Nombre del fichero de log.
	 * @return Objeto con los bytes de las &ulacute;timas l&iacute;neas del fichero.
	 */
	public LogData getLogTail(final int numLines, final String filename) {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.TAIL.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines)  //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LOG_FILE_NAME).append("=").append(filename); //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			final LogData logData = new LogData();
			logData.setError(new LogError(LogError.EC_CONNECTION, "Error de conexion con el servidor")); //$NON-NLS-1$
			return logData;
		}

		final LogData logData = new LogData();

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			logData.setLog(response.getContent());
			logData.setCharset(getCharsetContent());
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al recuperar las ultimas lineas del fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			logData.setError(new LogError(LogError.EC_CONTROLLED, errorParser.getMessage()));
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			logData.setError(new LogError(LogError.EC_UNKNOWN, "No se ha podido conectar con el servicio indicado"));
		}

		return logData;
	}

	/**
	 * Funci&oacute;n que obtiene del final del fichero el n&uacute;mero de l&iacute;neas
	 * indicadas por par&aacute;metro.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"Tail":[{"Code":200,"Result":"LOGS..."]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":CODIGO_ERROR,"Message":"MENSAJE_ERROR"}]}</code>
	 * @param numLines N&uacute;mero de l&iacute;neas a recuperar.
	 * @param filename Nombre del fichero de log.
	 * @return Bytes del JSON con el resultado de la operaci&oacute;n.
	 */
	public byte[] getLogTailJson(final int numLines, final String filename) {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.TAIL.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines)  //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LOG_FILE_NAME).append("=").append(filename); //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			return createErrorMessage("Error de conexion con el servidor", 0).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		String result;

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			final JsonArrayBuilder data = Json.createArrayBuilder()
					.add(Json.createObjectBuilder()
					.add("Code", response.statusCode) //$NON-NLS-1$
					.add("Result", new String(response.getContent(), getCharsetContent()))); //$NON-NLS-1$
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder()
					.add("Tail", data); //$NON-NLS-1$

			final StringWriter resultWriter = new StringWriter();
			try (final JsonWriter jw = Json.createWriter(resultWriter)) {
				jw.writeObject(jsonObj.build());
			}
			result = resultWriter.toString();
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al recuperar las ultimas lineas del fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			result = createErrorMessage(errorParser.getMessage(), errorParser.getStatus());
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result.getBytes(getCharsetContent());
	}

	/**
	 * Obtiene un objeto con los bytes de las siguientes l&iacute;neas del fichero continuando por
	 * la petici&oacute;n anterior.
	 * @param numLines N&uacute;mero de l&iacute;neas a devolver.
	 * @return Objeto con los bytes de las l&iacute;neas del fichero.
	 */
	public LogData getMoreLog(final int numLines) {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.GET_MORE.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines);  //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			final LogData logData = new LogData();
			logData.setError(new LogError(LogError.EC_CONNECTION, "Error de conexion con el servidor")); //$NON-NLS-1$
			return logData;
		}

		final LogData logData = new LogData();

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			logData.setLog(response.getContent());
			logData.setCharset(getCharsetContent());
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al recuperar los registros adicionales del fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());

			// No se encuentran mas resultados
			if (Integer.parseInt(errorParser.getStatus()) == STATUSCODE_NO_CONTENT) {
				LOGGER.log(Level.WARNING, "No se encuentran mas resultados en el fichero"); //$NON-NLS-1$
				logData.setError(new LogError(LogError.EC_NO_MORE_LINES, errorParser.getMessage()));
			}
			// Cualquier otro error
			else {
				logData.setError(new LogError(LogError.EC_CONTROLLED, errorParser.getMessage()));
			}
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			logData.setError(new LogError(LogError.EC_UNKNOWN, "No se ha podido conectar con el servicio indicado"));
		}

		return logData;
	}

	/**
	 * Obtiene en formato JSON las siguientes l&iacute;neas del fichero continuando por la
	 * petici&oacute;n anterior.
	 * @param numLines N&uacute;mero de l&iacute;neas a devolver.
	 * @param filename Nombre del fichero.
	 * @return Bytes de la estructura JSON con las l&iacute;neas del fichero.
	 */
	public byte[] getMoreLogJson(final int numLines) {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.GET_MORE.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines);  //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			return createErrorMessage("Error de conexion con el servidor", 0).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		String result;

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			final JsonArrayBuilder data = Json.createArrayBuilder()
					.add(Json.createObjectBuilder()
					.add("Code",response.statusCode) //$NON-NLS-1$
					.add("Result", new String(response.getContent(), getCharsetContent()))); //$NON-NLS-1$
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder()
					.add("More", data); //$NON-NLS-1$

			final StringWriter resultWriter = new StringWriter();
			try (final JsonWriter jw = Json.createWriter(resultWriter)) {
				jw.writeObject(jsonObj.build());
			}
			result = resultWriter.toString();
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al solicitar mas logs: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			result = createErrorMessage(errorParser.getMessage(), errorParser.getStatus());
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result.getBytes(getCharsetContent());
	}

	/**
	 * Obtiene un conjunto de registros de log que se ajustan a los criterios establecidos. Se
	 * recuperar&aacute; el m&aacute;ximo de l&iacute;neas de log indicado salvo que limitarse a
	 * ese n&uacute;mero dejase un registro a mitad, en cuyo caso se enviar&iacute;a entero.
	 * @param numLines N&uacute;mero de l&iacute;neas que se deben recuperar como m&aacute;ximo.
	 * @param startDate Fecha/hora m&iacute;nima indicada en el registro.
	 * @param endDate Fecha/hora m&aacute;xima indicada en el registro.
	 * @param level Nivel de traza m&iacute;nimo indicada en el registro.
	 * @param reset Indica si debe cerrarse y volver a abrir el fichero para evitar retomar la lectura desde
	 * un punto equivocado del fichero.
	 * @return Objeto con los bytes de las l&iacute;neas recuperadas del fichero.
	 */
	public LogData getLogFiltered(final int numLines, final long startDate, final long endDate, final String level, final boolean reset) {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.FILTER.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.START_DATETIME).append("=").append(startDate)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.END_DATETIME).append("=").append(endDate)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LEVEL).append("=").append(level)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").append(reset); //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			final LogData logData = new LogData();
			logData.setError(new LogError(LogError.EC_CONNECTION, "Error de conexion con el servidor")); //$NON-NLS-1$
			return logData;
		}

		final LogData logData = new LogData();

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			logData.setLog(response.getContent());
			logData.setCharset(getCharsetContent());
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al recuperar registros filtrados del fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());

			// No se encuentran mas resultados
			if (Integer.parseInt(errorParser.getStatus()) == STATUSCODE_NO_CONTENT) {
				LOGGER.log(Level.WARNING, "No se encuentran mas resultados en el fichero"); //$NON-NLS-1$
				logData.setError(new LogError(LogError.EC_NO_MORE_LINES, errorParser.getMessage()));
			}
			// Cualquier otro error
			else {
				logData.setError(new LogError(LogError.EC_CONTROLLED, errorParser.getMessage()));
			}
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			logData.setError(new LogError(LogError.EC_UNKNOWN, "No se ha podido conectar con el servicio indicado"));
		}

		return logData;
	}

	/**
	 * Obtiene en una estructura JSON un conjunto de registros de log que se ajustan a los criterios
	 * establecidos. Se recuperar&aacute; el m&aacute;ximo de l&iacute;neas de log indicado salvo que
	 * limitarse a ese n&uacute;mero dejase un registro a mitad, en cuyo caso se enviar&iacute;a entero.
	 * @param numLines N&uacute;mero de l&iacute;neas que se deben recuperar como m&aacute;ximo.
	 * @param startDate Fecha/hora m&iacute;nima indicada en el registro.
	 * @param endDate Fecha/hora m&aacute;xima indicada en el registro.
	 * @param level Nivel de traza m&iacute;nimo indicada en el registro.
	 * @param reset Indica si debe cerrarse y volver a abrir el fichero para evitar retomar la lectura desde
	 * un punto equivocado del fichero.
	 * @return Bytes de las l&iacute;neas obtenidas del fichero de log.
	 */
	public byte[] getLogFilteredJson(final int numLines, final long startDate, final long endDate, final String level, final boolean reset) {

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.FILTER.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.START_DATETIME).append("=").append(startDate)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.END_DATETIME).append("=").append(endDate)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LEVEL).append("=").append(level)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").append(reset); //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			return createErrorMessage("Error de conexion con el servidor", 0).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		String result;

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			final JsonArrayBuilder data = Json.createArrayBuilder()
					.add(Json.createObjectBuilder()
					.add("Code", response.statusCode) //$NON-NLS-1$
					.add("Result", new String(response.getContent(), getCharsetContent()))); //$NON-NLS-1$
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder().add("Filtered", data); //$NON-NLS-1$

			final StringWriter resultWriter = new StringWriter();
			try (final JsonWriter jw = Json.createWriter(resultWriter)) {
				jw.writeObject(jsonObj.build());
			}
			result = resultWriter.toString();
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al solicitar logs filtrados: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());

			// No se encuentran mas resultados
			if (Integer.parseInt(errorParser.getStatus()) == STATUSCODE_NO_CONTENT) {
				LOGGER.log(Level.WARNING, "No se encuentran mas resultados en el fichero"); //$NON-NLS-1$
				result = createErrorMessage(errorParser.getMessage(), LogError.EC_NO_MORE_LINES);
			}
			// Cualquier otro error
			else {
				result = createErrorMessage(errorParser.getMessage(), LogError.EC_CONTROLLED);
			}

		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result.getBytes(getCharsetContent());
	}

	/**
	 * Encuentra en un fichero  de log el registro en el que aparece un texto y devuelve dicho registro y
	 * las l&iacute;neas a continuaci&oacute;n del mismo.<br>
	 * @param numLines N&uacute;mero de l&iacute;neas a recuperar.
	 * @param text Texto a buscar.
	 * @param startDate Fecha m&iacute;nima en milisegundos en la que debe haberse impreso el texto.
	 * @param reset Indica si debe cerrarse y volver a abrir el fichero para evitar retomar la lectura desde
	 * un punto equivocado del fichero.
	 * @return Objeto con los bytes del resultado de la operaci&oacute;n.
	 */
	public LogData searchText(final int numLines, final String text, final long startDate, final boolean reset) {

		String textEncoded;
		try {
			textEncoded = URLEncoder.encode(text, StandardCharsets.UTF_8.name());
		}
		catch (final Exception e) {
			// No puede ocurrir nunca
			textEncoded = text;
		}

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.SEARCH_TEXT.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.SEARCH_TEXT).append("=").append(textEncoded) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.SEARCH_DATETIME).append("=").append(startDate) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").append(reset); //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			final LogData logData = new LogData();
			logData.setError(new LogError(LogError.EC_CONNECTION, "Error de conexion con el servidor")); //$NON-NLS-1$
			return logData;
		}

		final LogData logData = new LogData();

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			logData.setLog(response.getContent());
			logData.setCharset(getCharsetContent());
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al recuperar las l&iacute;neas buscadas del fichero: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());

			// No se encuentran mas resultados
			if (Integer.parseInt(errorParser.getStatus()) == STATUSCODE_NO_CONTENT) {
				LOGGER.log(Level.WARNING, "No se encuentran mas resultados en el fichero"); //$NON-NLS-1$
				logData.setError(new LogError(LogError.EC_NO_MORE_LINES, errorParser.getMessage()));
			}
			// Cualquier otro error
			else {
				logData.setError(new LogError(LogError.EC_CONTROLLED, errorParser.getMessage()));
			}
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			logData.setError(new LogError(LogError.EC_UNKNOWN, "No se ha podido conectar con el servicio indicado"));
		}

		return logData;
	}

	/**
	 * Encuentra en un fichero  de log el registro en el que aparece un texto y devuelve dicho registro y
	 * las l&iacute;neas a continuaci&oacute;n del mismo en formato JSON.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"Search":[{"Code":200,"Result":"LOGS..."]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":CODIGO_ERROR,"Message":"MENSAJE_ERROR"}]}</code>
	 * @param numLines N&uacute;mero de l&iacute;neas a recuperar.
	 * @param text Texto a buscar.
	 * @param startDate Fecha m&iacute;nima en milisegundos en la que debe haberse impreso el texto.
	 * @param reset Indica si debe cerrarse y volver a abrir el fichero para evitar retomar la lectura desde
	 * un punto equivocado del fichero.
	 * @return Bytes del JSON con el resultado de la operaci&oacute;n.
	 */
	public byte[] searchTextJson(final int numLines, final String text, final long startDate, final boolean reset) {

		String textEncoded;
		try {
			textEncoded = URLEncoder.encode(text, StandardCharsets.UTF_8.name());
		}
		catch (final Exception e) {
			// No puede ocurrir nunca
			textEncoded = text;
		}

		final StringBuilder urlBuilder = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.SEARCH_TEXT.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.NUM_LINES).append("=").append(numLines)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.SEARCH_TEXT).append("=").append(textEncoded) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.SEARCH_DATETIME).append("=").append(startDate) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").append(reset); //$NON-NLS-1$ //$NON-NLS-2$

		HttpResponse response;
		try {
			response = this.conn.readUrl(urlBuilder.toString(), UrlHttpMethod.GET);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en la conexion con el servidor de logs", e); //$NON-NLS-1$
			return createErrorMessage("Error de conexion con el servidor", 0).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		String result;

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			final JsonArrayBuilder data = Json.createArrayBuilder()
					.add(Json.createObjectBuilder()
					.add("Code", response.statusCode) //$NON-NLS-1$
					.add("Result", new String(response.getContent(), getCharsetContent()))); //$NON-NLS-1$
			final JsonObjectBuilder jsonObj = Json.createObjectBuilder()
					.add("Search", data); //$NON-NLS-1$

			final StringWriter resultWriter = new StringWriter();
			try (final JsonWriter jw = Json.createWriter(resultWriter)) {
				jw.writeObject(jsonObj.build());
			}
			result = resultWriter.toString();
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al solicitar busqueda de texto: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());

			// No se encuentran mas resultados
			if (Integer.parseInt(errorParser.getStatus()) == STATUSCODE_NO_CONTENT) {
				LOGGER.log(Level.WARNING, "No se encuentran mas resultados en el fichero"); //$NON-NLS-1$
				result = createErrorMessage(errorParser.getMessage(), LogError.EC_NO_MORE_LINES);
			}
			// Cualquier otro error
			else {
				result = createErrorMessage(errorParser.getMessage(), LogError.EC_CONTROLLED);
			}
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result.getBytes(getCharsetContent());
	}

	/**
	 * Descarga un fichero de log a un directorio.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"Ok":[{"Code":200,"Path":"RUTA_ABSOLUTA","Message":"Fichero RUTA_ABSOLUTA bajado correctamente]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":CODIGO_ERROR,"Message":"MENSAJE_ERROR"}]}</code>
	 * @param fileName Nombre del fichero a descargar.
	 * @param reset Indica que reinicie el fichero para asegurar que no se empieza la descarga desde un punto intermedio.
	 * @param downloadDir Directorio al que descargar el fichero.
	 * @return Bytes del JSON con el resultado de la operaci&oacute;n.
	 */
	public DownloadedLogFile download(final String fileName, final String downloadDir) {

		final String serviceUrlPattern = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.DOWNLOAD.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LOG_FILE_NAME).append("=").append(fileName)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").toString(); //$NON-NLS-1$ //$NON-NLS-2$;

		final File outputFile = new File(downloadDir, fileName + ".zip"); //$NON-NLS-1$

		HttpResponse response = null;

		// Hacemos peticiones hasta descargar todo el fichero, asegurandonos de reiniciarlo la primera vez
		try (final FileOutputStream fos = new FileOutputStream(outputFile))  {
			boolean reset;

			do {
				// En la primera llamada reiniciaremos el fichero de log
				reset = response == null;
				response = this.conn.readUrl(serviceUrlPattern + reset, UrlHttpMethod.GET);
				if (response.statusCode == STATUSCODE_OK || response.statusCode == STATUSCODE_PARTIAL_CONTENT) {
					fos.write(response.getContent());
				}
			} while (response.statusCode == STATUSCODE_PARTIAL_CONTENT);
		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado para descargar el fichero de log o no se pudo guardar", e); //$NON-NLS-1$
			return new DownloadedLogFile(new LogError("0", "Error la descargar o guardar el fichero")); //$NON-NLS-1$
		}

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {
			// Si al final del proceso no hay fichero, se interpretara como un error de guardado
			if (!outputFile.exists()) {
				LOGGER.log(Level.SEVERE, "El fichero de log no se ha guardado"); //$NON-NLS-1$
				return new DownloadedLogFile(new LogError(Integer.toString(response.statusCode), "El fichero de log no se ha guardado"));
			}
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al solicitar busqueda de texto: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			return new DownloadedLogFile(new LogError(errorParser.getStatus(), errorParser.getMessage()));
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			return new DownloadedLogFile(new LogError(Integer.toString(response.statusCode), "No se ha podido conectar con el servicio indicado"));
		}

		// Resultado correcto
		return new DownloadedLogFile(outputFile.getAbsolutePath());
	}

	/**
	 * Descarga un fichero de log a un directorio.<br>
	 * En caso de &eacute;xito:<br>
	 * <code>{"Ok":[{"Code":200,"Path":"RUTA_ABSOLUTA","Message":"Fichero RUTA_ABSOLUTA bajado correctamente]}</code><br>
	 * En caso de error:<br>
	 * <code>{"Error":[{"Code":CODIGO_ERROR,"Message":"MENSAJE_ERROR"}]}</code>
	 * @param fileName Nombre del fichero a descargar.
	 * @param reset Indica que reinicie el fichero para asegurar que no se empieza la descarga desde un punto intermedio.
	 * @param downloadDir Directorio al que descargar el fichero.
	 * @return Bytes del JSON con el resultado de la operaci&oacute;n.
	 */
	public byte[] downloadJson(final String fileName, final String downloadDir) {

		final File dir = new File(downloadDir);

		// Si no existe el directorio, lo intentamos crear
		if (!dir.exists()) {
			dir.mkdirs();
		}

		if (!dir.isDirectory() || !dir.canWrite()) {
			LOGGER.log(Level.SEVERE, String.format("El directorio '%1s' no existe o no tiene permisos de escritura", downloadDir)); //$NON-NLS-1$
			return createErrorMessage("Problema en servidor al descargar el fichero", 500).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		final String serviceUrlPattern = new StringBuilder(this.serviceUrl)
				.append("?").append(ServiceParams.OPERATION).append("=").append(ServiceOperations.DOWNLOAD.getId()) //$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.LOG_FILE_NAME).append("=").append(fileName)//$NON-NLS-1$ //$NON-NLS-2$
				.append("&").append(ServiceParams.PARAM_RESET).append("=").toString(); //$NON-NLS-1$ //$NON-NLS-2$;

		final File outputFile = new File(dir, fileName + ".zip"); //$NON-NLS-1$

		final String result;
		HttpResponse response = null;

		// Hacemos peticiones hasta descargar todo el fichero, asegurandonos de reiniciarlo la primera vez
		try (final FileOutputStream fos = new FileOutputStream(outputFile))  {
			boolean reset;

			do {
				// En la primera llamada reiniciaremos el fichero de log
				reset = response == null;
				response = this.conn.readUrl(serviceUrlPattern + reset, UrlHttpMethod.GET);
				if (response.statusCode == STATUSCODE_OK || response.statusCode == STATUSCODE_PARTIAL_CONTENT) {
					fos.write(response.getContent());
				}
			} while (response.statusCode == STATUSCODE_PARTIAL_CONTENT);
		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado para descargar el fichero de log o no se pudo guardar", e); //$NON-NLS-1$
			return createErrorMessage("Error la descargar o guardar el fichero", 0).getBytes(getCharsetContent()); //$NON-NLS-1$
		}

		// La peticion se tramito correctamente
		if (response.statusCode == STATUSCODE_OK) {

			// Si se creo el fichero, interpretamos que el proceso termino correctamente
			if (outputFile.exists()) {
				final JsonArrayBuilder data = Json.createArrayBuilder()
						.add(Json.createObjectBuilder()
								.add("Code", STATUSCODE_OK) //$NON-NLS-1$
								.add("Path", outputFile.getAbsolutePath()) //$NON-NLS-1$
								.add("Message", "Fichero " + outputFile.getName() + " bajado correctamente")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				final JsonObjectBuilder jsonObj = Json.createObjectBuilder()
						.add("Ok", data); //$NON-NLS-1$

				final StringWriter resultWritter = new StringWriter();
				try (final JsonWriter jw = Json.createWriter(resultWritter)) {
					jw.writeObject(jsonObj.build());
				}
				result = resultWritter.toString();
			}
			// Si al final del proceso no hay fichero, se interpretara como un error de guardado
			else {
				LOGGER.log(Level.SEVERE, "El fichero de log no se ha guardado"); //$NON-NLS-1$
				result = createErrorMessage("El fichero de log no se ha guardado", response.statusCode); //$NON-NLS-1$
			}
		}

		// Error controlado devuelto por el servidor
		else if (response.statusCode == STATUSCODE_CONTROLLED_ERROR) {
			LOGGER.log(Level.WARNING, "Mensaje devuelto por el servidor al solicitar busqueda de texto: " + //$NON-NLS-1$
					new String(response.getContent()));
			final ServerErrorParser errorParser = new ServerErrorParser(response.getContent());
			result = createErrorMessage(errorParser.getMessage(), errorParser.getStatus());
		}

		// Error no controlado devuelto por el servidor
		else {
			LOGGER.log(Level.SEVERE, "No se ha podido conectar con el servicio indicado: " + new String(response.getContent())); //$NON-NLS-1$
			result = createErrorMessage("No se ha podido conectar con el servicio indicado", response.statusCode); //$NON-NLS-1$
		}

		return result.getBytes(getCharsetContent());
	}

	/**
	 * Obtiene el juego de caracteres del fichero abierto.
	 * @return Juego de caracteres.
	 */
	public final Charset getCharsetContent() {
		return this.charsetContent;
	}

	private final void setCharsetContent(final Charset charsetContent) {
		this.charsetContent = charsetContent;
	}

	/**
	 * Crea un JSON de error.
	 * @param message Mensaje de error.
	 * @param statusCode C&oacute;digo HTTP de error o {@code 0} si no corresponde.
	 * @return JSON de error.
	 */
	private static String createErrorMessage(final String message, final int statusCode) {
		return createErrorMessage(message, Integer.toString(statusCode));
	}

	/**
	 * Crea un JSON de error.
	 * @param message Mensaje de error.
	 * @param statusCode C&oacute;digo HTTP de error o {@code 0} si no corresponde.
	 * @return JSON de error.
	 */
	private static String createErrorMessage(final String message, final String statusCode) {

		final JsonArrayBuilder data = Json.createArrayBuilder();
		data.add(Json.createObjectBuilder()
				.add("Code", statusCode) //$NON-NLS-1$
				.add("Message", message)); //$NON-NLS-1$
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder()
				.add("Error", data); //$NON-NLS-1$

		final StringWriter responseWriter = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(responseWriter)) {
			jw.writeObject(jsonObj.build());
		}

		return responseWriter.toString();
	}
}
