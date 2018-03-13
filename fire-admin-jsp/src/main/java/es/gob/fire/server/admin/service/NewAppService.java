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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.admin.dao.AplicationsDAO;

/**
 * Servicio para el alta de una nueva aplicaci&oacute;n en el sistema.
 */
@WebServlet("/newApp")
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


	private String name=null;
	private String res = null;
	private String email = null;
	private String tel = null;
	private String idcertificate=null;


	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		/*Obtenemos los par�metros enviados del formulario junto con el Certificado*/
		this.getParameters(req);

		//Obtener el tipo de operaci�n 1-Alta 2-Edici�n
		final int op = Integer.parseInt(req.getParameter(PARAM_OP));
		final String stringOp = op == 1 ? "alta" : "edicion" ;  //$NON-NLS-1$//$NON-NLS-2$

		try {

			boolean isOk = true;
			if (this.getName() == null || this.getRes() == null) {
				LOGGER.log(Level.SEVERE,
						"No se han proporcionado todos los datos requeridos para el alta de la aplicacion (nombre y responsable)"); //$NON-NLS-1$
				isOk = false;
			} else {
				// nueva aplicacion
				if (op == 1){
				LOGGER.info("Alta de la aplicacion con nombre: " + this.getName()); //$NON-NLS-1$
					try {
						AplicationsDAO.createApplication(this.getName(), this.getRes(), this.getEmail(), this.getTel(), this.getIdcertificate());
					} catch (final Exception e) {
						LOGGER.log(Level.SEVERE, "Error en el alta de la aplicacion", e); //$NON-NLS-1$
						isOk = false;
					}
				}
				// editar aplicacion
				else if (op == 2){
					LOGGER.info("Edicion de la aplicacion con nombre: " + this.getName()); //$NON-NLS-1$

					AplicationsDAO.updateApplication(req.getParameter("iddApp"), this.getName(), this.getRes(), this.getEmail(), this.getTel(), this.getIdcertificate());//$NON-NLS-1$
				}

				else{
					throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
				}

			}

			resp.sendRedirect("Application/AdminMainPage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0")+"&ent=app"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error crear la aplicacion : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("Application/AdminMainPage.jsp?op=alta&r=0&ent=app"); //$NON-NLS-1$
		}



	}

	/**
	 * Procedimiento que obtine los datos de los par�metros enviados desde el formulario para a�adir aplicaci�n.
	 * Par�metros: nombre-app, nombre-resp,email-resp, telf-resp y
	 * @param req
	 * @throws IOException
	 * @throws ServletException
	 */
	private void getParameters(final HttpServletRequest req) throws IOException, ServletException {
		this.setName(null);
		this.setRes(null);
		this.setEmail(null);
		this.setTel(null);
		this.setIdcertificate(null);

		if(req.getParameter(PARAM_NAME)!=null && !"".equals(req.getParameter(PARAM_NAME))) { //$NON-NLS-1$
			this.setName(req.getParameter(PARAM_NAME));
		}
		if(req.getParameter(PARAM_RESP)!=null && !"".equals(req.getParameter(PARAM_RESP))) {//$NON-NLS-1$
			this.setRes(req.getParameter(PARAM_RESP));
		}
		if(req.getParameter(PARAM_EMAIL)!=null && !"".equals(req.getParameter(PARAM_EMAIL))) {//$NON-NLS-1$
			this.setEmail(req.getParameter(PARAM_EMAIL));
		}
		if(req.getParameter(PARAM_TEL)!=null && !"".equals(req.getParameter(PARAM_TEL))) {//$NON-NLS-1$
			this.setTel(req.getParameter(PARAM_TEL));
		}
		if(req.getParameter(PARAM_CERTID)!=null && !"".equals(req.getParameter(PARAM_CERTID))) {//$NON-NLS-1$
			this.setIdcertificate(req.getParameter(PARAM_CERTID));
		}

	}

	// Getters and Setters
	private String getName() {
		return this.name;
	}

	private void setName(final String name) {
		this.name = name;
	}

	private String getRes() {
		return this.res;
	}

	private void setRes(final String res) {
		this.res = res;
	}

	private String getEmail() {
		return this.email;
	}

	private void setEmail(final String email) {
		this.email = email;
	}

	private String getTel() {
		return this.tel;
	}

	private void setTel(final String tel) {
		this.tel = tel;
	}

	private final String getIdcertificate() {
		return this.idcertificate;
	}

	private final void setIdcertificate(final String idcertificate) {
		this.idcertificate = idcertificate;
	}




}
