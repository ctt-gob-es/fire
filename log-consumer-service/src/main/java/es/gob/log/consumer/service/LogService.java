package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogFiles;

/**
 * Servicio de consulta de logs.
 */
public class LogService extends HttpServlet {

	/** Serial ID. */
	private static final long serialVersionUID = -8162434026674748888L;

	private static final Logger LOGGER = Logger.getLogger(LogService.class.getName());

	/** Status code personalizado que se proporciona cuando se quiere notificar
	 * un error interno controlado. */
	private static final int STATUSCODE_CONTROLLED_ERROR = 220;

	private static final int ERROR_SEPARATOR = '|';

	private static File pathLogs = null;


	@Override
	public void init() throws ServletException {
		super.init();

		pathLogs = ConfigManager.getInstance().getLogsDir();
		if (pathLogs == null || !pathLogs.isDirectory()) {
			LOGGER.severe("No se ha configurado un directorio de logs valido: " + pathLogs); //$NON-NLS-1$
			throw new ServletException("No se ha configurado un directorio de logs valido: " + pathLogs); //$NON-NLS-1$
		}
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

		final String opString = req.getParameter(ServiceParams.OPERATION);

		if (opString == null) {
			LOGGER.warning("No se ha indicado codigo de operacion"); //$NON-NLS-1$
			sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "No se ha indicado codigo de operacion"); //$NON-NLS-1$
			return;
		}

		// Comprobamos que se solicite una operacion valida
		ServiceOperations op;
		try {
			op = checkOperation(opString);
		}
		catch (final Exception e) {
			LOGGER.warning(String.format("Codigo de operacion no soportado (%s). Se rechaza la peticion.", opString.replaceAll("[\r\n]", ""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
					LOGGER.warning("Operacion no soportada sin login previo a pesar de estar marcada como tal. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
					sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada sin login previo a pesar de estar marcada como tal. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
					return;
				}
			}
			// Comprobamos si el usuario esta registrado
			else if (checkLogin(req)) {
				switch (op) {
				case GET_LOG_FILES:
					LOGGER.info("Solicitud entrante de listado de ficheros"); //$NON-NLS-1$
					result = getLogFiles(pathLogs);
					break;
				case OPEN_FILE:
					LOGGER.info("Solicitud entrante de apertura de fichero"); //$NON-NLS-1$
					result = openFile(req);
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
					LOGGER.warning(String.format("Operacion no soportada: %s", op)); //$NON-NLS-1$
					sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada: " + op); //$NON-NLS-1$
					return;
				}
			}
			else {
				LOGGER.warning("El cliente no inicio sesion. Se rechaza la peticion."); //$NON-NLS-1$
				sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "El cliente no inicio sesion"); //$NON-NLS-1$
				return;
			}
		}
		catch (final NoResultException e) {
			LOGGER.log(Level.INFO, "Se notifica al cliente que no se pudo obtener un resultado"); //$NON-NLS-1$
			sendControlledError(resp, HttpServletResponse.SC_NO_CONTENT, e.getLocalizedMessage());
			return;
		}
		catch (final IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "Se envio una peticion con parametros no validos: " + e); //$NON-NLS-1$
			sendControlledError(resp, HttpServletResponse.SC_BAD_REQUEST, "Se envio una peticion con parametros no validos"); //$NON-NLS-1$
			return;
		}
		catch (final SessionException e) {
			LOGGER.log(Level.WARNING,"Se solicito una operacion sin haber abierto sesion u ocurrio un error al abrirla",e); //$NON-NLS-1$
			sendControlledError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Sesion no iniciada"); //$NON-NLS-1$
			return;
		}
		catch (final UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE,"Codificacion no soportada",e); //$NON-NLS-1$
			sendControlledError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "La codificacion no es valida."); //$NON-NLS-1$
			return;
		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Ocurrio un error al procesar la peticion", e); //$NON-NLS-1$
			sendControlledError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocurrio un error al procesar la peticion"); //$NON-NLS-1$
			return;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Ocurrio un error desconocido al procesar la peticion", e); //$NON-NLS-1$
			sendControlledError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ocurrio un error desconocido al procesar la peticion"); //$NON-NLS-1$
			return;
		}

		resp.getOutputStream().write(result);
		resp.getOutputStream().flush();
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
	 * @param logsDir Directorio copn los ficheros de log.
	 * @return Array de bytes de un JSON con el listado de ficheros o con un mensaje de error.
	 */
	private static byte[] getLogFiles (final File logsDir)  {
		return LogFiles.getLogFiles(logsDir);
	}

	/**
	 *
	 * @param req Petici&oacute;n HTTP con los par&aacute;metros necesarios.
	 * @return JSON con la informaci&oacute;n necesaria para el tratamiento del fichero.
	 * @throws IOException Cuando No se puede encontrar o abrir el fichero.
	 * @throws IllegalArgumentException Cuando no se env&iacute;a el nombre de fichero o el valor no es v&aacute;lido.
	 */
	private static byte[] openFile(final HttpServletRequest req) throws IOException {
		return LogOpenServiceManager.process(req);
	}

	private static boolean closeFile(final HttpServletRequest req) {

		final HttpSession session = req.getSession(false);

		session.removeAttribute("Reader"); //$NON-NLS-1$
		session.removeAttribute("LogInfo"); //$NON-NLS-1$
		session.removeAttribute("FilePosition"); //$NON-NLS-1$
		session.removeAttribute("FileSize"); //$NON-NLS-1$

		final Object channelObject = session.getAttribute("Channel"); //$NON-NLS-1$

		session.removeAttribute("Channel"); //$NON-NLS-1$

		if (channelObject != null && channelObject instanceof AsynchronousFileChannel) {
			try {
				((AsynchronousFileChannel) channelObject).close();
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "No se ha cerrado correctamente el fichero de log: ", e); //$NON-NLS-1$
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
}
