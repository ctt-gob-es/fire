
<%@page import="es.gob.fire.server.services.internal.FirePages"%>
<%@page import="es.gob.fire.server.services.ProjectConstants"%>
<%@page import="es.gob.fire.server.services.internal.TransactionConfig"%>
<%@page import="es.gob.fire.server.services.internal.SessionFlags"%>
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Properties"%>
<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>
<%@page import="es.gob.fire.server.services.internal.ServiceNames"%>
<%@page import="java.util.Map"%>
<%@page import="es.gob.afirma.core.misc.AOUtil"%>
<%@page import="es.gob.afirma.core.misc.Base64"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.security.cert.X509Certificate"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
	String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
	String op = request.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
	
	if (subjectRef == null || trId == null) {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		return;
	}
	
	// Cargamos la sesion que deberia estar en memoria, pero permitimos su carga de otras fuentes
	FireSession fireSession = SessionCollector.getFireSessionOfuscated(trId, subjectRef, session, true, false);
	if (fireSession == null) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
		return;
	}
	
	// Si la operacion anterior no fue de solicitud de firma, forzamos a que se recargue por si faltan datos
	if (SessionFlags.OP_SIGN != fireSession.getObject(ServiceParams.SESSION_PARAM_PREVIOUS_OPERATION)) {
		fireSession = SessionCollector.getFireSessionOfuscated(trId, subjectRef, session, false, true);
	}
	
	// Nombre de la aplicacion
	String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_TITLE);
	
	String errorUrl = null;
	TransactionConfig connConfig = (TransactionConfig) fireSession.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
	if (connConfig != null && connConfig.isDefinedRedirectErrorUrl()) {
		errorUrl = connConfig.getRedirectErrorUrl();
		if (errorUrl != null) {
			errorUrl = URLEncoder.encode(errorUrl, "utf-8"); //$NON-NLS-1$
		}
	}

	// Preparamos el boton para cancelar o volver a atras. Se define el comportamiento
	// de este boton en base a si se forzo el origen (se muestra el boton Cancelar) o
	// no (boton Volver) 
	boolean originForced = Boolean
			.parseBoolean(fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED));

	String buttonUrlParams = ServiceParams.HTTP_PARAM_SUBJECT_REF + "=" + subjectRef + "&" + //$NON-NLS-1$ //$NON-NLS-2$
			ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId; //$NON-NLS-1$
	if (originForced) {
		if (errorUrl != null) {
			buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		if (op != null) {
			buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_OPERATION + "=" + op; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (errorUrl != null) {
			buttonUrlParams += "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	// Extraemos de la sesion los certificados y los eliminamos de la misma
	final X509Certificate[] certificates = (X509Certificate[]) fireSession.getObject(trId + "-certs"); //$NON-NLS-1$
	fireSession.removeAttribute(trId + "-certs"); //$NON-NLS-1$

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

	<meta name="description" content="Selección del certificado">
	<meta name="author" content="Gobierno de España">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Selección de certificado de firma</title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<link rel="stylesheet" type="text/css" href="css/headerFooter.css">
	<link rel="stylesheet" type="text/css" href="css/modChooseCertificate.css">
	<link rel="stylesheet" type="text/css" href="css/personal.css">

</head>
<body>
	<!-- Barra de navegacion -->
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
					
				</div>

		
			
			<div class="container-box-cert">
			
			<%  if (certificates != null) {
					SimpleDateFormat sf =  new SimpleDateFormat("dd-MM-yyyy"); //$NON-NLS-1$
					for (int i = 0; i < certificates.length; i ++) { 
						String cert = Base64.encode(certificates[i].getEncoded(), true); 
						X509Certificate certificate = certificates[i];
						String subject = AOUtil.getCN(certificate.getSubjectX500Principal().toString());
						String issuer = AOUtil.getCN(certificate.getIssuerX500Principal().toString());
						String date = sf.format(certificate.getNotAfter());
						%>
			
				<div class="cert-box">
					<div class="cert-box-int">
						<div class="cert-box-left">
							<img alt="Cl@ve" title="Cl@ve" src="img/general/dms/img-certificado-clave-firma.png">
						</div>
						<div class="cert-box-center">
							<h2 class="title-cert-box"><%= subject %></h2>
							<p class="text-cert-box">Emitido por <%= issuer %></p>
							<p class="text-cert-box">Fecha de caducidad: <%= date %></p>
						</div>
						<div class="cert-box-right">
							<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_PRESIGN %>" id="certForm<%= i %>">
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
							<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
							<input  type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT %>" value="<%= cert %>">
							<a class="button" title="Firmar con el certificado de <%= subject %>" onclick="document.getElementById('certForm<%= i %>').submit()" href="javascript:{}">
								<span >seleccionar</span>
								<span class="arrow-right"></span>
							</a>
							
							</form>
						</div>
					</div>
				</div>
			<%
				}
			}
			%>
			</div>
			
			<div class="container_btn_operation">
				<% if (originForced) { %>
					<a href="<%= ServiceNames.PUBLIC_SERVICE_CANCEL_OPERATION + "?" + buttonUrlParams %>" class="button-cancelar">
					<span >Cancelar</span>
					</a>
				<% } else { %>
					<a href= "<%= FirePages.PG_CHOOSE_CERTIFICATE_ORIGIN + "?" + buttonUrlParams %>" class="button-volver">
						<span class="arrow-left-white"></span>
						<span >Volver</span>
					</a>
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
			  <p class="footer-text">FIRe v<%= ProjectConstants.VERSION %> &copy; <%= ProjectConstants.COPY_YEAR %> Gobierno de Espa&ntilde;a</p> 
			</div>
		</div>
	</footer>
</body>
</html>
