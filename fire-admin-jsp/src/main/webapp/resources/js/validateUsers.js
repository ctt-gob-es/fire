
/**
 * 
 */

$(document).ready(function(){
	
	var emailPattr = /^[\w\-\.\+]+\@[a-zA-Z0-9\.\-]+\.[a-zA-z0-9]{2,4}$/;
	
	var appList;
	
	// Si recibimos el id de usuario (visualizacion y edicion) mostramos el listado de aplicaciones
	// de las que el usuario es responsable
	var urlParams = "requestType=" + requestType;
	if (idUser) {
		urlParams += "&id-users=" + idUser;
	
		$.get("user?" + urlParams, 
			function(data){
				
	    		var jsonData = JSON.parse(data);
	    		
	    		if (requestType == "getRecordsUsersApp" && jsonData.AppList && jsonData.AppList.length > 0) {
	    			
	    			$("#data").html("");
	    			appList = jsonData.AppList;
	    			//rolesWithResponsable = [];
	    			printApplicationsByUserTable(jsonData, jsonData.AppList.length);
	    		}
	    });
	}
	
	
		
	/*Validación de campos del formulario*/
	$("#formUser").submit(function(e){
	
		$( "label" ).each(function( index ) {
			if ( this.style.color == "red" ) {
			      this.style.color = "#434343";
			      var idInput=$(this).attr('for');
			      $('#'+idInput).css({backgroundColor:'#D3D3D3'});
			    } 
		});
				
		var ok = true;
		var msg = "";
		var email = $("#email").val();
		
		if($("#role-usr").val() == ""){
			e.preventDefault();
			$('label[for=role-usr]').css({color:'red'});
			$('#role-usr').css({backgroundColor:'#fcc'});
				msg = msg + "Debe asignar un rol al usuario\n";
				ok = false;
			
		}
		
		if($("#usr-name").val() == ""){
			e.preventDefault();
			$('label[for=usr-name]').css({color:'red'});
			$('#usr-name').css({backgroundColor:'#fcc'});
			msg = msg + "Debe introducir el nombre del usuario\n";
			ok = false;
		}
		if($("#usr-surname").val() == ""){
			e.preventDefault();
			$('label[for=usr-surname]').css({color:'red'});
			$('#usr-surname').css({backgroundColor:'#fcc'});
			msg = msg + "Debe introducir el/los apellido/s del usuario\n";
			ok = false;
		}
		
		if ( email != null && $.trim(email).length != 0 || op == 0 || op == 1 && $("#email").val() == "" || op == 2  && $("#email").val() == ""){
			if(!emailPattr.test(email)){
				e.preventDefault();			
				$('label[for=email]').css({color:'red'});
				$('#email').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir mail correcto para el usuario\n";
				ok = false;
			}
			if(op == 1 && $("#email").val() != ""){			
				var data="&email="+$("#email").val();
				$.ajax({
				 	async:false,
				 	cache:false,
		            type: "POST",
		            url: "../newUser?op=3",
		            data: data,		         
		            success: function (responseText) {
		            	if(responseText!="new"){
				e.preventDefault();
				$('label[for=email]').css({color:'red'});
				$('#email').css({backgroundColor:'#fcc'});
				msg =  msg + responseText+"\n";	
				ok = false;
		              	}	            		    					    				            	                     
		            }		           
		        });
			  }
			
			
		}if (!ok){
			alert(msg);
			return false;
		}
		
		if (op == 2)  {
			var roleHasResponsablePermission = false;
			var newResponsable = $("#role-usr").val();
			for (var i = 0; i < rolesWithResponsable.length; i++) {
				if (rolesWithResponsable[i] == newResponsable) {
					roleHasResponsablePermission = true;
					break;
				}
			}
			
			if  (!roleHasResponsablePermission && appList.length > 0) {
				msg = msg + "No puede modificar el rol que tenga asignado aplicaciones\n";
				ok = false;
			}
			
		}
		
		
		
		
		if((op == 0 || op == 1) && $("#login-usr").val() == ""){
			e.preventDefault();
			$('label[for=login-usr]').css({color:'red'});
			$('#login-usr').css({backgroundColor:'#fcc'});
			msg = msg + "Debe introducir un login para el usuario, el campo no puede estar vacio\n";
			ok = false;
		}
		else if(op == 1 && $("#login-usr").val() != ""){			
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
			    			msg = msg + responseText+"\n";		    			
			    			ok = false;
		            	}	            		    					    				            	                     
		            }		           
		        });
		}
	
	
		
		
		if (op == 1 && $("#passwd-usr").is(':enabled')) {
			if ($("#passwd-usr").val() == ""){
				e.preventDefault();
				$('label[for=passwd-usr]').css({color:'red'});
				$('#passwd-usr').css({backgroundColor:'#fcc'});
				msg = msg + "Debe introducir una contraseña para el usuario\n";
				ok = false;
			}
			if ($("#passwd-usr-copy").val() == ""){
				e.preventDefault();
				$('label[for=passwd-usr-copy]').css({color:'red'});
				$('#passwd-usr-copy').css({backgroundColor:'#fcc'});
				msg = msg + "Debe repetir la contraseña del usuario\n";
				ok = false;
			}
			if ($("#passwd-usr").val() !="" 
				&& $("#passwd-usr-copy").val() != "" 
				&& $("#passwd-usr").val() != $("#passwd-usr-copy").val()){
				e.preventDefault();
				$('label[for=passwd-usr]').css({color:'red'});
				$('label[for=passwd-usr-copy]').css({color:'red'});
				$('#passwd-usr').css({backgroundColor:'#fcc'});			
				$('#passwd-usr-copy').css({backgroundColor:'#fcc'});
				msg = msg + "Las contraseñas introducidas debe ser iguales\n";
				ok = false;
			}
		}
		
		
		
		
		
		if (!ok){
			alert(msg);
			return false;
		}	
		
		
		
		return ok;
	});//fin formUser submit!
	
	
	// funcion para imprimir por pantalla la tabla con las aplicaciones que esta asociado el usuario 
    function printApplicationsByUserTable(JSONData, recordsToFetch) {

		var htmlTableHead = "<table class='admin-table'>" +
				"<thead><tr>" +
					"<td>Aplicaci&oacute;n</td>" +
					"<td>ID</td>" +
					"<td>Fecha Alta</td>" +
				"</thead><tbody>";
		var htmlTableBody = "";
		var htmlTableFoot = "</tbody></table>";

		for (i = 0; i < recordsToFetch; ++i) {
			htmlTableBody = htmlTableBody
					+ "<tr><td>"
					+ dataUndefined(JSONData.AppList[i].nombre)
					+ "</td><td>"
					+ dataUndefined(JSONData.AppList[i].id)
					+ "<br>";

			if (JSONData.AppList[i].correo != null
					&& dataUndefined(JSONData.AppList[i].correo) != ""
					&& JSONData.AppList[i].correo != "") {
				htmlTableBody = htmlTableBody
						+ "<a href='mailto://"
						+ JSONData.AppList[i].correo + "'>"
						+ JSONData.AppList[i].correo + "</a>";
			}
			if (JSONData.AppList[i].telefono != null && dataUndefined(JSONData.AppList[i].telefono) != "" && JSONData.AppList[i].telefono != "") {
				htmlTableBody = htmlTableBody
						+ "(<a href='tel://"
						+ JSONData.AppList[i].telefono + "'>"
						+ JSONData.AppList[i].telefono
						+ "</a>)";
			}
			
			htmlTableBody = htmlTableBody + "</td><td>"+ JSONData.AppList[i].alta+ "</td>";
			htmlTableBody = htmlTableBody + "</tr>";
		}
		$("#data").append(htmlTableHead + htmlTableBody + htmlTableFoot);

	}
    
    
    function dataUndefined(data){
    	if(typeof data === "undefined"){
    		return "";
    	}
    	else{
    		return data;
    	}
    }
	
}); //fin document ready!

// validar dni
function comprobarDni(dni) {
	
	var numero;
	  var letra;
	  var letraCorrecta;
	  var expresion_regular_dni;
	 
	  expresion_regular_dni = /^(\d{8})([A-Z])$/;
	 
	  if(expresion_regular_dni.test (dni) == true){
	     numero = dni.substr(0,dni.length-1);
	     letra = dni.substr(dni.length-1,1);
	     numero = numero % 23;
	     letraCorrecta='TRWAGMYFPDXBNJZSQVHLCKET';
	     letraCorrecta=letraCorrecta.substring(numero,numero+1);
	    if (letraCorrecta!=letra.toUpperCase()) {
	     //  alert('Dni erroneo, la letra del NIF no se corresponde');
	       return false;
	     }else{
	//       alert('Dni correcto');
	       return true;
	     }
	  }else{
	  //   alert('Dni erroneo, formato no válido');
	     return false;
	   }
}



