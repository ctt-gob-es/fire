<!DOCTYPE html>
<!-- saved from url=(0164)https://clave-dninbrt.dev.seg-social.gob.es/rss-gateway/CertificateManagementServlet?id_transaction=8e6f5a59c6da85805ba534f596ed147baf371c52ec68f3b1d4c9d77df56a5720 -->
<%@page import="es.gob.afirma.core.misc.Base64"%>
<html lang="es">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta content="IE=edge" http-equiv="X-UA-Compatible">
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<meta content="no-cache" http-equiv="cache-control">
	<meta content="0" http-equiv="expires">
	<meta content="no-cache" http-equiv="pragma">
	<!--<base href="./">--><base href=".">
	<title>Tu Seguridad Social</title>
	<link media="screen" rel="stylesheet" href="">
	<link href="./TestCert_files/IdP.css" media="screen" rel="stylesheet">
	<script src="./TestCert_files/jquery-1.9.1.min.js"></script><script language="javascript">
		var mbMovilBrowser = false;
		var msIdFocus = null;
		var marSubmit = false;
		function isBrowserMobile() {
			if (navigator.userAgent.match(/Android/i) ||
					navigator.userAgent.match(/webOS/i) ||
					navigator.userAgent.match(/iPhone/i) ||
					navigator.userAgent.match(/iPad/i) ||
					navigator.userAgent.match(/iPod/i) ||
					navigator.userAgent.match(/BlackBerry/i) ||
					navigator.userAgent.match(/Windows Phone/i)) {
				return true;
			}
			else {
				return false;
			}
		}
	
		function onSubmit() {
			if (marSubmit == true) {
				var working=document.forms.decisionForm.id_texto.value;
				alert(working);
				return false;
			}
			marSubmit=true;
		}
	
		function cancel() {
			var formulario = document.getElementById("decisionForm");
			formulario.action = decodeURIComponent("<%=new String(Base64.decode(request.getParameter("redirectko"), true))%>");
			formulario.submit();
		}
	</script>
	<link href="https://clave-dninbrt.dev.seg-social.gob.es/rss-gateway-static/img/faviconQ3E1TZ34.ico" rel="icon">
