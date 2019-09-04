
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.List" %>

<%@page import="es.gob.fire.server.admin.dao.ConfigurationDAO" %>
<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO" %>
<%@page import="es.gob.fire.server.admin.entity.CertificateFire" %>
<%@page import="es.gob.fire.server.admin.entity.Application" %>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>
<%@page import="es.gob.fire.server.admin.dao.RolesDAO"%>
<%@page import="es.gob.fire.server.admin.entity.Role"%>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO" %>
<%@page import="es.gob.fire.server.admin.entity.User" %>


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%

	if (session == null) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	final Object state = session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED);
	if (state == null || !Boolean.parseBoolean((String) state)) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	final String EMPTY = "";//$NON-NLS-1$
	String id = request.getParameter("id-cert");//$NON-NLS-1$
	String nameCert = request.getParameter("nombre-cert");//$NON-NLS-1$
	final int op = Integer.parseInt(request.getParameter("op"));//$NON-NLS-1$
	
	
	// op = 0 -> Solo lectura, no se puede modificar nada
	// op = 1 -> nueva aplicacion
	// op = 2 -> editar aplicacion
	String title = ""; //$NON-NLS-1$
	String subTitle = ""; //$NON-NLS-1$
	String certDataPrincipal = "";//$NON-NLS-1$
	String certDataBkup = "";//$NON-NLS-1$
	String b64CertPrin = "";//$NON-NLS-1$
	String b64CertBkup = "";//$NON-NLS-1$
	
	
	switch (op) {
		case 0:
			title = "Ver el certificado " + nameCert;//$NON-NLS-1$
			subTitle = "Visualizaci&oacute;n de los certificados"; //$NON-NLS-1$
			break;
		case 1:
			title = "Alta de nuevo certificado"; //$NON-NLS-1$
			subTitle = "Inserte los datos del nuevo certificado."; //$NON-NLS-1$
			break;
		case 2:
			title = "Editar el certificado " + nameCert; //$NON-NLS-1$
			subTitle = "Modifique los datos que desee editar"; //$NON-NLS-1$
			break;
		default:
			response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
			return;
	}
	// prueba Carlos
	User[] users = UsersDAO.getUserAppResponsables();

	 
	Application app = null;
	if (id != null) {
		try {
			app = AplicationsDAO.getApplicationWithCompleteInfo(id);
		} catch (Exception e) {
			response.sendRedirect("AdminMainPage.jsp");
		}
	}
	if (app == null) {
		app = new Application();
	}
	// fin prueba Carlos
	
	
	CertificateFire cer = null;
	if (id != null) {
		cer = CertificatesDAO.selectCertificateByID (id);
	}
	if (cer == null) {
		cer = new CertificateFire();
	}

	if (cer.getX509Principal() != null &&
			cer.getCertPrincipal() != null && !cer.getCertPrincipal().isEmpty()){
		b64CertPrin = cer.getCertPrincipal();
		final String[] datCertificate = (cer.getX509Principal().getSubjectX500Principal().getName()).split(",");	//$NON-NLS-1$
		for (int i = 0 ; i <= datCertificate.length-1; i++){
			certDataPrincipal += datCertificate[i] + "</br>";//$NON-NLS-1$
		}
		//fecha caducidad
		Date fecha = cer.getX509Principal().getNotAfter();		
		certDataPrincipal += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha);//$NON-NLS-1$
	}
	if (cer.getX509Backup() != null && !"".equals(cer.getX509Backup()) && //$NON-NLS-1$
			cer.getCertBackup() != null && !"".equals(cer.getCertBackup())){//$NON-NLS-1$
		b64CertBkup = cer.getCertBackup();
		final String[] datCertificate = (cer.getX509Backup().getSubjectX500Principal().getName()).split(",");	//$NON-NLS-1$
		for (int i = 0; i<= datCertificate.length-1; i++){
			certDataBkup += datCertificate[i] + "</br>"; //$NON-NLS-1$
		}
		//fecha caducidad
		Date fecha = new Date();
		fecha = cer.getX509Backup().getNotAfter();		
		certDataBkup += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha);//$NON-NLS-1$
	}

