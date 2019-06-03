package es.gob.fire.server.admin.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.tool.Base64;

/**
 * Servlet implementation class ModifyUserService
 */
public class ModifyPasswdUserService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(NewUserService.class.getName());

	private static final String PARAM_NAME = "nombre-usr"; //$NON-NLS-1$
	private static final String PARAM_OLD_PASSWD = "old_passwd-usr"; //$NON-NLS-1$
	private static final String PARAM_NEW_PASSWD = "passwd-usr_1"; //$NON-NLS-1$
	private static final String PARAM_NEW_PASSWD_COPY = "passwd-usr_2"; //$NON-NLS-1$
	private static final String PARAM_OP = "op"; //$NON-NLS-1$
	private static final String PARAM_USERID = "id-usr"; //$NON-NLS-1$
	private static final String SHA_2 = "SHA-256"; //$NON-NLS-1$

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModifyPasswdUserService() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		// Obtener el tipo de operacion 2-Edicion, 3 Cambio de contrasena
		final int op = Integer.parseInt(req.getParameter(PARAM_OP));
		final String stringOp = op == 3 ? "clave" : "" ;  //$NON-NLS-1$ //$NON-NLS-2$
		// Obtenemos los parametros enviados del formulario
		final Parameters params = getParameters(req);
		boolean isOk = true;
		try {
			User usr = null;
			if (params.getUserId() != null) {
				usr = UsersDAO.getUser(params.getUserId());
			}

			if (usr == null) {
				LOGGER.log(Level.WARNING, "No se han proporcionado el identificador del usuario o no se han podido recuperar sus datos"); //$NON-NLS-1$
				isOk = false;
			}
			else if(params.getUsername() == null || params.getOldPassword() == null ||
					params.getNewPassword() == null && params.getNewPasswordCopy() == null) {
				LOGGER.log(Level.WARNING, "No se han proporcionado todos los datos requeridos para cambiar la clave (Nombre de usuario , clave antigua, Clave nueva y repetir clave nueva)"); //$NON-NLS-1$
				isOk = false;
			}
			else if(params.getNewPassword() != null && params.getNewPasswordCopy() != null
						&& !params.getNewPassword().equals(params.getNewPasswordCopy())) {
				LOGGER.log(Level.WARNING, "Clave nueva y repetir clave nueva, deben ser iguales"); //$NON-NLS-1$
				isOk = false;
			}
			//Comprobamos que la clave introducida como antigua es la que tiene guardada en la base de datos
			else if(usr.getPassword() != null && params.getOldPassword() != null && !"".equals(params.getOldPassword())) { //$NON-NLS-1$
				final MessageDigest md_oldPass = MessageDigest.getInstance(SHA_2);
				byte[] digest_oldPass;
				String oldPasswd = null;
				md_oldPass.update(params.getOldPassword().getBytes());
				digest_oldPass = md_oldPass.digest();
				oldPasswd = Base64.encode(digest_oldPass);
				if(!oldPasswd.equals(usr.getPassword())) {
					LOGGER.log(Level.SEVERE, "La clave antigua introducida es erronea."); //$NON-NLS-1$
					isOk = false;
				}
				else {

					try {

						if(op == 3) {
							final MessageDigest md = MessageDigest.getInstance(SHA_2);
							byte[] digest;
							String clave = null;

							//Obtenemos la clave codificada
							if(params.getNewPassword()!=null && !"".equals(params.getNewPassword())) { //$NON-NLS-1$
								md.update(params.getNewPassword().getBytes());
								digest = md.digest();
								clave = Base64.encode(digest);
							}

							LOGGER.info("Cambio de clave del usuario con nombre: " + params.getUsername()); //$NON-NLS-1$
							try {
								UsersDAO.updateUserPasswd(params.getUserId() , params.getUsername(), clave);
							}
							catch (final SQLException e) {
								LOGGER.log(Level.SEVERE, "Error en la modificacion de la contrasena del usuario con nombre:" + usr.getUserName(), e); //$NON-NLS-1$
								isOk = false;
							}

						}
					}
					catch (final NoSuchAlgorithmException e) {
						LOGGER.log(Level.SEVERE, "El algoritmo de codificacion de las contrasenas no esta soportado", e); //$NON-NLS-1$
						isOk = false;
					}
				}
			}
			else {
				throw new IllegalStateException("Estado no permitido");//$NON-NLS-1$
			}
			resp.sendRedirect("User/UserPage.jsp?op=" + stringOp + "&r=" + (isOk ? "1" : "0")+"&ent=user"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		catch (final IllegalArgumentException e){
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error con el base64 : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("./User/UserPage.jsp?error=true&name=" + params.getUsername() ); //$NON-NLS-1$
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE,"Ha ocurrido un error cambiar la clave : " + e, e); //$NON-NLS-1$
			resp.sendRedirect("./User/UserPage.jsp?op=clave&r=0&ent=user"); //$NON-NLS-1$
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Obtiene los par&aacute;metros aceptados por el servicio.
	 * @param req Petici&oacute;n HTTP.
	 */
	private Parameters getParameters(final HttpServletRequest req) {

		final Parameters params = new Parameters();

		if(req.getParameter(PARAM_USERID) != null && !"".equals(req.getParameter(PARAM_USERID))) { //$NON-NLS-1$
			params.setUserId(req.getParameter(PARAM_USERID));
		}
		if(req.getParameter(PARAM_NAME) != null && !"".equals(req.getParameter(PARAM_NAME))) { //$NON-NLS-1$
			params.setUsername(req.getParameter(PARAM_NAME));
		}
		if(req.getParameter(PARAM_NEW_PASSWD) != null && !"".equals(req.getParameter(PARAM_NEW_PASSWD))) { //$NON-NLS-1$
			params.setNewPassword(req.getParameter(PARAM_NEW_PASSWD));
		}
		if(req.getParameter(PARAM_OLD_PASSWD) != null && !"".equals(req.getParameter(PARAM_OLD_PASSWD))) { //$NON-NLS-1$
			params.setOldPassword(req.getParameter(PARAM_OLD_PASSWD));
		}
		if(req.getParameter(PARAM_NEW_PASSWD_COPY) != null && !"".equals(req.getParameter(PARAM_NEW_PASSWD_COPY))) { //$NON-NLS-1$
			params.setNewPasswordCopy(req.getParameter(PARAM_NEW_PASSWD_COPY));
		}
		return params;
	}

	/**
	 * Conjunto de parametros admitidos por el servicio.
	 */
	class Parameters {

		private String userId = null;
		private String username = null;
		private String oldPassword = null;
		private String newPassword = null;
		private String newPasswordCopy = null;

		public String getUserId() {
			return this.userId;
		}
		public String getUsername() {
			return this.username;
		}
		public String getOldPassword() {
			return this.oldPassword;
		}
		public String getNewPassword() {
			return this.newPassword;
		}
		public String getNewPasswordCopy() {
			return this.newPasswordCopy;
		}
		public void setUserId(final String userId) {
			this.userId = userId;
		}
		public void setUsername(final String username) {
			this.username = username;
		}
		public void setOldPassword(final String oldPassword) {
			this.oldPassword = oldPassword;
		}
		public void setNewPassword(final String newPassword) {
			this.newPassword = newPassword;
		}
		public void setNewPasswordCopy(final String newPasswordCopy) {
			this.newPasswordCopy = newPasswordCopy;
		}
	}
}
