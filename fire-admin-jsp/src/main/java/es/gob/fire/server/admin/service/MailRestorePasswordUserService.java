package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.gob.fire.server.admin.conf.ConfigManager;
import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.message.UserMessages;

/**
 * Clase para dar una nueva contrase&ntilde;as de los usuarios que estan dados de alta
 * @author Carlos.J.Raboso
 *
 */

public class MailRestorePasswordUserService  extends HttpServlet{
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(MailRestorePasswordUserService.class.getName());



    /**
     * @see HttpServlet#HttpServlet()
     */
    public MailRestorePasswordUserService() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

		// Recuperar codigo y usuario
		final Parameters params = getParameters(req);

		if ( params.getCode() == null) {
			LOGGER.log(Level.WARNING, "El codigo no ha sido encontrado o es nulo"); //$NON-NLS-1$
			resp.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		UserRestorationInfo restorationInfo;
		try {
			restorationInfo = UsersDAO.getRenovationInfo(params.getCode());
		} catch (final SQLException e) {
			LOGGER.log(Level.WARNING, "El usuario no ha sido encontrado", e); //$NON-NLS-1$
			resp.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		// Si el usuario tiene datos a nulo en base de datos se va fuera
		if (restorationInfo.getCodeInfo() == null || restorationInfo.getRenovationDate() == null) {
			LOGGER.log(Level.WARNING, "El usuario no tenia registrada la informacion de restauracion de contrasena"); //$NON-NLS-1$
			resp.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		// Comprobar que el usuario tiene asignado en BD el codigo
		if (!restorationInfo.getCodeInfo().equals(params.getCode())) {
			LOGGER.log(Level.WARNING, "No se han proporcionado el identificador del usuario o no se han podido recuperar sus datos"); //$NON-NLS-1$
			resp.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		// Si se ha excedido el tiempo de espera, no permitimos la renovacion
		final long currentTime = new Date().getTime();
		final long renovationTime = restorationInfo.getRenovationDate().getTime();
		final long expirationTime = ConfigManager.getExpiration();

		if (currentTime > renovationTime + expirationTime) {
			LOGGER.log(Level.WARNING, "Se ha excedido el tiempo maximo de espera hasta la renovacion de la contrasena"); //$NON-NLS-1$
			resp.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.EXCEP_TIME.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		// Actualizar verdadero el tiempo  de la nueva url
		final User user = new User();
		try {
			UsersDAO.updateRestorePasswordAuthoritation(user.getId(), true);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, " restablecer la contrase&ntilde;a", e); //$NON-NLS-1$
			resp.sendRedirect("Login.jsp"); //$NON-NLS-1$
			return;
		}

		// Crear sesion
		final HttpSession session = req.getSession();
		session.setAttribute(ServiceParams.SESSION_ATTR_RESTORATION, restorationInfo);

		// Redirigir al usuario a la web de restablecimiento indicando el codigo recibido
		resp.sendRedirect("User/RestorePasswordUser.jsp?" + ServiceParams.PARAM_USERID + "=" + restorationInfo.getId() + //$NON-NLS-1$ //$NON-NLS-2$
				"&" + ServiceParams.PARAM_USERNAME + "=" + restorationInfo.getName() + "&" + ServiceParams.PARAM_CODE + "=" + restorationInfo.getCodeInfo()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * Obtiene los par&aacute;metros aceptados por el servicio.
	 * @param req Petici&oacute;n HTTP.
	 */
	private Parameters getParameters(final HttpServletRequest req) {

		final String cod = req.getParameter(ServiceParams.PARAM_CODE);

		final Parameters params = new Parameters();


		if (cod != null && !cod.isEmpty()) {
			params.setCode(cod);
		}

		return params;
	}

	/**
	 * Conjunto de parametros admitidos por el servicio.
	 */
	class Parameters {


		private String cod = null;



		/**
		  * Establece la url del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @return La url del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public String getCode() {
			return this.cod;
		}
		/**
		  * Obtiene la url del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @param La url del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public void setCode(final String code) {
			this.cod = code;
		}
	}

}
