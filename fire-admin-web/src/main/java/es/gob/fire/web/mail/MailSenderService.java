/**
 * <b>File:</b><p>es.gob.fire.core.util.MailSenderService.java.</p>
 * <b>Description:</b><p>Class that manages the emails.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electronica para el Ambito Judicial.</p>
 * <b>Date:</b><p>8 mar. 2019.</p>
 * @author Consejería de Justicia e Interior de la Junta de Andalucía.
 * @version 1.0, 8 mar. 2019.
 */
package es.gob.fire.web.mail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.entity.User;

/**
 * <p>Class that manages the emails.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electronica para el Ambito Judicial.</p>
 * @version 1.1, 7 oct. 2019.
 */
@Component
public class MailSenderService {

	/**
	 * Attribute that represents the default expired time.
	 */
	public static final int DEFAULT_EXPIRED_TIME = 1800000;

	/**
	 * Attribute that represents file mail property.
	 */
	private static Properties properties = new Properties();

	/**
	 * Attribute that represents the session mail.
	 */
	private static Session sessionMail;

	/**
	 * Attribute that represents the mail hot.
	 */
	@Value("${mail.smtp.host}")
	private String mailSmtpHost;

	/**
	 * Attribute that represents the mail port.
	 */
	@Value("${mail.smtp.port}")
	private String mailSmtpPort;

	/**
	 * Attribute that represents the mail protocol.
	 */
	@Value("${mail.protocol}")
	private String mailProtocol;

	/**
	 * Attribute that represents the mail sender.
	 */
	@Value("${mail.smtp.mail.sender}")
	private String mailSmtpMailSender;

	/**
	 * Attribute that represents the mail start TLS enable.
	 */
	@Value("${mail.smtp.starttls.enable}")
	private String mailSmtpStarttlsEnable;

	/**
	 * Attribute that represents the mail user.
	 */
	@Value("${mail.smtp.user}")
	private String mailSmtpUser;

	/**
	 * Attribute that represents the mail password.
	 */
	@Value("${mail.smtp.password}")
	private String mailSmtpPassword;

	/**
	 * Attribute that represents the mail authentication.
	 */
	@Value("${mail.smtp.auth}")
	private String mailSmtpAuth;

	/**
	 * Attribute that represents the mail authentication.
	 */
	@Value("${mail.from.name}")
	private String mailFromName;

	/**
	 * Attribute that represents the mail password expiration.
	 */
	@Value("${mail.password.expiration}")
	private String mailPasswordExpiration;

	/**
	 * Constructor method for the class EmailSenderService.java.
	 */
	private MailSenderService() {
	}

	/**
	 * Method that configures the send mail.
	 * @throws FileNotFoundException file not found
	 * @throws IOException error load file
	 */
	private void init() throws FileNotFoundException, IOException {
		// Cargamos las propiedades de configuracion del correo
		properties.put(Constants.MAIL_SMTP_HOST, this.mailSmtpHost);
		properties.put(Constants.MAIL_SMTP_PORT, this.mailSmtpPort);
		properties.put(Constants.MAIL_SMTP_MAIL_SENDER, this.mailSmtpMailSender);
		properties.put(Constants.MAIL_SMTP_STARTTLS_ENABLE, this.mailSmtpStarttlsEnable);
		properties.put(Constants.MAIL_PROTOCOL, this.mailProtocol);
		// Comprobamos si es necesario autenticacion
		if (this.mailSmtpAuth != null && this.mailSmtpAuth.equals(Boolean.TRUE.toString())) {
			properties.put(Constants.MAIL_SMTP_AUTH, this.mailSmtpAuth);
			properties.put(Constants.MAIL_SMTP_USER, this.mailSmtpUser);
			properties.put(Constants.MAIL_SMTP_PASSWORD, this.mailSmtpPassword);
			// Obtenemos la sesion con seguridad.
			final Authenticator smtpAuthenticator = new SmtpAuthenticator(properties.getProperty(Constants.MAIL_SMTP_USER), properties.getProperty(Constants.MAIL_SMTP_PASSWORD));
			sessionMail = Session.getInstance(properties, smtpAuthenticator);
		} else {
			// Obtenemos la sesion sin seguridad
			sessionMail = Session.getDefaultInstance(properties);
		}
	}

