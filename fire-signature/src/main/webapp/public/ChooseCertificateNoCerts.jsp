
<%@page import="es.gob.fire.server.services.internal.TransactionConfig"%>
<%@page import="es.gob.fire.server.services.internal.SessionFlags"%>
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Properties"%>
<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>
<%@page import="java.util.Map"%>


<%
	final String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
	final String userId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
	
	FireSession fireSession = SessionCollector.getFireSession(trId, userId, session, false, false);
	if (fireSession == null) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
		return;
	}

	// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
	if (SessionFlags.OP_SIGN != fireSession.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
		fireSession = SessionCollector.getFireSession(trId, userId, session, false, true);
	}

	// Leemos los valores necesarios de la configuracion
	final String unregistered = request.getParameter(ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED);
	final String op = request.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
	final String appId = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
	final String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);
	final boolean originForced = Boolean.parseBoolean(
			fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED));
	
	String errorUrl = null;
	TransactionConfig connConfig =
			(TransactionConfig) fireSession.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
	if (connConfig != null && connConfig.isDefinedRedirectErrorUrl()) {
		errorUrl = connConfig.getRedirectErrorUrl();
		if (errorUrl != null) {
			errorUrl = URLEncoder.encode(errorUrl, "utf-8"); //$NON-NLS-1$
		}
	}

	// Se define el comportamiento del boton en base a si se forzo el origen
	// (se muestra el boton Cancelar) o no (boton Volver) 
	String buttonUrlParams = ServiceParams.HTTP_PARAM_SUBJECT_ID + "=" + userId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
			ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId; //$NON-NLS-1$
	if (originForced) {
		if (errorUrl != null) {
			buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	else {
		if (unregistered != null) {
			buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED + "=" + unregistered; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (op != null) {
			buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_OPERATION + "=" + op; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (errorUrl != null) {
			buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

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

	<meta name="description" content="El usuario no tiene certificados de Clave Firma">
	<meta name="author" content="Ministerio de Hacienda y Función Pública">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>No dispone de certificado de firma</title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<link rel="stylesheet" type="text/css" href="css/modChooseCertificateNoCerts.css">
<!-- 	<link rel="stylesheet" type="text/css" href="css/modules.css"> -->
	<link rel="stylesheet" type="text/css" href="css/personal.css">

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
		

			<section class="contenido contenido-error">
			
				<div  class="container-box-title">
					<div class="container_tit">
						<h1 class="title"><span class="bold">No tiene certificados en Cl@ve Firma</span></h1>
					</div>
					<div class="container_btn_operation">
					<% if (originForced) { %>
						<a href= "cancelOperationService?<%= buttonUrlParams %>" class="button-cancelar">
							<span >Cancelar</span>
						</a>
					<% } else { %>
						<a href= "ChooseCertificateOrigin.jsp?<%= buttonUrlParams %>" class="button-volver">
							<span class="arrow-left-white"></span>
							<span >Volver</span>
						</a>
					<% } %>
					</div>
				</div>
				<div class="container-box-error">
					<div class="container-textbox-error">
						<p class="text-error-box">Si lo desea puede emitir un nuevo certificado en la nube para firmar</p>
					</div>
					<div class="error-box">
						<form method="POST" action="requestCertificateService">
		  					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
		  					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_ID %>" value="<%= userId %>">
			  				<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_APPLICATION_ID %>" value="<%= appId %>">
							<input type="submit" class="button_firmar" value="Emitir certificado en Cl@ve Firma" />
						</form>
					</div>
									
				
				<% if (!originForced) { %>
			  		
					<div id="certLocalText" class="container-textbox-error hide">					
						<p class="text-error-box">Tambien puede firmar usando sus certificados locales (incluyendo DNIe).</p>
					</div>								  				
					<div  id="certLocalcontainer" class="error-box hide">
						<form id="certLocal" action="chooseCertificateOriginService" class="hide">
					  		<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
					  		<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_ID %>" value="<%= userId %>">
					  		<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT_ORIGIN %>" value="local" />
							<input type="submit" class="button_firmar" value="Usar certificado local" />
						</form>
					</div>
									
				<% } %>
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
				  <p class="footer-text">&copy; 2018 Gobierno de Espa&ntilde;a - FIRe</p> 
				</div>
			</div>
		</footer>
		
		<script type="text/javascript">
			// Si tenemos JavaScript podremos mostrar la opcion de certificado local
	   		document.getElementById("certLocal").className = document.getElementById("certLocal").className.replace( /(?:^|\s)hide(?!\S)/g , '' );
	   		document.getElementById("certLocalText").className = document.getElementById("certLocalText").className.replace( /(?:^|\s)hide(?!\S)/g , '' );
	   		document.getElementById("certLocalcontainer").className = document.getElementById("certLocalcontainer").className.replace( /(?:^|\s)hide(?!\S)/g , '' );
		</script>
	</body>
</html>