package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogDownload;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;

/**
 * Servicio de consulta de logs.
 */
public class LogService extends HttpServlet {

	/** Serial ID. */
	private static final long serialVersionUID = -8162434026674748888L;

	private static final Logger LOGGER = Logger.getLogger(LogService.class.getName());

	private static int statusCode = HttpServletResponse.SC_OK;


	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final HttpSession session = req.getSession(true);
		final String opString = req.getParameter(ServiceParams.OPERATION);

		if (opString == null) {
			LOGGER.warning("No se ha indicado codigo de operacion"); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha indicado codigo de operacion"); //$NON-NLS-1$
			return;
		}

		// Comprobamos que se solicite una operacion valida
		ServiceOperations op;
		try {
			op = checkOperation(opString);
		}
		catch (final Exception e) {
			LOGGER.warning(String.format("Codigo de operacion no soportado (%s). Se rechaza la peticion.", opString)); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Codigo de operacion no soportado"); //$NON-NLS-1$
			return;
		}

		final File pathLogs = ConfigManager.getInstance().getLogsDir();

		if (pathLogs == null || !pathLogs.exists()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha podido cargar el fichero de configuracion del servicio de consulta de logs"); //$NON-NLS-1$
			return;
		}

		// Procesamos la peticion segun si requieren login o no

