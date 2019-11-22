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
            
            /*prueba de carlos raboso*/
            /*MOSTRAMOS POR PANTALLA LA CARGA DE TODAS LAS APLICACIONES*/
        /********************** SIN PAGINACIÓN FIN*******************************/
        function printApplicationTable(jsonData, recordsToFetch){
        
        	var htmlTableHead = "<table id='appTable' class='admin-table'>"
        			+ "<thead>" + "<tr>" + "<td>Aplicaci&oacute;n</td>"
        			+ "<td>ID</td>" + "<td>Responsable</td>"
        			+ "<td>Fecha Alta</td>"
        			+ "<td>Estado</td>"
        			+ "<td id='acciones'>Acciones</td>"
        			+ "</tr>" + "</thead>";
        	var htmlTableBody = "";
        	var htmlTableFoot = "</table>";
        	
        	var lastAppId = null;
        	var newAppList = [];
        	
        	for (var i = 0; i < jsonData.AppList.length; i++) {
        		
        		if (jsonData.AppList[i].id == lastAppId) {
        			var lastApp = newAppList[newAppList.length - 1];
        			lastApp.nombre_responsable[lastApp.nombre_responsable.length] = jsonData.AppList[i].nombre_responsable; 
        		}
        		else {
        			newAppList[newAppList.length] = jsonData.AppList[i];
					var responsable = newAppList[newAppList.length - 1].nombre_responsable;
        			newAppList[newAppList.length - 1].nombre_responsable = [];
        			newAppList[newAppList.length - 1].nombre_responsable[0] = responsable;
						lastAppId = jsonData.AppList[i].id;
        		}
        	}
        		
        	
        	
        	
        	for (i = 0; i < Math.min(newAppList.length, recordsToFetch); ++i){
        		htmlTableBody = htmlTableBody + "<tr><td>" + dataUndefined(newAppList[i].nombre) + "</td>";
        		
        		htmlTableBody = htmlTableBody +	"<td>" + dataUndefined(newAppList[i].id) + "</td>";
        		htmlTableBody = htmlTableBody + "<td>";
        		
        		var responsablesText = "";
        		for (j = 0; j < newAppList[i].nombre_responsable.length; j++){
        			
        			responsablesText = responsablesText + dataUndefined(newAppList[i].nombre_responsable[j]);
    				if (j < newAppList[i].nombre_responsable.length - 1) {
    					responsablesText = responsablesText + "</br>";
    				}
				}
        		
        		
        		htmlTableBody = htmlTableBody + responsablesText + "</td>";
        		htmlTableBody = htmlTableBody + "<td>" + newAppList[i].alta + "</td>";
        		if (newAppList[i].habilitado){  
        			htmlTableBody = htmlTableBody + '<td title="' + dataUndefined(newAppList[i].habilitado) + '"><img  class = "habilitado" src="../resources/img/comprobado_icon.png"/></td>'
        		} else {
        			htmlTableBody = htmlTableBody + '<td title="' + dataUndefined(newAppList[i].habilitado) + '"><img  class = "deshabilitado" src="../resources/img/sin_entrada_icon.png"/></td>'
        		}

        		htmlTableBody = htmlTableBody + "<td>";
        		
        		htmlTableBody = htmlTableBody + "<a href='NewApplication.jsp?appid=" + newAppList[i].id + "&op=0' title='Visualizar'><img  src='../resources/img/details_icon.png'/></a>";
        		htmlTableBody = htmlTableBody + "<a href='NewApplication.jsp?appid=" + newAppList[i].id + "&op=2' title='Editar'><img src='../resources/img/editar_icon.png'/></a>";        		
        		htmlTableBody = htmlTableBody + "<a href='../deleteApp?appid=" + newAppList[i].id + "' title='Eliminar'><img src='../resources/img/delete_icon.png' onclick='return confirmar(\"" + newAppList[i].nombre + "\")'/></a>";     
        		
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
	 		
	 		if ($("#listresp").val() == "" ){
	 			$('label[for=listresp]').css({color:'red'});
				$('#listresp').css({backgroundColor:'#fcc'});
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
