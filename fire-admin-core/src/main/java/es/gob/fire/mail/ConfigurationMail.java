package es.gob.fire.mail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.gob.fire.commons.utils.Constants;

@Component
public class ConfigurationMail {

	/**
	 * Attribute that represents the default expired time.
	 */
	public static final int DEFAULT_EXPIRED_TIME = 1800000;

	/**
	 * Attribute that represents file mail property.
	 */
	public static Properties properties = new Properties();

	/**
	 * Attribute that represents the session mail.
	 */
	public static Session sessionMail;

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
	public ConfigurationMail() {
	}
	
	/**
	 * Method that configures the send mail.
	 * @throws FileNotFoundException file not found
	 * @throws IOException error load file
	 */
	public void init() throws FileNotFoundException, IOException {
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

	/**
	 * Gets mailSmtpHost.
	 * @return mailSmtpHost
	 */
	public String getMailSmtpHost() {
		return mailSmtpHost;
	}

	/**
	 * Sets mailSmtpHost
	 * @param mailSmtpHost
	 */
	public void setMailSmtpHost(String mailSmtpHost) {
		this.mailSmtpHost = mailSmtpHost;
	}

	/**
	 * Gets mailSmtpPort.
	 * @return mailSmtpPort
	 */
	public String getMailSmtpPort() {
		return mailSmtpPort;
	}

	/**
	 * Sets mailSmtpPort
	 * @param mailSmtpPort
	 */
	public void setMailSmtpPort(String mailSmtpPort) {
		this.mailSmtpPort = mailSmtpPort;
	}

	/**
	 * Gets mailProtocol.
	 * @return mailProtocol
	 */
	public String getMailProtocol() {
		return mailProtocol;
	}

	/**
	 * Sets mailProtocol
	 * @param mailProtocol
	 */
	public void setMailProtocol(String mailProtocol) {
		this.mailProtocol = mailProtocol;
	}

	/**
	 * Gets mailSmtpMailSender.
	 * @return mailSmtpMailSender
	 */
	public String getMailSmtpMailSender() {
		return mailSmtpMailSender;
	}

	/**
	 * Sets mailSmtpMailSender
	 * @param mailSmtpMailSender
	 */
	public void setMailSmtpMailSender(String mailSmtpMailSender) {
		this.mailSmtpMailSender = mailSmtpMailSender;
	}

	/**
	 * Gets mailSmtpStarttlsEnable.
	 * @return mailSmtpStarttlsEnable
	 */
	public String getMailSmtpStarttlsEnable() {
		return mailSmtpStarttlsEnable;
	}

	/**
	 * Sets mailSmtpStarttlsEnable
	 * @param mailSmtpStarttlsEnable
	 */
	public void setMailSmtpStarttlsEnable(String mailSmtpStarttlsEnable) {
		this.mailSmtpStarttlsEnable = mailSmtpStarttlsEnable;
	}

	/**
	 * Gets mailSmtpStarttlsRequired.
	 * @return mailSmtpStarttlsRequired
	 */
	public String getMailSmtpStarttlsRequired() {
		return mailSmtpStarttlsRequired;
	}

	/**
	 * Sets mailSmtpStarttlsRequired
	 * @param mailSmtpStarttlsRequired
	 */
	public void setMailSmtpStarttlsRequired(String mailSmtpStarttlsRequired) {
		this.mailSmtpStarttlsRequired = mailSmtpStarttlsRequired;
	}

	/**
	 * Gets mailSmtpSslProtocols.
	 * @return mailSmtpSslProtocols
	 */
	public String getMailSmtpSslProtocols() {
		return mailSmtpSslProtocols;
	}

	/**
	 * Sets mailSmtpSslProtocols
	 * @param mailSmtpSslProtocols
	 */
	public void setMailSmtpSslProtocols(String mailSmtpSslProtocols) {
		this.mailSmtpSslProtocols = mailSmtpSslProtocols;
	}

	/**
	 * Gets mailSmtpSocketFactoryPort.
	 * @return mailSmtpSocketFactoryPort
	 */
	public String getMailSmtpSocketFactoryPort() {
		return mailSmtpSocketFactoryPort;
	}

	/**
	 * Sets mailSmtpSocketFactoryPort
	 * @param mailSmtpSocketFactoryPort
	 */
	public void setMailSmtpSocketFactoryPort(String mailSmtpSocketFactoryPort) {
		this.mailSmtpSocketFactoryPort = mailSmtpSocketFactoryPort;
	}

	/**
	 * Gets mailSmtpUser.
	 * @return mailSmtpUser
	 */
	public String getMailSmtpUser() {
		return mailSmtpUser;
	}

	/**
	 * Sets mailSmtpUser
	 * @param mailSmtpUser
	 */
	public void setMailSmtpUser(String mailSmtpUser) {
		this.mailSmtpUser = mailSmtpUser;
	}

	/**
	 * Gets mailSmtpPassword.
	 * @return mailSmtpPassword
	 */
	public String getMailSmtpPassword() {
		return mailSmtpPassword;
	}

	/**
	 * Sets mailSmtpPassword
	 * @param mailSmtpPassword
	 */
	public void setMailSmtpPassword(String mailSmtpPassword) {
		this.mailSmtpPassword = mailSmtpPassword;
	}

	/**
	 * Gets mailSmtpAuth.
	 * @return mailSmtpAuth
	 */
	public String getMailSmtpAuth() {
		return mailSmtpAuth;
	}

	/**
	 * Sets mailSmtpAuth.
	 * @param mailSmtpAuth
	 */
	public void setMailSmtpAuth(String mailSmtpAuth) {
		this.mailSmtpAuth = mailSmtpAuth;
	}

	/**
	 * Gets mailFromName.
	 * @return mailFromName
	 */
	public String getMailFromName() {
		return mailFromName;
	}

	/**
	 * Sets mailFromName.
	 * @param mailFromName
	 */
	public void setMailFromName(String mailFromName) {
		this.mailFromName = mailFromName;
	}
}
