<%@page import="es.gob.log.consumer.client.LogError"%>
<%@page import="es.gob.log.consumer.client.LogInfo"%>
<%@page import="es.gob.log.consumer.client.ServiceOperations"%>

<%@page import="es.gob.fire.server.admin.service.ServiceParams"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	if (session == null ||
			!Boolean.parseBoolean((String) session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED))) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}
	
	String nameSrv = request.getParameter(ServiceParams.PARAM_NAMESRV);
	if (nameSrv == null) {
		nameSrv = ""; //$NON-NLS-1$
	}
	String fileName = request.getParameter(ServiceParams.PARAM_FILENAME);
	if (fileName == null) {
		fileName = ""; //$NON-NLS-1$
	}

	String errorMsg = ""; //$NON-NLS-1$
	String[] levels = null ;
	
	boolean date = false;
	boolean time =  false;
	boolean filter = true;
	
	// Logica para determinar si mostrar un resultado de operacion
	
	final LogInfo logInfo = (LogInfo) session.getAttribute(ServiceParams.SESSION_ATTR_JSON_LOGINFO); 
	if (logInfo == null) {
		response.sendRedirect("../log?op=" + ServiceOperations.GET_LOG_FILES.getId() + //$NON-NLS-1$
				"&name-srv=" + nameSrv); //$NON-NLS-1$
		return;
	}
	session.removeAttribute(ServiceParams.SESSION_ATTR_JSON_LOGINFO);
	
	try {
		levels = logInfo.getLevels();
		if (levels == null || levels.length <= 1) {
			filter = false;
		}
		
		date = logInfo.isDate();
		time = logInfo.isTime();
		
		LogError error = logInfo.getError();

		if (error != null) {
			if (error.getMessage() != null) {
				errorMsg = "Error: " + error.getCode() + "  " + error.getMessage() + "<br/>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				errorMsg = "No se ha recibido la información de formato del fichero";
			}
			// Bloqueamos el uso de las funciones para las que son necesarias el
			// formato de fichero
			date = false;
			time =  false;
			filter = false;
		}
	} catch(Exception e) {
		
		errorMsg = "No se pudo cargarla la información de formato del fichero";
		
		// Bloqueamos el uso de las funciones para las que son necesarias el
		// formato de fichero
		date = false;
		time =  false;
		filter = false;
	}

%>


