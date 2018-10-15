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
String htmlData =""; //$NON-NLS-1$
String htmlError = ""; //$NON-NLS-1$
String styleError="";//$NON-NLS-1$
String nameSrv = "";//$NON-NLS-1$
String numRec = "1";//$NON-NLS-1$
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
	String jsonData = "";//$NON-NLS-1$
	if(jsonData != null){
		if(request.getParameter("name-srv") != null && !"".equals(request.getParameter("name-srv"))){//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			 nameSrv = request.getParameter("name-srv");//$NON-NLS-1$
		}
	
		if(jsonObj.getJsonArray("Error") != null){ //$NON-NLS-1$
			styleError="style='display: block-inline;'";//$NON-NLS-1$
			final JsonArray Error = jsonObj.getJsonArray("Error");  //$NON-NLS-1$
			for(int i = 0; i < Error.size(); i++){
				final JsonObject json = Error.getJsonObject(i);
				htmlError += "<p id='error-txt'>" + "Error:" +String.valueOf(json.getInt("Code")) + "  " + json.getString("Message") + "</p>";//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$
			}
		} 
		else {
			if(request.getParameter("msg") != null && !"".equals(request.getParameter("msg"))){//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
				styleError="style='display: block-inline;'";//$NON-NLS-1$
				htmlError += "<p id='error-txt'>" + request.getParameter("msg") + "</p>";//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
			}
			else{
				styleError="style='display: none'";//$NON-NLS-1$
			}
			
			
			final JsonArray FileList = jsonObj.getJsonArray("FileList");//$NON-NLS-1$
			jsonData += "{\"FileList\":[";//$NON-NLS-1$
			

			numRec = String.valueOf(FileList.size()); 
			for (int i = 0; i < FileList.size(); i++) {
				final JsonObject json = FileList.getJsonObject(i);
				
				//Tratamiento de la fecha de &uacute;tima actializaci&oacute;n
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//$NON-NLS-1$
				final JsonNumber longDateJSON = json.getJsonNumber("Date");//$NON-NLS-1$
				Date date = new Date(longDateJSON.longValue());		
				final String dateModif = sdf.format(date);
				
				//Tratramiento del tama&ntilde;o
				final JsonNumber longSizeJSON = json.getJsonNumber("Size");//$NON-NLS-1$
				long size = longSizeJSON.longValue();
				String sSize = "";//$NON-NLS-1$
				if(size > 1024 && size < 1024000 ){
					sSize = String.valueOf(size/1024).concat(" Kbytes");//$NON-NLS-1$
				}
				else if(size > 1024000){
					sSize = String.valueOf(size/1024000).concat(" Mbytes");//$NON-NLS-1$
				}
				else{
					sSize = String.valueOf(size).concat(" bytes");//$NON-NLS-1$
				}
				
				if(i != FileList.size() - 1){
					jsonData += "{\"Name\":\"" + json.getString("Name") + "\",\"Date\":\"" + dateModif + "\",\"Size\":\"" + sSize +"\"},";//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
				}
				else{
					jsonData += "{\"Name\":\"" + json.getString("Name") + "\",\"Date\":\"" + dateModif + "\",\"Size\":\"" + sSize +"\"}]}";//$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
				}	
			}

		}
				
	
			
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
		<div id="message" <%=styleError%>><%=htmlError%></div>				
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
	   grid = $("#list");

	    grid.jqGrid({
	      colNames: ['Nombre fichero', 'Fecha de modificaci&oacute;n','Tama&ntilde;o'],
	      colModel: [
	    	
	        { name: 'Name', width: "400",index:"id", align: 'left',sortable: true, search:false },
	        { name: 'Date', width: "200", align: 'center',sortable: true , search:false},
	        { name: 'Size', width: "200", align: 'right',sortable: true, search:false }
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
	        	var fileName = rowData['Name'];
	        	document.location.href = "../LogAdminService?op=4&fname=" + fileName + "&name-srv=" + nameServer;
	        },
	    });
	    grid.jqGrid('navGrid', '#page',
	        { add: false, edit: false, del: false, search: false, refresh: false }, {}, {}, {},
	        { multipleSearch: false, multipleGroup: false });

		
</script>

</html>