
<%@page import="es.gob.fire.server.services.internal.ErrorManager"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="es.gob.fire.server.services.Responser"%>
<%@page import="es.gob.fire.server.services.FIReError"%>
<%@page import="es.gob.afirma.core.signers.ExtraParamsProcessor.IncompatiblePolicyException"%>
<%@page import="es.gob.afirma.core.signers.ExtraParamsProcessor"%>
<%@page import="es.gob.fire.server.services.internal.PropertiesUtils"%>
<%@page import="es.gob.fire.server.services.internal.TransactionAuxParams"%>
<%@page import="es.gob.fire.server.services.statistics.TransactionType"%>
<%@page import="es.gob.fire.server.services.internal.PublicContext"%>
<%@page import="es.gob.fire.server.services.internal.FirePages"%>
<%@page import="es.gob.fire.server.services.ProjectConstants"%>
<%@page import="es.gob.afirma.core.misc.AOUtil"%>
<%@page import="java.nio.charset.StandardCharsets"%>
<%@page import="es.gob.fire.server.services.internal.TransactionConfig"%>
<%@page import="es.gob.fire.server.services.DocInfo"%>
<%@page import="es.gob.fire.server.services.internal.FireSession"%>
<%@page import="es.gob.fire.server.services.internal.SessionCollector"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
<%@page import="es.gob.fire.server.services.internal.SignBatchConfig"%>
<%@page import="es.gob.fire.server.services.internal.MiniAppletHelper"%>
<%@page import="es.gob.fire.server.services.internal.BatchResult"%>
<%@page import="es.gob.fire.server.services.internal.ServiceParams"%>
<%@page import="es.gob.fire.server.services.internal.ServiceNames"%>
<%@page import="es.gob.fire.server.services.FIReTriHelper"%>
<%@page import="es.gob.fire.signature.ConfigManager"%>
<%@page import="es.gob.afirma.core.misc.Base64"%>
<%@page import="java.util.Properties"%>
<%@page import="es.gob.fire.i18n.Language"%>
<%@page import="es.gob.fire.i18n.IWebViewMessages"%>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

	final String EXTRA_PARAM_EXP_POLICY = "expPolicy"; //$NON-NLS-1$
	
	// Recogemos de la peticion todos los parametros y con el identificador de transaccion
	// recuperamos la configuracion de la operacion y la referencia a los datos sobre los
	// que operar
	String subjectRef = request.getParameter(ServiceParams.HTTP_PARAM_SUBJECT_REF);
	String trId = request.getParameter(ServiceParams.HTTP_PARAM_TRANSACTION_ID);
	
	if (subjectRef == null || trId == null) {
		Responser.sendError(response, FIReError.FORBIDDEN);
		return;
	}
	
	TransactionAuxParams trAux = new TransactionAuxParams(null, trId);
	
	FireSession fireSession = SessionCollector.getFireSessionOfuscated(trId, subjectRef, session, true, false, trAux);
	if (fireSession == null) {
		Responser.sendError(response, FIReError.FORBIDDEN);
		return;
	}
	
	String language = fireSession.getString(ServiceParams.SESSION_PARAM_LANGUAGE);
	if (language == null || language.isEmpty()) {
		language = "es";
	}
	Language.changeFireSignatureMessagesConfiguration(new Locale(language));

	String appId = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_ID);
	trAux.setAppId(appId);
	
	// Obtenemos las URL a las que hay que redirigir al usuario en caso de exito y error
	final TransactionConfig connConfig =
		(TransactionConfig) fireSession.getObject(ServiceParams.SESSION_PARAM_CONNECTION_CONFIG);
	final String errorUrl = connConfig.getRedirectErrorUrl();
	
	// Referencia a los datos cargados (que no el documento a firmar)
	final String refB64 = Base64.encode(trId.getBytes(StandardCharsets.UTF_8));
	
	// Nombre de la aplicacion
	final String appName = fireSession.getString(ServiceParams.SESSION_PARAM_APPLICATION_TITLE);
	
	// Identificamos si estamos ante una firma de lote o una firma normal
	final TransactionType transactionType = (TransactionType) fireSession.getObject(ServiceParams.SESSION_PARAM_TRANSACTION_TYPE);
	boolean isBatchOperation = TransactionType.BATCH == transactionType;

	// Valores genericos
	final boolean stopOnError = Boolean.parseBoolean(fireSession.getString(ServiceParams.SESSION_PARAM_BATCH_STOP_ON_ERROR));
	String cop = fireSession.getString(ServiceParams.SESSION_PARAM_CRYPTO_OPERATION);
	String algorithm = fireSession.getString(ServiceParams.SESSION_PARAM_ALGORITHM);
	String format = fireSession.getString(ServiceParams.SESSION_PARAM_FORMAT);
	Properties extraParams = (Properties) fireSession.getObject(ServiceParams.SESSION_PARAM_EXTRA_PARAM);
	String upgrade = fireSession.getString(ServiceParams.SESSION_PARAM_UPGRADE);
	Properties upgradeConfig = (Properties) fireSession.getObject(ServiceParams.SESSION_PARAM_UPGRADE_CONFIG);
	
	
	// AutoFirma es estricto al validar los atributos de los extraParams, asi que los expandimos
	// nostros previamente y no cancelamos la operacion en caso de no poder expandir alguno 
	try {
		extraParams = ExtraParamsProcessor.expandProperties(
				extraParams,
				null,
				format
				);
	}
	catch (final IncompatiblePolicyException e) {
		// Eliminamos el parametro extra de politica si no fuese compatible
		extraParams.remove(EXTRA_PARAM_EXP_POLICY);
	}
	
	// Valores de la operacion de firma
	String triphaseFormat = null;
	
	// Valores en la operacion de lote
	String preSignBatchUrl = null;
	String postSignBatchUrl = null;
	String certFilters = null;
	
	// Para la carga de recursos y acceso a los servicios, obtenemos la URL publica
	String baseUrl = PublicContext.getPublicContext(request);
	
	// Obtenemos las propiedades extra de configuracion de las firmas
	BatchResult batchResult = null;
	SignBatchConfig defaultConfig = null;
	if (isBatchOperation) {
		defaultConfig = new SignBatchConfig();
		defaultConfig.setCryptoOperation(cop);
		defaultConfig.setFormat(format);
		defaultConfig.setExtraParams(extraParams);
		defaultConfig.setUpgrade(upgrade);
		defaultConfig.setUpgradeConfig(upgradeConfig);
		
		certFilters = fireSession.getString(ServiceParams.SESSION_PARAM_FILTERS);
		batchResult = (BatchResult) fireSession.getObject(ServiceParams.SESSION_PARAM_BATCH_RESULT);
		
		if (batchResult == null) {	//TODO: Habria que comprobar en el servicio este caso
			String errorMessage = "No se ha encontrado en la sesion el listado de documentos del lote"; //$NON-NLS-1$
			String errorCode = Integer.toString(FIReError.INTERNAL_ERROR.getCode());
			Logger.getLogger("es.gob.fire").severe(trAux.getLogFormatter().f( //$NON-NLS-1$
					"Error %s - " + errorMessage, errorCode)); //$NON-NLS-1$
  				ErrorManager.setErrorToSession(fireSession, FIReError.INTERNAL_ERROR, true, errorMessage, trAux);
			Responser.redirectToExternalUrl(errorUrl, request, response, trAux);
			return;
		}
		
		preSignBatchUrl = baseUrl + "afirma/" + ServiceNames.PUBLIC_SERVICE_AFIRMA_BATCH_PRESIGN; //$NON-NLS-1$
		postSignBatchUrl = baseUrl + "afirma/" + ServiceNames.PUBLIC_SERVICE_AFIRMA_BATCH_POSTSIGN; //$NON-NLS-1$
	}
	else {
		triphaseFormat = FIReTriHelper.getTriPhaseFormat(format);
		
		// Agregamos a los extraParams el parametro necesario para la generacion de una firma trifasica
		extraParams.setProperty("serverUrl", baseUrl + "afirma/" + ServiceNames.PUBLIC_SERVICE_AFIRMA_TRISIGN); //$NON-NLS-1$ //$NON-NLS-2$
	}

	final String formFunction = isBatchOperation ? "doSignBatch()" : "doSign()"; //$NON-NLS-1$ //$NON-NLS-2$
	
	// Obtenemos si el origen del certificado vino forzado por la aplicacion. Si es asi, se
	// mostrara un boton cancelar y, en caso contrario, un boton volver. Configuramos tambien
	// los parametros que necesitaran estos dos botones
	boolean originForced = Boolean.parseBoolean(
	fireSession.getString(ServiceParams.SESSION_PARAM_CERT_ORIGIN_FORCED));

	// Preparamos la informacion de los documentos a firmar
	DocInfo[] docInfos = null;
	if (isBatchOperation) {
		final List<DocInfo> docInfosList = new ArrayList<DocInfo>();
		Iterator<String> it = batchResult.iterator();
		while (it.hasNext()) {
			String docId = it.next();			
			DocInfo docInfo = batchResult.getDocInfo(docId);
			if (docInfo != null && (docInfo.getName() != null || docInfo.getTitle() != null)) {
				docInfosList.add(docInfo);
			}
		}
		docInfos = docInfosList.toArray(new DocInfo[docInfosList.size()]);
	} else {
		DocInfo docInfo = DocInfo.extractDocInfo(extraParams);
		if (docInfo != null && (docInfo.getName() != null || docInfo.getTitle() != null)) {
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
	<meta http-equiv="Cache-Control" content="no-cache, must-revalidate" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Expires" content="0" />
	<meta http-equiv="Content-Security-Policy" content="style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src *; connect-src 'self' afirma: 127.0.0.1">

	<meta name="description" content="<%= Language.getResFireSignature(IWebViewMessages.SIGN_WITH_LOCAL_CERT) %>">
	<meta name="author" content="Gobierno de España">
	<meta name="robots" content="noindex, nofollow">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title><%= Language.getResFireSignature(IWebViewMessages.SIGN_WITH_LOCAL_CERT) %></title>
	<link rel="shortcut icon" href="img/general/dms/favicon.png">
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<link rel="stylesheet" type="text/css" href="css/headerFooter.css">
	<link rel="stylesheet" type="text/css" href="css/modMiniApplet.css">
	<link rel="stylesheet" type="text/css" href="css/personal.css">
	<script type="text/javascript" src="js/autoscript.js"></script>
	
	<script type="text/javascript">
		
		<%
		if (isBatchOperation) {
			String extraParamsPlain = PropertiesUtils.properties2String(defaultConfig.getExtraParams()).replace("\n", "\\n"); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		
			function prepareBatch() {
				AutoScript.createBatch( "<%= algorithm %>", 
						"<%= defaultConfig.getFormat() %>", 
						"<%= defaultConfig.getCryptoOperation() %>", 
						"<%= extraParamsPlain %>"
						);
				
		<%		
				final Iterator<String> it = batchResult.iterator();
				while (it.hasNext()) {
					final String docId = it.next();
					final String dataReference = batchResult.getDocumentReference(docId);
					SignBatchConfig signConfig = batchResult.getSignConfig(docId);
					
					if (signConfig == null) {
						signConfig = defaultConfig;
					}
					
					final Properties singleExtraParams = signConfig.getExtraParams();
					if (signConfig.getUpgrade() != null && !signConfig.getUpgrade().isEmpty()) {
						singleExtraParams.setProperty("upgradeFormat", signConfig.getUpgrade()); //$NON-NLS-1$
					}
					
					String singleExtraParamsPlain = PropertiesUtils.properties2String(signConfig.getExtraParams()).replace("\n", "\\n");  //$NON-NLS-1$ //$NON-NLS-2$
					
		%>
					AutoScript.addDocumentToBatch("<%= docId %>",
													"<%= dataReference %>",
													"<%= signConfig.getFormat() %>",
													"<%= signConfig.getCryptoOperation() %>",
													"<%= singleExtraParamsPlain %>"
													);
		<% 
				}
		%>
			}
		
			function doSignBatch() {
				
				document.getElementById("errorMsg").style.display = "none";
				
				// Obtenemos datos
				var stopOnError = <%= stopOnError %>;
				var preSignUrl = "<%= preSignBatchUrl %>";
				var postSignUrl = "<%= postSignBatchUrl %>";
				var certFilters = "<%= certFilters %>";

				try {
					showProgress();
					console.log(preSignUrl);
					
					AutoScript.signBatchProcess(
						stopOnError,
						preSignUrl,
						postSignUrl,
						certFilters,
						sendBatchResultCallback,
						sendErrorCallback);
	
				} catch (e) {
					sendErrorCallback(AutoScript.getErrorType(), AutoScript.getErrorMessage());
					return;
				}
			}
			
			function sendBatchResultCallback(batchResultB64, certificateB64) {
				var encodedResult = Base64.encode(JSON.stringify((batchResultB64)));
				document.getElementById("afirmaBatchResult").value = encodedResult;
				if (certificateB64) {
					document.getElementById("cert").value = certificateB64.replace(/\+/g, "-").replace(/\//g, "_");
				}
				document.getElementById("formSign").submit();
			}
		
		<% } else { %>

			function doSign() {
				
				document.getElementById("errorMsg").style.display = "none";
				
				// Obtenemos datos
				var cop = "<%= cop %>";
				var refB64 = "<%= refB64 %>";
				var format = "<%= triphaseFormat %>";
				var algorithm = "<%= algorithm %>";
				var extraParams = "<%= PropertiesUtils.properties2String(extraParams).replace("\n", "\\n") %>";
	
				try {				
					showProgress();
					if (cop.toUpperCase() === "SIGN") {
						AutoScript.sign(
							refB64,
							algorithm,
							format,
							extraParams,
							sendResultCallback,
							sendErrorCallback);
					}
					else if (cop.toUpperCase() === "COSIGN") {
						AutoScript.coSign(
								refB64,
							null,
							algorithm,
							format,
							extraParams,
							sendResultCallback,
							sendErrorCallback);
					}
					else if (cop.toUpperCase() === "COUNTERSIGN") {
						AutoScript.counterSign(
							refB64,
							algorithm,
							format,
							extraParams,
							sendResultCallback,
							sendErrorCallback);
					}
					else {
						hideProgress();
						sendErrorCallback("java.lang.UnsupportedOperationException", "Operacion de firma no soportada");
						return;
					}
					
				} catch(e) {
					hideProgress();
					sendErrorCallback(AutoScript.getErrorType(), AutoScript.getErrorMessage());
					return;
				}
			}

			function sendResultCallback(signatureB64, certificateB64) {
				if (certificateB64) {
					document.getElementById("cert").value = certificateB64.replace(/\+/g, "-").replace(/\//g, "_");
				}
				document.getElementById("formSign").submit();
			}
			
		<% } %>

			function sendErrorCallback(errorType, errorMessage) {
				hideProgress();
			<% if (isBatchOperation) { %>
				prepareBatch();
			<% } %>	
				document.getElementById("inputerrortype").value = errorType;
				document.getElementById("inputerrormsg").value = errorMessage;								
				document.getElementById("errorMsg").innerHTML = "" + errorType + ": " + errorMessage;
								
				if (errorType != "es.gob.afirma.core.AOCancelledOperationException") {
					showErrorOptions();
				}
			}

			function showErrorOptions() {
				
				// Ocultamos el boton de firmar
				document.getElementById("signButtonsPanel").style.display = "none";
				
				// Mostramos los botones de accion y llevamos el foco hasta ellos
				document.getElementById("errorMsg").style.display = "block";
				document.getElementById("errorButtonsPanel").style.display = "block";
				document.getElementById("buttonRetry").focus();
			}

			/**
			 * Redirige a la pantalla de error despues de haberse producido un error y
			 * el usuario haya decidido abortar la operaci&oacuten.
			 */
			function doCancel() {
				hideProgress();
				document.getElementById("formSign").action = "<%= ServiceNames.PUBLIC_SERVICE_MINIAPPLET_ERROR %>";
				document.getElementById("formSign").submit()
			}
			
			/**
			 * Actualiza el texto de requisitos de usuario en base al tipo de cliente
			 * @firma que se encuentre cargado.
			 */
			function updateRequirementsText() {
				var app;
				var appVersion;
				var href;
				if (AutoScript.isAndroid()) {
					app = "Cliente @firma Android";
					appVersion = "<%= Language.getResFireSignature(IWebViewMessages.MSG_WARNING_ANDORID_VERSION) %>";
					href = "https://play.google.com/store/apps/details?id=es.gob.afirma";
				}
				else if (AutoScript.isIOS()) {
					app = "Cliente @firma iOS";
					appVersion = "<%= Language.getResFireSignature(IWebViewMessages.MSG_WARNING_IOS_VERSION) %>";
					href = "https://itunes.apple.com/es/app/cliente-firma-movil/id627410001?mt=8&uo=4";
				}
				else {
					app = "AutoFirma";
					appVersion = "<%= Language.getResFireSignature(IWebViewMessages.MSG_WARNING_AUTOFIRMA_VERSION) %>";
					href = "http://firmaelectronica.gob.es/Home/Descargas";
				}

				document.getElementById("signningApp").innerText = app;
				document.getElementById("signningAppVersion").innerText = appVersion;
				document.getElementById("linkDownload").href = href;
			}
			
			<% if (isBatchOperation) { %>
				prepareBatch();
			<% } %>
		</script>
		
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
		
		<section class="contenido contenido-firmar">
			
			<div  class="container-box-title">
					<div class="container_tit">
						<h1 class="title"><span class="bold"><%= Language.getResFireSignature(IWebViewMessages.SIGN_WITH_LOCAL_CERT) %></span></h1>
					</div>
					
				</div>

			<div class="contenido-opciones temp-hide" id="errorButtonsPanel">
				<div id="mensaje_error" class="mensaje-error" >
				<h2 id="errorMsg"><%= Language.getResFireSignature(IWebViewMessages.ERROR_SIGN_OPERATION) %></h2>
				</div>
				
				<div id="containerError" class="botones">
					<input id="buttonRetry" type="button" class="button-operacion" value="<%= Language.getResFireSignature(IWebViewMessages.RETRY_BTN) %>" onclick="<%= formFunction %>"/>&nbsp;
					<input id="buttonCancel" type="button" class="button-operacion" value="<%= Language.getResFireSignature(IWebViewMessages.CANCEL_BTN) %>" onclick="doCancel()"/>
				</div>
			</div>

			<div id="signButtonsPanel" Class="container-firmar "> 
				<form name="formSign" id="formSign" method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_MINIAPPLET_SUCCESS %>">
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT_ORIGIN %>" value="local" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_IS_BATCH_OPERATION %>" value="<%= isBatchOperation %>" />
					<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
					<input id="inputerrortype" type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_TYPE %>" />
					<input id="inputerrormsg" type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_MESSAGE %>" />
					<input id="afirmaBatchResult" type="hidden" name="<%= ServiceParams.HTTP_PARAM_AFIRMA_BATCH_RESULT %>" />
					<input id="cert" type="hidden" name="<%= ServiceParams.HTTP_PARAM_CERT %>" value="" />
					<input id="buttonSign" type="button" class="button_firmar" value="<%= Language.getResFireSignature(IWebViewMessages.SIGN_BTN) %>" onclick="<%= formFunction %>"/>
				</form>
			</div>		
			<div class="nota-firmar">
					<span class="bold"><%= Language.getResFireSignature(IWebViewMessages.MSG_WARNING) %>:</span> 
					<%= Language.getResFireSignature(IWebViewMessages.MSG_WARNING_SIGN_WITH) %> <span id="signningApp" class="bold">AutoFirma</span>. 
					<%= Language.getResFireSignature(IWebViewMessages.MSG_WARNING_VERSION) %> 
						<a id="linkDownload" href="#" target="_blanc"> <span id="signningAppVersion" class="bold">AutoFirma</span>.</a>		
			</div>
			

			<div id="progressDialog" class="progress-dialog">
				<div class="progress-dialog-window">
					<div class="progress-dialog-text">
						<span id="progressText"></span>
					</div>
					<div class="progress-dialog-img">
						<img class="img-loading" src="img/general/dms/cargando-loading.gif"/>
					</div>				
				</div>
			</div>
			
		</section>
		
		<% if (docInfos != null && docInfos.length > 0) { %>
		<section class="contenido-firmar-listadocs">
			<div class="titulo-listaDocs" >Documentos a Firmar</div>
			<div id="listDocs" class="container-box">
					<div class="cabecera-listaDocs">Id. Documento</div>		
					<div class="cabecera-listaDocs">Título</div>
				<% int i=1;
				for (DocInfo docInfo : docInfos)  { %>
					<div class="celda-listaDocs"><%= docInfo.getName() != null ? docInfo.getName() : "" %></div>	
					<div class="celda-listaDocs"><%= docInfo.getTitle() != null ? docInfo.getTitle() : "" %></div>	
					<%i++; 
				}%>				

			</div>
		</section>
		<% } %>
		<div class="container_btn_operation">
		<% if (originForced) { %>
			<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_CANCEL_OPERATION %>" id="formCancel">
				<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
				<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
				<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
			</form>
		
			<a class="button-cancelar" onclick="document.getElementById('formCancel').submit();" href="javascript:{}">
				<span ><%= Language.getResFireSignature(IWebViewMessages.CANCEL_BTN) %></span>
			</a>
		<% } else { %>
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
		<form method="POST" action="<%= ServiceNames.PUBLIC_SERVICE_CHANGE %>" id="changeLangForm">
			<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" name="<%= ServiceParams.HTTP_PARAM_SUBJECT_REF %>" value="<%= subjectRef %>" />
			<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" name="<%= ServiceParams.HTTP_PARAM_TRANSACTION_ID %>" value="<%= trId %>" />
			<input type="hidden" id="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" name="<%= ServiceParams.HTTP_PARAM_ERROR_URL %>" value="<%= errorUrl %>" />
			<input type="hidden" id="languageConf" name="<%= ServiceParams.HTTP_PARAM_LANGUAGE %>" value="<%= language %>" />
			<input type="hidden" name="<%= ServiceParams.HTTP_PARAM_PAGE %>" value="<%= FirePages.PG_CLIENTE_AFIRMA %>" />
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

		SupportDialog.enableSupportDialog(false);

		AutoScript.setForceWSMode(true);
		AutoScript.setServlets("<%= baseUrl + "afirma/" + ServiceNames.PUBLIC_SERVICE_AFIRMA_STORAGE %>", "<%= baseUrl  + "afirma/" + ServiceNames.PUBLIC_SERVICE_AFIRMA_RETRIEVE %>");
		AutoScript.cargarAppAfirma("<%= baseUrl %>afirma");
		
		// Actualizamos el texto de requisitos
		updateRequirementsText();
		
		/** Muestra y actualiza el dialogo de progreso. */
		function showProgress() {
			document.getElementById("progressText").innerHTML = "<%= Language.getResFireSignature(IWebViewMessages.EXECUTING_SIGN) %>"; 
			document.getElementById("progressDialog").style.display = "block";
		}

		/** Oculta el dialogo de progreso. */
		function hideProgress() {
			document.getElementById("progressDialog").style.display = "none";
		}
		
		function changeLanguage() {
			var languageSelected = document.getElementById('languageSelect').value;	
			document.getElementById('languageConf').value = languageSelected;
		    document.getElementById('changeLangForm').submit();
		}
	</script>

</body>
</html>