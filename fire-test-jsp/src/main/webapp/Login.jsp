
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Prueba FIRe</title>
	<link rel="shortcut icon" href="img/cert.png">
	<link rel="stylesheet" href="styles/styles.css"/>
</head>
	<body>
		<% 	
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
		<div id="menubar">
			<div id="bar-txt"><b>Prueba FIRe</b></div>
		</div>
		<div id="container" style="height: 450px !important;">
			<div style="display: block-inline;text-align:center;">
				<img src="img/dni_icon_login.png" alt="Imagen esquem&aacute;tica de un usuario" height="80" width="80" style="margin-top:-40px;">
				<p id="descrp" style="margin-top:20px">
				 Introduzca el usuario con el que desee firmar.
				</p>
			</div>
			<%if(loginFail) {%>
				<p id="error-txt">El usuario introducido es incorrecto.</p> 
			<%}%>
			<form method="POST" action="LoginService">
				<div style="margin-top: 20px; margin-left: 120px;">
					<div>
						<label for="usuario" style="color: #404040">USUARIO</label>
					</div>
					<div>
						<input id="usuario" class="edit-txt" type="text" tabindex="1" name="user" style="width: 400px; ;margin-top:10px;" value="00001" spellcheck="false" autocomplete="off">
					</div>
				</div>
				
				<div style="margin-top: 20px; text-align: center;">
					<div>
						<label for="submit-btn" style=" color: #808080">Pulse el bot&oacute;n para acceder</label>
					</div>
					<div>
						<input id="submit-btn" type="submit" value="ACCEDER" style="margin-top: 10px;">
					</div>
				</div>
			</form>
		</div>
	</body>
</html>