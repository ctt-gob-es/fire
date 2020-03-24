<%@page import="es.gob.fire.test.webapp.Base64"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Prueba FIRe</title>
		<link rel="shortcut icon" href="img/cert.png">
		<link rel="stylesheet" href="styles/styles.css"/>
		<script type="text/javascript">
		
			// Funcion para activar y desactivar la configuracion de firma particular
			// para el documento que se va a agregar
			function enableCustomConfig() {
				
				var enabled = document.getElementById("custom-config").checked;
				var fields = document.getElementsByName("operation");
				for (var i = 0; i < fields.length; i++) {
					fields[i].disabled = !enabled;
				}
				fields = document.getElementsByName("format");
				for (var i = 0; i < fields.length; i++) {
					fields[i].disabled = !enabled;
				}
				fields = document.getElementsByName("upgrade");
				for (var i = 0; i < fields.length; i++) {
					fields[i].disabled = !enabled;
				}
			}
		</script>
		
	</head>
	<body style=" font-weight: 300;">
		<%
		
			if (session.getAttribute("user") == null) { //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

			// Comprobamos si ya se cargo algun documento
			boolean fileLoaded = Boolean.parseBoolean((String) session.getAttribute("fileLoaded")); //$NON-NLS-1$

			List<String> docNames = (List<String>) session.getAttribute("docNames"); //$NON-NLS-1$
			
			// Comprobamos si se cargo un documento con exito
			String attributes = request.getParameter("attributes"); //$NON-NLS-1$
			boolean attributesOk = attributes != null && attributes.equals("success"); //$NON-NLS-1$
			
			String errorType = request.getParameter("error"); //$NON-NLS-1$

			// Comprueba si se ha producido algun error
			String errorMsg = null;
			if (errorType != null && !attributesOk) {
				if ("errordocument".equals(errorType)) { //$NON-NLS-1$
					errorMsg = "Error en la carga el documenot"; //$NON-NLS-1$
				} else if ("nodocument".equals(errorType)) { //$NON-NLS-1$
					errorMsg = "No se ha cargado un fichero"; //$NON-NLS-1$
				} else if ("noid".equals(errorType)) { //$NON-NLS-1$
					errorMsg = "No se introducido el identificador del documento"; //$NON-NLS-1$
				} else if ("maxdocs".equals(errorType)) { //$NON-NLS-1$
					errorMsg = "Se ha alcanzado el m\u00E1ximo de documentos permitidos por lote"; //$NON-NLS-1$
				} else if ("duplid".equals(errorType)) { //$NON-NLS-1$
					errorMsg = "El identificador de documento ya se utilizo para otro documento del lote"; //$NON-NLS-1$
				}
			}
		%>
		<!-- Barra de navegacion -->
		<div id="menubar">
			<div id="bar-txt"><b>Prueba FIRe</b></div>
		</div>
		<!-- contenido -->
		
		<div id="sign-container">
			<h1 style="color:#303030;">AGREGAR DOCUMENTOS AL LOTE</h1>
			<% if (errorMsg != null) { %>
				<p id="error-txt-left">
				 <%= errorMsg %> 
				</p>
			<% } %>
			<% if (attributesOk) { %>
				<p id="success-txt-left">
				 Fichero cargado correctamente. 
				</p>
			<% } %>
			
			<div style="width: 100%; overflow: hidden;">
			
			<form class="left-column" method="POST" action="AddDocumentBatchService" enctype="multipart/form-data">
				<div style="display:inline-block;"></div>

				<br>
				<label for="batch-file-id">Identificador de documento:</label><br>
				<input id="batch-file-id" type="text" name="batch-file-id" /><br><br>
				<label for="batch-file">Seleccionar documento:</label><br>
				<input id="batch-file" type="file" name="batch-file" /><br><br>
				
				<fieldset>
				<legend>
				&nbsp;
					<input id="custom-config" type="checkbox" name="custom-config" onchange="enableCustomConfig()"/>
					<label for="custom-config">Configuraci&oacute;n particular</label><br>
				</legend>
					<fieldset class="fieldset-fire">
						<legend>Seleccione la operaci&oacute;n deseada: </legend>
							<input id="sign-op" type="radio" name="operation" value="sign" checked="checked" disabled='disabled'/>
								<label for="sign-op">Firma</label><br>
							<input id="cosign-op" type="radio" name="operation" value="cosign" disabled='disabled'/>
								<label for="cosign-op">Cofirma</label><br>
							<input id="countersign-op" type="radio" name="operation" value="countersign" disabled='disabled'/>
								<label for="countersign-op">Contrafirma</label><br>
				   	</fieldset>
	
				   	<fieldset class="fieldset-fire">
						<legend>Seleccione el formato de firma: </legend>
							<input id="cades-ft" type="radio" name="format" value="CAdES" checked="checked" disabled='disabled'/>
								<label for="cades-ft">CAdES</label><br>
							<input id="xades-ft" type="radio" name="format" value="XAdES" disabled='disabled'/>
								<label for="xades-ft" >XAdES</label><br>
							<input id="pades-ft" type="radio" name="format" value="PAdES" disabled='disabled'/>
								<label for="pades-ft" >PAdES</label><br>
							<input id="facturae-ft" type="radio" name="format" value="FacturaE" disabled='disabled'/>
								<label for="facturae-ft" >FacturaE</label><br>
							<input id="asic-cades-ft" type="radio" name="format" value="CAdES-ASiC-S" disabled='disabled'/>
								<label for="asic-cades-ft" >CAdES-ASIC</label><br>
							<input id="asic-xades-ft" type="radio" name="format" value="XAdES-ASiC-S" disabled='disabled'/>
								<label for="asic-xades-ft" >XAdES-ASIC</label><br>
							<input id="pkcs1-ft" type="radio" name="format" value="NONE" disabled='disabled'/>
								<label for="pkcs1-ft" >Ninguno</label><br>
				   	</fieldset>
	
					<fieldset class="fieldset-fire">
						<legend>Post-proceso de la firma: </legend>
							<input id="none-ft" type="radio" name="upgrade" value="" checked="checked" disabled='disabled'/>
								<label for="none-ft" >Ninguno</label><br>
							<input id="es-a-ft" type="radio" name="upgrade" value="ES-A" disabled='disabled'/>
								<label for="es-a-ft" >Actualizar a ES-A</label><br>
							<input id="es-t-ft" type="radio" name="upgrade" value="ES-T" disabled='disabled'/>
								<label for="es-t-ft">Actualizar a ES-T</label><br>
							<input id="t-level-ft" type="radio" name="upgrade" value="T-Level" disabled='disabled'/>
								<label for="t-level-ft">Actualizar a T-Level</label><br>
							<input id="es-ltv-ft" type="radio" name="upgrade" value="ES-LTV" disabled='disabled'/>
								<label for="es-ltv-ft">Actualizar a ES-LTV</label><br>
							<input id="upgrade-verify" type="radio" name="upgrade" value="verify" disabled='disabled'/>
								<label for="upgrade-verify">Validar</label><br>
				   	</fieldset>

					<input id="extraparams-conf" type="hidden" name="extraParams"
						value="<%= Base64.encode("mode=implicit".getBytes()) %>"/>
				</fieldset>

				<div style="margin-top:30px;text-align: left;">
					<input id="submit-btn-add" type="submit" value="AGREGAR DOCUMENTO AL LOTE">
				</div>
			</form>
			
			<% if (docNames != null && !docNames.isEmpty()) { %>
				<div class="right-column" >
				<div class="doc-list-title">Ficheros cargados:</div>
				<ul class="doc-list">
				<% for (String name : docNames) { %>
					<li><%= name %></li>
				<% } %>
				</ul>
				</div>
			<% } %>
			
			</div>
			
 			<% if ( fileLoaded ) { %>
 				<form method="POST" action="SignBatchService" style="margin-top:30px; text-align: center;" accept-charset="UTF-8">
					<input type="checkbox" name="stoponerror" value="false" />Detener en caso de error
					<div style="margin-top:30px;text-align: center; ">
						<input  id="submit-btn"  type="submit" value="FIRMAR LOTE" />
					</div>
				</form>
  			<% } %>
		</div>
	</body>
</html>