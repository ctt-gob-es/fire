<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO" %>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%
String req=request.getParameter("requestType");
String id=request.getParameter("id-cert");
String data="";

if(req.equals("countRecordsCert")){
    data= CertificatesDAO.getCertificatesCount();
}
if(req.equals("All")){
    data= CertificatesDAO.getCertificatesJSON();
}

//Paginado
if(req.equals("getRecordsCert")){
	String start=request.getParameter("currentIndex");
	String total=request.getParameter("recordsToFetch");
	data= CertificatesDAO.getCertificatesPag(start, total);
}

if(req.equals("countRecordsCertApp") && id!=null && !"null".equals(id) && !"".equals(id)){
    data= AplicationsDAO.getApplicationsCountByCertificate(id);
}

if(req.equals("getRecordsCertApp")&& id!=null && !"null".equals(id) && !"".equals(id)){
	String start=request.getParameter("currentIndex");
	String total=request.getParameter("recordsToFetch");
	//data=AplicationsDAO.getApplicationsPagByCertificate(id, start, total);
	data=AplicationsDAO.getApplicationsByCertificateJSON(id);
}


out.print(data);
%>