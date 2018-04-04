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
		if (confirm('¿Está seguro que quiere cambiar la contraseña?')) { 		      
			return true;
		}			   
		return false;
	}