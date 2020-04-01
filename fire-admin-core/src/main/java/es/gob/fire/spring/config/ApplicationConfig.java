/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/** 
 * <b>File:</b><p>es.gob.monitoriza.spring.config.ApplicationConfiguration.java.</p>
 * <b>Description:</b><p>Spring configuration class that sets the configuration of Spring components, entities and repositories.</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>7/03/2018.</p>
 * @author Gobierno de España.
 * @version 1.1, 25/01/2019.
 */
package es.gob.fire.spring.config;

import javax.annotation.PostConstruct;

//import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//import es.gob.monitoriza.i18n.ICoreLogMessages;
//import es.gob.monitoriza.i18n.Language;
//import es.gob.monitoriza.utilidades.UtilsGrayLog;

/** 
 * <p>Spring configuration class that sets the configuration of Spring components, entities and repositories.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.5, 25/01/2019.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("es.gob.fire")
@EntityScan("es.gob.fire.persistence.configuration.model.entity")
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class, basePackages = "es.gob.fire.persistence.configuration.model.repository")
public class ApplicationConfig {
	
	/**
	 * Attribute that represents the logger of this class.
	 */
	//private static Logger logger = Logger.getLogger(ApplicationConfig.class);
	
	
	/**
	 * Method that initializes elements for this class and for the application boot.
	 */
	@PostConstruct
	public void init() {

		try {
			initializePlatform();
			//logger.info(Language.getResCoreMonitoriza(ICoreLogMessages.CORE009));
		} catch (Exception e) {
			//logger.error(Language.getResCoreMonitoriza(ICoreLogMessages.CORE010), e);
		}

	}
	
	/**
	 * Method that initialize all the functions of the platform.
	 */
	private void initializePlatform() {
		
		//logger.info(Language.getResCoreMonitoriza(ICoreLogMessages.CORE008));
		// Inicializamos la conexión con GrayLog si es necesario.
		//UtilsGrayLog.loadGrayLogConfiguration();
		
	}
	
	
}
