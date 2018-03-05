<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>
<%@page import="es.gob.fire.server.services.internal.SessionFlags"%>
<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Properties"%>

<%
/*******************/

	

	String subjectId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
	String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
	String op = request.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
	
	if (subjectId == null || trId == null) {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		return;
	}
	
	FireSession fireSession = SessionCollector.getFireSession(trId, subjectId, session, true, true);
	if (fireSession == null) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
		return;
	}
	
	// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
		if (SessionFlags.OP_SIGN != fireSession.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
			fireSession = SessionCollector.getFireSession(trId, subjectId, session, false, true);
		}
		
		// Identificador del usuario
		String userId = fireSession.getString(ServiceParams.SESSION_PARAM_SUBJECT_ID);
		
		// Nombre de la aplicacion
		String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);
	
		String errorUrl = null;
		Properties connConfig = (Properties) fireSession.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
		if (connConfig != null && connConfig.containsKey(ServiceParams.CONNECTION_PARAM_ERROR_URL)) {
			errorUrl = connConfig.getProperty(ServiceParams.CONNECTION_PARAM_ERROR_URL);
			if (errorUrl != null) {
				errorUrl = URLEncoder.encode(errorUrl, "utf-8"); //$NON-NLS-1$
			}
		}
		
		// Preparamos el boton para cancelar o volver a atras. Se define el comportamiento
		// de este boton en base a si se forzo el origen (se muestra el boton Cancelar) o
		// no (boton Volver) 
		boolean originForced = Boolean.parseBoolean(
				fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED)
		);
		
		String buttonUrlParams = ServiceParams.HTTP_PARAM_SUBJECT_ID + "=" + userId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
				ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId; //$NON-NLS-1$
		if (originForced) {
			if (errorUrl != null) {
				buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else {
			if (op != null) {
				buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_OPERATION + "=" + op; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (errorUrl != null) {
				buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}


/******************/

	final String errorType = fireSession.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
	final String errorMsg = fireSession.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);
	boolean varStrings=false;
	
	// Preparamos el logo de la pantalla
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

	<meta name="description" content="Selección del certificado recién generado">
	<meta name="author" content="Ministerio de Hacienda y Función Pública">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Página de Error del certificado de firma</title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<link rel="stylesheet" type="text/css" href="css/modSignatureErrorPage.css">
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
						<div class="mod_claim_text_sec">Error en aplicación <%=appName %></div>	
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
				<div class="contenido-opciones" id="errorButtonsPanel">
					<div id="mensaje_error" class="mensaje-error" >
						<h2 class="title"><span class="bold">Error <%=String.valueOf(errorType)%></span></h2>
						<h3 class="subtitle"><span class="bold"><%=errorMsg%></span></h3>
					</div>
				
					<div id="botones_error" class="botones">
						<div class="containerbutton">
						<a href= "cancelOperationService?<%= buttonUrlParams %>" class="button-cancelar">
								<span >Cancelar</span>
							</a>
						</div>
						<div class="containerbutton">
						<a href= "ChooseCertificateOrigin.jsp?<%= buttonUrlParams %>" class="button-volver">
								<span class="arrow-left-white"></span>
								<span >volver</span>
							</a>
						</div>
						
						
							
					</div>				
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