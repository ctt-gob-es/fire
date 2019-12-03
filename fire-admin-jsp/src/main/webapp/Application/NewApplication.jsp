
<%@page import="es.gob.fire.server.admin.tool.Base64"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.List" %>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%@page import="es.gob.fire.server.admin.entity.Application" %>
<%@page import="es.gob.fire.server.admin.entity.CertificateFire"%>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>
<%@page import="es.gob.fire.server.admin.dao.UsersDAO" %>
<%@page import="es.gob.fire.server.admin.dao.RolesDAO"%>
<%@page import="es.gob.fire.server.admin.entity.Role"%>
<%@page import="es.gob.fire.server.admin.entity.User" %>


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	if (session == null || !Boolean.parseBoolean(
			(String) session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED))) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	String id = request.getParameter(ServiceParams.PARAM_APPID);
	final int op = Integer.parseInt(request.getParameter(ServiceParams.PARAM_OP));
	final String name = request.getParameter(ServiceParams.PARAM_NAME);
	final String EMPTY = ""; //$NON-NLS-1$
	final List<CertificateFire> lCert = CertificatesDAO.selectCertificateAll();

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
		title = "Ver la aplicaci&oacute;n " + id;//$NON-NLS-1$
		subTitle = "Visualizaci&oacute;n de las aplicaciones y su responsable"; //$NON-NLS-1$
		break;
	case 1:
		title = "Alta de nueva aplicaci&oacute;n"; //$NON-NLS-1$
		subTitle = "Inserte los datos de la nueva aplicaci&oacute;n."; //$NON-NLS-1$
		break;
	case 2:
		title = "Editar la aplicaci&oacute;n " + id; //$NON-NLS-1$
		subTitle = "Modifique los datos que desee editar"; //$NON-NLS-1$
		break;

	default:
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	User[] usersResponsable = UsersDAO.getUserAppResponsables();

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

	CertificateFire cert = null;
	if (id != null) {
		cert = CertificatesDAO.selectCertificateByID(id);
	}
	if (cert == null) {
		cert = app.getCertificate();
	}
	if (app.getCertificate() != null) {
		if (cert != null && cert.getX509Principal() != null) {
			b64CertPrin = Base64.encode(cert.getX509Principal().getEncoded());
			final String[] datCertificate = cert.getX509Principal().getSubjectX500Principal().getName()
					.split(",");//$NON-NLS-1$
			for (int i = 0; i < datCertificate.length; i++) {
				certDataPrincipal += datCertificate[i] + "</br>";//$NON-NLS-1$
			}
			// Fecha caducidad
			Date fecha = cert.getX509Principal().getNotAfter();
			certDataPrincipal += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha);//$NON-NLS-1$
		}
		if (cert != null && cert.getX509Backup() != null) {
			b64CertBkup = Base64.encode(cert.getX509Backup().getEncoded());
			final String[] datCertificate = cert.getX509Backup().getSubjectX500Principal().getName().split(",");//$NON-NLS-1$
			for (int i = 0; i < datCertificate.length; i++) {
				certDataBkup += datCertificate[i] + "</br>"; //$NON-NLS-1$
			}
			// Fecha caducidad
			Date fecha = cert.getX509Backup().getNotAfter();
			certDataBkup += "Fecha de Caducidad = " + Utils.getStringDateFormat(fecha);//$NON-NLS-1$
		}
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
	<script>var op=<%=  op%>;	
		$(function() {
			$("#id-certificate").change(function (){
		    	var opSel="0";
		    	$("#cert-prin").empty();
	      		$("#cert-resp").empty();
		      	$("select option:selected").each(function(){
		      		opSel=$(this).val();
		      	});
		      	if(opSel!="0"){
		      		$.get("application?requestType=getCertificateId&id-cert=" + opSel, function(data){
			      		var certificados=data.split("$*$");		      		
			      		if(certificados[0] != null && typeof certificados[0] != "undefined" && certificados[0].trim() != "--"){		      			
			      			$("#cert-prin").html(certificados[0]);
			      		}
			      		if(certificados[1] != null && typeof certificados[1] != "undefined" && certificados[1].trim() != "--"){		      			
			      			$("#cert-resp").html(certificados[1]);
			      		}
			      	});
		      	}		      			      	
			});
		});
		
		
		
		function downLoadCert(idDataCert, numCert) {
			var datCert = $("#" + idDataCert).html();
			var filename = $("#id-certificate").val() + "_" + numCert + ".cer";
			downloadCertificate(
					generateText(datCert), filename);
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
		};
		// Genera un objeto Blob con los datos del certificado en
		// base64
		function generateText(certB64) {
			var text = [];
			text.push(certB64);
			return new Blob(text, {
				type : 'text/plain'
			});
		};

		
			
		
	</script>
	<script src="../resources/js/validateAplications.js" type="text/javascript"></script>