%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script>
	/*Importante estas variables (requestTypeCount y requestType) deben estar declaradas antes que la llamada a ../resources/js/certificate.js*/
	requestTypeCount="countRecordsCertApp&id-cert="+<%= id%>;
	requestType="getRecordsCertApp&id-cert="+<%= id%>;
	var op=<%= op%>;	
	$(function() {
	     $("input:file").change(function (event){
	    	 
	      var id=event.target.id;
	      var data = new FormData($("#frmCertificate")[0]);
	      var idResult;
	      if(id == "fichero-firma-prin"){
	    	  idResult="cert-prin";
	    	  $('#b64CertPrin').val(null);
      		}
      	  else if(id == "fichero-firma-resp"){
      			idResult = "cert-resp";
      			$('#b64CertBkup').val(null);
      	  }
	    	      
	      $.ajax({
	            type: "POST",
	            enctype: 'multipart/form-data',
	            url: "../previewCert?op="+<%= op%>+"&id="+id,
	            data: data,
	            processData: false,
	            contentType: false,
	            success: function (responseText) {
	            	document.getElementById(idResult).innerHTML = responseText;	
	            },
	            error: function (e) {
	            	alert("Error al cargar el certificado");
	            }
	        });
	      
	     });
	          
	  });
	/*Borrar datos certificado en Editar*/									
	function cleanCertificate(idInputfile,idContainer,idb64Cert){
		var $fileupload = $("#"+idInputfile);
		$fileupload.wrap('<form>').closest('form').get(0).reset();
		$fileupload.unwrap();
		$("#"+idContainer).empty();
		$("#"+idb64Cert).val("");
		
	}
	/* Descargar Certificado */
	function downloadCertificate(contentBlob, fileName) {
		var reader = new FileReader();
		reader.onload = function(event) {
			var save = document.createElement('a');
			save.href = event.target.result;
			save.target = '_blank';
			save.download = fileName || 'certificado.cer';
			var clicEvent = new MouseEvent('click', {
				'view' : window,
				'bubbles' : true,
				'cancelable' : true
			});
			save.dispatchEvent(clicEvent);
			(window.URL || window.webkitURL)
					.revokeObjectURL(save.href);
			console.log('carlos spring')
		};
		reader.readAsDataURL(contentBlob);
	}
	;

	// Genera un objeto Blob con los datos del certificado en
	// base64
	function generateText(certB64) {
		var text = [];
		text.push(certB64);
		return new Blob(text, {
			type : 'text/plain'
		});
	};

	function downLoadCert(idDataCert,numCert){
		var datCert = $("#"+idDataCert).html();
		downloadCertificate(generateText(datCert), $("#nombre-cer").val() + '_' + numCert+ '.cer');
	}
	
	</script>
	
	<script src="../resources/js/validateCertificates.js" type="text/javascript"></script>
</head>

