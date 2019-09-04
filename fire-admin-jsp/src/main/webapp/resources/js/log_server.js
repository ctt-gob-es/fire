
/**
 * 
 */
  $(document).ready(function(){
	 
   
           
            /********************************************************/
            $.get("../logServer?act=0", function(data){
            	$("#data").html("");
                var JSONData = JSON.parse(data);            
                printServersTable(JSONData);   	
                
            }); 
           
            /***********************************************************/
            
            function printServersTable(JSONData){
         
            	var htmlTableHead = '<table class="admin-table"><thead><tr><th id="name">Servidor</th><th id="url">URL</th><th id="actions">Acciones</th></tr></thead>';
            	var htmlTableBody = "";
            	var htmlTableFoot = '</table>';
            	
            	for(var i = 0; i < JSONData.LogSrvs.length; i++){
            		
            		htmlTableBody = htmlTableBody + '<tr><td headers="name">' + JSONData.LogSrvs[i].nombre + '</td>';        
            		htmlTableBody = htmlTableBody + '<td headers="url">' + JSONData.LogSrvs[i].url_servicio_log + '</td>'  			      	
            		htmlTableBody = htmlTableBody + '<td headers="actions">';  
            		htmlTableBody = htmlTableBody + '<a href="../logServer?act=1&id-srv=' +  JSONData.LogSrvs[i].id_servidor + '" title="Conectar"><img src="../resources/img/servidor_icon.png"/></a>';   //Conectar
            		htmlTableBody = htmlTableBody + '<a href="./LogServer.jsp?act=5&id-srv=' +  JSONData.LogSrvs[i].id_servidor + '" title="Visualizar"><img src="../resources/img/details_icon.png"/></a>';
            		htmlTableBody = htmlTableBody + '<a href="./LogServer.jsp?act=3&id-srv=' +  JSONData.LogSrvs[i].id_servidor + '" title="Editar"><img src="../resources/img/editar_icon.png"/></a>'; //Visualizar   				
            		htmlTableBody = htmlTableBody + '<a href="../logServer?act=4&id-srv=' +  JSONData.LogSrvs[i].id_servidor + '" title="Eliminar"><img src="../resources/img/delete_icon.png" onclick="return confirmar(\'' + JSONData.LogSrvs[i].nombre + '\')"/></a>';	  			         			
         		
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
	function confirmar(nombreSrv) { 
		   if (confirm('¿Está seguro de eliminar el servidor '+nombreSrv+'?')) { 
		      document.tuformulario.submit();
		      return true;
		   }
		   return false;
	}
	
	
 