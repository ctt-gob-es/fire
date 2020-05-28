/** 
 * <b>File:</b><p>es.juntadeandalucia.justicia.biosign.integrationserver.core.constant.Constants.java.</p>
 * <b>Description:</b><p>Class that contains constants to use in the project.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * <b>Date:</b><p>14 oct. 2019.</p>
 * @author Consejería de Turismo, Regeneración, Justicia y Administración Local de la Junta de Andalucía.
 * @version 1.0, 14 oct. 2019.
 */
package es.gob.fire.commons.utils;

import es.gob.fire.commons.utils.UtilsStringChar;

/** 
 * <p>Class that contains constants to use in the project.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * @version 1.0, 14 oct. 2019.
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
	
	/**
	 * Constructor method for the class Constants.java.
	 */
	private Constants() {
	}
	
}
