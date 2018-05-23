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
	var linesCount = 0;
	var maxLines = 100;
	var isReset = false;
	var addResult = false;
	var filterOp = 0;
	var searchOp = 0;
 /*****************Funciones:******************************/
 
 /**
  * 
  * @returns
  */
 function printErrorResult(JSONData){
	var html = "";
	$("#error-txt-log").html(html);
	$("#ok-txt-log").html(html); 
	 $("#ok-txt-log").hide();
	html = JSONData.Error[0].Message;
	$("#error-txt-log").append(html);
	$("#error-txt-log").css('display:inline-block');
	$("#error-txt-log").show();
	 
 }
 
 function printOkResult(JSONData){
		var html = "";
		$("#error-txt-log").html(html);
		$("#error-txt-log").hide();
		$("#ok-txt-log").html(html);
		html = JSONData.Ok[0].Message;
		$("#ok-txt-log").append(html);
		$("#ok-txt-log").css('display:inline-block');
		$("#ok-txt-log").show();
		 
	 }
 /**
  * 
  * @param JSONData
  * @returns
  */
 function printResult(idContainer, JSONData){
	
	 var data = "";
	 var content = getResultLines($("#Nlines").val());
	 $("#"+ idContainer ).html("");
	 $("#error-txt-log").hide();
	 $("#ok-txt-log").hide();
	 $("#advice").hide();
	 
	 if(JSONData.hasOwnProperty('Tail')){		 
		 data = JSONData.Tail[0].Result;		
		 content = "";
	 }
	 else if(JSONData.hasOwnProperty('More')){
		 data = JSONData.More[0].Result;
	 }
	 else if(JSONData.hasOwnProperty('Search')){
		 data = JSONData.Search[0].Result;
		 if(!addResult){
			 content = ""; 
		 }
	 }
	 else if(JSONData.hasOwnProperty('Filtered')){
		 data = JSONData.Filtered[0].Result;
		 if(!addResult){
			 content = ""; 
		 }
	 }
	 
	 var arrHtml = data.split("</br>");
	 for (i = 0; i < arrHtml.length-1; i++) {
		 content += "<div>" + arrHtml[i] + "</div>";		
	 }
	 $("#"+ idContainer).append(content);
  }
 /**
  * 
  * @param nlines
  * @returns
  */
 function getTail(idContainer,nlines){

	 	var arrFields = ["Nlines"];

	 	var ok = validateFields(arrFields);						
		if(ok){
			 var url = "../LogAdminService?op=6&nlines="+nlines+"&fname="+$("#fileName").text();
			 $.post(url, function(data){		
				 var JSONData = JSON.parse(data);
				  if(JSONData.hasOwnProperty('Tail')){
					  printResult(idContainer,JSONData);  
				  }
				   else{
					   printErrorResult(JSONData);  
				   }  
				  addResult=false;
			}); 
		}
 }
 
 /**
  * 
  * @param nlines
  * @returns
  */
 function getMore(idContainer,nlines){

	var arrFields = ["Nlines"];
	var ok = validateFields(arrFields);						
	if(ok){
		 var url = "../LogAdminService?op=7&nlines="+nlines;
		 $.post(url, function(data){		
			 var JSONData = JSON.parse(data);
			  if(JSONData.hasOwnProperty('More')){
				  printResult(idContainer,JSONData);			
			  }
			   else {			   
				   printErrorResult(JSONData);  
			   }      	            
		}); 
	}
	
 }

 /**
  * 
  * @param text
  * @param date
  * @param nlines
  * @returns
  */
 function searchText(idContainer,nlines, text, date){
	
	var DateTime =  getlongDateTime(date);
	var arrFields = ["search_txt"];
	filterOp = 0;
	var ok = validateFields(arrFields);						
	if(ok){
		 var url = "../LogAdminService?op=8&nlines=" + nlines + "&search_txt=" + text + "&search_date=" + DateTime;
		 if(searchOp == 0){
			 addResult  = false;
		 }
		 else{
			 addResult  = true; 
		 }
		 				
		 var isFinal = markNextText(text,searchOp);
		 
		 if(isFinal || searchOp == 0){
			 $.post(url, function(data){		
				  var JSONData = JSON.parse(data);
				  if(JSONData.hasOwnProperty('Search')){
				  	printResult(idContainer,JSONData);
				  	isFinal = markNextText(text,searchOp);
				  
				  	
				  }
				   else {
				   	printErrorResult(JSONData);  
				  }
				  
			});
			 
		 }
		 
	}
	
 }
 
 /**
  * 
  * @param nlines
  * @param startDate
  * @param endDate
  * @param level
  * @returns
  */
 function getFiltered(idContainer,nlines, startDate, endDate, level){

	 var startDateTime =  getlongDateTime(startDate);
	 var endDateTime =  getlongDateTime(endDate);
	 var arrFields = ["Nlines","level_select"];
	 var ok = validateFields(arrFields);						
	if(ok){	
		searchOp = 0;
		if(filterOp == 0){
			 addResult  = false;
		 }
		 else{
			 addResult  = true; 
		 }
		filterOp = filterOp + 1;		
		var url = "../LogAdminService?op=9&nlines=" + nlines + "&start_date=" + startDateTime + "&end_date=" + endDateTime + "&level=" + level;		 
		 $.post(url, function(data){		
			 var JSONData = JSON.parse(data);
			  if(JSONData.hasOwnProperty('Filtered')){
			  	printResult(idContainer,JSONData);  
			  }
			   else {
			   	printErrorResult(JSONData);  
			  }      	            
		});  
	} 	 	
	 
 }
 
 
 /**
  * funcion que devuelve objeto fecha (Date), respecto de los datos de entrada (date) con formato (dd/mm/yyyy HH:mm:ss)
  * @param date
  * @returns
  */
 function getlongDateTime(date){
	 var result = -1;
	 var day = date.substr (0,2);
	 var month = date.substr (3,2);
	 var year = date.substr(6,4);

	 var hour = date.substr(11,2);
	 var minute = date.substr(14,2);
	 var seconds = date.substr(17,2);
	 	 
	 var DateTime =  new Date();
	 if(day != null && typeof day != "undefined"
		 	&& month != null && typeof month != "undefined"
			&& year != null && typeof year != "undefined"){
		 DateTime.setFullYear(year, month - 1, day);
	 }		 		 		 
	 if(!isNaN(DateTime.getTime())){
		 if(hour != null && typeof hour != "undefined"){
			 DateTime.setHours(hour); 
		 }	 
		 if(minute != null && typeof minute != "undefined"){
			 DateTime.setMinutes(minute); 
		 }
		 if(seconds != null && typeof seconds != "undefined"){
			 DateTime.setSeconds(seconds); 
		 }
		  result = DateTime.getTime();			
	 }

	 return result;
	
 }
 
 /**
  * Función de validación
  * @param fields
  * @returns
  */
 function validateFields(fields){
	 
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
				msg = msg + "Debe introducir un texto para realizar la búsqueda.";
				ok = false;			
			}		
			break;		
		case "level_select":
			if($("#level_select").val() == ""){								
				$('label[for=level_select]').css({color:'red'});
				$('#level_select').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir un nivel de log para filtrar.";
				ok = false;			
			}
			break;
		}
	 }
	 
	if(!ok){
		alert(msg);
	}	
	 
	 return ok;  	 
 }
 
 /**
  * 
  * @returns
  */
 function download(){
	 var url = "../LogAdminService?op=10&fname=" + file;
	 location.href = url;
 }
 
 /**
  * 
  * @returns
  */
 function goReturn(){	
	 location.href = 'LogsFileList.jsp?name-srv=' + server;	  
 }
 


 
 /**
  * 
  * @returns
  */
 function reset(idContainer){
	 isReset = true;	
	 addResult = false;
	 filterOp = 0;
	 searchOp = 0;
	 linesCount = 0;

	 var html = "";
	 $("#error-txt-log").html(html);
	 $("#ok-txt-log").html(html); 
	 $("#ok-txt-log").hide();
	 $("#error-txt-log").hide();
				 
	 var url = "../LogAdminService?op=5";
	 $.post(url,function(data){	
	 if(data != null && typeof(data) != "undefined"){
		 var JSONData = JSON.parse(data);
		   if(JSONData.hasOwnProperty('Error')){
			   printErrorResult(JSONData);
			   isReset = false;
		   }
		   else{			   	  
			   $("#" + idContainer).html("");
			   var urlOpen = "../LogAdminService?op=4&fname=" + file + "&name-srv=" + server + "&reset=yes";			   		 
			   $.post(urlOpen,function(dat){	
				   if(dat != null && typeof(dat) != "undefined"){
					   isReset = true;
					   $("#advice").show();
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
 }
 
 function closeFile(){
	 
	 var url = "../LogAdminService?op=5";
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
  * 
  * @param newLines
  * @returns
  */
 function getResultLines(idContainer,newLines){
	 var diffLines = 0;
	 var resultLines = "";	
	 if(parseInt(linesCount, 10) + parseInt(newLines, 10) <= parseInt(maxLines,10)){
		 resultLines +=  $("#" + idContainer).html();			
	 }
	 else{
		 var diffLines = (parseInt(linesCount, 10) + parseInt(newLines, 10)) - parseInt(maxLines,10);	
		 var diff = diffLines;
		 $("#" + idContainer + " > div").each(function () { 
			 if(diff != 0){
				 diff --;				
			 }
			 else{					 
				 resultLines += "<div>" + $(this).html() + "</div>"; 			
			 }
		 });		
	 }
	 linesCount = (parseInt(linesCount, 10) + parseInt(newLines, 10))-parseInt(diffLines, 10);
	 return resultLines;
 }
 
 /**
  * 
  * @param search_text
  * @returns
  */
 function markNextText(search_text, next_position){
	 var result = false;	
	 var allSpans = $("pre > div > span");
	 if(allSpans.length == 0){
		 return result;
	 }	
	 allSpans.each(function(id){		 
		 if(id == next_position){
			 $(this).removeClass( "highlight" ).addClass( "nextHighLight" ); 
		 }
		 else if(id == next_position - 1 && next_position - 1 >= 0 ){
			$(this).removeClass("nextHighLight").addClass("highlight" ); 	 
		 }
			 		
	 });
	 	 
	 if(allSpans.length == next_position){
		 result = true;; 
	 }

	 searchOp = searchOp + 1; 
	 return result;
	 
 }
 