		byte[] result = null;
		boolean fileClosed = false;
		try {
			if (!needLogin(op)) {
				switch (op) {
				case ECHO:
					LOGGER.info("Solicitud entrando de comprobacion del servicio"); //$NON-NLS-1$
					result = echo();
					break;
				case REQUEST_LOGIN:
					LOGGER.info("Solicitud entrando de inicio de sesion"); //$NON-NLS-1$
					result = requestLogin(req);
					break;
				case VALIDATE_LOGIN:
					LOGGER.info("Solicitud entrante de validacion de sesion"); //$NON-NLS-1$
					result = validateLogin(req);
					break;
				default:
					LOGGER.warning("Operacion no soportada sin login previo a pesar de estar marcada como tal. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada sin login previo a pesar de estar marcada como tal. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
					result = new String("Operacion no soportada sin login previo a pesar de estar marcada como tal. Este resultado refleja un problema en el codigo del servicio").getBytes(); //$NON-NLS-1$
					return;
				}
			}
			// Comprobamos si el usuario esta registrado
			else if (checkLogin(req)) {

				switch (op) {
				case GET_LOG_FILES:
					LOGGER.info("Solicitud entrante de listado de ficheros"); //$NON-NLS-1$
					result = getLogFiles(pathLogs);
					if(result == null) {
						resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se han encontrado ficheros log en el servidor indicado"); //$NON-NLS-1$
						result = new String("No se han encontrado ficheros log en el servidor indicado").getBytes(); //$NON-NLS-1$
						return;
					}
					break;
				case OPEN_FILE:
					LOGGER.info("Solicitud entrante de apertura de fichero"); //$NON-NLS-1$
					result = openFile(req,resp);
					if(result == null || result.length <= 0) {
						resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha podido abrir el fichero log indicado"); //$NON-NLS-1$
						result = new String("No se ha podido abrir el fichero log indicado").getBytes(); //$NON-NLS-1$
						return;
					}
					fileClosed = false;
					break;
				case CLOSE_FILE:
					LOGGER.info("Solicitud entrante de cierre de fichero"); //$NON-NLS-1$
					fileClosed = closeFile(req);
					if(!fileClosed) {
						resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha podido cerrar el fichero log"); //$NON-NLS-1$
						result = new String("No se ha podido cerrar el fichero log").getBytes(); //$NON-NLS-1$
						return;
					}
					break;
				case TAIL:
					LOGGER.info("Solicitud entrante de consulta del final del log"); //$NON-NLS-1$
					result = getLogTail(req,resp);
					break;
				case GET_MORE:
					LOGGER.info("Solicitud entrante de mas log"); //$NON-NLS-1$
					result = getMoreLog(req, resp);
					break;
				case SEARCH_TEXT:
					LOGGER.info("Solicitud entrante de busqueda de texto"); //$NON-NLS-1$
					result = searchText(req,resp);
					break;
				case FILTER:
					LOGGER.info("Solicitud entrante de filtrado de log"); //$NON-NLS-1$
					result = getLogFiltered(req,resp);
					break;
				case DOWNLOAD:
					LOGGER.info("Solicitud entrante de descarga de fichero"); //$NON-NLS-1$
					result = download(req, resp, pathLogs);
					break;
				default:
					LOGGER.warning("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Operacion no soportada. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
					result = new String("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio").getBytes(); //$NON-NLS-1$
					return;
				}
			}
			else {
				LOGGER.warning("Operacion no soportada sin login previo"); //$NON-NLS-1$
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada sin login previo"); //$NON-NLS-1$
				result = new String("Operacion no soportada sin login previo").getBytes(); //$NON-NLS-1$
				return;
			}
		}
		catch (final SessionException e) {
			removeDownloadSessions(session) ;
			LOGGER.log(Level.WARNING,"Se solicito una operacion sin haber abierto sesion u ocurrio un error al abrirla",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sesion no iniciada"); //$NON-NLS-1$
			result = new String("Sesion no iniciada").getBytes(); //$NON-NLS-1$
			return;
		}
		catch (final UnsupportedEncodingException e) {
			result = new String("La codificación no es valida.").getBytes(); //$NON-NLS-1$
			LOGGER.log(Level.SEVERE,"La codificación no es valida.",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "La codificación no es valida."); //$NON-NLS-1$
			return;
		}
		catch (final IOException e) {
			removeDownloadSessions(session) ;
			LOGGER.log(Level.SEVERE, "Ocurrio un error al procesar la peticion", e); //$NON-NLS-1$
			result = new String("Ocurrio un error al procesar la peticion").getBytes(); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ocurrio un error al procesar la peticion de tratamiento del fichero"); //$NON-NLS-1$
			return;
		}
		catch (final Exception e) {
			removeDownloadSessions(session) ;
			LOGGER.log(Level.SEVERE, "Ocurrio un error al procesar la peticion", e); //$NON-NLS-1$
			result = new String("Ocurrio un error al procesar la peticion.").getBytes(); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ocurrio un error al procesar la peticion"); //$NON-NLS-1$
			return;
		}

		if(getStatusCode() != HttpServletResponse.SC_OK) {
			resp.setStatus(getStatusCode());
		}
		if(!fileClosed) {
			resp.getOutputStream().write(result);
		}
		else {
			resp.getOutputStream().write(new String("Fichero cerrado").getBytes()); //$NON-NLS-1$
		}
		resp.getOutputStream().flush();
	}

	/**
	 * Funci&oacute;n que comprueba el c&oacute;digo de operaci&oacute;n
	 * @param opString
	 * @return
	 * @throws NumberFormatException
	 * @throws UnsupportedOperationException
	 */
	private static ServiceOperations checkOperation(final String opString)
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
	private static boolean needLogin(final ServiceOperations op) {
		// Las operaciones echo, peticion de login y validacion de login, son
		// las unicas que pueden realizarse sin haber establecido logging.
		return 	op != ServiceOperations.ECHO &&
				op != ServiceOperations.REQUEST_LOGIN &&
				op != ServiceOperations.VALIDATE_LOGIN;
	}

	/**
	 *  Funci&oacute;n que comprueba que se haya logado en sessi&oacute;n
	 * @param req
	 * @return
	 */
	private static boolean checkLogin(final HttpServletRequest req) {

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
	private static byte[] echo() {
		return EchoServiceManager.process();
	}

	/**
	 * Funci&oacute;n que retorna una cadena de bytes en formato Json con los datos del token generado,idsesion
	 * @param req
	 * @return
	 * @throws SessionException
	 */
	private static byte[] requestLogin(final HttpServletRequest req) throws SessionException {
		final HttpSession session = req.getSession();
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		return RequestLoginManager.process(session);
	}

	/**
	 * Funci&oacute;n que retorna una cadena de bytes en formato Json indicando si es correcto el login.
	 * @param req
	 * @return
	 * @throws SessionException
	 */
	private static byte[] validateLogin(final HttpServletRequest req) throws SessionException {
		final HttpSession session = req.getSession(false);
		if (session == null) {
			throw new SessionException("No se ha encontrado una sesion iniciada"); //$NON-NLS-1$
		}
		return ValidationLoginManager.process(req, session);
	}
	/**
	 * Funci&oacute;n que retorna una cadena de bytes en formato Json con el resultado de los ficheros con extendi&oacute;n log
	 * exitentes en el servidor con los datos nombre, fecha y tama&ntilde;o en caso de no encontrar ficheros la cadena en formato json
	 * lo indica como mensaje de error.
	 * @return
	 */
	private static byte[] getLogFiles (final File pathLogs)  {
		final byte[] result = LogFilesServiceManager.process(pathLogs);
		return result;
	}

	/**
	 *
	 * @param req
	 * @param resp
	 * @return
	 * @throws SessionException
	 * @throws IOException
	 */
	private static byte[] openFile(final HttpServletRequest req, final HttpServletResponse resp) throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		final byte[] result = LogOpenServiceManager.process(req, resp);
		if(LogOpenServiceManager.getLinfo()!=null) {
			session.setAttribute("LogInfo", LogOpenServiceManager.getLinfo()); //$NON-NLS-1$
		}
		if(LogOpenServiceManager.getChannel()!=null) {
			session.setAttribute("Channel", LogOpenServiceManager.getChannel()); //$NON-NLS-1$
			session.setAttribute("FileSize", new Long (LogOpenServiceManager.getChannel().size())); //$NON-NLS-1$
		}
		if(LogOpenServiceManager.getReader()!=null) {
			session.setAttribute("Reader", LogOpenServiceManager.getReader());	 //$NON-NLS-1$
			session.setAttribute("FilePosition", new Long(0L)); //$NON-NLS-1$
		}

		return result;
	}

	private static boolean closeFile(final HttpServletRequest req) throws SessionException, IOException {
		boolean result = false;
		setStatusCode(HttpServletResponse.SC_OK);
		final HttpSession session = req.getSession(true);
		if (session == null) {
			setStatusCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}

		try {
			if((LogReader) session.getAttribute("Reader") != null){ //$NON-NLS-1$
				session.removeAttribute("Reader"); //$NON-NLS-1$
			}
			if((AsynchronousFileChannel)session.getAttribute("Channel") != null) { //$NON-NLS-1$
				((AsynchronousFileChannel)session.getAttribute("Channel")).close();  //$NON-NLS-1$
				session.removeAttribute("Channel"); //$NON-NLS-1$
			}
			if((LogInfo)session.getAttribute("LogInfo") != null) { //$NON-NLS-1$
				session.removeAttribute("LogInfo"); //$NON-NLS-1$
			}
			if((Long) session.getAttribute("FilePosition") != null ) { //$NON-NLS-1$
				session.removeAttribute("FilePosition"); //$NON-NLS-1$
			}
			if(session.getAttribute("Reader") == null && //$NON-NLS-1$
			   session.getAttribute("Channel") == null && //$NON-NLS-1$
			   session.getAttribute("LogInfo") == null) { //$NON-NLS-1$
				result = true;
			}

		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "No se ha cerrado correctamente el fichero : ".concat(e.getMessage())); //$NON-NLS-1$
			throw new IOException();
		}
		return result;
	}

	private static byte[] getLogTail(final HttpServletRequest req,  final HttpServletResponse resp) throws SessionException, IOException{

		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}

		final byte[] result = LogTailServiceManager.process(req,resp);

		return result;
	}

