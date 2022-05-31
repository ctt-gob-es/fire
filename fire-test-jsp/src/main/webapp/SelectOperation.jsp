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
		%>
		<div id="menubar">
			<div id="bar-txt"><b>Prueba FIRe</b></div>
		</div>

		<div id="sign-container">
			<h1 style="color:#303030;">SELECCIONE LA OPERACI&Oacute;N QUE DESEE REALIZAR</h1>
			<div style="margin: 60px auto; text-align: center; ">
				<a class="submit-btn" href="Sign.jsp" style="text-decoration: none; font-size: 10pt;">REALIZAR OPERACION DE FIRMA</a>
			</div>
			<div style="margin: 60px auto; text-align: center;">
				<a class="submit-btn" href="CreateBatch.jsp" style="text-decoration: none; font-size: 10pt;">REALIZAR FIRMA DE LOTE</a>
			</div>
		</div>
	</body>
</html>