/**
 * <b>File:</b><p>es.gob.fire.core.util.MailSenderService.java.</p>
 * <b>Description:</b><p>Class that manages the emails.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electronica para el Ambito Judicial.</p>
 * <b>Date:</b><p>8 mar. 2019.</p>
 * @author Consejería de Justicia e Interior de la Junta de Andalucía.
 * @version 1.2, 24/02/2025.
 */
package es.gob.fire.mail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Component;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.utils.ConstantsMail;

/**
 * <p>Class that manages the emails.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electronica para el Ambito Judicial.</p>
 * @version 1.2, 24/02/2025.
 */
@Component
public class MailSenderService extends ConfigurationMail {

	private static final Logger LOGGER = Logger.getLogger(MailSenderService.class);

	/**
	 * Constructor method for the class EmailSenderService.java.
	 */
	public MailSenderService() {
		super();
	}
	
	/**
	 * Attribute that represents the utf-8 property.
	 */
	public static final String MAIL_TEXT_PLAIN_CHARSET = "text/plain; charset=UTF-8";
	
	public void sendEmail(Address[] addresses, String subject, StringBuilder bodySubject, String msgEmailSucces) {
		Transport transport = null;

	    try {
	    	// Creamos un nuevo MimeMessage para cada correo
            final MimeMessage message = new MimeMessage(sessionMail);
	    	
	        // Creamos el transport una sola vez
	        transport = sessionMail.getTransport(properties.getProperty(ConstantsMail.MAIL_PROTOCOL));
	        transport.connect(); // Establecemos la conexión

            // Indicamos el emisor
            message.setFrom(new InternetAddress((String) properties.get(ConstantsMail.MAIL_SMTP_MAIL_SENDER)));

            // Establecemos los destinatarios
            message.addRecipients(Message.RecipientType.TO, addresses);

            // Establecemos el asunto del correo
            message.setSubject(subject);

            // Establecemos el cuerpo del correo en formato HTML
            message.setContent(bodySubject.toString(), MAIL_TEXT_PLAIN_CHARSET);

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
