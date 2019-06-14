<%@page import="es.gob.log.consumer.client.ServiceOperations"%>
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
	if (session == null ||
	!Boolean.parseBoolean((String) session.getAttribute(ServiceParams.SESSION_ATTR_INITIALIZED))) {
		response.sendRedirect("../Login.jsp?login=fail"); //$NON-NLS-1$
		return;
	}

	// Cargamos el nombre del servidor seleccionado
	String nameSrv = ""; //$NON-NLS-1$
	if(request.getParameter("name-srv") != null){ //$NON-NLS-1$
		 nameSrv = request.getParameter("name-srv");//$NON-NLS-1$
	}

	final byte[] sJSON = (byte[]) session.getAttribute(ServiceParams.SESSION_ATTR_JSON); 
	if (sJSON == null){
		response.sendRedirect("../log" + //$NON-NLS-1$
				"?op=" + ServiceOperations.GET_LOG_FILES.getId() + //$NON-NLS-1$
				"&name-srv=" + nameSrv); //$NON-NLS-1$
		return;
	}
	session.removeAttribute(ServiceParams.SESSION_ATTR_JSON);
	
	String jsonData = "";//$NON-NLS-1$
	String htmlError = null;
	int numRec = 1;
	JsonObject jsonObj;
	try {
		final JsonReader reader = Json.createReader(new ByteArrayInputStream(sJSON));
		jsonObj = reader.readObject();
		reader.close();

		if (jsonObj.getJsonArray("Error") != null) { //$NON-NLS-1$
			final JsonArray error = jsonObj.getJsonArray("Error"); //$NON-NLS-1$
			for (int i = 0; i < error.size(); i++) {
				final JsonObject json = error.getJsonObject(i);
				htmlError += "<p id='error-txt'>" + "Error:" + String.valueOf(json.getInt("Code")) + "  " //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
						+ json.getString("Message") + "</p>";//$NON-NLS-1$//$NON-NLS-2$
			}
		} else {
			if (request.getParameter("msg") != null && !"".equals(request.getParameter("msg"))) {//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
				htmlError += "<p id='error-txt'>" + request.getParameter("msg") + "</p>";//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			}

			final JsonArray fileList = jsonObj.getJsonArray("fileList");//$NON-NLS-1$
			
			jsonData += "{\"FileList\":[";//$NON-NLS-1$

			numRec = fileList.size();
			for (int i = 0; i < fileList.size(); i++) {
				final JsonObject json = fileList.getJsonObject(i);

				// Tratamiento de la fecha de ultima actualizacion
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//$NON-NLS-1$
				final JsonNumber longDateJSON = json.getJsonNumber("date");//$NON-NLS-1$
				Date date = new Date(longDateJSON.longValue());
				final String dateModif = sdf.format(date);

				// Tratramiento del tamano
				final JsonNumber longSizeJSON = json.getJsonNumber("size");//$NON-NLS-1$
				long size = longSizeJSON.longValue();
				String sSize = "";//$NON-NLS-1$
				if (size > 1024 && size < 1024000) {
					sSize = String.valueOf(size / 1024) + " Kbytes";//$NON-NLS-1$
				} else if (size > 1024000) {
					sSize = String.valueOf(size / 1024000) + " Mbytes";//$NON-NLS-1$
				} else {
					sSize = String.valueOf(size) + " bytes";//$NON-NLS-1$
				}

				jsonData += "{\"name\":\"" + json.getString("name") + "\",\"date\":\"" + dateModif //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						+ "\",\"size\":\"" + sSize + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$
						
				jsonData +=  (i != fileList.size() - 1) ? "," : "]}"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	} catch (Exception e) {
		// Si al procesar el JSON con los datos se obtiene un error, se notifica al usuario y no se hace nada
		htmlError = "<p id='error-txt'>Error al procesar la respuesta del servidor</p>"; //$NON-NLS-1$
		jsonData = null;
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
	<link rel="stylesheet" href="../resources/css/ui.jqgrid.css">
	
	<script src="../resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
	<script src="../resources/js/jquery-ui.min.js" type="text/javascript"></script>
	
	<script src="../resources/js/grid.locale-es.js" type="text/javascript"></script>
	<script src="../resources/js/jquery.jqGrid.min.js" type="text/javascript"></script>
	<script type="text/javascript">
	  

 		
	</script>		
</head>
<body>

	<!-- Barra de navegacion -->
	<jsp:include page="../resources/jsp/NavigationBar.jsp" />
	
	<!-- contenido -->
	<div id="container">
		<% if (htmlError != null) { %>
			<div id="message" style='display: block-inline;'><%=htmlError%></div>
		<% } %>
		<div id="data" style="display: block-inline; text-align:center;">		
			<div id="jQGrid" style="padding-left: 11%; padding-right:12%;">
	 		 	<table id="list"></table>
	  			<div id="page">
	  			</div>
	 		</div>
 		</div>
				
   	</div>
	
</body>
<script type="text/javascript">

	var obJson = <%=jsonData%>; 
	
	if (obJson && obJson.FileList.length > 0) {
		
		var txtNumRec = '<%=numRec%>';
		var nameServer = '<%=nameSrv%>';
		var total =  Math.ceil(txtNumRec / 10);
		var dataJSON = '{"TotalPages":'+ total +',"ActualPage":1,"TotalRecords":'+txtNumRec+',"FileListRows":[';
				
		for (i = 0; i < obJson.FileList.length; i++) {
			if (i != obJson.FileList.length -1){
				dataJSON += JSON.stringify(obJson.FileList[i]) + ",";
			}
		    else{
		    	dataJSON += JSON.stringify(obJson.FileList[i]) + "]}" ;
		    }
		}
		
		console.log(dataJSON);

		grid = $("#list");
	    grid.jqGrid({
	      colNames: ['Nombre fichero', 'Fecha de modificaci&oacute;n','Tama&ntilde;o'],
	      colModel: [
	    	
	        { name: 'name', width: "400",index:"id", align: 'left',sortable: true, search:false },
	        { name: 'date', width: "200", align: 'center',sortable: true , search:false},
	        { name: 'size', width: "200", align: 'right',sortable: true, search:false }
	      ],
	       
	        pager: '#page',
	        datatype: "jsonstring",
	        datastr: dataJSON,
	        jsonReader: {
	        		  repeatitems: false,
	        		  root:"FileListRows",
	        		  page: "ActualPage",
	        		  total:"TotalPages" ,
	        		  records: "TotalRecords"
	        		},
	        rowNum: 10,
	        rowList:[10,20,30],
	        viewrecords: true,
	        caption: "Listado de ficheros log del servidor " + nameServer + ".",
	        height: "auto",
	        width:"auto",
	        ignoreCase: true,
	        ondblClickRow: function(id){        	
	        	var rowData = $(this).getRowData(id);
	        	var fileName = rowData['name'];
	        	document.location.href = "../log?op=4&fname=" + fileName + "&name-srv=" + nameServer;
	        },
	    });
	    grid.jqGrid('navGrid', '#page',
	        { add: false, edit: false, del: false, search: false, refresh: false }, {}, {}, {},
	        { multipleSearch: false, multipleGroup: false });
	}
		
</script>

</html>