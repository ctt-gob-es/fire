

<%@page import="es.gob.fire.signature.ConfigManager" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	final String userLogged = (String)request.getSession().getAttribute("user");//$NON-NLS-1$ 
	boolean statistic = false;
	try {
    	ConfigManager.checkConfiguration();
	}
	catch (final Exception e) {	
		statistic = false;
	}
	
	
	if(ConfigManager.getConfigStatistics()!= null && !"".equals(ConfigManager.getConfigStatistics()) ){//$NON-NLS-1$ 
		if(Integer.parseInt(ConfigManager.getConfigStatistics()) == 2){
			statistic = true;
		}
	}
%>
<div class="menubarPosition">
		<ul id="menubar">
			<li id="bar-txt"><b>Administraci&oacute;n de FIRe</b></li>
			<li id="header">				
					<nav>
						<ul class="nav">
							<li><a>Usuarios</a>
								<ul>
									<li><a class="enl_mnu" href="../User/UserPage.jsp" title="Gestiona las cuentas de usuarios permitiendo: visualizar, crear, modificar y eliminar.">Gesti&oacute;n</a></li>
									<li><a class="enl_mnu" href="../User/ChangePasswd.jsp?usr-name=<%= userLogged%>&op=3" title="Modificar contraseña del usuario logado">Cambio de contraseña</a></li>
								</ul>
							</li>
							<li><a class="enl_mnu" href="../Application/AdminMainPage.jsp" title="Gestiona las apliaciones permitiendo: visualizar, crear, modificar y eliminar.">Aplicaciones</a>
								
							</li>
							<li><a class="enl_mnu" href="../Certificate/CertificatePage.jsp" title="Gestiona los certificados permitiendo: visualizar, crear, modificar y eliminar.">Certificados</a>
								
							</li>
							<li><a class="enl_mnu" href="../Logs/LogsMainPage.jsp" title="Gestiona los servidores de Logs permitiendo: visualizar, crear, modificar, eliminar y conectarse al servidor para la gestión del fichero seleccionado.">Logs</a>
							</li>
							<% if(statistic){ %>						
							<li><a class="enl_mnu" href="../Statistics/StatisticsMainPage.jsp"  title="Consulta de datos estadísticos de las transacciones de firma realizadas." >Estad&iacute;sticas</a>
							</li>
							<% }%>
						</ul>
					</nav>			
			</li>	
		</ul>
		<div id="userLogged" style="display:none;"><%= userLogged %></div>
		<div id="menuiemLogout">
			<a class="enl_mnu" href="../Login.jsp" title="Cerrar sesión de usuario">
				<img src="../resources/img/logout.png" alt="Imagen esquem&aacute;tica de un una salida por una puerta" height="60" width="60" >
			</a>					
		</div>	
	</div>
<script type="text/javascript">
	//Comprueba cada vez que se pulsa en un enlace del menu, si se ha abiero el fichero log
	// en ese caso lanza la función de cerrar fichero log.
	// Solo se lanza cuando este menu esta la pagina LogsManager.jsp
	var openLog = false; 
 	$(".enl_mnu").click(function(e){
	 	if(openLog){
			closeFile();
	 	}
	});
 
 
</script>