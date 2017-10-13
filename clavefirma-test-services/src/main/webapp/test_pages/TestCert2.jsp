<!DOCTYPE html>
<!-- saved from url=(0096)https://clave-dninbpcert.policia.gob.es/raDninbUserConsole/web/content/console.html#/issue-dninb -->
<%@page import="java.net.URLDecoder"%>
<%@page import="es.gob.afirma.core.misc.Base64"%>
<html xmlns:ng="http://angularjs.org" xmlns:my="ignored" id="ng-app" lang="es" ng-app="raApp" class="no-js ng-scope"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><style type="text/css">@charset "UTF-8";[ng\:cloak],[ng-cloak],[data-ng-cloak],[x-ng-cloak],.ng-cloak,.x-ng-cloak,.ng-hide{display:none !important;}ng\:form{display:block;}.ng-animate-block-transitions{transition:0s all!important;-webkit-transition:0s all!important;}</style>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="RA">
        <meta name="author" content="SIA">
        <link rel="shortcut icon" href="./TestCert2_files/favicon.png">

        <title>Gesti&oacute;n de Contrase&ntilde;a Cl@ve</title>
        <style type="text/css" media="screen">
          [ng\:cloak], [ng-cloak], [data-ng-cloak], [x-ng-cloak], .ng-cloak, .x-ng-cloak {
            display: none;
          }
        </style>
        <!-- Bootstrap core CSS -->
        <link href="./TestCert2_files/bootstrap.css" rel="stylesheet">
        <!-- Personalize CSS -->
        <link href="./TestCert2_files/main.css" rel="stylesheet">
        <link href="./TestCert2_files/animations.css" rel="stylesheet">
        <!-- Other components -->
        
        <link href="./TestCert2_files/angular-busy.css" rel="stylesheet">

        <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
        <!--[if lt IE 9]>
          <script src="../js/vendor/html5shiv.js"></script>
          <script src="../js/vendor/respond.min.js"></script>
        <![endif]-->

        <!--[if lte IE 8]>
        <script>
          document.createElement('ng-include');
          document.createElement('ng-pluralize');
          document.createElement('ng-view');
          document.createElement('ui-view');
          document.createElement('ng-hide');
          document.createElement('ng-init');
          document.createElement('ng-model');
          document.createElement('ng-show');
          document.createElement('cert-password');
          document.createElement('deuce-inputs');
          document.createElement('validate-old-password');
          document.createElement('bloqMayusAlert');
          document.createElement('cg-busy');
          document.createElement('ng-class');
          document.createElement('ng-init');
          

          // Optionally these for CSS
          document.createElement('ng:include');
          document.createElement('ng:pluralize');
          document.createElement('ng:view');
        </script>
      <![endif]-->
      <!-- <style id="antiClickjack">body{display:none !important;}</style> -->
      
      <script>
      
      	function submitForm() {
      		document.getElementById('formulario').submit();
      	}
      
		function cancel() {
			var formulario = document.getElementById("formulario");
			formulario.action = decodeURIComponent("<%= request.getParameter("redirectko") %>");
			formulario.submit();
		}
      </script>
    </head>
    <body style="">
        <!-- Cabecera y menu -->
        <!-- <header class="navbar header-navbar navbar-static-top" role="banner">
            <div class="arlogo"></div>
            <div class="artitulos">
                <h1>Gesti&oacute;n de Contrase&ntilde;a Cl@ve</h1>
                <h2>Registro de contrase&ntilde;a. Verificaci&oacute;n del c&oacute;digo de activaci&oacute;n</h2>
            </div>
        </header>
        <div class="sub-header">
            <div class="line"></div>
            <div class="line"></div>
        </div> -->

        <div class="container">
            <div class="image-sub-header">
                <img class="img-responsive" id="banner" src="./TestCert2_files/imagenCorporativa.png">
            </div>
            <!-- Contenido -->
            <div ng-controller="InitCtrl" class="ng-scope">
                <div id="alertsIeContainer"></div>
                <!-- uiView:  --><div cg-busy="{promise:myPromise, backdrop: true, message:&#39;Espere por favor...&#39;}" class="view-container ng-scope" ui-view=""><div ng-controller="IssueDninbCtrl" cg-busy="{promise:sendingForm, backdrop: true, message:&#39;Espere por favor...&#39;}" class="ng-scope" style="position: relative;">
	<h3 class="ng-binding">Emisi&oacute;n de tu certificado de firma centralizado (P&Aacute;GINA DE PRUEBA)</h3>
	<!-- informacion del paso actual -->
	<fieldset class="form-container">
		<legend class="ng-binding">&#33;Informaci&oacute;n!</legend>
		<div id="error-container" class="error-msg" style="display: none">
			<div id="error-msg"></div>
		</div>
		<!-- ngIf: !isPasswordValid && (sessionData.isPinExpired || sessionData.operationData.isPreloaded) --><div ng-if="!isPasswordValid &amp;&amp; (sessionData.isPinExpired || sessionData.operationData.isPreloaded)" ng-bind-html="&#39;GENERAL.CHANGE_PASSWORD_INFO&#39; | translate" class="ng-scope ng-binding"><p>Por motivos de seguridad, es necesario que modifiques tu contrase&ntilde;a Cl@ve actual para almacenarla de forma segura junto a tu certificado de firma centralizado. Recuerda que esta ser&aacute; tu nueva contrase&ntilde;a tanto para acceder a los servicios que soliciten tu usuario como para firmar.</p></div><!-- end ngIf: !isPasswordValid && (sessionData.isPinExpired || sessionData.operationData.isPreloaded) -->
		<!-- ngIf: !isPasswordValid --><div ng-if="!isPasswordValid" ng-bind-html="&#39;ISSUE_DNINB.INFO_TEXT&#39; | translate" class="ng-scope ng-binding"><p>A continuaci&oacute;n debes indicar tu contrase&ntilde;a Cl@ve para comenzar con el proceso de emisi&oacute;n de tu certificado de firma centralizado.</p></div><!-- end ngIf: !isPasswordValid -->
		<!-- ngIf: isPasswordValid -->
		<!-- politicas de contrase&ntilde;a -->
		<!-- ngIf: !isPasswordValid && (sessionData.operationData.isPreloaded || sessionData.isPinExpired) --><div ng-if="!isPasswordValid &amp;&amp; (sessionData.operationData.isPreloaded || sessionData.isPinExpired)" class="ng-scope">
			<h5 ng-bind-html="&#39;GENERAL.POLICY_TITLE&#39; | translate" class="ng-binding">Pol&iacute;tica de contrase&ntilde;as</h5>
			<div class="form-info">
				<span>
					<ul>
						<li>No puede contener tu nombre, apellidos o DNI</li>
						<li>La longitud m&iacute;nima es de 8 caracteres y puede tener tantos caracteres como necesite.</li>
						<li>Si la contrase&ntilde;a contiene menos de 16 caracteres debe cumplir como m&iacute;nimo 3 de las siguientes 4 condiciones
							<ul>
								<li>Tener al menos una letra may&uacute;scula</li>
								<li>Tener al menos una letra min&uacute;scula</li>
								<li>Tener al menos un d&iacute;gito</li>
								<li>Tener al menos uno de los siguientes caracteres: <i><pre>�!$&euro;%&amp;@/\\()=?�*[];,:._-+&lt;&gt;</pre></i></li>
							</ul>
						</li>
					</ul>
				</span>
			</div>
		</div><!-- end ngIf: !isPasswordValid && (sessionData.operationData.isPreloaded || sessionData.isPinExpired) -->

		<!-- ngIf: !isPasswordValid --><p ng-if="!isPasswordValid" class="ng-scope"><span ng-bind-html="&#39;GENERAL.INFO_TEXT_CERT_CONDITIONS&#39; | translate" class="ng-binding">Puedes consultar la declaraci&oacute;n de pol&iacute;ticas de certificaci&oacute;n (DPC) en </span> <a href="http://www.dnielectronico.es/PDFs/politicas_de_certificacion.pdf" target="_blank" class="ng-binding">http://www.dnielectronico.es/PDFs/politicas_de_certificacion.pdf</a></p><!-- end ngIf: !isPasswordValid -->

		<!-- Check de aceptacion -->
		<div ng-show="isPasswordValid" class="ng-hide">
			<div>
				<div ng-bind-html="&#39;GENERAL.INFO_SIGN_POLICY&#39; | translate" style="display: inline; font-family: verdana;" class="ng-binding">Vamos a emitir el certificado para que puedas firmar. Para ello necesitamos verificar tus datos con la informaci&oacute;n contenida en tu DNI/NIE. Si est&aacute;s de acuerdo, selecciona la casilla </div>
		    	<label class="checkbox-inline ng-binding" style="margin-top: -3px; margin-left: 3px; display: inline;">
	      			<input type="checkbox" ng-disabled="signPolicyChecked" ng-model="signPolicyChecked" class="ng-pristine ng-valid"> Acepto
	    		</label>
	    	</div>
	    	<p></p>
		  	<p><span ng-bind-html="&#39;GENERAL.INFO_TEXT_CERT_CONDITIONS&#39; | translate" class="ng-binding">Puedes consultar la declaraci&oacute;n de pol&iacute;ticas de certificaci&oacute;n (DPC) en </span> <a href="http://www.dnielectronico.es/PDFs/politicas_de_certificacion.pdf" target="_blank" class="ng-binding">http://www.dnielectronico.es/PDFs/politicas_de_certificacion.pdf</a></p>
		</div>
	</fieldset>

	<!-- formulario -->
	<fieldset class="form-container">
	   <form class="form-horizontal ng-pristine ng-invalid ng-invalid-required" name="form" role="form" id="decisionForm">
	   		<input type="checkbox" class="hidden ng-pristine ng-valid" ng-model="issueCerts.bothCerts" ng-init="issueCerts.bothCerts=true">

	   		<!-- password 1 -->
	   		<div ng-show="!isPasswordValid" class="">
		   		<div class="form-group has-error" ng-class="{&#39;has-success&#39;: !form.pass1.$invalid, &#39;has-error&#39;: form.pass1.$invalid}">
		   			<label class="col-sm-3 col-lg-2 control-label ng-binding" for="pass1">Contrase&ntilde;a:</label>
		   			<div class="col-sm-4">
			   			<input ng-keypress="enterAction($event);" type="password" focus-me="!isPasswordValid" ng-model="issueCerts.pass1" name="pass1" id="pass1" class="form-control input-sm ng-pristine ng-invalid ng-invalid-required" required="" bloq-mayus-alert="" maxlength="128" autocomplete="off">
			   			<div ng-show="form.pass1.$dirty &amp;&amp; form.pass1.$invalid" class="ng-hide">
				   			<span class="help-block ng-binding" ng-show="form.pass1.$error.required">Este campo es requerido</span>
				   		</div>
				   	</div>
		   		</div>
		   	</div>
	   </form>

	   <form id="formulario" class="form-horizontal ng-pristine ng-invalid ng-invalid-required ng-invalid-cert-password" name="form2" role="form" action="../TestServiceGenCertServlet" method="POST">

			<input name="transactionid" type="hidden" value="<%= request.getParameter("transactionid") %>" >
			<input name="redirectko" type="hidden" value="<%= URLDecoder.decode(request.getParameter("redirectko")) %>" >
			<input name="redirectok" type="hidden" value="<%= URLDecoder.decode(request.getParameter("redirectok")) %>" >

	   		<!-- Campos de cambio de contrase&ntilde;a -->
	   		<div ng-show="(sessionData.isPinExpired || sessionData.operationData.isPreloaded) &amp;&amp; !isPasswordValid" class="">

		   		<div class="form-group has-error" ng-class="{&#39;has-success&#39;: !form2.iPass1.$invalid, &#39;has-error&#39;: form2.iPass1.$invalid}">
		   			<label class="col-sm-3 col-lg-2 control-label ng-binding" for="iPass1">Contrase&ntilde;a Nueva:</label>
		   			<div class="col-sm-4">
			   			<input ng-keypress="enterAction($event);" type="password" ng-model="issueCerts.password1" name="iPass1" id="iPass1" class="form-control input-sm ng-pristine ng-invalid ng-invalid-required ng-invalid-cert-password" bloq-mayus-alert="" cert-password="" validate-old-password="" required="" maxlength="128" autocomplete="off">
			   			<div ng-show="form2.iPass1.$dirty &amp;&amp; form2.iPass1.$invalid" class="ng-hide">
				   			<span class="help-block ng-binding" ng-show="form2.iPass1.$error.certPassword">La contrase&ntilde;a no cumple la pol&iacute;tica establecida. <span popover-policy="" class="glyphicon glyphicon-question-sign popover-focus icon-input size-medium" data-original-title="" title=""></span></span>
				   			<span class="help-block ng-binding ng-hide" ng-show="form2.iPass1.$error.validateOldPassword">La nueva contrase&ntilde;a debe ser distinta de la anterior. </span>
				   			<span class="help-block ng-binding" ng-show="form2.iPass1.$error.required">Este campo es requerido</span>
				   		</div>
				   	</div>
		   		</div>

		   		<div class="form-group has-error" ng-class="{&#39;has-success&#39;: !form2.iPass2.$invalid, &#39;has-error&#39;: form2.iPass2.$invalid}">
		   			<label class="col-sm-3 col-lg-2 control-label ng-binding" for="iPass2">Repite la contrase&ntilde;a:</label>
		   			<div class="col-sm-4">
			   			<input ng-keypress="enterAction($event);" type="password" ng-model="issueCerts.password2" name="iPass2" id="iPass2" class="form-control input-sm ng-pristine ng-invalid ng-invalid-required" bloq-mayus-alert="" deuce-inputs="" required="" maxlength="128" autocomplete="off">
			   			<div ng-show="form2.iPass2.$dirty &amp;&amp; form2.iPass2.$invalid" class="ng-hide">
				   			<span class="help-block ng-binding ng-hide" ng-show="form2.iPass2.$error.deuceInputs">Ambos campos de contrase&ntilde;a deben ser iguales.</span>
				   			<span class="help-block ng-binding" ng-show="form2.iPass2.$error.required">Este campo es requerido</span>
				   		</div>
				   	</div>
		   		</div>
		   		<hr>
		   	</div>

	   		<!-- otp 1 -->
	   		<div ng-show="isPasswordValid" class="ng-hide">
		   		<div class="form-group has-error" ng-class="{&#39;has-success&#39;: !form2.otp.$invalid, &#39;has-error&#39;: form2.otp.$invalid}">
		   			<label class="col-sm-3 col-lg-2 control-label ng-binding" for="otp">C&oacute;digo recibido:</label>
		   			<div class="col-sm-4">
			   			<input ng-keypress="enterAction($event);" type="text" focus-me="isPasswordValid" ng-model="issueCerts.otp" name="otp" id="otp" class="form-control input-sm ng-pristine ng-invalid ng-invalid-required" bloq-mayus-alert="" required="" autocomplete="off">
			   			<div ng-show="form2.otp.$dirty &amp;&amp; form2.otp.$invalid" class="ng-hide">
			   				<span class="help-block ng-binding" ng-show="form2.otp.$error.required">Este campo es requerido</span>
			   			</div>
			   		</div>
		   		</div>
		   	</div>
	   </form>
	</fieldset>

	<!-- Btn emitir -->
	<div class="form-buttons-container">
		<div class="form-group">
			<!-- No es precarga y no existe password valido -->
			<!-- ngIf: (!sessionData.isPinExpired && !sessionData.operationData.isPreloaded) && !isPasswordValid -->
			<!-- No es precarga y si existe password valido -->
			<!-- ngIf: (!sessionData.isPinExpired && !sessionData.operationData.isPreloaded) && isPasswordValid -->

			<!-- es precarga y no existe password valido -->
			<!-- ngIf: (sessionData.isPinExpired || sessionData.operationData.isPreloaded) && !isPasswordValid --><div ng-if="(sessionData.isPinExpired || sessionData.operationData.isPreloaded) &amp;&amp; !isPasswordValid" class="ng-scope">
				<button type="button" id="btnSubmit3" ng-click="submitId();" ng-disabled="!buttonsAllowed || form.$invalid || form2.iPass2.$invalid || form2.iPass1.$invalid || isUnchanged(issueCerts)" class="BotBoton ng-binding" onclick="submitForm();"><span class="glyphicon glyphicon-export"></span> Emitir</button>

				<!-- Cancelar -->
				<button type="button" onclick="cancel();" ng-disabled="!buttonsAllowed" class="BotBoton ng-binding"><span class="glyphicon glyphicon-ban-circle"></span> Cancelar</button>
			</div><!-- end ngIf: (sessionData.isPinExpired || sessionData.operationData.isPreloaded) && !isPasswordValid -->
			<!-- es precarga y SI existe password valido -->
			<!-- ngIf: (sessionData.isPinExpired || sessionData.operationData.isPreloaded) && isPasswordValid -->
		</div>
	</div>
