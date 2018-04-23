<%@page import="javax.json.JsonString"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="javax.json.Json"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String errorText = null;

final Object state = request.getSession().getAttribute("initializedSession"); //$NON-NLS-1$
final String usrLogged= (String) request.getSession().getAttribute("user");//$NON-NLS-1$
if (state == null || !Boolean.parseBoolean((String) state)) {
	response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	return;
}
String htmlData =""; //$NON-NLS-1$
String htmlError = ""; //$NON-NLS-1$
String styleError="";//$NON-NLS-1$
//Logica para determinar si mostrar un resultado de operacion
	
	final byte[] sJSON = (byte[]) request.getSession().getAttribute("JSON"); //$NON-NLS-1$ 
	if(sJSON == null){
		response.sendRedirect("../LogAdminService?op=3"); //$NON-NLS-1$
		return;
	}
	session.removeAttribute("JSON"); //$NON-NLS-1$
	final JsonReader reader = Json.createReader(new ByteArrayInputStream(sJSON));
	final JsonObject jsonObj = reader.readObject();
	reader.close();
	
	if(jsonObj.getJsonArray("Error") != null){ //$NON-NLS-1$
		styleError="style='display: block-inline;'";//$NON-NLS-1$
		final JsonArray Error = jsonObj.getJsonArray("Error");  //$NON-NLS-1$
		for(int i = 0; i < Error.size(); i++){
			final JsonObject json = Error.getJsonObject(i);
			htmlError += "<p id='error-txt'>" + "Error:" +String.valueOf(json.getInt("Code")) + "  " + json.getString("Message") + "</p>";
		}
	} else {
		styleError="style='display: none'";//$NON-NLS-1$
		final JsonArray FileList = jsonObj.getJsonArray("FileList");
		htmlData += "<table class='admin-table'><thead><tr><th id='fileName'>Nombre fichero</th><th id='Size'>Tama&ntilde;o</th><th id='actions'>Acciones</th></tr></thead>'";//$NON-NLS-1$
		for (int i = 0; i < FileList.size(); i++) {
			final JsonObject json = FileList.getJsonObject(i);
			int size = Integer.parseInt(json.getString("Size"));
			String sSize = "";
			if(size > 1024 && size < 1024000 ){
				sSize = String.valueOf(size/1024).concat(" Kbytes");
			}
			else if(size > 1024000){
				sSize = String.valueOf(size/1024000).concat(" Mbytes");
			}
			else{
				sSize = String.valueOf(size).concat(" bytes");
			}
			htmlData += "<tr><td headers ='fileName'>" + json.getString("Name") + " </td><td headers ='Size'>" + sSize + 
					"</td><td headers ='actions'><a href ='../LogAdminService?op=4&fname=" + json.getString("Name") +
					"'><img src ='../resources/img/details_icon.png'/></a></tr>";
		}
		htmlData += "</table>";
	}


%>


<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Administraci&oacute;n FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
			
</head>
<body>

	<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	
	<!-- contenido -->
	<div id="container">
	
	
		<div style="display: block-inline; text-align:center;">
			<p id="descrp">
			  Listado de ficheros log del sistema.
			</p>
		</div>
		<div id="message" <%=styleError%>><%=htmlError%></div>				
		<div id="data" style="display: block-inline; text-align:center;"><%=htmlData%></div>		
   	</div>
	
</body>
<script type="text/javascript">

</script>

</html>