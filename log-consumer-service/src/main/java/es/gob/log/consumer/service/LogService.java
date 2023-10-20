package es.gob.log.consumer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import es.gob.log.consumer.LogConstants;
import es.gob.log.consumer.LogDirInfo;
import es.gob.log.consumer.LogFiles;

/**
 * Servicio de consulta de logs.
 */
public class LogService extends HttpServlet {

	/** Serial ID. */
	private static final long serialVersionUID = -8162434026674748888L;

	private static final Logger LOGGER = LoggerFactory.getLogger(LogService.class);

	/** Status code personalizado que se proporciona cuando se quiere notificar
	 * un error interno controlado. */
	private static final int STATUSCODE_CONTROLLED_ERROR = 220;

	private static final int ERROR_SEPARATOR = '|';

	private static File pathLogs = null;

	private static LogDirInfo logDirInfo = null;

	@Override
	public void init() throws ServletException {


		LOGGER.debug("Se inicia el servicio de consulta de logs"); //$NON-NLS-1$

		// Cargamos el directorio de logs
		final ConfigManager configManager;
		try {
			configManager = ConfigManager.getInstance();
		}
		catch (final Exception e) {
			LOGGER.error("No se ha podido inicializar el servicio por no haber encontrado el fichero de configuracion", e); //$NON-NLS-1$
			throw new ServletException("No se ha podido inicializar el servicio"); //$NON-NLS-1$
		}

		pathLogs = configManager.getLogsDir();
		if (pathLogs == null || !pathLogs.isDirectory()) {
			LOGGER.error("No se ha configurado un directorio de logs valido: " + pathLogs); //$NON-NLS-1$
			throw new ServletException("No se ha configurado un directorio de logs valido: " + pathLogs); //$NON-NLS-1$
		}

		// Comprobamos si en el directorio hay un fichero con la configuracion general para los
		// ficheros de log. Si lo hay, extraemos tambien de el informacion adicional.
		final File defaultLogInfoFile = new File(pathLogs, LogConstants.FILE_EXT_LOGINFO);
		if (defaultLogInfoFile.isFile()) {
			LOGGER.info("Se ha encontrado un fichero '" + LogConstants.FILE_EXT_LOGINFO + //$NON-NLS-1$
			            "' con la configuracion general del directorio"); //$NON-NLS-1$

			// Se carga la configuracion del fichero referente al directorio
			logDirInfo = new LogDirInfo();
			try (InputStream fis = new FileInputStream(defaultLogInfoFile)) {
				logDirInfo.load(fis);
			}
			catch (final IOException e) {
				LOGGER.warn("No se ha podido leer el fichero '" + //$NON-NLS-1$
						LogConstants.FILE_EXT_LOGINFO +
						"' con la configuracion general del directorio", e); //$NON-NLS-1$
				logDirInfo = null;
			}

			// Si el fichero existe pero no se configura nada en el referente al directorio
			// se omite esta configuracion
			if (logDirInfo != null && !logDirInfo.hasConfiguration()) {
				logDirInfo = null;
			}
		}
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			doPost(req, resp);
		} catch (IOException e) {
			LOGGER.warn("Ocurrio un error en la operacion. Excepcion: " + e);
		}
		
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			final String opString = req.getParameter(ServiceParams.OPERATION);

