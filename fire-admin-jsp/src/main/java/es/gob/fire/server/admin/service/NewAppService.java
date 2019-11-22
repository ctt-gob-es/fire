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
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.AplicationsDAO;

/**
 * Servicio para el alta de una nueva aplicaci&oacute;n en el sistema.
 */
public class NewAppService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -6304364862591344482L;

	private static final Logger LOGGER = Logger.getLogger(NewAppService.class.getName());


	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		final HttpSession session = req.getSession(false);
		if (session == null) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		// Obtenemos los parametros enviados del formulario junto con el Certificado
		final Parameters params = getParameters(req);

		// Obtenemos el tipo de operacion 1-Alta 2-Edicion

		int op;
		try {
			op = params.getOp();
			if (op != 1 && op != 2) {
				throw new IllegalArgumentException("Operacion no soportada: " + op); //$NON-NLS-1$
			}
		} catch (final Exception e) {
			LOGGER.log(Level.WARNING,
					"Se ha proporcionado un identificador de operacion no soportado: " + ServiceParams.PARAM_OP); //$NON-NLS-1$
			resp.sendRedirect("Application/AdminMainPage.jsp?op=alta&r=0&ent=app"); //$NON-NLS-1$
			return;
		}

		final String stringOp = op == 1 ? "alta" : "edicion"; //$NON-NLS-1$//$NON-NLS-2$ ;

		if (params.getName() == null || params.getResponsables() == null) {
			LOGGER.log(Level.SEVERE,
					"No se han proporcionado todos los datos requeridos para la aplicacion (nombre y responsable)"); //$NON-NLS-1$
			resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=0&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// Nueva aplicacion
		if (op == 1) {
			LOGGER.info("Alta de la aplicacion con nombre: " + params.getName()); //$NON-NLS-1$
			String idAplicacion;
			try {
				idAplicacion = AplicationsDAO.createApplication(params.getName(), params.getIdcertificate(),
						params.isHabilitado());
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en el alta de la aplicacion", e); //$NON-NLS-1$
				resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=0&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}

			try {
				AplicationsDAO.createApplicationResponsable(idAplicacion, params.getResponsables());
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en el alta de la aplicacion", e); //$NON-NLS-1$
				resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=0&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}

		// Editar aplicacion
		else if (op == 2) {
			LOGGER.info("Edicion de la aplicacion con ID: " + params.getId()); //$NON-NLS-1$
			try {
				AplicationsDAO.updateApplication(
						params.getId(),
						params.getName(),
						params.getIdcertificate(),
						params.isHabilitado());
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error en la actualizacion de la aplicacion", e); //$NON-NLS-1$
				resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=0&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}

			// Para actualizar la tabla de relaciones entre aplicaciones y sus responsables,
			// eliminaremos todas las relaciones existentes de esta aplicacion y las crearemos
			// con el nuevo listado de responsables
			try {
				AplicationsDAO.updateApplicationResponsables(params.getId(), params.getResponsables());
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error al actualizar el listado de responsables de la aplicacion: " + params.getId(), e); //$NON-NLS-1$
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

		final String id = req.getParameter(ServiceParams.PARAM_APPID);
		final String op = req.getParameter(ServiceParams.PARAM_OP);
		final String nombreApp = req.getParameter(ServiceParams.PARAM_NAME);

		final String disabledParam = req.getParameter(ServiceParams.PARAM_ENABLED);
		final boolean enabled = disabledParam == null;

//		System.out.println(" ==== LISTA DE PARAMETROS:");
//		final Enumeration<String> paramNames = req.getParameterNames();
//		while (paramNames.hasMoreElements()) {
//			System.out.println(paramNames.nextElement());
//		}
//		System.out.println(" =========================");


		final String[] nombreResp = req.getParameterValues(ServiceParams.PARAM_RESPONSABLES);

		final String idCertificate = req.getParameter(ServiceParams.PARAM_CERTID);
		final String mail = req.getParameter(ServiceParams.PARAM_MAIL);
		final String telf = req.getParameter(ServiceParams.PARAM_TEL);
		final Parameters params = new Parameters();

		try {
			params.setOp(Integer.parseInt(op));
		}catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se han encontrado el parametro correspondido", e); //$NON-NLS-1$
			return params;

		}
		if (id != null && !id.isEmpty()) {
			params.setId(id);
		}

		if (nombreApp != null && !nombreApp.isEmpty()) {
			params.setName(nombreApp);
		}
		if (nombreResp != null && nombreResp.length > 0) {
			params.setResponsables(nombreResp);
		}
		if (idCertificate != null && !idCertificate.isEmpty()) {
			params.setIdcertificate(idCertificate);
		}
		if (mail != null && !mail.isEmpty()) {
			params.setEmail(mail);
		}
		if (telf != null && !telf.isEmpty()) {
			params.setTel(telf);
		}
		params.setHabilitado(enabled);


		return params;
	}

	class Parameters {

		private String id;
		private String name;
		private String[] responsables;
		private String email;
		private String tel;
		private String idcertificate;
		private boolean habilitado;
		private int op;


		public String getId() {
			return this.id;
		}
		public void setId(final String id) {
			this.id = id;
		}
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
		 * Recupera el nombre del responsable de la aplicaci&oacute;n.
		 * @return Nombre del responsable de la aplicaci&oacute;n.
		 */
		public String[] getResponsables() {
			return this.responsables;
		}


		/**
		 * Establece el nombre del responsable de la aplicaci&oacute;n.
		 * @param responsable Nombre del responsable de la aplicaci&oacute;n.
		 */
		public void setResponsables(final String[] responsables) {
			this.responsables = responsables;
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
		 * @return idcertificate
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

		public boolean isHabilitado() {
			return this.habilitado ;
		}

		public void setHabilitado(final boolean habilitado) {
			this.habilitado = habilitado;
		}

		public int getOp() {
			return this.op;
		}
		public void setOp(final int op) {
			this.op = op;
		}

	}
}
