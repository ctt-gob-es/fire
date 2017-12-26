
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
</head>
	<body>
		
		<!-- Barra de navegacion -->
		<jsp:include page="../resources/jsp/NavigationBar.jsp" />
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