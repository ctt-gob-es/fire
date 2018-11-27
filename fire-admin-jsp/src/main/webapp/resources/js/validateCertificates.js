/**
 * 
 */
$(document).ready(function(){
	
	/*Validación de campos del formulario*/
	
	$("#frmCertificate").submit(function(e){
		
		var msg = "";
 		var ok = true;

 		$( "label" ).each(function( index ) {
			if ( this.style.color == "red" ) {
			      this.style.color = "#434343";
			      var idInput=$(this).attr('for');
			      $('#'+idInput).css({backgroundColor:'#D3D3D3'});
			    } 
		});
 			
 		
 		if ($("#nombre-cer").val() == "" ){
 			$('label[for=nombre-cer]').css({color:'red'});
			$('#nombre-cer').css({backgroundColor:'#fcc'});
 			e.preventDefault();
 			msg = msg + "El nombre del certificado no puede estar vacío\n";			
 			ok = false;
 		}
 		
 		
 		if($("#fichero-firma-prin").val() == "" && $("#fichero-firma-resp").val() == ""  && op == 1){
 			$('label[for=fichero-firma-prin]').css({color:'red'}); 			
			$('#fichero-firma-prin').css({backgroundColor:'#fcc'});
			$('label[for=fichero-firma-resp]').css({color:'red'}); 			
			$('#fichero-firma-resp').css({backgroundColor:'#fcc'});
 			e.preventDefault();
 			msg = msg + "El certificado no puede estar vacío, seleccione un certificado '*.cer' \n";
 			ok = false;
 		}
 		
	
 		var cert1 = $("#cert-prin").html().trim();
 		var cert2 = $("#cert-resp").html().trim();
 				
		if(cert1.length == 0 && cert2.length == 0  && op==2){
 			$('label[for=fichero-firma-prin]').css({color:'red'}); 			
			$('#fichero-firma-prin').css({backgroundColor:'#fcc'});
			$('label[for=fichero-firma-resp]').css({color:'red'}); 			
			$('#fichero-firma-resp').css({backgroundColor:'#fcc'});
 			e.preventDefault();
 			msg = msg + "El certificado no puede estar vacío, seleccione un certificado '*.cer' \n";
 			ok = false;
 		}
 		
 		if(!ok){
			alert(msg);
		}
 		return ok;
 	});//fin formCertificate submit!
});