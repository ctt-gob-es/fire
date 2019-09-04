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
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;

/**
 * Servicio para el alta de una nueva aplicaci&oacute;n en el sistema.
 */
public class ResponsibleRefreshService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -6304364862591344482L;

	private static final Logger LOGGER = Logger.getLogger(ResponsibleRefreshService.class.getName());

	private static final String PARAM_ID = "id"; //$NON-NLS-1$

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		final HttpSession session = req.getSession(false);
		if (session == null) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		// Identificador del responsable
		JsonObject result;
		final String id = req.getParameter(PARAM_ID);
		if (id == null) {
			LOGGER.log(Level.WARNING, "No se ha proporcionado un identificador de operacion"); //$NON-NLS-1$
			final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
					.add("error"," Id no valido"); //$NON-NLS-1$ //$NON-NLS-2$

			result = jsonObjectBuilder.build();
		}
		else {
			final User user = UsersDAO.getUser(id);

			final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
					.add("mail", user.getMail()); //$NON-NLS-1$

			if (user.getTelephone() != null) {
				jsonObjectBuilder.add("telephone", user.getTelephone()); //$NON-NLS-1$
			}

			result = jsonObjectBuilder.build();
		}

		final StringWriter writer = new StringWriter();
		try (JsonWriter jsonWriter = Json.createWriter(writer)) {
			jsonWriter.writeObject(result);
		}

		resp.getWriter().print( writer.toString() );
}

	/**
	 * Procedimiento que obtine los datos de los par&aacute;metros reconocidos del
	 * formulario para a&ntilde;adir aplicaci&oacute;n.
	 * Par&aacute;metros: nombre-app, nombre-resp,email-resp, telf-resp y id-certificate.
	 * @param req Petici&oacute;n HTTP.
	 */
	private Parameters getParameters(final HttpServletRequest req) {

		final Parameters params = new Parameters();

		if(req.getParameter(PARAM_ID) != null && !"".equals(req.getParameter(PARAM_ID))) { //$NON-NLS-1$
			params.setId(req.getParameter(PARAM_ID));
		}


		return params;
	}

	class Parameters {

		private String id = null;


		/**
		 * Obtiene el id de un certificado
		 * @return
		 */
		String getId() {
			return this.id;
		}
		/**
		 *  Establece el id de un certificado
		 * @param name
		 */
		void setId(final String id) {
			this.id = id;
		}


	}
}
