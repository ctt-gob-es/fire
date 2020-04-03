
/** 
 * <b>File:</b><p>es.juntadeandalucia.justicia.biosign.integrationserver.war.config.WebServiceMvcConfig.java.</p>
 * <b>Description:</b><p>Class that registers the different beans used in the application.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * <b>Date:</b><p>14 oct. 2019.</p>
 * @author Consejería de Turismo, Regeneración, Justicia y Administración Local de la Junta de Andalucía.
 * @version 1.0, 14 oct. 2019.
 */
package es.gob.fire.web.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import es.gob.fire.core.constant.Constants;
import es.gob.fire.web.util.FileUtilsDirectory;
import es.gob.fire.web.util.UtilsServer;

/** 
 * <p>Class that registers the different beans used in the application.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * @version 1.0, 14 oct. 2019.
 */
@Configuration
@ComponentScan(basePackages = { Constants.MAIN_PERSISTENCE_PROJECT_PACKAGE }, useDefaultFilters = true)
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Constant attribute that represents the file name of the configuration
	 * properties for configure persistence.
	 */
	private static final String PROPS_CONF_FILE_PERSISTENCE = "persistence.properties";
	
	/**
	 * Method that registers a property.
	 * 
	 * @return PropertySourcesPlaceholderConfigurer
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		propertySourcesPlaceholderConfigurer.setLocations(new FileSystemResource(FileUtilsDirectory.createAbsolutePath(UtilsServer.getServerConfigDir(), PROPS_CONF_FILE_PERSISTENCE)));
		return propertySourcesPlaceholderConfigurer;
	}

}
