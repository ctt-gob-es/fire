package es.gob.fire.server.admin.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
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
import es.gob.fire.server.admin.message.AdminFilesNotFoundException;
import es.gob.log.consumer.client.LogConsumerClient;
import es.gob.log.consumer.client.ServiceOperations;
/**
 * Servlet implementation class LogAdminService
 */
public class LogAdminService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(LogAdminService.class.getName());

	private String url = "";//$NON-NLS-1$
	private String nameSrv = "";//$NON-NLS-1$
	private String logFileName = "";//$NON-NLS-1$
	private String opString = "";//$NON-NLS-1$
	private int numlines = 0;
	private String txt2search = "";//$NON-NLS-1$
	private String datetime = "";//$NON-NLS-1$
	private LogConsumerClient logclient = null;
	private  String level = "";//$NON-NLS-1$
	private  long startDateTime = 0L;
	private  long endDateTime = 0L;
	private boolean reset = false;
	private String message = "";//$NON-NLS-1$


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
		String result = null;

		//final String codeInit  = "I9lUuX+iEvzAD/hwaU2MbQ=="; //$NON-NLS-1$ // I9lUuX+iEvzAD/hwaU2MbQ==
		//D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=
		final String stringOp = "seleccion"; //$NON-NLS-1$
		final boolean isOk = false;


		try {


			this.getParameters(request, session);

		}
		catch (final Exception e) {
			LOGGER.warning("No se han podido recuperar correctamente los parametros."); //$NON-NLS-1$
			final String jsonError = getJsonError("No se han podido recuperar correctamente los parametros.", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			session.setAttribute("ERROR_JSON", jsonError); //$NON-NLS-1$
			response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return;
		}

		final String opString = request.getParameter("op"); //$NON-NLS-1$
		if (opString == null) {
			final StringWriter error = new StringWriter();
			LOGGER.warning("No se ha indicado codigo de operacion"); //$NON-NLS-1$
			final String jsonError = getJsonError("No se ha indicado codigo de operacion", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
	        response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return;
		}

		//Comprobamos que el c&oacute;digo de operaci&oacute;n sea correcto
		ServiceOperations op;
		try {
			op = checkOperation(opString);
		}
		catch (final Exception e) {
			LOGGER.warning(String.format("Codigo de operacion no soportado (%s). Se rechaza la peticion.", opString)); //$NON-NLS-1$
			final String jsonError = getJsonError(String.format("Codigo de operacion no soportado (%s). Se rechaza la peticion.", opString), HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			session.setAttribute("ERROR_JSON", jsonError); //$NON-NLS-1$
	        response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return;
		}

		//Comprobamos que se haya iniciado la conexi&oacute;n con el servidor
		if(!op.equals(ServiceOperations.ECHO) && this.logclient == null) {
			LOGGER.warning("No se ha indicado conexion con el servidor de log en sesion"); //$NON-NLS-1$
			final String jsonError = getJsonError("No se ha indicado conexion con el servidor de log en sesion", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			session.setAttribute("ERROR_JSON", jsonError); //$NON-NLS-1$
			response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			return;
		}

		// Selecionamos la operai&oacute;n a realizar respecto al c&oacute;digo recivido.
		switch (op) {
		case ECHO:
			result = echo(this.url);
			response.getWriter().write(result);
			break;

		case GET_LOG_FILES:
			LOGGER.info("Solicitud entrante de listado de ficheros"); //$NON-NLS-1$
			try {
				final byte datLogFiles[] = this.logclient.getLogFiles();
				if(datLogFiles != null) {
					session.setAttribute("JSON", datLogFiles); //$NON-NLS-1$
				}

				if(this.getMessage() != null && !"".equals(this.getMessage())) { //$NON-NLS-1$
					response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsFileList.jsp?")//$NON-NLS-1$
					.concat(ServiceParams.PARAM_NAMESRV).concat("=").concat(this.nameSrv) //$NON-NLS-1$
					.concat("&").concat(ServiceParams.PARAM_MSG).concat("=").concat(this.getMessage())//$NON-NLS-1$ //$NON-NLS-2$
					);
				}
				else {
					//en el caso de recivir un error, redigige a LogsMainPage para mostrar el mensaje
					//en caso contrario, redigige a LogsFileList para mostrar los ficheros encontrados.
					final JsonReader reader = Json.createReader(new ByteArrayInputStream(datLogFiles));
					final JsonObject jsonObj = reader.readObject();
					reader.close();
					if(jsonObj.getJsonArray("Error") != null){  //$NON-NLS-1$
						session.setAttribute("ERROR_JSON", new String (datLogFiles)); //$NON-NLS-1$
						response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					}
					else {
						response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsFileList.jsp?")//$NON-NLS-1$
						.concat(ServiceParams.PARAM_NAMESRV).concat("=").concat(this.nameSrv)); //$NON-NLS-1$
					}

				}
			}
			catch (final IOException e) {
				LOGGER.severe("Error al obtener los ficheros log del servidor central"+ e.getMessage()); //$NON-NLS-1$
				final String jsonError = getJsonError("Error al obtener los ficheros log del servidor central :" , HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
		        try {
					session.setAttribute("ERROR_JSON", jsonError); //$NON-NLS-1$
					response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				} catch (final IOException e1) {
					LOGGER.severe("Error en la respuesta del mensaje:"+ e1.getMessage()); //$NON-NLS-1$
				}
			}
			break;

		case OPEN_FILE:
			LOGGER.info("Solicitud entrante de apertura de fichero"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$
			final byte datOpenFiles[] = this.logclient.openFile(this.logFileName);

			if(datOpenFiles != null && datOpenFiles.length > 0) {
				final JsonReader reader = Json.createReader(new ByteArrayInputStream(datOpenFiles));
				final JsonObject jsonObj = reader.readObject();
				reader.close();

				if(this.logclient != null && this.logclient.getCharsetContent()!= null){
					response.setCharacterEncoding(this.logclient.getCharsetContent().toString());
				}

				if(jsonObj.getJsonArray("Error") != null){ //$NON-NLS-1$
					final JsonArray Error = jsonObj.getJsonArray("Error"); //$NON-NLS-1$
					response.sendRedirect(request.getContextPath().toString().concat("/LogAdminService?op=3")//$NON-NLS-1$
							.concat("&").concat(ServiceParams.PARAM_NAMESRV).concat("=").concat(this.nameSrv)//$NON-NLS-1$ //$NON-NLS-2$
							.concat("&").concat(ServiceParams.PARAM_MSG).concat("=").concat(Error.getJsonObject(0).getString("Message"))//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							);
				}
				else {
					session.setAttribute("JSON_LOGINFO", datOpenFiles); //$NON-NLS-1$

					if(!isReset()) {
						response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsManager.jsp?").concat(ServiceParams.PARAM_NAMESRV).concat("=")//$NON-NLS-1$ //$NON-NLS-2$
							 .concat(this.nameSrv).concat("&").concat(ServiceParams.PARAM_FILENAME).concat("=").concat(this.logFileName));  //$NON-NLS-1$//$NON-NLS-2$
					}
					else {
						response.getWriter().write(result);
						setReset(false);
					}

				}
			}

			break;

		case CLOSE_FILE:
			LOGGER.info("Solicitud entrante de cierre de fichero"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$
			final byte datCloseFiles[] = this.logclient.closeFile();
			if(datCloseFiles != null && datCloseFiles.length > 0) {
				final String res = new String(datCloseFiles,this.logclient.getCharsetContent());
				result += res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
			}
			if(this.logclient != null && this.logclient.getCharsetContent()!= null){
				response.setCharacterEncoding(this.logclient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;

		case TAIL:
			LOGGER.info("Solicitud entrante de consulta del final del log"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$
			final byte datTailFile[] = this.logclient.getLogTail(this.numlines, this.logFileName);
			if(datTailFile != null && datTailFile.length > 0) {
				final String res = new String(datTailFile,this.logclient.getCharsetContent());
				result += res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
			}

			if(this.logclient != null && this.logclient.getCharsetContent()!= null){
				response.setCharacterEncoding(this.logclient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;
		case GET_MORE:
			LOGGER.info("Solicitud entrante de mas log"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$
			final byte datMoreFile[] = this.logclient.getMoreLog(this.numlines, this.logFileName);
			if(datMoreFile != null && datMoreFile.length > 0 ) {
				final String res = new String(datMoreFile,this.logclient.getCharsetContent());
				result = res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
			}
			if(this.logclient != null && this.logclient.getCharsetContent() != null){
				response.setCharacterEncoding(this.logclient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;
		case SEARCH_TEXT:
			LOGGER.info("Solicitud entrante de busqueda de texto"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$
			final byte datSearchTxt[] = this.logclient.searchText(this.numlines, this.txt2search, this.datetime, this.isReset());
			if(datSearchTxt != null && datSearchTxt.length > 0 ) {
				final String res = new String(datSearchTxt,this.logclient.getCharsetContent());
				result += res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
			}
			if(this.logclient != null && this.logclient.getCharsetContent()!= null){
				response.setCharacterEncoding(this.logclient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;
		case FILTER:
			LOGGER.info("Solicitud entrante de filtrado de log"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$

			final byte datFiltered[] = this.logclient.getLogFiltered(this.numlines, this.getStartDateTime(), this.getEndDateTime(), this.getLevel(), this.isReset());
			if(datFiltered != null && datFiltered.length > 0 ) {
				final String res = new String(datFiltered,this.logclient.getCharsetContent());
				result += res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
			}
			if(this.logclient != null && this.logclient.getCharsetContent()!= null){
				response.setCharacterEncoding(this.logclient.getCharsetContent().toString());
			}
			response.getWriter().write(result);
			break;
		case DOWNLOAD:
			LOGGER.info("Solicitud entrante de descarga de fichero"); //$NON-NLS-1$
			long time_start, time_end;
			time_start = System.currentTimeMillis();
			String tempLogsDir = null;
			try {
				ConfigManager.initialize();
				tempLogsDir = ConfigManager.getLogsTempDir();
				if(tempLogsDir == null || tempLogsDir.equals("")) { //$NON-NLS-1$
					LOGGER.warning("No se encuentra configurada la variable logs.tempdir del fichero de configuración .properties"); //$NON-NLS-1$
					final String jsonError = getJsonError("No se encuentra configurada la variable logs.tempdir del fichero de configuración .properties", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
					session.setAttribute("ERROR_JSON", jsonError); //$NON-NLS-1$
					response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					return;
				}

			} catch (final AdminFilesNotFoundException e) {
				LOGGER.severe("Error no se encuentra el fichero de configuración .properties"); //$NON-NLS-1$
				final String jsonError = getJsonError("No se han podido recuperar correctamente el fichero de configuración .properties.", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
				session.setAttribute("ERROR_JSON", jsonError); //$NON-NLS-1$
				response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				return;
			}

			result = ""; //$NON-NLS-1$
			String path = ""; //$NON-NLS-1$

			final byte datDownload[] = this.logclient.download(this.getLogFileName(), this.isReset(), tempLogsDir);
			if(datDownload != null && datDownload.length > 0 ) {

				final JsonReader reader = Json.createReader(new ByteArrayInputStream(datDownload));
				final JsonObject jsonObj = reader.readObject();
				reader.close();

				if(jsonObj.getJsonArray("Ok") != null){	//$NON-NLS-1$
					final JsonArray download = jsonObj.getJsonArray("Ok"); //$NON-NLS-1$
					final JsonObject json = download.getJsonObject(0);
					if(json.get("Path") != null && !"".equals(json.get("Path").toString().trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						path = json.get("Path").toString(); //$NON-NLS-1$


						final String mimeType = "application/zip"; //$NON-NLS-1$
						final int ipos = this.getLogFileName().lastIndexOf("."); //$NON-NLS-1$
						final String zipfileName = this.getLogFileName().replace(this.getLogFileName().substring(ipos), ".zip"); //$NON-NLS-1$
				        // Modificamos el contenido de la  respuesta
				        response.setContentType(mimeType);


				        //Forzamos la descarga (valores de header)
				        final String headerKey = "Content-Disposition"; //$NON-NLS-1$
				        final String headerValue = String.format("attachment; filename=\"%s\"", zipfileName); //$NON-NLS-1$
				        response.setHeader(headerKey, headerValue);

				        // Obtenemos los datos de la respuesta
				        //final OutputStream outStream = response.getOutputStream();

				        final byte[] buffer = new byte[8 * 1024];
				        final File f =  new File(path.replaceAll("\"", "")); //$NON-NLS-1$ //$NON-NLS-2$
				        int tam = 0;
				        if (f.exists()) {
				        	tam = (int) f.length();
				        	final FileInputStream input = new FileInputStream(f);
				        	response.setContentLength(tam);
							try {
							 // final OutputStream output = new FileOutputStream(zipfileName);//guarda a fichero
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

							if(response.isCommitted()) {
								Files.deleteIfExists(f.toPath());
							}
				        }

					}
				}
				else {
					final String res = new String(datDownload,this.logclient.getCharsetContent());
					result += res.replace("\\n", "</br>");//$NON-NLS-1$//$NON-NLS-2$
					response.getWriter().write(result);
					break;
				}

			}

			time_end = System.currentTimeMillis();
			System.out.println("Downloaded :"+ ( time_end - time_start )/1000 +" sec"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		default:
			LOGGER.warning("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
			final String jsonError = getJsonError("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio.", HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			session.setAttribute("ERROR_JSON", jsonError); //$NON-NLS-1$
	        response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
			return;
		}


	}

	/**
	 * Funci&oacute;n que obtiene la respuesta del servidor si hay comunicaci&oacute;n a trv&eacute;s
	 * del api devolviendo un String con formato JSON, tanto si la comunicaci&oacute;n ha sido correcta
	 * como si ha habido un error.
	 * @param url
	 * @return
	 */
	protected final String echo(final String url) {
		final LogConsumerClient lclient = new LogConsumerClient();
		final StringWriter resultEcho = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		String result = ""; //$NON-NLS-1$
		try {
			result = lclient.echo(url);
		}
		catch (final IOException e) {
			 final String res = new String("No se ha podido conectar a la ruta indicada."); //$NON-NLS-1$
			data.add(Json.createObjectBuilder()
					.add("Code",404) //$NON-NLS-1$
					.add("Message", res)); //$NON-NLS-1$
			jsonObj.add("Error", data); //$NON-NLS-1$
			final JsonWriter jw = Json.createWriter(resultEcho);
	        jw.writeObject(jsonObj.build());
	        jw.close();
	        result = resultEcho.toString();
		}
		return result;

	}

	/**
	 * Devuelve un String con formato JSON
	 * @param msgError
	 * @return
	 */
	private final static String getJsonError(final String msgError, final int errorCode) {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final StringWriter error = new StringWriter();
		data.add(Json.createObjectBuilder()
				.add("Code",errorCode) //$NON-NLS-1$
				.add("Message", msgError)); //$NON-NLS-1$
		jsonObj.add("Error", data); //$NON-NLS-1$
		final JsonWriter jw = Json.createWriter(error);
	    jw.writeObject(jsonObj.build());
	    jw.close();
        return error.toString();
	}

	private static ServiceOperations checkOperation(final String opString)
			throws NumberFormatException, UnsupportedOperationException {

		final int op = Integer.parseInt(opString);
		return ServiceOperations.parseOperation(op);
	}


	/**
	 * Obtiene los par&aacute;metros
	 * @param request
	 * @param session
	 */
	private final void getParameters(final HttpServletRequest request, final HttpSession session) {

			if(request.getParameter(ServiceParams.PARAM_URL) != null && !"".equals(request.getParameter(ServiceParams.PARAM_URL))) { //$NON-NLS-1$
				this.url = request.getParameter(ServiceParams.PARAM_URL);
			}
			if(request.getParameter(ServiceParams.PARAM_NAMESRV) != null && !"".equals(request.getParameter(ServiceParams.PARAM_NAMESRV))) { //$NON-NLS-1$
				this.nameSrv = request.getParameter(ServiceParams.PARAM_NAMESRV);
			}
			if(request.getParameter(ServiceParams.PARAM_FILENAME) != null && !"".equals(request.getParameter(ServiceParams.PARAM_FILENAME))) { //$NON-NLS-1$
				this.logFileName = request.getParameter(ServiceParams.PARAM_FILENAME);
			}

			if(session.getAttribute("LOG_CLIENT") != null) { //$NON-NLS-1$
				this.logclient = (LogConsumerClient) session.getAttribute("LOG_CLIENT"); //$NON-NLS-1$
			}
			if(request.getParameter(ServiceParams.PARAM_NLINES) != null && !"".equals(request.getParameter(ServiceParams.PARAM_NLINES))) { //$NON-NLS-1$
				this.numlines = Integer.parseInt(request.getParameter(ServiceParams.PARAM_NLINES));
			}

			if(request.getParameter(ServiceParams.PARAM_TXT2SEARCH) != null && !"".equals(request.getParameter(ServiceParams.PARAM_TXT2SEARCH))) { //$NON-NLS-1$
				this.txt2search = request.getParameter(ServiceParams.PARAM_TXT2SEARCH);
			}
			if(request.getParameter(ServiceParams.PARAM_SEARCHDATE) != null && !"".equals(request.getParameter(ServiceParams.PARAM_SEARCHDATE))) { //$NON-NLS-1$
				this.datetime = request.getParameter(ServiceParams.PARAM_SEARCHDATE);
			}
			if(request.getParameter(ServiceParams.START_DATETIME) != null && !"".equals(request.getParameter(ServiceParams.START_DATETIME))) { //$NON-NLS-1$
				this.setStartDateTime(Long.parseLong(request.getParameter(ServiceParams.START_DATETIME)));
			}
			if(request.getParameter(ServiceParams.END_DATETIME) != null && !"".equals(request.getParameter(ServiceParams.END_DATETIME))) { //$NON-NLS-1$
				this.setEndDateTime(Long.parseLong(request.getParameter(ServiceParams.END_DATETIME)));
			}
			if(request.getParameter(ServiceParams.LEVEL) != null && !"".equals(request.getParameter(ServiceParams.LEVEL))) { //$NON-NLS-1$
				this.setLevel(request.getParameter(ServiceParams.LEVEL));
			}
			if(request.getParameter(ServiceParams.PARAM_RESET) != null && !"".equals(request.getParameter(ServiceParams.PARAM_RESET))) { //$NON-NLS-1$
				this.setReset(true);
			}else {
				this.setReset(false);
			}
			if(request.getParameter(ServiceParams.PARAM_MSG) != null && !"".equals(request.getParameter(ServiceParams.PARAM_MSG))) { //$NON-NLS-1$
				this.setMessage(request.getParameter(ServiceParams.PARAM_MSG));
			}
			else {
				this.setMessage(null);
			}

	}


	/* Propiedades */
	private final String getUrl() {
		return this.url;
	}

	private final void setUrl(final String url) {
		this.url = url;
	}

	private final String getNameSrv() {
		return this.nameSrv;
	}

	private final void setNameSrv(final String nameSrv) {
		this.nameSrv = nameSrv;
	}

	private final String getLogFileName() {
		return this.logFileName;
	}

	private final void setLogFileName(final String logFileName) {
		this.logFileName = logFileName;
	}

	private final String getOpString() {
		return this.opString;
	}

	private final void setOpString(final String opString) {
		this.opString = opString;
	}

	private final String getTxt2search() {
		return this.txt2search;
	}

	private final String getDatetime() {
		return this.datetime;
	}
	 private final String getLevel() {
			return this.level;
	}

	private final void setLevel(final String level) {
			this.level = level;
	}

	private final long getEndDateTime() {
		return this.endDateTime;
	}

	private final void setEndDateTime(final long endDateTime) {
		this.endDateTime = endDateTime;
	}

	private final long getStartDateTime() {
			return this.startDateTime;
	}

	private final void setStartDateTime(final long startDateTime) {
			this.startDateTime = startDateTime;
	}

	private final boolean isReset() {
		return this.reset;
	}

	private final void setReset(final boolean reset) {
		this.reset = reset;
	}

	private final String getMessage() {
		return this.message;
	}

	private final void setMessage(final String message) {
		this.message = message;
	}



}
