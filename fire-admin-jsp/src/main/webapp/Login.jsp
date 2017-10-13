
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Administraci&oacute;n FIRe</title>
	<link rel="shortcut icon" href="img/cert.png">
	<link rel="stylesheet" href="styles.css"/>
</head>
	<body>
		<% 	
		
			request.getSession().removeAttribute("initializedSession"); //$NON-NLS-1$
		
			// Este parametro comprueba si el usuario ha introducido su nombre de usuario y contrasena
			// En caso de que haya introducido datos erroneos, se recibe un "fail"
			String login = request.getParameter("login"); //$NON-NLS-1$
			boolean loginFail = false;
			if(login != null) {
				if(login.equals("fail")) { //$NON-NLS-1$
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
				<img src="img/dni_icon_login.png" alt="Imagen esquem&aacute;tica de un usuario" height="80" width="80" style="margin-top:-40px;">
				<p id="descrp" style="margin-top:20px">
				 Introduzca la contrase&ntilde;a para acceder a la interfaz de administraci&oacute;n.
				</p>
			</div>
			<%if(loginFail) {%>
				<p id="error-txt">La contrase&ntilde;a introducida es incorrecta.</p> 
			<%}%>
			<form method="POST" action="AdminMainPage.jsp">
				<ul style="margin-top: 20px;">
					<li>
						<!-- Label para la accesibilidad de la pagina -->
						<label for="contrasenia" style="color: #404040">CONTRASE&Ntilde;A</label>
					</li>
					<li>
						<input id="contrasenia" class="edit-txt" type="password" name="password" style="width: 400px;margin-top:10px;">
					</li>
				</ul>

				<div id="loginButton">
				<p style="color: #808080;">Pulse el bot&oacute;n para acceder</p>
				<input id="submit-btn" type="submit" value="ACCEDER" style="margin-top: 10px;">
				</div>
			</form>
		</div>
	</body>
</html>