
<%@page import="java.nio.charset.Charset"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="es.gob.afirma.core.misc.Base64"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

 <%
 	String infoDocumentos = request.getParameter("infoDocumentos");
	
 	String[] info = new String[0];
 	if (infoDocumentos != null && !infoDocumentos.isEmpty()){
 		infoDocumentos = new String(Base64.decode(infoDocumentos, true), "UTF-8");
 		info = infoDocumentos.split(",");
 	}
	
	boolean hayInfo = false;
	for (int i=0; i<info.length; i++){
		if (info[i]!=null && !info[i].trim().isEmpty()){
			hayInfo = true;
		}
	}
 %>
 
 
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
		<img src="./img/imagenCorporativa.png" alt="" title="">
	</div>
</div>
<div class="container" id="page-wrapper">
<div id="pasarela-header" class="home_titulo">
<div class="home_wrapper">
<h2>
<strong>P&aacute;gina de prueba de la Plataforma de firma centralizada - Cl@ve Permanente</strong>
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
<button onclick="cancelar();" id="confirmCancel_CancelBtn" type="button">SI</button>					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;					<button onclick="closeCancelar();" id="confirmCancel_VolverBtn" type="button">NO</button>
</div>
</div>
</div>
<div id="pasarela-content" class="contenedorSolicitud">
<div class="modalOff" id="errorServer">
<div class="errorServer">
<p class="titulo_popup_peq">Se ha producido un error durante el proceso de firma</p>
<p>
<span id="errorServerMsg"></span>
</p>
</div>
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


 <form method="post" action="<%= request.getContextPath() %>/TestServiceAuthServlet" id="pinAndSFDA">

 <% if (!hayInfo) { %>
	<section class="tmpl_2_cols">	
<% } else { %>
	<section class="tmpl_3_cols">
<% } %>
		<h1>Firma</h1> 
		<div class="centro_tituloCabecera">
		<p class="margen_der">Esta es una p&aacute;gina de prueba para simular el servicio de firma.</p>&nbsp;&nbsp;						   								<p class="margen_der">Para firmar, a continuación introduce tu contraseña.</p>
		</div>
	</section>

	
 <% if (hayInfo) { %>

	<section class="tmpl_4_cols">
	<fieldset>
		<div class="row" id="pasarela-content-row">
		<p class="tituloFirma">
			<label class="grisoscuro" for="owner">Documentos a firmar </label>
		</p>
		<table id="documents-table">
		<thead class="fixedHeaderTable">
		<tr>
			<th>Id. Documento</th><th>Título </th>
		</tr>
		</thead>
		<tbody>
			<%
				for (int i=0; i<info.length; i=i+2){
			%>
				<tr>
					<td><%= info[i] %></td>
					<td><%= info[i+1] %></td>
				</tr>
			<%
				}
			%>
		</tbody>
		</table>
		</div>
	</fieldset>
	</section>
 
	
<% } %>
	
	
	
<% if (!hayInfo) { %>
	<section class="tmpl_2_cols">
<% } else { %>
	<section class="tmpl_5_cols">
<% } %>
<fieldset>
	 <p>
	 <label class="grisoscuro" for="owner">USUARIO FIRMANTE</label>&nbsp;&nbsp;								<%= request.getParameter("id") %></p>
	  <label class="grisoscuro" for="owner">CONTRASEÑA&nbsp;&nbsp;										</label>
	  <input type="password" name="password" class="password" id="pin" value="" maxlength="255"  required="required" autocomplete="off">
	  <br>

	  
	  <input type="hidden" name="transactionid" value="<%= request.getParameter("transactionid") %>">
	  <input type="hidden" name="redirectok" value="<%= URLDecoder.decode(new String(Base64.decode(request.getParameter("redirectok"), true))) %>">
	  <input type="hidden" name="redirectko" value="<%= URLDecoder.decode(new String(Base64.decode(request.getParameter("redirectko"), true))) %>">
	 </p>
	 
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
<p>@ 2015 Cl@ve · Identidad Electrónica para las Administraciones · Gobierno de España</p>
</div>
<div class="clr"></div>
</div>
</footer>
 
  <script>
  	document.getElementById("pin").focus();
  </script>
</body>
</html>