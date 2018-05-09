<%@page import="javax.json.JsonNumber"%>
<%@page import="java.util.Date"%>
<%@page import="javax.json.JsonString"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="javax.json.Json"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String errorText = null;

final Object state = request.getSession().getAttribute("initializedSession"); //$NON-NLS-1$
final String usrLogged= (String) request.getSession().getAttribute("user");//$NON-NLS-1$
if (state == null || !Boolean.parseBoolean((String) state)) {
	response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	return;
}
String htmlData = ""; //$NON-NLS-1$
String htmlError = ""; //$NON-NLS-1$
String styleError = "";//$NON-NLS-1$
String nameSrv = "";//$NON-NLS-1$
String fileName = "";//$NON-NLS-1$
String levels[] = null ;
// String charset = "UTF-8";//$NON-NLS-1$
boolean date = false;
boolean time =  false;

//Logica para determinar si mostrar un resultado de operacion
	
	final byte[] jsonLOGINFO = (byte[]) request.getSession().getAttribute("JSON_LOGINFO"); //$NON-NLS-1$ 
	if(jsonLOGINFO == null){
		response.sendRedirect("../LogAdminService?op=3"); //$NON-NLS-1$
		return;
	}
	session.removeAttribute("JSON_LOGINFO"); //$NON-NLS-1$
	
	final JsonReader reader = Json.createReader(new ByteArrayInputStream(jsonLOGINFO));
	final JsonObject jsonObj = reader.readObject();
	reader.close();
	
	if(request.getParameter(ServiceParams.PARAM_NAMESRV) != null && !"".equals(request.getParameter(ServiceParams.PARAM_NAMESRV))){//$NON-NLS-1$
		 nameSrv = request.getParameter(ServiceParams.PARAM_NAMESRV);
	}
	if(request.getParameter(ServiceParams.PARAM_FILENAME) != null && !"".equals(request.getParameter(ServiceParams.PARAM_FILENAME))){//$NON-NLS-1$
		fileName = request.getParameter(ServiceParams.PARAM_FILENAME);
	}
	
	
	if(jsonObj.getJsonArray("Error") != null){ //$NON-NLS-1$
		styleError="style='display: block-inline;'";//$NON-NLS-1$
		final JsonArray Error = jsonObj.getJsonArray("Error");  //$NON-NLS-1$
		for(int i = 0; i < Error.size(); i++){
			final JsonObject json = Error.getJsonObject(i);
			htmlError += "<p id='error-txt'>" + "Error:" +String.valueOf(json.getInt("Code")) + "  " + json.getString("Message") + "</p>";//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
		}
	} else if(jsonObj.getJsonArray("LogInfo") != null){	//$NON-NLS-1$
		final JsonArray LogInfo = jsonObj.getJsonArray("LogInfo");  //$NON-NLS-1$
		for(int i = 0; i < LogInfo.size(); i++){
			final JsonObject json = LogInfo.getJsonObject(i);

			if(json.get(ServiceParams.PARAM_LEVELS) != null) { 
				final String levels_ = json.get("Levels").toString().replace("\"", "");//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				levels = levels_ .split(",");//$NON-NLS-1$
			}
			if(json.get("Date") != null && !"".equals(json.get("Date").toString()) //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					&& "\"true\"".equals(json.get("Date").toString())) { //$NON-NLS-1$//$NON-NLS-2$ 
				date = true;
			}
			if(json.get("Time")!=null&& !"".equals(json.get("Time").toString()) //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					&& "\"true\"".equals(json.get("Time").toString())) { //$NON-NLS-1$//$NON-NLS-2$ 
				time = true;
			}						
		}
		styleError="style='display: none'";//$NON-NLS-1$	
	}


%>


<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Administraci&oacute;n FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<link rel="stylesheet" href="../resources/css/jquery-ui.min.css">
	<link rel="stylesheet" href="../resources/css/jquery-ui.theme.min.css">
	<link rel="stylesheet" href="../resources/css/jquery.ui.timepicker.css">
		
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/jquery-ui.min.js" type="text/javascript"></script>
	<script src="../resources/js/jquery.ui.datepicker-es.js" type="text/javascript"></script>
	<script src="../resources/js/jquery.ui.timepicker-es.js" type="text/javascript"></script>
	<script src="../resources/js/jquery.ui.timepicker.js" type="text/javascript"></script>
	<script src="../resources/js/logs_manager.js" type="text/javascript"></script>
	
