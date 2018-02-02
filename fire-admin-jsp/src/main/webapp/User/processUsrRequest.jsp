<%@page import="es.gob.fire.server.admin.dao.UsersDAO" %>
<%
String req=request.getParameter("requestType");
String data="";

if(req.equals("countRecords")){
    data= UsersDAO.getUsersCount();
}

if(req.equals("getRecords")){
	String start=request.getParameter("currentIndex");
	String total=request.getParameter("recordsToFetch");
	data= UsersDAO.getUsersPag(start, total);
}

out.print(data);
%>