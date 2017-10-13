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
  	String message = request.getParameter("msg"); //$NON-NLS-1$
  	if (message != null) {
  		message = message.toUpperCase();
  	}
  %>
 	<div id="menubar">
		<div id="bar-txt"><b>Prueba FIRe</b></div>
	</div>
	<div id="container">
		<div id="error-txt">	
			<p style="text-align:center">
   				<%= message %> 
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