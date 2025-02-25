package es.gob.fire.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.utils.ConstantsJavaMail;
import es.gob.fire.utils.ConstantsJavaMailFromBD;

@Component
public class ConfigurationMail {

	private static final Logger LOGGER = Logger.getLogger(ConfigurationMail.class);
	
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
     * Attribute that represents the mail host.
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
    protected String mailSmtpMailSender;
	
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
     * Configures JavaMail properties using environment variables or external configuration.
     * <p>
     * This method initializes the mail properties and sets up an authenticated or non-authenticated 
     * mail session based on the configuration. If authentication is required, it creates a secure 
     * session with the provided credentials.
     * </p>
     * In case of an error, it logs the failure.
     */
    public void confJavaMailFromFromEnv() {
    	try {
    		properties.put(ConstantsJavaMail.MAIL_SMTP_HOST, mailSmtpHost);
            properties.put(ConstantsJavaMail.MAIL_SMTP_PORT, mailSmtpPort);
            properties.put(ConstantsJavaMail.MAIL_SMTP_MAIL_SENDER, mailSmtpMailSender);
            properties.put(ConstantsJavaMail.MAIL_SMTP_STARTTLS_ENABLE, mailSmtpStarttlsEnable);

            String starttlsRequired = mailSmtpStarttlsRequired;
            if (!starttlsRequired.isEmpty()) {
                properties.put(ConstantsJavaMail.MAIL_SMTP_STARTTLS_REQUIRED, starttlsRequired);
            }

            String sslProtocols = mailSmtpSslProtocols;
            if (!sslProtocols.isEmpty()) {
                properties.put(ConstantsJavaMail.MAIL_SMTP_SSL_PROTOCOLS, sslProtocols);
            }

            String socketFactoryPort = mailSmtpSocketFactoryPort;
            if (!socketFactoryPort.isEmpty()) {
                properties.put(ConstantsJavaMail.MAIL_SMTP_SOCKETFACTORY_PORT, socketFactoryPort);
            }

            properties.put(ConstantsJavaMail.MAIL_PROTOCOL, mailProtocol);

            // Comprobamos si es necesaria autenticación
            String mailSmtpAuth = this.mailSmtpAuth;
            if ("true".equalsIgnoreCase(mailSmtpAuth)) {
                properties.put(ConstantsJavaMail.MAIL_SMTP_AUTH, mailSmtpAuth);
                properties.put(ConstantsJavaMail.MAIL_SMTP_USER, mailSmtpUser);
                properties.put(ConstantsJavaMail.MAIL_SMTP_PASSWORD, mailSmtpPassword);

                // Obtenemos la sesión con seguridad
                final Authenticator smtpAuthenticator = new SmtpAuthenticator(
                	mailSmtpUser,
                	mailSmtpPassword
                );
                sessionMail = Session.getInstance(properties, smtpAuthenticator);
            } else {
                // Obtenemos la sesión sin seguridad
                sessionMail = Session.getDefaultInstance(properties);
            }
    	} catch (Exception e) {
			LOGGER.error("Se ha producido un fallo al cargar alguna propiedad procedente del properties externo", e);
		}
        
    }
	
    /**
     * Configures JavaMail properties using values retrieved from the database.
     * <p>
     * This method initializes the mail properties based on the provided {@link Properties} object,
     * which contains values fetched from the database. If authentication is required, it sets up 
     * a secure mail session with the given credentials.
     * </p>
     * In case of an error, it logs the failure.
     *
     * @param propertiesFromBD A {@link Properties} object containing mail configuration from the database.
     */
	public void confJavaMailFromBD(Properties propertiesFromBD) {
		try {
			properties.put(ConstantsJavaMail.MAIL_SMTP_HOST, propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_HOST_BD));
	        properties.put(ConstantsJavaMail.MAIL_SMTP_PORT, propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_PORT_BD));
	        this.mailSmtpMailSender = propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_MAIL_SENDER_BD);
	        properties.put(ConstantsJavaMail.MAIL_SMTP_MAIL_SENDER, propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_MAIL_SENDER_BD));
	        properties.put(ConstantsJavaMail.MAIL_SMTP_STARTTLS_ENABLE, propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_STARTTLS_ENABLE_BD));

	        String starttlsRequired = propertiesFromBD.getProperty(propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_STARTTLS_REQUIRED_BD), "");
	        if (!starttlsRequired.isEmpty()) {
	            properties.put(ConstantsJavaMail.MAIL_SMTP_STARTTLS_REQUIRED, starttlsRequired);
	        }

	        String sslProtocols = propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_SSL_PROTOCOLS_BD, "");
	        if (!sslProtocols.isEmpty()) {
	            properties.put(ConstantsJavaMail.MAIL_SMTP_SSL_PROTOCOLS, sslProtocols);
	        }

	        String socketFactoryPort = propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_SOCKETFACTORY_PORT_BD, "");
	        if (!socketFactoryPort.isEmpty()) {
	            properties.put(ConstantsJavaMail.MAIL_SMTP_SOCKETFACTORY_PORT, socketFactoryPort);
	        }

	        properties.put(ConstantsJavaMail.MAIL_PROTOCOL, propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_PROTOCOL_BD));

	        // Comprobamos si es necesaria autenticación
	        String mailSmtpAuth = propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_AUTH_BD);
	        if ("true".equalsIgnoreCase(mailSmtpAuth)) {
	            properties.put(ConstantsJavaMail.MAIL_SMTP_AUTH, mailSmtpAuth);
	            properties.put(ConstantsJavaMail.MAIL_SMTP_USER, propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_USER_BD));
	            properties.put(ConstantsJavaMail.MAIL_SMTP_PASSWORD, propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_PASSWORD_BD));

	            // Obtenemos la sesión con seguridad
	            final Authenticator smtpAuthenticator = new SmtpAuthenticator(
	            	propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_USER_BD),
	            	propertiesFromBD.getProperty(ConstantsJavaMailFromBD.MAIL_SMTP_PASSWORD_BD)
	            );
	            sessionMail = Session.getInstance(properties, smtpAuthenticator);
	        } else {
	            // Obtenemos la sesión sin seguridad
	            sessionMail = Session.getDefaultInstance(properties);
	        }
		} catch (Exception e) {
			LOGGER.error("Se ha producido un fallo al cargar alguna propiedad procedente del properties", e);
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
	
}
