<%@page import="es.gob.fire.server.admin.dao.UsersDAO"%>
<%@page import="es.gob.fire.server.admin.conf.DbManager"%>
<%@page import="es.gob.fire.server.admin.dao.ConfigurationDAO" %>
<%@page import="es.gob.fire.server.admin.message.MessageResult" %>
<%@page import="es.gob.fire.server.admin.message.MessageResultManager" %>
<%@page import="es.gob.fire.server.admin.entity.User" %>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	final Object state = request.getSession().getAttribute("initializedSession"); //$NON-NLS-1$
	final String loggedUsr=(String)request.getSession().getAttribute("user"); //$NON-NLS-1$

		
	if (state == null || !Boolean.parseBoolean((String) state) || loggedUsr==null ) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}
	User usr;	
	usr =  UsersDAO.getUserByName(loggedUsr);
	
	
	final String title = "Modificar contraseña"; //$NON-NLS-1$
	final String subTitle = "Modificar contraseña del usuario ".concat(usr.getNombre()).concat(" ").concat(usr.getApellidos());//$NON-NLS-1$
	
	
	
	final String msg = "Guardar cambios";   //$NON-NLS-1$ 
	final String tit= "Guarda las modificaciones realizadas";//$NON-NLS-2$
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
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
			<form id="frmChangePass" method="POST" action="../modifyPasswdUser?id-usr=<%=usr.getId_usuario() %>&nombre-usr=<%=usr.getNombre_usuario() %>&op=3">
			
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 30%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-usr" style="color: #404040">* Nombre de usuario</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
						<input id="nombre-usr" class="edit-txt" type="text" name="nombre-usr" style="width: 80%;margin-top:3px;" 
						value="<%=usr.getNombre_usuario() %>"> 
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