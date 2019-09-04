package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.AplicationsDAO;
import es.gob.fire.server.admin.dao.RolesDAO;
import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;

/**
 * Servlet implementation class DeleteUserService
 */
public class DeleteUserService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(DeleteUserService.class.getName());

	private static final String PARAM_ID = "id-usr"; //$NON-NLS-1$
	private static final String PARAM_USRNAME = "usr-name"; //$NON-NLS-1$
	private static final String PARAM_USER_ROLE = "role-usr";//$NON-NLS-1$


    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteUserService() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		final HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		request.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		final String idUser = request.getParameter(PARAM_ID);
		final String username = request.getParameter(PARAM_USRNAME);
	//	final HttpSession session = request.getSession();
		final String loggedUsr = (String) session.getAttribute(ServiceParams.SESSION_ATTR_USER);
		 String msg = ""; //$NON-NLS-1$


		LOGGER.info("Baja del usuario con ID: " + LogUtils.cleanText(idUser) + " Nombre: " 	+ LogUtils.cleanText(username)); //$NON-NLS-1$ //$NON-NLS-2$

		boolean isOk = true;

		if (idUser == null || idUser.isEmpty()) {
			isOk = false;
			LOGGER.warning("No se ha indicado al usuario al que dar de baja o se ha intentado dar de baja al usuario superadministrador"); //$NON-NLS-1$
		}
		else {
			try {

				final User usr = UsersDAO.getUser(idUser);
				if (usr == null || usr.isRoot()) {
					isOk = false;
					LOGGER.warning("No se ha encontrado al usuario o se ha intentado dar de baja al usuario superadministrador"); //$NON-NLS-1$

				}else {

					final RolePermissions permissions = RolesDAO.getPermissions(usr.getRole());
					if (permissions == null || !permissions.hasAppResponsable()) {
						UsersDAO.removeUser(idUser, username);
					}
					else {
						/*Comprobamos si tiene asociadas aplicaciones al usuario que se quiere eliminar*/

						final String countJson = AplicationsDAO.getApplicationsCountByUsersJSON(idUser);

						JsonObject jsonObj;
						try (final JsonReader jsonReader = Json.createReader(new StringReader(countJson));) {
							jsonObj = jsonReader.readObject();
						}
						final int total= jsonObj.getInt("count"); //$NON-NLS-1$

						if (total <= 0) {
							UsersDAO.removeUser(idUser,username );

						} else {
							isOk = false;
							LOGGER.log(Level.WARNING, "Error al dar de baja el usuario, tiene asociadas aplicaciones"); //$NON-NLS-1$
							msg = "Error al dar de baja el usuario, tiene asociadas aplicaciones"; //$NON-NLS-1$

						}

					}
				}
			}
			catch (final Exception e) {
				isOk = false;
				LOGGER.log(Level.SEVERE, "Error al dar de baja el usuario con ID:" + LogUtils.cleanText(idUser) + " Nombre:" + LogUtils.cleanText(username) , e); //$NON-NLS-1$ //$NON-NLS-2$

			}
		}

		// Comprobar que el usuario que se borra es el mismo que esta autenticado,
		// en ese caso se cierra la sesion redirigiendo a la pagina de Login.jsp
		if(isOk && loggedUsr != null && !"".equals(loggedUsr) && loggedUsr.equals(username)){ //$NON-NLS-1$
			response.sendRedirect("Login.jsp?"); //$NON-NLS-1$
		}
		else {
			response.sendRedirect("User/UserPage.jsp?op=baja&r=" + (isOk ? "1" : "0") + "&ent=user"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}



	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
