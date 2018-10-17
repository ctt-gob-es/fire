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


		
%> 
    
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Gesti&oacute;n de servidores de Log FIRe</title>
	<link rel="shortcut icon" href="../resources/img/cert.png">
	<link rel="stylesheet" href="../resources/css/styles.css">
	<link rel="stylesheet" href="../resources/css/jqueryUI/jquery-ui.min.css">
	<link rel="stylesheet" href="../resources/css/jqueryUI/jquery-ui.theme.min.css">
	<link rel="stylesheet" href="../resources/css/jqueryUI/jquery.ui.timepicker.css">
	<link rel="stylesheet" href="../resources/css/ui.jqgrid.css">
	<style>
		.ui-datepicker-calendar {display: none;}    
	</style>

	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/grid.locale-es.js" type="text/javascript"></script>
	<script src="../resources/js/jquery.jqGrid.min.js" type="text/javascript"></script>	
	<script src="../resources/js/jquery-ui.min.js" type="text/javascript"></script>	
	<script src="../resources/js/jquery.ui.datepicker-es.js" type="text/javascript"></script>
	<script src="../resources/js/Chart.min.js" type="text/javascript"></script>		
	<!-- Load pdfmake, jszip lib files -->
	<script type="text/javascript" language="javascript" src="../resources/js/pdfmake.min.js">	</script>
	<script type="text/javascript" language="javascript" src="../resources/js/vfs_fonts.js"></script>
	<script type="text/javascript" language="javascript" src="../resources/js/jszip.min.js"></script>
</head>
<body>
<script>

</script>
<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
		
		<!-- contenido -->
	<div id="containerQueryManager">
		<div id="subtitle" style="padding: 5px; height: 55px;background-color: #eeeeee;">
			<form id ="formStatictics" name="formStatictics" method="post" action="../StatisticsService" >
				<div id="selectedQuery" style="display:inline-block;">		 
				 	<label for = "select_query" >* Consulta:</label>
					<select id = "select_query" name = "select_query">	
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
				<div id ="actionButtons"  style = "display: inline-block; padding-left:0.5em;">
					<label> Exportar :</label>
		  			<button id="csv" title="Exporta los datos del resultado a un fichero con formato CSV" disabled>CSV</button>&nbsp;
					<button id="excel" title="Exporta los datos del resultado a un fichero con formato Excel xlsx"disabled>Excel</button>&nbsp;
					<button id="pdf" title="Exporta los datos del resultado a un fichero con formato PDF" disabled>PDF</button>
				</div>
				
			</form>
			<div id="error-txt" style="display:none;" ></div>		
		</div>
	
		<div id="main-content" style="margin: auto; width: 100%; background-color:: #eeeeee;" >
			<div id="contentQueryResult" style="width: 98%; height:420px; vertical-align: top; overflow-x: auto;overflow-y:auto; "  ><!-- background-color: #FFFFFF; -->
		
					<!-- Graficos de datos -->	
					<div id="ChartsContend" style="display: inline-block; width:49%;"><br>						
					</div>
					<!-- Tabla de datos -->			
					<div id="data" style=" display: inline-block;text-align:center;width:49%;position: absolute;">
						<br>										
						<div id="jQGrid" style="padding-left: 2%; padding-right:2%; z-index: -1;">
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