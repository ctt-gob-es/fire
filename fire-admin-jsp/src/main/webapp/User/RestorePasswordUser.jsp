<%@page import="es.gob.fire.server.admin.service.UserRestorationInfo"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO"%>
<%@page import="es.gob.fire.server.admin.entity.User" %>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	if (session == null) {
		response.sendRedirect("Login.jsp"); //$NON-NLS-1$
		return;
	}

	String user = request.getParameter(ServiceParams.PARAM_USERID); //$NON-NLS-1$
	String nombre = request.getParameter(ServiceParams.PARAM_USERNAME); //$NON-NLS-1$
	String code = request.getParameter(ServiceParams.PARAM_CODE);

	final UserRestorationInfo restorationInfo = (UserRestorationInfo) session.getAttribute(ServiceParams.SESSION_ATTR_RESTORATION);
	
	if (!user.equals(restorationInfo.getId())) {
		response.sendRedirect("Login.jsp"); //$NON-NLS-1$
		return;
	}
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Restaurar contrase&ntilde;a FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/validateChangePasswd.js" type="text/javascript"></script>
</head>
<body>
	
	
	<!-- Barra de navegacion -->		
		<!-- Barra de navegacion -->
		<ul id="menubar">
			<li id="bar-txt"><b>FIRe</b></li>
		</ul>
	<!-- contenido -->
	<div id="container">
		
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">Restaurar contrase&ntilde;a</p>
		</div>
			
		<p>Introduzca su nueva contarse&ntilde;a</p>
			<form id="frmMailChangePass" method="POST" action="../mailPasswordAuthoritation">
			
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 30%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-usr" style="color: #404040">Nombre de usuario</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
						<input id="nombre-usr" class="edit-txt" type="text" name="nombre-usr" style="width: 80%;margin-top:3px;" 
						value="<%= nombre %>"> 
					</div>
				</div>
				
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 30%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="password" style="color: #404040" >Contraseña nueva</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
							<input id="password" type="password" class="edit-txt"  name="<%= ServiceParams.PARAM_PASSWORD %>" style="width: 80%;margin-top:3px;"
						value="">
					</div>	
				</div>
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 30%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwordcopy" style="color: #404040" >Repetir contraseña nueva</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
							<input id="passwordcopy" type="password" class="edit-txt"  name="<%= ServiceParams.PARAM_PASSWORD_COPY %>" style="width: 80%;margin-top:3px;"
						value="">
					</div>	
				</div>
										
				<input id="userid" type="hidden" name="<%= ServiceParams.PARAM_USERID %>" value="<%= user %>" />
				<input id="cod" type="hidden" name="<%= ServiceParams.PARAM_CODE %>" value="<%= code %>" />
																		
				<fieldset class="fieldset-clavefirma" >
			   		
				<div  style="text-align: center; width: 100%;margin-top: 3px;">	
					<div  style="display: inline-block; width: 60%;margin: 5px;">
						<input class="menu-btn" name="chg-pss-btn" type="submit" value="Cambiar contrase&ntilde;a" title="Cambiar contrase&ntilde;a" >
					</div> 
				 </div>
		   	
			</fieldset>
		</form>
		<script>
			document.getElementById("nombre-usr").disabled = true ;	
			document.getElementById("nombre-usr").style.background = '#F5F5F5';
		</script>
   	</div>
	
</body>
</html>