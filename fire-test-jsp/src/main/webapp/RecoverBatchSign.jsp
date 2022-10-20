<%@page import="java.util.Map"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="es.gob.fire.test.webapp.Base64"%>
<%@page import="es.gob.fire.test.webapp.BatchHelper"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Prueba FIRe</title>
		<link rel="shortcut icon" href="img/cert.png">
		<link rel="stylesheet" href="styles/styles.css"/>
		<script type="text/javascript">
			function b64toBlob (b64Data, contentType='', sliceSize=512) {
			  const byteCharacters = atob(b64Data);
			  const byteArrays = [];

			  for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
			    const slice = byteCharacters.slice(offset, offset + sliceSize);

			    const byteNumbers = new Array(slice.length);
			    for (let i = 0; i < slice.length; i++) {
			      byteNumbers[i] = slice.charCodeAt(i);
			    }

			    const byteArray = new Uint8Array(byteNumbers);
			    byteArrays.push(byteArray);
			  }

			  return new Blob(byteArrays, {type: contentType});
			}
		
		</script>
	</head>
	<body style=" font-weight: 300;">

		<%
			if (session.getAttribute("user") == null) { //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}
		
		    byte[] signature = null;
		    try {
		    	signature = BatchHelper.recoverBatchSign(request);
		    }
		    catch (Exception e) {
				LoggerFactory.getLogger("es.gob.fire.test.webapp").error( //$NON-NLS-1$
						"Error al recuperar una firma del lote: {}", e.toString()); //$NON-NLS-1$
		    	response.sendRedirect("ErrorPage.jsp?msg=" + URLEncoder.encode(e.getMessage(), "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$
		    	return;
		    }
		    
			 // Identificamos si el resultado es demasiado grande, en cuyo caso no lo mostraremos en Base 64 
 			final boolean dataTooLarge = signature.length > 1024 * 1024; // 1 Mb
		    
 			// Definimos la extension para la descarga de la firma segun el formato
 			String ext = null;
 			
		    String docId = request.getParameter("docid"); //$NON-NLS-1$
		    Map<String, String> formats = (Map<String, String>) session.getAttribute("formats"); //$NON-NLS-1$
		    if (docId != null && formats != null && formats.containsKey(docId)) {
		    	String signFormat = formats.get(docId);
		    	if (signFormat != null) {
	 				switch (signFormat) {
	 					case "CAdES": //$NON-NLS-1$
	 						ext = ".csig"; //$NON-NLS-1$
	 						break;
	 					case "XAdES": //$NON-NLS-1$
	 						ext = ".xsig"; //$NON-NLS-1$
	 						break;
	 					case "PAdES": //$NON-NLS-1$
	 						ext = ".pdf"; //$NON-NLS-1$
	 						break;
	 					case "FacturaE": //$NON-NLS-1$
	 						ext = ".xml"; //$NON-NLS-1$
	 						break;
	 					case "CAdES-ASiC-S": //$NON-NLS-1$
	 					case "XAdES-ASiC-S": //$NON-NLS-1$
	 						ext = ".asics"; //$NON-NLS-1$
	 						break;
	 					default:
	 						ext = ""; //$NON-NLS-1$
	 				}
	 			}
		    }
		%>

		<div id="menubar">
			<div id="bar-txt"><b>Prueba FIRe</b></div>
		</div>

		<div id="sign-container">
			<h1 style="color:#303030;">OBTENCI&Oacute;N DE LA FIRMA</h1>

			<div style="display:inline-block;"></div>

			<div style="margin-top: 10px; text-align: left; ">
				<% if (!dataTooLarge) { %>
					<label for="datos-firma">Resultado: </label><br><br>
					<textarea id="datos-firma" rows="10" cols="150" name="sign-data"><%= Base64.encode(signature) %></textarea><br>
				<% } else { %>
					<span>La firma generada es demasiado grande para mostrarla. Pulse el siguiente enlace para descargar:</span>
				<% } %>
				<a id="download_link" download="firma<%= docId + ext %>" href="" style="display: none">Descargar fichero</a>
			</div>
		</div>
	
	</body>
	<%    
		if (signature != null) {
	%>
			<script type="text/javascript">
				var blob = b64toBlob("<%= Base64.encode(signature) %>");
				
				var url = window.URL.createObjectURL(blob);
		
				document.getElementById('download_link').href = url;
				document.getElementById('download_link').style = "display: block";
			</script>
	<%
		}
	%>
</html>