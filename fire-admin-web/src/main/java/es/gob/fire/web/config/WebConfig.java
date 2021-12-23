/**
 * <b>File:</b><p>es.gob.fire.web.config.WebConfig.java.</p>
 * <b>Description:</b><p>Class that registers the different beans used in the application.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>14/05/2020.</p>
 * @version 1.0, 14/05/2020
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
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14/05/2020
 */
@Configuration
@ComponentScan(basePackages = { Constants.MAIN_PERSISTENCE_PROJECT_PACKAGE, Constants.MAIN_WEB_PROJECT_PACKAGE }, useDefaultFilters = true)
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Constant attribute that represents the file name of the configuration
	 * properties for configure.
	 */
	private static final String PROPS_CONF_FILE_GENERAL = "admin_config.properties";

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
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		propertySourcesPlaceholderConfigurer.setLocations(new FileSystemResource(FileUtilsDirectory.createAbsolutePath(UtilsServer.getServerConfigDir(), PROPS_CONF_FILE_GENERAL)));
		return propertySourcesPlaceholderConfigurer;
	}

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
