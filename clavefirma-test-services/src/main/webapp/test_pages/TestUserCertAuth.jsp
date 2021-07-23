
<%@page import="java.nio.charset.Charset"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="es.gob.afirma.core.misc.Base64"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Tu seguridad social</title>
<link href="./TestAuth_files/IdP.css" media="screen" rel="stylesheet">

<script type="text/javascript">

	function cancelar() {
		var formulario = document.getElementById("pinAndSFDA");
		formulario.action = '<%= URLDecoder.decode(new String(Base64.decode(request.getParameter("redirectko"), true))) %>';
		formulario.submit();
	}
</script>
</head>
<body id="pasarela-body">
<div class="container_cabecera">
	<div class="cabecera_clave">
		<img src="./TestAuth_files/imagenCorporativa.png" alt="" title="">
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
	 <label class="grisoscuro" for="owner">USUARIO</label>&nbsp;&nbsp;								<%= request.getParameter("id") %></p>
	  <label class="grisoscuro" for="owner">CONTRASEÑA&nbsp;&nbsp;										</label>
	  <input type="password" name="password" class="password" id="pin" value="" maxlength="255"  required="required" autocomplete="off">
	  <br>

	  <input type="hidden" name="subjectid" value="<%= request.getParameter("subjectid") %>">
	  <input type="hidden" name="redirectok" value="<%= URLDecoder.decode(new String(Base64.decode(request.getParameter("redirectok"), true))) %>">
	  <input type="hidden" name="redirectko" value="<%= URLDecoder.decode(new String(Base64.decode(request.getParameter("redirectko"), true))) %>">

	 
	 <div align="left" id="botonera">
	<button type="submit">Continuar</button>&nbsp;&nbsp;								<button onclick="cancelar();" id="confirmCancel_CancelarSub" type="button">Cancelar</button>
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
<p>@ 2021 Cl@ve · Identidad Electrónica para las Administraciones · Gobierno de España</p>
</div>
<div class="clr"></div>
</div>
</footer>
 
</body>
</html>