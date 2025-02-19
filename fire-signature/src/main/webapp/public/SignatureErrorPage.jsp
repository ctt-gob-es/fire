<%@page import="es.gob.fire.server.services.FIReError"%>
<%@page import="es.gob.fire.server.services.ProjectConstants"%>
<%@page import="es.gob.fire.server.services.Responser"%>
<%@page import="es.gob.fire.server.services.internal.TransactionAuxParams"%>
<%@page import="es.gob.fire.server.services.internal.ServiceNames"%>
<%@page import="es.gob.fire.server.services.internal.FirePages"%>
<%@page import="es.gob.fire.server.services.internal.TransactionConfig"%>
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>
<%@page import="es.gob.fire.server.services.internal.SessionFlags"%>
<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Properties"%>
<%@page import="java.util.Locale"%>
<%@page import="es.gob.fire.i18n.Language"%>
<%@page import="es.gob.fire.i18n.IWebViewMessages"%>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

	String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
	String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
	
	if (subjectRef == null || trId == null) {
		Responser.sendError(response, FIReError.FORBIDDEN);
		return;
	}

	TransactionAuxParams trAux = new TransactionAuxParams(null, trId);

	// Nos aseguramos de tener cargada la ultima version de la sesion
	FireSession fireSession = SessionCollector.getFireSessionOfuscated(trId, subjectRef, session, false, true, trAux);
	if (fireSession == null) {
		Responser.sendError(response, FIReError.FORBIDDEN);
		return;
	}
	
	String language = fireSession.getString(ServiceParams.SESSION_PARAM_LANGUAGE);
	if (language == null || language.isEmpty()) {
		language = "es";
	}
	Language.changeFireSignatureMessagesConfiguration(new Locale(language));

	// Recuperamos la informacion del la aplicacion
	String appId = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
	String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_TITLE);

	trAux.setAppId(appId);

	String errorUrl = null;
	TransactionConfig connConfig = (TransactionConfig) fireSession
	.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
	if (connConfig != null && connConfig.isDefinedRedirectErrorUrl()) {
		errorUrl = connConfig.getRedirectErrorUrl();
		if (errorUrl != null) {
	errorUrl = URLEncoder.encode(errorUrl, "utf-8"); //$NON-NLS-1$
		}
	}

	// Preparamos el boton para cancelar o volver a atras. Se define el comportamiento
	// de este boton en base a si se forzo el origen (se muestra el boton Cancelar) o
	// no (boton Volver) 
	boolean originForced = Boolean.parseBoolean(fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED));

	// Cargamos los errores configurados
	final String errorType = fireSession.getString(ServiceParams.SESSION_PARAM_ERROR_TYPE);
	final String errorMsg = fireSession.getString(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);

	// Eliminamos los errores de la sesion para que no afecten a siguientes operaciones
	fireSession.removeAttribute(ServiceParams.SESSION_PARAM_ERROR_TYPE);
	fireSession.removeAttribute(ServiceParams.SESSION_PARAM_ERROR_MESSAGE);

	SessionCollector.commit(fireSession, trAux);

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
	<meta http-equiv="Cache-Control" content="no-cache, no-store" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Expires" content="0" />
	<meta http-equiv="Content-Security-Policy" content="style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' *">

	<meta name="description" content="Error en la petición de firma">
	<meta name="author" content="Gobierno de España">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title><%= Language.getResFireSignature(IWebViewMessages.ERROR_PAGE) %></title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<link rel="stylesheet" type="text/css" href="css/headerFooter.css">
	<link rel="stylesheet" type="text/css" href="css/modSignatureErrorPage.css">
