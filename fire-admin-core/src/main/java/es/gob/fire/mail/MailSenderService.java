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

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Component;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Constants;

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
			message.setFrom(new InternetAddress(super.getMailSmtpMailSender(), super.getMailFromName()));
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
