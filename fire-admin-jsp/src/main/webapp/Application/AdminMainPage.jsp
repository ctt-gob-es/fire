<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO"%>
<%@page import="es.gob.fire.server.admin.message.MessageResult" %>
<%@page import="es.gob.fire.server.admin.message.MessageResultManager" %>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	if (session == null) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	String errorText = null;
	String valueText = null;

	// Logica para determinar si mostrar un resultado de operacion
	String op = request.getParameter("op"); //$NON-NLS-1$
	String result = request.getParameter("r"); //$NON-NLS-1$
	String entity= request.getParameter("ent"); //$NON-NLS-1$
	MessageResult mr = MessageResultManager.analizeResponse(op, result,entity);
	
	
	 if  ("edicion".equals(op) && "1".equals(result)) { //$NON-NLS-1$ //$NON-NLS-2$
		valueText = "La aplicacion ha sido modificada correctamente";
	 
	}else if  ("baja".equals(op) && "1".equals(result)) { //$NON-NLS-1$ //$NON-NLS-2$
		valueText = "La aplicacion ha sido borrada correctamente"; //$NON-NLS-1$
		
	}else if ("alta".equals(op) && "1".equals(result)) { //$NON-NLS-1$ //$NON-NLS-2${
		valueText = "La aplicacion ha sido creada correctamente"; //$NON-NLS-1$
	}

%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Administraci&oacute;n FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
<!--	<link rel="stylesheet" href="../resources/css/jquery-ui.min.css"> -->
<!-- 	<link rel="stylesheet" href="../resources/css/datatables.min.css"> -->
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/jquery-ui.min.js" type="text/javascript"></script>
<!-- 	<script src="../resources/js/datatables.min.js" type="text/javascript"></script> -->

		
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
	<% if (valueText != null) { %>
		<p id="success-txt"><%= valueText %></p> 
	<%
	valueText = null;
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
					<%=  mr.getMessage() %>
				</p>
			<% } %>
			
			
		<div id="data" style="display: block-inline; text-align:center;">
		
			<h4>No hay Aplicaciones</h4>	
		</div>
   	</div>
	
</body>
<script src="../resources/js/validateAplications.js" type="text/javascript"></script>

</html>
