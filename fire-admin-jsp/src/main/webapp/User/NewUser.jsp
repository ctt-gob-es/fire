

<%@page import="es.gob.fire.server.admin.dao.UsersDAO" %>
<%@page import="es.gob.fire.server.admin.entity.User" %>


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	final Object state = request.getSession().getAttribute("initializedSession"); //$NON-NLS-1$
	final String usrLogged= (String) request.getSession().getAttribute("user");//$NON-NLS-1$
	if (state == null || !Boolean.parseBoolean((String) state)) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}
	final String empty="";
	String idUsr = request.getParameter("id-usr");//$NON-NLS-1$
	final int op = Integer.parseInt(request.getParameter("op"));//$NON-NLS-1$

	// op = 0 -> Solo lectura, no se puede modificar nada
	// op = 1 -> nuev0 usuario
	// op = 2 -> editar usuario
	String title = ""; //$NON-NLS-1$
	String subTitle = ""; //$NON-NLS-1$
	
	User usr;
	usr = idUsr != null ? UsersDAO.getUser(idUsr) : new User();
	
	
	switch (op) {
		case 0:
			title = "Ver usuario " + idUsr;//$NON-NLS-1$
			subTitle = ""; //$NON-NLS-1$
			break;
		case 1:
			title = "Alta de nuevo usuario"; //$NON-NLS-1$
			subTitle = "Inserte los datos del nuevo usuario."; //$NON-NLS-1$
			break;
		case 2:		
			title = "Editar usuario ".concat(usr.getNombre()).concat(" ").concat(usr.getApellidos()); //$NON-NLS-1$
			subTitle = "Modifique los datos que desee editar."; //$NON-NLS-1$
			break;
		default:
			response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
			return;
	}
	
		
		
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script type="text/javascript">var op=<%=op%>;	</script>
	<script src="../resources/js/validateUsers.js" type="text/javascript"></script>
<!-- <title>Insert title here</title> -->
</head>
<body>
<!-- Barra de navegacion -->		
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	<!-- contenido -->
	<div id="container">
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  <%= subTitle %>
			</p>
		</div>
		
		<p>Los campos con * son obligatorios</p>
			<form id="formUser" method="post" action="../newUser?op=<%=op%>&idUser=<%=usr.getId_usuario()!=null?usr.getId_usuario():empty%>"> 
			
<!-- 			<div style="margin: auto;width: 100%;padding: 3px;"> -->
<!-- 					<div style="display: inline-block; width: 20%;margin: 3px;"> -->
<!-- 						Label para la accesibilidad de la pagina -->
<!-- 						<label for="role-usr" style="color: #404040">* Tipo de usuario</label> -->
<!-- 					</div> -->
<!-- 					<div  style="display: inline-block; width: 70%;margin: 3px;"> -->
<!-- 						<input id="role-usr" class="edit-txt" type="text" name="role-usr" style="width: 34%;margin-top:3px;"  -->
<%-- 						value="<%= usr.getRol()!= null ? usr.getRol(): empty %>"> --%>
<!-- 					</div>	 -->
<!-- 			</div> -->
			
			<%if(op!=2){ %>
			
			<%if(op==1 || op==0 ){ %>	
				<div style="margin: auto;width: 100%;padding: 3px;">		
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="login-usr" style="color: #404040">* Nombre de usuario (Login)</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="login-usr" class="edit-txt" type="text" name="login-usr" style="width: 80%;margin-top:3px;" 
						value="<%= usr.getNombre_usuario()!= null ? usr.getNombre_usuario(): empty %>"> 
					</div>
				</div>									
				<%} %>
			<%if(op==1){ %>
			<div style="margin: auto;width: 100%;padding: 3px;">	
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwd-usr" style="color: #404040" >* Clave</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="passwd-usr" type="password" class="edit-txt"  name="passwd-usr" style="width: 80%;margin-top:3px;"
						value="">
					</div>
					<div style="display: inline-block; width: 10%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwd-usr-copy" style="color: #404040" >* Repetir clave</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="passwd-usr-copy" type="password" class="edit-txt"  name="passwd-usr-copy" style="width: 80%;margin-top:3px;"
						value="">
					</div>		
				</div>							
				<%} %>
			
			<%} %>
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="usr-name" style="color: #404040">* Nombre </label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="usr-name" class="edit-txt" type="text" name="usr-name" style="width: 80%;margin-top:3px;" 
						value="<%= usr.getNombre()!= null ? usr.getNombre(): empty %>"> 
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="usr-surname" style="color: #404040">* Apellidos</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					<input id="usr-surname" class="edit-txt" type="text" name="usr-surname" style="width: 80%;margin-top:3px;" 
						value="<%= usr.getApellidos()!= null ? usr.getApellidos(): empty %>"> 
				</div>						
			</div>	
			
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="email" style="color: #404040">Correo electrónico</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="email" class="edit-txt" type="text" name="email" style="width: 80%;margin-top:3px;" 
						value="<%= usr.getCorreo_elec()!= null ? usr.getCorreo_elec(): empty %>"> 
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="telf-contact" style="color: #404040">Telf. Contacto</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					<input id="telf-contact" class="edit-txt" type="text" name="telf-contact" style="width: 80%;margin-top:10px;" 
						value="<%= usr.getTelf_contacto()!= null ? usr.getTelf_contacto(): empty %>"> 
				</div>						
			</div>	
			<fieldset class="fieldset-clavefirma" >			
		   	<div style="margin: auto;width: 50%;padding: 3px; margin-top: 5px;">
				<div style="display: inline-block; width: 45%;margin: 3px;">
					<input class="menu-btn" name="add-usr-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de Usuarios" onclick="location.href='UserPage.jsp'"/>
				</div>
		   		
		   		<% 
		   		if (op > 0) {
		   			final String msg = (op == 1 ) ? "Crear usuario" : "Guardar cambios";   //$NON-NLS-1$ 
					final String tit= (op == 1 ) ? "Crea nuevo usuario":"Guarda las modificaciones realizadas";//$NON-NLS-2$
		   		%>
			   		
			   		<div  style="display: inline-block; width: 45%;margin: 3px;">
			   			<input class="menu-btn" name="add-usr-btn" type="submit" value="<%= msg %>" title="<%=tit %>" >
			   		</div>
		   		<% } %>
		   	</div>	
		</fieldset>	
		</form>
		<script>
			//bloqueamos los campos en caso de que sea una operacion de solo lectura
			document.getElementById("login-usr").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("role-usr").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("usr-name").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("usr-surname").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("email").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("telf-contact").disabled = <%= op == 0 ? "true" : "false" %>
																
		</script>
   	</div>
</body>
</html>