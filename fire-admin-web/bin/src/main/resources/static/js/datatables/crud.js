/* 
 * Requiere:
 *    - buttons.dataTables
 *    - select.dataTables
 */

var tbl = $('.dataTable').DataTable({
    data:{data:[{data:'id', type:'string'}, {data:'campo_1', type:'string'}/*, {data:'campo_n', type:'string'}*/]},
    columns:{data:[{title:'Identificador Primario'}, {title:'placeholder_1'}/*, {title:'placeholder_n'}*/]},
    
    "sPaginationType": "full_numbers",
    dom: 'Bfrtip',
    select: 'single',
    responsive: true,
    
    altEditor: true,
    buttons: [{text: 'Agregar',name: 'add'}, {extend: 'selected',text: 'Editar',name: 'edit'}, {extend: 'selected',text: 'Eliminar',name: 'delete'}],
    "columnDefs": [{
        "targets": [0],//id
        "visible": false,
        "searchable": false
    }],
}).on('savedata', function(e, accion, pkid, data, rowindex){
    // e          Evento Jquery
    // accion     [add|edit|delete]
    // pkid       Primer campo en la data [id]                ... en add,    retorna null
    // data       Los campos adicionales  [campo_1, campo_n]  ... en delete, retorna null
    // rowindex   El index de la fila para el dataTable       ... en add,    retorna null
    
    $('#altEditor-modal .modal-body .alert').remove();
    $('#altEditor-modal .modal-body').append('<div class="alert alert-info" role="alert"><strong>Subiendo información en el servidor!</strong></div>');
    
    switch(accion){
        case 'add':
            $.ajax('[ruta a enviar los datos a guardar]', {
                data : $.param({'accion':'add', 'data':JSON.stringify(data)}),
                type:'POST',
                success: function(data){
                    // preferentemente retornar data como JSON
                    // --- data : {data:{id:'new_pkid', campo_1:'', campo_n:''}}
                    
                    tbl.row.add(data.data).draw(false);
                    
                    $('#altEditor-modal .modal-body .alert').remove();
                    $('#altEditor-modal').modal('hide');
                },
                error:function(){}
            });
            break;
        case 'edit':
            $.ajax('[ruta a enviar los datos a guardar]', {
                data:$.param({'accion':'edit', 'pkid':pkid, 'data':JSON.stringify(data), 'rowindex':rowindex}),
                type:'POST',
                success: function(data){
                    // preferentemente retornar data como JSON
                    // --- data : {data:{id:'new_pkid', campo_1:'', campo_n:''}, rowindex:'$_POST[rowindex]'}
                    
                    tbl.row(data.rowindex).data(data.data).draw();
                    
                    $('#altEditor-modal .modal-body .alert').remove();
                    $('#altEditor-modal').modal('hide');
                },
                error:function(){}
            });
            break;
        case 'delete':
            $.ajax('[ruta a enviar los datos a guardar]',{
                data:$.param({'accion':'delete', 'pkid':pkid, 'rowindex':rowindex}),
                type:'POST',
                success: function(data){
                    // preferentemente retornar data como JSON
                    // --- data : {rowindex:'$_POST[rowindex]'}
                    
                    tbl.row(data.rowindex).remove().draw();
                    
                    $('#altEditor-modal .modal-body .alert').remove();
                    $('#altEditor-modal').modal('hide');
                },
                error:function(){}
            });
            break;
        default:
            $('#altEditor-modal .modal-body .alert').remove();
            $('#altEditor-modal .modal-body').append('<div class="alert alert-danger" role="alert"><strong>Acción "'+accion+'" no autorizada!</strong></div>');
            break;
    }
});


$.fn.serializeArrayObj=function(a){$params=this.serializeArray(),$obj={};for($x in $params)$dats=$params[$x],$dats.name=$dats.name.replace("[]",""),"undefined"==typeof $obj[$dats.name]&&($obj[$dats.name]={length:0}),$obj[$dats.name][$obj[$dats.name].length]=$dats.value,$obj[$dats.name].length++;return"undefined"==typeof a&&(a=!0),a&&($obj=JSON.stringify($obj)),$obj};