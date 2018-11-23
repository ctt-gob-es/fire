<%@page import="es.gob.fire.server.admin.dao.UsersDAO" %>
<%

if (session == null) {
	response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	return;
}

String req = request.getParameter("requestType"); //$NON-NLS-1$
String data = ""; //$NON-NLS-1$

if (req.equals("countRecords")){ //$NON-NLS-1$
    data = UsersDAO.getUsersCount();
}
if (req.equals("All")){ //$NON-NLS-1$
    data = UsersDAO.getUsersJSON();
}
if (req.equals("getRecords")){ //$NON-NLS-1$
	String start = request.getParameter("currentIndex"); //$NON-NLS-1$
	String total = request.getParameter("recordsToFetch"); //$NON-NLS-1$
	data = UsersDAO.getUsersPag(start, total);
}

out.print(data);
%>