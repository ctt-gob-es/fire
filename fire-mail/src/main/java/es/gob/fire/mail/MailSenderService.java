/**
 * <b>Description:</b><p>Class that manages the emails.</p>
 * <b>Date:</b><p>8 mar. 2019.</p>
 * @version 1.2, 24/02/2025.
 */
package es.gob.fire.mail;

import java.util.Arrays;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Class that manages the emails.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electronica para el Ambito Judicial.</p>
 * @version 1.2, 24/02/2025.
 */
public class MailSenderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailSenderService.class);

	/**
	 * Attribute that represents the utf-8 property.
	 */
	public static final String MAIL_TEXT_PLAIN_CHARSET = "text/plain; charset=UTF-8"; //$NON-NLS-1$

	/**
	 * Attribute that represents the HTML text type.
	 */
	public static final String MAIL_TEXT_HTML_CHARSET = "text/html; charset=UTF-8"; //$NON-NLS-1$

	private Properties properties;

	private Session sessionMail = null;

	/**
     * Initializes the email configuration using the provided {@link Properties} object.
     * <p>
     * This method sets up the necessary properties for sending emails. If authentication
     * is required, it configures a secure session using the provided credentials.
     * </p>
     * If an error occurs during the setup, it is logged appropriately.
     *
     * @param props A {@link Properties} object containing email configuration values.
     */
	public void init(final Properties props) {

		this.properties = new Properties();
		try {
			this.properties.put(ConstantsMail.MAIL_SMTP_HOST, props.getProperty(ConstantsMail.MAIL_SMTP_HOST));
	        this.properties.put(ConstantsMail.MAIL_SMTP_PORT, props.getProperty(ConstantsMail.MAIL_SMTP_PORT));
	        this.properties.put(ConstantsMail.MAIL_SMTP_MAIL_SENDER, props.getProperty(ConstantsMail.MAIL_SMTP_MAIL_SENDER));
	        this.properties.put(ConstantsMail.MAIL_SMTP_STARTTLS_ENABLE, props.getProperty(ConstantsMail.MAIL_SMTP_STARTTLS_ENABLE));

	        final String starttlsRequired = props.getProperty(ConstantsMail.MAIL_SMTP_STARTTLS_REQUIRED, ""); //$NON-NLS-1$
	        if (!starttlsRequired.isEmpty()) {
	            this.properties.put(ConstantsMail.MAIL_SMTP_STARTTLS_REQUIRED, starttlsRequired);
	        }

	        final String sslProtocols = props.getProperty(ConstantsMail.MAIL_SMTP_SSL_PROTOCOLS, ""); //$NON-NLS-1$
	        if (!sslProtocols.isEmpty()) {
	            this.properties.put(ConstantsMail.MAIL_SMTP_SSL_PROTOCOLS, sslProtocols);
	        }

	        final String socketFactoryPort = props.getProperty(ConstantsMail.MAIL_SMTP_SOCKETFACTORY_PORT, ""); //$NON-NLS-1$
	        if (!socketFactoryPort.isEmpty()) {
	            this.properties.put(ConstantsMail.MAIL_SMTP_SOCKETFACTORY_PORT, socketFactoryPort);
	        }

	        this.properties.put(ConstantsMail.MAIL_PROTOCOL, props.getProperty(ConstantsMail.MAIL_PROTOCOL));

	        // Comprobamos si es necesaria autenticacion
	        final String mailSmtpAuth = props.getProperty(ConstantsMail.MAIL_SMTP_AUTH);
	        if (Boolean.parseBoolean(mailSmtpAuth)) {
	            this.properties.put(ConstantsMail.MAIL_SMTP_AUTH, mailSmtpAuth);
	            this.properties.put(ConstantsMail.MAIL_SMTP_USER, props.getProperty(ConstantsMail.MAIL_SMTP_USER));
	            this.properties.put(ConstantsMail.MAIL_SMTP_PASSWORD, props.getProperty(ConstantsMail.MAIL_SMTP_PASSWORD));

	            // Obtenemos la sesion con seguridad
	            final Authenticator smtpAuthenticator = new SmtpAuthenticator(
	            	props.getProperty(ConstantsMail.MAIL_SMTP_USER),
	            	props.getProperty(ConstantsMail.MAIL_SMTP_PASSWORD)
	            );
	            this.sessionMail = Session.getInstance(this.properties, smtpAuthenticator);
	        } else {
	            // Obtenemos la seson sin seguridad
	            this.sessionMail = Session.getDefaultInstance(this.properties);
	        }
		} catch (final Exception e) {
			LOGGER.error("Se ha producido un fallo al cargar alguna propiedad procedente del properties", e); //$NON-NLS-1$
		}

	}

	public void sendEmail(final Address[] addresses, final String subject, final StringBuilder bodyMessage, final String msgEmailSucces, final String textType) {

		if (this.sessionMail == null) {
			LOGGER.error("No se ha inicializado el servicio"); //$NON-NLS-1$
			return;
		}

		final StringBuilder buffer = new StringBuilder();
		buffer.append(" ======= Enviamos correo electronico ========")
		 .append("\tEmisor: ").append(this.properties.getProperty(ConstantsMail.MAIL_SMTP_MAIL_SENDER))
		 .append("\tReceptores: ").append(Arrays.toString(addresses))
		 .append("\tAsunto: ").append(subject)
		 .append("\tMensaje:\n\t").append(bodyMessage.toString())
		 .append("=============================================");


		Transport transport = null;

	    try {
	    	// Creamos un nuevo MimeMessage para cada correo
            final MimeMessage message = new MimeMessage(this.sessionMail);

	        // Creamos el transport una sola vez
	        transport = this.sessionMail.getTransport(this.properties.getProperty(ConstantsMail.MAIL_PROTOCOL));
	        transport.connect(); // Establecemos la conexion

            // Indicamos el emisor
            message.setFrom(new InternetAddress(this.properties.getProperty(ConstantsMail.MAIL_SMTP_MAIL_SENDER)));

            message.addRecipients(Message.RecipientType.TO, addresses);

            // Establecemos el asunto del correo
            message.setSubject(subject);

            // Establecemos el cuerpo del correo en el formato que se haya indicado
            message.setContent(bodyMessage.toString(), textType);

            // Guardamos los cambios
            message.saveChanges();

            // Enviamos el mensaje
            transport.sendMessage(message, message.getAllRecipients());

            LOGGER.info(msgEmailSucces);
	    } catch (final Exception e) {
	        LOGGER.error("Se ha producido un error al enviar el correo: ", e); //$NON-NLS-1$
	    } finally {
	        // Cerramos el transport solo una vez al final
	        if (transport != null) {
	            try {
	                transport.close();
	            } catch (final MessagingException e) {
	                LOGGER.error("Error al cerrar el transport", e); //$NON-NLS-1$
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
		 * Constructor method.
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

}
