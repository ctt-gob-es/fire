/**
 * 
 */

  $(document).ready(function(){
	 
	        var totalRecords;
            var recordsPerPage = 5;
            var recordsToFetch = recordsPerPage;
            var totalPages;
            var currentPage = 1;
            
            /********************************************************/
           
        	
        	/***************************************************************/
					
					//En caso de tener registros pintar la tabla
            		var urlParams = "requestType=" + requestType;
            		if (idUser) {
            			urlParams += "&id-users=" + idUser;
            		}
					$.get("user?" + urlParams, 
						function(data){
							
	                		var jsonData = JSON.parse(data);
	                		
	                		if (requestType == "All" && jsonData.UsrList && jsonData.UsrList.length > 0) {
	                			$("#data").html("");
	                			printUsersTable(jsonData, jsonData.UsrList.length);
	                			
	                		}
	                });
					
           
            /***********************************************************/
            
            function printUsersTable(JSONData, recordsToFetch){
            	
            	var htmlTableHead = '<table class="admin-table"><thead><tr><th id="login_username">Usuario</th><th id="username">Nombre</th><th id="usersurname">Apellidos</th><th id="email">E-Mail</th><th id="telf">Telf. Contacto</th><th id="nombre_rol">Rol</th><th id="actions">Acciones</th></tr></thead>';
            	var htmlTableBody = "";
            	var htmlTableFoot = '</table>';
       		        		
            	for(i = 0; i < recordsToFetch; ++i){
            		htmlTableBody = htmlTableBody + '<tr><td headers="login_username">' + dataUndefined(JSONData.UsrList[i].nombre_usuario) + '</td>';        
            		htmlTableBody = htmlTableBody + '<td headers="username">' + dataUndefined(JSONData.UsrList[i].nombre) + '</td>'  				
            		htmlTableBody = htmlTableBody + '<td headers="usersurname">' + dataUndefined(JSONData.UsrList[i].apellidos) + '</td>';
            		htmlTableBody = htmlTableBody + '<td headers="email">' + dataUndefined(JSONData.UsrList[i].correo_elec) + '</td>';            		
            		htmlTableBody = htmlTableBody + '<td headers="telf">' + dataUndefined(JSONData.UsrList[i].telf_contacto) + '</td>';
            		
            		htmlTableBody = htmlTableBody + '<td title="' + traductor(JSONData.UsrList[i].nombre_rol) + '" headers="nombre_rol"><img  class = "nombre_rol" src="../resources/img/' + dataUndefined(JSONData.UsrList[i].nombre_rol) + '_icon.png")/></td>';

            		htmlTableBody = htmlTableBody + '<td headers="actions">';            		
            		htmlTableBody = htmlTableBody + '<a href="NewUser.jsp?id-usr=' + JSONData.UsrList[i].id_usuario + '&usr-name=' + JSONData.UsrList[i].nombre_usuario + '&op=0" title="Visualizar"><img src="../resources/img/details_icon.png"/></a>';    				
            		htmlTableBody = htmlTableBody + '<a href="NewUser.jsp?id-usr=' + JSONData.UsrList[i].id_usuario + '&usr-name=' + JSONData.UsrList[i].nombre_usuario + '&op=2" title="Editar"><img src="../resources/img/editar_icon.png"/></a>';
            		if(JSONData.UsrList[i].usu_defecto == '0'){            		           		          			
            			if ($("#userLogged").html().trim() == JSONData.UsrList[i].nombre_usuario){
            				htmlTableBody = htmlTableBody + '<a href="../deleteUsr?id-usr=' + JSONData.UsrList[i].id_usuario + '&usr-name=' + JSONData.UsrList[i].nombre_usuario 
            				+ '" title="Eliminar"><img src="../resources/img/delete_icon.png" onclick="return confirmarBorradoUsuario(\'' + JSONData.UsrList[i].nombre_usuario + '\', true)"/></a>';	
            			}
            			else{            				
            				htmlTableBody = htmlTableBody + '<a href="../deleteUsr?id-usr=' + JSONData.UsrList[i].id_usuario + '&usr-name=' + JSONData.UsrList[i].nombre_usuario
            				+ '" title="Eliminar"><img src="../resources/img/delete_icon.png" onclick="return confirmarBorradoUsuario(\'' + JSONData.UsrList[i].nombre_usuario + '\', false)"/></a>';	
            			}            			
            		}
            		htmlTableBody = htmlTableBody + '</td></tr>';
            	}
            	$("#data").append(htmlTableHead + htmlTableBody + htmlTableFoot); 
            	
            }
            
            function  convertDateFormat(date){
            	 var dd = date.getDate();
                 var mm = date.getMonth() + 1; 
                 var yyyy = date.getFullYear();
                 if(dd < 10) {
                     dd = '0' + dd;
                 } 
                 if(mm < 10) {
                     mm = '0' + mm;
                 } 
                 return dd + "/" + mm + "/" + yyyy;
            }
           
            function dataUndefined(data){
            	if(typeof data === "undefined"){
            		return "";
            	}
            	else{
            		return data;
            	}
            }
            
            
        });
  
  

  function confirmarBorradoUsuario(nombreApp, usuarioPropio) { 
	if (usuarioPropio) {
		 return confirm('¿Está seguro de eliminar el usuario ' + nombreApp + '?\n En este caso se borrará el mismo usuario con el que está logado,\n se cerrará la sesión y se redirigirá a la página de inicio.');
	} else {
		return confirm('¿Está seguro de eliminar el usuario ' + nombreApp + '?');
	}
  }
  
  function traductor(nombre_rol){
	 // var nombre_rol = $("input[name=nombre_rol]").val();
	  
	  switch (nombre_rol) {
	  case "admin":
		  return "Administrador";
		break
	  case "responsible":
		  return "Responsable";
		 break
	  case "contact":
			return "Contacto";
		break
	  
	  }
  }
  


