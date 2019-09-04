

<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.LogServersDAO" %>
<%@page import="es.gob.fire.server.admin.entity.LogServer" %>


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
	final String empty = "";//$NON-NLS-1$
	
	String idSrv = request.getParameter("id-srv");//$NON-NLS-1$
	final int act = Integer.parseInt(request.getParameter("act"));//$NON-NLS-1$

	String title = ""; //$NON-NLS-1$
	String subTitle = ""; //$NON-NLS-1$
	
	LogServer logSrv;
	logSrv = idSrv != null ? LogServersDAO.selectLogServer(idSrv)  : new LogServer();
	
	switch (act) {

		case 2:
			title = "Alta de nuevo servidor de log"; //$NON-NLS-1$
			subTitle = "Inserte los datos del nuevo servidor."; //$NON-NLS-1$
			break;
		case 3:
			title = "Editar servidor " + logSrv.getNombre() + " " + logSrv.getUrl(); //$NON-NLS-1$ //$NON-NLS-2$
			subTitle = "Modifique los datos que desee editar."; //$NON-NLS-1$
			break;
		case 5:
			title = "Visualizar servidor de log"; //$NON-NLS-1$
			subTitle = "Visualizar los datos del  servidor."; //$NON-NLS-1$
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
	<script type="text/javascript">var act=<%=act%>;</script>
	

</head>
<body>
<!-- Barra de navegacion -->		
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	<!-- contenido -->
	<div id="container">
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  <%=subTitle %>
			</p>
		</div>
		
		<p>Los campos con * son obligatorios</p>
			<form id="formLogServer" method="post" autocomplete="off" action="../logServer?act=<%= act%><%= idSrv != null ? ("&id-srv=" + idSrv) : "" %>"> 
			
				<div style="margin: auto;width: 100%;padding: 3px;">		
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="name-srv" style="color: #404040">* Nombre de servidor</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
						<input id="name-srv" class="edit-txt" type="text" name="name-srv" style="width: 80%;margin-top:3px;" 
						value="<%= logSrv.getNombre() != null ? logSrv.getNombre() : empty %>"/> 
					</div>
				</div>									
																		
				<div style="margin: auto;width: 100%;padding: 3px;">
					<div style="display: inline-block; width: 20%;margin: 3px;">
							<!-- Label para la accesibilidad de la pagina -->
							<label for="clave" style="color: #404040" >* Clave</label>
					</div>
					<div  style="display: inline-block; width: 30%;margin: 3px;">
							<input id="clave" type="password" class="edit-txt"  name="clave" style="width: 80%;margin-top:3px;"
							value="<%= logSrv.getClave() != null ? logSrv.getClave() : empty %>"/>
					</div>		
				</div>

				<div style="margin: auto;width: 100%;padding: 3px;">
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<!-- Label para la accesibilidad de la pagina -->
						<label for="url" style="color: #404040">* Direcci&oacute;n URL</label>
					</div>
					<div  style="display: inline-block; width: 50%;margin: 3px;">
						<input id="url" class="edit-txt" type="text" name="url" style="width: 80%;margin-top:3px;" 
							value="<%= logSrv.getUrl() != null ? logSrv.getUrl() : empty %>"/>
					</div>

					<div style="display: inline-block; width: 25%;margin: 3px;">
						<input class="menu-btn" name="echo-srv-btn" type="button" value="Comprobar conexi&oacute;n" title="Comprueba la conexi&oacute;n con el Servidor de Log" 
						onclick="comprobarServidorLog();"/>
					</div>
				</div>
				<div style="margin: auto;width: 100%;padding: 3px;">
					<div  style="display: inline-block; margin: 3px; text-align: left;">
						<input id="verifyssl" name="verifyssl" type="checkbox" style="margin-top:3px;" 
							 value="true" <%= logSrv.isVerificarSsl() ? "checked" : "" %> />
						<label for="verifyssl" style="color: #404040">Verificar certificado SSL servidor</label>
					</div>
				</div>
				<div style="margin: auto;width: 100%;padding: 3px;">
					<div id="urlStatus" style="display: inline-block; width: 50%;margin: 3px;">
						<div id="okIcon" style="display:none;"><span id="messageOk"></span><img alt="Icono indicando la conexión correcta de la url del servidor" src="../resources/img/comprobado_icon.png" width="22px" height="22px"></div>
						<div id="NoOkIcon" style="display:none;"><span id="messageNoOk"></span><img alt="Icono indicando la conexión incorrecta de la url del servidor" src="../resources/img/sin_entrada_icon.png" width="22px" height="22px"></div>
					</div>
				</div>
				<br>
			<fieldset class="fieldset-clavefirma" >			
		   	<div style="margin: auto;width: 50%;padding: 3px; margin-top: 5px;">
				<div style="display: inline-block; width: 45%;margin: 3px;">
					<input class="menu-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de Servidores de Log" onclick="location.href='LogsMainPage.jsp'"/>
				</div>
		   		
		   		<% 
		   		if (act != 5) {
		   			final String msg = (act == 1) ? "Crear servidor" : "Guardar cambios";   //$NON-NLS-1$ //$NON-NLS-2$
					final String tit = (act == 1) ? "Crea nuevo servidor" : "Guarda las modificaciones realizadas";//$NON-NLS-1$ //$NON-NLS-2$
		   		%>
			   		
			   		<div  style="display: inline-block; width: 45%;margin: 3px;">
			   			<input class="menu-btn" type="submit" value="<%= msg %>" title="<%=tit %>" >
			   		</div>
		   		<% } %>
		   	</div>	
		</fieldset>	
		</form>
		<script>
			//bloqueamos los campos en caso de que sea una operacion de solo lectura
			document.getElementById("name-srv").disabled = <%= act == 5 %>
			document.getElementById("url").disabled = <%= act == 5 %>
			document.getElementById("verifyssl").disabled = <%= act == 5 %>
			document.getElementById("clave").disabled = <%= act == 5 %> 
			
			if(act == 5){
				document.getElementById("url").style.background = '#F5F5F5';
				document.getElementById("clave").style.background = '#F5F5F5';
				document.getElementById("name-srv").style.background = '#F5F5F5';
			}
			
																
		</script>
   	</div>
</body>
<script src="../resources/js/validateLogServer.js" type="text/javascript"></script>
</html>