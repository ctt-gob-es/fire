
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
			String login = request.getParameter("login"); //$NON-NLS-1$
			boolean loginFail = false;
			if (login != null) {
				if (login.equals("fail")) { //$NON-NLS-1$
					loginFail = true;
				}
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
			<%if(loginFail) {%>
				<p id="error-txt">La contrase&ntilde;a introducida es incorrecta.</p> 
			<%}%>
			<form method="POST" action="./Application/AdminMainPage.jsp">
			
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 20%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="usuario" style="color: #404040">USUARIO</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
						<input id="usuario" class="edit-txt" type="text" name="user" autocomplete="off" style="width: 100%;margin-top:10px;" />
					</div>	
				</div>
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 20%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="contrasenia" style="color: #404040">CONTRASE&Ntilde;A</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
							<input id="contrasenia" class="edit-txt" type="password" name="password" autocomplete="off" style="width: 100%;margin-top:10px;"/>
					</div>	
				</div>
			
				<div id="loginButton">
				<p style="color: #808080;">Pulse el bot&oacute;n para acceder</p>
				<input id="submit-btn" type="submit" value="ACCEDER" style="margin-top: 10px;">
				</div>
			</form>
		</div>
	</body>
</html>