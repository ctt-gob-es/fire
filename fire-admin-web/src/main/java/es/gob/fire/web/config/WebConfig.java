
/** 
 * <b>File:</b><p>es.juntadeandalucia.justicia.biosign.integrationserver.war.config.WebServiceMvcConfig.java.</p>
 * <b>Description:</b><p>Class that registers the different beans used in the application.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * <b>Date:</b><p>14 oct. 2019.</p>
 * @author Consejería de Turismo, Regeneración, Justicia y Administración Local de la Junta de Andalucía.
 * @version 1.0, 14 oct. 2019.
 */
package es.gob.fire.web.config;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.FileUtilsDirectory;
import es.gob.fire.commons.utils.UtilsServer;

/** 
 * <p>Class that registers the different beans used in the application.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * @version 1.0, 14 oct. 2019.
 */
@Configuration
@ComponentScan(basePackages = { Constants.MAIN_PERSISTENCE_PROJECT_PACKAGE, Constants.MAIN_WEB_PROJECT_PACKAGE }, useDefaultFilters = true)
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Constant attribute that represents the file name of the configuration
	 * properties for configure persistence.
	 */
	private static final String PROPS_CONF_FILE_PERSISTENCE = "persistence.properties";

	/**
	 * Constant attribute that represents the file name of the configuration
	 * properties for configure the mail.
	 */
	private static final String PROPS_CONF_FILE_MAIL = "mail.properties";
	
	/**
	 * Attribute that represents the messages path.
	 */
	private static final String MESSAGE_SOURCE = "classpath:messages/i18n/messages";

	/**
	 * Method that registers a property.
	 * 
	 * @return PropertySourcesPlaceholderConfigurer
	 */

	@Bean
	public MessageSource messageSource() {
		final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename(MESSAGE_SOURCE);
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setUseCodeAsDefaultMessage(Boolean.TRUE);
		return messageSource;
	}

	@Bean
	public LocaleResolver localeResolver() {
		final SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
		sessionLocaleResolver.setDefaultLocale(new Locale("es", "ES"));
		return sessionLocaleResolver;
	}

}
