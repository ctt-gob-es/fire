<!DOCTYPE html>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.Collection"%>
<%@page import="es.gob.fire.test.webapp.ErrorHelper"%>
<%@page import="es.gob.fire.client.TransactionResult"%>
<html>
 <head>
 	<meta charset="UTF-8">
	<title>Prueba FIRe</title>
	<link rel="shortcut icon" href="img/cert.png">
	<link rel="stylesheet" href="styles/styles.css"/>
 </head>
 <body>
  <%
	TransactionResult error;
	try {
		error = ErrorHelper.recoverErrorResult(request);
	}
	catch (Exception e) {
		error = new TransactionResult(0, 0, "No se pudo obtener el error de la operaci\u00F3n"); //$NON-NLS-1$
	}
  %>
 	<div id="menubar">
		<div id="bar-txt"><b>Prueba FIRe</b></div>
	</div>
	<div id="container">
		<div id="error-txt">	
			<p style="text-align:center">
   				Error <%= error.getErrorCode() %>: <%= error.getErrorMessage() %> 
  			</p>
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