
<%@page import="es.gob.fire.server.admin.message.UserMessages"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Administraci&oacute;n FIRe</title>
	<link rel="shortcut icon" href="./resources/img/cert.png">
	<link rel="stylesheet" href="./resources/css/styles.css"/>
</head>
	<body>
		<%
			response.setHeader("Cache-control", "no-cache");//$NON-NLS-1$//$NON-NLS-2$
			response.setHeader("Cache-control", "no-store");//$NON-NLS-1$//$NON-NLS-2$
			response.setDateHeader("Expires", 0);//$NON-NLS-1$
			response.setHeader("Pragma", "no-cache");//$NON-NLS-1$//$NON-NLS-2$
			
			if (session != null) {
				session.removeAttribute(ServiceParams.SESSION_ATTR_INITIALIZED);
				session.removeAttribute(ServiceParams.SESSION_ATTR_USER);
				session.invalidate();
			}
			// Este parametro comprueba si el usuario ha introducido su nombre de usuario y contrasena
			// En caso de que haya introducido datos erroneos, se recibe un "fail"
			String err = request.getParameter(ServiceParams.PARAM_ERR);
			String errMsg = null;
			try {
			if (err != null) 
				errMsg = UserMessages.parse(err).getText();
			}
			catch (Exception e) {
				err = null;
			}
			
			
			String succ = request.getParameter(ServiceParams.PARAM_SUCCESS); //$NON-NLS-1$
			String succMsg = null;
			try {
			if (succ != null) 
				succMsg = UserMessages.parse(succ).getText();
			}
			catch (Exception e) {
				succ = null;
			}
			
		%>
		<!-- Barra de navegacion -->
		<ul id="menubar">
			<li id="bar-txt"><b>FIRe</b></li>
		</ul>
		<!-- Contenido -->
		<div id="container" style="height: 450px !important;">
			<div style="display: block-inline;text-align:center;">
				<img src="./resources/img/dni_icon_login.png" alt="Imagen esquem&aacute;tica de un usuario" height="80" width="80" style="margin-top:-40px;">
				<p id="descrp" style="margin-top:20px">
				 Introduzca la contrase&ntilde;a para acceder a la interfaz de administraci&oacute;n.
				</p>
			</div>
			<% if (errMsg != null) { %>
				<p id="error-txt"><%= errMsg %></p> 
			<% } else if (succMsg != null) { %>
				<p id="success-txt"><%= succMsg %></p>
			<% } %>
			<form method="POST" action="./login">
			
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 20%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="usuario" style="color: #404040">USUARIO</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
						<input id="usuario" class="edit-txt" type="text" name="<%= ServiceParams.PARAM_USERNAME %>" autocomplete="off" style="width: 100%;margin-top:10px;" />
					</div>	
				</div>
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 20%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="contrasenia" style="color: #404040">CONTRASE&Ntilde;A</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
							<input id="contrasenia" class="edit-txt" type="<%= ServiceParams.PARAM_PASSWORD %>" name="password" autocomplete="off" style="width: 100%;margin-top:10px;"/>
					</div>	
				</div>
			
				<div id="loginButton">
				<p style="color: #808080;right:inherit;">Pulse el bot&oacute;n para acceder</p>
				<input id="submit-btn" type="submit" value="ACCEDER" style="margin-top: 10px;">
				</div>
				
				
				
				
			<!-- enlace para acceder a la pagina MailChangePassword -->
			</form>
			<a href="User/MailPasswordRestoration.jsp"  class="enlace">&iquest;Ha olvidado su contrase&ntilde;a?</a>
		</div>
	</body>
</html>