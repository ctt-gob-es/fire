package es.gob.fire.control.tasks;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import es.gob.fire.mail.ConstantsMail;

@Component
public class ConfigurationMail {

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
     * Obtiene las propiedades de configuraci&oacute;n establecidas en fichero.
     * @return Propiedades de configuraci&oacute;n para el env&iacute;o de correos.
     */
    public Properties getProperties() {

    	final Properties properties = new Properties();
    	properties.put(ConstantsMail.MAIL_SMTP_HOST, this.mailSmtpHost);
    	properties.put(ConstantsMail.MAIL_SMTP_PORT, this.mailSmtpPort);
    	properties.put(ConstantsMail.MAIL_SMTP_MAIL_SENDER, this.mailSmtpMailSender);
    	properties.put(ConstantsMail.MAIL_SMTP_STARTTLS_ENABLE, this.mailSmtpStarttlsEnable);

    	final String starttlsRequired = this.mailSmtpStarttlsRequired;
    	if (!starttlsRequired.isEmpty()) {
    		properties.put(ConstantsMail.MAIL_SMTP_STARTTLS_REQUIRED, starttlsRequired);
    	}

    	final String sslProtocols = this.mailSmtpSslProtocols;
    	if (!sslProtocols.isEmpty()) {
    		properties.put(ConstantsMail.MAIL_SMTP_SSL_PROTOCOLS, sslProtocols);
    	}

    	final String socketFactoryPort = this.mailSmtpSocketFactoryPort;
    	if (!socketFactoryPort.isEmpty()) {
    		properties.put(ConstantsMail.MAIL_SMTP_SOCKETFACTORY_PORT, socketFactoryPort);
    	}

    	properties.put(ConstantsMail.MAIL_PROTOCOL, this.mailProtocol);

    	// Comprobamos si es necesaria autenticacion
    	if (Boolean.parseBoolean(this.mailSmtpAuth)) {
    		properties.put(ConstantsMail.MAIL_SMTP_AUTH, this.mailSmtpAuth);
    		properties.put(ConstantsMail.MAIL_SMTP_USER, this.mailSmtpUser);
    		properties.put(ConstantsMail.MAIL_SMTP_PASSWORD, this.mailSmtpPassword);
    	}

    	return properties;
    }
}
