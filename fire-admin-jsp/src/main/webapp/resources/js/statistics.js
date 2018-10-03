/**
 * 
 */

$(function() {
     $('.date-picker').datepicker(
                    {
                        dateFormat: "mm/yy",
                        changeMonth: true,
                        changeYear: true,
                        showButtonPanel: true,
                        onClose: function(dateText, inst) {


                            function isDonePressed(){
                                return ($('#ui-datepicker-div').html().indexOf('ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all ui-state-hover') > -1);
                            }

                            if (isDonePressed()){
                                var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
                                var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
                                $(this).datepicker('setDate', new Date(year, month, 1)).trigger('change');
                                
                                 $('.date-picker').focusout()//Added to remove focus from datepicker input box on selecting date
                            }
                        },
                        beforeShow : function(input, inst) {

                            inst.dpDiv.addClass('month_year_datepicker')

                            if ((datestr = $(this).val()).length > 0) {
                                year = datestr.substring(datestr.length-4, datestr.length);
                                month = datestr.substring(0, 2);
                                $(this).datepicker('option', 'defaultDate', new Date(year, month-1, 1));
                                $(this).datepicker('setDate', new Date(year, month-1, 1));
                                $(".ui-datepicker-calendar").hide();
                            }
                        }
                    })
});

/*Funcion para la validacion*/