			if (opString == null) {
				LOGGER.warn("No se ha indicado codigo de operacion"); //$NON-NLS-1$
				sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "No se ha indicado codigo de operacion"); //$NON-NLS-1$
				return;
			}

			// Comprobamos que se solicite una operacion valida
			ServiceOperations op;
			try {
				op = checkOperation(opString);
			}
			catch (final Exception e) {
				LOGGER.warn(String.format("Codigo de operacion no soportado (%s). Se rechaza la peticion.", opString.replaceAll("[\r\n]", ""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "Codigo de operacion no soportado"); //$NON-NLS-1$
				return;
			}

			// Procesamos la peticion segun si requieren login o no

			byte[] result = null;
			try {
				if (!needLogin(op)) {
					switch (op) {
					case ECHO:
						LOGGER.info("Solicitud entrante de comprobacion del servicio"); //$NON-NLS-1$
						result = echo();
						break;
					case REQUEST_LOGIN:
						LOGGER.info("Solicitud entrante de inicio de sesion"); //$NON-NLS-1$
						result = requestLogin(req);
						break;
					case VALIDATE_LOGIN:
						LOGGER.info("Solicitud entrante de validacion de sesion"); //$NON-NLS-1$
						result = validateLogin(req);
						break;
					default:
						LOGGER.warn("Operacion no soportada sin login previo a pesar de estar marcada como tal. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
						sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada sin login previo a pesar de estar marcada como tal. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
						return;
					}
				}
				// Comprobamos si el usuario esta registrado
				else if (checkLogin(req)) {
					switch (op) {
					case GET_LOG_FILES:
						LOGGER.info("Solicitud entrante de listado de ficheros"); //$NON-NLS-1$
						result = getLogFiles(pathLogs, logDirInfo);
						break;
					case OPEN_FILE:
						LOGGER.info("Solicitud entrante de apertura de fichero"); //$NON-NLS-1$
						result = openFile(req, logDirInfo);
						break;
					case CLOSE_FILE:
						LOGGER.info("Solicitud entrante de cierre de fichero"); //$NON-NLS-1$
						if (!closeFile(req)) {
							sendControlledError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se ha podido cerrar el fichero log"); //$NON-NLS-1$
							return;
						}
						result = "Fichero cerrado".getBytes(); //$NON-NLS-1$
						break;
					case TAIL:
						LOGGER.info("Solicitud entrante de consulta del final del log"); //$NON-NLS-1$
						result = getLogTail(req);
						break;
					case GET_MORE:
						LOGGER.info("Solicitud entrante de mas log"); //$NON-NLS-1$
						result = getMoreLog(req);
						break;
					case SEARCH_TEXT:
						LOGGER.info("Solicitud entrante de busqueda de texto"); //$NON-NLS-1$
						result = searchText(req);
						break;
					case FILTER:
						LOGGER.info("Solicitud entrante de filtrado de log"); //$NON-NLS-1$
						result = getLogFiltered(req);
						break;
					case DOWNLOAD:
						LOGGER.info("Solicitud entrante de descarga de fichero"); //$NON-NLS-1$
						final DataFragment data = download(req, pathLogs);
						if (!data.isComplete()) {
							resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
						}
						result = data.getData();
						break;
					case CLOSE_CONNECTION:
						LOGGER.info("Solicitud entrante para el cierre de la conexion con el servidor"); //$NON-NLS-1$
						closeConection(req);
						result = "Conexion cerrada".getBytes(); //$NON-NLS-1$
						break;
					default:
						LOGGER.warn("Operacion no soportada: " + op); //$NON-NLS-1$
						sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada: " + op); //$NON-NLS-1$
						return;
					}
				}
				else {
					LOGGER.warn("El cliente no inicio sesion. Se rechaza la peticion."); //$NON-NLS-1$
					sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "El cliente no inicio sesion"); //$NON-NLS-1$
					return;
				}
			}
			catch (final NoResultException e) {
				LOGGER.info("Se notifica al cliente que no se pudo obtener un resultado"); //$NON-NLS-1$
				sendControlledError(resp, HttpServletResponse.SC_NO_CONTENT, e.getLocalizedMessage());
				return;
			}
			catch (final IllegalArgumentException e) {
				LOGGER.warn("Se envio una peticion con parametros no validos", e); //$NON-NLS-1$
				sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "Se envio una peticion con parametros no validos"); //$NON-NLS-1$
				return;
			}
			catch (final SessionException e) {
				LOGGER.warn("Se solicito una operacion sin haber abierto sesion u ocurrio un error al abrirla", e); //$NON-NLS-1$
				sendControlledError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Sesion no iniciada"); //$NON-NLS-1$
				return;
			}
			catch (final UnsupportedEncodingException e) {
				LOGGER.error("Codificacion no soportada", e); //$NON-NLS-1$
				sendControlledError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "La codificacion no es valida."); //$NON-NLS-1$
				return;
			}
			catch (final IOException e) {
				LOGGER.error("Ocurrio un error al procesar la peticion", e); //$NON-NLS-1$
				sendControlledError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocurrio un error al procesar la peticion"); //$NON-NLS-1$
				return;
			}
			catch (final Exception e) {
				LOGGER.error("Ocurrio un error desconocido al procesar la peticion", e); //$NON-NLS-1$
				sendControlledError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocurrio un error desconocido al procesar la peticion"); //$NON-NLS-1$
				return;
			}
			
			resp.getOutputStream().write(result);
			resp.getOutputStream().flush();
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.getWriter().write("Error interno: " + e.getMessage()); //$NON-NLS-1$
			resp.flushBuffer();

		}
	}

	/**
	 * Funci&oacute;n que comprueba el c&oacute;digo de operaci&oacute;n
	 * @param opString
	 * @return
	 * @throws NumberFormatException
	 * @throws UnsupportedOperationException
	 */
	private static final ServiceOperations checkOperation(final String opString)
			throws NumberFormatException, UnsupportedOperationException {

		final int op = Integer.parseInt(opString);
		return ServiceOperations.parseOperation(op);
	}

	/**
	 *  Funci&oacute;n que comprueba que el c&oacute;digo de operaci&oacute;n corresponde a al grupo
	 *  de operaciones que necesitan logarse.
	 * @param op
	 * @return true en caso de necesitar login, false en caso contrario
	 */
	private static final boolean needLogin(final ServiceOperations op) {
		// Las operaciones echo, peticion de login y validacion de login, son
		// las unicas que pueden realizarse sin haber establecido logging.
		return 	op != ServiceOperations.ECHO &&
				op != ServiceOperations.REQUEST_LOGIN &&
				op != ServiceOperations.VALIDATE_LOGIN;
	}

	/**
	 *  Funci&oacute;n que comprueba que el usuario se se haya autenticado.
	 * @param req Petici&oacute;n HTTP.
	 * @return {@code true} si el cliente est&aacute; autenticado y {@code false}
	 * en caso contrario.
	 */
	private static final boolean checkLogin(final HttpServletRequest req) {

		boolean logged = false;

		final HttpSession session = req.getSession(false);
		if (session != null) {
			final Object loggedValue = session.getAttribute(SessionParams.LOGGED);
			logged = loggedValue != null ? ((Boolean) loggedValue).booleanValue() : false;
		}

		return logged;
	}

	/**
	 * Funci&oacute;n que devuelve una cadena con formato en byte[]
	 * @return
	 */
	private static final byte[] echo() {
		return EchoServiceManager.process();
	}

	/**
	 * Funci&oacute;n que retorna una cadena de bytes en formato Json con los datos del token generado.
	 * @param req Petici&oacute;n HTTP.
	 * @return Token que deber&aacute; cifrarse para el acceso al servicio.
	 * @throws SessionException Cuando no es posible crear la sesi&oacute;n.
	 */
	private static final byte[] requestLogin(final HttpServletRequest req) throws SessionException {
		final HttpSession session = req.getSession();
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		return RequestLoginManager.process(session);
	}

	/**
	 * Completa el proceso de login.
	 * @param req Petici&oacute;n HTTP para la validaci&oacute;n de la sesi&oacute;n.
	 * @return Cadena de bytes en formato Json indicando que el login es correcto.
	 * @throws SessionException Cuando la sesi&oacute;n no es valida.
	 */
	private static byte[] validateLogin(final HttpServletRequest req) throws SessionException {
		final HttpSession session = req.getSession(false);
		if (session == null) {
			throw new SessionException("No se ha encontrado una sesion iniciada"); //$NON-NLS-1$
		}
		return ValidationLoginManager.process(req, session);
	}

	/**
	 * Funci&oacute;n que cierra la conexi&oacute;n con el servidor.
	 * @param req Petici&oacute;n HTTP para el cierre de sesi&oacute;n.
	 */
	private static void closeConection(final HttpServletRequest req) {

		// Cerramos el fichero actual por si no lo estuviese
		closeFile(req);

		// Invalidamos la sesion
		final HttpSession session = req.getSession(false);
		session.invalidate();
	}
	/**
	 * Funci&oacute;n que retorna una cadena de bytes en formato Json con el resultado de los
	 * ficheros con extendi&oacute;n log exitentes en el servidor con los datos nombre, fecha y
	 * tama&ntilde;o. En caso de no encontrar ficheros, la cadena en formato JSON lo indica como
	 * mensaje de error.
	 * @param logsDir Directorio con los ficheros de log.
	 * @param dirInfo Configuraci&oacute;n referente al directorio que se quiere listar.
	 * @return Array de bytes de un JSON con el listado de ficheros o con un mensaje de error.
	 */
	private static byte[] getLogFiles (final File logsDir, final LogDirInfo dirInfo)  {
		return LogFiles.getLogFiles(logsDir, dirInfo);
	}

	/**
	 * Prepara un fichero para ser procesado.
	 * @param req Petici&oacute;n HTTP con los par&aacute;metros necesarios.
	 * @param dirInfo Configuraci&oacute;n referente al directorio que se quiere listar.
	 * @return JSON con la informaci&oacute;n necesaria para el tratamiento del fichero.
	 * @throws IOException Cuando No se puede encontrar o abrir el fichero.
	 * @throws IllegalArgumentException Cuando no se env&iacute;a el nombre de fichero o el valor no es v&aacute;lido.
	 */
	private static byte[] openFile(final HttpServletRequest req, final LogDirInfo dirInfo) throws IOException {
		return LogOpenServiceManager.process(req, dirInfo);
	}

	private static boolean closeFile(final HttpServletRequest req) {

		final HttpSession session = req.getSession(false);

		session.removeAttribute(SessionParams.FILE_READER);
		session.removeAttribute(SessionParams.LOG_INFO);
		session.removeAttribute(SessionParams.FILE_POSITION);
		session.removeAttribute(SessionParams.FILE_SIZE);

		final Object channelObject = session.getAttribute(SessionParams.FILE_CHANNEL);

		session.removeAttribute(SessionParams.FILE_CHANNEL);

		if (channelObject != null && channelObject instanceof AsynchronousFileChannel) {
			try {
				((AsynchronousFileChannel) channelObject).close();
			} catch (final IOException e) {
				LOGGER.error("No se ha cerrado correctamente el fichero de log: ", e); //$NON-NLS-1$
				return false;
			}
		}

		return true;
	}

	private static byte[] getLogTail(final HttpServletRequest req) throws IOException{
		return LogTailServiceManager.process(req);
	}

	private static byte[] getMoreLog(final HttpServletRequest req)throws IOException, NoResultException {
		return LogMoreServiceManager.process(req);
	}

	private static byte[] getLogFiltered(final HttpServletRequest req) throws IOException, NoResultException {
		return LogFilteredServiceManager.process(req);
	}

	private static byte[] searchText(final HttpServletRequest req) throws IOException, NoResultException {
		return LogSearchServiceManager.process(req);
	}


	private static DataFragment download(final HttpServletRequest req, final File logsDir) throws IOException {
		return LogDownloadServiceManager.process(req, logsDir.getPath());
	}

	/**
	 * Envia una respuesta de error con un statusCode propio. El statusCode utilizado es de tipo
	 * 200 y permite el env&iacute;o de un mensaje de error que llegar&aacute; tal cual al cliente.
	 * @param response Respuesta a trav&eacute;s de la que enviar el mensaje.
	 * @param statusCode C&oacute;digo de error que se quiere hacer llegar al cliente.
	 * @param message Mensaje de error.
	 * @throws IOException Si ocurre algun error durante el envio de la respuesta
	 */
	private static void sendControlledError(final HttpServletResponse response, final int statusCode, final String message) throws IOException {

		response.setStatus(STATUSCODE_CONTROLLED_ERROR);
		response.getOutputStream().write(Integer.toString(statusCode).getBytes(StandardCharsets.UTF_8));
		response.getOutputStream().write(ERROR_SEPARATOR);
		response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
		response.getOutputStream().flush();
		return;
	}

	@Override
	public void destroy() {

		// Cerramos el contexto de logback si es el API de logger que se uso
		final ILoggerFactory loggerContext = LoggerFactory.getILoggerFactory();
		if (loggerContext instanceof LoggerContext) {
			((LoggerContext) loggerContext).stop();
		}
	}
}
