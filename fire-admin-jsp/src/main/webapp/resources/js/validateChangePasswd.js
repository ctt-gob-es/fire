/**
 * 
 */
$(document).ready(function(){
		
		/*Validación de campos del formulario*/
		$("#frmChangePass").submit(function(e){
			var ok = true;
			/*Repasa todos los campos que se han marcado en rojo (con error) anteriormente para inicializarlos*/
			$( "label" ).each(function( index ) {
				if ( this.style.color == "red" ) {
				      this.style.color = "#434343";
				      var idInput=$(this).attr('for');
				      $('#'+idInput).css({backgroundColor:'#D3D3D3'});
				    } 
			});	
		
				var msg = "";
				if($("#old_passwd-usr").val() == ""){
					e.preventDefault();
					$('label[for=old_passwd-usr]').css({color:'red'});
					$('#old_passwd-usr').css({backgroundColor:'#fcc'});
					msg = msg + "Debe introducir la contraseña antigua\n";			
					ok = false;
					
				}
				if($("#passwd-usr_1").val() == ""){
					e.preventDefault();
					$('label[for=passwd-usr_1]').css({color:'red'});
					$('#passwd-usr_1').css({backgroundColor:'#fcc'});
					msg = msg+ "Debe introducir la contraseña nueva\n";			
					ok= false;
					
				}
				if($("#passwd-usr_2").val() == ""){
					e.preventDefault();
					$('label[for=passwd-usr_2]').css({color:'red'});
					$('#passwd-usr_2').css({backgroundColor:'#fcc'});
					msg = msg + "Debe introducir repetir contraseña nueva\n";			
					ok = false;
					
				}
				
				if($("#passwd-usr_1").val() != "" 
					&& $("#passwd-usr_2").val() != "" 
					&& $("#passwd-usr_1").val() != $("#passwd-usr_2").val()){
					e.preventDefault();
					$('label[for=passwd-usr_1]').css({color:'red'});
					$('label[for=passwd-usr_2]').css({color:'red'});
					$('#passwd-usr_1').css({backgroundColor:'#fcc'});			
					$('#passwd-usr_2').css({backgroundColor:'#fcc'});
					msg = msg + "Las contraseñas introducidas deben ser iguales\n";
					ok = false;				
				}
				
				if(!ok){
					alert(msg);
					return ok;
				}								
				confirmar();	
			
		});//fin formUser submit!
		 
		
		
	}); //fin document ready!
	
	
	function confirmar() { 
		return confirm('¿Está seguro que quiere cambiar la contraseña?');
	}
	
	
	
$(document).ready(function(){
		
		/*Validación de campos del formulario*/
		$("#frmMailChangePass").submit(function(e){
			var ok = true;
			/*Repasa todos los campos que se han marcado en rojo (con error) anteriormente para inicializarlos*/
			$( "label" ).each(function( index ) {
				if ( this.style.color == "red" ) {
				      this.style.color = "#434343";
				      var idInput=$(this).attr('for');
				      $('#'+idInput).css({backgroundColor:'#D3D3D3'});
				    } 
			});	
			
			
			var msg = "";
			if($("#mail").val() == ""){
				e.preventDefault();
				$('label[for=mail]').css({color:'red'});
				$('#mail').css({backgroundColor:'#fcc'});
				msg = msg + "El campo no puede estar vacio, debe introducir un nombre de usuario o correo electrónico\n";
				ok = false;
			}
			if($("#password").val() == ""){
				e.preventDefault();
				$('label[for=password]').css({color:'red'});
				$('#password').css({backgroundColor:'#fcc'});
				msg = msg+ "Debe introducir la contraseña nueva\n";			
				ok= false;
				
			}
			if($("#passwordcopy").val() == ""){
				e.preventDefault();
				$('label[for=passwordcopy]').css({color:'red'});
				$('#passwordcopy').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir repetir contraseña nueva\n";			
				ok = false;
				
			}
			
			if($("#password").val() != "" 
				&& $("#passwordcopy").val() != "" 
				&& $("#password").val() != $("#passwordcopy").val()){
				e.preventDefault();
				$('label[for=password]').css({color:'red'});
				$('label[for=passwordcopy]').css({color:'red'});
				$('#password').css({backgroundColor:'#fcc'});			
				$('#passwordcopy').css({backgroundColor:'#fcc'});
				msg = msg + "Las contraseñas introducidas deben ser iguales\n";
				ok = false;				
			}
			
			
			if(!ok){
				alert(msg);
				return ok;
			}	
		});//fin formUser submit!
		 
		
		
}); //fin document ready!