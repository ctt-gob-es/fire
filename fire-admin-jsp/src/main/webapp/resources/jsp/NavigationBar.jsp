<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	final String userLogged=(String)request.getSession().getAttribute("user");
%>
<div class="menubarPosition">
		<ul id="menubar">
			<li id="bar-txt"><b>Administraci&oacute;n de FIRe</b></li>
			<li id="header">				
					<nav>
						<ul class="nav">
							<li><a href="">Usuarios</a>
								<ul>
									<li><a href="../User/UserPage.jsp" title="Gestiona las cuentas de usuarios permitiendo: visualizar, crear, modificar y eliminar.">Gesti&oacute;n</a></li>
									<li><a href="../User/ChangePasswd.jsp?usr-name=<%=userLogged%>&op=3" title="Modificar contraseña del usuario logado">Cambio de contraseña</a></li>
								</ul>
							</li>
							<li><a href="../Application/AdminMainPage.jsp" title="Gestiona las apliaciones permitiendo: visualizar, crear, modificar y eliminar.">Aplicaciones</a>
								
							</li>
							<li><a href="../Certificate/CertificatePage.jsp" title="Gestiona los certificados permitiendo: visualizar, crear, modificar y eliminar.">Certificados</a>
								
							</li>
<!-- 							<li><a href="">Logs</a> -->
<!-- 								<ul> -->
<!-- 									<li><a href="">Gesti&oacute;n</a></li>								 -->
<!-- 								</ul> -->
<!-- 							</li> -->
						</ul>
					</nav>			
			</li>	
		</ul>
		<div id="userLogged" style="display:none;"><%=userLogged %></div>
		<div id="menuiemLogout">
			<a href="../Login.jsp" title="Cerrar sesión de usuario">
				<img src="../resources/img/logout.png" alt="Imagen esquem&aacute;tica de un una salida por una puerta" height="60" width="60" >
			</a>					
		</div>	
	</div>