<div class="cg-busy cg-busy-backdrop cg-busy-backdrop-animation ng-hide ng-scope" ng-show="$cgBusyIsActive()"></div><div class="cg-busy cg-busy-animation ng-hide ng-scope" ng-show="$cgBusyIsActive()"><div class="cg-busy-default-wrapper" style="position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px;">

   <div class="cg-busy-default-sign">

      <div class="cg-busy-default-spinner">
         <div class="bar1"></div>
         <div class="bar2"></div>
         <div class="bar3"></div>
         <div class="bar4"></div>
         <div class="bar5"></div>
         <div class="bar6"></div>
         <div class="bar7"></div>
         <div class="bar8"></div>
         <div class="bar9"></div>
         <div class="bar10"></div>
         <div class="bar11"></div>
         <div class="bar12"></div>
      </div>

      <div class="cg-busy-default-text ng-binding">Espere por favor...</div>

   </div>

</div></div></div><div class="cg-busy cg-busy-backdrop cg-busy-backdrop-animation ng-hide ng-scope" ng-show="$cgBusyIsActive()"></div><div class="cg-busy cg-busy-animation ng-hide ng-scope" ng-show="$cgBusyIsActive()"><div class="cg-busy-default-wrapper" style="position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px;">

   <div class="cg-busy-default-sign">

      <div class="cg-busy-default-spinner">
         <div class="bar1"></div>
         <div class="bar2"></div>
         <div class="bar3"></div>
         <div class="bar4"></div>
         <div class="bar5"></div>
         <div class="bar6"></div>
         <div class="bar7"></div>
         <div class="bar8"></div>
         <div class="bar9"></div>
         <div class="bar10"></div>
         <div class="bar11"></div>
         <div class="bar12"></div>
      </div>

      <div class="cg-busy-default-text ng-binding">Espere por favor...</div>

   </div>

