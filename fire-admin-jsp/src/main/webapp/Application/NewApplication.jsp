
<%@page import="es.gob.fire.server.admin.dao.CertificatesDAO"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.List" %>
<%@page import="es.gob.fire.server.admin.dao.ConfigurationDAO" %>
<%@page import="es.gob.fire.server.admin.dao.AplicationsDAO" %>
<%@page import="es.gob.fire.server.admin.entity.Application" %>
<%@page import="es.gob.fire.server.admin.entity.CertificateFire"%>
<%@page import="es.gob.fire.server.admin.tool.Utils" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	final Object state = request.getSession().getAttribute("initializedSession"); //$NON-NLS-1$
	if (state == null || !Boolean.parseBoolean((String) state)) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	String id = request.getParameter("id-app");//$NON-NLS-1$
	final int op = Integer.parseInt(request.getParameter("op"));//$NON-NLS-1$
	final List<CertificateFire> lCert = CertificatesDAO.selectCertificateALL();
	CertificateFire cert=null;
	// op = 0 -> Solo lectura, no se puede modificar nada
	// op = 1 -> nueva aplicacion
	// op = 2 -> editar aplicacion
	String title = ""; //$NON-NLS-1$
	String subTitle = ""; //$NON-NLS-1$
	String certDataPrincipal="";
	String certDataBkup="";
	Application app;
	switch (op) {
		case 0:
			title = "Ver la aplicaci&oacute;n " + id;//$NON-NLS-1$
			subTitle = ""; //$NON-NLS-1$
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
	app = id != null ? AplicationsDAO.selectApplication(id): new Application();
		
	if(app.getFk_certificado()!=null && !"".equals(app.getFk_certificado())){
		cert=CertificatesDAO.selectCertificateByID(app.getFk_certificado());
		if(cert.getCertX509_principal()!=null){
			final String[] datCertificate=cert.getCertX509_principal().getSubjectX500Principal().getName().split(",");
			for (int i=0;i<datCertificate.length;i++){
				certDataPrincipal=certDataPrincipal.concat(datCertificate[i]).concat("</br>");
			}
			//fecha caducidad
			Date fecha= new Date();
			fecha=cert.getCertX509_principal().getNotAfter();		
			certDataPrincipal=certDataPrincipal.concat("Fecha de Caducidad = ").concat(Utils.getStringDateFormat(fecha));
		}
		if(cert.getCertX509_backup()!=null){
			final String[] datCertificate=cert.getCertX509_backup().getSubjectX500Principal().getName().split(",");
			for (int i=0;i<datCertificate.length;i++){
				certDataBkup=certDataBkup.concat(datCertificate[i]).concat("</br>");
			}
			//fecha caducidad
			Date fecha= new Date();
			fecha=cert.getCertX509_backup().getNotAfter();		
			certDataBkup=certDataBkup.concat("Fecha de Caducidad = ").concat(Utils.getStringDateFormat(fecha));
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
	<script>var op=<%=op%>;	
		$(function() {
			$("select").change(function (){
		    	var opSel="0";
		    	$("#cert-prin").empty();
	      		$("#cert-resp").empty();
		      	$("select option:selected").each(function(){
		      		opSel=$(this).val();
		      	});
		      	if(opSel!="0"){
		      		$.get("processAppRequest.jsp?requestType=getCertificateId&id-cert="+opSel, function(data){	      		
			      		var certificados=data.split("§");		      		
			      		if(certificados[0]!=null && typeof certificados[0]!="undefined" && certificados[0].trim()!="--"){		      			
			      			$("#cert-prin").html(certificados[0]);
			      		}
			      		if(certificados[1]!=null && typeof certificados[1]!="undefined" && certificados[1].trim()!="--"){		      			
			      			$("#cert-resp").html(certificados[1]);
			      		}
			      	});
		      	}		      			      	
			});	          
		});
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
			  <%= subTitle %>
			</p>
		</div>			
		<p>Los campos con * son obligatorios</p>
			<form id="frmApplication" method="POST" action="../newApp?iddApp=<%= id %>&op=<%= op %>"  onsubmit="isCert()">
							
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-app" style="color: #404040">* Nombre de aplicaci&oacute;n</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					
						<input id="nombre-app" class="edit-txt" type="text" name="nombre-app" style="width: 80%;margin-top:3px;" 
						value="<%=(request.getParameter("name")!= null) ? request.getParameter("name") : (app.getNombre()!=null)?app.getNombre():"" %>"> 
						
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="nombre-resp" style="color: #404040" >* Responsable</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
				<input id="nombre-resp" class="edit-txt" type="text" name="nombre-resp" style="width: 80%;margin-top:3px;"
						value="<%= request.getParameter("res")!= null ? request.getParameter("res") : (app.getResponsable()!=null)?app.getResponsable():""%>">				
				</div>						
			</div>	
			
			<div style="margin: auto;width: 100%;padding: 3px;">
				<div style="display: inline-block; width: 20%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
						<label for="email-resp" style="color: #404040">Correo electrónico</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
					<input id="email-resp" class="edit-txt" type="text" name="email-resp" style="width: 80%;margin-top:3px;"
						value="<%= request.getParameter("email")!= null ? request.getParameter("email") :(app.getCorreo()!=null)?app.getCorreo():"" %>">											
				</div>
					
				<div style="display: inline-block; width: 10%;margin: 3px;">
					<!-- Label para la accesibilidad de la pagina -->
					<label for="telf-resp" style="color: #404040">Telf. Contacto</label>
				</div>
				<div  style="display: inline-block; width: 30%;margin: 3px;">
				
				<input id="telf-resp" class="edit-txt" type="text" name="telf-resp" style="width: 80%;margin-top:3px;" 
						value="<%= request.getParameter("tel")!= null ? request.getParameter("tel") :(app.getTelefono()!=null)?app.getTelefono():""%>">									
				</div>						
			</div>
							
			<div style="margin: auto;width: 100%;padding: 3px;">
				
					<div style="display: inline-block; width: 20%;margin: 3px;">
						<label for="id-certificate" style="color: #404040;">* Seleccionar certificado</label><br>	
					</div>	
					<div  style="display: inline-block; width: 30%;margin: 3px;">				
					<%if(op!=0){ %>							
						<select id="id-certificate" name="id-certificate" class="edit-txt">
							<option value="0"></option> 			
						<% for (CertificateFire cer:lCert){ 
							if(op==1){%>													
							<option value="<%=cer.getId_certificado()%>"><%=cer.getNombre_cert() %></option>  
							<%}
							else{%>
							<option value="<%=cer.getId_certificado()%>" <%=cert.getId_certificado().equals(cer.getId_certificado())?"selected='selected'":"" %>><%=cer.getNombre_cert() %></option>  
							<% }
						} %>			    		  			   
				  		</select>				  					
					<%}else{%>						
						<input id="id-certificate" class="edit-txt" type="text" name="id-certificate" style="width: 80%;margin-top:3px;" 
						value="<%=cert.getNombre_cert()%>"/>																								
					<%} %>			
					</div>
			
				</div>
			
				<div style="margin: auto;width: 100%;padding: 3px;">
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-prin" style="color: #404040">Certificado 1</label>
							</div>															
						</div>											
						<div id="cert-prin" name="cert-prin" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<%if(certDataPrincipal!=null && !"".equals(certDataPrincipal)){ %>
								<p><%= certDataPrincipal %></p>							
							<%}%>						
						</div>
					</div>
					<div style="display: inline-block; width: 48%;margin: 3px;">
						<div>
							<div style="display: inline-block;width: 75%;">
								<label for="cert-resp"  style="color: #404040">Certificado 2</label>
							</div>																					
						</div>				
						<div id="cert-resp" name="cert-resp" class="edit-txt" style="width: 90%;height:8.5em;overflow-y: auto;margin-top:3px;resize:none">
							<%if(certDataBkup!=null && !"".equals(certDataBkup)){ %>
								<p><%= certDataBkup %></p>						
							<%}%>
						</div>					
					</div>
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
					final String tit= (op == 1 ) ? "Crea nueva aplicación":"Guarda las modificaciones realizadas";
		   		%>
			   		
			   		<div  style="display: inline-block; width: 35%;margin: 3px">
			   			<input class="menu-btn" name="add-app-btn" type="submit" value="<%= msg %>" title="<%=tit %>" >
			   		</div>
		   		<% } %>
			   					   		
		   	</div>	
		</fieldset>

		</form>
		<script>
			//bloqueamos los campos en caso de que sea una operacion de solo lectura
			document.getElementById("nombre-app").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("email-resp").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("nombre-resp").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("telf-resp").disabled = <%= op == 0 ? "true" : "false" %>
			document.getElementById("id-certificate").disabled = <%= op == 0 ? "true" : "false" %>
		
		</script>
   	</div>
</body>
</html>
