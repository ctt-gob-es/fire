<%@page import="es.gob.fire.server.admin.conf.DbManager"%>
<%@page import="es.gob.fire.server.admin.dao.ConfigurationDAO" %>
<%@page import="es.gob.fire.server.admin.message.MessageResult" %>
<%@page import="es.gob.fire.server.admin.message.MessageResultManager" %>   
<%@page import="javax.json.JsonString"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="javax.json.Json"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="java.io.StringReader"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String errorText = null;
String numRec = "1";//$NON-NLS-1$
final Object state = request.getSession().getAttribute("initializedSession"); //$NON-NLS-1$
final String usrLogged= (String) request.getSession().getAttribute("user");//$NON-NLS-1$
if (state == null || !Boolean.parseBoolean((String) state)) {
	response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
	return;
}

final String jsonError = (String) request.getSession().getAttribute("ERROR_JSON"); //$NON-NLS-1$ 
if(jsonError != null){
	final JsonReader reader = Json.createReader(new StringReader(jsonError));
	final JsonObject jsonObj = reader.readObject();
	reader.close();
	if(jsonObj.getJsonArray("Error") != null){ //$NON-NLS-1$
		final JsonArray Error = jsonObj.getJsonArray("Error");  //$NON-NLS-1$
		for(int i = 0; i < Error.size(); i++){
			final JsonObject json = Error.getJsonObject(i);
			errorText = "Error:" +String.valueOf(json.getInt("Code")) + "  " + json.getString("Message");//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
		}	
	} 
}
session.removeAttribute("ERROR_JSON"); //$NON-NLS-1$



	final String jsonData = (String)request.getAttribute("TRBYAPP");//$NON-NLS-1$ 


	//Logica para determinar si mostrar un resultado de operacion
	String op = request.getParameter("op"); //$NON-NLS-1$
	String result = request.getParameter("r"); //$NON-NLS-1$
	String entity= request.getParameter("ent"); //$NON-NLS-1$
	MessageResult mr = MessageResultManager.analizeResponse(op, result,entity);
		
%> 
    
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Gesti&oacute;n de servidores de Log FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<link rel="stylesheet" href="../resources/jquery-ui/jquery-ui.min.css">
	<link rel="stylesheet" href="../resources/jquery-ui/jquery-ui.theme.min.css">
	<link rel="stylesheet" href="../resources/css/ui.jqgrid.css">
	
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/grid.locale-es.js" type="text/javascript"></script>
	<script src="../resources/js/jquery.jqGrid.min.js" type="text/javascript"></script>	
	<script src="../resources/js/jquery-ui.min.js" type="text/javascript"></script>	
	<script src="../resources/js/jquery.ui.datepicker-es.js" type="text/javascript"></script>
	<script src="../resources/js/Chart.min.js" type="text/javascript"></script>		
	<!-- Load pdfmake, jszip lib files -->
	<script type="text/javascript" language="javascript" src="//cdn.rawgit.com/bpampuch/pdfmake/0.1.20/build/pdfmake.min.js">	</script>
	<script type="text/javascript" language="javascript" src="//cdn.rawgit.com/bpampuch/pdfmake/0.1.20/build/vfs_fonts.js"></script>
	<script type="text/javascript" language="javascript" src="//cdnjs.cloudflare.com/ajax/libs/jszip/2.5.0/jszip.min.js"></script>
</head>
<body>
<script>

</script>
<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
		
		<!-- contenido -->
	<div id="containerQueryManager">
		<div id="subtitle" style="padding: 5px;width:100%;height= 42;">
			<form id ="formStatictics" name="formStatictics" method="post" action="../StatisticsService" >
				<div id="selectedQuery" style="display:inline-block;">		 
				 	<label for = "select_query" >* Consulta:</label>
					<select id = "select_query" name = "select_query" >	
						<option value = "0" selected></option>			    		
					    <option value = "1">Transacciones finalizadas por cada aplicaci&oacute;n</option>
					    <option value = "2">Transacciones finalizadas  por cada origen de certificados/proveedor.</option>
					    <option value = "3">Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n</option>
				    	<option value = "4">Transacciones realizadas seg&uacute;n el tipo de transacci&oacute;n (simple o lote)</option>
				    	<option value = "5">Documentos firmados por cada aplicaci&oacute;n.</option>
				    	<option value = "6">Documentos firmados por cada origen de certificados/proveedor.</option>
				    	<option value = "7">Documentos firmados en cada formato de firma.</option>
				    	<option value = "8">Documentos que utilizan cada formato de firma longevo.</option> 			    								    			
					</select>													      						      					 				
				</div>
				<div style="display: inline-block; padding-left:1em;">
						<label for="start_date" >* Fecha:</label>
	    				<input name="start_date" id="start_date" class="date-picker" size="5em" />
				</div>
				<div style = "display: inline-block; padding-left:1em;">
					<button id="accept-button"  name="accept-button"  title="Ejecuta la consulta indicada en los campos de Consulta y Fecha" type="submit"><span class="ui-icon ui-icon-play"></span>Aceptar</button>
				</div>	
				<div style = "display: inline-block; padding-left:0.5em;">
					<button id="clear-button"   name="clear-button"  title="Borra el contenido de los campos de Consulta y Fecha" type="reset" ><span class="ui-icon ui-icon-trash"></span>Limpiar</button>	
				</div >
				<label> Exportar :</label>
		  		<button id="csv">CSV</button>
				<button id="excel">Excel</button>
				<button id="pdf">PDF</button>&nbsp;&nbsp;
<!-- 				<button id="getimages">Get Charts Images</button> -->
			</form>
			
<!-- 			<div id="progress_download" style="display:none;"> -->
<!-- 				<img  style="vertical-align: middle;" alt="Icono animado cargando fichero" src="../resources/img/load.gif" height="42" width="55" > -->
<!-- 				Consulta <span id="fileName"></span> del servidor <span id="ServerName"></span>.			 -->
<!-- 			</div>				      						    -->
			<div id="error-txt-log" style="display:none;width:30%;" onload="setIdErrorTxtQuery($(this).attr('id'))"></div>
			<div id="ok-txt-log" style="display:none;width:30%;" onload="setIdOkTxtQuery($(this).attr('id'))"></div>			
		</div>
	
		<div id="main-content" style="margin: auto; width: 100%;" >
			<div id="contentQueryResult" style="width: 98%; height:420px; vertical-align: top; overflow-x: auto;overflow-y:auto; background-color: #FFFFFF;"  >
		
<!-- 				<div id="advice" style="display:block; text-align: center;" onload="setIdAdvice($(this).attr('id'))">					 -->
<!-- 					<p>Para ver los resultados en esta p&aacute;gina debe  seleccionar "Consulta" y "Fecha" .</p> -->
<!-- 				</div>		 -->
<!-- 				<pre id="QueryResult"onload="setIdContainer($(this).attr('id'))"></pre> -->
					<!-- Graficos de datos -->	
					<div id="ChartsContend" style="display: inline-block; width:49%;"><br>						
					</div>
<!-- 					<div id="LinksImages"></div>	 -->
					<!-- Tabla de datos -->			
					<div id="data" style=" display: inline-block;text-align:center;width:49%;position: absolute;">
						<br>										
						<div id="jQGrid" style="padding-left: 2%; padding-right:2%;">
		 		 			<table id="resultQuery"></table>
		  					<div id="page"></div>		  					
		 				</div>
 					</div>
										
			</div>
						
		</div>	 <!-- main-content -->
		   
   	</div><!-- containerLogsManager -->

</body>

<script src="../resources/js/statistics.js" type="text/javascript"></script>	
</html>