<%@page import="es.gob.fire.test.webapp.Base64"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Prueba FIRe</title>
		<link rel="shortcut icon" href="img/cert.png">
		<link rel="stylesheet" href="styles/styles.css"/>
	</head>
	<body style=" font-weight: 300;">
		<% 
		
			if (session.getAttribute("user") == null) { //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}

			// Este parametro comprueba si el usuario ha introducido mal alguno de los atributos de la operacion
			// de firma; en caso de que no haya introducido alguna, recibimos attributes == fail
			boolean attributesFail = false;
			String attributes = request.getParameter("attributes"); //$NON-NLS-1$
			if (attributes != null) {
				if (attributes.equals("fail")) { //$NON-NLS-1$
					attributesFail = true;
				}
			}
		%>
		<div id="menubar">
			<div id="bar-txt"><b>Prueba FIRe</b></div>
		</div>
		
		<div id="sign-container">
			<h1 style="color:#303030;">CARGA DE DATOS PARA SU FIRMA</h1>
			<% if (attributesFail) { %>
				<p id="error-txt-left">
				 Debe seleccionar la operaci&oacute;n de firma, el formato e introducir los datos a firmar.
				</p>
			<% } %>
			<form method="POST" action="SignatureService" enctype="multipart/form-data">
				<div style="display:inline-block;"></div>

				<fieldset  class="fieldset-fire">
					<legend>Seleccione la operaci&oacute;n deseada: </legend>
						<input id="sign-op" type="radio" name="operation" value="sign" checked="checked"/>
							<label for="sign-op">Firma</label><br>
						<input id="cosign-op" type="radio" name="operation" value="cosign"/>
							<label for="cosign-op">Cofirma</label><br>
						<input id="countersign-op" type="radio" name="operation" value="countersign"/>
							<label for="countersign-op">Contrafirma</label><br>
			   	</fieldset>

				<fieldset  class="fieldset-fire">
					<legend>Seleccione el algoritmo de firma: </legend>
						<input id="algo-256" type="radio" name="algorithm" value="SHA256withRSA" checked="checked"/>
							<label for="algo-256" >SHA256withRSA</label><br>
						<input id="algo-512" type="radio" name="algorithm" value="SHA512withRSA"/>
							<label for="algo-512" >SHA512withRSA</label><br>
						<input id="algo-1" type="radio" name="algorithm" value="SHA1withRSA"/>
							<label for="algo-1">SHA1withRSA</label><br>
			   	</fieldset>

			   	<fieldset  class="fieldset-fire">
					<legend>Seleccione el formato de firma: </legend>
						<input id="cades-ft" type="radio" name="format" value="CAdES" checked="checked"/>
							<label for="cades-ft">CAdES</label><br>
						<input id="xades-ft" type="radio" name="format" value="XAdES"/>
							<label for="xades-ft" >XAdES</label><br>
						<input id="facturae-ft" type="radio" name="format" value="FacturaE"/>
							<label for="facturae-ft" >FacturaE</label><br>
						<input id="pades-ft" type="radio" name="format" value="PAdES"/>
							<label for="pades-ft" >PAdES</label><br>
						<input id="asic-cades-ft" type="radio" name="format" value="CAdES-ASiC-S"/>
							<label for="asic-cades-ft" >CAdES-ASIC</label><br>
						<input id="asic-xades-ft" type="radio" name="format" value="XAdES-ASiC-S"/>
							<label for="asic-xades-ft" >XAdES-ASIC</label><br>
						<input id="pkcs1-ft" type="radio" name="format" value="NONE"/>
							<label for="pkcs1-ft" >Ninguno</label><br>
			   	</fieldset>
				
				<fieldset  class="fieldset-fire">
					<legend>Post-proceso de la firma: </legend>
						<input id="none-ft" type="radio" name="upgrade" value="" checked="checked"/>
							<label for="none-ft" >Ninguno</label><br>
						<input id="t-level-ft" type="radio" name="upgrade" value="T-Level"/>
							<label for="t-level-ft">Actualizar a T-Level</label><br>
						<input id="lt-level-ft" type="radio" name="upgrade" value="LT-Level"/>
							<label for="t-level-ft">Actualizar a LT-Level</label><br>
						<input id="lta-level-ft" type="radio" name="upgrade" value="LTA-Level"/>
							<label for="lta-level-ft">Actualizar a LTA-Level</label><br>
						<input id="upgrade-verify" type="radio" name="upgrade" value="verify"/>
							<label for="upgrade-verify">Validar</label><br>
			   	</fieldset>
				
				 <%-- ExtraParams en Base64. En este ejemplo, se establecen los parametros:
				   - mode=implicit
				   - filters=keyusage.nonrepudiation:true;nonexpired:
				  Con ellos se generan firmas que contienen los datos firmados y solo se permite el uso
				  de certificados de firma no caducados. --%>
 
				<input id="extraparams-conf" type="hidden" name="extraParams"
	 				value="<%= Base64.encode("mode=implicit\nfilters=keyusage.nonrepudiation:true;nonexpired:".getBytes()) %>" />

				<div style="margin-top: 10px; text-align: left; ">
					<label for="fichero-firma">Seleccionar documento:</label><br>
					<input id="fichero-firma" type="file" name="sign-file" />
				</div>

				<div style="margin-top:30px;text-align: center; ">
					<input  id="submit-btn"  type="submit" value="FIRMAR">
				</div>
			</form>
		</div>
	</body>
</html>