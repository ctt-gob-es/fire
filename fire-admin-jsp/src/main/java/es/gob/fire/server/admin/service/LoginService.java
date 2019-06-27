package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.message.UserMessages;
/**
 *
 * servicios para el acceso de usuarios a la aplicacion
 */
public class LoginService extends HttpServlet {

	/** Serial Id. */
	private static final long serialVersionUID = -1102186174811586133L;

	private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

		final String username = req.getParameter(ServiceParams.PARAM_USERNAME);
		final String password = req.getParameter(ServiceParams.PARAM_PASSWORD);

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
        	resp.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_LOGIN_ACCESS.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
        	return;
        }

        User user;
		try {
			user = UsersDAO.getUserByName(username);

		} catch (final SQLException e) {
        	LOGGER.log(Level.WARNING, "No se pudo encontrar el usuario", e); //$NON-NLS-1$
        	resp.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_LOGIN_ACCESS.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}



        if (user == null) {
        	LOGGER.warning("El usuario tiene que tener un login"); //$NON-NLS-1$
        	resp.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_LOGIN_ACCESS.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
        }

        final boolean logginPermission = user.getPermissions().hasLoginPermission();

        if (!logginPermission) {
        	LOGGER.log(Level.WARNING, "El usuario " + LogUtils.cleanText(username) + " no tiene permisos de acceso"); //$NON-NLS-1$ //$NON-NLS-2$
        	resp.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_LOGIN_ACCESS.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
        }

		boolean logged = false;
		try {
			logged = UsersDAO.checkAdminPassword(password, username);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No es posible autenticar al usuario", e); //$NON-NLS-1$
			logged = false;
		}

		if (!logged) {
        	resp.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_LOGIN_ACCESS.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		final HttpSession session = req.getSession();
		session.setAttribute(ServiceParams.SESSION_ATTR_INITIALIZED, "true"); //$NON-NLS-1$
		session.setAttribute(ServiceParams.SESSION_ATTR_USER, username);

		resp.sendRedirect("Application/AdminMainPage.jsp"); //$NON-NLS-1$
	}
}