</head>
<body>
	<%-- Barra de navegacion --%>
	<header>
		<div class="header_top wrapper">
			<div class="mod_claim_izq">
				<div class="mod_claim_in mod_claim_in_izq mod_claim_in_image">
					<a title="Logo">
						<img alt="Logo" src="<%= logoUrl %>">
					</a> 
				</div>
				<div class="mod_claim_in_der">
					<div class="mod_claim_text"><%= ConfigManager.getPagesTitle() %></div>	
					<% if (appName != null && appName.length() > 0) { %>				
						<div class="mod_claim_text_sec"><%= Language.getResFireSignature(IWebViewMessages.SIGN_REQUESTED_BY_TITLE) %> <%=appName %></div>	
					<% } %>				
				</div>
			</div>
			<div class="clr"></div>
			<div class="header_menu_right"><%= Language.getResFireSignature(IWebViewMessages.SELECT_LANGUAGE) %>:						
			<select id="languageSelect" name="languageSelect" onchange="changeLanguage()">
				<option value="es" <%= language != null && language.equals("es") ? "selected" : "" %>>Espa&ntilde;ol</option>
				<option value="en" <%= language != null && language.equals("en") ? "selected" : "" %>>English</option>
				<option value="ca" <%= language != null && language.equals("ca") ? "selected" : "" %>>Catal&agrave;</option>
				<option value="gl" <%= language != null && language.equals("gl") ? "selected" : "" %>>Galego</option>
				<option value="eu" <%= language != null && language.equals("eu") ? "selected" : "" %>>Euskera</option>
				<option value="va" <%= language != null && language.equals("va") ? "selected" : "" %>>Valenciano</option>
			</select>
		</div>
		</div>
	</header>

	<%-- contenido --%>
	<main class="main">		
		<section class="contenido">
			<div  class="container-box-title">
				<div class="contenido-opciones" id="errorButtonsPanel">
					<div id="mensaje_error" class="mensaje-error" >
						<h2 class="title"><span class="bold">Error <%=String.valueOf(errorType)%></span></h2>
						<h3 class="subtitle"><span class="bold"><%=errorMsg%></span></h3>
					</div>
				
					<div id="containerError" class="botones">
						<div class="containerbutton">
	
							<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_CANCEL_OPERATION %>" id="formCancel">
								<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
								<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
								<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
							</form>
						
							<a class="button-cancelar" onclick="document.getElementById('formCancel').submit();" href="javascript:{}">
								<span ><%= Language.getResFireSignature(IWebViewMessages.CANCEL_BTN) %></span>
							</a>
						</div>
					<% if (!originForced) { %>
						<div class="containerbutton">
							<div class="separatorbutton"></div>
							<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_BACK %>" id="formBack">
								<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
								<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
								<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
								<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_PAGE %>" value="<%= FirePages.PG_CHOOSE_CERTIFICATE_ORIGIN %>" />
							</form>
						
							<a class="button-volver" onclick="document.getElementById('formBack').submit();" href="javascript:{}">
								<span class="arrow-left-white"></span>
								<span ><%= Language.getResFireSignature(IWebViewMessages.RETURN_BTN) %></span>
							</a>
					<% } %>
						</div>
					</div>

				</div>
			</div>
		</section>
		
		<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_CHANGE %>" id="changeLangForm">
			<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
			<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
			<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
			<input type="hidden" id="languageConf" name="<%= ServiceParams.HTTP_PARAM_LANGUAGE %>" value="<%= language %>" />
			<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_PAGE %>" value="<%= FirePages.PG_SIGNATURE_ERROR %>" />
			<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_ERROR_TYPE %>" name="<%= ServiceParams.HTTP_PARAM_ERROR_TYPE %>" value="<%= errorType %>" />
			<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_ERROR_MESSAGE %>" name="<%= ServiceParams.HTTP_PARAM_ERROR_MESSAGE %>" value="<%= errorMsg %>" />
		</form>
		
	</main>		
	
	<%-- Pie de pagina --%>
	<div class="clear" ></div>	
	<footer class="mod_footer">
		<div class="footer_top wrapper">
			<div class="txt_alignL">
				<img alt="FIRe" title="FIRe" src="img/general/dms/logo-fire-pie.png">
			</div>
		</div>
		<div class="footer_bottom wrapper">
			<div class="comp_left">
			  <p class="footer-text">FIRe v<%= ProjectConstants.VERSION %> &copy; <%= ProjectConstants.COPY_YEAR %> Gobierno de Espa&ntilde;a</p>
			</div>
		</div>
	</footer>
	<script type="text/javascript">
	 	function changeLanguage() {
			var languageSelected = document.getElementById('languageSelect').value;	
			document.getElementById('languageConf').value = languageSelected;
			document.getElementById('changeLangForm').submit();
		}   		
	</script>
</body>
</html>