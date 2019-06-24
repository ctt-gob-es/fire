package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.LogServersDAO;
import es.gob.fire.server.admin.entity.LogServer;
import es.gob.log.consumer.client.LogConsumerClient;
import es.gob.log.consumer.client.ServiceOperations;

/**
 * Servlet implementation class LogServerService
 */
public class LogServerService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(LogServerService.class.getName());

	private static final String PARAM_ACTION = "act"; //$NON-NLS-1$

	private static final String PARAM_IDSRV = "id-srv"; //$NON-NLS-1$

	private static final String PARAM_NAMESRV = "name-srv"; //$NON-NLS-1$

	private static final String PARAM_URLSRV = "url"; //$NON-NLS-1$

	private static final String PARAM_VERIFY_SSL = "verifyssl"; //$NON-NLS-1$

	private static final String PARAM_PASSSRV = "clave"; //$NON-NLS-1$

	private static final int ACTION_LIST = 0;
    private static final int ACTION_CONNECT = 1;
    private static final int ACTION_CREATE = 2;
    private static final int ACTION_EDIT = 3;
    private static final int ACTION_DELETE = 4;
    private static final int ACTION_VIEW = 5;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public LogServerService() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {



		final Parameters params = getParameters(request);

		LogServer logServer = null;
		if (params.getAction() != ACTION_LIST) {
			try {
				logServer = loadServerConfig(params);
			}
			catch (final Exception e) {
				LOGGER.severe("No se pudo cargar la configuracion del servidor: " + e); //$NON-NLS-1$
				response.sendRedirect(request.getContextPath() +
						"/Logs/LogsMainPage.jsp?op=" + params.getAction() + //$NON-NLS-1$
						"&r=0&ent=srv"); //$NON-NLS-1$
				return;
			}

			if (logServer == null) {
				LOGGER.severe("No se pudo cargar la configuracion del servidor"); //$NON-NLS-1$

				response.sendRedirect(request.getContextPath() +
						"/Logs/LogsMainPage.jsp?op=" + params.getAction() + //$NON-NLS-1$
						"&r=0&ent=srv"); //$NON-NLS-1$
				return;
			}
		}


		try {
			switch (params.getAction()) {

				case ACTION_CONNECT:	// Seleccionar para conectar con servidor
					final LogConsumerClient logclient = new LogConsumerClient();
					logclient.setDisableSslChecks(!logServer.isVerificarSsl());
					if (connectServer(logclient, logServer, request)) {
						response.sendRedirect("log?op=" + ServiceOperations.GET_LOG_FILES.getId() + //$NON-NLS-1$
								"&name-srv=" + logServer.getNombre()); //$NON-NLS-1$
						return;
					}
					break;
				case ACTION_CREATE: // Nuevo
					final int createResult = LogServersDAO.createLogServer(
							logServer.getNombre(), logServer.getUrl(), logServer.getClave(),
							logServer.isVerificarSsl());
					if (createResult > 0) { //Resultado correcto
						response.sendRedirect("Logs/LogsMainPage.jsp?op=" + params.getAction() + //$NON-NLS-1$
								"&r=1&ent=srv"); //$NON-NLS-1$
						return;
					}
					break;
				case ACTION_EDIT: // Editar / Modificar
					final int editResultDML = LogServersDAO.updateLogServer(
							Integer.toString(logServer.getId()), logServer.getNombre(),
							logServer.getUrl(), logServer.getClave(), logServer.isVerificarSsl());
					if (editResultDML > 0) {//Resultado correcto
						response.sendRedirect("Logs/LogsMainPage.jsp?op=" + params.getAction() + //$NON-NLS-1$
								"&r=1&ent=srv"); //$NON-NLS-1$
						return;
					}
					break;
				case ACTION_DELETE: // Eliminar
					final int deleteResult = LogServersDAO.removeServer(Integer.toString(logServer.getId()));
					if (deleteResult > 0) {//Resultado correcto
						response.sendRedirect("Logs/LogsMainPage.jsp?op=" + params.getAction() + //$NON-NLS-1$
								"&r=1&ent=srv"); //$NON-NLS-1$
						return;
					}
					break;
				case ACTION_VIEW: // Visualizar datos
					response.sendRedirect("Logs/LogServer.jsp?act=5&id-srv=" + logServer.getId()); //$NON-NLS-1$
					return;
				default: //Por defecto, se listan todos los servidores
					  final String data = LogServersDAO.getLogServersJSON();
					  if (data != null && !data.isEmpty()) {
							response.getWriter().write(data);
							return;
						}
					break;
			}
		} catch (final SQLException e) {
			LOGGER.log(Level.SEVERE, "No se ha podido ejecutar la operacion", e); //$NON-NLS-1$
		} catch (final GeneralSecurityException e) {
			LOGGER.log(Level.SEVERE, "No se ejecutara la operacion por seguridad", e); //$NON-NLS-1$
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Error de entrada/salida al ejecutar la operacion por seguridad", e); //$NON-NLS-1$
		}

		response.sendRedirect("Logs/LogsMainPage.jsp?op=" + params.getAction() + "&r=0&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Obtiene los par&aacute;metros de la petici&oacute;n.
	 * @param request Petici&oacute;n realizada.
	 * @throws IllegalArgumentException Se proporcionaron parametros erroneos
	 * o falta alguno obligatorio.
	 */
	private static Parameters getParameters(final HttpServletRequest request) throws IllegalArgumentException {

		final Parameters params = new Parameters();

		final String actionParam = request.getParameter(LogServerService.PARAM_ACTION);
		if (actionParam != null && !actionParam.isEmpty()) {
			try {
				params.setAction(Integer.parseInt(actionParam));
			}
			catch (final Exception e) {
				LOGGER.warning("Se proporciono un ID de accion mal formado: " + LogUtils.cleanText(actionParam)); //$NON-NLS-1$
			}
		}
		else {
			LOGGER.warning("No se proporciono un ID de accion"); //$NON-NLS-1$
		}

		final String id = request.getParameter(PARAM_IDSRV);
		if (id != null && !id.isEmpty()) {
			params.setIdServidor(id);
		}

		final String name = request.getParameter(PARAM_NAMESRV);
		if (name != null && !name.isEmpty()) {
			params.setNombreSrv(name);
		}

		final String key = request.getParameter(PARAM_PASSSRV);
		if (key != null && !key.isEmpty()) {
			params.setClave(key);
		}

		final String url = request.getParameter(PARAM_URLSRV);
		if (url != null && !url.isEmpty()) {
			params.setUrlSrv(url);
		}

		final String verificarSsl = request.getParameter(PARAM_VERIFY_SSL);
		params.setVerificarSsl(Boolean.parseBoolean(verificarSsl));

		return params;
	}

	/**
	 * Carga la informaci&oacute;n de servidor de logs.
	 * @param params Par&aacute;metros proporcionados por la petici&oacute;n.
	 * @return Informaci&oacute;n del servidor de logs requerida para ejecutar la operaci&oacute;n solicitada.
	 * @throws IOException Cuando no se pudo obtener la informaci&oacute;n del servidor.
	 * @throws IllegalArgumentException Cuando no se proporcion&oacute; al&uacute;n dato necesario en la petici&oacute;n.
	 */
	private static LogServer loadServerConfig(final Parameters params) throws IOException {
		final LogServer logServer;

		switch (params.getAction()) {
		case ACTION_CREATE:
			if (params.getNombreSrv() == null) {
				throw new IllegalArgumentException("No se proporciono el nombre del servidor en una accion que lo requiere"); //$NON-NLS-1$
			}
			if (params.getClave() == null) {
				throw new IllegalArgumentException("No se proporciono la clave del servidor en una accion que lo requiere"); //$NON-NLS-1$
			}
			if (params.getUrlSrv() == null) {
				throw new IllegalArgumentException("No se proporciono la URL del servidor en una accion que lo requiere"); //$NON-NLS-1$
			}

			logServer = new LogServer();
			logServer.setNombre(params.getNombreSrv());
			logServer.setClave(params.getClave());
			logServer.setUrl(params.getUrlSrv());
			logServer.setVerificarSsl(params.isVerificarSsl());
			break;

		case ACTION_EDIT:
			if (params.getIdServidor() == null) {
				throw new IllegalArgumentException("No se proporciono el identificador del servidor en una accion que lo requiere"); //$NON-NLS-1$
			}
			int id;
			try {
				id = Integer.parseInt(params.getIdServidor());
			}
			catch (final Exception e) {
				throw new IllegalArgumentException("Se proporciono un identificador del servidor de logs con formato no valido", e); //$NON-NLS-1$
			}
			if (params.getNombreSrv() == null) {
				throw new IllegalArgumentException("No se proporciono el nombre del servidor en una accion que lo requiere"); //$NON-NLS-1$
			}
			if (params.getClave() == null) {
				throw new IllegalArgumentException("No se proporciono la clave del servidor en una accion que lo requiere"); //$NON-NLS-1$
			}
			if (params.getUrlSrv() == null) {
				throw new IllegalArgumentException("No se proporciono la URL del servidor en una accion que lo requiere"); //$NON-NLS-1$
			}

			logServer = new LogServer();
			logServer.setId(id);
			logServer.setNombre(params.getNombreSrv());
			logServer.setClave(params.getClave());
			logServer.setUrl(params.getUrlSrv());
			logServer.setVerificarSsl(params.isVerificarSsl());
		break;
		case ACTION_CONNECT:
		case ACTION_DELETE:
		case ACTION_VIEW:
			if (params.getIdServidor() == null) {
				throw new IllegalArgumentException("No se proporciono el identificador del servidor en una accion que lo requiere"); //$NON-NLS-1$
			}
			try {
				logServer = LogServersDAO.selectLogServer(params.getIdServidor());
			} catch (final SQLException e) {
				LOGGER.log(Level.SEVERE, "No se pudo acceder a la tabla de servidores de log", e); //$NON-NLS-1$
				throw new IOException("No se pudo cargar de BD el servidor de log: " + params.getIdServidor(), e); //$NON-NLS-1$
			}
			break;
		default:
			logServer = null;
			break;
		}

		return logServer;
	}


	/**
	 * Inicializa la conexi&oacute;n con el servidor y se guarda en sesi&oacute;n
	 * @param logClient
	 * @param logServer
	 * @param request
	 * @return
	 */
	private final static boolean connectServer(final LogConsumerClient logClient, final LogServer logServer, final HttpServletRequest request)
	{
		boolean connect = false;
		try {
			logClient.setDisableSslChecks(!logServer.isVerificarSsl());
			logClient.init(logServer.getUrl(), logServer.getClave());
			final HttpSession session = request.getSession(false);
			session.setAttribute(ServiceParams.SESSION_ATTR_LOG_CLIENT, logClient);
			connect = true;
		}
		catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Error al conectar con el servidor de logs", e); //$NON-NLS-1$
			return connect;
		}
		catch (final SecurityException e) {
			LOGGER.log(Level.SEVERE, "El servidor de logs rechazo la conexion", e); //$NON-NLS-1$
			return connect;
		}
		return connect;

	}

	/**
	 * Par&aacute;metros que pueden recuperarse de una petici&oacute;n a este servicio.
	 */
	private static class Parameters {

		private int action = -1;

		private String idServidor = null;

		private String nombreSrv = null;

		private String urlSrv = null;

		private boolean verificarSsl = true;

		private String clave = null;

		Parameters() {
			// Constructor por defecto
		}

		final int getAction() {
			return this.action;
		}

		final void setAction(final int action) {
			this.action = action;
		}

		final String getIdServidor() {
			return this.idServidor;
		}

		final void setIdServidor(final String idServidor) {
			this.idServidor = idServidor;
		}

		final String getNombreSrv() {
			return this.nombreSrv;
		}

	    final void setNombreSrv(final String nombreSrv) {
			this.nombreSrv = nombreSrv;
		}

		final String getUrlSrv() {
			return this.urlSrv;
		}

		final void setUrlSrv(final String urlSrv) {
			this.urlSrv = urlSrv;
		}

		final boolean isVerificarSsl() {
			return this.verificarSsl;
		}

		final void setVerificarSsl(final boolean verificarSsl) {
			this.verificarSsl = verificarSsl;
		}

		final String getClave() {
			return this.clave;
		}

		final void setClave(final String clave) {
			this.clave = clave;
		}
	}
}
