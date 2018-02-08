<%@page import="es.gob.fire.server.services.internal.ProviderInfo"%>
<%@page import="es.gob.fire.server.services.internal.ProviderManager"%>
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Properties"%>
<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
	String subjectId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
	String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
	String unregistered = request.getParameter(ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED);
	boolean userRegistered = !Boolean.parseBoolean(unregistered);
	String errorUrl = request.getParameter(ServiceParams.HTTP_PARAM_ERROR_URL);
	
	if (subjectId == null || trId == null) {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		return;
	}

	final FireSession fireSession = SessionCollector.getFireSession(trId, subjectId, session, false, false);
	if (fireSession == null) {
		if (errorUrl != null) {
			response.sendRedirect(errorUrl);	
		}
		else {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
		return;
	}
	
	String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);
	Properties connConfig = (Properties) fireSession.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
	if (connConfig != null && connConfig.containsKey(ServiceParams.CONNECTION_PARAM_ERROR_URL)) {
		errorUrl = connConfig.getProperty(ServiceParams.CONNECTION_PARAM_ERROR_URL);
	} else {
		errorUrl = URLEncoder.encode(errorUrl, "utf-8"); //$NON-NLS-1$
	}

	// En caso de que accedamos desde un dispositivo movil y la operacion sea de lote,
	// accedemos directamente al uso de certificados remotos
	boolean isMobile = false;
	String userAgent = request.getHeader("User-Agent"); //$NON-NLS-1$
	if (userAgent != null) {
		userAgent = userAgent.toUpperCase();
		isMobile = userAgent.contains("ANDROID") || userAgent.contains("WEBOS") || //$NON-NLS-1$ //$NON-NLS-2$
				userAgent.contains("IPHONE") || userAgent.contains("IPAD") || //$NON-NLS-1$ //$NON-NLS-2$
				userAgent.contains("IPOD") || userAgent.contains("BLACKBERRY") || //$NON-NLS-1$ //$NON-NLS-2$
				userAgent.contains("IEMOBILE") || userAgent.contains("OPERA MINI"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	boolean localAllowed = true;
	String op = request.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
	if (op != null && op.equals(ServiceParams.OPERATION_BATCH) && isMobile) {
		localAllowed = false;
	}

	// Preparamos el logo de la pantalla
	String logoUrl = ConfigManager.getPagesLogoUrl();
	if (logoUrl == null || logoUrl.isEmpty()) {
		logoUrl = "img/general/dms/logo-institucional.png"; //$NON-NLS-1$
	}

	final String cancelUrlParams = ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId + "&" + //$NON-NLS-1$ //$NON-NLS-2$ 
			ServiceParams.HTTP_PARAM_SUBJECT_ID + "=" + subjectId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
			(errorUrl != null ? ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl : ""); //$NON-NLS-1$ //$NON-NLS-2$
%>

<!DOCTYPE html>
<html xml:lang="es" lang="es" class="no-js">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

	<meta name="description" content="Selección de origen del certificado">
	<meta name="author" content="Ministerio de Hacienda y Función Pública">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Selección del mecanismo de firma</title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<link rel="stylesheet" type="text/css" href="css/modules.css">
	<link rel="stylesheet" type="text/css" href="css/personal.css">
<script type="text/javascript">
	alert(<%=errorUrl%>);
</script>
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
		<div class="container-title"  >
			<div class="title-head">
				<h1 class="title">Seleccione el sistema de firma</h1>
			</div>
			<div class="title-button">				
				<a href= "cancelOperationService?<%= cancelUrlParams %>" class="button-cancelar">
					<div>Cancelar</div>
				</a>
			</div>
		</div>
		
		<div class="container-box">	
		<%
		String[] providers = ProviderManager.getProviders();
		for (int i=1;i<=3;i++){//Borrar 
		for (String provider : providers) {
			ProviderInfo info = ProviderManager.getProviderInfo(provider);
			boolean enabled = true;
			if (ProviderManager.PROVIDER_NAME_LOCAL.equalsIgnoreCase(provider)) {
				enabled = localAllowed;
			} else {
				enabled = userRegistered && !info.isNeedJavaScript();
			}
		%>
			<div name="provider-option" class="main-box-left <%= info.isNeedJavaScript() ? "need-javascript" : "" %> <%= enabled ? "" : "disabled" %>" id="option<%= info.getName() %>">
					<div class="contain-box-top">
						<img alt="<%= info.getTitle() %>" title="<%= info.getTitle() %>" src="<%= info.getLogoUri() %>" >
					</div>
					<div class="contain-box-bottom">
						<h2 class="title-box"><%= info.getHeader() %></h2>
						<% if(info.isNeedJavaScript()) { %>
							<noscript>
								<p class="text-box">Su navegador web tiene JavaScript desactivado. Habilite JavaScript para poder usar sus certificados.</p>
							</noscript>
						<% } %>
						<p name="provider-description" class="text-box <%= info.isNeedJavaScript() ? "hide" : "" %>">
							<%= userRegistered ? info.getDescription() : info.getNoRegisteredMessage() %>													
						</p>
					</div>
					<form method="POST" action="chooseCertificateOriginService" id="form<%= info.getName() %>">
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_ID %>" value="<%= subjectId %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT_ORIGIN %>" value="<%= info.getName() %>" />
						<% if (unregistered != null) { %>
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED %>" value="<%= unregistered %>" />
						<% } %>
						<% if (op != null) { %>
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_OPERATION %>" value="<%= op %>" />
						<% } %>
						
						<a class="button" title="<%= info.getHeader() %>" onclick="document.getElementById('form<%= info.getName() %>').submit();" href="javascript:{}">
							<span class="button-text">Acceder</span>
							<span class="arrow-right arrow-right-inicio"></span>
						</a>
					</form>
				</div>
		<%
		}
		}//Borrar
		%>
<!-- 
				<div class="main-box-left <%= userRegistered ? "" : "disabled" %>" id="certRemote">
					<div class="contain-box-top">
						<img alt="Clave firma" title="Cl@ve Firma" src="img/general/dms/logo-clavefirma.png">
					</div>
					<div class="contain-box-bottom">
						<h2 class="title-box">Firma con Cl@ve Firma</h2>
						<% if (!userRegistered) { %>
							<p class="text-box">El usuario no esta dado de alta en Cl@ve, por lo que no podr&aacute; firmar con certificado en la nube.</p>
						<% } else { %>
							<p class="text-box">Utilice un certificado de firma de Cl@ve o realice su solicitud si no dispone de &eacute;l.</p>
						<% } %>
					</div>
					<form method="POST" action="chooseCertificateOriginService" id="formClaveFirma">
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_ID %>" value="<%= subjectId %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT_ORIGIN %>" value="<%= ServiceParams.CERTIFICATE_ORIGIN_CLAVEFIRMA %>" />
						<% if (unregistered != null) { %>
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED %>" value="<%= unregistered %>" />
						<% } %>
						<% if (op != null) { %>
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_OPERATION %>" value="<%= op %>" />
						<% } %>
						
						<a class="button" title="Firmar con Clave Firma" onclick="document.getElementById('formClaveFirma').submit();" href="javascript:{}">
							<span>Acceder</span>
							<span class="arrow-right arrow-right-inicio"></span>
						</a>
					</form>
				</div>
			
				<div class="main-box-right disabled" id="certLocal">
					<div class="contain-box-top">
						<img alt="AutoFirma" title="AutoFirma" src="img/general/dms/logo-autofirma.png">
					</div>
					<div class="contain-box-bottom">
						<h2 class="title-box title-box-autofirma">Firma con certificado local</h2>
						<noscript>
							<p class="text-box">Su navegador web tiene JavaScript desactivado. Habilite JavaScript para poder usar sus certificados.</p>
						</noscript>
						<p class="text-box hide" id="certLocalText">Utilice un certificado instalado en el almacén de claves de su navegador o alojado en tarjeta inteligente.</p>
						
					</div>
					<form method="POST" action="chooseCertificateOriginService" id="formLocal">
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_ID %>" value="<%= subjectId %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
						<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT_ORIGIN %>" value="<%= ServiceParams.CERTIFICATE_ORIGIN_LOCAL %>" />
						<% if (unregistered != null) { %>
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED %>" value="<%= unregistered %>" />
						<% } %>
						<% if (op != null) { %>
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_OPERATION %>" value="<%= op %>" />
						<% } %>
						 <a class="button" title="Firmar con certificado local" onclick="document.getElementById('formLocal').submit();" href="javascript:{}">
							<span>Acceder</span>
							<span class="arrow-right arrow-right-inicio"></span>
						</a>
					</form>
				</div>
 -->

			</div>
		</section>
	</main>
      
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
   	<script type="text/javascript">
   	
		// Si funciona JavaScript podremos retirar la clase que
		// desactiva la opcion de firma local y la que oculta su texto
		var disabledElementsProvDesc = document.getElementsByName("provider-description");
		for (var i = 0; i < disabledElementsProvDesc.length; i++) {		
			disabledElementsProvDesc[i].className= disabledElementsProvDesc[i].className.replace( /(?:^|\s)hide(?!\S)/g , '' );	
		}
		
		var disabledElementsProvOp = document.getElementsByName("provider-option");
		for (var i = 0; i < disabledElementsProvOp.length; i++) {
			disabledElementsProvOp[i].className=disabledElementsProvOp[i].className.replace( /(?:^|\s)need-javascript(?!\S)/g , '' )	
		}
		
		/*
   		document.getElementById("certLocal").className =
   		   document.getElementById("certLocal").className.replace( /(?:^|\s)disabled(?!\S)/g , '' )
   		document.getElementById("certLocalText").className =
   		   document.getElementById("certLocalText").className.replace( /(?:^|\s)hide(?!\S)/g , '' )
		*/
   		   
   	</script>
</body>
</html>