	private static byte[] getMoreLog(final HttpServletRequest req, final HttpServletResponse resp)throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		final byte[] result = LogMoreServiceManager.process(req,resp);

		return result;
	}

	private static byte[] getLogFiltered(final HttpServletRequest req, final HttpServletResponse resp) throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		final byte[] result = LogFilteredServiceManager.process(req, resp);

		return result;
	}

	private static byte[] searchText(final HttpServletRequest req,final HttpServletResponse resp) throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}

		final byte[] result = LogSearchServiceManager.process(req,resp);

		return result;
	}


	private static byte[] download(final HttpServletRequest req, final HttpServletResponse resp, final File pathLogs) throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}

		final boolean reset = Boolean.parseBoolean(req.getParameter(ServiceParams.PARAM_RESET));
		if(reset) {
			removeDownloadSessions(session) ;
		}
		final byte[] result = LogDownloadServiceManager.process(req, resp, pathLogs.getPath());
		if(LogDownloadServiceManager.isHasMore()) {
			setStatusCode(HttpServletResponse.SC_PARTIAL_CONTENT);
		}
		else {
			removeDownloadSessions(session) ;
			setStatusCode(HttpServletResponse.SC_OK);
		}

		return result;
	}

	private static final int getStatusCode() {
		return statusCode;
	}

	private static final void setStatusCode(final int statusCode) {
		LogService.statusCode = statusCode;
	}

	private static final void removeDownloadSessions(final HttpSession session ) throws IOException {
		if ((SeekableByteChannel)session.getAttribute("ChannelDownload") != null) { //$NON-NLS-1$
			try(final SeekableByteChannel chanel = (SeekableByteChannel)session.getAttribute("ChannelDownload");){ //$NON-NLS-1$
			chanel.close();
			session.removeAttribute("ChannelDownload"); //$NON-NLS-1$
			}
		}
		if((LogDownload)session.getAttribute("Download") != null){ //$NON-NLS-1$
			session.removeAttribute("Download"); //$NON-NLS-1$
		}

		if((Long) session.getAttribute("FileDownloadPos") != null ) { //$NON-NLS-1$
			session.removeAttribute("FileDownloadPos"); //$NON-NLS-1$
		}
	}


}
