/**
 * 
 */
$(document).ready(function(){
		     
	     /*Validación de campos del formulario*/
	 	
	 	var emailPattr=/^[\w\-\.\+]+\@[a-zA-Z0-9\.\-]+\.[a-zA-z0-9]{2,4}$/;
	 		
	 	$("#frmApplication").submit(function(e){
	 		
	 		$( "label" ).each(function( index ) {
				if ( this.style.color == "red" ) {
				      this.style.color = "#434343";
				      var idInput=$(this).attr('for');
				      $('#'+idInput).css({backgroundColor:'#D3D3D3'});
				    } 
			});
	 		
	 		var email=$("#email-resp").val();
	 		var msg="";
	 		var ok=true;
	 		if(email!=null && $.trim(email).length != 0){
	 			if(!emailPattr.test(email)){
	 				$('label[for=email-resp]').css({color:'red'});
					$('#email-resp').css({backgroundColor:'#fcc'});
	 				e.preventDefault();
	 				msg=msg+"Debe introducir un formato de correo electrónico correcto\n";	 				
	 				ok= false;
	 			}
	 		}
	 		
	 		if ($("#nombre-app").val() == "" ){
	 			$('label[for=nombre-app]').css({color:'red'});
				$('#nombre-app').css({backgroundColor:'#fcc'});
	 			e.preventDefault();
	 			msg=msg+"El nombre de la aplicación no puede estar vacío\n";			
	 			ok= false;
	 		}
	 		
	 		if ($("#nombre-resp").val() == "" ){
	 			$('label[for=nombre-resp]').css({color:'red'});
				$('#nombre-resp').css({backgroundColor:'#fcc'});
	 			e.preventDefault();
	 			msg=msg+"El nombre del responsable no puede estar vacío\n";		
	 			ok= false;
	 		}
	 		
	 		if($("#id-certificate").val() == "0"){
	 			$('label[for=id-certificate]').css({color:'red'});
				$('#id-certificate').css({backgroundColor:'#fcc'});
	 			e.preventDefault();
	 			msg=msg+"El certificado no puede estar vacío, seleccione un certificado.  \n";
	 			ok= false;
	 		}
	 		if(!ok){
				alert(msg);
			}
	 		return ok;
	 	});//fin frmApplication submit!
	          
	});