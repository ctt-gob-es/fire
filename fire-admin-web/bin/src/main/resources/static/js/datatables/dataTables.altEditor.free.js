/*! Datatables altEditor 2.0
 */

/**
 * @summary     altEditor
 * @description Lightweight editor for DataTables
 * @version     1.0
 * @file        dataTables.editor.lite.js
 * @author      kingkode (www.kingkode.com)
 * @contact     www.kingkode.com/contact
 * @copyright   Copyright 2016 Kingkode
 *
 * This source file is free software, available under the following license:
 *   MIT license - http://datatables.net/license/mit
 *
 * This source file is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the license files for details.
 *
 * For details please refer to: http://www.kingkode.com
 */
 
/*
 * Modificado por Jesús Rojas (jrojash@jys.pe)
 * Modificado por Ricoh.
 */

! function(t) {
    "function" == typeof define && define.amd ? define(["jquery", "datatables.net"], function(o) {
        return t(o, window, document)
    }) : "object" == typeof exports ? module.exports = function(o, a) {
        return o || (o = window), a && a.fn.dataTable || (a = require("datatables.net")(o, a).$), t(a, o, o.document)
    } : t(jQuery, window, document)
}(function(t, o, a, e) {
    "use strict";
    var d = t.fn.dataTable,
        l = 0,
        i = function(o, a) {
            if (!d.versionCheck || !d.versionCheck("1.10.8")) throw "Warning: altEditor requires DataTables 1.10.8 or greater";
            this.c = t.extend(!0, {}, d.defaults.altEditor, i.defaults, a), this.s = {
                dt: new d.Api(o),
                namespace: ".altEditor" + l++
            }, this.dom = {
                modal: t('<div class="dt-altEditor-handle"/>')
            }, this._constructor()
        };
    return t.extend(i.prototype, {
        _constructor: function() {
            var o = this,
                e = this.s.dt;
            this._setup(), e.on("destroy.altEditor", function() {
                e.off(".altEditor"), t(e.table().body()).off(o.s.namespace), t(a.body).off(o.s.namespace)
            })
        },
        _setup: function() {
            var o = this;
            this.s.dt;
            //t("body").append('<div class="modal fade" id="altEditor-modal"><div class="modal-dialog"><div class="modal-content"><div class="bd p-15"><h5 class="m-0" id="modal-title"></h5><button type="button" class="close" data-dismiss="modal" aria-label="Cerrar"><span aria-hidden="true">&times;</span></button></div><div class="modal-body"><p></p></div><div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">Cerrar</button><button type="button" class="btn btn-primary">Save changes</button></div></div></div></div>'), this.s.dt.button("edit:name") && (this.s.dt.button("edit:name").action(function(t, a, e, d) {
            t("body").append('<div class="modal fade" id="altEditor-modal" tabindex="-1" role="dialog"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h4 class="modal-title"></h4><button type="button" class="close" data-dismiss="modal" aria-label="Cerrar" id="closeRowBtn"><span aria-hidden="true">&times;</span></button></div><div class="modal-body"><p></p></div><div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">Cerrar</button><button type="button" class="btn btn-primary">Save changes</button></div></div></div></div>'), this.s.dt.button("edit:name") && (this.s.dt.button("edit:name").action(function(t, a, e, d) {
                a.rows({
                    selected: !0
                }).count();
                o._openEditModal()
            }), t(a).on("click", "#editRowBtn", function(t) {
                t.preventDefault(), t.stopPropagation(), o._editRowData()
            })), this.s.dt.button("delete:name") && (this.s.dt.button("delete:name").action(function(t, a, e, d) {
                a.rows({
                    selected: !0
                }).count();
                o._openDeleteModal()
            }), t(a).on("click", "#deleteRowBtn", function(t) {
                t.preventDefault(), t.stopPropagation(), o._deleteRow()
            })), this.s.dt.button("add:name") && (this.s.dt.button("add:name").action(function(t, a, e, d) {
                a.rows({
                    selected: !0
                }).count();
                o._openAddModal()
            }), t(a).on("click", "#addRowBtn", function(t) {
                t.preventDefault(), t.stopPropagation(), o._addRowData()
            }))
        },
        _emitEvent: function(o, a) {
            this.s.dt.iterator("table", function(e, d) {
                t(e.nTable).triggerHandler(o + ".dt", a)
            })
        },
        _openEditModal: function() {
            for (var o = this.s.dt, a = [], e = 0; e < o.context[0].aoColumns.length; e++) a.push({
                id: o.context[0].aoColumns[e].mData,
            	title: o.context[0].aoColumns[e].sTitle                
            });
            var d = o.rows({
                    selected: !0
                }),
            l = "";

            var editTemplate = o.context[0].oAjaxData.editTemplate;
            var formId = o.context[0].oAjaxData.formId;
              
            // Se carga el formulario en el modal mediante una llamada Ajax al servidor.
            // De esta manera obtenemos un formulario con las etiquetas thymeleaf
            // preprocesadas y favorecemos la modularización de componentes.
            $.ajax({
            	url: editTemplate+ "?id=" + d.data()[0][a[0].id],
            	type: 'POST',
            	data: null,
            	success: function(data){
                   var l = data;

                   t("#altEditor-modal").on("show.bs.modal", function() {
                       t("#altEditor-modal").find(".modal-title").html("Editar Registro"), 
                       t("#altEditor-modal").find(".modal-body").html(l), 
                       t("#altEditor-modal").find(".modal-footer").html("<button type='button' data-content='remove' class='btn btn-default' data-dismiss='modal' id='closeRowBtn'>Cerrar</button><button type='button' data-content='remove' class='btn btn-primary' id='editRowBtn'>Guardar Cambios</button>")
                     }), t("#altEditor-modal").modal("show"), t("#altEditor-modal input.primarykey+div input").focus()
                      
                },
                error:function(){}
            });
                        
        },
        _editRowData: function() {
            var o = this,
                a = this.s.dt,
                e = {},
                d = t('form[name="altEditor-form"] input.primarykey').attr("name");
            t('form[name="altEditor-form"] input, select').each(function(o) {
                e[t(this).attr("id")] = t(this).val()
            }), t("#altEditor-modal .modal-body .alert").remove();
            var l = a.row({
                    selected: !0
                }).index(),
                i = '<div class="alert alert-success" role="alert"><strong>Satisfactorio!</strong> El registro ha sido actualizado en la base datos.</div>';
            t("#altEditor-modal .modal-body").append(i), t(o.s.dt.context[0].nTable).trigger("crudaction", ["edit", d, e, l])
        },
        _openDeleteModal: function() {
            for (var o = this.s.dt, a = [], e = 0; e < o.context[0].aoColumns.length; e++) a.push({
            	id: o.context[0].aoColumns[e].mData,
                title: o.context[0].aoColumns[e].sTitle
            });
            var d = o.rows({
                    selected: !0
                }),
                l = "";
            l += "<form name='altEditor-form' role='form'>", 
            l += "<input type='hidden' class='primarykey' id='" + a[0].id + "' name='" + a[0].title + "' placeholder='" + a[0].title + "' value='" + d.data()[0][a[0].id] + "'>";
            l += "<p>¿Está seguro de que quiere eliminar el registro?</p>";
            l += "</form>", t("#altEditor-modal").on("show.bs.modal", function() {
                t("#altEditor-modal").find(".modal-title").html("Eliminar"), t("#altEditor-modal").find(".modal-body").html("<pre class='modal-pre'>" + l + "</pre>"), t("#altEditor-modal").find(".modal-footer").html("<button type='button' data-content='remove' class='btn btn-default' data-dismiss='modal' id='closeRowBtn'>Cerrar</button><button type='button' data-content='remove' class='btn btn-danger' id='deleteRowBtn'>Eliminar</button>")
            }), t("#altEditor-modal").modal("show"), t("#altEditor-modal input.primarykey+div input").focus()
                        
        },
        _deleteRow: function() {
            var o = this,
                a = this.s.dt;
            t("#altEditor-modal .modal-body .alert").remove();
            var e = {},
                d = t('form[name="altEditor-form"] input.primarykey').attr("value");
            t('form[name="altEditor-form"] input').each(function(o) {
                e[t(this).attr("id")] = t(this).val()
            });
            var l = a.row({
                selected: !0
            }).index();
            t(o.s.dt.context[0].nTable).trigger("crudaction", ["delete", d, e, l]), a.draw()
        },
        _openAddModal: function() {
            for (var o = this.s.dt, a = [], e = 0; e < o.context[0].aoColumns.length; e++) a.push({
            	id: o.context[0].aoColumns[e].mData,
                title: o.context[0].aoColumns[e].sTitle
            });
            var d = "";
                        
            var addTemplate = o.context[0].oAjaxData.addTemplate;
            var formId = o.context[0].oAjaxData.formId;
            
            // Se carga el formulario en el modal mediante una llamada Ajax al servidor.
            // De esta manera obtenemos un formulario con las etiquetas thymeleaf
            // preprocesadas y favorecemos la modularización de componentes.
            $.ajax({
            	url: addTemplate,
            	type: 'POST',
            	data: null,
            	success: function(data){
                   var d = data;

                   t("#altEditor-modal").on("show.bs.modal", function() {
                      t("#altEditor-modal").find(".modal-title").html("Agregar"), 
                      t("#altEditor-modal").find(".modal-body").html(d), 
                      t("#altEditor-modal").find(".modal-footer").html("<button type='button' data-content='remove' class='btn btn-default' data-dismiss='modal' id='closeRowBtn'>Cerrar</button><button type='button' data-content='remove' class='btn btn-primary' id='addRowBtn'>Agregar</button>")
                    }), t("#altEditor-modal").modal("show"), t("#altEditor-modal input.primarykey+div input").focus()
                    
                },
                error:function(){}
            });
        },
        _addRowData: function() {
            var o = this,
                a = (this.s.dt, {}),
                e = t('form[name="altEditor-form"] input.primarykey').attr("name");
            t('form[name="altEditor-form"] input, select').each(function(o) {
                a[t(this).attr("id")] = t(this).val()
            }), t("#altEditor-modal .modal-body .alert").remove(), t(o.s.dt.context[0].nTable).trigger("crudaction", ["add", e, a, null])
        },
        _getExecutionLocationFolder: function() {
            var o = "dataTables.altEditor.js",
                a = t("script[src]"),
                e = t.grep(a, function(t) {
                    return -1 !== t.src.indexOf(o) ? t : void 0
                }),
                d = e[0].src,
                l = d.substring(0, d.lastIndexOf("/") + 1);
            return l
        }
    }), i.version = "1.0", i.defaults = {
        alwaysAsk: !1,
        focus: null,
        columns: "",
        update: null,
        editor: null
    }, i.classes = {
        btn: "btn"
    }, t(a).on("preInit.dt.altEditor", function(o, a, e) {
        if ("dt" === o.namespace) {
            var l = a.oInit.altEditor,
                n = d.defaults.altEditor;
            if (l || n) {
                var r = t.extend({}, l, n);
                l !== !1 && new i(a, r)
            }
        }
    }), d.altEditor = i, i
});
