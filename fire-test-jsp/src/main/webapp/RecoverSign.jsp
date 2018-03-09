<%@page import="java.net.URLEncoder"%>
<%@page import="es.gob.fire.test.webapp.Base64"%>
<%@page import="es.gob.fire.test.webapp.SignHelper"%>
<%@page import="java.util.logging.Logger"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Prueba FIRe</title>
		<link rel="shortcut icon" href="img/cert.png">
		<link rel="stylesheet" href="styles/styles.css"/>
	</head>
	<body style=" font-weight: 300;">

		<%
			if (session.getAttribute("user") == null) { //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

		    byte[] signature = null;
		    try {
		    	signature = SignHelper.recoverSignResult(request);
		    }
		    catch (Exception e) {
				Logger.getLogger("es.gob.fire.test.webapp").severe( //$NON-NLS-1$
						"Error durante la operacion de recuperacion de firma: " + e); //$NON-NLS-1$
		    	response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode(e.getMessage(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
		    }
		%>

		<!-- Barra de navegacion -->
		<div id="menubar">
			<div id="bar-txt"><b>Prueba FIRe</b></div>
		</div>

		<!-- contenido -->
		<div id="sign-container">
			<h1 style="color:#303030;">OBTENCI&Oacute;N DE LA FIRMA</h1>

			<div style="display:inline-block;"></div>

			<div style="margin-top: 10px; text-align: left; ">
				<label for="datos-firma">Firma generada: </label><br><br>
				<textarea id="datos-firma" rows="10" cols="150" name="sign-data"><%= 
						signature != null ? Base64.encode(signature) : "" //$NON-NLS-1$
				%></textarea>
			</div>


			<form method="POST" action="Login.jsp">
				<div style="margin-top:30px;text-align: center; ">
					<label for="submit-btn">Pulse el bot&oacute;n para realizar una nueva firma</label><br><br>
					<input  id="submit-btn"  type="submit" value="NUEVA FIRMA">
				</div>
			</form>
		</div>
	
	</body>
</html>