</head>
<body id="decision-body">
<div class="container_cabecera">
<div class="cabecera_clave">
<img src="./TestCert_files/imagenCorporativa.png" alt="" title=""></div>
</div>
<div class="container" id="page-wrapper">
<div id="pasarela-header" class="home_titulo">
<div class="home_wrapper">
<h2>
<strong>Plataforma de firma centralizada - Cl@ve Permanente</strong>
</h2>
</div>
</div>
<section>
<h1>Solicitud del certificado centralizado (P&aacute;gina de prueba)</h1>
<form onsubmit="return onSubmit();" autocomplete="off" method="post" id="decisionForm" action="TestCert2.jsp">
<input name="transactionid" type="hidden" value="<%= request.getParameter("transactionid") %>">
<input name="redirectko" type="hidden" value="<%= new String(Base64.decode(request.getParameter("redirectko"), true)) %>">
<input name="redirectok" type="hidden" value="<%= new String(Base64.decode(request.getParameter("redirectok"), true)) %>">
<p class="margen_der">
</p><div type="hidden" id="no_url_clave" style="display: none;">Vas a generar tu certificado de firma centralizado. Este certificado podr&aacute;s utilizarlo igual que el actual certificado digital, pero sin necesidad de tenerlo instalado en el dispositivo con el que est&aacute;s accediendo a internet. Para m&aacute;s informaci&oacute;n, puedes consultar en la web de#t#http://clave.gob.es/clave_Home/dnin.html#a#Cl@ve.gob.es#f#</div>
<div style="display:inline" type="hidden" id="url_clave">
<div style="display:inline" id="primera_frase">Vas a generar tu certificado de firma centralizado. Este certificado podr&aacute;s utilizarlo igual que el actual certificado digital, pero sin necesidad de tenerlo instalado en el dispositivo con el que est&aacute;s accediendo a internet. Para m&aacute;s informaci&oacute;n, puedes consultar en la web de</div>
<a href="http://clave.gob.es/clave_Home/dnin.html" target="_blank" id="url_name" style="display:inline">Cl@ve.gob.es</a>
</div>
<p></p>
<p></p>
<br>
<p class="margen_der">
</p>
<p></p>
<div class="formButtons" id="botonera">
<button name="btnYes" id="btnYes" type="submit">Solicitar Certificado</button>										&nbsp;&nbsp;&nbsp;&nbsp;
<button name="btnNo" id="btnNo" onclick="cancel()">Cancelar</button>
</div>
</form>
<script type="text/javascript">								var aux = "Vas a generar tu certificado de firma centralizado. Este certificado podr&aacute;s utilizarlo igual que el actual certificado digital, pero sin necesidad de tenerlo instalado en el dispositivo con el que est&aacute;s accediendo a internet. Para m&aacute;s informaci&oacute;n, puedes consultar en la web de#t#http://clave.gob.es/clave_Home/dnin.html#a#Cl@ve.gob.es#f#";								window.onload= function(){								if ($("#btnYes").length > 0){								$('#btnYes').focus()								} else {								if ($("#btnNo").length > 0){								$('#btnNo').focus()								}								}								var url_search = "#a#";								var texto_search = "#t#";								var final_search = "#f#";								var pos_texto_search = aux.indexOf(texto_search);								if(Number(pos_texto_search) > 0){								var primera_frase = document.getElementById("primera_frase");								var url_name = document.getElementById("url_name");								var primera_parte = aux.substr(0,pos_texto_search);								aux=aux.substr(pos_texto_search+3);								var pos_url_search = aux.indexOf(url_search);								var url_name_href = aux.substr(0,pos_url_search);								aux=aux.substr(pos_url_search+3);								var pos_final_search = aux.indexOf(final_search);								var url_name_valor = aux.substr(0,pos_final_search);								primera_frase.innerHTML = primera_parte;								url_name.href = url_name_href;								url_name.innerHTML = url_name_valor;								$('#url_clave').show();								$('#no_url_clave').hide();								}else{								$('#url_clave').hide();								$('#no_url_clave').show();								}								var url_search = "#u#";								var final_search = "#f#";								var aux_aviso = "Aviso Legal#u#https://tu.seg-social.gob.es/wps/portal/tuss/tuss/Informacion/AvisoLegal/!ut/p/a1/hY6xDoIwEIafxaGj9KqIxK2DMTZEF43QhYDWUlMKKRV8fMHESdHb7vL93_2Y4xhzk7VKZk5VJtPDzoMUNr5PWDhje8LWQP0gOpLDjgBb9kAyACND4V_-hPlPBII3MP6CYS51lb_qJtTk81BibsVVWGG9u-3PhXP1CgGCruu8RshpU51Vpj3RINgaJ6wRLiUIlLmIh1e48pupqBqH4w8DrssYbgvdRnQyeQJ4t5Df/dl5/d5/L2dJQSEvUUt3QS80SmlFL1o2XzBHNDQxSjgySk8xSkUwQTQ2TFUxVE4xMFIx/#f#";								var aux_seguridad = "Seguridad Social#u#http://www.seg-social.es/Internet_1/index.htm#f#";								var aux_sede = "Sede Electr√≥nica#u#https://sede.seg-social.gob.es/Sede_1/index.htm#f#";								var pos_texto_search_aviso = aux_aviso.indexOf(url_search);								url_name_aviso.innerHTML =								aux_aviso.substr(0,pos_texto_search_aviso);								aux_aviso=aux_aviso.substr(pos_texto_search_aviso+3);								var pos_final_search_aviso = aux_aviso.indexOf(final_search);								url_name_aviso.href =								aux_aviso.substr(0,pos_final_search_aviso);								var pos_texto_search_seguridad = aux_seguridad.indexOf(url_search);								url_name_seguridad.innerHTML =								aux_seguridad.substr(0,pos_texto_search_seguridad);								aux_seguridad=aux_seguridad.substr(pos_texto_search_seguridad+3);								var pos_final_search_seguridad =								aux_seguridad.indexOf(final_search);								url_name_seguridad.href = aux_seguridad.substr(0,pos_final_search_seguridad);								var pos_texto_search_sede = aux_sede.indexOf(url_search);								url_name_sede.innerHTML =								aux_sede.substr(0,pos_texto_search_sede);								aux_sede=aux_sede.substr(pos_texto_search_sede+3);								var pos_final_search_sede = aux_sede.indexOf(final_search);								url_name_sede.href = aux_sede.substr(0,pos_final_search_sede);						}							</script>
</section>
</div>
<footer role="banner">
<div class="footer_top wrapper">
<div class="logoClave"></div>
<div class="clr"></div>
</div>
<div class="footer_bottom wrapper">
<div class="comp_left">
<p>@ 2015 Cl@ve "Identidad Electr&oacute;nica para las Administraciones"∑ Gobierno de Espa&ntilde;a</p>
</div>
<div class="clr"></div>
</div>
</footer>


</body></html>