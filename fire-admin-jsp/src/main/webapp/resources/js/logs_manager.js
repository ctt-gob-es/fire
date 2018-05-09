/**
 * 
 */
 $(document).ready(function(){
	 
		 
	 var url = "../LogAdminService?op=6&nlines=20&fname="+$("#fileName").text();
	//Obtine las ultimas lineas por defecto al cargar la pagina
   $.get(url, function(data){
   	$("#logResult").html("");
       var JSONData = JSON.parse(data);     
     if(JSONData.hasOwnProperty('Error')){
    	   printErrorResult(JSONData);  
       } 
     else{
    	 printResult(JSONData);   
     }
  	   
   }); 
	 
   $( function() {
     $( "#startDate" ).datepicker({
       changeMonth: true,
       changeYear: true
     });
     $('#startTime').timepicker();
     $( "#endDate" ).datepicker({
         changeMonth: true,
         changeYear: true
       });
       $('#endTime').timepicker();
   } );

   
   
  });//fin de document ready
  
  
 /*****************Funciones:******************************/
 
 /**
  * 
  * @returns
  */
 function printErrorResult(JSONData){
	var html = "";
	$("#error-txt-log").html(html);
	html = JSONData.Error[0].Message;
	$("#error-txt-log").append(html);
	$("#error-txt-log").css('display:inline-block');
	$("#error-txt-log").show();
	 
 }
 /**
  * 
  * @param JSONData
  * @returns
  */
 function printResult(JSONData){
	 var html = "";
	
	 $("#error-txt-log").hide();
	 $("#logResult").html("");
	 if(JSONData.hasOwnProperty('Tail')){
		 html = JSONData.Tail[0].Result; 
	 }
	 else if(JSONData.hasOwnProperty('More')){
		 html = JSONData.More[0].Result;
	 }
	 else if(JSONData.hasOwnProperty('Search')){
		 html = JSONData.Search[0].Result;
	 }
	 else if(JSONData.hasOwnProperty('Filtered')){
		 html = JSONData.Filtered[0].Result;
	 }
	 $("#logResult").append(html);
  }
 /**
  * 
  * @param nlines
  * @returns
  */
 function getTail(nlines){

	 	var arrFields = ["Nlines"];

	 	var ok = validateFields(arrFields);						
		if(ok){
			 var url = "../LogAdminService?op=6&nlines="+nlines+"&fname="+$("#fileName").text();
			 $.post(url, function(data){		
				 var JSONData = JSON.parse(data);
				  if(JSONData.hasOwnProperty('Tail')){
					  printResult(JSONData);  
				  }
				   else{
					   printErrorResult(JSONData);  
				   }      	            
			}); 
		}
 }
 
 /**
  * 
  * @param nlines
  * @returns
  */
 function getMore(nlines){
	var arrFields = ["Nlines"];
	var ok = validateFields(arrFields);						
	if(ok){
		 var url = "../LogAdminService?op=7&nlines="+nlines;
		 $.post(url, function(data){		
			 var JSONData = JSON.parse(data);
			  if(JSONData.hasOwnProperty('More')){
				  printResult(JSONData);  
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
 function searchText(nlines, text, date){
	 
	var DateTime =  getlongDateTime(date);
	var arrFields = ["search_txt"];

	var ok = validateFields(arrFields);						
	if(ok){
		 var url = "../LogAdminService?op=8&nlines=" + nlines + "&search_txt=" + text + "&search_date=" + DateTime;

		 $.post(url, function(data){		
			 var JSONData = JSON.parse(data);
			  if(JSONData.hasOwnProperty('Search')){
			  	printResult(JSONData);  
			  }
			   else {
			   	printErrorResult(JSONData);  
			   }      	            
		}); 
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
 function getFiltered(nlines, startDate, endDate, level){
	 
	 
	 var startDateTime =  getlongDateTime(startDate);
	 var endDateTime =  getlongDateTime(endDate);
	 var arrFields = ["Nlines","level_select"];
	 var ok = validateFields(arrFields);						
	if(ok){	 	
		var url = "../LogAdminService?op=9&nlines=" + nlines + "&start_date=" + startDateTime + "&end_date=" + endDateTime + "&level=" + level;		 
		 $.post(url, function(data){		
			 var JSONData = JSON.parse(data);
			  if(JSONData.hasOwnProperty('Filtered')){
			  	printResult(JSONData);  
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

	 var day = date.substr (0,2);
	 var month = date.substr (3,2);
	 var year = date.substr(6,4);
	 
	 var hour = date.substr(11,2);
	 var minute = date.substr(14,2);
	 var seconds = date.substr(17,2);
	 	 
	 var DateTime =  new Date();
	 DateTime.setFullYear(year, month - 1, day);
	 
	 if(hour != null && typeof hour != "undefined"){
		 DateTime.setHours(hour); 
	 }	 
	 if(minute != null && typeof minute != "undefined"){
		 DateTime.setMinutes(minute); 
	 }
	 if(seconds != null && typeof seconds != "undefined"){
		 DateTime.setSeconds(seconds); 
	 }
	 var result = DateTime.getTime();
	 
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
				$('label[for=search_txt]').css({color:'red'});
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