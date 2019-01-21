<%@page import="java.io.StringReader"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.message.MessageResult" %>
<%@page import="es.gob.fire.server.admin.message.MessageResultManager" %>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="javax.json.Json"%>
<%@page import="javax.json.JsonObject"%>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%

	if (session == null) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	String errorText = null;

	final Object state = session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED);
	final String usrLogged= (String) session.getAttribute(ServiceParams.SESSION_ATTR_USER);
	if (state == null || !Boolean.parseBoolean((String) state)) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}
	
	final String jsonError = (String) session.getAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON);
	if(jsonError != null){
		final JsonReader reader = Json.createReader(new StringReader(jsonError));
		final JsonObject jsonObj = reader.readObject();
		reader.close();
		if(jsonObj.getJsonArray("Error") != null){ //$NON-NLS-1$
			final JsonArray Error = jsonObj.getJsonArray("Error");  //$NON-NLS-1$
			for(int i = 0; i < Error.size(); i++){
				final JsonObject json = Error.getJsonObject(i);
				errorText = "Error:" +String.valueOf(json.getInt("Code")) + "  " + json.getString("Message");//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
			}	
		} 
	}
	session.removeAttribute(ServiceParams.SESSION_ATTR_ERROR_JSON);
	
	
	//Logica para determinar si mostrar un resultado de operacion
	String op = request.getParameter("op"); //$NON-NLS-1$
	String result = request.getParameter("r"); //$NON-NLS-1$
	String entity= request.getParameter("ent"); //$NON-NLS-1$
	MessageResult mr = MessageResultManager.analizeResponse(op, result,entity);
		
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  Servidores de Log, dados de alta en el sistema.
			</p>
		</div>
		
			<% if(mr != null) { %>
				<p id="<%=
						mr.isOk() ? "success-txt" : "error-txt"  //$NON-NLS-1$ //$NON-NLS-2$
						%>">
					<%=
					errorText != null ? mr.getMessage() + ". " + errorText : mr.getMessage() //$NON-NLS-1$
					%>
				</p>
			<% } %>
			
		<div id="data">		
		</div>

   	</div>

</body>
<script src="../resources/js/log_server.js" type="text/javascript"></script>
</html>