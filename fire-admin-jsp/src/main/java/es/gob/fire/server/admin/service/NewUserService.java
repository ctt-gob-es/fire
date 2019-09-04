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
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.RolesDAO;
import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.tool.Base64;


/**
 * Servlet implementation class NewUserService
 */
public class NewUserService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(NewUserService.class.getName());







	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

		final HttpSession session = req.getSession(false);
		if (session == null) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		req.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		// Obtener el tipo de operacion 1-Alta 2-Edicion
		int op;
		final String idUser = req.getParameter(ServiceParams.PARAM_IDUSER);
		final String username = req.getParameter(ServiceParams.PARAM_USERNAME);
		op = Integer.parseInt(req.getParameter(ServiceParams.PARAM_OP_USER));
		final String stringOp = op == 1 ? "alta" : "edicion" ;  //$NON-NLS-1$//$NON-NLS-2$
		final String stringOp4 = op == 4 ? "baja" : "edicion" ;  //$NON-NLS-1$//$NON-NLS-2$
		 final String msg = ""; //$NON-NLS-1$


		// Obtenemos los parametros enviados del formulario
		final Parameters params = getParameters(req);

		try {
			boolean isOk = true;

			// nuevo usuario
			if (op == 1) {
				final RolePermissions permissions = RolesDAO.getPermissions(Integer.parseInt(params.getUserRole()));


				if (params.getLoginUser() == null || params.getUserRole() == null ||
						params.getUserName() == null || params.getUserSurname() == null) {
					LOGGER.log(Level.SEVERE,
							"No se han proporcionado todos los datos requeridos para el alta del usuario (login, rol, nombre y apellidos)"); //$NON-NLS-1$
					isOk = false;
				}
				else if (permissions.hasLoginPermission() && params.getPassword() == null) {
					LOGGER.log(Level.SEVERE,
							"Un usuario con permisos de acceso tiene que establecer una contrasena"); //$NON-NLS-1$
					isOk = false;
				}
				else {

					// Codificamos la clave
					String clave = null;
					if (params.getPassword() != null) {
						final MessageDigest md = MessageDigest.getInstance(ServiceParams.SHA_2);
						md.update(params.getPassword().getBytes());
						final byte[] digest = md.digest();
						clave = Base64.encode(digest);
					}


					// Comprobar que el login de usuario no existe anteriormente en la tabla de usuarios dado de alta
					final User usr = UsersDAO.getUserByName(params.getLoginUser());
					if (usr != null && usr.getUserName() != null && !"".equals(usr.getUserName()))//$NON-NLS-1$
					{
						LOGGER.log(Level.SEVERE, "Se ha proporcionado un nombre de login repetido, no se puede dar de alta el usuario"); //$NON-NLS-1$
						isOk = false;
					}
					else {
						LOGGER.info("Alta del usuario con nombre de login: " + params.getLoginUser()); //$NON-NLS-1$
						try {
							UsersDAO.createUser(params.getLoginUser(), clave,
									params.getUserName(), params.getUserSurname(),
									params.getUserEMail(), params.getUserTelf(), params.getUserRole());
						} catch (final SQLIntegrityConstraintViolationException e) {
							LOGGER.log(Level.SEVERE, "Se ha detectado un error de duplicidad en base de datos: " + e); //$NON-NLS-1$
							isOk = false;

							final User mail = UsersDAO.getUserByMail(params.getUserEMail());
							if (mail != null && mail.getMail() != null && !"".equals(mail.getMail()))//$NON-NLS-1$
							{
								LOGGER.log(Level.SEVERE, "Se ha proporcionado un dirección de correo repetida, no se puede dar de alta el usuario"); //$NON-NLS-1$
								isOk = false;

							}


						}
						catch (final Exception e) {
							LOGGER.log(Level.SEVERE, "Error en el alta del usuario", e); //$NON-NLS-1$
							isOk = false;
						}
					}


					if (isOk) {
						LOGGER.info("Alta del usuario con mail de login: " + params.getUserEMail()); //$NON-NLS-1$
					}

				}

			}

			else if(op == 2) {	//Edicion de usuario

				final User usr = UsersDAO.getUser(idUser);

				final RolePermissions permissions = RolesDAO.getPermissions(usr.getRole());

				if (params.getIdUser()==null || params.getUserName() == null || params.getUserSurname() == null ||
						params.getUserRole() == null) {
					LOGGER.log(Level.SEVERE,
							"No se han proporcionado todos los datos requeridos para la edicion del usuario (login, rol, nombre y apellidos)"); //$NON-NLS-1$
					isOk = false;
				}
				else {
					LOGGER.info("Edicion del usuario con nombre de login: " + params.getLoginUser()); //$NON-NLS-1$
					try {
						if (permissions == null || !permissions.hasAppResponsable()) {
							UsersDAO.updateUser(params.getIdUser(), params.getUserName(),
									params.getUserSurname(), params.getUserEMail(),
									params.getUserTelf(), params.getUserRole());
						} else {

							UsersDAO.updateUser(params.getIdUser(), params.getUserName(),
									params.getUserSurname(), params.getUserEMail(),
									params.getUserTelf(), params.getUserRole());
						}
					}
					catch (final Exception e) {
						LOGGER.log(Level.SEVERE, "Error en la edicion del usuario", e); //$NON-NLS-1$
						isOk = false;
					}
				}
			}


			else if(op == 3 && params.getLoginUser() != null ){
				// Comprobar que el login de usuario no existe anteriormente en la tabla de usuarios dado de alta
				final User usr = UsersDAO.getUserByName(params.getLoginUser());
				resp.setContentType("text/html");//$NON-NLS-1$
				if (usr != null && usr.getId() != null && !"".equals(usr.getId())) { //$NON-NLS-1$
					final String usrLogin = "El usuario con login '" + params.getLoginUser() + //$NON-NLS-1$
							"' ya existe en el sistema."; //$NON-NLS-1$
					resp.getWriter().write(usrLogin);
				}
				else {
					resp.getWriter().write("new");//$NON-NLS-1$
				}
			}
			else if(op == 3 && params.getUserEMail() != null ){
				// Comprobar que el correo de usuario no existe anteriormente en la tabla de usuarios dado de alta
				final User mail = UsersDAO.getUserByMail(params.getUserEMail());
				resp.setContentType("text/html");//$NON-NLS-1$
				if (mail != null && mail.getId() != null && !"".equals(mail.getId())) { //$NON-NLS-1$
					final String usrMail = "El usuario con direccion de correo '" + params.getUserEMail() + //$NON-NLS-1$
							"' ya existe en el sistema."; //$NON-NLS-1$
					resp.getWriter().write(usrMail);
					return;
				}
				else {
					resp.getWriter().write("new");//$NON-NLS-1$
					return;
				}
			}

			else {
				throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
			}


			if (op != 3) {
				resp.sendRedirect("User/UserPage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0")+"&ent=user"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				return;
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

	if(req.getParameter(ServiceParams.PARAM_IDUSER) != null && !req.getParameter(ServiceParams.PARAM_IDUSER).isEmpty()) {
		params.setIdUser(req.getParameter(ServiceParams.PARAM_IDUSER));
	}
	if(req.getParameter(ServiceParams.PARAM_LOGNAME) != null && !req.getParameter(ServiceParams.PARAM_LOGNAME).isEmpty()) {
		params.setLoginUser(req.getParameter(ServiceParams.PARAM_LOGNAME));
	}
	if(req.getParameter(ServiceParams.PARAM_PASSWD) != null && !req.getParameter(ServiceParams.PARAM_PASSWD).isEmpty()) {
		params.setPassword(req.getParameter(ServiceParams.PARAM_PASSWD));
	}
	if(req.getParameter(ServiceParams.PARAM_USER_ROLE) != null && !"".equals(req.getParameter(ServiceParams.PARAM_USER_ROLE))) {//$NON-NLS-1$
		params.setUserRole(req.getParameter(ServiceParams.PARAM_USER_ROLE));
	}
	if(req.getParameter(ServiceParams.PARAM_USERNAME) != null && !req.getParameter(ServiceParams.PARAM_USERNAME).isEmpty()) {
		params.setUserName(req.getParameter(ServiceParams.PARAM_USERNAME));
	}
	if(req.getParameter(ServiceParams.PARAM_USERSURNAME) != null && !req.getParameter(ServiceParams.PARAM_USERSURNAME).isEmpty()) {
		params.setUserSurname(req.getParameter(ServiceParams.PARAM_USERSURNAME));
	}
	if(req.getParameter(ServiceParams.PARAM_USEREMAIL) != null && !req.getParameter(ServiceParams.PARAM_USEREMAIL).isEmpty()) {
		params.setUserEMail(req.getParameter(ServiceParams.PARAM_USEREMAIL));
	}
	if(req.getParameter(ServiceParams.PARAM_USERTELF) != null && !"".equals(req.getParameter(ServiceParams.PARAM_USERTELF))) {//$NON-NLS-1$
		params.setUserTelf(req.getParameter(ServiceParams.PARAM_USERTELF));

	}
	return params;
}

static class Parameters {

	private String idUser = null;
	private String loginUser = null;
	private String password = null;
	private String userRole = null;
	private String userName = null;
	private String userSurname = null;
	private String userEMail = null;
	private String userTelf = null;
	private final RolePermissions rolePermision = null;

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
	 * @return Clave.
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
	 * Obtiene el nombre de pila del usuario.
	 * @return Nombre de pila.
	 */
	final String getUserName() {
		return this.userName;
	}
	/**
	 * Establece el nombre de pila del usuario.
	 */
	final void setUserName(final String userName) {
		this.userName = userName;
	}
	/**
	 * Obtiene los apellidos del usuario
	 * @return Apellidos.
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
	 * Obtiene la direcci&oacute;n de correo electr&oacute;nico del usuario.
	 * @return Direcci&oacute;n de correo electr&oacute;nico.
	 */
	final String getUserEMail() {
		return this.userEMail;
	}
	/**
	 * Establece la direcci&oacute;n de correo electr&oacute;nico del usuario.
	 */
	final void setUserEMail(final String userEMail) {
		this.userEMail = userEMail;
	}
	/**
	 * Obtiene el tel&eacute;fono del usuario.
	 * @return Tel&eacute;fono.
	 */
	final String getUserTelf() {
		return this.userTelf;
	}
	/**
	 * Establece el tel&eacute;fono del usuario.
	 */
	final void setUserTelf(final String userTelf) {
		this.userTelf = userTelf;
	}
	/**
	 * Obtiene el ID  del usuario
	 * @return Identificado del usuario.
	 */
	final String getIdUser() {
		return this.idUser;
	}
	/**
	 * Establece el ID  del usuario.
	 */
	final void setIdUser(final String idUser) {
		this.idUser = idUser;
	}


}

}
