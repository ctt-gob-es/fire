package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

		final File f = ConfigManager.getInstance().getLogsDir();
		if (!f.exists()) {
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
//					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result = new String("Operacion no soportada sin login previo a pesar de estar marcada como tal. Este resultado refleja un problema en el codigo del servicio").getBytes(); //$NON-NLS-1$
					return;
				}
			}
			// Comprobamos si el usuario esta registrado
			else if (checkLogin(req)) {

				switch (op) {
				case GET_LOG_FILES:
					LOGGER.info("Solicitud entrante de listado de ficheros"); //$NON-NLS-1$
					result = getLogFiles();
					if(result == null) {
						resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se han encontrado ficheros log en el servidor indicado"); //$NON-NLS-1$
						result = new String("No se han encontrado ficheros log en el servidor indicado").getBytes(); //$NON-NLS-1$
						return;
					}
					break;
				case OPEN_FILE:
					LOGGER.info("Solicitud entrante de apertura de fichero"); //$NON-NLS-1$
					result = openFile(req);
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
					result = download(req,resp);
					break;
				default:
					LOGGER.warning("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Operacion no soportada. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
//					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result = new String("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio").getBytes(); //$NON-NLS-1$
					return;
				}
			}
			else {
				LOGGER.warning("Operacion no soportada sin login previo"); //$NON-NLS-1$
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada sin login previo"); //$NON-NLS-1$
//				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				result = new String("Operacion no soportada sin login previo").getBytes(); //$NON-NLS-1$
				return;
			}
		}
		catch (final SessionException e) {
			LOGGER.log(Level.WARNING,"Se solicito una operacion sin haber abierto sesion u ocurrio un error al abrirla",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Sesion no iniciada"); //$NON-NLS-1$
			result = new String("Sesion no iniciada").getBytes(); //$NON-NLS-1$
			return;
		}
		catch (final UnsupportedEncodingException e) {
//			resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			result = new String("La codificación no es valida.").getBytes(); //$NON-NLS-1$
			LOGGER.log(Level.SEVERE,"La codificación no es valida.",e); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "La codificación no es valida."); //$NON-NLS-1$
			return;
		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Ocurrio un error al procesar la peticion", e.getMessage()); //$NON-NLS-1$
			result = new String("Ocurrio un error al procesar la peticion").getBytes(); //$NON-NLS-1$
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ocurrio un error al procesar la peticion de tratamiento del fichero"); //$NON-NLS-1$
			return;
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Ocurrio un error al procesar la peticion", e.getMessage()); //$NON-NLS-1$
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

	private static ServiceOperations checkOperation(final String opString)
			throws NumberFormatException, UnsupportedOperationException {

		final int op = Integer.parseInt(opString);
		return ServiceOperations.parseOperation(op);
	}

	private static boolean needLogin(final ServiceOperations op) {
		// Las operaciones echo, peticion de login y validacion de login, son
		// las unicas que pueden realizarse sin haber establecido logging.
		return 	op != ServiceOperations.ECHO &&
				op != ServiceOperations.REQUEST_LOGIN &&
				op != ServiceOperations.VALIDATE_LOGIN;
	}

	private static boolean checkLogin(final HttpServletRequest req) {

		boolean logged = false;

		final HttpSession session = req.getSession(false);
		if (session != null) {
			final Object loggedValue = session.getAttribute(SessionParams.LOGGED);
			logged = loggedValue != null ? ((Boolean) loggedValue).booleanValue() : false;
		}

		return logged;
	}

	private static byte[] echo() {
		return EchoServiceManager.process();
	}

	private static byte[] requestLogin(final HttpServletRequest req) throws SessionException {
		final HttpSession session = req.getSession();
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		return RequestLoginManager.process(session);
	}

	private static byte[] validateLogin(final HttpServletRequest req) throws SessionException {
		final HttpSession session = req.getSession(false);
		if (session == null) {
			throw new SessionException("No se ha encontrado una sesion iniciada"); //$NON-NLS-1$
		}
		return ValidationLoginManager.process(req, session);
	}

	private static byte[] getLogFiles ()  {
		final byte[] result = LogFilesServiceManager.process();
		return result;
	}

	private static byte[] openFile(final HttpServletRequest req) throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		final byte[] result = LogOpenServiceManager.process(req);
		if(LogOpenServiceManager.getLinfo()!=null) {
			session.setAttribute("LogInfo", LogOpenServiceManager.getLinfo()); //$NON-NLS-1$
		}
		if(LogOpenServiceManager.getChannel()!=null) {
			session.setAttribute("Channel", LogOpenServiceManager.getChannel()); //$NON-NLS-1$
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

//		if(LogTailServiceManager.getError() != null) {
//			setStatusCode(LogTailServiceManager.getError().getNumError());
//		}else {
//			setStatusCode(HttpServletResponse.SC_OK);
//		}
		return result;
	}

	private static byte[] getMoreLog(final HttpServletRequest req, final HttpServletResponse resp)throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		final byte[] result = LogMoreServiceManager.process(req,resp);

//		if(LogMoreServiceManager.getError()!=null) {
//			setStatusCode(LogMoreServiceManager.getError().getNumError());
//		}
//		else if(LogMoreServiceManager.getStatus() != HttpServletResponse.SC_OK) {
//			setStatusCode(LogMoreServiceManager.getStatus());
//		}
//		else {
//			setStatusCode(HttpServletResponse.SC_OK);
//		}
		return result;
	}

	private static byte[] getLogFiltered(final HttpServletRequest req, final HttpServletResponse resp) throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		final byte[] result = LogFilteredServiceManager.process(req, resp);

//		if( LogFilteredServiceManager.getError() != null  && LogFilteredServiceManager.getError().getNumError() != 0) {
//			setStatusCode(LogFilteredServiceManager.getError().getNumError());
//		}else if (LogFilteredServiceManager.getStatus() != HttpServletResponse.SC_OK) {
//			setStatusCode(LogFilteredServiceManager.getStatus());
//		}
//		else {
//			setStatusCode(HttpServletResponse.SC_OK);
//		}

		return result;
	}

	private static byte[] searchText(final HttpServletRequest req,final HttpServletResponse resp) throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
//		LogSearchServiceManager.setError(null);
		final byte[] result = LogSearchServiceManager.process(req,resp);
//		if(LogSearchServiceManager.getError() != null) {
//			setStatusCode(LogSearchServiceManager.getError().getNumError());
//		}
//		else if(LogSearchServiceManager.getStatus() != HttpServletResponse.SC_OK) {
//			setStatusCode(LogSearchServiceManager.getStatus());
//		}
//		else {
//			setStatusCode(HttpServletResponse.SC_OK);
//		}
		return result;
	}

	private static byte[] compress(final HttpServletRequest req) {
		throw new UnsupportedOperationException();
	}

	private static byte[] compressChecking(final HttpServletRequest req) {
		throw new UnsupportedOperationException();
	}

	private static byte[] download(final HttpServletRequest req, final HttpServletResponse resp) throws SessionException, IOException {
		final HttpSession session = req.getSession(true);
		if (session == null) {
			throw new SessionException("No ha sido posible crear la sesion"); //$NON-NLS-1$
		}
		final byte[] result = LogDownloadServiceManager.process(req, resp);
		if(LogDownloadServiceManager.isHasMore()) {
			resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		}

		return result;
	}

	private static final int getStatusCode() {
		return statusCode;
	}

	private static final void setStatusCode(final int statusCode) {
		LogService.statusCode = statusCode;
	}



}
