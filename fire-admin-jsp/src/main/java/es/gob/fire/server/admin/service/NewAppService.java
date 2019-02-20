/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.admin.dao.AplicationsDAO;

/**
 * Servicio para el alta de una nueva aplicaci&oacute;n en el sistema.
 */
public class NewAppService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -6304364862591344482L;

	private static final Logger LOGGER = Logger.getLogger(NewAppService.class.getName());

	private static final String PARAM_NAME = "nombre-app"; //$NON-NLS-1$
	private static final String PARAM_RESP = "nombre-resp"; //$NON-NLS-1$
	private static final String PARAM_EMAIL = "email-resp"; //$NON-NLS-1$
	private static final String PARAM_TEL = "telf-resp"; //$NON-NLS-1$
	private static final String PARAM_CERTID = "id-certificate"; //$NON-NLS-1$
	private static final String PARAM_OP = "op"; //$NON-NLS-1$

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		// Obtenemos el tipo de operacion 1-Alta 2-Edicion
		int op;
		try {
			op = Integer.parseInt(req.getParameter(PARAM_OP));
			if (op != 1 && op != 2) {
				throw new IllegalArgumentException();
			}
		} catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Se ha proporcionado un identificador de operacion no soportado: " + req.getParameter(PARAM_OP)); //$NON-NLS-1$
			resp.sendRedirect("Application/AdminMainPage.jsp?op=alta&r=0&ent=app"); //$NON-NLS-1$
			return;
		}

		final String stringOp = op == 1 ? "alta" : "edicion" ;  //$NON-NLS-1$//$NON-NLS-2$;

		// Obtenemos los parametros enviados del formulario junto con el Certificado
		final Parameters params = getParameters(req);

		if (params.getName() == null || params.getRes() == null) {
			LOGGER.log(Level.SEVERE,
					"No se han proporcionado todos los datos requeridos para la aplicacion (nombre y responsable)"); //$NON-NLS-1$
			resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=0&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Nueva aplicacion
		if (op == 1) {
			LOGGER.info("Alta de la aplicacion con nombre: " + params.getName()); //$NON-NLS-1$
			try {
				AplicationsDAO.createApplication(params.getName(), params.getRes(), params.getEmail(), params.getTel(), params.getIdcertificate());
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en el alta de la aplicacion", e); //$NON-NLS-1$
				resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=0&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		// Editar aplicacion
		else if (op == 2) {
			LOGGER.info("Edicion de la aplicacion con nombre: " + params.getName()); //$NON-NLS-1$
			try {
				AplicationsDAO.updateApplication(req.getParameter("iddApp"), params.getName(), params.getRes(), params.getEmail(), params.getTel(), params.getIdcertificate());//$NON-NLS-1$
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en la actualizacion de la aplicacion", e); //$NON-NLS-1$
				resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=0&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}

		resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=1&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Procedimiento que obtine los datos de los par&aacute;metros reconocidos del
	 * formulario para a&ntilde;adir aplicaci&oacute;n.
	 * Par&aacute;metros: nombre-app, nombre-resp,email-resp, telf-resp y id-certificate.
	 * @param req Petici&oacute;n HTTP.
	 */
	private Parameters getParameters(final HttpServletRequest req) {

		final Parameters params = new Parameters();

		if(req.getParameter(PARAM_NAME) != null && !"".equals(req.getParameter(PARAM_NAME))) { //$NON-NLS-1$
			params.setName(req.getParameter(PARAM_NAME));
		}
		if(req.getParameter(PARAM_RESP) != null && !"".equals(req.getParameter(PARAM_RESP))) {//$NON-NLS-1$
			params.setRes(req.getParameter(PARAM_RESP));
		}
		if(req.getParameter(PARAM_EMAIL) != null && !"".equals(req.getParameter(PARAM_EMAIL))) {//$NON-NLS-1$
			params.setEmail(req.getParameter(PARAM_EMAIL));
		}
		if(req.getParameter(PARAM_TEL) != null && !"".equals(req.getParameter(PARAM_TEL))) {//$NON-NLS-1$
			params.setTel(req.getParameter(PARAM_TEL));
		}
		if(req.getParameter(PARAM_CERTID) != null && !"".equals(req.getParameter(PARAM_CERTID))) {//$NON-NLS-1$
			params.setIdcertificate(req.getParameter(PARAM_CERTID));
		}
		return params;
	}

	class Parameters {

		private String name = null;
		private String res = null;
		private String email = null;
		private String tel = null;
		private String idcertificate = null;

		/**
		 * Obtiene el nombre de la aplicaci&ácute;n
		 * @return
		 */
		String getName() {
			return this.name;
		}
		/**
		 *  Establece el nombre de la aplicaci&ácute;n
		 * @param name
		 */
		void setName(final String name) {
			this.name = name;
		}

		/**
		 * Obtiene el nombre del responsable
		 * @return
		 */
		String getRes() {
			return this.res;
		}
		/**
		 *  Establece el nombre del responsable
		 * @param res
		 */
		void setRes(final String res) {
			this.res = res;
		}
		/**
		 * Obtiene el e-mail del responsable
		 * @return
		 */
		String getEmail() {
			return this.email;
		}
		/**
		 * Establece el e-mail del responsable
		 * @param email
		 */
		void setEmail(final String email) {
			this.email = email;
		}
		/**
		 * Obtiene el tel&eacute;fono del responsable
		 * @return
		 */
		String getTel() {
			return this.tel;
		}
		/**
		 * Establece el tel&eacute;fono del responsable
		 * @param tel
		 */
		void setTel(final String tel) {
			this.tel = tel;
		}
		/**
		 * Obtiene el id del certificado
		 * @return
		 */
		final String getIdcertificate() {
			return this.idcertificate;
		}
		/**
		 * Establece el id del certificado
		 * @param idcertificate
		 */
		final void setIdcertificate(final String idcertificate) {
			this.idcertificate = idcertificate;
		}
	}
}