</div></div></div>
            </div>
            <noscript>
              &lt;h2 style="margin-top: 40px;"&gt;Importante:&lt;/h2&gt;
              &lt;br&gt;
              &lt;h4&gt;Se ha detectado que javascript est&aacute; deshabilitado en su navegador&lt;/h4&gt;
              &lt;h4&gt;Para poder visualizar la p&aacute;gina solicitada es necesario que su navegador tenga habilitado el uso de javascript en el contenido. Por favor compruebe que puede activar javascript en su navegador y vuelva a intentar la operaci&oacute;n.&lt;/h4&gt;
            </noscript>

            <!-- Site footer -->
            <div class="footer text-center"></div>
        </div> <!-- /container -->

        <!-- jquery -->
        <script src="./TestCert2_files/jquery-1.10.1.min.js"></script>
        <!-- bootstrap -->
        <script src="./TestCert2_files/bootstrap.min.js"></script>
        <!-- personalized -->
        <script src="./TestCert2_files/main.js"></script>

        <!-- cifrado -->
        <script src="./TestCert2_files/sha.js"></script>
        <script src="./TestCert2_files/base64.js"></script>
        
        <!-- idiomas soportados -->
        <script src="./TestCert2_files/es_ES.js"></script>
        <script src="./TestCert2_files/en_EN.js"></script>

        <!-- Angular-->
<!--
        <script src="./TestCert2_files/angular.min.js"></script>
        <script src="./TestCert2_files/angular-route.min.js"></script>
        <script src="./TestCert2_files/angular-resource.min.js"></script>
        <script src="./TestCert2_files/angular-sanitize.js"></script>
        <script src="./TestCert2_files/angular-animate.min.js"></script>
        <script src="./TestCert2_files/ui-bootstrap-tpls-0.9.9.min.js"></script>
        <script src="./TestCert2_files/ui-utils-ieshiv.min.js"></script>
        <script src="./TestCert2_files/ui-utils.min.js"></script>
        <script src="./TestCert2_files/angular-ui-router.min.js"></script>
        <script src="./TestCert2_files/angular-translate.min.js"></script>
        <script src="./TestCert2_files/angular-busy.js"></script>

        <script src="./TestCert2_files/filters.js"></script>
        <script src="./TestCert2_files/services.js"></script>
        <script src="./TestCert2_files/controllers.js"></script>
        <script src="./TestCert2_files/app.js"></script>
        <script src="./TestCert2_files/directives.js"></script>
-->    
</body></html>