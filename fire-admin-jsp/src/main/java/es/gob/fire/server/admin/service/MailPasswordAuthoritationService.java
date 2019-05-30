package es.gob.fire.server.admin.service;



import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import es.gob.fire.server.admin.message.UserMessages;
import es.gob.fire.server.admin.tool.Base64;

/**
 * Servicio para autorizar el cambio de contrase&ntilde;a
 * @author Carlos.J.Raboso
 *
 */

public class MailPasswordAuthoritationService extends HttpServlet {


	private static final long serialVersionUID = -1102186174811586133L;

	private static final Logger LOGGER = Logger.getLogger(MailPasswordAuthoritationService.class.getName());
	private static final String SHA_2 = "SHA-256"; //$NON-NLS-1$


	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

		 final HttpSession session = req.getSession(false);
		 if (session == null) {
			 resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			 return;
		 }

		final Parameters params = getParameters(req);

		final UserRestorationInfo restorationInfo = (UserRestorationInfo) session.getAttribute(ServiceParams.SESSION_ATTR_RESTORATION);

		// Comprobamos que el usuario es, el que realmente nos ha realizado la peticion
		if (!restorationInfo.getId().equals(params.getUserId())) {
			LOGGER.log(Level.WARNING, "El id de usuario es distinto al utilizado en la sesion"); //$NON-NLS-1$
			resp.sendRedirect("Login.jsp"); //$NON-NLS-1$
        	return;
		 }

		 if (params.getCode() == null && !restorationInfo.getCodeInfo().equals(params.getCode())) {
			 LOGGER.log(Level.WARNING, "El usuario no ha sido encontrado o es nulo"); //$NON-NLS-1$
			 resp.sendRedirect("Login.jsp"); //$NON-NLS-1$
			 return;
		 }

		 // Comprobamos que el enlace no ha caducado
		 if (new Date().getTime() > restorationInfo.getRenovationDate().getTime() + ConfigManager.getExpiration()) {
			 LOGGER.log(Level.WARNING, "Se ha excedido el tiempo maximo de espera hasta la renovacion de la contrasena"); //$NON-NLS-1$
			 resp.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.EXCEP_TIME.getCode()); //$NON-NLS-1$
			 return;
		 }

		 // Comprobamos que las passwords insertadas sean iguales
		 if(params.getNewPassword() != null && params.getNewPasswordCopy() != null
					&& !params.getNewPassword().equals(params.getNewPasswordCopy())) {
			LOGGER.log(Level.WARNING, "Clave nueva y repetir clave nueva, deben ser iguales"); //$NON-NLS-1$
			return;
		}

		 // Calculamos el valor codificado de la contrasena
		 try {
			 final MessageDigest md = MessageDigest.getInstance(SHA_2);

			 String clave =null;

			 //Obtenemos la clave codificada
			 if (params.getNewPassword() != null && !params.getNewPassword().isEmpty()) {
				 md.update(params.getNewPassword().getBytes());
				 final byte[] digest = md.digest();
				 clave = Base64.encode(digest);
			 }
			 LOGGER.info("Cambio de clave del usuario con nombre: " + params.getUserId()); //$NON-NLS-1$


			// Guardamos la password en base de datos y reseteamos los valores de comprobacion
			 try {
				 UsersDAO.updateRestoreValues(params.getUserId(),clave);

		 } catch (final Exception e) {
			 LOGGER.log(Level.SEVERE, "la clave codificada no se ha podido guardar", e); //$NON-NLS-1$
			 resp.sendRedirect("Login.jsp"); //$NON-NLS-1$
			 return;
		 }




		 }
		 catch (final NoSuchAlgorithmException e) {
				LOGGER.log(Level.SEVERE, "El algoritmo de codificacion de las contrasenas no esta soportado", e); //$NON-NLS-1$

			}





		 // Salimos de la sesion
		 session.invalidate();
		 resp.sendRedirect("Login.jsp?" + ServiceParams.PARAM_SUCCESS + "=" + UserMessages.PASS_RESTORE_CORRECT.getCode()); //$NON-NLS-1$
	}



	private Parameters getParameters(final HttpServletRequest req) {

		final String userid = req.getParameter(ServiceParams.PARAM_USERID);
		final String cod = req.getParameter(ServiceParams.PARAM_CODE);
		final String password = req.getParameter(ServiceParams.PARAM_PASSWORD);
		final String passwordcopy = req.getParameter(ServiceParams.PARAM_PASSWORD_COPY);

		final Parameters params = new Parameters();

		if (userid != null && !userid.isEmpty()) {
			params.setUserId(userid);
		}
		if (cod != null && !cod.isEmpty()) {
			params.setCode(cod);
		}
		if(password != null && !password.isEmpty()) {
			params.setNewPassword(password);
		}

		if(passwordcopy != null && !passwordcopy.isEmpty()) {
			params.setNewPasswordCopy(passwordcopy);
		}
		return params;
	}

	/**
	 * Conjunto de parametros admitidos por el servicio.
	 */
	class Parameters {


		private String userid = null;
		private String cod = null;
		private String newPassword = null;
		private String newPasswordCopy = null;


		/**
		  * Establece el id del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @return El ig del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public String getUserId() {
			return this.userid;
		}

		/**
		  * Obtiene el id del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @param El id del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public void setUserId(final String userId) {
			this.userid = userId;
		}
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

		/**
		  * Establece la password del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @return La password del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public String getNewPassword() {
			return this.newPassword;
		}

		/**
		  * Obtiene la password al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @param La password del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public void setNewPassword(final String newPassword) {
			this.newPassword = newPassword;
		}
		/**
		  * Establece la copia de la password del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @return La copia de la password al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public String getNewPasswordCopy() {
			return this.newPasswordCopy;

		}
		/**
		  * Obtiene la copia de la password del usuario al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  * @param La copia de la password al que se le va a enviar el c&oacute;digo de renovaci&oacute;n de contrase&ntilde;a
		  */
		public void setNewPasswordCopy(final String PasswordCopy) {
			this.newPasswordCopy = PasswordCopy;
		}



	}
}
