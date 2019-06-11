<%@page import="es.gob.fire.server.admin.message.UserMessages"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
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

final Object state = session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED);
final String usrLogged= (String) session.getAttribute(ServiceParams.SESSION_ATTR_USER);
if (state == null || !Boolean.parseBoolean((String) state)) {
	response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	return;
}

//Este parametro comprueba si el usuario ha introducido su nombre de usuario y contrasena
// En caso de que haya introducido datos erroneos, se recibe un "fail"
String err = request.getParameter(ServiceParams.PARAM_ERR); //$NON-NLS-1$
String errMsg = null;
if (err != null) {
	errMsg = UserMessages.parse(err).getText();
}
String succ = request.getParameter(ServiceParams.PARAM_SUCCESS); //$NON-NLS-1$
String succMsg = null;
if (succ != null) {
	succMsg = UserMessages.parse(succ).getText();
}

//Logica para determinar si mostrar un resultado de operacion
	String op = request.getParameter("op"); //$NON-NLS-1$
	String result = request.getParameter("r"); //$NON-NLS-1$
	String entity= request.getParameter("ent"); //$NON-NLS-1$
	MessageResult mr = MessageResultManager.analizeResponse(op, result,entity);
	
	if ("baja".equals(op) && "0".equals(result)) { //$NON-NLS-1$ //$NON-NLS-2$
		errorText = "No se ha podido borrar el usuario, tiene aplicaciones asociadas.";
	
	}else if  ("baja".equals(op) && "1".equals(result)) { //$NON-NLS-1$ //$NON-NLS-2$
		valueText = "El usuario ha sido borrado correctamente"; //$NON-NLS-1$
		
	}else if ("alta".equals(op) && "1".equals(result)) { //$NON-NLS-1$ //$NON-NLS-2${
		valueText = "El usuario ha sido creado correctamente"; //$NON-NLS-1$
	}
	
	if ("edicion".equals(op) && "0".equals(result)) { //$NON-NLS-1$ //$NON-NLS-2$
		errorText = "Los usuarios no pueden tener el mismo correo.";
	
	}else if  ("edicion".equals(op) && "1".equals(result)) { //$NON-NLS-1$ //$NON-NLS-2$
		valueText = "El usuario ha sido modificado correctamente";
	}
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Gesti&oacute;n de usuarios FIRe</title>
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
		<input class="menu-btn" name="add-usr-btn" type="button" value="Alta de usuario" title="Crear un nuevo usuario" onclick="location.href='NewUser.jsp?op=1'"/>
	</div>
	
	<% if (errorText != null) { %>
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
			  Usuarios dados de alta en el sistema.
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
<!-- 		<br> -->
<!-- 		<div id="nav_page" style="display: block-inline; text-align:right;"> -->
<!-- 			<button id="back">Anterior</button> -->
<!-- 	        <button id="next">Siguiente</button> -->
<!-- 	        <p id="page"></p> -->
<!-- 		</div> -->
   	</div>

</body>
<script type="text/javascript">
	/*Importante estas variables (requestTypeCount y requestType) deben estar declaradas antes que la llamada a ../resources/js/user.js*/
	       // requestTypeCount="countRecordsUsers";
			requestType="All";
			idUser = null;
//          
	</script>

<script src="../resources/js/user.js" type="text/javascript"></script>
</html>