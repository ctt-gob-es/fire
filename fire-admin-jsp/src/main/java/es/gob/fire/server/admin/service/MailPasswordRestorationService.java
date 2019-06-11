package es.gob.fire.server.admin.service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.fire.server.admin.dao.UsersDAO;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.message.UserMessages;
import es.gob.fire.server.admin.tool.SendMail;


/**
 * Servicio para el envio del enlace y mensaje al correo del usuario que quiere restablecer la contrase&ntilde;a
 * @author Carlos.J.Raboso
 *
 */

public class MailPasswordRestorationService extends HttpServlet{

	private static final long serialVersionUID = -1102186174811586133L;

	private static final Logger LOGGER = Logger.getLogger(MailPasswordRestorationService.class.getName());

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		final String mail = request.getParameter(ServiceParams.PARAM_MAIL);

        if (mail == null || mail.isEmpty()) {
        	response.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_SEND_MAIL.getCode()); //$NON-NLS-1$
        	return;
        }

        // Recuperamos la informacion del usuario
        User user;
		try {
			user = UsersDAO.getUserInfoByMailOrLogin(mail);
		} catch (final SQLException e) {
        	LOGGER.log(Level.WARNING, "No se pudo encontrar el mail", e); //$NON-NLS-1$
        	response.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_INCORRET_MAIL.getCode()); //$NON-NLS-1$
        	return;
		}

		// Comprobamos que se haya encontrado el usuario y que tenga correo y si se han modificado se cambian
		if (user == null || user.getMail() == null) {
			LOGGER.log(Level.WARNING, "El usuario no existe o no tiene asignado mail "); //$NON-NLS-1$
        	response.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_INCORRET_USER.getCode()); //$NON-NLS-1$
        	return;
		}

		// Comprobamos que el usuario tenga permiso de acceso
		final boolean logginPermission = user.getPermissions().hasLoginPermission();
		if (!logginPermission) {
			LOGGER.log(Level.WARNING, "El usuario " + user + " no tiene permiso de acceso"); //$NON-NLS-1$ //$NON-NLS-2$
        	response.sendRedirect("Login.jsp?" + ServiceParams.PARAM_SUCCESS + "=" + UserMessages.SUC_SEND_MAIL.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
        	return;
		}

		 final String id = new String();
		// Generar codigo de autenticacion
		final String cod = buildRestorationCode(id);

		// Crear URL
		final String url = getRestorationPageUrl(request) + "?" + ServiceParams.PARAM_CODE + "=" + cod; //$NON-NLS-1$


		// Asociar el codigo al usuario
		try {
			final Date currentDate = new Date();

			UsersDAO.updateRenovationPasswordCode(user.getId(), cod, currentDate);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, " restablecer la contrase&ntilde;a", e); //$NON-NLS-1$
        	response.sendRedirect("Login.jsp"); //$NON-NLS-1$
        	return;
		}


		// Cargar titulo y cuerpo del mensaje
		String subject = null;
		String body = ""; //$NON-NLS-1$

		final FileReader f = new FileReader("C:\\Users\\carlos.j.raboso\\Documents\\RepositorioGit\\fire\\fire-admin-jsp\\src\\main\\resources\\mail_reset_password.txt"); //$NON-NLS-1$
		final BufferedReader b = new BufferedReader(f);
		String line;
		while ((line = b.readLine())!=null) {
			if (subject == null) {
				subject = line;
			}
			else {
				body += line + "<br>"; //$NON-NLS-1$
			}
		}
		b.close();

		// Integrar el nombre de la persona y la URL en el mensaje
		body = body.replace("%%NAME%%", user.getName()) //$NON-NLS-1$
				.replace("%%USERNAME%%", user.getUserName()) //$NON-NLS-1$
				.replace("%%MAIL%%", user.getMail()) //$NON-NLS-1$
				.replace("%%URL%%", url); //$NON-NLS-1$


		// Enviar correo
		final List<String> receivers = Collections.singletonList(user.getMail());
		try {
			SendMail.sendMail(receivers, subject, body);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "El mensaje no ha sido enviado", e); //$NON-NLS-1$
			response.sendRedirect("Login.jsp?" + ServiceParams.PARAM_ERR + "=" + UserMessages.ERR_SEND_MAIL.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
	        return;
		}

		 response.sendRedirect("Login.jsp?" + ServiceParams.PARAM_SUCCESS + "=" + UserMessages.SUC_SEND_MAIL.getCode()); //$NON-NLS-1$ //$NON-NLS-2$
	}

    /**
    * Construye un codigo de restauracion de contrase&ntilde;a.
    * @param id Identificador del usuario que solicita la restauraci&oacute;n.
    * @return C&oacute;digo de restauraci&oacute;n en base64.
    */
    private static String buildRestorationCode(final String id) {

          final UUID uuid = UUID.randomUUID();
          final byte[] code = uuid.toString().getBytes();

          final SecureRandom random = new SecureRandom();
          final byte[] salt = new byte[16];
          random.nextBytes(salt);

          MessageDigest md;
          try {
                 md = MessageDigest.getInstance("SHA-512"); //$NON-NLS-1$
          } catch (final NoSuchAlgorithmException e) {
                 LOGGER.severe("Algoritmo de cifrado no soportado: " + e); //$NON-NLS-1$
                 throw new RuntimeException("Error grave: Algoritmo interno de cifrado no soportado", e); //$NON-NLS-1$
          }
          md.update(salt);
          md.update(id.getBytes());
          md.update(code);

          return Base64.getUrlEncoder().encodeToString(md.digest());
    }

    private static String getRestorationPageUrl(final HttpServletRequest req) {

        return req.getScheme()
                     + "://" //$NON-NLS-1$
                     + req.getServerName()
                     + ":" //$NON-NLS-1$
                     + req.getServerPort()
                     + req.getRequestURI().substring(0, req.getRequestURI().indexOf('/', 1))
                     + "/mailRestorePasswordUser"; //$NON-NLS-1$
  }


}
