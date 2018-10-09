package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;

/**
 * Servlet implementation class DeleteUserService
 */
@WebServlet("/deleteUsr")
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
		final String user_name = request.getParameter(PARAM_USRNAME);
		final HttpSession session = request.getSession();
		final String loggedUsr = (String) session.getAttribute(ServiceParams.SESSION_ATTR_USER);

		LOGGER.info("Baja del usuario con ID: " + idUser + " Nombre:" + user_name); //$NON-NLS-1$ //$NON-NLS-2$

		boolean isOk = true;
		if (idUser == null || "".equals(idUser)) { //$NON-NLS-1$
			isOk = false;
		}
		else {
			try {
				final User usr= UsersDAO.getUser(idUser);
				if(usr!=null && usr.getUsu_defecto()!=null && Integer.parseInt(usr.getUsu_defecto())!=1) {
					UsersDAO.removeUser(idUser,user_name);
				}
				else {
					LOGGER.info("Se ha intentado dar de baja al usuario por defecto con ID: " + idUser + " Nombre:" + user_name); //$NON-NLS-1$ //$NON-NLS-2$
					isOk = false;
				}
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, "Error al dar de baja el usuario con ID:" + idUser + " Nombre:" + user_name , e); //$NON-NLS-1$ //$NON-NLS-2$
				isOk = false;
			}
		}
		//Comprobar que el usuario que se borra es el mismo que est� logado,
		//en ese caso se cierra la sesi�n redirigiendo a la p�gina de Login.jsp
		if(isOk && loggedUsr != null && !"".equals(loggedUsr) && loggedUsr.equals(user_name)){ //$NON-NLS-1$
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
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