<body>
	<!-- Barra de navegacion -->		
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	<!-- contenido -->
	<div id="container">
		
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  <%= subTitle %>
			</p>
		</div>			
		<p>Los campos con * son obligatorios
		<%if(op != 0){%>
		<br>
			Los campos con [*] al menos uno es obligatorio
		<%} %>
		</p>
			<form id="frmCertificate" method="POST" autocomplete="off" action="../newCert?op=<%= op%>&id-cert=<%= cer.getId() != null ? cer.getId() : EMPTY %>" enctype="multipart/form-data" onsubmit="isCert()">
							
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-cer" style="color: #404040">* Nombre de certificado</label>
				</div>
				<div  style="display: inline-block; width: 78%;margin: 3px;">
						<input id="nombre-cer" class="edit-txt" type="text" name="nombre-cer" style="width: 80%;margin-top:3px;" 
						value="<%= request.getParameter("nombre-cert") != null ? request.getParameter("nombre-cert") : EMPTY %>"> 
				</div>
										
			</div>				
			<!-- -FILE -->
			
			<div style="margin: auto;width: 100%;padding: 3px;">
				<%if(op != 0){ %>
				<div style="display: inline-block; width: 48%;margin: 3px;">
					<div>
						<div style="display: inline-block;">
							<label for="fichero-firma-prin" style="color: #404040">[*] Certificado 1</label>
						</div>				
						<div  style="display: inline-block; width:52%;margin: 3px;">
							<input id="fichero-firma-prin" type="file" name="fichero-firma-prin" accept=".cer"/>										
						</div>
						<%if(op == 2){ %>
						<input type="hidden" id="b64CertPrin" name="b64CertPrin" value="<%= b64CertPrin%>">
						
						<div  style="display: inline-block; width: 20%;margin:3px;">
							<input id="cleanCertPrin-button" class="btn-borrar-cert" name="cleanCertPrin-button" 
							type="button" value="Borrar" title="Borrar el certificado" onclick="cleanCertificate('fichero-firma-prin','cert-prin','b64CertPrin');" />
						</div>
						<%} %>	
					</div>											
					<div id="cert-prin" name="cert-prin" class="edit-txt" style="width: 90%;height:8em;overflow-y: auto;margin-top:3px;resize:none">
						<%if (certDataPrincipal != null && !certDataPrincipal.isEmpty()) { %>
							<p><%= certDataPrincipal%></p>							
						<%}%>						
					</div>
				</div>
				<div style="display: inline-block; width: 48%;margin: 3px;">
					<div>
						<div style="display: inline-block;">
							<label for="fichero-firma-resp"  style="color: #404040">[*] Certificado 2</label>
						</div>
						<div  style="display: inline-block; width: 52%;margin: 3px;">
							<input id="fichero-firma-resp" type="file" name="fichero-firma-resp" accept=".cer"/>										
						</div>
						<%if(op == 2){ %>
						<input type="hidden" id="b64CertBkup" name="b64CertBkup" value="<%= b64CertBkup%>">
							
						<div  style="display: inline-block; width: 20%;margin: 3px;">						
							<input id="cleanCertResp-button" class="btn-borrar-cert" name="cleanCertResp-button"
							type="button" value="Borrar" title="Borrar el certificado" onclick="cleanCertificate('fichero-firma-resp','cert-resp','b64CertBkup')" />
						</div>
						<%} %>
					</div>
								
					<div id="cert-resp" name="cert-resp" class="edit-txt" style="width: 90%;height:8em;overflow-y: auto;margin-top:3px;resize:none">
						<%if(certDataBkup != null && !certDataBkup.isEmpty()) { %>
						
							<p><%= certDataBkup %></p>						
						<%}%>
					</div>					
				</div>			
				<%}else{%>
				<div style="display: inline-block; width: 48%;margin: 3px;">
					<div>
						<div style="display: inline-block;width: 75%;">
							<label for="cert-prin" style="color: #404040">[*] Certificado 1</label>
						</div>				
									
						<div  style="display: inline-block; width: 20%;margin:3px;">
							<textarea style="display:none;" id="b64CertPrin" name="b64CertPrin"><%= b64CertPrin%></textarea>
							<%if(certDataPrincipal != null && !certDataPrincipal.isEmpty()){ %>	
							<input id="CertPrincipal-button" class="btn-borrar-cert" name="add-usr-btn" type="button" value="Descargar .cer" 
							title="Descargar certificado en fichero con formato .cer" onclick="downLoadCert('b64CertPrin','1')" />
							<%}%>
						</div>
						
					</div>											
					<div id="cert-prin" name="cert-prin" class="edit-txt" style="width: 90%;height:8em;overflow-y: auto;margin-top:3px;resize:none">
						<%if(certDataPrincipal != null && !certDataPrincipal.isEmpty()){ %>
							<p><%= certDataPrincipal %></p>							
						<%}%>						
					</div>
				</div>
				<div style="display: inline-block; width: 48%;margin: 3px;">
					<div>
						<div style="display: inline-block;width: 75%;">
							<label for="cert-resp"  style="color: #404040">[*] Certificado 2</label>
						</div>															
						<div  style="display: inline-block; width: 20%;margin: 3px;">						
							<textarea style="display:none;" id="b64CertBkup" name="b64CertBkup"><%= b64CertBkup%></textarea>
							<%if(certDataBkup != null && !certDataBkup.isEmpty()){ %>	
							<input id="CertBkup-button" class="btn-borrar-cert" name="add-usr-btn" type="button" value="Descargar .cer" title="Descargar certificado en fichero con formato .cer" onclick="downLoadCert('b64CertBkup','2')" />
							<%}%>
						</div>					
					</div>				
					<div id="cert-resp" name="cert-resp" class="edit-txt" style="width: 90%;height:8em;overflow-y: auto;margin-top:3px;resize:none">
						<%if(certDataBkup != null && !"".equals(certDataBkup)){ %>
							<p><%= certDataBkup %></p>						
						<%}%>
					</div>					
				</div>			
				
				<%} %>																							
			</div>
			<% 
		   	if (op > 0) {
		   			final String msg = (op == 1 ) ? "Crear certificado" : "Guardar cambios";   //$NON-NLS-1$ //$NON-NLS-2$
					final String tit= (op == 1 ) ? "Crea nuevo certificado" : "Guarda las modificaciones realizadas";//$NON-NLS-1$ //$NON-NLS-2$
					
		   	%>	
			<fieldset class="fieldset-clavefirma" >			
			   	<div style="margin: auto;width: 60%;padding: 3px; margin-top: 5px;">
					<div style="display: inline-block; width: 45%;margin: 3px;">
						<input class="menu-btn" name="return-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de administraci&oacute;n" onclick="location.href='CertificatePage.jsp'"/>
					</div>
				   	<div  style="display: inline-block; width: 35%;margin: 3px">
				   		<input class="menu-btn" name="save-btn" type="submit" value="<%= msg %>" title="<%= tit %>">
				   	</div>			   						   		
			   	</div>	
			</fieldset>
			<% } %>					
		</form>
		
		<% if(op == 0){ %>
		<br>
		
		<fieldset>
			<legend>Aplicaciones</legend>
			<div id="data" style="display: block-inline; text-align:center; overflow-y: auto;margin-top:3px;resize:none">
				<h4>No hay Aplicaciones asociadas al certificado <%=nameCert%> </h4>		
			</div>

		</fieldset>
		
		<fieldset class="fieldset-clavefirma">			
			<div style="margin: auto;width: 60%;padding: 3px; margin-top: 5px;">
				<div style="display: inline-block; width: 45%;margin: 3px;">
					<input class="menu-btn" name="add-usr-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de administraci&oacute;n" onclick="location.href='CertificatePage.jsp'"/>
				</div>
			</div>	
		</fieldset>
		<%} %>
	
		<script>
			//bloqueamos los campos en caso de que sea una operacion de solo lectura
			document.getElementById("nombre-cer").disabled = <%= op == 0 %>
			
			if(op == 0){
				document.getElementById("nombre-cer").style.background = '#F5F5F5';
				document.getElementById("cert-prin").style.background = '#F5F5F5';
				document.getElementById("cert-resp").style.background = '#F5F5F5';
			}else if(op == 1 || op == 2){
				
				document.getElementById("cert-prin").style.background = '#F5F5F5';
				document.getElementById("cert-resp").style.background = '#F5F5F5';
				
			}
					
			// quitamos los espacios en blanco que se han agregado en el certificado
			//document.getElementById("cert-prin").value = document.getElementById("cert-prin").value.trim();
			if (<%= Boolean.parseBoolean(request.getParameter("error")) %>){ 
				alert('El certificado introducido no es correcto, por favor introduzca un certificado valido');
				document.getElementById("fichero-firma-prin").focus();
			}
			
		</script>
   	</div>
</body>
<script src="../resources/js/certificate.js" type="text/javascript"></script>
</html>