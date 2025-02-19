<%@page import="es.gob.fire.server.services.Responser"%>
<%@page import="es.gob.fire.server.services.FIReError"%>
<%@page import="es.gob.fire.server.services.internal.ErrorManager"%>
<%@page import="es.gob.fire.server.services.internal.TransactionAuxParams"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="es.gob.fire.server.services.ProjectConstants"%>
<%@page import="es.gob.fire.server.services.internal.TransactionConfig"%>
<%@page import="es.gob.fire.server.services.internal.ProviderInfo"%>
<%@page import="es.gob.fire.server.services.internal.ProviderManager"%>
<%@page import="es.gob.fire.server.services.internal.FirePages"%>
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Properties"%>
<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>
<%@page import="es.gob.fire.server.services.internal.ServiceNames"%>
<%@page import="es.gob.fire.i18n.Language"%>
<%@page import="es.gob.fire.i18n.IWebViewMessages"%>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

	String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
	String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
	
	if (subjectRef == null || trId == null) {
		Responser.sendError(response, FIReError.FORBIDDEN);
		return;
	}

	TransactionAuxParams trAux = new TransactionAuxParams(null, trId);

	final FireSession fireSession = SessionCollector.getFireSessionOfuscated(trId, subjectRef, session, true, false, trAux);
	if (fireSession == null) {
		Responser.sendError(response, FIReError.FORBIDDEN);
		return;
	}
	
	String appId = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
	if (appId != null) {
		trAux.setAppId(appId);
	}
	
	String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_TITLE);
	TransactionConfig connConfig = (TransactionConfig) fireSession.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);

	// Usamos la URL de error indicada en la transaccion
	String errorUrl = connConfig.getRedirectErrorUrl();
	
	String language = fireSession.getString(ServiceParams.SESSION_PARAM_LANGUAGE);
	if (language == null || language.isEmpty()) {
		language = "es";
	}
	Language.changeFireSignatureMessagesConfiguration(new Locale(language));

	// Preparamos el logo de la pantalla
	String logoUrl = ConfigManager.getPagesLogoUrl();
	if (logoUrl == null || logoUrl.isEmpty()) {
		logoUrl = "img/general/dms/logo-institucional.png"; //$NON-NLS-1$
	}

	// Comprobamos tener un listado de proveedores validos
	String[] providers = (String[]) fireSession.getObject(ServiceParams.SESSION_PARAM_PROVIDERS);
	if (providers == null || providers.length == 0) {
		ErrorManager.setErrorToSession(fireSession, FIReError.PROVIDER_NOT_SELECTED, trAux);
		if (errorUrl != null) {
			errorUrl = URLDecoder.decode(errorUrl, "utf-8"); //$NON-NLS-1$
			Responser.redirectToExternalUrl(errorUrl, request, response, trAux);
		} else {
			Responser.sendError(response, FIReError.FORBIDDEN);
		}
		return;
	}
%>

<!DOCTYPE html>
<html xml:lang="es" lang="es" class="no-js">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Expires" content="0" />
	<meta http-equiv="Content-Security-Policy" content="style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src * data:">

	<meta name="description" content="Selección de origen del certificado">
	<meta name="author" content="Gobierno de España">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title><%= Language.getResFireSignature(IWebViewMessages.SIGN_MECHANISM_SYSTEM_SUBTITLE) %></title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<link rel="stylesheet" type="text/css" href="css/headerFooter.css">
	<link rel="stylesheet" type="text/css" href="css/modChooseCertificateOrigin.css">
	<link rel="stylesheet" type="text/css" href="css/personal.css">
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
						<div class="mod_claim_text_sec"><%= Language.getResFireSignature(IWebViewMessages.SIGN_REQUESTED_BY_TITLE) %> <%= appName %></div>
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
		<div class="container-title">
			<div class="title-head">
				<h1 class="title"><%= Language.getResFireSignature(IWebViewMessages.SIGN_SYSTEM_SELECT_TITLE) %></h1>
			</div>	
		</div>
		
		<div class="container-box">	
		<%
		for (String provider : providers) {
			ProviderInfo info = ProviderManager.getProviderInfo(provider, trAux.getLogFormatter(), language);
		%>
			<div name="provider-option" class="main-box-left" id="option<%= info.getName() %>">
			
				<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_CHOOSE_CERT_ORIGIN %>" id="form<%= info.getName() %>" class="formProvider">
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT_ORIGIN %>" value="<%= info.getName() %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_NEED_AUTH_USER %>" value="<%= info.isUserRequiredAutentication() %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_LANGUAGE %>" value="<%= language %>" />
				</form>
				
				<div class="contain-box-top">
					<img alt="<%= info.getTitle() %>" title="<%= info.getTitle() %>" src="<%= info.getLogoUri() %>" >
				</div>
				<div class="contain-box-bottom">
					<h2 class="title-box"><%= info.getHeader() %></h2>
					<% if (info.isNeedJavaScript()) { %>
						<noscript>
							<p class="text-box">Su navegador web tiene JavaScript desactivado. Habilite JavaScript para poder usar sus certificados.</p>
						</noscript>
					<% } %>
					<p name="provider-description" class="text-box <%= info.isNeedJavaScript() ? "hide" : "" %>">
						<%= info.getDescription() %>													
					</p>
				</div>
				<a name="provider-option" class="button <%= info.isNeedJavaScript() ? "invisible" : "" %>" title="<%= info.getHeader() %>" onclick="document.getElementById('form<%= info.getName() %>').submit();" href="javascript:{}">
					<span><%= Language.getResFireSignature(IWebViewMessages.ACCESS_BTN) %></span>
					<span class="arrow-right arrow-right-inicio"></span>
				</a>
			</div>
		<%
		}
		%>
			</div>
			<div class="container-title">
				<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_CANCEL_OPERATION %>" id="formCancel">
					<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
					<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
					<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
					<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_LANGUAGE %>" name="<%= ServiceParams.HTTP_PARAM_LANGUAGE %>" value="<%= language %>" />
				</form>
				
				<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_CHANGE %>" id="changeLangForm">
					<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
					<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
					<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
					<input type="hidden" id="languageConf" name="<%= ServiceParams.HTTP_PARAM_LANGUAGE %>" value="<%= language %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_PAGE %>" value="<%= FirePages.PG_CHOOSE_CERTIFICATE_ORIGIN %>" />
				</form>

				<div class="title-button">
					<a class="button-cancelar" onclick="document.getElementById('formCancel').submit();" href="javascript:{}">
						<span><%= Language.getResFireSignature(IWebViewMessages.CANCEL_BTN) %></span>
					</a>
				</div>
			</div>
					
		</section>
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
   	
		// Si funciona JavaScript podremos retirar la clase que
		// desactiva la opcion de firma local y la que oculta su texto
		var disabledElementsProvDesc = document.getElementsByName("provider-description");
		for (var i = 0; i < disabledElementsProvDesc.length; i++) {		
			disabledElementsProvDesc[i].className= disabledElementsProvDesc[i].className.replace( /(?:^|\s)hide(?!\S)/g , '' );	
		}
		
		var disabledElementsProvOp = document.getElementsByName("provider-option");
		for (var i = 0; i < disabledElementsProvOp.length; i++) {
			disabledElementsProvOp[i].className=disabledElementsProvOp[i].className.replace( /(?:^|\s)invisible(?!\S)/g , '' )	
		}
		
		function changeLanguage() {
			var languageSelected = document.getElementById('languageSelect').value;	
			document.getElementById('languageConf').value = languageSelected;
		    document.getElementById('changeLangForm').submit();
		}
   	</script>
</body>
</html>