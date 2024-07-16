<%@page import="org.slf4j.Logger"%>
<%@page import="es.gob.fire.client.GracePeriodInfo"%>
<%@page import="java.security.cert.CertificateEncodingException"%>
<%@page import="es.gob.fire.client.TransactionResult"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="es.gob.fire.test.webapp.Base64"%>
<%@page import="es.gob.fire.test.webapp.SignHelper"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	String user = (String) session.getAttribute("user"); //$NON-NLS-1$
	if (user == null) {
		Logger LOGGER = LoggerFactory.getLogger("es.gob.fire.test.webapp.recoversign"); //$NON-NLS-1$
		LOGGER.warn("No se encontro sesion de usuario"); //$NON-NLS-1$
		response.sendRedirect("Login.jsp"); //$NON-NLS-1$
		return;
	}
	
	Boolean resultState = (Boolean) session.getAttribute("resultstate"); //$NON-NLS-1$
	String message = (String) session.getAttribute("message"); //$NON-NLS-1$
%>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Prueba FIRe</title>
		<link rel="shortcut icon" href="img/cert.png">
		<link rel="stylesheet" href="styles/styles.css"/>
	</head>
	<body style=" font-weight: 300;">

		<div id="menubar">
			<div id="bar-txt"><b>Prueba FIRe</b></div>
		</div>

		<div id="sign-container">
			<h1 style="color:#303030;">OBTENCI&Oacute;N DE LA FIRMA</h1>

			<div style="display:inline-block;"></div>

			<div style="margin-top: 10px; text-align: left; ">
				<% if (resultState != null && resultState.booleanValue()) { %>
					<span>La firma se ha generado correctamente.</span><br>
					<a id="download_link" href="DownloadSignatureService" onclick="this.style='display:none;'">Descargar firma</a>
				<% } else { %>
					<span id="message-txt"><%= message %></span>
				<% } %>
			</div>

			<br><br>
			<form method="POST" action="Login.jsp">
				<div style="margin-top:30px;text-align: center; ">
					<label for="submit-btn">Pulse el bot&oacute;n para realizar una nueva firma</label><br><br>
					<input  id="submit-btn"  type="submit" value="NUEVA FIRMA">
				</div>
			</form>
		</div>
	</body>
</html>

<%
	if (resultState == null || !resultState.booleanValue()) {
		session.invalidate();
	}
%>