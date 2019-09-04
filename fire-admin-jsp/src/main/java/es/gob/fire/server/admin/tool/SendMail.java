package es.gob.fire.server.admin.tool;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.Recipient;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import es.gob.fire.server.admin.conf.ConfigManager;




public class SendMail {

	/**
	 *
	 * Atributo que representa el objeto que administra el registro de la clase.
	 * @throws UnsupportedEncodingException
	 */
	private static final Logger LOGGER = Logger.getLogger(SendMail.class.getName());

	public static void sendMail(final List<String> receivers, final String subject, final String body)
   		 {

    	final List<Recipient> recipients = new ArrayList<>();
    	for (final String receiver : receivers) {
    		recipients.add(new Recipient(null, receiver, javax.mail.Message.RecipientType.TO));
    	}

    	final Email email = EmailBuilder.startingBlank()
    		    .from(ConfigManager.getMailUsername(), ConfigManager.getMailFrom())
    		    .to(recipients)
    		    .withSubject(subject)
    		    .withHTMLText(body)
    		    .buildEmail();

    	final Mailer mailer = MailerBuilder
    	          .withSMTPServer(ConfigManager.getMailHost(), ConfigManager.getMailPort(), ConfigManager.getMailUsername(), ConfigManager.getMailPassword())
    	          .withTransportStrategy(TransportStrategy.SMTP_TLS)
    	          .withSessionTimeout((int) ConfigManager.getExpiration())
    	          .clearEmailAddressCriteria() // turns off email validation
    	          //.withProperty("mail.smtp.sendpartial", true) //$NON-NLS-1$
    	          .buildMailer();

    	mailer.sendMail(email);
    }

}
