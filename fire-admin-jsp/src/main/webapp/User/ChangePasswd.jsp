<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO"%>
<%@page import="es.gob.fire.server.admin.entity.User" %>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	if (session == null) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	final Object state = session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED);
	final String loggedUsr=(String)session.getAttribute(ServiceParams.SESSION_ATTR_USER);
	final String pass=(String)session.getAttribute(ServiceParams.SESSION_ATTR_USER);

		
	if (state == null || !Boolean.parseBoolean((String) state) || loggedUsr==null ) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}
	User usr = null;
	try {
		usr = UsersDAO.getUserByName(loggedUsr);
	}
	catch (Exception e) {
		usr = null;
	}
	String nombre = ""; //$NON-NLS-1$
	String apellidos = ""; //$NON-NLS-1$
	if (usr != null) {
		nombre = usr.getName();
		apellidos = usr.getSurname();
	}
	final String title = "Modificar contrase&ntilde;a"; //$NON-NLS-1$
	final String subTitle = "Modificar contrase&ntilde;a del usuario " + nombre + " " + apellidos; //$NON-NLS-1$ //$NON-NLS-2$
	
	final String msg = "Guardar cambios";   //$NON-NLS-1$ 
	final String tit = "Guarda las modificaciones realizadas"; //$NON-NLS-1$
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Gesti&oacute;n de usuarios FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/validateChangePasswd.js" type="text/javascript"></script>
</head>
<body>
	
	
	<!-- Barra de navegacion -->		
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	<!-- contenido -->
	<div id="container">
		
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  <%= subTitle %>
			</p>
		</div>
			
		<p>Los campos con * son obligatorios</p>
			<form id="frmChangePass" method="POST" action="../modifyPasswdUser?id-usr=<%=usr.getId() %>&nombre-usr=<%= nombre %>&op=3">
			
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 30%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-usr" style="color: #404040">* Nombre de usuario</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
						<input id="nombre-usr" class="edit-txt" type="text" name="nombre-usr" style="width: 80%;margin-top:3px;" 
						value="<%= nombre %>"> 
					</div>	
				</div>
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 30%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="old_passwd-usr" style="color: #404040" >* Contraseña antigua</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
							<input id="old_passwd-usr" type="password" class="edit-txt"  name="old_passwd-usr" style="width: 80%;margin-top:3px;"
						value="">
					</div>	
				</div>
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 30%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwd-usr_1" style="color: #404040" >* Contraseña nueva</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
							<input id="passwd-usr_1" type="password" class="edit-txt"  name="passwd-usr_1" style="width: 80%;margin-top:3px;"
						value="">
					</div>	
				</div>
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 30%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwd-usr_2" style="color: #404040" >* Repetir contraseña nueva</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
							<input id="passwd-usr_2" type="password" class="edit-txt"  name="passwd-usr_2" style="width: 80%;margin-top:3px;"
						value="">
					</div>	
				</div>
																	
			<fieldset class="fieldset-clavefirma" >
		   		
			<div  style="text-align: center; width: 100%;margin-top: 3px;">	
				<div style="display: inline-block; width: 30%;margin: 5px;">
					<input class="menu-btn" name="add-usr-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de Usuarios" onclick="location.href='UserPage.jsp'"/>
<!-- 					<a class="menu-btn" href="UserPage.jsp" >Volver a la p&aacute;gina de Usuarios</a> -->
				</div>
				<div  style="display: inline-block; width: 60%;margin: 5px;">
					<input class="menu-btn" name="add-usr-btn" type="submit" value="<%= msg %>" title="<%=tit %>" >
				</div> 
			 </div>		 
		   	
			</fieldset>
		</form>
		<script>
			document.getElementById("nombre-usr").disabled = true ;														
		</script>
   	</div>
	
</body>
</html>