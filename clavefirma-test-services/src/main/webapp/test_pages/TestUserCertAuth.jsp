
<%@page import="java.nio.charset.Charset"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="es.gob.afirma.core.misc.Base64"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Tu seguridad social</title>
<link href="./test_pages/css/IdP.css" media="screen" rel="stylesheet">
<script src="./test_pages/js/jquery-4.0.0.min.js"></script>

<script type="text/javascript">

	function showCancelar() {											
		$('#pasarela-content').hide();						
		$('#confirmCancel').attr('class', 'modalOn');							
		$('#page-overlay').show();						
		$('#tablaCentrada').hide();					
	}
	
	function closeCancelar() {
	    $('#confirmCancel').attr('class', 'modalOff');
	    $('#page-overlay').hide();
	    $('#pasarela-content').show();
	    $('#tablaCentrada').show();
	    if ($("#pin").length > 0) {
	        $('#pin').focus()
	    } else {
	        if ($("#sfdaValue0").length > 0) {
	            $('#sfdaValue0').focus()
	        }
	    }
	}

	function cancelar() {
		var formulario = document.getElementById("pinAndSFDA");
		formulario.action = '<%= URLDecoder.decode(request.getParameter("redirectko"), "utf-8") %>';
		formulario.submit();
	}
</script>
</head>
<body id="pasarela-body">
<div class="container_cabecera">
	<div class="cabecera_clave">
		<img src="./test_pages/img/imagenCorporativa.png" alt="" title="">
	</div>
</div>
<div class="container" id="page-wrapper">
<div id="pasarela-header" class="home_titulo">
<div class="home_wrapper">
<h2>
<strong>P&aacute;gina de prueba de la Plataforma de firma centralizada - Autenticaci&oacute;n para acceso a certificados en la nube</strong>
</h2>
</div>
</div>
<div class="modalOff" id="confirmCancel">
<div class="confirmCancel">
<p class="titulo_popup_peq">¿Deseas cancelar la firma de documentos?</p>
<p>
<span class="grisoscuro" id="errorMsg">Si realmente deseas cancelar la firma de documentos pulsa el botón 'SI'. En el caso de querer continuar con la transacción de firma pulsa el botón 'NO'.</span>
</p>
<br>
<div id="botoneraModal">
<button onclick="cancelar();" id="confirmCancel_CancelBtn" type="button">SI</button>					
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;					
<button onclick="closeCancelar();" id="confirmCancel_VolverBtn" type="button">NO</button>
</div>
</div>
</div>
<div id="pasarela-content" class="contenedorSolicitud">
<div class="modalOff" id="errorServer">
</div>
<div class="modalOff" id="alert">
<h2>Los datos introducidos no son correctos</h2>
<p>
</p><div id="errorMsg"></div>
<p></p>
<div class="botoneraModal">
<button class="modalClose" onclick="hideError();" type="button">Cerrar</button>
</div>
</div>
<br>


 <form method="post" action="<%= request.getContextPath() %>/TestServiceUserCertAuthServlet" id="pinAndSFDA">
		<h1>Autenticaci&oacute;n</h1> 
		<div class="centro_tituloCabecera">
		<p class="margen_der">Esta es una p&aacute;gina de prueba para simular el servicio de autenticaci&oacute;n de usuarios para 
		la obtenci&oacute;n de certificados en la nube.</p>
		<p class="margen_der">A continuación introduce tu contraseña.</p>
		</div>
<fieldset>
	 <p>
	 <label class="grisoscuro">USUARIO</label>&nbsp;&nbsp;								<%= request.getParameter("subjectid") %></p>
	  <label class="grisoscuro" for="password">CONTRASEÑA&nbsp;&nbsp;										</label>
	  <input type="password" name="password" class="password" id="pin" value="" maxlength="255"  required="required" autocomplete="off">
	  <br>

	  <input type="hidden" name="subjectid" value="<%= request.getParameter("subjectid") %>">
	  <input type="hidden" name="redirectok" value="<%= URLDecoder.decode(request.getParameter("redirectok"), "utf-8") %>">
	  <input type="hidden" name="redirectko" value="<%= URLDecoder.decode(request.getParameter("redirectko"), "utf-8") %>">

	 
	 <div align="left" id="botonera">
	<button type="submit">Continuar</button>&nbsp;&nbsp;								<button onclick="showCancelar();" id="confirmCancel_CancelarSub" type="button">Cancelar</button>
	</div>

 </fieldset>
 </section>
 </form>
 
 </div>
</div>
<footer role="banner">
<div class="footer_top wrapper">
<div class="logoClave"></div>
<div class="clr"></div>
</div>
<div class="footer_bottom wrapper">
<div class="comp_left">
<p>@ 2026 Cl@ve · Identidad Electrónica para las Administraciones · Gobierno de España</p>
</div>
<div class="clr"></div>
</div>
</footer>
         <script>
  			document.getElementById("pin").focus();
  		</script>
</body>
</html>