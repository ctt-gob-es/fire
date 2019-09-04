package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.AplicationsDAO;
import es.gob.fire.server.admin.dao.UsersDAO;

public class UsersService extends HttpServlet{


	@Override
	protected  void doGet(final HttpServletRequest request, final HttpServletResponse response) throws  IOException {

	final HttpSession session = request.getSession(false);
	if (session == null) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
		return;
	}
	//final String requ = request.getParameter("requestTypeCount"); //$NON-NLS-1$
	final String req = request.getParameter("requestType"); //$NON-NLS-1$
	final String id = request.getParameter("id-users");//$NON-NLS-1$
	String data = ""; //$NON-NLS-1$



	if (req.equals("countRecordsUsers")){ //$NON-NLS-1$
	    data = UsersDAO.getUsersCount();
	}


	if (req.equals("All")){ //$NON-NLS-1$
	    data = UsersDAO.getUsersJSON();
	}


	if (req.equals("getRecords")){ //$NON-NLS-1$
		final String start = request.getParameter("currentIndex"); //$NON-NLS-1$
		final String total = request.getParameter("recordsToFetch"); //$NON-NLS-1$
		data = UsersDAO.getUsersPag(start, total);
	}

	if (req.equals("countRecordsUsersApp") && id != null && !"null".equals(id) && !"".equals(id)){//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    data = AplicationsDAO.getApplicationsCountByUsersJSON(id);
	}

	if (req.equals("getRecordsUsersApp")&& id != null && !"null".equals(id) && !"".equals(id)){//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		data = AplicationsDAO.getApplicationsByUserJSON(id);
	}

	try (final PrintWriter out = response.getWriter();) {
		out.print(data);
	}

	}

}
