<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO"%>
<%@page import="es.gob.fire.server.admin.conf.DbManager"%>
<%@page import="es.gob.fire.server.admin.dao.ConfigurationDAO" %>
<%@page import="es.gob.fire.server.admin.message.MessageResult" %>
<%@page import="es.gob.fire.server.admin.message.MessageResultManager" %>
<%@page import="es.gob.fire.server.admin.entity.User" %>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>
<%@page import="java.util.List" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%  String errorText = null;

final Object state = request.getSession().getAttribute("initializedSession"); //$NON-NLS-1$
final String usrLogged= (String) request.getSession().getAttribute("user");
if (state == null || !Boolean.parseBoolean((String) state)) {
	response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	return;
}



//Logica para determinar si mostrar un resultado de operacion
	String op = request.getParameter("op"); //$NON-NLS-1$
	String result = request.getParameter("r"); //$NON-NLS-1$
	String entity= request.getParameter("ent"); //$NON-NLS-1$
	MessageResult mr = MessageResultManager.analizeResponse(op, result,entity);
		
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Gesti&oacute;n de servidores de Log FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>	
	
</head>
<body>
<script>

</script>
<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
		
	<!-- contenido -->
	<div id="container">
	
	<div id="menu-bar">
		<input class="menu-btn" name="add-serv-btn" type="button" value="Alta de servidor" title="Crear un nuevo servidor de log" onclick="location.href='./LogServer.jsp?act=2'"/>
	</div>
	<% if(errorText != null) { %>
		<p id="error-txt"><%= errorText %></p> 
	<%
		errorText = null;
	  }
	%>
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  Servidores de Log, dados de alta en el sistema.
			</p>
		</div>
		
			<% if(mr != null) { %>
				<p id="<%=
						mr.isOk() ? "success-txt" : "error-txt"  //$NON-NLS-1$ //$NON-NLS-2$
						%>">
					<%= mr.getMessage() %>
				</p>
			<% } %>
			
		<div id="data">		
		</div>

   	</div>

</body>
<script src="../resources/js/log_server.js" type="text/javascript"></script>
</html>