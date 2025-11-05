package es.gob.fire.utils;

public class ConstantsMail {
	
	/**
	 * Attribute that represents the smtp property.
	 */
	public static final String MAIL_PROTOCOL = "mail.protocol";

	/**
	 * Attribute that represents the mail.recipients property.
	 */
	public static final String MAIL_RECIPIENTS = "mail.recipients";

	/**
	 * Attribute that represents the Tmail.smtp.host property.
	 */
	public static final String MAIL_SMTP_HOST = "mail.smtp.host";

	/**
	 * Attribute that represents the mail.smtp.port property.
	 */
	public static final String MAIL_SMTP_PORT = "mail.smtp.port";

	/**
	 * Attribute that represents the mail.smtp.mail.sender property.
	 */
	public static final String MAIL_SMTP_MAIL_SENDER = "mail.smtp.mail.sender";

	/**
	 * Attribute that represents the mail.smtp.starttls.enable property.
	 */
	public static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

	/**
	 * Attribute that represents the mail.smtp.starttls.required property.
	 */
	public static final String MAIL_SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";

	/**
	 * Attribute that represents the mail.smtp.ssl.protocols property.
	 */
	public static final String MAIL_SMTP_SSL_PROTOCOLS = "mail.smtp.ssl.protocols";

	/**
	 * Attribute that represents the mail.smtp.socketFactory.port property.
	 */
	public static final String MAIL_SMTP_SOCKETFACTORY_PORT = "mail.smtp.socketFactory.port";

	/**
	 * Attribute that represents the mail.smtp.user property.
	 */
	public static final String MAIL_SMTP_USER = "mail.smtp.user";

	/**
	 * Attribute that represents the mail.smtp.password property.
	 */
	public static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";

	/**
	 * Attribute that represents the mail.smtp.auth property.
	 */
	public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones CRITICAL
	 * para enviar un correo.
	 */
	public static final String MAIL_CRITICAL_NOTIFY_LIMIT = "mail.critical.notify.limit"; //$NON-NLS-1

	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones ERROR
	 * para enviar un correo.
	 */
	public static final String MAIL_ERROR_NOTIFY_LIMIT = "mail.error.notify.limit"; //$NON-NLS-1$
	
	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones WARNING
	 * para enviar un correo.
	 */
	public static final String MAIL_WARNING_NOTIFY_LIMIT = "mail.warning.notify.limit"; //$NON-NLS-1$
	
	/**
	 * Nombre de la propiedad de configuraci&oacute;n que determina el limite de notificaciones INFO
	 * para enviar un correo.
	 */
	public static final String MAIL_INFO_NOTIFY_LIMIT = "mail.info.notify.limit"; //$NON-NLS-1$

	/**
	 * Intervalo de tiempo en el que se comprobar&aacute; cuantas alarmas hay registradas.
	 */
	public static final String MAIL_NOTIFY_DELAY = "mail.notify.delay.time"; //$NON-NLS-1$

	/**
	 * Asunto del correo a enviar.
	 */
	public static final String MAIL_SUBJECT = "mail.subject"; //$NON-NLS-1$

	/**
	 * Entorno desde el que se notifica.
	 */
	public static final String MAIL_ENVIRONMENT = "mail.environment"; //$NON-NLS-1$
}
