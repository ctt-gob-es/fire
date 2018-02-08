<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>

<%@page import="es.gob.fire.server.services.internal.SessionFlags"%>
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
String subjectId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);


FireSession fireSession = SessionCollector.getFireSession(trId, subjectId, session, true, false);
if (fireSession == null) {
	response.sendError(HttpServletResponse.SC_FORBIDDEN);
	return;
}
//Nombre de la aplicacion
	String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);

//Preparamos el logo de la pantalla
	String logoUrl = ConfigManager.getPagesLogoUrl();
	if (logoUrl == null || logoUrl.isEmpty()) {
		logoUrl = "img/general/dms/logo-institucional.png"; //$NON-NLS-1$
	}
%>
<!DOCTYPE html>
<html xml:lang="es" lang="es" class="no-js">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="description" content="Error selección del certificado">
	<meta name="author" content="Ministerio de Hacienda y Función Pública">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Error selección de certificado de firma</title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/modules.css">
</head>
<body>
<!-- Barra de navegacion -->
	<header>
		<div class="header_top wrapper">
			<div class="mod_claim_izq">
				<div class="mod_claim_in mod_claim_in_izq">
					<a title="Logo">
						<img alt="Logo" src="<%= logoUrl %>">
					</a> 
				</div>
				<div class="mod_claim_in_der">
					<div class="mod_claim_text"><%= ConfigManager.getPagesTitle() %></div>
					<% if (appName != null && appName.length() > 0) { %>
						<div class="mod_claim_text_sec">Firma solicitada por <%= appName %></div>
					<% } %>
				</div>
			</div>
			<div class="clr"></div>
		</div>
	</header>
		<!-- contenido -->
	<main class="main">
	<section class="contenido">
				<div  class="container-box-title">
					<div class="container_tit">
						<h1 class="title"><span class="bold">Seleccione el certificado de firma</span></h1>
					</div>
					<div class="container_btn_operation">					
						<a href= "ChooseCertificateOrigin.jsp" class="button-volver">
							<span class="arrow-left-white"></span>
							<span >volver</span>
						</a>	
					</div>
				</div>
	</section>
	</main>
	<!-- Pie de pagina -->
	<div class="clear" ></div>	
	<footer class="mod_footer">
		<div class="footer_top wrapper">
			<div class="txt_alignL">
				<img alt="FIRe" title="FIRe" src="img/general/dms/logo-fire-pie.png">
			</div>
		</div>
		<div class="footer_bottom wrapper">
			<div class="comp_left">
			  <p class="footer-text">&copy; 2017 Gobierno de Espa&ntilde;a - FIRe</p> 
			</div>
		</div>
	</footer>
</body>
</html>