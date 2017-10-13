
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>FIRe</title>
	<link rel="shortcut icon" href="img/cert.png">
	<link rel="stylesheet" href="styles.css"/>
</head>
	<body>
		
		<!-- Barra de navegacion -->
		<ul id="menubar">
			<li id="bar-txt"><b>FIRe</b></li>
		</ul>
		<!-- Contenido -->
		<div id="container" style="height: 450px !important;">
			<% 	

		if (request.getParameter("file") != null){  //$NON-NLS-1$
			%>
			<h2>No se ha encontrado el fichero <%= request.getParameter("file") //$NON-NLS-1$ 
			%></h2>
			<p> Consulte con el administrador del sistema.</p>
			<%
		}
		else{
			response.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}
		%>
		</div>
	</body>
</html>