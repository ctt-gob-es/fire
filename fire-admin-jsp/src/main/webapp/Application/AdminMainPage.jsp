

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
		String user = request.getParameter("user");
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
</head>
<body>
<script>
	function confirmar() { 
		   if (confirm('¿Está seguro de eliminar esta aplicación?')) { 
		      document.tuformulario.submit();
		      return true;
		   }
		   return false;
	}
</script>
	<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	
	<!-- contenido -->
	<div id="container">
	
	<div id="menu-bar">
		<a class="menu-btn" href="NewApplication.jsp?op=1" >Alta de aplicaci&oacute;n</a>
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
		
		<table class="admin-table">
		<thead>
		<tr><td>Aplicaci&oacute;n</td><td>ID</td><td>Responsable</td><td>Fecha Alta</td><td>Acciones</td></tr>
		</thead>
		<%
			List<Application> apps;
			try {
				apps = AplicationsDAO.getApplications();
			}
			catch (Exception e) {
				response.sendRedirect("../Error/SevereError.jsp?msg=" + e.toString()); //$NON-NLS-1$
				return;
			}
			for (Application app : apps) {
		%>
			<tr>
				<td><%= app.getNombre() %></td>
				<td><%= app.getId() %></td>
				<td><%= app.getResponsable() %><br>
					<% if (app.getCorreo() != null && app.getCorreo().length() > 0) { %>
						<a href="mailto://<%= app.getCorreo() %>"><%= app.getCorreo() %></a>
					<% } %>
					<% if (app.getTelefono() != null && app.getTelefono().length() > 0) { %> 
						(<a href="tel://<%= app.getTelefono() %>"><%= app.getTelefono() %></a>)
					<% } %> 
				</td>
				<td><%=Utils.getStringDateFormat(app.getAlta()) %></td>
				<td>
					<a href="NewApplication.jsp?id-app=<%= app.getId() %>&op=0"><img src="../resources/img/details_icon.png"/></a>
					<a href="NewApplication.jsp?id-app=<%= app.getId() %>&op=2"><img src="../resources/img/editar_icon.png"/></a>
					<a href="deleteApp?id-app=<%= app.getId() %>"><img src="../resources/img/delete_icon.png" onclick="return confirmar()"/></a>
				</td>
			</tr>
		<%
			}
		%>
		
		</table>
   	</div>
</body>
</html>
