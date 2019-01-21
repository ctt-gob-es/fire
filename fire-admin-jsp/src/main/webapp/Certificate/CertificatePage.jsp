<%@page import="es.gob.fire.server.admin.message.MessageResult" %>
<%@page import="es.gob.fire.server.admin.message.MessageResultManager" %>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	if (session == null) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	// Logica para determinar si mostrar un resultado de operacion
	String op = request.getParameter("op"); //$NON-NLS-1$
	String result = request.getParameter("r"); //$NON-NLS-1$
	String entity = request.getParameter("ent"); //$NON-NLS-1$
	String msg = request.getParameter("msg");//$NON-NLS-1$
	MessageResult mr = MessageResultManager.analizeResponse(op, result, entity);
		
	
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Administraci&oacute;n FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
			
</head>
<body>

	<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	
	<!-- contenido -->
	<div id="container">
	
	<div id="menu-bar"  style="display: text-align:right;">
	<input class="menu-btn" name="add-usr-btn" type="button" value="Alta certificado" title="Crear una nueva aplicaci&oacute;n" onclick="location.href='NewCertificate.jsp?op=1'"/>
	</div>

		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  Certificados dados de alta en el sistema.
			</p>
		</div>
		
			<% if (mr != null) { %>
				<p id="<%=
						mr.isOk() ? "success-txt" : "error-txt"  //$NON-NLS-1$ //$NON-NLS-2$
						%>">
					<%= msg != null && !msg.isEmpty() ? mr.getMessage() + msg : mr.getMessage() %>
				</p>
			<% } %>
		<div id="data" style="display: block-inline; text-align:center;">
			<h4>No hay Certificados</h4>		
		</div>        
	
   	</div>
	
</body>
<script type="text/javascript">
	/*Importante estas variables (requestTypeCount y requestType) deben estar declaradas antes que la llamada a ../resources/js/certificate.js*/
			requestTypeCount="countRecordsCert";
			requestType="All";
//          requestType="getRecordsCert";
	</script>
	<script src="../resources/js/certificate.js" type="text/javascript"></script>
	
</html>
