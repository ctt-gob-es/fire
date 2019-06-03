
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO"%>
<%@page import="es.gob.fire.server.admin.entity.Application" %>
<%@page import="es.gob.fire.server.admin.dao.RolesDAO"%>
<%@page import="es.gob.fire.server.admin.entity.Role"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO" %>
<%@page import="es.gob.fire.server.admin.entity.User" %>
<%@page import="es.gob.log.consumer.client.ServiceOperations"%>
<%@page import="javax.json.JsonNumber"%>
<%@page import="java.util.Date"%>
<%@page import="javax.json.JsonString"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="javax.json.Json"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="java.text.SimpleDateFormat"%>


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	if (session == null) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	final Object state = session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED);
	final String usrLogged= (String) session.getAttribute(ServiceParams.SESSION_ATTR_USER);
	if (state == null || !Boolean.parseBoolean((String) state)) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}
	final String EMPTY = ""; //$NON-NLS-1$
	String idUsr = request.getParameter("id-usr");//$NON-NLS-1$
	String nameUser = request.getParameter("login-usr");//$NON-NLS-1$
	final int op = Integer.parseInt(request.getParameter("op"));//$NON-NLS-1$

	// op = 0 -> Solo lectura, no se puede modificar nada
	// op = 1 -> nuevo usuario
	// op = 2 -> editar usuario
	String title = ""; //$NON-NLS-1$
	String subTitle = ""; //$NON-NLS-1$
	
	User user = null;
	if (idUsr != null) {
		try {
			user = UsersDAO.getUser(idUsr);
		
		} catch (Exception e) {
			response.sendRedirect("UserPage.jsp");
			return;
		}
	}
	else {
		user = new User(); 
	}
	
	
	Role[] roles = RolesDAO.getRoles();
	
	
	
	
	switch (op) {
		case 0:
	title = "Ver usuario " + idUsr;//$NON-NLS-1$
	subTitle = "Visualizaci&oacute;n de los datos del usuario"; //$NON-NLS-1$
	break;
		case 1:
	title = "Alta de nuevo usuario"; //$NON-NLS-1$
	subTitle = "Inserte los datos del nuevo usuario."; //$NON-NLS-1$
	break;
		case 2:
	title = "Editar usuario " + user.getName() + " " + user.getSurname(); //$NON-NLS-1$ //$NON-NLS-2$
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
	<script>
	/*Importante estas variables (requestTypeCount y requestType) deben estar declaradas antes que la llamada a ../resources/js/certificate.js*/
	requestTypeCount="countRecordsUsersApp&id-usr="+<%= idUsr%>;
	requestType="getRecordsUsersApp&id-usr="+<%= idUsr%>;
	
	</script>
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
			  <%=subTitle%>
			</p>
		</div>
		
		<p>Los campos con * son obligatorios</p>
		
			<form id="formUser" method="post" autocomplete="off" action="../newUser?op=<%=op%>&idUser=<%=user.getId() != null ? user.getId() : EMPTY%>"> 
			
			
			
 			<div style="margin: auto;width: 100%;padding: 3px;">
					<div style="display: inline-block; width: 20%;margin: 3px;">
					
						<!-- Label para la accesibilidad de la pagina -->
						
						<label for="role-usr" style="color: #404040">* Tipo de usuario</label>
						<select id="role-usr" name="role-usr" class="edit-txt">
						
						<% if (user.getRole() == 0) { %>
							<option value="">Seleccione un Rol</option>
						<% } %>
							
						<%
							for (Role role : roles) {
						%>
								<option value="<%= role.getId() %>" <%= user.getRole() == role.getId() ? "selected" : EMPTY %>>
									<%= Role.getRoleLegibleText(role) %></option>
						<%
							}
						%>							
						</select>
							  					
					</div>

			</div> 
		
					
						
			
			<%
 			if(op==1 || op==0 ){
			%>	
				<div style="margin: auto;width: 100%;padding: 3px;">		
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="login-usr" style="color: #404040">* DNI (Login)</label>
						
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
					
						<input id="login-usr" class="edit-txt" type="text" name="login-usr"   style="width: 80%;margin-top:3px;" 
						value="<%=user.getUserName() != null ? user.getUserName(): EMPTY%>"> 						
						</div>
						
					
						
						
						<div  style="display: inline-block; width: 30%;margin: 3px;">
						<div  id = "msg" class="center" style="display: inline-block; color:#008000; width: 30%;margin: 3px;"></div>
						<div  id = "login-usr-img" class="center" style="display: inline-block; width: 30%;margin: 3px;"></div>
						</div>
				</div>									
			<%
			}else{
				%>
				<div style="display: inline" width: 100%;padding: 3px;">		
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="login-usr" style="color: #404040">* DNI (Login)</label>
					
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
				
					<input id="login-usr" class="edit-txt" type="text" name="login-usr"   style="width: 80%;margin-top:3px;" 
					value="<%=user.getUserName() != null ? user.getUserName(): EMPTY%>"> 						
					</div>
					</div>
			<%	
			}
			
			if(op==1){
			%>
			<div style="margin: auto;width: 100%;padding: 3px;">	
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwd-usr" style="color: #404040" >* Contraseña</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="passwd-usr" type="password" class="edit-txt"  name="passwd-usr" style="width: 80%;margin-top:3px;"
						value="">
					</div>
					<div style="display: inline-block; width: 10%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwd-usr-copy" style="color: #404040" >* Repetir contraseña</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="passwd-usr-copy" type="password" class="edit-txt"  name="passwd-usr-copy" style="width: 80%;margin-top:3px;"
						value="">
					</div>		
				</div>							
			<%
			}else if(op == 2){
				
			%>
			<div style="display: none" width: 100%;padding: 3px;">	
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwd-usr" style="color: #404040" >* Contraseña</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="passwd-usr" type="password" class="edit-txt"  name="passwd-usr" style="width: 80%;margin-top:3px;"
						value="">
					</div>
					<div style="display: inline-block; width: 10%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="passwd-usr-copy" style="color: #404040" >* Repetir contraseña</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="passwd-usr-copy" type="password" class="edit-txt"  name="passwd-usr-copy" style="width: 80%;margin-top:3px;"
						value="">
					</div>		
				</div>							
			<%	
			}
			%>
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="usr-name" style="color: #404040">* Nombre </label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="usr-name" class="edit-txt" type="text" name="usr-name" style="width: 80%;margin-top:3px;" 
						value="<%=user.getName()!= null ? user.getName(): EMPTY%>"> 
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="usr-surname" style="color: #404040">* Apellidos</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					<input id="usr-surname" class="edit-txt" type="text" name="usr-surname" style="width: 80%;margin-top:3px;" 
						value="<%=user.getSurname()!= null ? user.getSurname(): EMPTY%>"> 
				</div>						
			</div>	
			
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="email" style="color: #404040">* Correo electrónico</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="email" class="edit-txt" type="text" name="email" style="width: 80%;margin-top:3px;" 
						value="<%=user.getMail() != null ? user.getMail(): EMPTY%>"> 
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="telf-contact" style="color: #404040">Telf. Contacto</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					<input id="telf-contact" class="edit-txt" type="text" name="telf-contact" style="width: 80%;margin-top:10px;" 
						value="<%=user.getTelephone() != null ? user.getTelephone(): EMPTY%>"> 
				</div>						
			</div>	
			
			
			<fieldset class="fieldset-clavefirma" >			
		   	<div style="margin: auto;width: 50%; padding: 3px; margin-top: 5px;">
				<div style="display: inline-block; width: 45%; margin: 3px;">
					<input class="menu-btn" name="add-usr-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de Usuarios" onclick="location.href='UserPage.jsp'" onclick="imgValidarDni()"/>
				</div>
		   		
		   		<% 
		   		if (op > 0) {
		   			final String msg = (op == 1 ) ? "Crear usuario" : "Guardar cambios"; //$NON-NLS-1$ //$NON-NLS-2$
					final String tit= (op == 1 ) ? "Crea nuevo usuario" : "Guarda las modificaciones realizadas"; //$NON-NLS-1$ //$NON-NLS-2$
		   		%>
			   		
			   		<div  style="display: inline-block; width: 45%;margin: 3px;">
			   			<input class="menu-btn" name="add-usr-btn" type="submit" value="<%= msg %>" title="<%=tit %>" >
			   		</div>
		   		<% } %>
		   	</div>	
		</fieldset>	
		</form>
		<%
			if (op == 0) {
			%>
		<fieldset>
		<script type="text/javascript">
	/*Importante estas variables (requestTypeCount y requestType) deben estar declaradas antes que la llamada a ../resources/js/user.js*/
			requestType="getRecordsUsersApp";
			idUser = "<%= idUsr %>";
</script>
		
			<legend>Responsables Aplicaciones</legend>
			<div id="data" style="display: block-inline; text-align:center;">
			
				<h4>No hay Aplicaciones asociadas al usuario responsable <%=user.getName()%> <%=user.getSurname()%></h4>		
			
			</div>

		</fieldset>
		
			<%
			}
			%>
		
		<script >
		<%
		if (op == 2) {
		%>
		document.getElementById("login-usr").disabled = 'disabled';
		document.getElementById("login-usr").style.background = '#F5F5F5';
		<%
		}
		%>
			<%
			if (op == 0) {
			%>
			
				//bloqueamos los campos en caso de que sea una operacion de solo lectura
				document.getElementById("login-usr").disabled = 'disabled';
				document.getElementById("login-usr").style.background = '#F5F5F5';
				document.getElementById("role-usr").disabled = 'disabled';
				document.getElementById("role-usr").style.background = '#F5F5F5';
				document.getElementById("usr-name").disabled = 'disabled';
				document.getElementById("usr-name").style.background = '#F5F5F5';
				document.getElementById("usr-surname").disabled = 'disabled';
				document.getElementById("usr-surname").style.background = '#F5F5F5';
				document.getElementById("email").disabled = 'disabled';
				document.getElementById("email").style.background = '#F5F5F5';
				document.getElementById("telf-contact").disabled = 'disabled';
				document.getElementById("telf-contact").style.background = '#F5F5F5';
			<%
			}
			else if (user.isRoot()) {
			%>
				document.getElementById("role-usr").disabled = 'disabled';
				document.getElementById("role-usr").style.background = '#F5F5F5';
				
			<%
			}
			%>
			document.getElementById("role-usr").addEventListener("change", roleSelection);
			document.getElementById("login-usr").addEventListener("input", onInputDniCallback);
			
			
			
			
			var rolesWithAccess = {};
			<%
				for (Role role : roles) {
					if (role.getPermissions().hasLoginPermission()) {
			%>
						rolesWithAccess.id<%= role.getId() %> = true;
			<% 
					}
				}
			%>
			

		// recuperamos los valores de los usuarios que son responsables
			
			var rolesWithResponsable = [];
			<%
				int idx = 0;
				for (Role role : roles) {
					if (role.getPermissions().hasAppResponsable()) {
			%>
						rolesWithResponsable[<%= idx %>] = <%= role.getId() %>;
			<% 
						++idx;
					}
				}
			%>	
			
			
			
			
			
			
			
			function roleSelection(evt) {
				var id = 'id' + evt.target.value;
				var hasAccess = eval("rolesWithAccess." + id);
				var color = $('#search-form').find('input:text,select, textarea').val();
				
				if (hasAccess) {
					// Habilitar
					document.getElementById("passwd-usr").disabled = null;
					document.getElementById("passwd-usr").style.background = null;
					document.getElementById("passwd-usr-copy").disabled = null;
					document.getElementById("passwd-usr-copy").style.background = null;
					

				}
				else {
					// Deshabilitar
					document.getElementById("passwd-usr").value = '';
					document.getElementById("passwd-usr").disabled = 'disabled';
					document.getElementById("passwd-usr").style.background = '#F5F5F5';
					document.getElementById("passwd-usr-copy").value = '';
					document.getElementById("passwd-usr-copy").disabled = 'disabled';
					document.getElementById("passwd-usr-copy").style.background = '#F5F5F5';
				}
			}
						
			
			
			
			
			
			// Funcion para recoger la validacion del dni ir mostrando en el campo de texto
			function onInputDniCallback(evt) {
				 

				var dni = evt.target.value;
				var valido = comprobarDni(dni);
				
				var imagen;
				var mensaje;
				var color;
				var texto = '';
				
				if (valido == true) {
					imagen = "../resources/img/comprobado_icon.png";	
					mensaje = "DNI Válido";
					color = "#90EE90"
					
				
				document.getElementById("msg").innerHTML = mensaje;
				//document.getElementById("mensaje").style.background = color;
				
				document.getElementById("login-usr-img").innerHTML='<img src="' + imagen + '" width="40" height="30" alt=""/>';

			}else{
				imagen = "../resources/img/incorrecto.png";
				mensaje = "DNI incorrecto";
				//color = '#F08080';
				document.getElementById("msg").innerHTML = '';
				document.getElementById("login-usr-img").innerHTML='';
			}
			}
			
			
		</script>
		
   	</div>
</body>
<script type="text/javascript">
	/*Importante estas variables (requestTypeCount y requestType) deben estar declaradas antes que la llamada a ../resources/js/user.js*/
			requestType="getRecordsUsersApp";
			idUser = "<%= idUsr != null ? idUsr : "" %>";
</script>

</html>