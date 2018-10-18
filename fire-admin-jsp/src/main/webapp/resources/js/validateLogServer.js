/**
 * 
 */

$(document).ready(function(){
	
	
	/*Validación de campos del formulario*/
	$("#formLogServer").submit(function(e){
	
		$( "label" ).each(function( index ) {
			if ( this.style.color == "red" ) {
			      this.style.color = "#434343";
			      var idInput=$(this).attr('for');
			      $('#'+idInput).css({backgroundColor:'#D3D3D3'});
			    } 
		});
				
		var ok = true;
		var msg = "";
		
		var name = $("#name-srv").val();
		
		if($("#name-srv").val()==""){			
			e.preventDefault();			
			$('label[for=name-srv]').css({color:'red'});
			$('#name-srv').css({backgroundColor:'#fcc'});
			msg = msg + "Debe introducir  un nombre de servidor\n";
			ok = false;			
		}		
		if($("#clave").val() == ""){
			e.preventDefault();
			$('label[for=clave]').css({color:'red'});
			$('#clave').css({backgroundColor:'#fcc'});
			msg = msg + "Debe introducir la clave\n";
			ok = false;
		}
		
		
		
		if($("#url").val() == ""){
			e.preventDefault();
			$('label[for=url]').css({color:'red'});
			$('#url').css({backgroundColor:'#fcc'});
			msg = msg + "Debe introducir una dirección URL\n";
			ok = false;
		}
				
		
		var urlvalid = ValidURL($("#url").val());
		
		if(!urlvalid){
			e.preventDefault();
			$('label[for=url]').css({color:'red'});
			$('#url').css({backgroundColor:'#fcc'});
			msg = msg + "Debe introducir una dirección URL válida (dirección del servicio sin parámetros)\n";
			ok = false;
		}
		if(!ok){
			alert(msg);
		}		
		return ok;
	});//fin formUser submit!
	 
	
}); //fin document ready!

function comprobarServidorLog(){
	
	$( "#NoOkIcon" ).hide();
	$( "#okIcon").hide();
	$( "#messageNoOk" ).html("");	
	$( "#messageOk" ).html("");
	$( "#urlStatus" ).show();

	var url = $("#url").val();
	var verifySsl = $("#verifyssl").prop('checked') ? "true" : "false";
	$.post( "../LogAdminService?op=0&url="+url+"&verifyssl="+verifySsl, function( data ) {
		  var JSONData = JSON.parse(data);
		  if(JSONData.hasOwnProperty('Error')){
			  $( "#NoOkIcon" ).show();
			  $( "#messageNoOk" ).html(JSONData.Error[0].Message);			  
		  }
		  else if(JSONData.hasOwnProperty('Ok')){
			  $( "#okIcon").show();
			  $( "#messageOk" ).html(JSONData.Ok[0].Message);
		  }		 		 
	});
}


function ValidURL(url) {

	//Patron validación de url sín los parámetros (? = &) 
	var pattern = /^([a-z][a-z0-9\*\-\.]*):\/\/(?:(?:(?:[\w\.\-\+!$&'\(\)*\+,;=]|%[0-9a-f]{2})+:)*(?:[\w\.\-\+%!$&'\(\)*\+,;=]|%[0-9a-f]{2})+@)?(?:(?:[a-z0-9\-\.]|%[0-9a-f]{2})+|(?:\[(?:[0-9a-f]{0,4}:)*(?:[0-9a-f]{0,4})\]))(?::[0-9]+)?(?:[\/|\?](?:[\w#!:\.\+@!$'~*,;\/\(\)\[\]\-]|%[0-9a-f]{2})*)?$/g;	  
	if(!pattern.test(url)) {	
	    return false;
	 } else {		
		 return true;
	  }
	}
