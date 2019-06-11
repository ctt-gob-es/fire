/**
 * 
 */
$(document).ready(function(){
	
	
	
		    /*VARIABLES*/
	/**************************/
	 		var totalRecords;
            var recordsPerPage=6;
            var recordsToFetch=recordsPerPage;
            var totalPages;
            var currentPage=1;
            var currentIndex=0;
            
            
          	/*RECUPERAMOS LOS DATOS PARA IMPRIMIR POR PANTALLA LA CARGA DE TODAS LAS APLICACIONES*/
       /*********************************** SIN PAGINACIÓN *******************************************/
            
            $.get("application?requestType=countRecords",function(data){
          
                var JSONData = JSON.parse(data);
          
                totalRecords = JSONData.count;
           
                if(totalRecords > 0){
                	$("#data").html("");
                	$.get("application?requestType=All", function(data){
                    	var JSONData = JSON.parse(data);                
                    	  printApplicationTable(JSONData, totalRecords);                        

                    });
                }
                else{                    	
                	return;
                }              
               
            });
            
            
            /*MOSTRAMOS POR PANTALLA LA CARGA DE TODAS LAS APLICACIONES*/
        /********************** SIN PAGINACIÓN FIN*******************************/
        function printApplicationTable(JSONData, recordsToFetch){
        
        	var htmlTableHead = "<table id='appTable' class='admin-table'>"
        			+ "<thead>" + "<tr>" + "<td>Aplicaci&oacute;n</td>"
        			
        			+ "<td>ID</td>" + "<td>Responsable</td>"
        			+ "<td>Fecha Alta</td>"
        			+ "<td>Estado</td>"
        			+ "<td id='acciones'>Acciones</td>"
        			+ "</tr>" + "</thead><tbody>";
        	var htmlTableBody = "";
        	var htmlTableFoot = "</tbody></table>";
        	
   		        		
        	for(i = 0; i < recordsToFetch; ++i){
        		htmlTableBody = htmlTableBody + "<tr><td>" + dataUndefined(JSONData.AppList[i].nombre) + "</td>";
        		
        		htmlTableBody = htmlTableBody +	"<td>" + dataUndefined(JSONData.AppList[i].id) + "</td>";
        		htmlTableBody = htmlTableBody + "<td>" + dataUndefined(JSONData.AppList[i].nombre_responsable) + "</td>"
        		
        		if ( JSONData.AppList[i].correo != null && dataUndefined(JSONData.AppList[i].correo) != "" && JSONData.AppList[i].correo != "") { 
        			htmlTableBody=htmlTableBody + "<a href='mailto://" + JSONData.AppList[i].correo + "'>" + JSONData.AppList[i].correo + "</a>";
        		} 
        		if (JSONData.AppList[i].telefono != null && dataUndefined(JSONData.AppList[i].telefono) != "" && JSONData.AppList[i].telefono != "") { 
        			htmlTableBody=htmlTableBody + "(<a href='tel://" + JSONData.AppList[i].telefono + "'>" + JSONData.AppList[i].telefono + "</a>)";
        		}

        		htmlTableBody = htmlTableBody + "</td><td>" + JSONData.AppList[i].alta + "</td>";
        		if (JSONData.AppList[i].habilitado){  
        			htmlTableBody = htmlTableBody + '<td title="' + dataUndefined(JSONData.AppList[i].habilitado) + '"><img  class = "habilitado" src="../resources/img/comprobado_icon.png"/></td>'
        		} else {
        			htmlTableBody = htmlTableBody + '<td title="' + dataUndefined(JSONData.AppList[i].habilitado) + '"><img  class = "deshabilitado" src="../resources/img/sin_entrada_icon.png"/></td>'
        		}

        		htmlTableBody = htmlTableBody + "<td>";
        		
        		htmlTableBody = htmlTableBody + "<a href='NewApplication.jsp?id-app=" + JSONData.AppList[i].id + "&op=0' title='Visualizar'><img  src='../resources/img/details_icon.png'/></a>";
        		htmlTableBody = htmlTableBody + "<a href='NewApplication.jsp?id-app=" + JSONData.AppList[i].id + "&op=2' title='Editar'><img src='../resources/img/editar_icon.png'/></a>";        		
        		htmlTableBody = htmlTableBody + "<a href='../deleteApp?id-app=" + JSONData.AppList[i].id + "' title='Eliminar'><img src='../resources/img/delete_icon.png' onclick='return confirmar(\"" + JSONData.AppList[i].nombre + "\")'/></a>";     
        		
        		htmlTableBody = htmlTableBody + "</td></tr>"; 
        		
        		currentIndex++;
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
        
        
        
        /*VALIDACION DE LOS CAMPOS DEL FORMULARIO*/
 /***********************************************************************************************************************/   
	     
	 	
	 	var emailPattr = /^[\w\-\.\+]+\@[a-zA-Z0-9\.\-]+\.[a-zA-z0-9]{2,4}$/;
	 		
	 	$("#frmApplication").submit(function(e){
	 		
	 		$( "label" ).each(function( index ) {
				if ( this.style.color == "red" ) {
				      this.style.color = "#434343";
				      var idInput=$(this).attr('for');
				      $('#'+ idInput).css({backgroundColor:'#D3D3D3'});
				    } 
			});
	 		
	 		var email = $("#email-resp").val();
	 		var msg = "";
	 		var ok = true;
	 		if(email != null && $.trim(email).length != 0){
	 			if(!emailPattr.test(email)){
	 				$('label[for=email-resp]').css({color:'red'});
					$('#email-resp').css({backgroundColor:'#fcc'});
	 				e.preventDefault();
	 				msg = msg + "Debe introducir un formato de correo electrónico correcto\n";	 				
	 				ok = false;
	 			}
	 		}
	 		
	 		if ($("#nombre-app").val() == "" ){
	 			$('label[for=nombre-app]').css({color:'red'});
				$('#nombre-app').css({backgroundColor:'#fcc'});
	 			e.preventDefault();
	 			msg = msg + "El nombre de la aplicación no puede estar vacío\n";			
	 			ok = false;
	 		}
	 		
	 		if ($("#nombre-resp").val() == "" ){
	 			$('label[for=nombre-resp]').css({color:'red'});
				$('#nombre-resp').css({backgroundColor:'#fcc'});
	 			e.preventDefault();
	 			msg = msg + "El nombre del responsable no puede estar vacío\n";		
	 			ok = false;
	 			
	 		
			}
	 		
	 	
	 		
	 		
	 		if($("#id-certificate").val() == "0"){
	 			$('label[for=id-certificate]').css({color:'red'});
				$('#id-certificate').css({backgroundColor:'#fcc'});
	 			e.preventDefault();
	 			msg = msg + "El certificado no puede estar vacío, seleccione un certificado.  \n";
	 			ok = false;
	 		}
	 		if(!ok){
				alert(msg);
			}
	 		return ok;
	 	});//fin frmApplication submit!
	          
	});


function confirmar(nombreApp) { 
	   if (confirm('¿Está seguro de eliminar la aplicación '+ nombreApp + '?')) {
		
		   document.tuformulario.submit();     
	      return true;
	   }
	   return false;
}
