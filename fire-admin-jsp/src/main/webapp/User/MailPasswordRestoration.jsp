<%@page import="es.gob.fire.server.admin.message.UserMessages"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO"%>
<%@page import="es.gob.fire.server.admin.entity.User" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Gesti&oacute;n de contrase&ntilde;as FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css"/>
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/validateChangePasswd.js" type="text/javascript"></script>
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
			String login = request.getParameter("mailChangePassword"); //$NON-NLS-1$
			
			// Este parametro comprueba si el usuario ha introducido su nombre de usuario y contrasena
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
		%>
			<!-- Barra de navegacion -->
		<ul id="menubar">
			<li id="bar-txt"><b>FIRe</b></li>
		</ul>
		<!-- Contenido -->
		<div id="container" style="height: 450px !important;">
			<div style="display: block-inline;text-align:center;">
				<img src="../resources/img/dni_icon_login.png" alt="Imagen esquem&aacute;tica de un usuario" height="80" width="80" style="margin-top:-40px;">
				<p id="descrp" style="margin-top:20px">
				 Introduzca el nombre de usuario o correo electr&oacute;nico para restaurar la contrase&ntilde;a.
				</p>
			</div>
			
			
           <form id="frmMailChangePass" method="POST" action="../mailPasswordRestoration">
			
				<div style="margin: auto;width: 60%;padding: 5px;">
					<div style="display: inline-block; width: 20%;margin: 5px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="mail" style="color: #404040">CORREO ELECTR&Oacute;NICO</label>
					</div>
					<div  style="display: inline-block; width: 60%;margin: 5px;">
						<input id="mail" class="edit-txt" type="text" name="<%= ServiceParams.PARAM_MAIL %>" autocomplete="off" style="width: 100%;margin-top:10px;" />
					</div>	
				</div>
				
				
				<div id="loginButton">
				<p style="color: #808080;right:inherit;">Pulse el bot&oacute;n (Restaurar) para restaurar la contrase&ntilde;a o el bot&oacute;n (Volver) para ir a la p&aacute;gina principal</p>
				<input class="menu-btn" type="submit" value="Restaurar" style="margin-top: 10px;">
				<input class="menu-btn" name="add-usr-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de administraci&oacute;n" onclick="location.href='../Login.jsp'" style="margin-top: 10px;"/>
				</div>
				
				</form>
				
		</div>
	</body>
</html>