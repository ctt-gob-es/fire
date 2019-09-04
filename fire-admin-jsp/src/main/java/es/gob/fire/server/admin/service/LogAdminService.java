package es.gob.fire.server.admin.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.conf.ConfigManager;
import es.gob.log.consumer.client.LogConsumerClient;
import es.gob.log.consumer.client.LogInfo;
import es.gob.log.consumer.client.ServiceOperations;

/**
 * Servlet implementation class LogAdminService
 */
public class LogAdminService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(LogAdminService.class.getName());

	private static final String OPERATION_SELECTION = "1"; //$NON-NLS-1$

	/**
     * @see HttpServlet#HttpServlet()
     */
    public LogAdminService() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException  {

		final HttpSession session = request.getSession(false);

		if (session == null) {
			LOGGER.warning("No se iniciado sesion en la aplicacion"); //$NON-NLS-1$
			response.sendRedirect("Login.jsp?login=fail"); //$NON-NLS-1$
			return;
		}

		// Cargamos los parametros de la peticion
		Parameters params;

		try {
			params = loadParameters(request);
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Se recibido parametros no validos", e); //$NON-NLS-1$
			String errorMessage;
			if (e instanceof IllegalArgumentException) {
				errorMessage = e.getMessage();
			}
			else {
				errorMessage = "No se han podido recuperar correctamente los parametros."; //$NON-NLS-1$
			}
			final String jsonError = buildJsonError(errorMessage, HttpServletResponse.SC_BAD_REQUEST);
			response.getOutputStream().write(jsonError.getBytes(StandardCharsets.UTF_8));
			return;
		}

		final String opString = request.getParameter("op"); //$NON-NLS-1$
		if (opString == null) {
			LOGGER.warning("No se ha indicado codigo de operacion"); //$NON-NLS-1$
			final String jsonError = buildJsonError("No se ha indicado codigo de operacion", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			session.setAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON, jsonError);
			response.sendRedirect(getSelectionResultUrl(request, false));
			return;
		}

		// Comprobamos que el c&oacute;digo de operaci&oacute;n sea correcto
		ServiceOperations op;
		try {
			op = checkOperation(opString);
		}
		catch (final Exception e) {
			LOGGER.warning(String.format("Codigo de operacion no soportado (%s). Se rechaza la peticion.", LogUtils.cleanText(opString))); //$NON-NLS-1$
			final String jsonError = buildJsonError(String.format("Codigo de operacion no soportado (%s). Se rechaza la peticion.", opString), HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			session.setAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON, jsonError);
			response.sendRedirect(getSelectionResultUrl(request, false));
			return;
		}

		// Si es una peticion real y no una comprobacion, cargamos el cliente para la consulta de logs
		LogConsumerClient logClient = null;
		if (op.equals(ServiceOperations.ECHO)) {
			logClient = new LogConsumerClient();
			logClient.setDisableSslChecks(!params.isVerifySsl());
		}
		else {
			logClient = (LogConsumerClient) session.getAttribute(ServiceParams.SESSION_ATTR_LOG_CLIENT);
		}

		if (logClient == null) {
			LOGGER.warning("No se pudo recuperar el cliente para el acceso a los logs"); //$NON-NLS-1$
			final String jsonError = buildJsonError("No se pudo recuperar el cliente para el acceso a los logs", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			session.setAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON, jsonError);
			response.sendRedirect(getSelectionResultUrl(request, false));
			return;
		}

		// Selecionamos la operacion a realizar respecto al codigo recibido
		String result;
		switch (op) {
		case ECHO:
			LOGGER.info("Solicitud entrante de comprobacion de servidor"); //$NON-NLS-1$
			result = logClient.echo(params.getUrl());
			response.getWriter().write(result);
			break;

		case GET_LOG_FILES:
			LOGGER.info("Solicitud entrante de listado de ficheros"); //$NON-NLS-1$
			try {
				final byte[] datLogFiles = logClient.getLogFiles();
				if (datLogFiles != null) {
					session.setAttribute(ServiceParams.SESSION_ATTR_JSON, datLogFiles);
				}

				// En caso de exito, redirigimos a LogsFileList para mostrar los ficheros encontrados
				if (params.getMessage() != null && !params.getMessage().isEmpty()) {
					response.sendRedirect("Logs/LogsFileList.jsp?"//$NON-NLS-1$
						.concat(ServiceParams.PARAM_NAMESRV).concat("=").concat(params.getNameSrv()) //$NON-NLS-1$
						.concat("&").concat(ServiceParams.PARAM_MSG).concat("=").concat(params.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}

				// En caso de recibir un error, redirige a LogsMainPage para mostrar el mensaje
				// en caso contrario, redigige a .
				JsonObject jsonObj;
				try (final JsonReader reader = Json.createReader(new ByteArrayInputStream(datLogFiles));) {
					jsonObj = reader.readObject();
				}

				if (jsonObj.getJsonArray("Error") != null) {  //$NON-NLS-1$
					session.setAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON, new String (datLogFiles));
					response.sendRedirect(getSelectionResultUrl(request, false));
					return;
				}
				response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsFileList.jsp?")//$NON-NLS-1$
						.concat(ServiceParams.PARAM_NAMESRV).concat("=").concat(params.getNameSrv())); //$NON-NLS-1$
				return;
			}
			catch (final IOException e) {
				LOGGER.severe("Error al obtener los ficheros log del servidor central: " + e); //$NON-NLS-1$
				final String jsonError = buildJsonError("Error al obtener los ficheros log del servidor central :" , HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
		        try {
					session.setAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON, jsonError);
					response.sendRedirect(getSelectionResultUrl(request, false));
					return;
				} catch (final IOException e1) {
					LOGGER.severe("Error en la respuesta del mensaje:"+ e1.getMessage()); //$NON-NLS-1$
				}
			}
			break;

		case OPEN_FILE:
			LOGGER.info("Solicitud entrante de apertura de fichero"); //$NON-NLS-1$
			final LogInfo datOpenFiles = logClient.openFile(params.getLogFileName());


				if (datOpenFiles.getError() != null) {
					response.sendRedirect("log?op=" + ServiceOperations.GET_LOG_FILES.getId() + //$NON-NLS-1$
							"&" + ServiceParams.PARAM_NAMESRV + "=" + params.getNameSrv() + //$NON-NLS-1$ //$NON-NLS-2$
							"&" + ServiceParams.PARAM_MSG + "=" + datOpenFiles.getError().getMessage() //$NON-NLS-1$ //$NON-NLS-2$
							);
					return;
				}

				session.setAttribute(ServiceParams.SESSION_ATTR_JSON_LOGINFO, datOpenFiles);

				if (!params.isReset()) {
					response.sendRedirect("Logs/LogsManager.jsp?" + //$NON-NLS-1$
						ServiceParams.PARAM_NAMESRV + "=" + params.getNameSrv() + //$NON-NLS-1$
						 "&" + ServiceParams.PARAM_FILENAME + "=" + params.getLogFileName());  //$NON-NLS-1$//$NON-NLS-2$
					return;
				}

				// Devolvemos cadena vacia para que se interprete como un exito
				// y no se imprima nada en la pantalla
				result = ""; //$NON-NLS-1$
				params.setReset(false);


			if (logClient.getCharsetContent() != null){
				response.setCharacterEncoding(logClient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;

		case CLOSE_FILE:
			LOGGER.info("Solicitud entrante de cierre de fichero"); //$NON-NLS-1$
			final byte[] datCloseFiles = logClient.closeFileJson();
			if (datCloseFiles != null && datCloseFiles.length > 0) {
				final String res = new String(datCloseFiles, logClient.getCharsetContent());
				result = res.replace("\\n", "</br>"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				result = buildJsonError("No se obtuvo respuesta del servidor", 500); //$NON-NLS-1$
			}
			if (logClient.getCharsetContent() != null){
				response.setCharacterEncoding(logClient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;

		case TAIL:
			LOGGER.info("Solicitud entrante de consulta del final del log"); //$NON-NLS-1$
			final byte[] datTailFile = logClient.getLogTailJson(params.getNumlines(), params.getLogFileName());
			if(datTailFile != null && datTailFile.length > 0) {
				final String res = new String(datTailFile,logClient.getCharsetContent());
				result = res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
			}
			else {
				result = buildJsonError("No se obtuvo respuesta del servidor", 500); //$NON-NLS-1$
			}

			if (logClient.getCharsetContent() != null) {
				response.setCharacterEncoding(logClient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;
		case GET_MORE:
			LOGGER.info("Solicitud entrante de mas log"); //$NON-NLS-1$
			final byte datMoreFile[] = logClient.getMoreLogJson(params.getNumlines());
			if (datMoreFile != null && datMoreFile.length > 0 ) {
				final String res = new String(datMoreFile,logClient.getCharsetContent());
				result = res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
			}
			else {
				// No hay mas texto
				result = buildJsonError("No se obtuvo respuesta del servidor", 500); //$NON-NLS-1$
			}

			if (logClient.getCharsetContent() != null) {
				response.setCharacterEncoding(logClient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;
		case SEARCH_TEXT:
			LOGGER.info("Solicitud entrante de busqueda de texto"); //$NON-NLS-1$
			final byte[] datSearchTxt = logClient.searchTextJson(params.getNumlines(), params.getTxt2search(), params.getDatetime(), params.isReset());
			if (datSearchTxt != null && datSearchTxt.length > 0 ) {
				final String res = new String(datSearchTxt,logClient.getCharsetContent());
				result = res.replace("\\n", "</br>"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				// No se encontro texto
				result = buildJsonError("No se obtuvo respuesta del servidor", 500); //$NON-NLS-1$
			}

			if(logClient.getCharsetContent() != null){
				response.setCharacterEncoding(logClient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;
		case FILTER:
			LOGGER.info("Solicitud entrante de filtrado de log"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$

			final byte[] datFiltered = logClient.getLogFilteredJson(params.getNumlines(), params.getStartDateTime(), params.getEndDateTime(), params.getLevel(), params.isReset());
			if(datFiltered != null && datFiltered.length > 0 ) {
				final String res = new String(datFiltered,logClient.getCharsetContent());
				result = res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
			}
			else {
				// No se encontro texto
				result = buildJsonError("No se obtuvo respuesta del servidor", 500); //$NON-NLS-1$
			}

			if (logClient.getCharsetContent()!= null) {
				response.setCharacterEncoding(logClient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;
		case DOWNLOAD:
			LOGGER.info("Solicitud entrante de descarga de fichero"); //$NON-NLS-1$

			final String tempDir = ConfigManager.getTempDir();
			if (tempDir == null || tempDir.equals("")) { //$NON-NLS-1$
				LOGGER.warning("No se encuentra configurado el directorio temporal en el fichero de configuracion .properties"); //$NON-NLS-1$
				final String jsonError = buildJsonError("No se encuentra configurado el directorio temporal en el fichero de configuracion .properties", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
				session.setAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON, jsonError);
				response.sendRedirect(getSelectionResultUrl(request, false));
				return;
			}

			result = ""; //$NON-NLS-1$
			String path = ""; //$NON-NLS-1$

			final byte datDownload[] = logClient.downloadJson(params.getLogFileName(), tempDir);
			if (datDownload != null && datDownload.length > 0 ) {

				JsonObject jsonObj;
				try (final JsonReader reader = Json.createReader(new ByteArrayInputStream(datDownload));) {
					jsonObj = reader.readObject();
				}

				if (jsonObj.getJsonArray("Ok") != null) {	//$NON-NLS-1$
					final JsonArray download = jsonObj.getJsonArray("Ok"); //$NON-NLS-1$
					final JsonObject json = download.getJsonObject(0);
					if (json.get("Path") != null && !"".equals(json.get("Path").toString().trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						path = json.get("Path").toString(); //$NON-NLS-1$

						final String mimeType = "application/zip"; //$NON-NLS-1$
						final int ipos = params.getLogFileName().lastIndexOf("."); //$NON-NLS-1$
						final String zipfileName =
								(ipos == -1 ? params.getLogFileName() : params.getLogFileName().substring(0, ipos))
								+ ".zip"; //$NON-NLS-1$
				        // Modificamos el contenido de la  respuesta
				        response.setContentType(mimeType);

				        //Forzamos la descarga (valores de header)
				        final String headerKey = "Content-Disposition"; //$NON-NLS-1$
				        final String headerValue = String.format("attachment; filename=\"%s\"", zipfileName); //$NON-NLS-1$
				        response.setHeader(headerKey, headerValue);

				        // Obtenemos los datos de la respuesta

				        final File f =  new File(path.replaceAll("\"", "")).getCanonicalFile(); //$NON-NLS-1$ //$NON-NLS-2$
				        final String normalizedTempDir = new File(tempDir).getCanonicalPath();

				        if (f.getAbsolutePath().startsWith(normalizedTempDir)) {

				        	final byte[] buffer = new byte[8 * 1024];
				        	int tam = 0;
				        	if (f.exists()) {
				        		tam = (int) f.length();
				        		final FileInputStream input = new FileInputStream(f);
				        		response.setContentLength(tam);
				        		try {
				        			final OutputStream output = response.getOutputStream();//gestiona la descarga el navegador
				        			try {
				        				int bytesRead;
				        				while ((bytesRead = input.read(buffer)) != -1) {
				        					output.write(buffer, 0, bytesRead);
				        				}
				        			} finally {
				        				output.flush();
				        				output.close();
				        			}
				        		} finally {
				        			input.close();
				        		}

				        		if (response.isCommitted()) {
				        			Files.deleteIfExists(f.toPath());
				        		}
				        		return;
				        	}
				        	result = buildJsonError("No se encontro el fichero para la descarga", 500); //$NON-NLS-1$
				        }
				        else {
				        	result = buildJsonError("Se especifico una ruta de fichero no valida", 400); //$NON-NLS-1$
				        }
					}
					else {
						result = buildJsonError("La respuesta del servicio no esta bien formada", 500); //$NON-NLS-1$
					}
				}
				else {
					final String errorText = datDownload.length <= 200 ?
							new String(datDownload) : new String(datDownload, 0, 200);
					LOGGER.warning("Error durante la descarga. Mensaje enviado " + errorText); //$NON-NLS-1$
					result = buildJsonError("El resultado de la operacion no es correcto", 500); //$NON-NLS-1$
				}
			}
			else {
				// No se obtuvo resultado
				result = buildJsonError("No se obtuvo respuesta del servidor", 500); //$NON-NLS-1$
			}


			response.getWriter().write(result);
			break;
		default:
			LOGGER.warning("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
			final String jsonError = buildJsonError("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio.", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			session.setAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON, jsonError);
			response.sendRedirect(getSelectionResultUrl(request, false));
			return;
		}
	}

	/**
	 * Devuelve la URL a la que redirigir en caso de un error en el proceso de selecci&oacute;n.
	 * @param request Petici&oacute;n realizada al servicio.
	 * @param isOk {@code true} en caso de querer la URL de &eacute;xito, {@code false} en caso contrario.
	 * @return URL de redirecci&oacute;n.
	 */
	private static String getSelectionResultUrl(final HttpServletRequest request, final boolean isOk) {
		return request.getContextPath().toString() +
				"/Logs/LogsMainPage.jsp?op=" + OPERATION_SELECTION + //$NON-NLS-1$
				"&r=" + (isOk ? "1" : "0") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"&ent=srv"; //$NON-NLS-1$
	}

	/**
	 * Devuelve una cadena con un JSON con un mensaje de error.
	 * @param msgError Mensaje de error.
	 * @param errorCode C&oacute;digo de error.
	 * @return Cadena de texto con el JSON.
	 */
	private final static String buildJsonError(final String msgError, final int errorCode) {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		data.add(Json.createObjectBuilder()
				.add("Code",errorCode) //$NON-NLS-1$
				.add("Message", msgError)); //$NON-NLS-1$
		jsonObj.add("Error", data); //$NON-NLS-1$

		final StringWriter error = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(error);) {
			jw.writeObject(jsonObj.build());
		}

        return error.toString();
	}

	private static ServiceOperations checkOperation(final String opString)
			throws NumberFormatException, UnsupportedOperationException {

		final int op = Integer.parseInt(opString);
		return ServiceOperations.parseOperation(op);
	}

	/**
	 * Carga en los atributos del objeto los par&aacute;metros de la petici&oacute;n.
	 * @param request Petici&oacute;n realizada al servicio.
	 * @throws IllegalArgumentException Cuando se recibi&oacute; un par&aacute;metro
	 * con un formato no v&aacute;lido.
	 */
	private final static Parameters loadParameters(final HttpServletRequest request) throws IllegalArgumentException {

		final Parameters params = new Parameters();
		try {
			if (request.getParameter(ServiceParams.PARAM_URL) != null &&
					!request.getParameter(ServiceParams.PARAM_URL).isEmpty()) {
				params.setUrl(request.getParameter(ServiceParams.PARAM_URL));
			}
			params.setVerifySsl(Boolean.parseBoolean(request.getParameter(ServiceParams.PARAM_VERIFY_SSL)));

			if (request.getParameter(ServiceParams.PARAM_NAMESRV) != null &&
					!request.getParameter(ServiceParams.PARAM_NAMESRV).isEmpty()) {
				params.setNameSrv(request.getParameter(ServiceParams.PARAM_NAMESRV));
			}
			if (request.getParameter(ServiceParams.PARAM_FILENAME) != null &&
					!request.getParameter(ServiceParams.PARAM_FILENAME).isEmpty()) {
				params.setLogFileName(request.getParameter(ServiceParams.PARAM_FILENAME));
			}
			if (request.getParameter(ServiceParams.PARAM_NLINES) != null &&
					!request.getParameter(ServiceParams.PARAM_NLINES).isEmpty()) {
				try {
					params.setNumlines(Integer.parseInt(request.getParameter(ServiceParams.PARAM_NLINES)));
				}
				catch (final Exception e) {
					throw new IllegalArgumentException("El n\u00FAmero de l\u00EDneas indicado no es v\u00E1lido", e); //$NON-NLS-1$
				}
			}
			if (request.getParameter(ServiceParams.PARAM_TXT2SEARCH) != null &&
					!request.getParameter(ServiceParams.PARAM_TXT2SEARCH).isEmpty()) {
				params.setTxt2search(request.getParameter(ServiceParams.PARAM_TXT2SEARCH));
			}
			if (request.getParameter(ServiceParams.PARAM_SEARCHDATE) != null &&
					!request.getParameter(ServiceParams.PARAM_SEARCHDATE).isEmpty()) {
				try {
					params.setDatetime(Long.parseLong(request.getParameter(ServiceParams.PARAM_SEARCHDATE)));
				}
				catch (final Exception e) {
					throw new IllegalArgumentException("La fecha para la b\u00FAsqueda del texto no es v\u00E1lida", e); //$NON-NLS-1$
				}
			}
			if (request.getParameter(ServiceParams.PARAM_START_DATETIME) != null &&
					!request.getParameter(ServiceParams.PARAM_START_DATETIME).isEmpty()) {
				try {
					params.setStartDateTime(Long.parseLong(request.getParameter(ServiceParams.PARAM_START_DATETIME)));
				}
				catch (final Exception e) {
					throw new IllegalArgumentException("La fecha de inicio del filtro no es v\u00E1lida", e); //$NON-NLS-1$
				}
			}
			if (request.getParameter(ServiceParams.PARAM_END_DATETIME) != null &&
					!request.getParameter(ServiceParams.PARAM_END_DATETIME).isEmpty()) {
				try {
					params.setEndDateTime(Long.parseLong(request.getParameter(ServiceParams.PARAM_END_DATETIME)));
				}
				catch (final Exception e) {
					throw new IllegalArgumentException("La fecha de fin del filtro no es v\u00E1lida", e); //$NON-NLS-1$
				}
			}
			if (request.getParameter(ServiceParams.PARAM_LEVEL) != null &&
					!request.getParameter(ServiceParams.PARAM_LEVEL).isEmpty()) {
				params.setLevel(request.getParameter(ServiceParams.PARAM_LEVEL));
			}
			params.setReset(
					request.getParameter(ServiceParams.PARAM_RESET) != null &&
					!request.getParameter(ServiceParams.PARAM_RESET).isEmpty());
			params.setMessage(
					request.getParameter(ServiceParams.PARAM_MSG) != null &&
					!request.getParameter(ServiceParams.PARAM_MSG).isEmpty() ?
							request.getParameter(ServiceParams.PARAM_MSG) : null);
		}
		catch (final IllegalArgumentException e) {
			throw e;
		}
		catch (final Exception e) {
				throw new IllegalArgumentException("Par\u00E1metro no v\u00E1lido", e); //$NON-NLS-1$
			}
			return params;
		}

	static class Parameters {
		private String url = "";//$NON-NLS-1$
		private boolean verifySsl = true;
		private String nameSrv = "";//$NON-NLS-1$
		private String logFileName = "";//$NON-NLS-1$
		private int numlines = 0;
		private String txt2search = "";//$NON-NLS-1$
		private long datetime = 0L;
		private String level = "";//$NON-NLS-1$
		private long startDateTime = 0L;
		private long endDateTime = 0L;
		private boolean reset = false;
		private String message = null;

		public String getUrl() {
			return this.url;
		}
		public void setUrl(final String url) {
			this.url = url;
		}
		public boolean isVerifySsl() {
			return this.verifySsl;
		}
		public void setVerifySsl(final boolean verifySsl) {
			this.verifySsl = verifySsl;
		}
		public String getNameSrv() {
			return this.nameSrv;
		}
		public void setNameSrv(final String nameSrv) {
			this.nameSrv = nameSrv;
		}
		public String getLogFileName() {
			return this.logFileName;
		}
		public void setLogFileName(final String logFileName) {
			this.logFileName = logFileName;
		}
		public int getNumlines() {
			return this.numlines;
		}
		public void setNumlines(final int numlines) {
			this.numlines = numlines;
		}
		public String getTxt2search() {
			return this.txt2search;
		}
		public void setTxt2search(final String txt2search) {
			this.txt2search = txt2search;
		}
		public long getDatetime() {
			return this.datetime;
		}
		public void setDatetime(final long datetime) {
			this.datetime = datetime;
		}
		public String getLevel() {
			return this.level;
		}
		public void setLevel(final String level) {
			this.level = level;
		}
		public long getStartDateTime() {
			return this.startDateTime;
		}
		public void setStartDateTime(final long startDateTime) {
			this.startDateTime = startDateTime;
		}
		public long getEndDateTime() {
			return this.endDateTime;
		}
		public void setEndDateTime(final long endDateTime) {
			this.endDateTime = endDateTime;
		}
		public boolean isReset() {
			return this.reset;
		}
		public void setReset(final boolean reset) {
			this.reset = reset;
		}
		public String getMessage() {
			return this.message;
		}
		public void setMessage(final String message) {
			this.message = message;
		}
	}
}
