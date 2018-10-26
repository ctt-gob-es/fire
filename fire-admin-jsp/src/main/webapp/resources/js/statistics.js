/**
 * 
 */

$(function() {
	var yearDef = new Date().getFullYear();
	var monthDef = new Date().getMonth()-1;

	
     $('.date-picker').datepicker(
                    {
                        dateFormat: "mm/yy",
                        changeMonth: true,
                        changeYear: true,                 
                        showButtonPanel: true,
                        defaultDate: new Date(yearDef, monthDef, 1),
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

	
	
	/**Colores definidos para el gráfico de transaciones y firmas correctas*/
	var goodColors = ["#B40404","#B45F04","#AEB404","#04B404","#04B4AE","#0404B4","#8904B1","#B40486","#B40431","#585858"];
	/**Colores definidos para el gráfico de transaciones y firmas incorrectas*/
	var badColors = ["#B40404","#B45F04","#AEB404","#04B404","#04B4AE","#0404B4","#8904B1","#B40486","#B40431","#585858"];
	
	var titlePDF = "";
	var descriptionPDF = "";
	var fileNamePDF = "";
	var fileNameExcel = "";
	var fileNameCSV = "";

	var arrCharts = new Array();
		
	
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
			msg = msg + "Debe introducir un Mes, (mes / año)\n";
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
			var contentType =  "application/json; charset=utf-8"; 			
			var posting = $.post( url,{select_query : query , start_date : date},contentType);			
			posting.done(function( data ) { 
			
				//Se recorren los campos de consulta y fecha para colocarlos en blanco ya que la consulta es correcta
				$( "label" ).each(function( index ) {
					this.style.color = "#000000";
					var idInput=$(this).attr('for');
					$('#'+idInput).css({backgroundColor:'#FFFFFF'});    
				});
				
										
				var obJson = JSON.parse(data);				
				var numReg = 0;
				var total = 0;
				var dataJSON = '{"TotalPages":';
				var totBad = 0;
				var totGood = 0;
				var sumTotal = 0;
				var userData = ""; 
				var rootSt = "";
				var captionSt = "";
				var chartDataGood =  new Array("");
				var chartDataBad =  new Array("");
				var chartLabelsGood =  new Array("");
				var chartLabelsBad =  new Array("");
				var columnNames = new Array("");
				var columnModel =  new Array("");
			
				if(obJson.Error){ //No tiene datos, mostramos mensaje de Error
					printErrorResult(obJson);
					return;
				}
				
				//se habilitan los botones de exportar
				disabledExportsButtons(false);
				//Si anteriormente ha habido un error se limpian los mensajes de la pantalla
				var html = "";	
				$("#error-txt").html(html);
				$("#error-txt").hide();
				
				switch (Number(query)){
					case 1: //"Transacciones finalizadas por cada aplicacion"
						
						/******* INFORMES *********/
						titlePDF = "Informe de Estadísticas de Fire";
						descriptionPDF = 'Transacciones finalizadas por cada aplicación para el Mes '+ $("#start_date").val()+ '.';
						fileNamePDF = "TransacionesPorAplicacion.pdf";
						fileNameExcel = "TransacionesPorAplicacion.xlsx";
						fileNameCSV =  "TransacionesPorAplicacion.csv";
											
						/****** GRAFICAS *********/	
						
												
						var oJSONGood = JSON.parse(JSON.stringify(obJson));
						var oJSONBad = JSON.parse(JSON.stringify(obJson));
						
						ordenarDesc(oJSONGood.TransByApp, 'CORRECTAS');											
						ordenarDesc(oJSONBad.TransByApp, 'INCORRECTAS');
						
						
						if(oJSONGood.TransByApp.length <= 9){
							
							chartLabelsGood = oJSONGood.TransByApp.map(function(e){return e.NOMBRE});
							chartLabelsBad = oJSONBad.TransByApp.map(function(e){return e.NOMBRE});
							
							chartDataGood = oJSONGood.TransByApp.map(function(e){return e.CORRECTAS});							
							chartDataBad = oJSONBad.TransByApp.map(function(e){return e.INCORRECTAS});
						}
						else{
							var sumOthersGood = 0;
							var sumOthersBad = 0;
							for(i = 0; i < oJSONGood.TransByApp.length; i++){
								if(i < 9){
									chartLabelsGood[i] = oJSONGood.TransByApp[i].NOMBRE;
									chartLabelsBad[i] = oJSONBad.TransByApp[i].NOMBRE;
									
									chartDataGood[i] = oJSONGood.TransByApp[i].CORRECTAS;
									chartDataBad[i] = oJSONBad.TransByApp[i].INCORRECTAS;
								}
								else{
									sumOthersGood += Number(oJSONGood.TransByApp[i].CORRECTAS);
									sumOthersBad += Number(oJSONBad.TransByApp[i].INCORRECTAS);
								}
							}							
							chartDataGood[9] = sumOthersGood;
							chartDataBad[9] = sumOthersBad;
							chartLabelsGood.splice(9,0,"OTRAS");
							chartLabelsBad.splice(9,0,"OTRAS");
						}
												
						var backGroundGood = [""];
						var backGroundBad = [""];
					
						//Obtenemos los colores a mostrar por aplicación 
						// hasta um maximo de 10 colores correspondiendo el último al grupo de "OTRAS" ( Aplicaciones )
						for(i = 0; i < chartLabelsGood.length; i++){
							if(i <= 9){
								backGroundGood[i]=goodColors[i];
								backGroundBad[i]=badColors[i];
							}							
							else{
								break;
							}						
						}
						
						/**Dibujar las graficas**/
						
						var sLabel = "Transacciones";
						var textGood = 'Transacciones correctas por aplicación';
						var textBad = 'Transacciones incorrectas por aplicación';										
																
						var idCanvasGood = "chartGood";
						var idCanvasBad = "chartBad";
							
						arrCharts = [
							{idCanvas : idCanvasGood, chartLabels : chartLabelsGood, chartData : chartDataGood,backGround : backGroundGood, text: textGood},
							{idCanvas : idCanvasBad, chartLabels : chartLabelsBad, chartData : chartDataBad, backGround : backGroundBad, text: textBad},						
						];
						printPieCharts(sLabel,arrCharts);
						
						/*****************/
						
						/****** TABLA CON DATOS ******/
						numReg = obJson.TransByApp.length;		
						total =  Math.ceil(numReg / 10);
						rootSt = "TransByAppRows";
						captionSt = "Transacciones finalizadas por cada aplicaci&oacute;n para el Mes "+ $("#start_date").val()+ ".";
						dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
												
						for (i = 0; i < obJson.TransByApp.length; i++) {
												
							totBad += Number(obJson.TransByApp[i].INCORRECTAS);
							totGood += Number(obJson.TransByApp[i].CORRECTAS);
													
							if (i != obJson.TransByApp.length -1){							
								dataJSON += JSON.stringify(obJson.TransByApp[i]).trim() + ",";
							}
						    else{
						    	dataJSON += JSON.stringify(obJson.TransByApp[i]).trim() + "]" ;
						    }							
						}
						sumTotal = totBad + totGood;
						userData =',"userdata":{"NOMBRE":"Totales", "INCORRECTAS":"'+totBad+'","CORRECTAS":"'+totGood+ '","TOTAL":"'+sumTotal+'"}}';
						dataJSON = dataJSON + userData;
								
						columnNames = ['Nombre', 'INCORRECTAS','CORRECTAS','TOTAL'];
						columnModel =[				    	 	
					        { name: 'NOMBRE',index:'NOMBRE', width: '250', align: 'left',sortable: true , search:false}, 
					        { name: 'INCORRECTAS',index:'INCORRECTAS', width: '100', align: 'right',sortable: true, search:false }, 
					        { name: 'CORRECTAS',index:'CORRECTAS', width: '100', align: 'right',sortable: true, search:false } ,
					        { name: 'TOTAL',index:'TOTAL', width: '100', align: 'right',sortable: true, search:false } 
					        ];
						
						printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt,null);					
						/************/
						break;
					case 2:	//Transacciones finalizadas  por cada origen de certificados/proveedor.
						
						/******* INFORMES *********/
						titlePDF = "Informe de Estadísticas de Fire";
						descriptionPDF = 'Transacciones finalizadas por cada origen de certificados/proveedor para el Mes '+ $("#start_date").val()+ '.';
						fileNamePDF = "TransacionesPorProveedor.pdf";
						fileNameExcel = "TransacionesPorProveedor.xlsx";
						fileNameCSV =  "TransacionesPorProveedor.csv";
											
						/****** GRAFICAS *********/	
												
						var oJSONGood = JSON.parse(JSON.stringify(obJson));
						var oJSONBad = JSON.parse(JSON.stringify(obJson));
						
						ordenarDesc(oJSONGood.TransByProv, 'CORRECTAS');											
						ordenarDesc(oJSONBad.TransByProv, 'INCORRECTAS');
						
																	
						if(oJSONGood.TransByProv.length <= 9){
							
							chartLabelsGood = oJSONGood.TransByProv.map(function(e){return e.NOMBRE});
							chartLabelsBad = oJSONBad.TransByProv.map(function(e){return e.NOMBRE});
							
							chartDataGood = oJSONGood.TransByProv.map(function(e){return e.CORRECTAS});							
							chartDataBad = oJSONBad.TransByProv.map(function(e){return e.INCORRECTAS});

						}
						else{
							var sumOthersGood = 0;
							var sumOthersBad = 0;
							for(i = 0; i < chartLabelsGood.length; i++){
								if(i < 9){
									chartDataGood[i] = oJSONGood.TransByProv[i].CORRECTAS;
									chartDataBad[i] = oJSONBad.TransByProv[i].INCORRECTAS;
								}
								else{
									sumOthersGood += Number(oJSONGood.TransByProv[i].CORRECTAS);
									sumOthersBad += Number(oJSONBad.TransByProv[i].INCORRECTAS);
								}
							}							
							chartDataGood[9] = sumOthersGood;
							chartDataBad[9] = sumOthersBad;
							chartLabelsGood.splice(9,0,"OTRAS");
							chartLabelsBad.splice(9,0,"OTRAS");
						}
												
						var backGroundGood = [""];
						var backGroundBad = [""];
					
						//Obtenemos los colores a mostrar por aplicación 
						// hasta um maximo de 10 colores correspondiendo el último al grupo de "OTRAS" ( Aplicaciones )
						for(i = 0; i < chartLabelsGood.length; i++){
							if(i <= 9){
								backGroundGood[i]=goodColors[i];
								backGroundBad[i]=badColors[i];
							}							
							else{
								break;
							}						
						}
						/**Dibujar las graficas**/
						
						var sLabel = "Transacciones";
						var textGood = 'Transacciones correctas por proveedor';
						var textBad = 'Transacciones incorrectas por proveedor';							
						
						var idCanvasGood = "chartGood";
						var idCanvasBad = "chartBad";
							
						arrCharts = [
							{idCanvas : idCanvasGood, chartLabels : chartLabelsGood, chartData : chartDataGood,backGround : backGroundGood, text: textGood},
							{idCanvas : idCanvasBad, chartLabels : chartLabelsBad, chartData : chartDataBad, backGround : backGroundBad, text: textBad},						
						];
						printPieCharts(sLabel,arrCharts);
						
						/*****************/
						
						/****** TABLA CON DATOS ******/
						numReg = obJson.TransByProv.length;		
						total =  Math.ceil(numReg / 10);
						rootSt = "TransByProvRows";
						captionSt = "Transacciones finalizadas  por cada origen de certificados/proveedor para el Mes "+ $("#start_date").val()+ ".";
						dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
												
						for (i = 0; i < obJson.TransByProv.length; i++) {
												
							totBad += Number(obJson.TransByProv[i].INCORRECTAS);
							totGood += Number(obJson.TransByProv[i].CORRECTAS);
													
							if (i != obJson.TransByProv.length -1){							
								dataJSON += JSON.stringify(obJson.TransByProv[i]).trim() + ",";

							}
						    else{
						    	dataJSON += JSON.stringify(obJson.TransByProv[i]).trim() + "]" ;
						    }							
						}
						sumTotal = totBad + totGood;
						userData =',"userdata":{"NOMBRE":"Totales", "INCORRECTAS":"'+totBad+'","CORRECTAS":"'+totGood+ '","TOTAL":"'+sumTotal+'"}}';
						dataJSON = dataJSON + userData;
								
						columnNames = ['Nombre', 'INCORRECTAS','CORRECTAS','TOTAL'];
						columnModel =[
					        { name: 'NOMBRE',index:'NOMBRE', width: '250', align: 'left',sortable: true , search:false}, 
					        { name: 'INCORRECTAS',index:'INCORRECTAS', width: '100', align: 'right',sortable: true, search:false }, 
					        { name: 'CORRECTAS',index:'CORRECTAS', width: '100', align: 'right',sortable: true, search:false } ,
					        { name: 'TOTAL',index:'TOTAL', width: '100', align: 'right',sortable: true, search:false } 
					        ];
						
						printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt,null);					
						/************/
						break;
					case 3:	//Transacciones segun el tamaño de los datos de cada aplicacion
						
						/******* INFORMES *********/
						titlePDF = "Informe de Estadísticas de Fire";
						descriptionPDF = 'Transacciones según el tamaño de los datos de cada aplicación para el Mes '+ $("#start_date").val()+ '.';
						fileNamePDF = "TamañoTransacionesPorAplicacion.pdf";
						fileNameExcel = "TamañoTransacionesPorAplicacion.xlsx";
						fileNameCSV =  "TamañoTransacionesPorAplicacion.csv";
											
						/****** GRAFICAS *********/	
						var oJSONGood = JSON.parse(JSON.stringify(obJson));
						ordenarDesc(oJSONGood.TransByDocSize, 'MB');
									
						
						
						if(oJSONGood.TransByDocSize.length <= 9){	
							chartLabelsGood = oJSONGood.TransByDocSize.map(function(e){return e.NOMBRE});
							chartDataGood = oJSONGood.TransByDocSize.map(function(e){return e.MB});							
						}
						else{
							var sumOthersGood = 0;
							for(i = 0; i < oJSONGood.TransByDocSize.length; i++){
								if(i < 9){
									chartLabelsGood[i] = oJSONGood.TransByDocSize[i].NOMBRE;
									chartDataGood[i] = oJSONGood.TransByDocSize[i].MB;
								}
								else{
									sumOthersGood += Number(oJSONGood.TransByDocSize[i].MB);

								}
							}							
							chartDataGood[9] = sumOthersGood;
							chartLabelsGood.splice(9,0,"OTRAS");
						}
												
						var backGroundGood = [""];
					
						//Obtenemos los colores a mostrar por aplicación 
						// hasta um maximo de 10 colores correspondiendo el último al grupo de "OTRAS" ( Aplicaciones )
						for(i = 0; i < chartLabelsGood.length; i++){
							if(i <= 9){
								backGroundGood[i]=goodColors[i];
							}							
							else{
								break;
							}						
						}
						/**Dibujar las graficas**/
						
						var sLabel = "Transacciones";
						var textGood = 'Transacciones según el tamaño de los datos por aplicación';														
						var idCanvasGood = "chartGood";
							
						arrCharts = [
							{idCanvas : idCanvasGood, chartLabels : chartLabelsGood, chartData : chartDataGood,backGround : backGroundGood, text: textGood}						
						];
						printPieCharts(sLabel,arrCharts);
						
						/*****************/
						
						/****** TABLA CON DATOS ******/
						numReg = obJson.TransByDocSize.length;		
						total =  Math.ceil(numReg / 10);
						rootSt = "TransByDocSizeRows";
						captionSt = "Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n para el Mes "+ $("#start_date").val()+ ".";
						dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
												
						for (i = 0; i < obJson.TransByDocSize.length; i++) {												
							totGood += Number(obJson.TransByDocSize[i].MB);													
							if (i != obJson.TransByDocSize.length -1){							
								dataJSON += JSON.stringify(obJson.TransByDocSize[i]).trim() + ",";
							}
						    else{
						    	dataJSON += JSON.stringify(obJson.TransByDocSize[i]).trim() + "]" ;
						    }							
						}
						sumTotal = totGood;
						userData =',"userdata":{"NOMBRE":"Total", "MB":"'+(sumTotal).toFixed(2)+'"}}';
						dataJSON = dataJSON + userData;
								
						columnNames = ['Nombre', 'Megabyte'];
						columnModel =[
					        { name: 'NOMBRE',index:'NOMBRE', width: '250', align: 'left',sortable: true , search:false}, 
					        { name: 'MB',index:'MB', width: '300', align: 'right',sortable: true, search:false } 				        
					        ];		
						
						printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt,null);	
						
						/************/
						
						break;
					case 4:		//Transacciones realizadas según el tipo de transacción (simple o lote)
						
						var chartLabelsGoodSimple = new Array("");
						var chartLabelsBadSimple = new Array("");
						var chartLabelsGoodBatch =  new Array("");
						var chartLabelsBadBatch =  new Array("");
						
						var chartDataGoodSimple =  new Array("");
						var chartDataBadSimple =  new Array("");
						var chartDataGoodBatch =  new Array("");
						var chartDataBadBatch =  new Array("");
											
						var totBadSimple = 0;
						var totGoodSimple = 0;
						var totBadBatch = 0;
						var totGoodBatch = 0;
						var sumTotalSimple = 0;
						var sumTotalBatch = 0;
						
						/******* INFORMES *********/
						titlePDF = "Informe de Estadísticas de Fire";
						descriptionPDF = 'Transacciones realizadas según el tipo de transacción (Simple o lote) para el Mes '+ $("#start_date").val()+ '.';
						fileNamePDF = "TransacionesPorOperacion.pdf";
						fileNameExcel = "TransacionesPorOperacion.xlsx";
						fileNameCSV =  "TransacionesPorOperacion.csv";
											
						/****** GRAFICAS *********/	
						
						var oJSONGoodSimple = JSON.parse(JSON.stringify(obJson));
						ordenarDesc(oJSONGoodSimple.TransByOperation, 'FirmasSimplesCorrectas');
						
						var oJSONBadSimple = JSON.parse(JSON.stringify(obJson));
						ordenarDesc(oJSONBadSimple.TransByOperation, 'FirmasSimplesINCorrectas');
						
						var oJSONGoodBatch =  JSON.parse(JSON.stringify(obJson));
						ordenarDesc(oJSONGoodBatch.TransByOperation, 'FirmasLotesCorrectas');
						
						var oJSONBadBatch = JSON.parse(JSON.stringify(obJson));
						ordenarDesc(oJSONBadBatch.TransByOperation, 'FirmasLotesINCorrectas');
																								
						//Tratamiento operaciones Simples Correctas.
						if(oJSONGoodSimple.TransByOperation.length <= 9){							
							chartLabelsGoodSimple = oJSONGoodSimple.TransByOperation.map(function(e){return e.NOMBRE});													
							chartDataGoodSimple = oJSONGoodSimple.TransByOperation.map(function(e){return e.FirmasSimplesCorrectas});													
						}
						else{							
							var sumOthersGoodSimple = 0;											
							for(i = 0; i < oJSONGoodSimple.TransByOperation.length; i++){
								if(i < 9){									
									chartLabelsGoodSimple[i] = oJSONGoodSimple.TransByOperation[i].NOMBRE;																		
									chartDataGoodSimple[i] = oJSONGoodSimple.TransByOperation[i].FirmasSimplesCorrectas;									
								}
								else{									
									sumOthersGoodSimple += Number(oJSONGoodSimple.TransByOperation[i].FirmasSimplesCorrectas);																		
								}
							}							
							chartDataGood[9] = sumOthersGoodSimple;						
							chartDataGoodSimple.splice(9,0,"OTRAS");							
						}
							
						//Tratamiento operaciones Simples Incorrectas.
						if(oJSONBadSimple.TransByOperation.length <= 9){							
							chartLabelsBadSimple = oJSONBadSimple.TransByOperation.map(function(e){return e.NOMBRE});														
							chartDataBadSimple = oJSONBadSimple.TransByOperation.map(function(e){return e.FirmasSimplesINCorrectas});

						}
						else{							
							var sumOthersBadSimple = 0;							
							for(i = 0; i < oJSONBadSimple.TransByOperation.length; i++){
								if(i < 9){									
									chartLabelsBadSimple[i] = oJSONBadSimple.TransByOperation[i].NOMBRE;
									chartDataBadSimple[i] = oJSONBadSimple.TransByOperation[i].FirmasSimplesINCorrectas;
								}
								else{
									sumOthersBadSimple += Number(oJSONBadSimple.TransByOperation[i].FirmasSimplesINCorrectas);									
								}
							}
							chartDataBad[9] = sumOthersBadSimple;							
							chartLabelsBadSimple.splice(9,0,"OTRAS");							
						}
												
						//Tratamiento operaciones Lote Correctas
						if(oJSONGoodBatch.TransByOperation.length <= 9){																			
							chartLabelsGoodBatch = oJSONGoodBatch.TransByOperation.map(function(e){return e.NOMBRE});																					
							chartDataGoodBatch = oJSONGoodBatch.TransByOperation.map(function(e){return e.FirmasLotesCorrectas});													
						}
						else{
							var sumOthersGoodBatch = 0;							
							for(i = 0; i < oJSONGoodBatch.TransByOperation.length; i++){
								if(i < 9){							
									chartLabelsGoodBatch[i] = oJSONGoodBatch.TransByOperation[i].NOMBRE;																	
									chartDataGoodBatch[i] = oJSONGoodBatch.TransByOperation[i].FirmasLotesCorrectas;									
								}
								else{									
									sumOthersGoodBatch += Number(oJSONGoodBatch.TransByOperation[i].FirmasLotesCorrectas);
								}
							}							
							chartDataGood[9] = sumOthersGoodBatch;							
							chartLabelsGoodBatch.splice(9,0,"OTRAS");
						}
						
						//Tratamiento operaciones Lote Incorrectas
						if(oJSONBadBatch.TransByOperation.length <= 9){																										
							chartLabelsBadBatch = oJSONBadBatch.TransByOperation.map(function(e){return e.NOMBRE});																										
							chartDataBadBatch = oJSONBadBatch.TransByOperation.map(function(e){return e.FirmasLotesINCorrectas});
						}
						else{
							var sumOthersBadBatch = 0;						
							for(i = 0; i < oJSONBadBatch.TransByOperation.length; i++){
								if(i < 9){																
									chartLabelsBadBatch[i] = oJSONBadBatch.TransByOperation[i].NOMBRE;																		
									chartDataBadBatch[i] = oJSONBadBatch.TransByOperation[i].FirmasLotesINCorrectas;
								}
								else{																		
									sumOthersBadBatch += Number(oJSONBadBatch.TransByOperation[i].FirmasLotesINCorrectas);
								}
							}							
							chartDataBad[9] = sumOthersBadBatch;
							chartDataBadBatch.splice(9,0,"OTRAS");
						}
						
						var backGroundGood = [""];
						var backGroundBad = [""];
					
						//Obtenemos los colores a mostrar por aplicación 
						// hasta um maximo de 10 colores correspondiendo el último al grupo de "OTRAS" ( Aplicaciones )
						for(i = 0; i < oJSONGoodBatch.TransByOperation.length; i++){
							if(i <= 9){
								backGroundGood[i]=goodColors[i];
								backGroundBad[i]=badColors[i];
							}							
							else{
								break;
							}						
						}
						/**Dibujar las graficas**/
						
						var sLabel = "Transacciones";
						var textGoodSimple = 'Transacciones Simples correctas por aplicación';
						var textBadSimple = 'Transacciones Simples incorrectas por aplicación';
						var textGoodBatch = 'Transacciones Lote correctas por aplicación';
						var textBadBatch = 'Transacciones Lote incorrectas por aplicación';
						
						var idCanvasGoodSimple = "chartGoodSimple";
						var idCanvasBadSimple = "chartBatSimple";
						var idCanvasGoodBatch = "chartGoodBatch";
						var idCanvasBadBatch = "chartBadBatch";
							
						arrCharts = [
							{idCanvas : idCanvasGoodSimple, chartLabels : chartLabelsGoodSimple, chartData : chartDataGoodSimple,backGround : backGroundGood, text: textGoodSimple},
							{idCanvas : idCanvasBadSimple, chartLabels : chartLabelsBadSimple, chartData : chartDataBadSimple, backGround : backGroundBad, text: textBadSimple},
							{idCanvas : idCanvasGoodBatch, chartLabels : chartLabelsGoodBatch, chartData : chartDataGoodBatch, backGround : backGroundGood, text: textGoodBatch},
							{idCanvas : idCanvasBadBatch, chartLabels : chartLabelsBadBatch, chartData : chartDataBadBatch, backGround : backGroundBad, text: textBadBatch}
						];
						
						printPieCharts(sLabel, arrCharts);
						/*****************/
						
						/****** TABLA CON DATOS ******/
						numReg = obJson.TransByOperation.length;		
						total =  Math.ceil(numReg / 10);
						rootSt = "TransByOperationRows";
						captionSt = "Transacciones finalizadas por cada operaci&oacute;n (simple o lote) para el Mes "+ $("#start_date").val()+ ".";
						dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
												
						for (i = 0; i < obJson.TransByOperation.length; i++) {
												
							totBadSimple += Number(obJson.TransByOperation[i].FirmasSimplesINCorrectas);
							totGoodSimple += Number(obJson.TransByOperation[i].FirmasSimplesCorrectas );
							totBadBatch += Number(obJson.TransByOperation[i].FirmasLotesINCorrectas);
							totGoodBatch += Number(obJson.TransByOperation[i].FirmasLotesCorrectas);
													
							if (i != obJson.TransByOperation.length -1){							
								dataJSON += JSON.stringify(obJson.TransByOperation[i]).trim() + ",";
							}
						    else{
						    	dataJSON += JSON.stringify(obJson.TransByOperation[i]).trim() + "]" ;
						    }							
						}
						sumTotalSimple = totBadSimple + totGoodSimple;
						sumTotalBatch = totBadBatch + totGoodBatch;
						
						userData =',"userdata":{"NOMBRE":"Totales","FirmasSimplesCorrectas":"'+totGoodSimple+'", "FirmasSimplesINCorrectas":"'+totBadSimple+'","TOTAL_SIMPLES":"'+sumTotalSimple+ '","FirmasLotesCorrectas":"'+totGoodBatch+'", "FirmasLotesINCorrectas":"'+totBadBatch+'","TOTAL_LOTES":"'+sumTotalBatch+ '"}}';
						dataJSON = dataJSON + userData;
						
						columnNames = ['Nombre App.','Correctas', 'Incorrectas','Total','Correctas', 'Incorrectas','Total'];
						columnModel =[	
							{ name: 'NOMBRE',index:'NOMBRE', width: '200', align: 'left',sortable: true , search:false}, 
					        { name: 'FirmasSimplesCorrectas',index:'FirmasSimplesCorrectas', width: '50', align: 'right',sortable: true , search:false}, 
					        { name: 'FirmasSimplesINCorrectas',index:'FirmasSimplesINCorrectas', width: '50', align: 'right',sortable: true, search:false }, 
					        { name: 'TOTAL_SIMPLES',index:'TOTAL_SIMPLES', width: '50', align: 'right',sortable: true, search:false } ,
					        { name: 'FirmasLotesCorrectas',index:'FirmasSimplesCorrectas', width: '50', align: 'right',sortable: true , search:false}, 
					        { name: 'FirmasLotesINCorrectas',index:'FirmasSimplesINCorrectas', width: '50', align: 'right',sortable: true, search:false }, 
					        { name: 'TOTAL_LOTES',index:'TOTAL_LOTES', width: '50', align: 'right',sortable: true, search:false } 
					        ];
						var group = [
								{startColumnName: 'FirmasSimplesCorrectas', numberOfColumns: 3, titleText: 'Oper. Simple'},
								{startColumnName: 'FirmasLotesCorrectas', numberOfColumns: 3, titleText: 'Oper. Lote'}
								];
						
						printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt,group);
						
						/************/
						
						break;
					case 5: //Documentos firmados por cada aplicacion.
						
						/******* INFORMES *********/
						titlePDF = "Informe de Estadísticas de Fire";
						descriptionPDF = 'Documentos firmados por cada aplicación para el Mes '+ $("#start_date").val()+ '.';
						fileNamePDF = "DocFirmadosPorAplicacion.pdf";
						fileNameExcel = "DocFirmadosPorAplicacion.xlsx";
						fileNameCSV =  "DocFirmadosPorAplicacion.csv";
											
						/****** GRAFICAS *********/	
																
						var oJSONGood = JSON.parse(JSON.stringify(obJson));
						var oJSONBad = JSON.parse(JSON.stringify(obJson));
						
						ordenarDesc(oJSONGood.SignByApp, 'CORRECTAS');											
						ordenarDesc(oJSONBad.SignByApp, 'INCORRECTAS');
																					
						if(oJSONGood.SignByApp.length <= 9){
							
							chartLabelsGood = oJSONGood.SignByApp.map(function(e){return e.NOMBRE});
							chartLabelsBad = oJSONBad.SignByApp.map(function(e){return e.NOMBRE});
							
							chartDataGood = oJSONGood.SignByApp.map(function(e){return e.CORRECTAS});							
							chartDataBad = oJSONBad.SignByApp.map(function(e){return e.INCORRECTAS});
						}
						else{
							var sumOthersGood = 0;
							var sumOthersBad = 0;
							for(i = 0; i < oJSONGood.SignByApp.length; i++){
								if(i < 9){								
									chartLabelsGood[i] = oJSONGood.SignByApp[i].NOMBRE;
									chartLabelsBad[i] = oJSONBad.SignByApp[i].NOMBRE;
									
									chartDataGood[i] = oJSONGood.SignByApp[i].CORRECTAS;
									chartDataBad[i] = oJSONBad.SignByApp[i].INCORRECTAS;
								}
								else{
									sumOthersGood += Number(oJSONGood.SignByApp[i].CORRECTAS);
									sumOthersBad += Number(oJSONBad.SignByApp[i].INCORRECTAS);
								}
							}							
							chartDataGood[9] = sumOthersGood;
							chartDataBad[9] = sumOthersBad;
							chartLabelsGood.splice(9,0,"OTRAS");
							chartLabelsBad.splice(9,0,"OTRAS");
						}
												
						var backGroundGood = [""];
						var backGroundBad = [""];
					
						//Obtenemos los colores a mostrar por aplicación 
						// hasta um maximo de 10 colores correspondiendo el último al grupo de "OTRAS" ( Aplicaciones )
						for(i = 0; i < oJSONGood.SignByApp.length; i++){
							if(i <= 9){
								backGroundGood[i]=goodColors[i];
								backGroundBad[i]=badColors[i];
							}							
							else{
								break;
							}						
						}
						
						/**Dibujar las graficas**/
						
						var sLabel = "Firmas";
						var textGood = 'Firmas correctas por aplicación';
						var textBad = 'Firmas incorrectas por aplicación';										
																
						var idCanvasGood = "chartGood";
						var idCanvasBad = "chartBad";
							
						arrCharts = [
							{idCanvas : idCanvasGood, chartLabels : chartLabelsGood, chartData : chartDataGood,backGround : backGroundGood, text: textGood},
							{idCanvas : idCanvasBad, chartLabels : chartLabelsBad, chartData : chartDataBad, backGround : backGroundBad, text: textBad},						
						];
						printPieCharts(sLabel,arrCharts);
						
						/*****************/
						
						/****** TABLA CON DATOS ******/
						numReg = obJson.SignByApp.length;		
						total =  Math.ceil(numReg / 10);
						rootSt = "SignByAppRows";
						captionSt = "Firmas finalizadas por cada aplicaci&oacute;n para el Mes "+ $("#start_date").val()+ ".";
						dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
												
						for (i = 0; i < obJson.SignByApp.length; i++) {
												
							totBad += Number(obJson.SignByApp[i].INCORRECTAS);
							totGood += Number(obJson.SignByApp[i].CORRECTAS);
													
							if (i != obJson.SignByApp.length -1){							
								dataJSON += JSON.stringify(obJson.SignByApp[i]).trim() + ",";
							}
						    else{
						    	dataJSON += JSON.stringify(obJson.SignByApp[i]).trim() + "]" ;
						    }							
						}
						sumTotal = totBad + totGood;
						userData =',"userdata":{"NOMBRE":"Totales", "INCORRECTAS":"'+totBad+'","CORRECTAS":"'+totGood+ '","TOTAL":"'+sumTotal+'"}}';
						dataJSON = dataJSON + userData;
								
						columnNames = ['Nombre', 'INCORRECTAS','CORRECTAS','TOTAL'];
						columnModel =[				    	 	
					        { name: 'NOMBRE',index:'NOMBRE', width: '250', align: 'left',sortable: true , search:false}, 
					        { name: 'INCORRECTAS',index:'INCORRECTAS', width: '100', align: 'right',sortable: true, search:false }, 
					        { name: 'CORRECTAS',index:'CORRECTAS', width: '100', align: 'right',sortable: true, search:false } ,
					        { name: 'TOTAL',index:'TOTAL', width: '100', align: 'right',sortable: true, search:false } 
					        ];
						
						printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt,null);					
						
						break;
					case 6:	//Documentos firmados por cada origen de certificados/proveedor.
						
						/******* INFORMES *********/
						titlePDF = "Informe de Estadísticas de Fire";
						descriptionPDF = 'Documentos firmados por cada origen de certificados/proveedor para el Mes '+ $("#start_date").val()+ '.';
						fileNamePDF = "DocFirmadosPorProveedor.pdf";
						fileNameExcel = "DocFirmadosPorProveedor.xlsx";
						fileNameCSV =  "DocFirmadosPorProveedor.csv";
											
						/****** GRAFICAS *********/	
						
						var oJSONGood = JSON.parse(JSON.stringify(obJson));
						var oJSONBad = JSON.parse(JSON.stringify(obJson));
						
						ordenarDesc(oJSONGood.SignByProv, 'CORRECTAS');											
						ordenarDesc(oJSONBad.SignByProv, 'INCORRECTAS');
												

						
						if(chartLabelsGood.length <= 9){
							chartLabelsGood = oJSONGood.SignByProv.map(function(e){return e.NOMBRE});
							chartLabelsBad = oJSONBad.SignByProv.map(function(e){return e.NOMBRE});
							
							chartDataGood = oJSONGood.SignByProv.map(function(e){return e.CORRECTAS});							
							chartDataBad = oJSONBad.SignByProv.map(function(e){return e.INCORRECTAS});

						}
						else{
							var sumOthersGood = 0;
							var sumOthersBad = 0;
							for(i = 0; i < chartLabelsGood.length; i++){
								if(i < 9){
									chartLabelsGood[i] = oJSONGood.SignByProv[i].NOMBRE;
									chartLabelsBad[i] = oJSONBad.SignByProv[i].NOMBRE;
									
									chartDataGood[i] = oJSONGood.SignByProv[i].CORRECTAS;
									chartDataBad[i] = oJSONBad.SignByProv[i].INCORRECTAS;
								}
								else{
									sumOthersGood += Number(oJSONGood.SignByProv[i].CORRECTAS);
									sumOthersBad += Number(oJSONBad.SignByProv[i].INCORRECTAS);
								}
							}							
							chartDataGood[9] = sumOthersGood;
							chartDataBad[9] = sumOthersBad;
							chartLabelsGood.splice(9,0,"OTRAS");
							chartLabelsBad.splice(9,0,"OTRAS");
						}
												
						var backGroundGood = [""];
						var backGroundBad = [""];
					
						//Obtenemos los colores a mostrar por aplicación 
						// hasta um maximo de 10 colores correspondiendo el último al grupo de "OTRAS" ( Aplicaciones )
						for(i = 0; i < chartLabelsGood.length; i++){
							if(i <= 9){
								backGroundGood[i]=goodColors[i];
								backGroundBad[i]=badColors[i];
							}							
							else{
								break;
							}						
						}
						
						/**Dibujar las graficas**/
						
						var sLabel = "Firmas";
						var textGood = 'Firmas correctas por proveedor';
						var textBad = 'Firmas incorrectas por proveedor';										
																
						var idCanvasGood = "chartGood";
						var idCanvasBad = "chartBad";
							
						arrCharts = [
							{idCanvas : idCanvasGood, chartLabels : chartLabelsGood, chartData : chartDataGood,backGround : backGroundGood, text: textGood},
							{idCanvas : idCanvasBad, chartLabels : chartLabelsBad, chartData : chartDataBad, backGround : backGroundBad, text: textBad},						
						];
						printPieCharts(sLabel,arrCharts);
						
						/*****************/
						
						/****** TABLA CON DATOS ******/
						numReg = obJson.SignByProv.length;		
						total =  Math.ceil(numReg / 10);
						rootSt = "SignByProvRows";
						captionSt = 'Firmas finalizadas por cada proveedor para el Mes '+ $("#start_date").val()+ '.';
						dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
												
						for (i = 0; i < obJson.SignByProv.length; i++) {
												
							totBad += Number(obJson.SignByProv[i].INCORRECTAS);
							totGood += Number(obJson.SignByProv[i].CORRECTAS);
													
							if (i != obJson.SignByProv.length -1){							
								dataJSON += JSON.stringify(obJson.SignByProv[i]).trim() + ",";
							}
						    else{
						    	dataJSON += JSON.stringify(obJson.SignByProv[i]).trim() + "]" ;
						    }							
						}
						sumTotal = totBad + totGood;
						userData =',"userdata":{"NOMBRE":"Totales", "INCORRECTAS":"'+totBad+'","CORRECTAS":"'+totGood+ '","TOTAL":"'+sumTotal+'"}}';
						dataJSON = dataJSON + userData;
								
						columnNames = ['Nombre', 'INCORRECTAS','CORRECTAS','TOTAL'];
						columnModel =[				    	 	
					        { name: 'NOMBRE',index:'NOMBRE', width: '250', align: 'left',sortable: true , search:false}, 
					        { name: 'INCORRECTAS',index:'INCORRECTAS', width: '100', align: 'right',sortable: true, search:false }, 
					        { name: 'CORRECTAS',index:'CORRECTAS', width: '100', align: 'right',sortable: true, search:false } ,
					        { name: 'TOTAL',index:'TOTAL', width: '100', align: 'right',sortable: true, search:false } 
					        ];
						
						printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt,null);	
						
						break;
					case 7:	//Documentos firmados en cada formato de firma.
						
						/******* INFORMES *********/
						titlePDF = "Informe de Estadísticas de Fire";
						descriptionPDF = 'Documentos firmados en cada formato de firma para el Mes '+ $("#start_date").val()+ '.';
						fileNamePDF = "DocFirmadosPorFormato.pdf";
						fileNameExcel = "DocFirmadosPorFormato.xlsx";
						fileNameCSV =  "DocFirmadosPorFormato.csv";
											
						/****** GRAFICAS *********/	
						
						var oJSONGood = JSON.parse(JSON.stringify(obJson));
						var oJSONBad = JSON.parse(JSON.stringify(obJson));
						
						ordenarDesc(oJSONGood.SignByFormat, 'CORRECTAS');											
						ordenarDesc(oJSONBad.SignByFormat, 'INCORRECTAS');
											
						
						if(obJson.SignByFormat.length <= 9){
							chartLabelsGood = oJSONGood.SignByFormat.map(function(e){return e.NOMBRE});
							chartLabelsBad = oJSONBad.SignByFormat.map(function(e){return e.NOMBRE});
							
							chartDataGood = oJSONGood.SignByFormat.map(function(e){return e.CORRECTAS});							
							chartDataBad = oJSONBad.SignByFormat.map(function(e){return e.INCORRECTAS});

						}
						else{
							var sumOthersGood = 0;
							var sumOthersBad = 0;
							for(i = 0; i < obJson.SignByFormat.length; i++){
								if(i < 9){
									chartDataGood[i] = oJSONGood.SignByFormat[i].CORRECTAS;
									chartDataBad[i] = oJSONBad.SignByFormat[i].INCORRECTAS;
								}
								else{
									sumOthersGood += Number(oJSONGood.SignByFormat[i].CORRECTAS);
									sumOthersBad += Number(oJSONBad.SignByFormat[i].INCORRECTAS);
								}
							}							
							chartDataGood[9] = sumOthersGood;
							chartDataBad[9] = sumOthersBad;
							chartLabelsGood.splice(9,0,"OTRAS");
							chartLabelsBad.splice(9,0,"OTRAS");
						}
												
						var backGroundGood = [""];
						var backGroundBad = [""];
					
						//Obtenemos los colores a mostrar por aplicación 
						// hasta um maximo de 10 colores correspondiendo el último al grupo de "OTRAS" ( Aplicaciones )
						for(i = 0; i < obJson.SignByFormat.length; i++){
							if(i <= 9){
								backGroundGood[i]=goodColors[i];
								backGroundBad[i]=badColors[i];
							}							
							else{
								break;
							}						
						}
						
						/**Dibujar las graficas**/
						
						var sLabel = "Firmas";
						var textGood = 'Firmas correctas por formato';
						var textBad = 'Firmas incorrectas por formato';										
																
						var idCanvasGood = "chartGood";
						var idCanvasBad = "chartBad";
							
						arrCharts = [
							{idCanvas : idCanvasGood, chartLabels : chartLabelsGood, chartData : chartDataGood,backGround : backGroundGood, text: textGood},
							{idCanvas : idCanvasBad, chartLabels : chartLabelsBad, chartData : chartDataBad, backGround : backGroundBad, text: textBad},						
						];
						printPieCharts(sLabel,arrCharts);
						
						/*****************/
						
						/****** TABLA CON DATOS ******/
						numReg = obJson.SignByFormat.length;		
						total =  Math.ceil(numReg / 10);
						rootSt = "SignByFormatRows";
						captionSt = 'Firmas finalizadas por cada formato para el Mes '+ $("#start_date").val()+ '.';
						dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
												
						for (i = 0; i < obJson.SignByFormat.length; i++) {
												
							totBad += Number(obJson.SignByFormat[i].INCORRECTAS);
							totGood += Number(obJson.SignByFormat[i].CORRECTAS);
													
							if (i != obJson.SignByFormat.length -1){							
								dataJSON += JSON.stringify(obJson.SignByFormat[i]).trim() + ",";
							}
						    else{
						    	dataJSON += JSON.stringify(obJson.SignByFormat[i]).trim() + "]" ;
						    }							
						}
						sumTotal = totBad + totGood;
						userData =',"userdata":{"NOMBRE":"Totales", "INCORRECTAS":"'+totBad+'","CORRECTAS":"'+totGood+ '","TOTAL":"'+sumTotal+'"}}';
						dataJSON = dataJSON + userData;
								
						columnNames = ['Nombre', 'INCORRECTAS','CORRECTAS','TOTAL'];
						columnModel =[				    	 	
					        { name: 'NOMBRE',index:'NOMBRE', width: '250', align: 'left',sortable: true , search:false}, 
					        { name: 'INCORRECTAS',index:'INCORRECTAS', width: '100', align: 'right',sortable: true, search:false }, 
					        { name: 'CORRECTAS',index:'CORRECTAS', width: '100', align: 'right',sortable: true, search:false } ,
					        { name: 'TOTAL',index:'TOTAL', width: '100', align: 'right',sortable: true, search:false } 
					        ];
						
						printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt,null);	
						break;
					case 8:	//Documentos que utilizan cada formato de firma longevo
						
						
						/******* INFORMES *********/
						titlePDF = "Informe de Estadísticas de Fire";
						descriptionPDF = 'Documentos firmados en cada formato longevo para el Mes '+ $("#start_date").val()+ '.';
						fileNamePDF = "DocFirmadosPorFormatoLongevo.pdf";
						fileNameExcel = "DocFirmadosPorFormatoLongevo.xlsx";
						fileNameCSV =  "DocFirmadosPorFormatoLongevo.csv";
											
						/****** GRAFICAS *********/	
						
						var oJSONGood = JSON.parse(JSON.stringify(obJson));
						var oJSONBad = JSON.parse(JSON.stringify(obJson));
						
						ordenarDesc(oJSONGood.SignByLongLiveFormat, 'CORRECTAS');											
						ordenarDesc(oJSONBad.SignByLongLiveFormat, 'INCORRECTAS');					
						

						
						if(oJSONGood.SignByLongLiveFormat.length <= 9){
							chartLabelsGood = oJSONGood.SignByLongLiveFormat.map(function(e){return e.NOMBRE});
							chartLabelsBad = oJSONBad.SignByLongLiveFormat.map(function(e){return e.NOMBRE});
							chartDataGood = oJSONGood.SignByLongLiveFormat.map(function(e){return e.CORRECTAS});							
							chartDataBad = oJSONBad.SignByLongLiveFormat.map(function(e){return e.INCORRECTAS});

						}
						else{
							var sumOthersGood = 0;
							var sumOthersBad = 0;
							for(i = 0; i < oJSONGood.SignByLongLiveFormat.length; i++){
								if(i < 9){
									chartLabelsGood[i] = oJSONGood.SignByLongLiveFormat[i].NOMBRE;
									chartLabelsBad[i] = oJSONBad.SignByLongLiveFormat[i].NOMBRE;
									
									chartDataGood[i] = oJSONGood.SignByLongLiveFormat[i].CORRECTAS;
									chartDataBad[i] = oJSONBad.SignByLongLiveFormat[i].INCORRECTAS;
								}
								else{
									sumOthersGood += Number(oJSONGood.SignByLongLiveFormat[i].CORRECTAS);
									sumOthersBad += Number(oJSONBad.SignByLongLiveFormat[i].INCORRECTAS);
								}
							}							
							chartDataGood[9] = sumOthersGood;
							chartDataBad[9] = sumOthersBad;
							chartLabelsGood.splice(9,0,"OTRAS");
							chartLabelsBad.splice(9,0,"OTRAS");
						}
												
						var backGroundGood = [""];
						var backGroundBad = [""];
					
						//Obtenemos los colores a mostrar por aplicación 
						// hasta um maximo de 10 colores correspondiendo el último al grupo de "OTRAS" ( Aplicaciones )
						for(i = 0; i < oJSONGood.SignByLongLiveFormat.length; i++){
							if(i <= 9){
								backGroundGood[i]=goodColors[i];
								backGroundBad[i]=badColors[i];
							}							
							else{
								break;
							}						
						}
						
						/**Dibujar las graficas**/
						
						var sLabel = "Firmas";
						var textGood = 'Firmas correctas por formato longevo';
						var textBad = 'Firmas incorrectas por formato longevo';										
																
						var idCanvasGood = "chartGood";
						var idCanvasBad = "chartBad";
							
						arrCharts = [
							{idCanvas : idCanvasGood, chartLabels : chartLabelsGood, chartData : chartDataGood,backGround : backGroundGood, text: textGood},
							{idCanvas : idCanvasBad, chartLabels : chartLabelsBad, chartData : chartDataBad, backGround : backGroundBad, text: textBad},						
						];
						printPieCharts(sLabel,arrCharts);
						
						/*****************/
						
						/****** TABLA CON DATOS ******/
						numReg = obJson.SignByLongLiveFormat.length;		
						total =  Math.ceil(numReg / 10);
						rootSt = "SignByLongLiveFormatRows";
						captionSt = 'Firmas finalizadas por cada formato longevo para el Mes '+ $("#start_date").val()+ '.';
						dataJSON = dataJSON + total +',"ActualPage":1,"TotalRecords":'+numReg+',"'+rootSt+'":[';
												
						for (i = 0; i < obJson.SignByLongLiveFormat.length; i++) {
												
							totBad += Number(obJson.SignByLongLiveFormat[i].INCORRECTAS);
							totGood += Number(obJson.SignByLongLiveFormat[i].CORRECTAS);
													
							if (i != obJson.SignByLongLiveFormat.length -1){							
								dataJSON += JSON.stringify(obJson.SignByLongLiveFormat[i]).trim() + ",";
							}
						    else{
						    	dataJSON += JSON.stringify(obJson.SignByLongLiveFormat[i]).trim() + "]" ;
						    }							
						}
						sumTotal = totBad + totGood;
						userData =',"userdata":{"NOMBRE":"Totales", "INCORRECTAS":"'+totBad+'","CORRECTAS":"'+totGood+ '","TOTAL":"'+sumTotal+'"}}';
						dataJSON = dataJSON + userData;
								
						columnNames = ['Nombre', 'INCORRECTAS','CORRECTAS','TOTAL'];
						columnModel =[				    	 	
					        { name: 'NOMBRE',index:'NOMBRE', width: '250', align: 'center',sortable: true , search:false}, 
					        { name: 'INCORRECTAS',index:'INCORRECTAS', width: '100', align: 'right',sortable: true, search:false }, 
					        { name: 'CORRECTAS',index:'CORRECTAS', width: '100', align: 'right',sortable: true, search:false } ,
					        { name: 'TOTAL',index:'TOTAL', width: '100', align: 'right',sortable: true, search:false } 
					        ];
						
						printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt,null);	
						break;
					
				}
			
			}).fail(function() {
				var json ={Error:[{Code:204,Message:"Error al recivir los datos del servidor"}]};
				printErrorResult(json);
			  });
		}
		
		
	});//fin formUser submit!
	 
	
		
			/**
			 * Evento del boton Exportar a PDF, lógica para crear el docuemnto PDF
			 */
			$("#pdf").on("click", function(){
				
				var arrChartsJPG = new Array();
				//Recorremos los canvas obteniendo las imagenes de los gráficos en formato JPG 
				//almacenándolas en un array				
				$("canvas").each(function(index){			
					var canvas = document.getElementById($(this).attr('id'));					
					var imgJPG = canvas.toDataURL("image/jpeg");			
					arrChartsJPG.push(imgJPG);					
				});
							
				$("#resultQuery").jqGrid("exportToPdf",{
					title: titlePDF,
					orientation: 'portrait',
					pageSize: 'A4',
					description:descriptionPDF,
					onBeforeExport : function(doc) {
						//Introducimos en la definición del documento PDF las imágenes de los Gráficos
						var images =  new Array();
						for(i = 0; i < arrChartsJPG.length; i++){						
							images.push({image:arrChartsJPG[i], width:200});
							if(i % 2 == 1){
								doc.content.push({columns:images});
								images = new Array();
							}
						}
						if(images.length == 1){
							doc.content.push({columns:images});
						}
																		
					},
					customSettings: null,
					download: 'download',
					includeLabels : true,
					includeGroupHeader : true,
					includeFooter: true,
					fileName : fileNamePDF
					
				})			
			});
			
			/**
			 * Evento del boton Exportar a Excel, lógica para crear el documento xslx
			 */
			$("#excel").on("click", function(){				
				$("#resultQuery").jqGrid("exportToExcel",{
				includeLabels : true,
				includeGroupHeader : true,
				includeFooter: true,
				fileName : fileNameExcel,
				maxlength : 100 // maxlength for visible string data 
				});
			});
			
			/**
			 *Evento del boton Exportar a texto con formato CSV, lógica para crear el documento csv 
			 */
			$("#csv").on("click", function(){						
				$("#resultQuery").jqGrid("exportToCsv",{
					separator: ",",
					separatorReplace : "", // in order to interpret numbers
					quote : '"', 
					escquote : '"', 
					newLine : "\r\n", // navigator.userAgent.match(/Windows/) ?	'\r\n' : '\n';
					replaceNewLine : " ",
					includeCaption : true,
					includeLabels : true,
					includeGroupHeader : true,
					includeFooter: true,
					fileName : fileNameCSV,
					returnAsString : false
				})				
			});	
	
	/**
	 * Dibuja las gráficas con formato de tarta.
	 * @param sLabel 
	 * @param  arrCharts : Array de objetos de configuración del gráfico (Etiquetas, datos, colores y título)
	 */		
	function printPieCharts(sLabel,arrCharts){ 


		$("#ChartsContend").html("");
				
		for(i = 0; i < arrCharts.length; i++){
				
			//generamos los canvas en donde se van a crear los grágicos.
			$("#ChartsContend").append('<div id="'+ arrCharts[i].idCanvas +'" style="display: inline-block; width:45%; height: 45%;padding-left: 2%; padding-right:2%;"></div>');
			$("#"+arrCharts[i].idCanvas).append('<canvas id="pie'+ arrCharts[i].idCanvas+'" width="100px" height="100px"></canvas>');
			
			if(i % 2 == 1){
				$("#ChartsContend").append('<br>');
			}
			
			var pie = document.getElementById('pie'+ arrCharts[i].idCanvas);
			var ctx = pie.getContext('2d');				
			//Pintamos el fondo del canvas en blanco para poder traspasar la imagen al documento PDF
			//ya que por defecto la genera con fondo transparente y al obtenerla en JPEG el fondo lo genera en color negro.
			Chart.plugins.register({
				  beforeDraw: function(chartInstance) {
				    var ctx = chartInstance.chart.ctx;
				    ctx.fillStyle = "white"; //color de fondo al que queremos que se genere el gráfico 
				    ctx.fillRect(0, 0, chartInstance.chart.width, chartInstance.chart.height);
				  }
				});
			
			//Generamos el gráfico
			var pieChart = new Chart(ctx, {
				  type: 'pie', //tipo de gráfico (tarta)
				  data: {
				      labels: arrCharts[i].chartLabels, // array de etiquetas
				      datasets: [{
				        label: sLabel, //etiqueta del conjunto de datos
				        backgroundColor: arrCharts[i].backGround, // array de colores
				        data: arrCharts[i].chartData // array de datos
				      }]
				    },
				    options: {
				      title: {
				        display: true,
				        text: arrCharts[i].text //Título
				      }, 
				      animation: {
				    	  duration:500,// tiempo de duración de la animación en milisegundos
				    	  onComplete: function() {}
				      }
			     
				    }
				}); 
			
		}// fin del bucle.
					
	}		
	
	/**
	 * Dibuja la tabla de datos resultado de la consulta.
	 * @param columnNames: Array de configuración de los nombres de columnas a mostrar
	 * @param columnModel: Array de configuración de los datos en las columnas a mostrar
	 * @param dataJSON: Fuente de datos en formato JSON
	 * @param rootSt: raiz del objeto JSON indicado en el dataJSON
	 * @param captionSt : Título
	 * @param grupo: indica el nombre de  Grupo de columnas, null en caso de no haber grupo.
	 */
	function printTableData(columnNames,columnModel,dataJSON,rootSt,captionSt, group){
		$("#jQGrid").html("");		
		$("#jQGrid").append("<table id='resultQuery'></table><div id='page'></div>");
		
		$("#resultQuery").jqGrid({
			colNames: columnNames,
		    colModel: columnModel,
		     pager: '#page',
		     datatype: "jsonstring",
		     datastr: dataJSON,
		     jsonReader: {
		    	 repeatitems: false,
		    	 root: rootSt,
		    	 page: "ActualPage",
		    	 total:"TotalPages" ,
		    	 records: "TotalRecords"
		     },
		     rowNum: 10,
		     rowList:[10,20,30],
		     viewrecords: true,
		     loadonce: true,
		     footerrow: true,
		     userDataOnFooter : true,
		     caption: captionSt,
		     height: "auto",
		     width:"auto",
		     ignoreCase: true
		    });
		    
		$("#resultQuery").jqGrid('navGrid', '#page',
		        { add: false, edit: false, del: false, search: false, refresh: false }, {}, {}, {},
		        { multipleSearch: false, multipleGroup: false });
		
		if(group != null){
		
			jQuery("#resultQuery").jqGrid('setGroupHeaders', {
				  useColSpanStyle: false, 
				  groupHeaders:group					  		
			});
		}
									
	}
	

	/*Limpiar los campos */
	document.getElementById("formStatictics").onreset = function() {resetFunction()};
		
	/**
	 * Inicializa los campos de consulta y borra los resultados anteriores.
	 */
	function resetFunction() {
		$("label").each(function( index ) {
			this.style.color = "#000000";
			var idInput=$(this).attr('for');
			$('#'+idInput).css({backgroundColor:'#FFFFFF'});    
		});
		$("#jQGrid").html("");
		$("#ChartsContend").html("");
		//se deshabilitan los botones de exportar
		
		disabledExportsButtons(true);
		
		var html = "";	
		$("#error-txt").html(html);
		$("#error-txt").hide();
	}
	
	/**
	 * Ordena el array de un Objeto Json de forma Ascendente
	 *  @example ordenarAsc(oJson.nombreArray,'nombre_key')
	 */
	function ordenarAsc(array_json, key) {
		   array_json.sort(function (a, b) {
		      return a[key] > b[key];
		   });
		}
	/**
	 * Ordena el array de un Objeto Json de forma Descendente
	 * @example ordenarDesc(oJson.nombreArray,'nombre_key')
	 */
	function ordenarDesc(array_json, key) {
		   array_json.sort(function (a, b) {
		      return a[key] < b[key];
		   });
		}
	
	
	 /**
	  * Muestra en pantalla el mensaje de error indicado en el objeto JSON que se le pasa.
	  * @returns
	  */
	 function printErrorResult(JSONData){
		var html = "";	
		$("#error-txt").html(html);
		html = JSONData.Error[0].Message;
		$("#error-txt").append(html);
		$("#error-txt").css('display:inline-block');
		$("#error-txt").show();	
		disabledExportsButtons(true);
		$("#jQGrid").html("");
		$("#ChartsContend").html("");
	 }
	/**
	 * Habilita y deshabilita la propiedad disabled de los botones de acción de exportar
	 * an CSV, EXCEL y PDF
	 */
	 function disabledExportsButtons(disabled){
		 $("#actionButtons > button").each(function(){
				$(this).prop('disabled',disabled);
			});
	 }
	
}); //fin document ready!