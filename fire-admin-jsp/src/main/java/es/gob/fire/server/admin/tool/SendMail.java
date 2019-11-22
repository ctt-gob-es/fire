package es.gob.fire.server.admin.tool;

import java.util.ArrayList;
import java.util.List;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.Recipient;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import es.gob.fire.server.admin.conf.ConfigManager;

/**
 * Clase para el env&oacute;o de correos electr&oacute;nicos.
 */
public class SendMail {

	/**
	 * Env&iacute;a un correo electronico utilizando los par&aacute;metros establecidos en el fichero
	 * de configuraci&oacute;n.
	 * @param receivers Listado de destinatarios.
	 * @param subject Asunto del correo.
	 * @param body Cuerpo del correo.
	 */
	public static void sendMail(final List<String> receivers, final String subject, final String body) {

		final List<Recipient> recipients = new ArrayList<>();
		for (final String receiver : receivers) {
			recipients.add(new Recipient(null, receiver, javax.mail.Message.RecipientType.TO));
		}

		final Email email = EmailBuilder.startingBlank()
				.from(ConfigManager.getMailFromName(), ConfigManager.getMailFromAddress())
				.to(recipients)
				.withSubject(subject)
				.withHTMLText(body)
				.buildEmail();

		final Mailer mailer = MailerBuilder
				.withSMTPServer(ConfigManager.getMailHost(), ConfigManager.getMailPort())
				.withSMTPServerUsername(ConfigManager.getMailUsername())
				.withSMTPServerPassword(ConfigManager.getMailPassword())
				.withTransportStrategy(TransportStrategy.SMTP_TLS)
				//.clearEmailAddressCriteria() // desactiva la validacion de correo electronico
				.buildMailer();

		mailer.sendMail(email);
	}

}