	/**
	 * Method that sends a email to restore the user password.
	 * @param user to send the email
	 * @param URL to restore the user password
	 * @throws FileNotFoundException file not found
	 * @throws IOException error load file
	 * @throws MessagingException
	 * @throws AddressException
	 */
	public void sendEmail(final User user, final String url) throws FileNotFoundException, IOException, AddressException, MessagingException {
			init();

			final MimeMessage message = new MimeMessage(sessionMail);
			// String cuerpoMensaje = "";

			// Establecemos los destinatarios
			message.setFrom(new InternetAddress((String) properties.get(Constants.MAIL_SMTP_MAIL_SENDER)));
			// Insertamos todos los destinatarios configurados en el properties
			final String[ ] listaDestinatarios = user.getEmail().split(UtilsStringChar.SYMBOL_COMMA_STRING);
			for (int i = 0; i < listaDestinatarios.length; i++) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(listaDestinatarios[i]));
			}

			// Creamos el cuerpo del correo
			final BodyPart bodyPart = new MimeBodyPart();
			final String bodyBase = Constants.MAIL_TEXT + "<br><br>" + Constants.MAIL_FOOTER;

			// Insertamos los datos del usuario
			final String body = bodyBase
					.replace("%%NAME%%", user.getName()) //$NON-NLS-1$
					.replace("%%USERNAME%%", user.getUserName()) //$NON-NLS-1$
					.replace("%%MAIL%%", user.getEmail()) //$NON-NLS-1$
					.replace("%%URL%%", url); //$NON-NLS-1$

			bodyPart.setContent(body, Constants.MAIL_TEXT_HTML_CHARSET);


			// Unimos el cuerpo con el archivo a adjuntar
			final MimeMultipart multiPart = new MimeMultipart();
			multiPart.addBodyPart(bodyPart);

			// Añadimos los datos al mensaje
			// Establecemos el Asunto del correo
			message.setSubject(Constants.MAIL_SUBJECT);
			message.setFrom(new InternetAddress(this.mailSmtpMailSender, this.mailFromName));
			message.setContent(multiPart);

			// Guardamos los cambios
			message.saveChanges();

			// Obtenemos el objeto transport para conectarnos y enviar el correo
			final Transport transport = sessionMail.getTransport(properties.getProperty(Constants.MAIL_PROTOCOL));
			transport.connect();
			transport.sendMessage(message, message.getAllRecipients());
	}

	/**
	 * <p>Class that implements the java mail authentication.</p>
	 * <b>Project:</b><p>Servicios Integrales de Firma Electronica para el Ambito Judicial.</p>
	 * @version 1.0, 12 mar. 2019.
	 */
	static class SmtpAuthenticator extends Authenticator {

		/**
		 * Attribute that represents the user name authentication.
		 */
		private final String username;
		/**
		 * Attribute that represents the password authentication.
		 */
		private final String password;

		/**
		 * Constructor method for the class EmailSenderService.SmtpAuthenticator.java.
		 * @param usernameParam user name
		 * @param passwordParam password
		 */
		SmtpAuthenticator(final String usernameParam, final String passwordParam) {
			super();
			this.username = usernameParam;
			this.password = passwordParam;
		}

		/**
		 * {@inheritDoc}
		 * @see javax.mail.Authenticator#getPasswordAuthentication()
		 */
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(this.username, this.password);
		}
	}

	/**
	 * Gets mailPasswordExpiration.
	 * @return mailPasswordExpiration
	 */
	public String getMailPasswordExpiration() {
		return this.mailPasswordExpiration;
	}

	/**
	 * Sets mailPasswordExpiration
	 * @param mailPasswordExpiration
	 */
	public void setMailPasswordExpiration(final String mailPasswordExpirationP) {
		this.mailPasswordExpiration = mailPasswordExpirationP;
	}

}
