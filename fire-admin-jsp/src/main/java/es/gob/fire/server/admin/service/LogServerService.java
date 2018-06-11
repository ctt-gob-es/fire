package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.LogServersDAO;
import es.gob.fire.server.admin.entity.LogServer;
import es.gob.log.consumer.client.LogConsumerClient;

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

	private static final String PARAM_PASSSRV = "clave"; //$NON-NLS-1$

	private  int action = 0;

	private String id_servidor = ""; //$NON-NLS-1$

	private LogServer logSrv = new LogServer();

	private   String nombreSrv =""; //$NON-NLS-1$

	private   String urlSrv = ""; //$NON-NLS-1$

	private   String clave = ""; //$NON-NLS-1$
    private final void setNombreSrv(final String nombreSrv) {
		this.nombreSrv = nombreSrv;
	}

	private final void setUrlSrv(final String urlSrv) {
		this.urlSrv = urlSrv;
	}

	private final void setClave(final String clave) {
		this.clave = clave;
	}

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

		getParameters(request);
		String data = ""; //$NON-NLS-1$
		boolean isOk = false;
		String stringOp = ""; //$NON-NLS-1$
		try {
			switch (this.action) {

				case 1:	//Seleccionar para conectar con servidor

					if(this.logSrv.getNombre() != null && !"".equals(this.logSrv.getNombre()) //$NON-NLS-1$
					&& this.logSrv.getUrl() != null && !"".equals(this.logSrv.getUrl()) && //$NON-NLS-1$
					this.logSrv.getClave() != null && !"".equals(this.logSrv.getClave())) { //$NON-NLS-1$
						final LogConsumerClient logclient = new LogConsumerClient();
						if(this.connectServer(logclient, request)) {
							isOk = true;
							response.sendRedirect(request.getContextPath().toString().concat("/LogAdminService?op=3&").concat(PARAM_NAMESRV).concat("=").concat(this.logSrv.getNombre())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
					if(!isOk) {
						stringOp = "seleccion"; //$NON-NLS-1$
						response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					}
					break;
				case 2: // Nuevo

					if(this.logSrv.getNombre() != null && !"".equals(this.logSrv.getNombre()) //$NON-NLS-1$
						&& this.logSrv.getUrl() != null && !"".equals(this.logSrv.getUrl()) && //$NON-NLS-1$
						this.logSrv.getClave() != null && !"".equals(this.logSrv.getClave())) { //$NON-NLS-1$
						  stringOp = "alta"; //$NON-NLS-1$
						final int resultDML = LogServersDAO.createLogServer(this.logSrv.getNombre(), this.logSrv.getUrl(), this.logSrv.getClave());
						if(resultDML > 0) {//Resultado correcto
							isOk = true;
							response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						}
					}
					break;
				case 3: // Editar / Modificar

					if(this.logSrv.getNombre() != null && !"".equals(this.logSrv.getNombre()) //$NON-NLS-1$
					&& this.logSrv.getUrl() != null && !"".equals(this.logSrv.getUrl()) && //$NON-NLS-1$
					this.logSrv.getClave() != null && !"".equals(this.logSrv.getClave())) { //$NON-NLS-1$
						 stringOp = "edicion"; //$NON-NLS-1$
						final int resultDML = LogServersDAO.updateLogServer(String.valueOf(this.logSrv.getId()),this.logSrv.getNombre(), this.logSrv.getUrl(), this.logSrv.getClave());
						if(resultDML > 0) {//Resultado correcto
							isOk = true;
							response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						}
					}
					break;
				case 4: // Eliminar

					final int resultDML = LogServersDAO.removeServer(this.id_servidor);
					if(resultDML > 0) {//Resultado correcto
						isOk = true;
						response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					}
					break;
				case 5://Visualizar datos

					if(this.logSrv.getNombre() != null && !"".equals(this.logSrv.getNombre()) //$NON-NLS-1$
					&& this.logSrv.getUrl() != null && !"".equals(this.logSrv.getUrl()) && //$NON-NLS-1$
					this.logSrv.getClave() != null && !"".equals(this.logSrv.getClave())) { //$NON-NLS-1$
						response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogServer.jsp?act=5&id-srv=").concat(String.valueOf(this.logSrv.getId()))); //$NON-NLS-1$
					}
					break;
				default://Listar todos los servidores

					  data = LogServersDAO.getLogServersJSON();
					  if(!"".equals(data)) { //$NON-NLS-1$
							response.getWriter().write(data);
						}
					break;
			}
		} catch (final SQLException e) {
			isOk = false;
			response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		} catch (final GeneralSecurityException e) {
			isOk = false;
			response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (final IOException e) {
			isOk = false;
			response.sendRedirect(request.getContextPath().toString().concat("/Logs/LogsMainPage.jsp?op=").concat(stringOp).concat("&r=") + (isOk ? "1" : "0") + "&ent=srv"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}


	/**
	 * Obtiene los par&aacute;metros del HttpServletRequest
	 * @param request
	 */
	private void getParameters(final HttpServletRequest request) {
		if(request.getParameter(LogServerService.PARAM_ACTION) != null && !"".equals(request.getParameter(LogServerService.PARAM_ACTION))) { //$NON-NLS-1$
			this.setAction(Integer.parseInt(request.getParameter(LogServerService.PARAM_ACTION)));
		}
		if(request.getParameter(LogServerService.PARAM_IDSRV) != null && !"".equals(request.getParameter(LogServerService.PARAM_IDSRV))) { //$NON-NLS-1$
			this.setId_servidor(request.getParameter(LogServerService.PARAM_IDSRV));
			if(this.getAction() != 2 && this.getAction() != 3) {
				try {
					this.logSrv = LogServersDAO.selectLogServer(this.getId_servidor());
				} catch (final SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				this.logSrv.setId(Integer.parseInt(request.getParameter(LogServerService.PARAM_IDSRV)));
			}
		}
		if(request.getParameter(LogServerService.PARAM_NAMESRV) != null && !"".equals(request.getParameter(LogServerService.PARAM_NAMESRV))) { //$NON-NLS-1$
			this.setNombreSrv(request.getParameter(LogServerService.PARAM_NAMESRV));
			this.logSrv.setNombre(this.getNombreSrv());
		}
		if(request.getParameter(LogServerService.PARAM_PASSSRV) != null && !"".equals(request.getParameter(LogServerService.PARAM_PASSSRV))) { //$NON-NLS-1$
			this.setClave(request.getParameter(LogServerService.PARAM_PASSSRV));
			this.logSrv.setClave(this.getClave());
		}
		if(request.getParameter(LogServerService.PARAM_URLSRV) != null && !"".equals(request.getParameter(LogServerService.PARAM_URLSRV))) { //$NON-NLS-1$
			this.setUrlSrv(request.getParameter(LogServerService.PARAM_URLSRV));
			this.logSrv.setUrl(this.getUrlSrv());
		}



	}

	/**
	 * Inicializa la conexi&oacute;n con el servidor y se guarda en sesi&oacute;n
	 * @param logclient
	 * @param request
	 * @return
	 */
	private final boolean connectServer(final LogConsumerClient logclient, final HttpServletRequest request)
	{
		boolean connect = false;
		try {
			logclient.init(this.logSrv.getUrl(), this.logSrv.getClave());
			final HttpSession session = request.getSession();
			session.setAttribute("LOG_CLIENT", logclient); //$NON-NLS-1$
			connect = true;
		}
		catch (final IOException e) {
			LOGGER.warning("No se ha podido iniciar el servidor seleccionado. Error:"+e.getMessage()); //$NON-NLS-1$
			return connect;
		}
		return connect;

	}




	private final int getAction() {
		return this.action;
	}

	private final void setAction(final int action) {
		this.action = action;
	}

	private final String getId_servidor() {
		return this.id_servidor;
	}

	private final void setId_servidor(final String id_servidor) {
		this.id_servidor = id_servidor;
	}

	private final String getNombreSrv() {
		return this.nombreSrv;
	}

	private final String getUrlSrv() {
		return this.urlSrv;
	}

	private final String getClave() {
		return this.clave;
	}





}
