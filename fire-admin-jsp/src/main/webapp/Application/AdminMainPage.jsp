<%@page import="java.text.SimpleDateFormat"%>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO"%>
<%@page import="es.gob.fire.server.admin.conf.DbManager"%>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%@page import="es.gob.fire.server.admin.entity.Application" %>
<%@page import="es.gob.fire.server.admin.message.MessageResult" %>
<%@page import="es.gob.fire.server.admin.message.MessageResultManager" %>
<%@page import="es.gob.fire.server.admin.message.AdminFilesNotFoundException" %>
<%@page import="java.util.List" %>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	String user="";
	String errorText = null;
	try {
		DbManager.initialize();
	}
	catch (AdminFilesNotFoundException e){
		response.sendRedirect("../Error/FileNotFound.jsp?file=" + AdminFilesNotFoundException.getFileName()); //$NON-NLS-1$
		return;
	}
	catch (Exception e){
		response.sendRedirect("../Error/SevereError.jsp?msg=" + e.toString()); //$NON-NLS-1$
		return;
	}

	Object state = request.getSession().getAttribute("initializedSession"); //$NON-NLS-1$
	if (state == null) {
		// Leemos la contrasena de entrada
		String psswd = request.getParameter("password"); //$NON-NLS-1$
		user = request.getParameter("user");
		// Comprobamos la contrasena
		if (psswd == null || user==null) {
			response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
			return;
		}

		try {
			if (!UsersDAO.checkAdminPassword(psswd,user)) {
				response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
				return;	
			}
		}
		catch (Exception e) {
			response.sendRedirect("../Error/SevereError.jsp?msg=" + e.toString()); //$NON-NLS-1$
			return;
		}

		// Marcamos la sesion como iniciada 
		request.getSession().setAttribute("initializedSession", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		request.getSession().setAttribute("user", user);//$NON-NLS-1$ //$NON-NLS-2$
	}
	else if (!"true".equals(state)) { //$NON-NLS-1$
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	}

	// Logica para determinar si mostrar un resultado de operacion
	String op = request.getParameter("op"); //$NON-NLS-1$
	String result = request.getParameter("r"); //$NON-NLS-1$
	String entity= request.getParameter("ent"); //$NON-NLS-1$
	MessageResult mr = MessageResultManager.analizeResponse(op, result,entity);
	
	
	
	
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Administraci&oacute;n FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<link rel="stylesheet" href="../resources/css/jquery-ui.min.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/jquery-ui.min.js" type="text/javascript"></script>
	<script src="../resources/js/application.js" type="text/javascript"></script>
		
</head>
<body>

	<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	
	<!-- contenido -->
	<div id="container">
	
	<div id="menu-bar"  style="display: text-align:right;">
	<input class="menu-btn" name="add-usr-btn" type="button" value="Alta de aplicaci&oacute;n" title="Crear una nueva aplicaci&oacute;n" onclick="location.href='NewApplication.jsp?op=1'"/>
<!-- 		<a class="menu-btn" href="NewApplication.jsp?op=1" >Alta de aplicaci&oacute;n</a> -->
	</div>
	<% if(errorText != null) { %>
		<p id="error-txt"><%= errorText %></p> 
	<%
		errorText = null;
	  }
	%>
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  Aplicaciones dadas de alta en el sistema.
			</p>
		</div>
		
			<% if(mr != null) { %>
				<p id="<%=
						mr.isOk() ? "success-txt" : "error-txt"  //$NON-NLS-1$ //$NON-NLS-2$
						%>">
					<%= mr.getMessage() %>
				</p>
			<% } %>
		<div id="data" style="display: block-inline; text-align:center;">
			<h4>No hay Aplicaciones</h4>"	
		</div>
	<br>
	<div style="display: block-inline; text-align:right;">
		<button id="back">Anterior</button>
        <button id="next">Siguiente</button>
        <p id="page"></p>
	</div>
        
	
   	</div>
	
</body>
</html>
