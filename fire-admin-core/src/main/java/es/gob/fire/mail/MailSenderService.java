/**
 * <b>File:</b><p>es.gob.fire.core.util.MailSenderService.java.</p>
 * <b>Description:</b><p>Class that manages the emails.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electronica para el Ambito Judicial.</p>
 * <b>Date:</b><p>8 mar. 2019.</p>
 * @author Consejería de Justicia e Interior de la Junta de Andalucía.
 * @version 1.2, 24/02/2025.
 */
package es.gob.fire.mail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
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

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Constants;

/**
 * <p>Class that manages the emails.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electronica para el Ambito Judicial.</p>
 * @version 1.2, 24/02/2025.
 */
@Component
public class MailSenderService {


	private static final Logger LOGGER = Logger.getLogger(MailSenderService.class);

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
	 * Attribute that represents the mail start TLS required.
	 */
	@Value("${mail.smtp.starttls.required:}")
	private String mailSmtpStarttlsRequired;

	/**
	 * Attribute that represents the SSL protocols to use with SMTP.
	 */
	@Value("${mail.smtp.ssl.protocols:}")
	private String mailSmtpSslProtocols;

	/**
	 * Attribute that represents the port to SocketFactory.
	 */
	@Value("${mail.smtp.socketFactory.port:}")
	private String mailSmtpSocketFactoryPort;

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
		if (!this.mailSmtpStarttlsRequired.isEmpty()) {
			properties.put(Constants.MAIL_SMTP_STARTTLS_REQUIRED, this.mailSmtpStarttlsRequired);
		}

		if (!this.mailSmtpSslProtocols.isEmpty()) {
			properties.put(Constants.MAIL_SMTP_SSL_PROTOCOLS, this.mailSmtpSslProtocols);
		}
		if (!this.mailSmtpSocketFactoryPort.isEmpty()) {
			properties.put(Constants.MAIL_SMTP_SOCKETFACTORY_PORT, this.mailSmtpSocketFactoryPort);
		}
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
	 * Method that sends a email to itself to check the mail is working.
	 * @throws FileNotFoundException file not found
	 * @throws IOException error load file
	 * @throws MessagingException
	 * @throws AddressException
	 */
	public void checkSendEmail() throws FileNotFoundException, IOException, AddressException, MessagingException {
			init();

			// Establezco la propia direccion de envio como la de destino
			final String[ ] listaDestinatarios = new String[]
					{
							(String) properties.get(Constants.MAIL_SMTP_MAIL_SENDER)
					};

			// Establecemos como mensaje el basico sin personalizar
			final String body = Constants.MAIL_TEXT + "<br><br>" + Constants.MAIL_FOOTER; //$NON-NLS-1$

			// Realizamos el envio
			sendEmail(listaDestinatarios, body);
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
	private void sendEmail(final String[] listaDestinatarios, final String body) throws FileNotFoundException, IOException, AddressException, MessagingException {

			final MimeMessage message = new MimeMessage(sessionMail);

			// Indicamos el emisor
			message.setFrom(new InternetAddress((String) properties.get(Constants.MAIL_SMTP_MAIL_SENDER)));

			// Establecemos los destinatarios
			for (int i = 0; i < listaDestinatarios.length; i++) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(listaDestinatarios[i]));
			}

			// Creamos el cuerpo del correo
			final BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(body, Constants.MAIL_TEXT_HTML_CHARSET);


			// Unimos el cuerpo con el archivo a adjuntar
			final MimeMultipart multiPart = new MimeMultipart();
			multiPart.addBodyPart(bodyPart);

			// Anadimos los datos al mensaje
			// Establecemos el Asunto del correo
			message.setSubject(Constants.MAIL_SUBJECT);
			message.setFrom(new InternetAddress(this.mailSmtpMailSender, this.mailFromName));
			message.setContent(multiPart);

			// Guardamos los cambios
			message.saveChanges();

			// Obtenemos el objeto transport para conectarnos y enviar el correo
			Transport transport = null;

			try {
			    transport = sessionMail.getTransport(properties.getProperty(Constants.MAIL_PROTOCOL));
			    transport.connect();
			    transport.sendMessage(message, message.getAllRecipients());
			} catch (MessagingException e) {
			    LOGGER.error(e);
			} finally {
			    // Cerramos el transport manualmente en el bloque finally
			    if (transport != null) {
			        try {
			            transport.close();
			        } catch (MessagingException e) {
			            e.printStackTrace();
			        }
			    }
			}
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

	public void sendEmail(Address[] addresses, String subject, StringBuilder bodySubject, String msgEmailSucces) {
		Transport transport = null;

	    try {
	    	init();
	    	
	    	// Creamos un nuevo MimeMessage para cada correo
            final MimeMessage message = new MimeMessage(sessionMail);
	    	
	        // Creamos el transport una sola vez
	        transport = sessionMail.getTransport(properties.getProperty(Constants.MAIL_PROTOCOL));
	        transport.connect(); // Establecemos la conexión

            // Indicamos el emisor
            message.setFrom(new InternetAddress((String) properties.get(Constants.MAIL_SMTP_MAIL_SENDER)));

            // Establecemos los destinatarios
            message.addRecipients(Message.RecipientType.TO, addresses);

            // Establecemos el asunto del correo
            message.setSubject(subject);

            // Establecemos el cuerpo del correo en formato HTML
            message.setContent(bodySubject.toString(), "text/plain; charset=UTF-8");

            // Guardamos los cambios
            message.saveChanges();

            // Enviamos el mensaje
            transport.sendMessage(message, message.getAllRecipients());

            LOGGER.info(msgEmailSucces);
	    } catch (Exception e) {
	        LOGGER.error("Se ha producido un error al enviar el correo: ", e);
	    } finally {
	        // Cerramos el transport solo una vez al final
	        if (transport != null) {
	            try {
	                transport.close();
	            } catch (MessagingException e) {
	                LOGGER.error("Error al cerrar el transport", e);
	            }
	        }
	    }
	}

}
