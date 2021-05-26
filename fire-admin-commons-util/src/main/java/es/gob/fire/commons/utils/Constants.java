/** 
 * <b>File:</b><p>es.gob.fire.commons.utils.Constants.java.</p>
 * <b>Description:</b><p>Class that contains es.gob.fire.constants to use in the project.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>14/05/2020.</p>
 * @version 1.0, 14/05/2020.
 */
package es.gob.fire.commons.utils;

/** 
 * <p>Class that contains es.gob.fire.constants to use in the project.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/05/2020.
 */
public final class Constants {
	
	/**
	 * Attribute that represents the main project package.
	 */
	public static final String MAIN_PROJECT_PACKAGE = "es.gob.fire";
	
	/**
	 * Attribute that represents the main war project package.
	 */
	public static final String MAIN_WEB_PROJECT_PACKAGE = "es.gob.fire.web";
	
	/**
	 * Attribute that represents the main core project package.
	 */
	public static final String MAIN_CORE_PROJECT_PACKAGE = "es.gob.fire.core";
	
	/**
	 * Attribute that represents the main persistence project package.
	 */
	public static final String MAIN_PERSISTENCE_PROJECT_PACKAGE = "es.gob.fire.persistence";
	
	/**
	 * Attribute that represents the main entity project package.
	 */
	public static final String MAIN_ENTITY_PROJECT_PACKAGE = "es.gob.fire.persistence.entity";
	
	/**
	 * Attribute that represents the main repository project package.
	 */
	public static final String MAIN_REPOSITORY_PROJECT_PACKAGE = "es.gob.fire.persistence.repository";
	
	/**
	 * Attribute that represents the messages directory.
	 */
	public static final String MESSAGES = "messages";
	
	/**
	 * Attribute that represents the row index value for the data table in case of error.
	 */
	public static final String ROW_INDEX_ERROR = "-1";
	
	/**
	 * Attribute that represents the administrator role.
	 */
	public static final String ROLE_ADMIN = "Administrator";
	
	/**
	 * Attribute that represents the responsible rol.
	 */
	public static final String ROLE_RESPONSIBLE = "Responsible";
	
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
	 * Attribute that represents the utf-8 property. 
	 */
	public static final String MAIL_TEXT_HTML_CHARSET = "text/html; charset=utf-8";
	
	/**
	 * Attribute that represents the mail subject. 
	 */
	public static final String MAIL_SUBJECT = "Restablecimiento de contraseña de FIRe";
	
	/**
	 * Attribute that represents the text subject. 
	 */
	public static final String MAIL_TEXT = "Hola %%NAME%%," +  "<br>" + "<br>"
					+ "Estás recibiendo este correo porque se hizo una solicitud de recuperación de contraseña para su cuenta de administración de FIRe." + "<br>"
					+ "Recuerde que su usuario es %%USERNAME%%"  + "<br>" + "<br>" + "<br>"
					+ "Para poder restablecer la contraseña acceda a la siguiente URL:"  + "<br>" + "<br>"
					+ "%%URL%%" + "<br>" + "<br>"
					+ "Si usted no realizó esta solicitud, por favor ignore este correo." + "<br>" + "<br>" + "<br>"
					+ "Un saludo,";
	
	/**
	 * Attribute that represents the mail footer. 
	 */
	public static final String MAIL_FOOTER = "El administrador.";
	
	public static final String CORRECTAS = "correctas";
	
	public static final String INCORRECTAS = "incorrectas";
	
	public static final String CORRECTAS_SIMPLE = "correctassimple";
	
	public static final String INCORRECTAS_SIMPLE = "incorrectassimple";
	
	public static final String CORRECTAS_LOTE = "correctaslote";
	
	public static final String INCORRECTAS_LOTE = "incorrectaslote";
	
	public static final String SIZE = "tamaño";
	
	public static final String MB = "Megabyte";
	
	public static final String TRANS_CORRECTAS = "Transacciones";
	
	public static final String TRANS_INCORRECTAS = "Transacciones";
	
	public static final String TRANS_SIMPLES = "simples";
	
	public static final String TRANS_LOTE = "lote";
	
	public static final String FIRMAS_CORRECTAS = "Firmas correctas por";
	
	public static final String FIRMAS_INCORRECTAS = "Firmas incorrectas por";
	
	public static final String QUERYBYTYPE_PROVEEDOR = "por proveedor";
	
	public static final String QUERYBYTYPE_APP = "por aplicación";
	
	public static final String QUERYBYTYPE_FORMATO = "por formato";
	
	public static final String QUERYBYTYPE_FORMATO_LONG = "por formato longevo";
	
	public static final String OTRAS = "Otras";
		
	/**
	 * Constructor method for the class Constants.java.
	 */
	public Constants() {
	}
	
}
