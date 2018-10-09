<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO" %>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%

if (session == null) {
	response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	return;
}

String req = request.getParameter("requestType");//$NON-NLS-1$ 
String id = request.getParameter("id-cert");//$NON-NLS-1$ 
String data = "";//$NON-NLS-1$ 

if(req.equals("countRecordsCert")){//$NON-NLS-1$ 
    data = CertificatesDAO.getCertificatesCount();
}
if(req.equals("All")){//$NON-NLS-1$ 
    data = CertificatesDAO.getCertificatesJSON();
}

//Paginado
if(req.equals("getRecordsCert")){//$NON-NLS-1$ 
	String start = request.getParameter("currentIndex");//$NON-NLS-1$ 
	String total = request.getParameter("recordsToFetch");//$NON-NLS-1$ 
	data = CertificatesDAO.getCertificatesPag(start, total);
}

if(req.equals("countRecordsCertApp") && id != null && !"null".equals(id) && !"".equals(id)){//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
    data = AplicationsDAO.getApplicationsCountByCertificate(id);
}

if(req.equals("getRecordsCertApp")&& id != null && !"null".equals(id) && !"".equals(id)){//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	String start = request.getParameter("currentIndex");//$NON-NLS-1$ 
	String total = request.getParameter("recordsToFetch");//$NON-NLS-1$ 
	//data=AplicationsDAO.getApplicationsPagByCertificate(id, start, total);
	data = AplicationsDAO.getApplicationsByCertificateJSON(id);
}


out.print(data);
%>