$(document).ready(function(){
	
	
	/**Valida los campos del formulario y envia por POST
	 * la consulta seleccionada, y añade el resultado
	*/
	
	$("#formStatictics").submit(function(e){
		e.preventDefault();
		$( "label" ).each(function( index ) {
			if ( this.style.color == "red" ) {
			      this.style.color = "#434343";
			      var idInput=$(this).attr('for');
			      $('#'+idInput).css({backgroundColor:'#D3D3D3'});
			    } 
		});
				
		var ok = true;
		var msg = "";
				
		if($("#select_query").val()=="0"){			
			e.preventDefault();			
			$('label[for=select_query]').css({color:'red'});
			$('#select_query').css({backgroundColor:'#fcc'});
			msg = msg + "Debe seleccionar una consulta\n";
			ok = false;			
		}		
		if($("#start_date").val() == ""){
			e.preventDefault();
			$('label[for=start_date]').css({color:'red'});
			$('#start_date').css({backgroundColor:'#fcc'});
			msg = msg + "Debe introducir una Fecha, (mes y año)\n";
			ok = false;
		}
														
		if(!ok){
			alert(msg);
		}		
		else
		{
			var $form = $( this ),
			    query = $form.find( "select[name='select_query']" ).val(),
			    date = $form.find( "input[name='start_date']" ).val(),
			    url = $form.attr( "action" );
			var posting = $.post( url, {select_query : query , start_date : date});			
			posting.done(function( data ) { 
				
				printResults(data);
			});
		}
		
	});//fin formUser submit!
	 
	/**
	 * 
	 */
	function printResults(data){
		var obJson = JSON.parse(data);
		alert(data);
		var numReg = 0;
		var total = 0;
		var dataJSON = '{"TotalPages":';
		var colNamesSt ="";
		var colModelSt = "";
		var rootSt = "";
		var captionSt = "";
		var totIncorrectas = 0;
		var totCorrectas = 0;
		var userData ='"userdata":{"ID_APP":"","NOMBRE":"Total:","Country":"Total","Price":"19521.68","Quantity":""}'
		
		if(obJson.TransByApp != null){
		
			numReg = obJson.TransByApp.length;		
			total =  Math.ceil(numReg / 10);
			rootSt = "TransByAppRows";
			captionSt = "Transacciones finalizadas por cada aplicaci&oacute;n";
			dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
			
			for (i = 0; i < obJson.TransByApp.length; i++) {
				if(JSON.stringify(obJson.TransByApp[i])=="INCORRECTAS"){
					totIncorrectas += Number(JSON.stringify(obJson.TransByApp[i+1]));
				}
				if(JSON.stringify(obJson.TransByApp[i])=="CORRECTAS"){
					totCorrectas += Number(JSON.stringify(obJson.TransByApp[i+1]));
				}
					
				if (i != obJson.TransByApp.length -1){							
					dataJSON += JSON.stringify(obJson.TransByApp[i]).trim() + ",";
				}
			    else{
			    	dataJSON += JSON.stringify(obJson.TransByApp[i]).trim() + "]}" ;
			    }							
			}
//			colNamesSt = "['Id','Nombre', 'INCORRECTAS','CORRECTAS']";
//			colModelSt =  "[{ name: 't.id_aplicacion',index:'t.id_aplicacion', width: '50',align: 'left',sortable: true, search:false },"+
//		        "{ name: 'a.nombre',index:'a.nombre', width: '200', align: 'center',sortable: true , search:false}, "+
//		        "{ name: 'INCORRECTAS',index:'INCORRECTAS', width: '100', align: 'right',sortable: true, search:false }, "+
//		        "{ name: 'CORRECTAS', index:'CORRECTAS', width: '100', align: 'right',sortable: true, search:false } "+
//				"]";
	
		}
		else if(obJson.TransByProv != null){
//			numReg = obJson.TransByProv.length;
//			total =  Math.ceil(txtNumRec / 10);
//			rootSt = '"TransByProvRows"';
//			captionSt = "Transacciones finalizadas  por cada origen de certificados/proveedor.";
//			dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+txtNumRec+','+rootSt+':[';
//			for (i = 0; i < obJson.TransByProv.length; i++) {
//				if (i != obJson.TransByProv.length -1){
//					dataJSON += JSON.stringify(obJson.TransByProv[i]) + ",";
//				}
//			    else{
//			    	dataJSON += JSON.stringify(obJson.TransByProv[i]) + "]}" ;
//			    }							
//			}	
		}
		else if(obJson.TransByDocSize != null){
			
//			numReg = obJson.TransByDocSize.length;
//			total =  Math.ceil(txtNumRec / 10);
//			rootSt = '"TransByDocSizeRows"';
//			captionSt = "Transacciones según el tamaño de los datos de cada aplicación";
//			dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+txtNumRec+','+rootSt+':[';
//			for (i = 0; i < obJson.TransByDocSize.length; i++) {
//				if (i != obJson.TransByDocSize.length -1){
//					dataJSON += JSON.stringify(obJson.TransByDocSize[i]) + ",";
//				}
//			    else{
//			    	dataJSON += JSON.stringify(obJson.TransByDocSize[i]) + "]}" ;
//			    }							
//			}	
		}	
		else if(obJson.TransByOperation != null){
//			numReg = obJson.TransByOperation.length;
//			total =  Math.ceil(txtNumRec / 10);
//			rootSt = '"TransByOperationRows"';
//			captionSt = "Transacciones realizadas según el tipo de transacción (simple o lote)";
//			dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+txtNumRec+','+rootSt+':[';
//			for (i = 0; i < obJson.TransByOperation.length; i++) {
//				if (i != obJson.TransByOperation.length -1){
//					dataJSON += JSON.stringify(obJson.TransByOperation[i]) + ",";
//				}
//			    else{
//			    	dataJSON += JSON.stringify(obJson.TransByOperation[i]) + "]}" ;
//			    }							
//			}	
		}	
		else if(obJson.SignByApp != null){
//			numReg = obJson.SignByApp.length;
//			total =  Math.ceil(txtNumRec / 10);
//			rootSt = '"SignByAppRows"';	
//			captionSt = "Documentos firmados por cada aplicación.";
//			dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+txtNumRec+','+rootSt+':[';
//			for (i = 0; i < obJson.SignByApp.length; i++) {
//				if (i != obJson.SignByApp.length -1){
//					dataJSON += JSON.stringify(obJson.SignByApp[i]) + ",";
//				}
//			    else{
//			    	dataJSON += JSON.stringify(obJson.SignByApp[i]) + "]}" ;
//			    }							
//			}	
		}
		else if(obJson.SignByFormat != null){
//			numReg = obJson.SignByFormat.length;
//			total =  Math.ceil(txtNumRec / 10);
//			rootSt = '"SignByFormatRows"';		
//			captionSt = "Documentos firmados en cada formato de firma.";
//			dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+txtNumRec+','+rootSt+':[';
//			for (i = 0; i < obJson.SignByFormat.length; i++) {
//				if (i != obJson.SignByFormat.length -1){
//					dataJSON += JSON.stringify(obJson.SignByFormat[i]) + ",";
//				}
//			    else{
//			    	dataJSON += JSON.stringify(obJson.SignByFormat[i]) + "]}" ;
//			    }							
//			}	
		}
		else if(obJson.SignByLongLiveFormat != null){
//			numReg = obJson.SignByLongLiveFormat.length;
//			total =  Math.ceil(txtNumRec / 10);
//			rootSt = '"SignByLongLiveFormatRows"';
//			captionSt = "Documentos que utilizan cada formato de firma longevo.";
//			dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+txtNumRec+','+rootSt+':[';
//			for (i = 0; i < obJson.SignByLongLiveFormat.length; i++) {
//				if (i != obJson.SignByLongLiveFormat.length -1){
//					dataJSON += JSON.stringify(obJson.SignByLongLiveFormat[i]) + ",";
//				}
//			    else{
//			    	dataJSON += JSON.stringify(obJson.SignByLongLiveFormat[i]) + "]}" ;
//			    }							
//			}	
		}
		else if(obJson.SignByProv != null){
//			numReg = obJson.SignByProv.length;
//			total =  Math.ceil(txtNumRec / 10);
//			rootSt = '"SignByProvRows"';			
//			captionSt = "Documentos firmados por cada origen de certificados/proveedor.";
//			dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+txtNumRec+','+rootSt+':[';
//			for (i = 0; i < obJson.SignByProv.length; i++) {
//				if (i != obJson.SignByProv.length -1){
//					dataJSON += JSON.stringify(obJson.SignByProv[i]) + ",";
//				}
//			    else{
//			    	dataJSON += JSON.stringify(obJson.SignByProv[i]) + "]}" ;
//			    }							
//			}	
		}
		
		/***Rellenar el grid*/
	
		 grid = $("#resultQuery");
		 grid.jqGrid({
			 colNames: ['Id','Nombre', 'INCORRECTAS','CORRECTAS','TOTAL'],
		     colModel: [
		    	 	{ name: 'ID_APP',index:'ID_APP', width: '100', align: 'center',sortable: true, search:false },
			        { name: 'NOMBRE',index:'NOMBRE', width: '200', align: 'center',sortable: true , search:false}, 
			        { name: 'INCORRECTAS',index:'INCORRECTAS', width: '100', align: 'right',sortable: true, search:false }, 
			        { name: 'CORRECTAS',index:'CORRECTAS', width: '100', align: 'right',sortable: true, search:false } ,
			        { name: 'TOTAL',index:'TOTAL', width: '100', align: 'right',sortable: true, search:false , 
			        	formatter: function (cellvalue, options, rowObject) 
                        {
			        		var incorrectas = rowObject["INCORRECTAS"] ;
			        		var correctas =  rowObject["CORRECTAS"] ;
			        		cellvalue = Number(incorrectas) + Number(correctas);
			        		return  cellvalue;
                        }
			        } 
					],		       
//		     pager: '#page',
		     datatype: "jsonstring",
		     datastr: dataJSON,
		     jsonReader: {
		    	 repeatitems: false,
		    	 root: "TransByAppRows",
		    	 page: "ActualPage",
		    	 total:"TotalPages" ,
		    	 records: "TotalRecords"
		     },
//		     rowNum: 10,
//		     rowList:[10,20,30],
		     loadonce: true,
		     viewrecords: true,
		     footerrow: true,
		     loadComplete: function(){
		    	 $(this).jqGrid("footerData", "set", {NOMBRE:"Total:"});
//			     $(this).jqGrid("footerData", "set", [{
//			    	 NOMBRE:"Total:",
//			    	 INCORRECTAS: $(this).jqGrid('getCol', 'INCORRECTAS', false, 'sum'),
//			    	 CORRECTAS: $(this).jqGrid('getCol', 'CORRECTAS', false, 'sum'),
//			    	 TOTAL: $(this).jqGrid('getCol', 'TOTAL', false, 'sum')
//			     }]); 
		     } 
		     caption: captionSt,
		     height: "auto",
		     width:"auto",
		     ignoreCase: true
		    });
		    
//		    grid.jqGrid('navGrid', '#page',
//		        { add: false, edit: false, del: false, search: false, refresh: true }, {}, {}, {},
//		        { multipleSearch: false, multipleGroup: false });
		
	}
	
	
	
	
	
	/*Limpiar los campos */
	document.getElementById("formStatictics").onreset = function() {resetFunction()};

	function resetFunction() {
		$( "label" ).each(function( index ) {
			this.style.color = "#000000";
			var idInput=$(this).attr('for');
			$('#'+idInput).css({backgroundColor:'#FFFFFF'});    
		});
	}
	
	

	
}); //fin document ready!