
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="es.gob.fire.server.services.internal.SignBatchConfig"%>
<%@page import="es.gob.fire.server.services.internal.MiniAppletHelper"%>
<%@page import="es.gob.fire.server.services.internal.BatchResult"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>
<%@page import="es.gob.fire.server.services.FIReServiceOperation"%>
<%@page import="es.gob.fire.server.services.FIReTriHelper"%>
<%@page import="es.gob.fire.server.services.ServiceUtil"%>
<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="es.gob.fire.signature.DocInfo"%>
<%@page import="java.util.Map"%>
<%@page import="es.gob.afirma.core.misc.Base64"%>
<%@page import="java.util.Properties"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	// Recogemos de la peticion todos los parametros y con el identificador de transaccion
	// recuperamos la configuracion de la operacion y la referencia a los datos sobre los
	// que operar
	String userId = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_ID);
	String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
	String unregistered = request.getParameter(ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED);
	String op = request.getParameter(ServiceParams.HTTP_PARAM_OPERATION);
	
	FireSession fireSession = SessionCollector.getFireSession(trId, userId, session, true);
	if (fireSession == null) {
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
		return;
	}
	
	// Referencia a los datos cargados (que no el documento a firmar)
	final String refB64 = Base64.encode(trId.getBytes());
	
	// Nombre de la aplicacion
	final String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_NAME);
	
	// Identificamos si estamos ante una firma de lote o una firma normal
	final String operation = fireSession.getString(ServiceParams.SESSION_PARAM_OPERATION);
	boolean isBatchOperation = FIReServiceOperation.CREATE_BATCH.getId().equals(operation);

	// Valores genericos
	String cop = fireSession.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION);
	String algorithm = fireSession.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
	String format = fireSession.getString(ServiceParams.SESSION_PARAM_FORMAT);
	String extraParamsB64 = fireSession.getString(ServiceParams.SESSION_PARAM_EXTRA_PARAM);
	String upgrade = fireSession.getString(ServiceParams.SESSION_PARAM_UPGRADE);
	
	// Valores de la operacion de firma
	String triphaseFormat = null;
	final StringBuilder extraParams = new StringBuilder();
	
	// Valores en la operacion de lote
	String preSignBatchUrl = null;
	String postSignBatchUrl = null;
	String batchXmlB64 = null;

	// Obtenemos la URL de la pagina para obtener la URL base a partir de la cual
	// acceder a varios servicios y recursos
	String baseUrl = request.getRequestURL().toString();
	if (baseUrl != null) {
		baseUrl = baseUrl.substring(0, baseUrl.toString().lastIndexOf('/') + 1);
	}
	BatchResult batchResult = null;
	if (isBatchOperation) {
		final SignBatchConfig defaultConfig = new SignBatchConfig();
		defaultConfig.setCryptoOperation(cop);
		defaultConfig.setFormat(format);
		defaultConfig.setExtraParamsB64(extraParamsB64);
		defaultConfig.setUpgrade(upgrade);
		
		batchResult = (BatchResult) fireSession.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
		final String stopOnError = fireSession.getString(ServiceParams.SESSION_PARAM_BATCH_STOP_ON_ERROR);
		
		preSignBatchUrl = baseUrl + "afirma/preSignBatchService"; //$NON-NLS-1$
		postSignBatchUrl = baseUrl + "afirma/postSignBatchService"; //$NON-NLS-1$
		batchXmlB64 = MiniAppletHelper
		.createBatchXml(Boolean.parseBoolean(stopOnError), algorithm, defaultConfig, batchResult);
	}
	else {
		triphaseFormat = FIReTriHelper.getTriPhaseFormat(format);
		
		// Obtenemos las propiedades de configuracion de la firma y le agregamos
		// los parametros necesarios para la generacion de una firma trifasica.
		final Properties extraParamsProperties = ServiceUtil.base642Properties(extraParamsB64);
		extraParamsProperties.setProperty("serverUrl", baseUrl + "afirma/triphaseSignService"); //$NON-NLS-1$ //$NON-NLS-2$
		
		for (String k : extraParamsProperties.keySet().toArray(new String[extraParamsProperties.size()])) {
	extraParams.append(k).append("="). //$NON-NLS-1$
			append(extraParamsProperties.getProperty(k)).append("\\n"); //$NON-NLS-1$
		}
	}


	// Obtenemos las URL a las que hay que redirigir al usuario en caso de exito y error
	final Properties resultRefs = (Properties) fireSession.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
	final String successUrl = resultRefs.getProperty(ServiceParams.CONNECTION_PARAM_SUCCESS_URL);
	final String errorUrl = resultRefs.getProperty(ServiceParams.CONNECTION_PARAM_ERROR_URL);
	
	final String formFunction = isBatchOperation ? "doSignBatch()" : "doSign()"; //$NON-NLS-1$ //$NON-NLS-2$
	
	// Obtenemos si el origen del certificado vino forzado por la aplicacion. Si es asi, se
	// mostrara un boton cancelar y, en caso contrario, un boton volver. Configuramos tambien
	// los parametros que necesitaran estos dos botones
	boolean originForced = Boolean.parseBoolean(
			fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED));

	// Parametros para el enlace del boton Volver (solo si se selecciono la operacion desde la pagina anterior)
	String buttonBackUrlParams = null;
	if (!originForced) {
		buttonBackUrlParams = ServiceParams.HTTP_PARAM_SUBJECT_ID + "=" + userId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
				ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId; //$NON-NLS-1$
		if (unregistered != null) {
			buttonBackUrlParams += "&" + ServiceParams.HTTP_PARAM_USER_NOT_REGISTERED + "=" + unregistered; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (op != null) {
			buttonBackUrlParams += "&" + ServiceParams.HTTP_PARAM_OPERATION + "=" + op; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (errorUrl != null) {
			buttonBackUrlParams += "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	// Parametros para el enlace del boton Cancelar
	String buttonCancelUrlParams = ServiceParams.HTTP_PARAM_SUBJECT_ID + "=" + userId + "&" + //$NON-NLS-1$ //$NON-NLS-2$
			ServiceParams.HTTP_PARAM_TRANSACTION_ID + "=" + trId; //$NON-NLS-1$
	if (errorUrl != null) {
		buttonCancelUrlParams += "&" + ServiceParams.HTTP_PARAM_ERROR_URL + "=" + errorUrl; //$NON-NLS-1$ //$NON-NLS-2$
	}

	// Preparamos la informacion de los documentos a firmar
	DocInfo[] docInfos = null;
	if (isBatchOperation) {
		if (batchResult != null) {
			final List<DocInfo> docInfosList = new ArrayList<DocInfo>();
			Iterator<String> it = batchResult.iterator();
			while (it.hasNext()) {
				String docId = it.next();
				DocInfo docInfo = batchResult.getDocInfo(docId);
				if (docInfo != null) {
					docInfosList.add(docInfo);
				}
			}
			docInfos = docInfosList.toArray(new DocInfo[docInfosList.size()]);
		}
	} else {
		final Properties extraParamsProperties = ServiceUtil.base642Properties(extraParamsB64);
		DocInfo docInfo = DocInfo.extractDocInfo(extraParamsProperties);
		if (docInfo.getName() != null || docInfo.getTitle() != null) {
			docInfos = new DocInfo[] { docInfo };
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

	<meta name="description" content="Firma con certificado local">
	<meta name="author" content="Ministerio de Hacienda y Función Pública">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Firma con certificado local</title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<link rel="stylesheet" type="text/css" href="css/modules.css">
	<link rel="stylesheet" type="text/css" href="css/personal.css">
	<script type="text/javascript" src="js/miniapplet.js"></script>
	<script type="text/javascript" src="js/deployJava.js"></script>
	<script type="text/javascript">
		
		<% if (isBatchOperation) { %>
		
			function doSignBatch() {

				document.getElementById("errorMsg").style.display = "none";
				
				// Obtenemos datos
				var batchXmlB64 = "<%= batchXmlB64 %>";
				var preSignUrl = "<%= preSignBatchUrl %>";
				var postSignUrl = "<%= postSignBatchUrl %>";
	
				try {
					MiniApplet.signBatch(
						batchXmlB64,
						preSignUrl,
						postSignUrl,
						null, // ExtraParams para establecer filtro de certificados
						sendBatchResultCallback,
						sendErrorCallback);
	
				} catch (e) {
					sendErrorCallback(MiniApplet.getErrorType(), MiniApplet
							.getErrorMessage());
				}
			}
		
		<% } else { %>

			function doSign() {
	
				document.getElementById("errorMsg").style.display = "none";
				
				// Obtenemos datos
				var cop = "<%= cop %>";
				var refB64 = "<%= refB64 %>";
				var format = "<%= triphaseFormat %>";
				var algorithm = "<%= algorithm %>";
				var extraParamsB64 = "<%= extraParams.toString() %>";
	
				try {
					if (cop.toUpperCase() === "SIGN") {
						MiniApplet.sign(
							refB64,
							algorithm,
							format,
							extraParamsB64,
							sendResultCallback,
							sendErrorCallback);
					}
					else if (cop.toUpperCase() === "COSIGN") {
						MiniApplet.coSign(
								refB64,
							null,
							algorithm,
							format,
							extraParamsB64,
							sendResultCallback,
							sendErrorCallback);
					}
					else if (cop.toUpperCase() === "COUNTERSIGN") {
						MiniApplet.counterSign(
							refB64,
							algorithm,
							format,
							extraParamsB64,
							sendResultCallback,
							sendErrorCallback);
					}
					else {
						sendErrorCallback("java.lang.UnsupportedOperationException", "Operacion de firma no soportada");
					}
				} catch(e) {
					sendErrorCallback(MiniApplet.getErrorType(), MiniApplet.getErrorMessage());
				}
			}

		<% } %>
			function sendResultCallback(signatureB64, certificateB64) {
				document.getElementById("cert").value = certificateB64.replace(/\+/g, "-").replace(/\//g, "_");
				document.getElementById("formSign").submit();
			}

			function sendBatchResultCallback(batchResultB64) {
				document.getElementById("afirmaBatchResult").value = batchResultB64;
				document.getElementById("formSign").submit();
			}

			function sendErrorCallback(errorType, errorMessage) {
				document.getElementById("errortype").value = errorType;
				document.getElementById("errormsg").value = errorMessage;
				
				if (errorType != "es.gob.afirma.core.AOCancelledOperationException") {
					showErrorOptions();
				}
			}

			function showErrorOptions() {
				
				// Ocultamos el boton de firmar
				document.getElementById("buttonSign").style.display = "none";
				
				// Mostramos los botones de accion y llevamos el foco hasta ellos
				document.getElementById("errorMsg").style.display = "block";
				document.getElementById("errorButtonsPanel").style.display = "block";
				document.getElementById("buttonSign2").focus();
			}
			
			/**
			 * Redirige a la pantalla de error despues de haberse producido un error y
			 * el usuario haya decidido abortar la operaci&oacuten.
			 */
			function doCancel() {
				
				document.getElementById("formSign").action = "miniappletErrorService";
				document.getElementById("formSign").submit()
			}
			
			/**
			 * Actualiza el texto de requisitos de usuario en base al tipo de cliente
			 * @firma que se encuentre cargado.
			 */
			function updateRequirementsText() {
				var app;
				var appVersion;
				if (MiniApplet.echo() === "Cliente JavaScript") {
					if (<%= ConfigManager.getClienteAfirmaForceNative() %>) {
						app = "AutoFirma";
						appVersion = "AutoFirma 1.5 o superior";
					}
					else {
						app = "AutoFirma WebStart";
						appVersion = "Java 8 o superior";
					}
				}
				else {
					app = "MiniApplet @firma";
					appVersion = "Java 7 o superior";
				}
				document.getElementById("signningApp").innerText = app;
				document.getElementById("signningAppVersion").innerText = appVersion;
			}
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
		<% if (originForced) { %>
			<a href= "cancelOperationService?<%= buttonCancelUrlParams %>" class="button-cancelar">
				<span >Cancelar</span>
			</a>
		<% } else { %>
			<a href= "ChooseCertificateOrigin.jsp?<%= buttonBackUrlParams %>" class="button-volver">
				<span class="arrow-left-white"></span>
				<span >volver</span>
			</a>
		<% } %>

		<section class="contenido contenido-firmar">
			<h1 class="title"><span class="bold">Firma con certificado local</span></h1>
			
			<div class="contenido contenido-opciones temp-hide" id="errorButtonsPanel">
				<h2 class="mensaje-error" id="errorMsg">Ocurri&oacute; un error en la operaci&oacute;n de firma</h2>
				<div class="botones">
					<input id="buttonSign2" type="button" class="button-operacion" value="Reintentar" onclick="<%= formFunction %>"/>
					<input id="buttonCancel" type="button" class="button-operacion" value="Cancelar" onclick="doCancel()"/>
				</div>
			</div>

			<div class="container-box no-float">
			<% if (docInfos != null && docInfos.length > 0) { %>
				<ul class="lista-elem">
				<li class="elem bold">Nombre - Título</li>
				<% for (DocInfo docInfo : docInfos)  { %>
					<li class="elem">
					<%= docInfo.getName() != null ? docInfo.getName() : "" %>
					<%= docInfo.getName() != null && docInfo.getTitle() != null ? " - " : "" %>
					<%= docInfo.getTitle() != null ? docInfo.getTitle() : "" %>
					</li>
				<% } %>
				</ul>
			<% } %>
			</div>
			
			<div Class="container-firmar"> 
				<form name="formSign" id="formSign" method="POST" action="miniappletSuccessService">
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT_ORIGIN %>" value="local" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_ID %>" value="<%= userId %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_IS_BATCH_OPERATION %>" value="<%= isBatchOperation %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
					<input id="errortype" type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_TYPE %>" />
					<input id="errormsg" type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_MESSAGE %>" />
					<input id="afirmaBatchResult" type="hidden" name="<%= ServiceParams.HTTP_PARAM_AFIRMA_BATCH_RESULT %>" />
					<input id="cert" type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT %>" value="" />
					<input id="buttonSign" type="button" class="button_firmar" value="Firmar" onclick="<%= formFunction %>"/>
				</form>
			</div>
			<div><span class="bold">Nota:</span> La firma se va a realizar con el <span id="signningApp" class="bold">MiniApplet @firma</span>. Aseg&uacute;rese de tener instalado <span id="signningAppVersion" class="bold">Java 7 o superior</span>.</div>
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

	<script type="text/javascript">
	
		// En caso de que podemos comprobar si el navegador tiene Java, evitamos configurar
		// la carga JNLP si no lo tiene
		if (deployJava.versionCheck("1.8+")) {
			MiniApplet.setJnlpService("<%= baseUrl %>afirma/afirmaJnlpService");
			MiniApplet.setForceAFirma(<%= ConfigManager.getClienteAfirmaForceNative() %>);
		}

		MiniApplet.setForceWSMode(true);
		<% if (ConfigManager.getClienteAfirmaForceAutoFirma()) { %>
			MiniApplet.cargarAppAfirma("<%= baseUrl %>afirma");
		<% } else { %>
			MiniApplet.cargarMiniApplet("<%= baseUrl %>afirma");
		<% } %>
		MiniApplet.setServlets("<%= baseUrl %>afirma/storage", "<%= baseUrl %>afirma/retrieve");

		// Actualizamos el texto de requisitos
		updateRequirementsText();

	</script>

</body>
</html>