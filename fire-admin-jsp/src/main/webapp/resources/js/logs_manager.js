/**
 * 
 */
 $(document).ready(function(){

	
   $( function() {
     $( "#startDate" ).datepicker({
       changeMonth: true,
       changeYear: true
     });
     $('#startTime').timepicker();
     
     $( "#search_StartDate" ).datepicker({
         changeMonth: true,
         changeYear: true
       });
       $('#search_StartTime').timepicker();
     
     $( "#endDate" ).datepicker({
         changeMonth: true,
         changeYear: true
       });
       $('#endTime').timepicker();
   } );

   $(window).on('beforeunload', function() {
		 
		 if(!isReset){
			  closeFile();
			  return '';			
		 }
		 return "salir sin cerrar";
		});
  
   
  });//fin de document ready
 
 /**Constantes*/
	
	var maxLines = 600;
	var points = "<div>. . . . . . . . . .</div><div>. . . . . . . . . .</div><div>. . . . . . . . . .</div>";
	
/**Variables Globales */	
	
	var isReset = false;
	var addResult = false;
	var isFinal = false;
	
	var filterOp = 0;
	var searchOp = 0;
	
	var text2Search = "";
	var date2Search = 0;
	
	var initDate2Filter = 0;
	var endDate2Filter = 0;
	var level2Filter = 0;
	
	var linesCount = 0;
	var diffLines = 0;
	var next_position = searchOp;
	var scrollTopPosition = 0;
	var scrollLefPosition = 0;
	var param_reset = "";
	var  oper = "";
	var arrFieldsFilter = ["level_select","Nlines"]; 
	var arrFieldsSearch = ["search_txt"]; 


	 
	 
	/** Elementos del DOM (id) utilizados en las funciones*/
	var idContainer = "";
	var idScrollElement = "";
	var idErrorTxtLog = "";
	var idOkTxtLog = "";
	var idAdvice = "";
	var idProgress = "progress_download";
	var idMsgTxtLog = "msg-txt-log";

	 
 /*****************Funciones:******************************/
 
 /**
  * Muestra en pantalla el mensaje de error indicado en el objeto JSON que se le pasa.
  */
 function printErrorResult(JSONData){
	printErrorText(JSONData.Error[0].Message);
 }
 
 /**
  * Muestra en pantalla un mensaje de error.
  */
 function printErrorText(errorText){
	$("#" + idErrorTxtLog).html("");
	$("#" + idOkTxtLog).html(""); 
	$("#" + idOkTxtLog).hide();
	$("#" + idMsgTxtLog).hide();
	$("#" + idErrorTxtLog).append(errorText);
	$("#" + idErrorTxtLog).css('display:inline-block');
	$("#" + idErrorTxtLog).show();
 }
 
 /**
  * Muestra en pantalla el mensaje de Ok indicado en el objeto JSON que se le pasa.
  * @param JSONData
  * @returns
  */
 function printOkResult(JSONData){
		var html = "";
		$("#" + idErrorTxtLog).html(html);
		$("#" + idErrorTxtLog).hide();
		$("#" + idOkTxtLog).html(html);
		html = JSONData.Ok[0].Message;
		$("#" + idOkTxtLog).append(html);
		$("#" + idOkTxtLog).css('display:inline-block');
		$("#" + idOkTxtLog).show();
		 
	 }
 

 
 /**
  * Visualiza en pantalla la imagen de cargar en lugar del bot&oacute;n en caso de que la variable display = 1
  * en caso contrario se vuelve a visualizar el bot&oacute;n en lugar de la imagen de carga.
  * Al mismo tiempo se desactivan los botones de tail, more, search, filter y reset cuando se visualiza la imagen de load
  * y se vuelve a activar cuando se deja de visualizar la imagen de load.
  * @param display
  * @returns
  */
 function displayProgressBar(display){	
	 var btnActives = $(".btn-log");
	 var btnInActives = $(".btn-log_inactive");
	 if (display == 1){		
		$("#" + idProgress).css('display:inline-block');
		$("#" + idProgress).show();
		$("#btnContainer").hide(); 
		$("#selectedFile").hide();
		
		btnActives.each(function(id){					 
			 if($(this).attr("id") !== "back-button" && $(this).attr("id") !== "download-button"){
				 activeElement($(this).attr("id"), false); 
			 }			
		 });	
	 }
	 else if(display == 0){		
		$("#" + idProgress).css('display:none');
		$("#" + idProgress).hide();
		$("#btnContainer").show(); 
		$("#selectedFile").show();
		
		btnInActives.each(function(id){					 			
			activeElement($(this).attr("id"), true); 						
		 });

	 }			
	
 }
 
 /**
  * Muestra en pantalla el resultado de las operaciones (Tail, More, Search y Filter) sobre el fichero de log.
  * que se le pasan desde el objeto JSON 
  * @param JSONData, nlines
  * @returns
  */
 function printResult(JSONData, nlines){
	 
	 var moreWithSearch = false;
	 var data = "";
	 var content = getResultLines(nlines);

	 $("#" + idContainer ).html("");
	 $("#" + idErrorTxtLog).hide();
	 $("#" + idOkTxtLog).hide();
	 $("#" + idAdvice).hide();
	 
	 if(JSONData.hasOwnProperty('Tail')){
		 
		 if ($("#" + idMsgTxtLog).is(":hidden")) {
		 	$("#" + idMsgTxtLog).fadeIn("slow");
		 }
		 data = JSONData.Tail[0].Result;		
		 content = "";
		 oper = "Tail";
	
	 }
	 else if(JSONData.hasOwnProperty('More')){
		 data = JSONData.More[0].Result;
		 if(oper === "Search"){
			 moreWithSearch = true;
		 }
		 oper = "More";	 	
	 }
	 else if(JSONData.hasOwnProperty('Search')){
	
		
		if(searchOp == 0 && $("#" + idMsgTxtLog).is(":hidden")) {
			$("#" + idMsgTxtLog).fadeIn("slow");
		}		 	
		 oper = "Search";
		 data = JSONData.Search[0].Result;
			 
		 if(!addResult){
			 content = ""; 
		 }
		 if (content !== ""){
			 content += points; 
		 }
	 }
	 else if(JSONData.hasOwnProperty('Filtered')){
		
		if(filterOp == 1 && $("#" + idMsgTxtLog).is(":hidden")){
			$("#" + idMsgTxtLog).fadeIn("slow");
		}
		 oper = "Filtered";
		 data = JSONData.Filtered[0].Result;
		 if(!addResult){
			 content = ""; 
		 }	 
	 }

	var arrHtml = data.split("</br>");
	for (i = 0; i < arrHtml.length-1; i++) { 
		/* En el caso de ser la busqueda se selecciona el texto a buscar */
		if (oper === "Search" || moreWithSearch){
			var line = "<div>" + arrHtml[i] + "</div>";
			var text2replace = "<span class='highlight'>" + text2Search + "</span>";
			var newline = line.replace (new RegExp(text2Search, 'g'),text2replace);		
			content += newline;		
		}
		else{
			content += "<div>" + arrHtml[i] + "</div>"; 
		}	
	}
	 $("#" + idMsgTxtLog).fadeOut(4000);

	 $("#"+ idContainer).append(content);	 
	 
//	 alert($('#'+idScrollElement).scrollTop());
	 
//	 $('#'+idScrollElement).scrollTop(scrollTopPosition);
//	 $('#'+idScrollElement).scrollLeft(scrollLefPosition);
	 
	 console.log("scrollTopPosition: "+scrollTopPosition);
	
  }
 /**
  * Funci&oacute;n Ajax que llama a la operac&oacute;n Tail indicando el n&uacute;mero de lineas que se
  * quieren obtener
  * @param nlines
  * @returns
  */
 function getTail(nlines){
	 	searchOp = 0;
	 	filterOp = 0;
		$('#'+idScrollElement).scrollTop(0);
	 	var arrFields = ["Nlines"];

	 	var ok = validateFields(arrFields);						
		if(ok){
			 activeElement("more-button", true);
			 var url = "../log?op=6&nlines="+nlines+"&fname="+$("#fileName").text();
			 $.post(url, function(data){		
				 var JSONData = JSON.parse(data);
				  if(JSONData.hasOwnProperty('Tail')){
					  printResult(JSONData, nlines);  
				  }
				   else{
					   printErrorResult(JSONData);  
				   }  
				  addResult=false;
			}); 
		}
 }
 
 /**
  * Funci&oacute;n Ajax que llama a la operac&oacute;n More indicando el n&uacute;mero de lineas que se
  * quieren obtener 
  * @param nlines
  * @returns
  */
 function getMore(nlines){

	var arrFields = ["Nlines"];
	var ok = validateFields(arrFields);						
	if(ok){
		 var url = "../log?op=7&nlines="+nlines+"&fname="+$("#fileName").text();
		 $.post(url, function(data){		
			 var JSONData = JSON.parse(data);
			  if(JSONData.hasOwnProperty('More')){
				  printResult(JSONData, nlines);			
			  }
			   else {			   
				   printErrorResult(JSONData);  
			   }      	            
		}); 
	}
	
 }

 /**
  * Funci&oacute;n Ajax que llama a la operac&oacute;n Search indicando el n&uacute;mero de lineas que se
  * quieren obtener, el texto a buscar y la fecha desde la que se quiere encontrar el texto
  * @param nlines
  * @param text
  * @param date
  * @returns
  */
 function searchText(nlines, text, date){
	
	var DateTime =  getlongDateTime(date);
	
	filterOp = 0;
	var ok = validateFields(arrFieldsSearch);						
	if(ok){
		 activeElement("more-button", true);
		
		 if(searchOp == 0){
			 resetSearch( text, DateTime)
		 }
		 else{
			 addResult  = true; 
			 param_reset = "";
		 }
					 
		 if (text2Search !== text || date2Search != DateTime){
			 resetSearch(text, DateTime)
		 }	
		 var url = "../log?op=8&nlines=" + nlines + "&search_txt=" + text + "&search_date=" + DateTime + param_reset;

		 if(isFinal || searchOp == 0){ 
			 $.post(url, function(data,status){

				  var jsonData = JSON.parse(data);

				  if(jsonData.hasOwnProperty('Search')){
					console.log("Print Search ");
				  	printResult(jsonData, nlines);
				  	searchOp = 0;
				  	isFinal = markNextText(text);
				  	
				  }
				   else {
					   if(jsonData.hasOwnProperty('Error')){
						 	printErrorResult(jsonData);  
					   }			
				  }			  
			});
			 
		 }
		 else if ( !isFinal && searchOp > 0){
				isFinal = markNextText(text);
		 }
		 		 	
	}
	
 }
 
 /**
  * Funci&oacute;n Ajax que llama a la operac&oacute;n Filter indicando el n&uacute;mero de lineas que se
  * quieren obtener, el nivel del registro a filtrar y las fechas desde las que se quieren filtrar los registros 
  * @param nlines
  * @param startDate
  * @param endDate
  * @param level
  * @returns
  */
 function getFiltered(nlines, startDate, endDate, level){

	var startDateTime =  getlongDateTime(startDate);
	var endDateTime =  getlongDateTime(endDate);	
	var ok = validateFields(arrFieldsFilter);
	 
	if(ok){	
		searchOp = 0;
		activeElement("more-button", false);
		if(filterOp == 0){
			$('#'+idScrollElement).scrollTop(0);
			resetFilter(startDateTime, endDateTime, level);			
		 }
		 else{
			 addResult  = true; 
			 param_reset = "";
		 }
		
		//console.log("startDate :"+startDate + " endDate :"+endDate+ " level:"+level);
		
		 if (initDate2Filter !== startDateTime || 
			 endDate2Filter != endDateTime || 
			 level2Filter !== level){
			 resetFilter(startDateTime, endDateTime, level);		
			// console.log("startDate :"+startDate + " endDate :"+endDate+ " level:"+level);
		 }	
			
		filterOp = filterOp + 1;		
		var url = "../log?op=9&nlines=" + nlines + "&start_date=" + startDateTime + "&end_date=" + endDateTime + "&level=" + level + param_reset;		 
		 $.post(url, function(data){		
			 var JSONData = JSON.parse(data);
			  if(JSONData.hasOwnProperty('Filtered')){
			  	printResult(JSONData,nlines);  
			  }
			   else {
			   	printErrorResult(JSONData);  
			  }      	            
		});  
	} 	 	
	 
 }
 
 
 /**
  * funci&oacute;n que devuelve objeto fecha (Date), respecto de los datos de entrada (date) con formato (dd/mm/yyyy HH:mm:ss)
  * @param date
  * @returns
  */
 function getlongDateTime(date){
	 var result = -1;

	 if(date == null || typeof date === 'undefined' || date.trim()==""){
		 return result;
	 }
	

	 var day = date.substr (0,2);
	 var month = date.substr (3,2);
	 var year = date.substr(6,4);

	 var hour = date.substr(11,2);
	 var minute = date.substr(14,2);
	 var seconds = date.substr(17,2);
	 var millisec = "000";
	 
	 var DateTime =  new Date();
	 if(day != null && typeof day != "undefined"
		 	&& month != null && typeof month != "undefined"
			&& year != null && typeof year != "undefined"){
		 DateTime.setFullYear(year, month - 1, day);
	 }		 		 		 
	 if(!isNaN(DateTime.getTime())){
		 if(hour != null && typeof hour != "undefined" && hour != ""){
			 DateTime.setHours(hour); 
		 }
		 else{
			 DateTime.setHours("00"); 
		 }
		 if(minute != null && typeof minute != "undefined" && minute != ""){
			 DateTime.setMinutes(minute); 
		 }
		 else{
			 DateTime.setMinutes("00"); 
		 }
		 if(seconds != null && typeof seconds != "undefined" && seconds != ""){
			 DateTime.setSeconds(seconds); 
		 }
		 else{
			 DateTime.setSeconds("00");  
		 }
		 DateTime.setMilliseconds(millisec);
		 
		 result = DateTime.getTime();			
	 }
	 return result;
	
 }
 
 /**
  * Funci&oacute;n de validaci&oacute;n de los campos pasados como un array de nombres,
  * en caso de no tener valor los marca en rojo y muestra un mensaje alert
  * en paralla.
  * @param fields
  * @returns
  */
 function validateFields(fields){
	 
	 var datePattern = /^(?:3[01]|[12][0-9]|0?[1-9])([\-/.])(0?[1-9]|1[1-2])\1\d{4}$/g;
	 var timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$/g;
	 $("label").each(function( index ) {
			if (this.style.color = "red") {				
			      this.style.color = "#000000";
			      var idInput = $(this).attr('for');
			      $('#'+idInput).css({backgroundColor:'#FFFFFF'});
			    } 
		});
	 var ok = true;
	 var msg = "";
	 
	 for ( i = 0; i < fields.length; i++ ){
		
		switch(String(fields[i])){
		case "Nlines":
			
			if($("#Nlines").val() == "0"){								
				$('label[for=Nlines]').css({color:'red'});
				$('#Nlines').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir un número mayor de 0 líneas\n";
				ok = false;			
			}		
			break;
		case "search_txt":
	
			if($("#search_txt").val() == ""){								
				$('label[for=search_txt]').css({color:'red'});
				$('#search_txt').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir un texto para realizar la búsqueda.\n";
				ok = false;			
			}		
			break;		
		case "level_select":
			
			if($("#level_select").val() == ""){								
				$('label[for=level_select]').css({color:'red'});
				$('#level_select').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir un nivel de log para filtrar.\n";
				ok = false;			
			}
			break;
				
		case "search_StartDate":
			
			var dateValue = $("#search_StartDate").val().trim();			
			if(dateValue != "" &&  datePattern.test(dateValue) == false){								
				$('label[for=search_StartDate]').css({color:'red'});
				$('#search_StartDate').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir una fecha de inicio para la búsqueda, con un formato correcto dd/MM/yyyy.\n";
				ok = false;			
			}
			break;
		case "search_StartTime":
			
			var timeValue = $("#search_StartTime").val().trim();
			if(timeValue != "" && timePattern.test(timeValue) == false){				
				$('label[for=search_StartTime]').css({color:'red'});
				$('#search_StartTime').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir una Hora de inicio para la búsqueda, con un formato correcto HH:mm:ss o HH:mm .\n";
				ok = false;			
			}
			break;
		case "startDate":			
			var dateValue = $("#startDate").val() .trim();	
			if(dateValue != "" &&  datePattern.test(dateValue) == false){
				$('label[for=startDate]').css({color:'red'});
				$('#startDate').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir una fecha de inicio para el filtrado, formato correcto dd/MM/yyyy .\n";
				ok = false;			
			}
			console.log ("startDate ok="+ok);
			console.log ("startDate value=#"+$("#startDate").val()+"#");
			console.log ("startDate datePattern="+ datePattern.test($("#startDate").val())); 
			
		case "startTime":			
			var timeValue = $("#startTime").val().trim();
			if(timeValue != "" &&  timePattern.test(timeValue) == false){
				$('label[for=startTime]').css({color:'red'});
				$('#startTime').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir una Hora de inicio para el filtrado, un formato  correcto HH:mm:ss o HH:mm.\n";
				ok = false;			
			}
			console.log ("startTime ok="+ok);
			console.log ("startTime value=#"+$("#startTime").val()+"#");
			console.log ("startTime timePattern="+ timePattern.test($("#startTime").val())); 
			
			break;	
		case "endDate":					
			var dateValue = $("#endDate").val().trim();	
			if($("#endDate").val() != "" &&  datePattern.test($("#endDate").val()) == false){
				$('label[for=endDate]').css({color:'red'});
				$('#endDate').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir una fecha de fin para el filtrado, formato correcto dd/MM/yyyy.\n";
				ok = false;			
			}
			console.log ("endDate ok="+ok);
			console.log ("endDate value=#"+$("#endDate").val()+"#");
			console.log ("endDate datePattern="+ datePattern.test($("#endDate").val())); 
			break;	
		case "endTime":
			var timeValue = $("#endTime").val().trim();
			if(timeValue != "" && timePattern.test(timeValue) == false){
				$('label[for=endTime]').css({color:'red'});
				$('#endTime').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir una Hora de fin para el filtrado, un formato  correcto HH:mm:ss o HH:mm.\n";
				ok = false;			
			}
			console.log ("endTime ok="+ok);
			console.log ("endTime value=#"+$("#endTime").val()+"#");
			console.log ("endTime timePattern="+ timePattern.test($("#endTime").val())); 
			break;	
			
		}
	 }
	 
	if(!ok){
		alert(msg);
	}	
	 
	 return ok;  	 
 }
 
 /**
  * Funci&oacute;n que llama a la operaci&oacute;n download mediante post con el objeto XMLHttpRequest
  * que comprueba la cabecera devuelta por el response si tiene datos adjuntos para ejecutar 
  * autom&aacute;ticamente un enlace al fichero devuelto.
  * @returns
  */
 function download(){
	  
	 var url = "../log?op=10&fname=" + file;

	 displayProgressBar(1);	
	 var xhr = new XMLHttpRequest();
	 xhr.open("POST", url, true);
	 xhr.responseType = "arraybuffer";
	 xhr.onload = function () {
		 				 
		 var filename = "";
	     var disposition = xhr.getResponseHeader('Content-Disposition');
	     if (disposition && disposition.indexOf('attachment') !== -1) {
	         var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
	         var matches = filenameRegex.exec(disposition);
	         if (matches != null && matches[1]) filename = matches[1].replace(/['"]/g, '');
	     }
	         var type = xhr.getResponseHeader('Content-Type');
	         var blob = typeof File === 'function'
	             ? new File([this.response], filename, { type: type })
	             : new Blob([this.response], { type: type });
	         if (typeof window.navigator.msSaveBlob !== 'undefined') {
	             // IE workaround for "HTML7007: One or more blob URLs were revoked by closing 
	        	 //the blob for which they were created. These URLs will no longer resolve as the data backing the URL has been freed."
	             window.navigator.msSaveBlob(blob, filename);
	         } else {
	             var URL = window.URL || window.webkitURL;
	             var downloadUrl = URL.createObjectURL(blob);
	
	             if (filename) {
	                 // use HTML5 a[download] attribute to specify filename
	                 var a = document.createElement("a");
	                 // safari doesn't support this yet
	                 if (typeof a.download === 'undefined') {
	                     window.location = downloadUrl;
	                 } else {
	                     a.href = downloadUrl;
	                     a.download = filename;
	                     document.body.appendChild(a);
	                     a.click();
	                 }
	             } else {
	                 window.location = downloadUrl;
	             }
	         }	       
	         
	     // La respuesta es de otro tipo, Json por ejemplo
//         else{
//         }

	 };
//	 xhr.onprogress = function(e) {
//	     if (e.lengthComputable) {
//	         progressBar.max = e.total;
//	         progressBar.value = e.loaded;	         	               	         
//	     }
//	 };
//	 xhr.onloadstart = function(e) {
//		
//		
//	 };
	 xhr.onloadend = function(e) {
		 displayProgressBar(0);
	 };
	
	xhr.send();  
	
	
 }
 
 /**
  * Funci&oacute;n recarga la p&aacute;gina he inicializa las variables y se carga la p&aacute;gina anterior
  * en la que se muestra el listado de ficheros log del servidor seleccionado anteriormente.
  * @returns
  */
 function goReturn(server){	
	 reset();
	 //location.href = 'LogsFileList.jsp?name-srv=' + server;
	 $('#back-button-name-srv').val(server);
	 $('#back-button-form').submit();
 }
 
 
 /**
  * Funci&oacute;n recarga la p&aacute;gina he inicializa las variables 
  * @returns
  */
 function reset(){
	 
	linesCount = 0;
	isReset = false;
	addResult = false;
	filterOp = 0;
	searchOp = 0;
	text2Search = "";
	diffLines = 0;
	next_position = searchOp;
	scrollTopPosition = 0;
	scrollLefPosition = 0;
	 	
	 var html = "";
	 $("#" + idErrorTxtLog).html(html);
	 $("#" + idErrorTxtLog).hide();
	 $("#" + idOkTxtLog).html(html); 
	 $("#" + idOkTxtLog).hide();
	 activeElement("more-button", true);

	 var url = "../log?op=5";
	 $.post(url,function(data){	
	 if(data != null && typeof(data) != "undefined"){
		 var JSONData = JSON.parse(data);
		   if(JSONData.hasOwnProperty('Error')){
			   printErrorResult(JSONData);
			   isReset = false;
		   }
		   else{			   	  
			   $("#" + idContainer).html("");
			   var urlOpen = "../log?op=4&fname=" + file + "&name-srv=" + server + "&reset=yes";			   		 
			   $.post(urlOpen,function(dat){	
				   if(dat != null && typeof(dat) != "undefined"){
					   isReset = true;
					   $("#" + idAdvice).show();
				   }
			   });			  
		   }
	 }   	             		   
   });
 }
 
 
 /**
  * Funcion que borra el contenido de los filtros del contenedor indicado
  * @returns
  */
 function Clean(css_class){

	 $("." + css_class).each(function() {
	    var type = this.type;	   
	    if (type == 'checkbox' || type == 'radio'){
	    	this.checked = false;   	
	    }
	    else {
	    	this.value = "";	    	
	    }   
	  });
	 
//	 $("select").each(function() {		   
//		 this.selectedIndex = 0;   			    		  	    
//	 }); 
	// param_reset = "&reset=yes";
 }
 
 /**
  * Funci&oacute;n Ajax que llama a la operac&oacute;n Close 
  * @returns
  */
 function closeFile(){
	 
	 var url = "../log?op=5";
	 $.post(url,function(data){	
		 if(data != null && typeof(data) != "undefined"){
			 var JSONData = JSON.parse(data);
			 if(JSONData.hasOwnProperty('Error')){
				 printErrorResult(JSONData); 
				 
			 }
		}
			    	             		   
	});
		
 }
 
 /**
  * Funci&oacute;n que obtiene las l&iacute;neas que est&aacute;n en pantalla anteriores a la nueva operaci&oacute;n
  * teniendo en cuenta, el maximo de lineas a mostrar en pantalla, esto es no se obtienen
  * las l&iacute;neas anteriomente impresas en pantalla correspondientes a la diferencia de las lineas anterioemres + nuevas lineas y  maximo de lineas.
  * 
  *  Al mismo tiempo se inicializa la siguiente posici&oacute;n de las marcas de span usadas para las b&uacute;squedas, 
  *  ya que al añadir m&aacute;s lineas y quitar la diferencia, la posici&oacute;n de la b&uacute;squeda se ha modificado. 
  * @param newLines
  * @returns
  */
 function getResultLines(newLines){
	 var contSpan = 0;
	 var resultLines = "";	
	 if(parseInt(linesCount, 10) + parseInt(newLines, 10) <= parseInt(maxLines,10)){
		 resultLines +=  $("#" + idContainer).html();			
	 }
	 else{
		 diffLines = (parseInt(linesCount, 10) + parseInt(newLines, 10)) - parseInt(maxLines,10);	
		 var diff = diffLines;
		 $("#" + idContainer + " > div").each(function () { 			 
			 if(diff != 0){
				 contSpan += $(this).children("span").length;
				 diff --;				
			 }
			 else{					 
				 resultLines += "<div>" + $(this).html() + "</div>"; 			
			 }
		 });		
	 }
	 linesCount = (parseInt(linesCount, 10) + parseInt(newLines, 10))-parseInt(diffLines, 10);
	 next_position = next_position - contSpan;
	 return resultLines;
 }
 
 /**
  * Funci&oacute;n que marca de otro color el texto buscado en el que se encuentra en pantalla la posici&oacute;n
  * actual (next_position), seg&uacute;n se llame en sucesivas ocasiones a esta funci&oacute;n se ir&aacute;n marcando
  * las siguientes ocurrencias encontradas en la busqueda controlando la posici&oacute;n del scroll seg&uacute;n
  * la posici&oacute;n actual del resultado de la busqueda seleccionada.
  * @param search_text Texto a buscar
  * @returns
  */
 function markNextText(search_text){
	 var result = false;	
	 var allSpans = $("pre > div > span");
	 var elementTopPos;
	 var elementLeftPos;
	 
	 if(allSpans.length == 0){
		 return result;
	 }
	 
	 
	 allSpans.each(function(id){
		 if(id == next_position ){
			 elementTopPos = $(this).offset().top;
			 elementLeftPos = $(this).offset().left;
			 $(this).removeClass("highlight").addClass("nextHighLight"); 
		 }
		 else if(id == next_position - 1 && next_position - 1 >= 0 ){
			$(this).removeClass("nextHighLight").addClass("highlight"); 	 
		 }
			 		
	 });
	 searchOp = searchOp + 1; 
	 next_position = next_position + 1;
	 
	 var containerTopPos = $("#"+ idContainer ).offset().top;
	 var containerLeftPos = $("#"+ idContainer ).offset().left;
	
	 var width = $('#'+idScrollElement).width();
	 var movTop = (elementTopPos - containerTopPos);
	 var movLeft = (elementLeftPos - containerLeftPos);
	 
	 $('#'+idScrollElement).scrollTop(movTop);
	 setScrollTopPosition($('#'+idScrollElement).scrollTop());
	 
	 if(elementLeftPos > (width + containerLeftPos)){
		 $('#'+idScrollElement).scrollLeft(elementLeftPos - containerLeftPos);
	 }
	 else if(elementLeftPos < (width + containerLeftPos)){
		 $('#'+idScrollElement).scrollLeft(0);
	 }
	 setScrollLeftPosition($('#'+idScrollElement).scrollLeft());
//	 console.log("Movimiento de  seleccion: scrollTopPosition : " + scrollTopPosition + " scrollLefPosition:" + scrollLefPosition);
	 if(allSpans.length == next_position){ //==		
		 result = true;
	 }	  
	 return result;
	 
 }
 
 /**
  * Funci&oacute;n que habilita y deshabilita el elemento indicado con el id
  * @param idElement id del elemento.
  * @param switchOn boolean true,false
  * @returns
  */
 function activeElement(idElement, switchOn){
	 if(switchOn){//activar
		$("#"+idElement).removeClass("btn-log_inactive").addClass("btn-log"); 
		$("#"+idElement).prop('disabled', false);
	 }
	 else{//desactivar
		 $("#"+idElement).removeClass("btn-log").addClass("btn-log_inactive"); 
		 $("#"+idElement).prop('disabled', true);
	 }
 }
 
 /**
  * Reinicia las variables globales usadas en la operaci&oacute;n de B&uacute;squeda.
  * @returns
  */
 function resetSearch(text,DateTime){
	 addResult  = false;
	 text2Search = text;
	 date2Search = DateTime;
	 isFinal = true;			 			 
	 linesCount = 0;							
	 filterOp = 0;
	 searchOp = 0;				
	 diffLines = 0;
	 next_position = searchOp;
	 scrollTopPosition = 0;
	 scrollLefPosition = 0;
	 param_reset = "&reset=yes";
 }
 /**
  * Reinicia las variables globales usadas en la operaci&oacute;n de Filtrado.
  * @returns
  */
  function resetFilter(startDateTime, endDateTime, level){
	  addResult  = false;
		initDate2Filter = startDateTime;
		endDate2Filter = endDateTime;
		level2Filter = level;
			 			 
		linesCount = 0;							
		filterOp = 0;
		searchOp = 0;				
		diffLines = 0;
		next_position = searchOp;
		scrollTopPosition = 0;
		scrollLefPosition = 0;
		param_reset = "&reset=yes";
  }
 
 /*******Inicializar variables globales  *****/
 
 function setLinesCount(numLines){
	 linesCount = parseInt(numLines, 10);
 }

 function setIsReset (bool){
	isReset = (bool === 'true');	
 }

 function setAddResult (bool){
	 addResult = (bool === 'true');
 }
	
 function setFilterOp(intNum){
	 filterOp = parseInt(intNum, 10);
 }
	
 function setSearchOp(intNum){
	 searchOp = parseInt(intNum, 10);
 }

 function setText2Search(txt){
	 text2Search = txt; 
 }

 function setDiffLines(intNum){
	 diffLines = parseInt(intNum, 10);
 }

 function setScrollTopPosition(num){
	 scrollTopPosition = parseFloat(num);
 }
 
 function setScrollLeftPosition(num){
	 scrollTopPosition = parseFloat(num);
 }	
	
 function setIdContainer (id){
	 idContainer = id;
 }
 
 function setIdScrollElement (id){
	 idScrollElement = id;
 }
 
 function setIdErrorTxtLog (id){
	 idErrorTxtLog = id;
 }
 
 function setIdOkTxtLog (id){
	 idOkTxtLog = id;
 }
 
 function setIdAdvice(id){
	 idAdvice = id;
 }
 
 function setIdProgrees(id){
	 idProgress = id;
 }