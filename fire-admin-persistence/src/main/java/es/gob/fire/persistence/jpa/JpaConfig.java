/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
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
 * <b>File:</b><p>es.gob.fire.persistence.jpa.JpaConfig.java.</p>
 * <b>Description:</b><p>Class that manages the data base configuration.</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>01/04/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 01/04/2020.
 */
package es.gob.fire.persistence.jpa;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.StringUtils;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.FileUtilsDirectory;
import es.gob.fire.commons.utils.UtilsServer;

/**
 * <p>Class that manages the data base configuration.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 01/04/2020.
 */


@Configuration
@ComponentScan(Constants.MAIN_PROJECT_PACKAGE)
@EntityScan(Constants.MAIN_ENTITY_PROJECT_PACKAGE)
@EnableTransactionManagement
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class, basePackages = Constants.MAIN_REPOSITORY_PROJECT_PACKAGE)
public class JpaConfig {

	/**
	 * Constant attribute that represents the file name of the configuration
	 * properties for configure MySql strategy identity.
	 */
	private static final String CONF_IDENTITY_MYSQL = "mysql-orm.xml";

	/**
	 * Constant attribute that represents the MySql dialect.
	 */
	private static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQLDialect";
	
	/**
	 * Attribute that represents the datasource JNDI name.
	 */
	@Value("${datasource.jndi-name}")
	private String jndiName;
	
	/**
	 * Attribute that represents the dialect data base.
	 */
	@Value("${hibernate.dialect}")
	private String dialect;
	/**
	 * Attribute that shows SQL (true), or not (false).
	 */
	@Value("${hibernate.showSQL}")
	private boolean showSQL;

	/**
	 * Method that configures the data source.
	 * @return DataSource
	 * @throws IOException 
	 */
	@Bean
	public DataSource configureDataSource() {
		JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
		bean.setJndiName(this.jndiName);
		bean.setProxyInterface(DataSource.class);
		try {
			bean.afterPropertiesSet();
		} catch (IllegalArgumentException | NamingException e) {
			//Lanzar excepcion
		}
        return (DataSource) bean.getObject();
	}

	/**
	 * Method that configures the entity manager factory.
	 *
	 * @return EntityManagerFactory
	 */
	@Bean(name = "entityManagerFactory")
	public EntityManagerFactory configureEntityManagerFactory() throws SQLException {
		final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(configureDataSource());
		entityManagerFactoryBean.setPackagesToScan(Constants.MAIN_PERSISTENCE_PROJECT_PACKAGE);
		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		final Properties jpaProperties = new Properties();
		if (StringUtils.equals(this.dialect, MYSQL_DIALECT)) {
			if (UtilsServer.getServerConfigDir() != null) {
				entityManagerFactoryBean.setMappingResources("file:///"+FileUtilsDirectory.createAbsolutePath(UtilsServer.getServerConfigDir(), CONF_IDENTITY_MYSQL));
			}
			else {
				entityManagerFactoryBean.setMappingResources("/" + CONF_IDENTITY_MYSQL);
			}
			jpaProperties.put(AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, Boolean.TRUE);
			//jpaProperties.put("spring.jpa.hibernate.naming.implicit-strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl");
			//jpaProperties.put("spring.jpa.hibernate.naming.physical-strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
		}
		jpaProperties.put(AvailableSettings.DIALECT, this.dialect);
		jpaProperties.put(AvailableSettings.SHOW_SQL, this.showSQL);

		entityManagerFactoryBean.setJpaProperties(jpaProperties);
		entityManagerFactoryBean.afterPropertiesSet();

		return entityManagerFactoryBean.getObject();
	}

	/**
	 * Method that registers a property.
	 *
	 * @return PlatformTransactionManager
	 */
	@Bean(name = "transactionManager")
	public PlatformTransactionManager annotationDrivenTransactionManager() throws SQLException {
		return new JpaTransactionManager(configureEntityManagerFactory());
	}

	/**
	 * Method that registers a property.
	 *
	 * @return HibernateExceptionTranslator
	 */
	@Bean(name = "hibernateExceptionTranslator")
	public HibernateExceptionTranslator hibernateExceptionTranslator() {
		return new HibernateExceptionTranslator();
	}

	/**
	 * Gets the value of the attribute {@link #dialect}.
	 * @return the value of the attribute {@link #dialect}.
	 */
	public String getDialect() {
		return this.dialect;
	}

	/**
	 * Sets the value of the attribute {@link #dialect}.
	 * @param dialectP The value for the attribute {@link #dialect}.
	 */
	public void setDialect(final String dialectP) {
		this.dialect = dialectP;
	}

	/**
	 * Gets the value of the attribute {@link #showSQL}.
	 * @return the value of the attribute {@link #showSQL}.
	 */
	public boolean isShowSQL() {
		return this.showSQL;
	}

	/**
	 * Sets the value of the attribute {@link #showSQL}.
	 * @param showSQLP The value for the attribute {@link #showSQL}.
	 */
	public void setShowSQL(final boolean showSQLP) {
		this.showSQL = showSQLP;
	}

}