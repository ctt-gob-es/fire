/**
 * 
 */
  $(document).ready(function(){
            var totalRecords;
            var recordsPerPage=5;
            var recordsToFetch=recordsPerPage;
            var totalPages;
            var currentPage=1;
            var currentIndex=0;
            
            $.get("processUsrRequest.jsp?requestType=countRecords",function(data){
                var JSONData=JSON.parse(data);
             
                totalRecords=JSONData.count; 
                totalPages=Math.floor(totalRecords/recordsPerPage);
                
                if(totalRecords%recordsPerPage!=0){
                	totalPages++;
                }
                
                if(totalRecords<recordsPerPage){
                    recordsToFetch=totalRecords%recordsPerPage;
                }
                else{
                	recordsToFetch=recordsPerPage;
                }
                
                $("#page").html("Página "+currentPage+" de "+totalPages);
                
            });    
            
            $.get("processUsrRequest.jsp?requestType=getRecords&currentIndex="+currentIndex+"&recordsToFetch="+recordsToFetch,function(data){
            	var JSONData=JSON.parse(data);
            	printUsersTable(JSONData, recordsToFetch);       
          	
                if(currentPage==totalPages){
                    $("#next").hide();
                }
                else{
                    $("#next").show();
                }
                
                if(currentPage==1){
                    $("#back").hide();
                }
                else{
                    $("#back").show();
                }

            });
            
            
            $("#next").click(function(){
            	$("#data").html("");
            	currentPage++;

            	if(currentPage==totalPages){
            		$("#next").hide();
                    if(totalRecords%recordsPerPage!=0){
                    	recordsToFetch=totalRecords%recordsPerPage;
                    }
                    else{
                    	recordsToFetch=recordsPerPage;
                    }
                }
                else{
                    $("#next").show();
                    recordsToFetch=recordsPerPage;
                }
            	                
                if(currentPage==1){
                    $("#back").hide();
                }
                else{
                    $("#back").show();
                }

                $.get("processUsrRequest.jsp?requestType=getRecords&currentIndex="+currentIndex+"&recordsToFetch="+recordsToFetch,function(data){
                    var JSONData=JSON.parse(data);
                    printUsersTable(JSONData, recordsToFetch);
                    
                });
                
                $("#page").html("Página "+currentPage+" de "+totalPages);

            });
            
            
            $("#back").click(function(){
                $("#data").html("");
                currentPage--;
                currentIndex=currentIndex-recordsToFetch-recordsPerPage;

                if(currentPage==totalPages){
                    $("#next").hide();
                    recordsToFetch=totalRecords%recordsPerPage;
                }
                else{
                    $("#next").show();
                    recordsToFetch=recordsPerPage;
                }
                
                if(currentPage==1){
                    $("#back").hide();
                }
                else{
                    $("#back").show();
                }

                $.get("processUsrRequest.jsp?requestType=getRecords&currentIndex="+currentIndex+"&recordsToFetch="+recordsToFetch,function(data){
                    var JSONData=JSON.parse(data);
                    printUsersTable(JSONData, recordsToFetch);
                    
                });
                
                $("#page").html("Página "+currentPage+" de "+totalPages);

            });

            function printUsersTable(JSONData, recordsToFetch){
            	
            	var htmlTableHead='<table class="admin-table"><thead><tr><th id="login_username">Usuario</th><th id="username">Nombre</th><th id="usersurname">Apellidos</th><th id="email">E-Mail</th><th id="telf">Telf. Contacto</th><th id="datetime">Fecha Alta</th><th id="actions">Acciones</th></tr></thead>';
            	var htmlTableBody="";
            	var htmlTableFoot='</table>';
       		        		
            	for(i=0;i<recordsToFetch;++i){
            		htmlTableBody=htmlTableBody+'<tr><td headers="login_username">'+ dataUndefined(JSONData.UsrList[i].nombre_usuario)+'</td>';        
            		htmlTableBody=htmlTableBody+'<td headers="username">'+dataUndefined(JSONData.UsrList[i].nombre)+'</td>'  				
            		htmlTableBody=htmlTableBody+'<td headers="usersurname">'+dataUndefined(JSONData.UsrList[i].apellidos)+'</td>';
            		htmlTableBody=htmlTableBody+'<td headers="email">'+ dataUndefined(JSONData.UsrList[i].correo_elec)+'</td>';            		
            		htmlTableBody=htmlTableBody+'<td headers="telf">'+ dataUndefined(JSONData.UsrList[i].telf_contacto)+'</td>';
            		fecAlta=new Date(JSONData.UsrList[i].fec_alta);
            		htmlTableBody=htmlTableBody+'<td headers="datetime">'+convertDateFormat(fecAlta)+'</td>';            		
            		htmlTableBody=htmlTableBody+'<td headers="actions">';            		
            		htmlTableBody=htmlTableBody+'<a href="NewUser.jsp?id-usr='+JSONData.UsrList[i].id_usuario+'&usr-name='+JSONData.UsrList[i].nombre_usuario+'&op=0"><img src="../resources/img/details_icon.png"/></a>';    				
            		htmlTableBody=htmlTableBody+'<a href="NewUser.jsp?id-usr='+JSONData.UsrList[i].id_usuario+'&usr-name='+JSONData.UsrList[i].nombre_usuario+'&op=2"><img src="../resources/img/editar_icon.png"/></a>';
            		if(JSONData.UsrList[i].usu_defecto=='0'){            		           		          			
            			if($("#userLogged").html().trim()==JSONData.UsrList[i].nombre_usuario){
            				htmlTableBody=htmlTableBody+'<a href="../deleteUsr?id-usr='+JSONData.UsrList[i].id_usuario+'&usr-name='+JSONData.UsrList[i].nombre_usuario+'"><img src="../resources/img/delete_icon.png" onclick="return confirmarUsuLogin(\''+JSONData.UsrList[i].nombre_usuario+'\')"/></a>';	
            			}
            			else{            				
            				htmlTableBody=htmlTableBody+'<a href="../deleteUsr?id-usr='+JSONData.UsrList[i].id_usuario+'&usr-name='+JSONData.UsrList[i].nombre_usuario+'"><img src="../resources/img/delete_icon.png" onclick="return confirmar(\''+JSONData.UsrList[i].nombre_usuario+'\')"/></a>';	
            			}            			
            		}            		
            		htmlTableBody=htmlTableBody+'</td></tr>';
            		
        		currentIndex++;
            	}
            	$("#data").append(htmlTableHead+htmlTableBody+htmlTableFoot); 
            	
            }
            
            function  convertDateFormat(date){
            	 var dd = date.getDate();
                 var mm = date.getMonth()+1; 
                 var yyyy = date.getFullYear();
                 if(dd<10) {
                     dd='0'+dd;
                 } 
                 if(mm<10) {
                     mm='0'+mm;
                 } 
                 return dd+"/"+mm+"/"+yyyy;
            }
           
            function dataUndefined(data){
            	if(typeof data==="undefined"){
            		return "";
            	}
            	else{
            		return data;
            	}
            }
            
            
        });
	function confirmar(nombreApp) { 
		   if (confirm('¿Está seguro de eliminar el usuario '+nombreApp+'?')) { 
		      document.tuformulario.submit();
		      return true;
		   }
		   return false;
	}
	function confirmarUsuLogin(nombreApp) { 
		   if (confirm('¿Está seguro de eliminar el usuario '+nombreApp+'?\n En este caso se borrará el mismo usuario con el que está logado,\n se cerrará la sesión y se redirigirá a la página de inicio.')) { 
		      document.tuformulario.submit();
		      return true;
		   }
		   return false;
	}
	
 