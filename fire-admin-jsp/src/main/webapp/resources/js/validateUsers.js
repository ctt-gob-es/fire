/**
 * 
 */

$(document).ready(function(){
	
	var emailPattr=/^[\w\-\.\+]+\@[a-zA-Z0-9\.\-]+\.[a-zA-z0-9]{2,4}$/;
		
	/*Validación de campos del formulario*/
	$("#formUser").submit(function(e){
	
		$( "label" ).each(function( index ) {
			if ( this.style.color == "red" ) {
			      this.style.color = "#434343";
			      var idInput=$(this).attr('for');
			      $('#'+idInput).css({backgroundColor:'#D3D3D3'});
			    } 
		});
				
		var ok=true;
		var msg="";
		var email=$("#email").val();
		if(email!=null && $.trim(email).length != 0){
			if(!emailPattr.test(email)){
				e.preventDefault();			
				$('label[for=email]').css({color:'red'});
				$('#email').css({backgroundColor:'#fcc'});
				msg = msg+ "Debe introducir un formato de correo electrónico correcto\n";
				ok= false;
			}
		}

		if((op==0 || op==1) && $("#login-usr").val()==""){
			e.preventDefault();
			$('label[for=login-usr]').css({color:'red'});
			$('#login-usr').css({backgroundColor:'#fcc'});
			msg = msg+ "Debe introducir un nombre de login para el usuario\n";
			ok= false;
		}
		else if(op==1 && $("#login-usr").val()!=""){			
			var data="&login-usr="+$("#login-usr").val();
			 $.ajax({
				 	async:false,
				 	cache:false,
		            type: "POST",
		            url: "../newUser?op=3",
		            data: data,		         
		            success: function (responseText) {
		            	if(responseText!="new"){
		            		e.preventDefault();
			    			$('label[for=login-usr]').css({color:'red'});
			    			$('#login-usr').css({backgroundColor:'#fcc'});
			    			msg = msg+responseText+"\n";		    			
			    			ok= false;
		            	}	            		    					    				            	                     
		            }		           
		        });
		}
		if(op==1 && $("#passwd-usr").val()==""){
			e.preventDefault();
			$('label[for=passwd-usr]').css({color:'red'});
			$('#passwd-usr').css({backgroundColor:'#fcc'});
			msg = msg+ "Debe introducir una clave para el usuario\n";
			ok= false;
		}
		if(op==1 && $("#passwd-usr-copy").val()==""){
			e.preventDefault();
			$('label[for=passwd-usr-copy]').css({color:'red'});
			$('#passwd-usr-copy').css({backgroundColor:'#fcc'});
			msg = msg+ "Debe introducir repetir clave para el usuario\n";
			ok= false;
		}
		if(op==1 && $("#passwd-usr").val()!="" 
			&& $("#passwd-usr-copy").val()!="" 
			&& $("#passwd-usr").val()!=$("#passwd-usr-copy").val()){
			e.preventDefault();
			$('label[for=passwd-usr]').css({color:'red'});
			$('label[for=passwd-usr-copy]').css({color:'red'});
			$('#passwd-usr').css({backgroundColor:'#fcc'});			
			$('#passwd-usr-copy').css({backgroundColor:'#fcc'});
			msg = msg+ "Las claves introducidas debe ser iguales\n";
			ok= false;
		}
		
		if($("#usr-name").val()==""){
			e.preventDefault();
			$('label[for=usr-name]').css({color:'red'});
			$('#usr-name').css({backgroundColor:'#fcc'});
			msg = msg+ "Debe introducir el nombre del usuario\n";
			ok= false;
		}
		if($("#usr-surname").val()==""){
			e.preventDefault();
			$('label[for=usr-surname]').css({color:'red'});
			$('#usr-surname').css({backgroundColor:'#fcc'});
			msg = msg+ "Debe introducir el/los apellido/s del usuario\n";
			ok= false;
		}
		if(!ok){
			alert(msg);
		}		
		return ok;
	});//fin formUser submit!
	 
}); //fin document ready!
	
     
