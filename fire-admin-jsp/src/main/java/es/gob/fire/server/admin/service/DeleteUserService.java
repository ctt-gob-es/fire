package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

		final String idUser = request.getParameter(PARAM_ID);
		final String username = request.getParameter(PARAM_USRNAME);
		final HttpSession session = request.getSession();
		final String loggedUsr = (String) session.getAttribute(ServiceParams.SESSION_ATTR_USER);

		LOGGER.info("Baja del usuario con ID: " + idUser + " Nombre:" + username); //$NON-NLS-1$ //$NON-NLS-2$

		boolean isOk = true;
		if (idUser == null || "".equals(idUser)) { //$NON-NLS-1$
			isOk = false;
		}
		else {
			try {
				final User usr= UsersDAO.getUser(idUser);
				if(usr != null && usr.getPorDefecto() != null && Integer.parseInt(usr.getPorDefecto()) != 1) {
					UsersDAO.removeUser(idUser,username);
				}
				else {
					LOGGER.info("Se ha intentado dar de baja al usuario por defecto con ID: " + idUser + " Nombre:" + username); //$NON-NLS-1$ //$NON-NLS-2$
					isOk = false;
				}
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error al dar de baja el usuario con ID:" + idUser + " Nombre:" + username , e); //$NON-NLS-1$ //$NON-NLS-2$
				isOk = false;
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