</head>
<body>
	<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	
	<!-- contenido -->
	<div id="containerLogsManager">
		<div id="subtitle" style="padding: 10px;width:100%;">
			<div id="selectedFile" style="display:inline-block;width:35%;">
			 	 Fichero <span id="fileName"><%=fileName%></span> del servidor <span id="ServerName"><%=nameSrv%></span>.
			</div>
			<div id="error-txt-log" style="display:none;width:60%;"></div>	
			
		</div>
	
		<div id="main-content" style="margin: auto; width: 100%;" >
			<div id="contentlogResult" style="display:inline-block;width: 80%; height:400px; vertical-align: top; overflow-x: auto;overflow-y:auto;">		
				<pre id="logResult">
				</pre>
			</div>
			<div id="operations" style="display:inline-block;width:18%; vertical-align: top;">
				   
				    	<div id="lines">
				    		<label for="Nlines">* L&iacute;neas</label>
				    		<select id="Nlines" name ="Nlines">
				    			<option >0</option> 
				    			<%
				    				for(int i = 1 ; i <= 10; i++){
				    					if(i<=10){
				    				%>
				    					<option ><%=i*10%></option> 
				    				<%}
				    				}
				    				for(int i = 2 ; i <= 10; i++){
			    						if(i<=10){
			    					%>
			    						<option ><%=i*100%></option> 
			    					<%}
			    					}%>
				    			
				    		</select>				      		
				      	</div>	<!-- lines -->		      	
				     	<br>
				      	<div id="searchText" style="display:block; width:100%;" >
				      		<label for="search_txt">* Texto a buscar</label>
				      		<textarea id="search_txt"  name="search_txt" cols="20"></textarea>
				      	</div><!-- searchText -->
				      	<br>
				      	<div id="dateTimes" style="display:block; width:100%;">
				      		<div>
					      		<div style="display: inline-block;width:49%;">
					      		<% if(date){%>
					      			<label for="startDate">Fecha inicio</label>
					      			<input type="text" id="startDate" name="startDate" maxlength="10" size="10">
					      		<%}%>
					      		</div>
					      		<div style="display: inline-block;width:49%;">
					      		<% if(time){%>
					      			<label for="startTime">Hora inicio</label>
					      			<input type="text" id="startTime" name="startTime"  maxlength="8" size="8">	
					      		<%}%>
					      		</div>
				      		</div>
				      		<br>
				      		<div>
					      		<div style="display: inline-block;width:49%;">
					    		<% if(date){%>
					    			<label for="endDate">Fecha fin</label>
					      			<input type="text" id="endDate" name="endDate" maxlength="10" size="10">
					      		<%}%>
					    		</div>
					    		<div style="display: inline-block;width:49%;">
					    		<% if(time){%>
					    			<label for="endTime">Hora fin</label>
					      			<input type="text" id="endTime" name="endTime"  maxlength="8" size="8">	
					      		<%}%>	
					    		</div>
				    		</div>			      				      					      					      		      					      					      				      				      		
				      	</div> <!-- dateTimes -->
				      	<br>
				      	<div id="filtered"  style="display:block; width:100%;">
				      		<% if(levels != null){ %>
				      		<label for="level_select">* Nivel</label>
				      		<select id="level_select" name ="level_select">
				      		<% for(int i = 0; i < levels.length ; i++){ %>
				    			<option value="<%=i%>"><%=levels[i]%></option> 			    			
				    		<%}%>
				    		</select>		    			    		
				    		<%} %>
				    				      		
				      	</div><!-- filtered -->
				      	<br><br>
				      	<div  id="all_buttons" style="display:block; width:100%;">
					      	<div style="display: inline-block;width:49%;">
					      		<input id="tail-button" class="btn-log" name="tail-button" type="button" value="&uacute;ltimas l&iacute;neas" title="Obtiene las &uacute;ltimas l&iacute;neas del fichero log" onclick="getTail($('#Nlines').val());" />
					      	</div>
					      	<div style="display: inline-block;width:49%;">			      		
					      		<input id="more-button" class="btn-log" name="more-button" type="button" value="+ M&aacute;s" title="Obtiene las siguentes l&iacute;neas del fichero log" onclick="getMore($('#Nlines').val());" />
					      	</div>
				      		<br><br>
					      	<div style="display: inline-block;width:49%;">
					      		<input id="search-button" class="btn-log" name="search-button" type="button" value="Buscar" title="Obtiene las  l&iacute;neas del fichero log en donde se encuentra la primera ocurrencia del texto buscado" onclick="searchText($('#Nlines').val(),$('#search_txt').val(),$('#startDate').val() + ' '+  $('#startTime').val());" />
					      	</div>
					      	<div style="display: inline-block;width:49%;">
					      	<%if(date || time || levels != null) {%>
					      		<input id="filtered-button" class="btn-log" name="filtered-button" type="button" value="Filtrar" title="Obtiene las  l&iacute;neas del fichero log en donde se encuentra la primera ocurrencia del filtro indicado" onclick="getFiltered($('#Nlines').val(), $('#startDate').val() + ' '+  $('#startTime').val(), $('#endDate').val() + ' '+  $('#endTime').val(), $('#level_select').val() );" />
					      	<%}%>	
					      	</div>
				      		<br><br>
				      		<input id="download-button" class="btn-log" name="download-button" type="button" value="Download" title="Obtiene el fichero log completo en formato .zip" onclick="download();" />
				      	</div><!-- all_buttons -->				    
			</div>	<!-- operations -->	
		</div>	 <!-- main-content -->			    
   	</div><!-- containerLogsManager -->
	
</body>

</html>