<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Administraci&oacute;n FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<link rel="stylesheet" href="../resources/css/jqueryUI/jquery-ui.min.css">
	<link rel="stylesheet" href="../resources/css/jqueryUI/jquery-ui.theme.min.css">
	<link rel="stylesheet" href="../resources/css/jqueryUI/jquery.ui.timepicker.css">
	<script type="text/javascript">
		var file = '<%=fileName%>';
		var server = '<%=nameSrv%>';
	</script>

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
		<div id="subtitle" style="padding: 10px;width:100%;height= 42;">
			<div id="btnContainer" style="display: inline-block;width:8%;">				
				<input id="download-button" class="btn-log" name="download-button" type="button" value="Descargar" title="Obtiene el fichero log completo en formato .zip"  onclick="download();" />					      		
			</div>
			<div id="selectedFile" style="display:inline-block;width:40%; height=42;">		 
			 	 Fichero <span id="fileName"><%=fileName%></span> del servidor <span id="ServerName"><%=nameSrv%></span>.			 	
			</div>
			<div id="progress_download" style="display:none;"><img  style="vertical-align: middle;" alt="Icono animado cargando fichero" src="../resources/img/load.gif" height="42" width="55" >Fichero <span id="fileName"><%=fileName%></span> del servidor <span id="ServerName"><%=nameSrv%></span>.</div>				      						   
			<div id="error-txt-log" style="display:none;width:50%;" onload="setIdErrorTxtLog($(this).attr('id'))"></div>			
			<div id="ok-txt-log" class="success-log"  style="display:none;width:50%;" onload="setIdOkTxtLog($(this).attr('id'))"></div>
			<div id="msg-txt-log" class="success-log" style="display:none;width:50%;" >Se ha realizado una nueva consulta.</div>					
		</div>
	
		<div id="main-content" style="margin: auto; width: 100%;" >
			<div id="contentlogResult" style="display:inline-block;width: 80%; height:420px; vertical-align: top; overflow-x: auto;overflow-y:auto; background-color: #FFFFFF;"  >
		
				<div id="advice" style="display:block; text-align: center;" onload="setIdAdvice($(this).attr('id'))">					
					<p>Para ver los resultados en esta p&aacute;gina debe usar las funciones de b&uacute;squeda y filtrado.</p>
				</div>		
				<pre id="logResult"onload="setIdContainer($(this).attr('id'))"></pre>
			</div>
			
			<div id="operations" style="display:inline-block;width:19%; vertical-align: top; padding-left: 0.2em;">
				   
				    	<div id="lines">
				    		<label for="Nlines">* L&iacute;neas</label>
				    		<select id="Nlines" name ="Nlines">				    		
				    			<option >50</option>
				    			<option >100</option>
				    			<option >200</option>
			    				<option >500</option> 			    								    			
				    		</select>				      		
				      	</div>	<!-- lines -->		      	
				     	
				     	<fieldset  id ="setSearch" style="margin-bottom: 1em;"><legend> B&uacute;squeda </legend>
					      	<div id="searchText" style="display:block; width:100%;" >
					      		<label for="search_txt">* Texto a buscar</label>
					      		<textarea id="search_txt"  class="log_search" name="search_txt" cols="20"></textarea>
					      		<div>
						      		<div style="display: inline-block;width:48%;">
						      		<% if(date){%>
						      			<label for="search_StartDate">Fecha inicio</label>
						      			<input type="text" id="search_StartDate" class="log_search" name="search_StartDate" maxlength="10" size="10">
						      		<%}%>
						      		</div>
						      		<div style="display: inline-block;width:48%;">
						      		<% if(time){%>
						      			<label for="search_StartTime">Hora inicio</label>
						      			<input type="text" id="search_StartTime" class="log_search" name="search_StartTime"  maxlength="8" size="8">	
						      		<%}%>
						      		</div>
					      		</div>
					      		<br>
					      		<div>					      							      		
					      			<div style="display: inline-block;width:48%;">
					      				<input id="search-button" class="btn-log" name="search-button" type="button" value="Buscar" title="Obtiene las  l&iacute;neas del fichero log en donde se encuentra la primera ocurrencia del texto buscado" onclick="searchText($('#Nlines').val(),$('#search_txt').val(),$('#search_StartDate').val() + ' '+  $('#search_StartTime').val());" />
					      			</div>
					      			<div style="display: inline-block;width:48%;"><!-- <span class="ui-icon ui-icon-trash"></span> -->
					      				<button id="clear-button_search"  name="clear-button_search"  title="Borra el contenido de los campos de b&uacute;squeda"  onclick="Clean('log_search')" ><span class="ui-icon ui-icon-trash"></span>Limpiar</button>
					      			</div>	
					      		</div>
					      	</div><!-- searchText -->
				      	</fieldset>
				      	<% if (filter){ %>
				      	<fieldset id ="setFilter"><legend> Filtrado </legend>
				      	<div id="dateTimes" style="display:block; width:100%;">
				      		<div>
					      		<div style="display: inline-block;width:48%;">
					      		<% if (date) {%>
					      			<label for="startDate">Fecha inicio</label>
					      			<input type="text" id="startDate" name="startDate" class="log_filter" maxlength="10" size="10">
					      		<%}%>
					      		</div>
					      		<div style="display: inline-block;width:48%;">
					      		<% if (time) {%>
					      			<label for="startTime">Hora inicio</label>
					      			<input type="text" id="startTime" name="startTime" class="log_filter"  maxlength="8" size="8">	
					      		<%}%>
					      		</div>
				      		</div>				      	
				      		<div>
					      		<div style="display: inline-block;width:48%;">
					    		<% if (date) {%>
					    			<label for="endDate">Fecha fin</label>
					      			<input type="text" id="endDate" name="endDate" class="log_filter" maxlength="10" size="10">
					      		<%}%>
					    		</div>
					    		<div style="display: inline-block;width:48%;">
					    		<% if(time){%>
					    			<label for="endTime">Hora fin</label>
					      			<input type="text" id="endTime" name="endTime" class="log_filter" maxlength="8" size="8">	
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
				      	<br>
				      					      					      	
					      	<div style="display: inline-block;width:48%;">
					      	<%if(date || time || levels != null) {%>
					      		<input id="filtered-button" class="btn-log" name="filtered-button" type="button" value="Filtrar" title="Obtiene las  l&iacute;neas del fichero log en donde se encuentra la primera ocurrencia del filtro indicado" onclick="getFiltered($('#Nlines').val(), $('#startDate').val() + ' '+  $('#startTime').val(), $('#endDate').val() + ' '+  $('#endTime').val(), $('#level_select').val() );" />
					      	<%}%>	
					      	</div>
					      	<div style="display: inline-block;width:48%;">
					      		<button id="clear-button_filter"  name="clear-button_filter"  title="Borra el contenido de los campos de filtrado"  onclick="Clean('log_filter')" ><span class="ui-icon ui-icon-trash"></span>Limpiar</button>					      		
					      	</div>	
				      	</fieldset>	
				      	<%}%>			     
				      			    
			</div>	<!-- operations -->	
			<div id="actions" style="display:block;width:80%;padding-top: 0.5em;">
				<div style="display: inline-block;width:10%;">
					<form id="back-button-form" method="GET" action="LogsFileList.jsp">					
						<input id="back-button-name-srv" name="name-srv" type="hidden" />
						<input id="back-button" class="btn-log" name="back-button" type="button" value="Volver" title="Retorna al listado de ficheros log." onclick="goReturn(server);" />					
					</form>
				</div>
				<div style="display: inline-block;width:10%;">
					<input id="reset-button" class="btn-log" name="reset-button" type="button" value="Recargar" title="Recarga el fichero log, limpiando filtros y resultados anteriores" onclick="reset();" />
				</div>
				<div style="display: inline-block;width:58%;"></div>
				<div style="display: inline-block;width:10%;">
					      		<input id="tail-button" class="btn-log" name="tail-button" type="button" value="&Uacute;ltimas l&iacute;neas" title="Obtiene las &uacute;ltimas l&iacute;neas del fichero log" onclick="getTail($('#Nlines').val());" />
				</div>
				<div style="display: inline-block;width:10%;">			      		
					<input id="more-button" class="btn-log" name="more-button" type="button" value="+ M&aacute;s" title="Obtiene las siguentes l&iacute;neas del fichero log" onclick="getMore($('#Nlines').val());" />
				</div>
			</div>
		</div>	 <!-- main-content -->
		   
   	</div><!-- containerLogsManager -->
	
</body>
<script type="text/javascript">
	setIdScrollElement('contentlogResult');
	setIdContainer('logResult');
	setIdErrorTxtLog('error-txt-log');
	setIdOkTxtLog('ok-txt-log');
	setIdAdvice('advice');
	openLog = true;

	/* Inicializa el array de campos del filtrado */
	var arrfilter = $("#setFilter input").toArray();
	for(i = 0 ; i< arrfilter.length ; i++){
		arrFieldsFilter.push(arrfilter[i].id);
	}
	/* Inicializa el array de campos de la busqueda */
	var arrsearch = $("#setSearch input").toArray();
	for(i = 0 ; i< arrsearch.length ; i++){
		arrFieldsSearch.push(arrsearch[i].id);
	}
	
	var error = "<%= errorMsg %>"; /* Se carga dinamicamente */
	if (error) {
		printErrorText(error);
	}
	
</script>
</html>