</head>

<body>
	<!-- Barra de navegacion -->		
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	<!-- contenido -->
	<div id="container">
		
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  <%=  subTitle %>
			</p>
		</div>	
		
		
		<% if (op == 2 || op == 1) { %>	
		<p>Los campos con * son obligatorios</p>
			<form id="frmApplication" name="frmApplication" method="POST" autocomplete="off" action="../newApp" >
			
			<input type="hidden" name="<%= ServiceParams.PARAM_APPID %>" value="<%=  id %>" />
			<input type="hidden" name="<%= ServiceParams.PARAM_OP %>" value="<%=  op %>" />  
			
			<% if (op == 2){ %>
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
			
             Deshabilitar aplicación: 
             <input type="checkbox" id="habilitado" name="<%= ServiceParams.PARAM_ENABLED %>"  onclick="myFunctionEnableDilable()" 
            <%= app.isHabilitado() ? "" : "checked" %>> 

               <p id="text" style="display:none">La aplicacion se ha habilitado</p>
              
               
               </div>
			</div>
			
							<% }%>
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-app" style="color: #404040">* Nombre de aplicaci&oacute;n</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					
						<input id="nombre-app" class="edit-txt" type="text" autocomplete="off" name=<%= ServiceParams.PARAM_NAME %> style="width: 80%;margin-top:3px;" 
						value="<%= (name != null) ? name : (app.getNombre()!=null)?app.getNombre() : "" %>"> 
						
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="listresp" style="color: #404040" ></label>
				</div>
				<div  style="display:flex; width: 30%;margin: 3px;">
				
				
				<% if (op != 0) { %>
					
 				<div  style='float: left; margin: 30px; padding: 5px; text-align: left;'>
					<label for="pasarOpciones(this.form)" >Listado de responsables del sistema</label>
						<select name="listadoCompletoResponsables[]"  style="width:400px" multiple="multiple" id="listadoCompletoResponsables" size="8" >
						<% for (User responsable : usersResponsable) { %>
							<option value="<%= responsable.getId() %>"> <%= responsable.getSurname() + "," + " " +responsable.getFirstName()  + " " + "(" + responsable.getUserName() + ")"%></option>
						<% } %>
						</select>
						
						
				</div>
				
				<div style='float: center; margin: 30px; padding: 5px;'>
					<br></br>
					<input type="button" id="moverright"  value="Añadir »" >
				
					<br></br>
					<input type="button" id="moverleft"    value="« Eliminar" >
				
				</div>
				
				
				<div  class="" id ="buscar" style='float: left; margin: 30px; padding: 5px; text-align: left;'>
					<label for="listadoAplicacionResponsables" display: inline;>Listado de responsables de la aplicaci&oacute;n</label>
						<select  style="width:400px" multiple="multiple"  id="listadoAplicacionResponsables" name="<%= ServiceParams.PARAM_RESPONSABLES %>"  size="8" >
						<% for (User responsable : app.getResponsables()) { %>
							<option value="<%= responsable.getId() %>"> <%= responsable.getSurname() + "," + " " +responsable.getFirstName()  + " " + "(" + responsable.getUserName() + ")"%></option>
						<% } %>
						
						</select>
					</div>
				</div>
		
					<% } else { %>
				<fieldset>
				<legend>Responsables:</legend>
					<table>
						<thead>
							<tr>
							 <div>
							<label><th>Nombre</th></label>
							 </div>
							 <br/>
							  <div>
							<<label> <th>Correo Electr&oacute;nico</th></label>
							 </div>
							 <br/>
							  <div>
							<<label><th>Tel&eacute;fono</th></label>
							 </div>
							</tr>
						</thead>
						<% for (User user : app.getResponsables()) { %>
							<tr><td><%= user.getSurname() + "," + user.getFirstName() %></td><td><%= user.getMail() %></td><td><%= user.getTelephone() %></td></tr>
						<% } %>
					
				<% } %>
				</fieldset>				
			</table>					
			</div>	
			
							
			<div style="margin: auto;width: 100%;padding: 3px;">
				
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<label for="id-certificate" style="color: #404040;">* Seleccionar certificado</label><br>	
					</div>	
					<div  style="display: inline-block; width: 30%;margin: 3px;">				
					<%if (op != 0){ %>							
						<select id="id-certificate" name=<%= ServiceParams.PARAM_CERTID %> class="edit-txt">
						<% if (op == 1) { %>
							<option value="0">Seleccione un certificado</option>
						<% } %>
						<% for (CertificateFire cer : lCert) { 
							if (op == 1) { %>													
								<option value="<%= cer.getId() %>"><%= cer.getNombre() %></option>  
							<% } else { %>
								<option value="<%= cer.getId() %>" <%= cer.getId().equals(app.getCertificate().getId()) ? "selected='selected'" : "" %>><%= cer.getNombre() %></option>  
							<% }
						} %>			    		  			   
				  		</select>				  					
					<%}else{%>						
						<input id="id-certificate" class="edit-txt" type="text" name=<%= ServiceParams.PARAM_CERTID %> style="width: 80%;margin-top:3px;" 
						value="<%= app.getCertificate().getNombre() %>" />																								
					<%} %>			
					</div>
			
				</div>
				
				
				<div style="margin: auto;width: 100%;padding: 3px;">
					<% if (op == 2 || op == 1) { %>
				
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-prin" style="color: #404040">Certificado 1</label>
							</div>															
						</div>											
						<div id="cert-prin" name="cert-prin" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<% if (certDataPrincipal != null && !certDataPrincipal.isEmpty()) { %>
								<p><%= certDataPrincipal %></p>							
							<% } %>						
						</div>
					</div>
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-resp"  style="color: #404040">Certificado 2</label>
							</div>																					
						</div>				
						<div id="cert-resp" name="cert-resp" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<% if (certDataBkup != null && !certDataBkup.isEmpty()) { %>
								<p><%= certDataBkup %></p>						
							<% } %>
						</div>					
					</div>
				<
			<% } else{%>
			
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-prin" style="color: #404040">Certificado 1</label>
							</div>
							<div  style="display: inline-block; width: 20%;margin:3px;">
								<textarea style="display:none;" id="b64CertPrin" name="b64CertPrin"><%= b64CertPrin %></textarea>
								<%if(certDataPrincipal != null && !certDataPrincipal.isEmpty()){ %>	
								<input id="CertPrincipal-button" class="btn-borrar-cert" name="add-usr-btn" type="button" value="Descargar .cer" 
								title="Descargar certificado en fichero con formato .cer" onclick="downLoadCert('b64CertPrin','1')" />
								<%}%>
							</div>															
						</div>	
						
						
																
						<div id="cert-prin" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<% if (certDataPrincipal != null && !certDataPrincipal.isEmpty()) { %>
								<p><%= certDataPrincipal %></p>							
							<% } %>						
						</div>
					</div>
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-resp"  style="color: #404040">Certificado 2</label>
							</div>	
						
							<div  style="display: inline-block; width: 20%;margin: 3px;">
												
								<textarea style="display:none;" id="b64CertBkup" name="b64CertBkup"><%= b64CertBkup%></textarea>
								<%if(certDataBkup != null && !certDataBkup.isEmpty()){ %>	
								<input id="CertBkup-button" class="btn-borrar-cert" name="add-usr-btn" type="button" value="Descargar .cer" title="Descargar certificado en fichero con formato .cer" onclick="downLoadCert('b64CertBkup','2')" />
								<%}%>
							</div>	
																								
						</div>				
						<div id="cert-resp" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<% if (certDataBkup != null && !certDataBkup.isEmpty()) { %>
								<p><%= certDataBkup %></p>						
							<% } %>
						</div>					
					</div>
								
			<% } %>	
			</div>										
			<fieldset class="fieldset-clavefirma" >			
		   	<div style="margin: auto;width: 60%;padding: 3px; margin-top: 5px;">
				<div style="display: inline-block; width: 45%;margin: 3px;">
				<input class="menu-btn" name="add-usr-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de administraci&oacute;n" onclick="location.href='AdminMainPage.jsp'"/>

<!-- 					<a class="menu-btn" href="AdminMainPage.jsp" >Volver a la p&aacute;gina de administraci&oacute;n</a> -->
				</div>
		   		<% 
		   		if (op > 0) {
		   			final String msg = (op == 1 ) ? "Crear aplicaci&oacute;n" : "Guardar cambios";   //$NON-NLS-1$ //$NON-NLS-2$
					final String tit= (op == 1 ) ? "Crea nueva aplicación" : "Guarda las modificaciones realizadas";   //$NON-NLS-1$ //$NON-NLS-2$
		   		%>
			   		
			   		<div  style="display: inline-block; width: 35%;margin: 3px">
			   			<input class="menu-btn" name="add-app-btn"type="button" onclick="selectResponsables()" value="<%= msg %>" title="<%= tit %>" >
			   		</div>
		   		<% } %>
			   					   		
		   	</div>	
		</fieldset>
		
		
		<% }else { %>	
		<p>Los campos con * son obligatorios</p>
			<form id="frmApplication" method="POST"  autocomplete="off" action="../newApp" onsubmit="isCert()" >
			
			<input type="hidden" name="<%= ServiceParams.PARAM_APPID %>" value="<%=  id %>" />
			<input type="hidden" name="<%= ServiceParams.PARAM_OP %>" value="<%=  op %>" />  
							
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-app" style="color: #404040">* Nombre de aplicaci&oacute;n</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					
						<input id="nombre-app" class="edit-txt" type="text" autocomplete="off" name="nombre-app" style="width: 80%;margin-top:3px;" 
						value="<%= (request.getParameter("name") != null) ? request.getParameter("name") : (app.getNombre()!=null)?app.getNombre() : "" %>"> 
						
				</div>
					
				
				
			
			<div style="margin: auto;width: 100%;padding: 3px;">
				
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<label for="id-certificate" style="color: #404040;">* Seleccionar certificado</label><br>	
					</div>	
					<div  style="display: inline-block; width: 30%;margin: 3px;">				
					<%if (op != 0){ %>							
						<select id="id-certificate" name=<%= ServiceParams.PARAM_CERTID %> class="edit-txt">
						<% if (op == 1) { %>
							<option value="0">Seleccione un certificado</option>
						<% } %>
						<% for (CertificateFire cer : lCert) { 
							if (op == 1) { %>													
								<option value="<%= cer.getId() %>"><%= cer.getNombre() %></option>  
							<% } else { %>
								<option value="<%= cer.getId() %>" <%= cer.getId().equals(app.getCertificate().getId()) ? "selected='selected'" : "" %>><%= cer.getNombre() %></option>  
							<% }
						} %>			    		  			   
				  		</select>				  					
					<%}else{%>						
						<input id="id-certificate" class="edit-txt" type="text" name="id-certificate" style="width: 80%;margin-top:3px;" 
						value="<%= app.getCertificate().getNombre() %>" />																								
					<%} %>			
					</div>
			
				</div>
			
				<div style="margin: auto;width: 100%;padding: 3px;">
			<% if (op == 2 || op == 1) { %>
				
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-prin" style="color: #404040">Certificado 1</label>
							</div>															
						</div>											
						<div id="cert-prin" name="cert-prin" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<% if (certDataPrincipal != null && !certDataPrincipal.isEmpty()) { %>
								<p><%= certDataPrincipal %></p>							
							<% } %>						
						</div>
					</div>
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-resp"  style="color: #404040">Certificado 2</label>
							</div>																					
						</div>				
						<div id="cert-resp" name="cert-resp" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<% if (certDataBkup != null && !certDataBkup.isEmpty()) { %>
								<p><%= certDataBkup %></p>						
							<% } %>
						</div>					
					</div>
				<
			<% } else{%>
			
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-prin" style="color: #404040">Certificado 1</label>
							</div>
							<div  style="display: inline-block; width: 20%;margin:3px;">
								<textarea style="display:none;" id="b64CertPrin" name="b64CertPrin"><%= b64CertPrin%></textarea>
								<%if(certDataPrincipal != null && !certDataPrincipal.isEmpty()){ %>	
								<input id="CertPrincipal-button" class="btn-borrar-cert" name="add-usr-btn" type="button" value="Descargar .cer" 
								title="Descargar certificado en fichero con formato .cer" onclick="downLoadCert('b64CertPrin','1')" />
								<%}%>
							</div>															
						</div>	
						
						
																
						<div id="cert-prin" name="cert-prin" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<% if (certDataPrincipal != null && !certDataPrincipal.isEmpty()) { %>
								<p><%= certDataPrincipal %></p>							
							<% } %>						
						</div>
					</div>
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-resp"  style="color: #404040">Certificado 2</label>
							</div>	
						
							<div  style="display: inline-block; width: 20%;margin: 3px;">
												
								<textarea style="display:none;" id="b64CertBkup" name="b64CertBkup"><%= b64CertBkup%></textarea>
								<%if(certDataBkup != null && !certDataBkup.isEmpty()){ %>	
								<input id="CertBkup-button" class="btn-borrar-cert" name="add-usr-btn" type="button" value="Descargar .cer" title="Descargar certificado en fichero con formato .cer" onclick="downLoadCert('b64CertBkup','2')" />
								<%}%>
							</div>	
																								
						</div>				
						<div id="cert-resp" name="cert-resp" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<% if (certDataBkup != null && !certDataBkup.isEmpty()) { %>
								<p><%= certDataBkup %></p>						
							<% } %>
						</div>					
					</div>
								
			<% } %>	
			</div>	
			</div>					
					<div   style="display: inline-block; width: 93%;margin: 10px;">
				
				
				<% if (op != 0) { %>
					
				<label for="listadoAplicacionResponsables">Listado de responsables de la aplicaci&oacute;n</label>
					<select multiple id="listadoAplicacionResponsables" name="listadoAplicacionResponsables">
					<% for (User responsable : app.getResponsables()) { %>
						<option value="<%= responsable.getId() %>"> <%= responsable.getSurname() + "," + " " +responsable.getFirstName()  + " " + "(" + responsable.getUserName() + ")"%></option>
					<% } %>
					</select>

		
				<% } else { %>
				<fieldset  style="text-align: left;">
				<legend>Responsables</legend>
					<table width="100%"  border="1" style="margin: 0 auto;">
						<thead>
							<tr>
							<label><th >Nombre</th></label>
							<label><th>Correo Electr&oacute;nico</th></label>
							<label><th>Tel&eacute;fono</th></label>
							</tr>
						</thead>
						<% for (User user : app.getResponsables()) { %>
							<tr>
								<td align="center"><%= user.getSurname() + " " + user.getFirstName() %></td>
								<td align="center"><%= user.getMail() %></td>
								<td align="center"><%=user.getTelephone() != null ? user.getTelephone(): EMPTY %></td>
							</tr>
						<% } %>
					</table>	
			</fieldset>
				<% } %>
							
								
					
								
			</div>	
			
										
			<fieldset class="fieldset-clavefirma" >			
		   	<div style="margin: auto;width: 60%;padding: 3px; margin-top: 5px;">
				<div style="display: inline-block; width: 45%;margin: 3px;">
				<input class="menu-btn" name="add-usr-btn" type="button" value="Volver" title="Volver a la p&aacute;gina de administraci&oacute;n" onclick="location.href='AdminMainPage.jsp'"/>
<!-- 					<a class="menu-btn" href="AdminMainPage.jsp" >Volver a la p&aacute;gina de administraci&oacute;n</a> -->
				</div>
		   		<% 
		   		if (op > 0) {
		   			final String msg = (op == 1 ) ? "Crear aplicaci&oacute;n" : "Guardar cambios";   //$NON-NLS-1$ //$NON-NLS-2$
					final String tit= (op == 1 ) ? "Crea nueva aplicación" : "Guarda las modificaciones realizadas";   //$NON-NLS-1$ //$NON-NLS-2$
		   		%>
			   		
			   		<div  style="display: inline-block; width: 35%;margin: 3px">
			   			<input class="menu-btn" name="add-app-btn" type="button" onclick="selectResponsables()"  value="<%= msg %>" title="<%= tit %>" >
			   		</div>
		   		<% } %>
			   					   		
		   	</div>	
		</fieldset>
<% } %>
		</form>
		<script>

			// bloqueamos los campos en caso de que sea una operacion de solo lectura
			document.getElementById("nombre-app").disabled = <%=  op == 0 %>;
			document.getElementById("id-certificate").disabled = <%=  op == 0 %>;
			document.getElementById("cert-prin").disabled = <%=  op == 0 %>;
			document.getElementById("cert-resp").disabled = <%=  op == 0 %>;
			
			
			// bloqueamos los campos necesarios y cambiamos de color
			 if (op == 2) { 
			
				
				document.getElementById("cert-prin").style.background = '#F5F5F5';
				document.getElementById("cert-resp").style.background = '#F5F5F5';

				// cambiamos el color a los campos bloqueados de edicion
			 } else if (op == 0){
				 document.getElementById("nombre-app").style.background = '#F5F5F5';
					document.getElementById("id-certificate").style.background = '#F5F5F5';
					document.getElementById("cert-prin").style.background = '#F5F5F5';
					document.getElementById("cert-resp").style.background = '#F5F5F5';
					
			 } else if (op == 1){
					
					document.getElementById("cert-prin").style.background = '#F5F5F5';
					document.getElementById("cert-resp").style.background = '#F5F5F5';

			 }
			 else {
				 document.getElementById("cert-prin").style.background = '#F5F5F5';
					document.getElementById("cert-resp").style.background = '#F5F5F5';
			 }
			
			// funcion para deshabilitar una aplicacion
			 function myFunctionEnableDilable() {
				  var checkBox = document.getElementById("habilitado");
				  var text = document.getElementById("text");
				  
				  
				  if (checkBox.checked == false && op == 1 || op == 2){
				    //text.style.display = "none";
				 
				    
				  } else {
				    // text.style.display = "block";
				   
				  }
			}
				
			
	
				//función para el guardado del formulario
				
			function selectResponsables() {
				var sel = $("#listadoAplicacionResponsables");
				for (var i = 0; i < sel[0].length; ++i) {
					sel[0].options[i].selected = true;
				}
				     
			        $("#frmApplication").submit();
			}
			
			
			//función para añadir o eliminar los usuarios responsables de las cajas de texto
			
			(function () {
			    $('#moverright').click(function (e) {
			        var selectedOpts = $('#listadoCompletoResponsables option:selected');
			        if (selectedOpts.length == 0) {
			            alert("Nada que mover.");
			            e.preventDefault();
			        }
			        $('#listadoAplicacionResponsables').append($(selectedOpts).clone());
			        $(selectedOpts).remove();
			        e.preventDefault();
			    });


			    $('#moverleft').click(function (e) {
			        var selectedOpts = $('#listadoAplicacionResponsables option:selected');
			        if (selectedOpts.length == 0) {
			            alert("Nada que mover.");
			            e.preventDefault();
			        }
			        $('#listadoCompletoResponsables').append($(selectedOpts).clone());
			        $(selectedOpts).remove();
			        e.preventDefault();
			    });
			    
			}(jQuery));
			
			</script>	

   	</div>
</body>
</html>
