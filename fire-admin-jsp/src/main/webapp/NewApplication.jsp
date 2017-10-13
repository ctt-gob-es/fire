

<%@page import="java.util.List" %>

<%@page import="es.gob.fire.server.admin.ConfigurationDAO" %>
<%@page import="es.gob.fire.server.admin.AplicationsDAO" %>
<%@page import="es.gob.fire.server.admin.Application" %>


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	final Object state = request.getSession().getAttribute(
			"initializedSession"); //$NON-NLS-1$
	if (state == null || !Boolean.parseBoolean((String) state)) {
		response.sendRedirect("Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	String id = request.getParameter("id-app");//$NON-NLS-1$
	final int op = Integer.parseInt(request.getParameter("op"));//$NON-NLS-1$

	// op = 0 -> Solo lectura, no se puede modificar nada
	// op = 1 -> nueva aplicacion
	// op = 2 -> editar aplicacion
	String title = ""; //$NON-NLS-1$
	String subTitle = ""; //$NON-NLS-1$
	Application app;
	switch (op) {
		case 0:
			title = "Ver la aplicaci&oacute;n " + id;//$NON-NLS-1$
			subTitle = ""; //$NON-NLS-1$
			break;
		case 1:
			title = "Alta de nueva aplicaci&oacute;n"; //$NON-NLS-1$
			subTitle = "Inserte los datos de la nueva aplicaci&oacute;n."; //$NON-NLS-1$
			break;
		case 2:
			title = "Editar la aplicaci&oacute;n " + id; //$NON-NLS-1$
			subTitle = "Modifique los datos que desee editar"; //$NON-NLS-1$
			break;
		default:
			response.sendRedirect("Login.jsp?login=fail"); //$NON-NLS-1$
			return;
	}
	app = id != null ? AplicationsDAO.selectApplication(id)
			: new Application();
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>FIRe</title>
	<link rel="shortcut icon" href="img/cert.png">
	<link rel="stylesheet" href="styles.css">
</head>
<script>
	function isCert(){
		if (document.getElementById("nombre-app").value == "" ){
			alert('El nombre de la aplicación no puede estar vacío');
			document.getElementById("nombre-app").focus();
			return false;
		}
		
		if (document.getElementById("nombre-resp").value == "" ){
			alert('El nombre del responsable no puede estar vacío');
			document.getElementById("nombre-resp").focus();
			return false;
		}
		
				
		var data = document.getElementById("cert-resp").value
		if (data == ""){
			alert("El certificado no puede estar vacio, introduzca un certificado en base 64");
			document.getElementById("cert-resp").focus();
			return false;
		}
		var begin = data.split("-----BEGIN CERTIFICATE-----");
		if(begin.length >1){
			data = begin[1].trim();
		}
		var end = data.split("-----END CERTIFICATE-----")
		if(end.length > 1){
			data = end[0].trim();
		}
		// nos quitamos los espacios
		document.getElementById("cert-resp").value = data.replace(" ","");
	}
</script>
<body>
	<!-- Barra de navegacion -->
	<ul id="menubar">
		<li id="bar-txt"><b><%= title %></b></li>
	</ul>
	<!-- contenido -->
	<div id="container">
	
		<div id="menu-bar">
			<a class="menu-btn" href="AdminMainPage.jsp" >Volver a la p&aacute;gina de administraci&oacute;n</a>
		</div>
	
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  <%= subTitle %>
			</p>
		</div>
	
		<div style="display: block-inline; text-align:center;">

		</div>
		
		<p>Los campos con * son obligatorios</p>
			<form method="POST" action="newApp?iddApp=<%= id %>&op=<%= op %>">
				<ul style="margin-top: 20px;">
					<li class="field-text">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-app" style="color: #404040">* Nombre de aplicaci&oacute;n</label>
					</li>
					<li>
						<input id="nombre-app" class="edit-txt" type="text" name="nombre-app" style="width: 400px;margin-top:10px;" 
						value="<%= request.getParameter("name")!= null ? request.getParameter("name") : app.getNombre() %>"> 
					</li>
				</ul>

				<ul style="margin-top: 20px;">
					<li class="field-text">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-resp" style="color: #404040" >* Responsable</label>
					</li>
					<li>
						<input id="nombre-resp" class="edit-txt" type="text" name="nombre-resp" style="width: 400px;margin-top:10px;"
						value="<%= request.getParameter("res")!= null ? request.getParameter("res") : app.getResponsable() %>">
					</li>
				</ul>
				
				<ul style="margin-top: 20px;">
					<li class="field-text">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="email-resp" style="color: #404040">Correo de contacto</label>
					</li>
					<li>
						<input id="email-resp" class="edit-txt" type="text" name="email-resp" style="width: 400px;margin-top:10px;"
						value="<%= request.getParameter("email")!= null ? request.getParameter("email") : app.getCorreo() %>"
						>
					</li>
				</ul>
				
				<ul style="margin-top: 20px;">
					<li class="field-text">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="telf-resp" style="color: #404040">Tel&eacute;fono de contacto</label>
					</li>
					<li>
						<input id="telf-resp" class="edit-txt" type="text" name="telf-resp" style="width: 400px;margin-top:10px;"
						value="<%= request.getParameter("tel")!= null ? request.getParameter("tel") : app.getTelefono()%>">
					</li>
				</ul>
				
				<ul style="margin-top: 20px;">
					<li class="field-text">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="cert-resp" style="color: #404040;">* Certificado en Base64</label>
					</li>
					<li>
						<textArea rows="10" cols="62" id="cert-resp" name="cert-resp" class="edit-txt" style="width: 400px;margin-top:10px;resize:none">
						<%= request.getParameter("cer")!= null ? request.getParameter("cer").trim() : app.getCer()%></textArea>
					</li>
				</ul>
	
			<fieldset class="fieldset-clavefirma" >
		   		
		   		<% 
		   		if (op > 0) {
		   			final String msg = (op == 1 ) ? "Crear aplicaci&oacute;n" : "Guardar cambios";   //$NON-NLS-1$ //$NON-NLS-2$

		   		%>
			   		
			   		<div  style="text-align: center; margin-top: 1%;">
			   			<input class="form-btn" name="add-app-btn" type="submit" value="<%= msg %>" onclick="return isCert()">
			   		</div>
		   		<% } %>
		   		
			</fieldset>
		</form>
		<script>
			//bloqueamos los campos en caso de que sea una operacion de solo lectura
			document.getElementById("nombre-app").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("email-resp").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("nombre-resp").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("telf-resp").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("cert-resp").disabled = <%= op == 0 ? "true" : "false" %>
		
			// quitamos los espacios en blanco que se han agregado en el certificado
			document.getElementById("cert-resp").value = document.getElementById("cert-resp").value.trim();
			if (<%= Boolean.parseBoolean(request.getParameter("error")) %>){ 
				alert('El certificado introducido no es correcto, por favor introduzca un certificado valido');
				document.getElementById("cert-resp").focus();
			}
			
		</script>
   	</div>
</body>
</html>
