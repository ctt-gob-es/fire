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
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.tool.Base64;


/**
 * Servlet implementation class NewUserService
 */
@WebServlet("/newUser")
public class NewUserService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(NewUserService.class.getName());

	private static final String PARAM_IDUSER = "idUser";//$NON-NLS-1$
	private static final String PARAM_LOGNAME = "login-usr"; //$NON-NLS-1$
	private static final String PARAM_PASSWD = "passwd-usr"; //$NON-NLS-1$
	private static final String PARAM_USER_ROLE = "role-usr";//$NON-NLS-1$
	private static final String PARAM_OP = "op"; //$NON-NLS-1$
	private static final String PARAM_USERNAME = "usr-name";//$NON-NLS-1$
	private static final String PARAM_USERSURNAME = "usr-surname";//$NON-NLS-1$
	private static final String PARAM_USEREMAIL = "email";//$NON-NLS-1$
	private static final String PARAM_USERTELF = "telf-contact";//$NON-NLS-1$

	private static final String SHA_2 = "SHA-256"; //$NON-NLS-1$

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		// Obtener el tipo de operacion 1-Alta 2-Edicion
		final int op = Integer.parseInt(req.getParameter(PARAM_OP));
		final String stringOp = op == 1 ? "alta" : "edicion" ;  //$NON-NLS-1$//$NON-NLS-2$

		// Obtenemos los parametros enviados del formulario
		final Parameters params = getParameters(req);

		try {
			final MessageDigest md = MessageDigest.getInstance(SHA_2);
			byte[] digest;
			String clave = null;
			//Comprobar que se ha cargado el Certificado nuevo.
			//Obtenemos la clave codificada
			if (params.getPassword() != null) {
				md.update(params.getPassword().getBytes());
				digest = md.digest();
				clave = Base64.encode(digest);
			}

			boolean isOk = true;

			// nuevo usuario
			if (op == 1) {

				if (params.getLoginUser() == null || params.getPassword() == null ||
						params.getUserName() == null || params.getUserSurname() == null) {
					LOGGER.log(Level.SEVERE,
							"No se han proporcionado todos los datos requeridos para el alta del usuario (login, clave, rol, nombre y apellidos)"); //$NON-NLS-1$
					isOk = false;
				}
				else {
					//Comprobar que el login de usuario no existe anteriormente en la tabla de usuarios dado de alta
					final User usr = UsersDAO.getUserByName(params.getLoginUser());
					if(usr != null && usr.getNombre_usuario() != null && !"".equals(usr.getNombre_usuario()))//$NON-NLS-1$
					{
						LOGGER.log(Level.SEVERE,"Se ha proporcionado un nombre de login repetido, no se puede dar de alta el usuario"); //$NON-NLS-1$
						isOk = false;
					}
					else {
						LOGGER.info("Alta del usuario con nombre de login: " + params.getLoginUser()); //$NON-NLS-1$
						try {
							 UsersDAO.createUser(params.getLoginUser(), clave,
									 params.getUserName(), params.getUserSurname(),
									 params.getUserEMail(), params.getUserTelf());
						} catch (final Exception e) {
							LOGGER.log(Level.SEVERE, "Error en el alta del usuario", e); //$NON-NLS-1$
							isOk = false;
						}
					}

				}
			}
			else if(op == 2) {	//Edicion de usuario
				if (params.getIdUser()==null
						|| params.getUserName() == null || params.getUserSurname() == null) {
					LOGGER.log(Level.SEVERE,
							"No se han proporcionado todos los datos requeridos para la edicion del usuario (login, rol, nombre y apellidos)"); //$NON-NLS-1$
					isOk = false;
				}
				else {
					LOGGER.info("Edicion del usuario con nombre de login: " + params.getLoginUser()); //$NON-NLS-1$
					try {
						 UsersDAO.updateUser(params.getIdUser(), params.getUserName(),
								 params.getUserSurname(), params.getUserEMail(),
								 params.getUserTelf());
					} catch (final Exception e) {
						LOGGER.log(Level.SEVERE, "Error en la edici�n del usuario", e); //$NON-NLS-1$
						isOk = false;
					}
				}

			}
			else if(op == 3 && params.getLoginUser() != null ){
				// Comprobar que el login de usuario no existe anteriormente en la tabla de usuarios dado de alta
				final User usr = UsersDAO.getUserByName(params.getLoginUser());
				resp.setContentType("text/html");//$NON-NLS-1$
				if (usr != null && usr.getId_usuario() != null && !"".equals(usr.getId_usuario())) { //$NON-NLS-1$
					final String usrLogin = "El usuario con login, " + params.getLoginUser() + //$NON-NLS-1$
							", ya existe en el sistema."; //$NON-NLS-1$
					resp.getWriter().write(usrLogin);
				}
				else {
					resp.getWriter().write("new");//$NON-NLS-1$
				}
			}
			else {
				throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
			}

			if (op != 3) {
				resp.sendRedirect("User/UserPage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0")+"&ent=user"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		catch (final IllegalArgumentException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error con el base64 : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("./User/NewUser.jsp?error=true&name=" + params.getLoginUser() ); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error crear el usuario : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("./User/NewUser.jsp?op=1&r=0&ent=user"); //$NON-NLS-1$
		}

	}




	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}



	/**
	 * Procedimiento que obtiene los par&aacute;metros enviados al servicio.
	 * @param req Petici&ioacute;n HTTP realizada al servicio.
	 */
	private static Parameters getParameters(final HttpServletRequest req)  {

		final Parameters params = new Parameters();

		if(req.getParameter(PARAM_IDUSER) != null && !"".equals(req.getParameter(PARAM_IDUSER))) {//$NON-NLS-1$
			params.setIdUser(req.getParameter(PARAM_IDUSER));
		}
		if(req.getParameter(PARAM_LOGNAME) != null && !"".equals(req.getParameter(PARAM_LOGNAME))) {//$NON-NLS-1$
			params.setLoginUser(req.getParameter(PARAM_LOGNAME));
		}
		if(req.getParameter(PARAM_PASSWD) != null && !"".equals(req.getParameter(PARAM_PASSWD))) {//$NON-NLS-1$
			params.setPassword(req.getParameter(PARAM_PASSWD));
		}
		if(req.getParameter(PARAM_USER_ROLE) != null && !"".equals(req.getParameter(PARAM_USER_ROLE))) {//$NON-NLS-1$
			params.setUserRole(req.getParameter(PARAM_USER_ROLE));
		}
		if(req.getParameter(PARAM_USERNAME) != null && !"".equals(req.getParameter(PARAM_USERNAME))) {//$NON-NLS-1$
			params.setUserName(req.getParameter(PARAM_USERNAME));
		}
		if(req.getParameter(PARAM_USERSURNAME) != null && !"".equals(req.getParameter(PARAM_USERSURNAME))) {//$NON-NLS-1$
			params.setUserSurname(req.getParameter(PARAM_USERSURNAME));
		}
		if(req.getParameter(PARAM_USEREMAIL) != null && !"".equals(req.getParameter(PARAM_USEREMAIL))) {//$NON-NLS-1$
			params.setUserEMail(req.getParameter(PARAM_USEREMAIL));
		}
		if(req.getParameter(PARAM_USERTELF) != null && !"".equals(req.getParameter(PARAM_USERTELF))) {//$NON-NLS-1$
			params.setUserTelf(req.getParameter(PARAM_USERTELF));
		}
		return params;
	}


	/*GETTER Y SETTER*/

	static class Parameters {

		private String idUser = null;
		private String loginUser = null;
		private String password = null;
		private String userRole = null;
		private String userName = null;
		private String userSurname = null;
		private String userEMail = null;
		private String userTelf = null;

		/**
		 * Obtiene el login (nombre con el que se accede a la aplicaci&oacute;n) del usuario
		 * @return
		 */
		final String getLoginUser() {
			return this.loginUser;
		}
		/**
		 * Establece el login (nombre con el que se accede a la aplicaci&oacute;n) del usuario.
		 * @param loginUser Nombre de usuario;
		 */
		final void setLoginUser(final String loginUser) {
			this.loginUser = loginUser;
		}
		/**
		 * Obtiene la clave (Password)
		 * @return
		 */
		final String getPassword() {
			return this.password;
		}
		/**
		 * Establece la clave (Password)
		 * @param password Clave de usuario.
		 */
		final void setPassword(final String password) {
			this.password = password;
		}
		/**
		 * Obtiene el rol del usuario.
		 * @return Rol del usuario.
		 */
		final String getUserRole() {
			return this.userRole;
		}
		/**
		 * Establece el rol del usuario.
		 * @param userRole Rol del usuario.
		 */
		final void setUserRole(final String userRole) {
			this.userRole = userRole;
		}
		/**
		 * Obtiene el nombre del usuario
		 * @return
		 */
		final String getUserName() {
			return this.userName;
		}
		/**
		 * Establece el nombre del usuario
		 */
		final void setUserName(final String userName) {
			this.userName = userName;
		}
		/**
		 * Obtiene los apellidos del usuario
		 * @return
		 */
		final String getUserSurname() {
			return this.userSurname;
		}
		/**
		 * Establece los apellidos del usuario
		 */
		final void setUserSurname(final String userSurname) {
			this.userSurname = userSurname;
		}
		/**
		 * Obtiene los email del usuario
		 * @return
		 */
		final String getUserEMail() {
			return this.userEMail;
		}
		/**
		 * Establece los email del usuario
		 */
		final void setUserEMail(final String userEMail) {
			this.userEMail = userEMail;
		}
		/**
		 * Obtiene el tel&eacute;fono del usuario
		 * @return
		 */
		final String getUserTelf() {
			return this.userTelf;
		}
		/**
		 * Establece el tel&eacute;fono del usuario
		 */
		final void setUserTelf(final String userTelf) {
			this.userTelf = userTelf;
		}
		/**
		 * Obtiene el ID  del usuario
		 * @return
		 */
		final String getIdUser() {
			return this.idUser;
		}
		/**
		 * Establece el ID  del usuario
		 */
		final void setIdUser(final String idUser) {
			this.idUser = idUser;
		}
	}

}
