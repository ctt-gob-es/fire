package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.client.LogConsumerClient;
import es.gob.log.consumer.client.ServiceOperations;

/**
 * Servlet implementation class LogAdminService
 */
public class LogAdminService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(LogAdminService.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogAdminService() {
        super();
        // TODO Auto-generated constructor stub
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
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final HttpSession session = request.getSession(false);
//		RequestDispatcher dis = null;
//		final ServletContext context = request.getServletContext();
		final String codeInit  = "I9lUuX+iEvzAD/hwaU2MbQ=="; //$NON-NLS-1$ // I9lUuX+iEvzAD/hwaU2MbQ==
		//D/4avRoIIVNTwjPW4AlhPpXuxCU4Mqdhryj/N6xaFQw=
		final String url = "http://localhost:8080/log-consumer-service/Logservice"; //$NON-NLS-1$
		final LogConsumerClient logclient = new LogConsumerClient();
		logclient.init(url, codeInit);
		String result = null;

		final String opString = request.getParameter("op"); //$NON-NLS-1$
		if (opString == null) {
			LOGGER.warning("No se ha indicado codigo de operacion"); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se ha indicado codigo de operacion"); //$NON-NLS-1$
			return;
		}

		ServiceOperations op;
		try {
			op = checkOperation(opString);
		}
		catch (final Exception e) {
			LOGGER.warning(String.format("Codigo de operacion no soportado (%s). Se rechaza la peticion.", opString)); //$NON-NLS-1$
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Codigo de operacion no soportado"); //$NON-NLS-1$
			return;
		}

		switch (op) {
		case GET_LOG_FILES:
			LOGGER.info("Solicitud entrante de listado de ficheros"); //$NON-NLS-1$
			final byte datLogFiles[] = logclient.getLogFiles();
			session.setAttribute("JSON", datLogFiles);
			response.sendRedirect( request.getContextPath().toString().concat("/Logs/LogsFileList.jsp"));
//		    dis = context.getRequestDispatcher("/Logs/LogsMainPage.jsp"); //$NON-NLS-1$
			break;
		case OPEN_FILE:
			LOGGER.info("Solicitud entrante de apertura de fichero"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$
			final String logFileName = request.getParameter("fname"); //$NON-NLS-1$
			final byte datOpenFiles[] = logclient.openFile(logFileName);
			for(int i = 0; i < datOpenFiles.length; i++){
			    	result += (char)datOpenFiles[i];
			    }
				break;
		case CLOSE_FILE:
			LOGGER.info("Solicitud entrante de cierre de fichero"); //$NON-NLS-1$
			//fileClosed = closeFile(req);
			break;
		case TAIL:
			LOGGER.info("Solicitud entrante de consulta del final del log"); //$NON-NLS-1$
			result = ""; //$NON-NLS-1$
			final String name = request.getParameter("fname"); //$NON-NLS-1$
			final int numlines = Integer.parseInt(request.getParameter("nlines")); //$NON-NLS-1$
			final byte datTailFile[] = logclient.getLogTail(numlines, name);
			for(int i = 0; i < datTailFile.length; i++){
			    	result += (char)datTailFile[i];
			    }
			break;
		case GET_MORE:
			LOGGER.info("Solicitud entrante de mas log"); //$NON-NLS-1$
			//result = getMoreLog(req);
			break;
		case SEARCH_TEXT:
			LOGGER.info("Solicitud entrante de busqueda de texto"); //$NON-NLS-1$
			//result = searchText(req);
			break;
		case FILTER:
			LOGGER.info("Solicitud entrante de filtrado de log"); //$NON-NLS-1$
			//result = getLogFiltered(req);
			break;
		case DOWNLOAD:
			LOGGER.info("Solicitud entrante de descarga de fichero"); //$NON-NLS-1$
			//result = download(req);
			break;
		default:
			LOGGER.warning("Operacion no soportada. Este resultado refleja un problema en el codigo del servicio"); //$NON-NLS-1$
			//resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Operacion no soportada sin login previo"); //$NON-NLS-1$
			return;
		}
//		if(dis != null) {
//			dis.forward(request, response);
//		}

//		response.getWriter().write(result);



	}




	private static ServiceOperations checkOperation(final String opString)
			throws NumberFormatException, UnsupportedOperationException {

		final int op = Integer.parseInt(opString);
		return ServiceOperations.parseOperation(op);
	}

}
