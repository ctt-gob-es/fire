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
 * <b>File:</b><p>es.gob.fire.persistence.configuration.jpa.JpaConfig.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>01/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 01/04/2020.
 */
package es.gob.fire.persistence.jpa;

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import es.gob.fire.commons.utils.Constants;
/** 
 * <p>Class .</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 01/04/2020.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(Constants.MAIN_PROJECT_PACKAGE)
@EntityScan(Constants.MAIN_ENTITY_PROJECT_PACKAGE)
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class, basePackages = Constants.MAIN_REPOSITORY_PROJECT_PACKAGE)
public class JpaConfig {

	/**
	 * Attribute that represents the driver data base.
	 */
//	@Value("${datasource.driver}")
	private String driver = "com.mysql.cj.jdbc.Driver";
	/**
	 * Attribute that represents the URL data base.
	 */
//	@Value("${datasource.url}")
	private String url = "jdbc:mysql://127.0.0.1:3306/fire_db";
	/**
	 * Attribute that represents the user data base.
	 */
//	@Value("${datasource.user}")
	private String user = "fire";
	/**
	 * Attribute that represents the user pass data base.
	 */
//	@Value("${datasource.pass}")
	private String pass = "1111" ;
	/**
	 * Attribute that represents the dialect data base.
	 */
//	@Value("${hibernate.dialect}")
	private String dialect = "org.hibernate.dialect.MySQLDialect";
	/**
	 * Attribute that shows SQL (true), or not (false).
	 */
	
//	@Value("${hibernate.showSQL}")
//	private boolean showSQL = true;

	/**
	 * Method that configures the data source.
	 * @return DataSource
	 */
	@Bean
	public DataSource configureDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(user);
		dataSource.setPassword(pass);
		return dataSource;
	}

	/**
	 * Method that configures the entity manager factory.
	 * 
	 * @return EntityManagerFactory
	 */
	@Bean(name = "entityManagerFactory")
	public EntityManagerFactory configureEntityManagerFactory() throws SQLException {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(configureDataSource());
		entityManagerFactoryBean.setPackagesToScan("es.gob.fire.persistence");
		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		Properties jpaProperties = new Properties();
		jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect);
		//jpaProperties.put(org.hibernate.cfg.Environment.SHOW_SQL, showSQL);
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
	public PlatformTransactionManager annotationDrivenTransactionManager() throws SQLException  {
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
	 * Gets the value of the attribute {@link #driver}.
	 * @return the value of the attribute {@link #driver}.
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Sets the value of the attribute {@link #driver}.
	 * @param driverP The value for the attribute {@link #driver}.
	 */
	public void setDriver(final String driverP) {
		this.driver = driverP;
	}
	
	/**
	 * Gets the value of the attribute {@link #url}.
	 * @return the value of the attribute {@link #url}.
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Sets the value of the attribute {@link #url}.
	 * @param urlP The value for the attribute {@link #url}.
	 */
	public void setUrl(final String urlP) {
		this.url = urlP;
	}
	
	/**
	 * Gets the value of the attribute {@link #user}.
	 * @return the value of the attribute {@link #user}.
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Sets the value of the attribute {@link #user}.
	 * @param userP The value for the attribute {@link #user}.
	 */
	public void setUser(final String userP) {
		this.user = userP;
	}
	
	/**
	 * Gets the value of the attribute {@link #pass}.
	 * @return the value of the attribute {@link #pass}.
	 */
	public String getPass() {
		return pass;
	}
	
	/**
	 * Sets the value of the attribute {@link #pass}.
	 * @param passP The value for the attribute {@link #pass}.
	 */
	public void setPass(final String passP) {
		this.pass = passP;
	}
	
	/**
	 * Gets the value of the attribute {@link #dialect}.
	 * @return the value of the attribute {@link #dialect}.
	 */
	public String getDialect() {
		return dialect;
	}
	
	/**
	 * Sets the value of the attribute {@link #dialect}.
	 * @param dialectP The value for the attribute {@link #dialect}.
	 */
	public void setDialect(final String dialectP) {
		this.dialect = dialectP;
	}

//	/**
//	 * Gets the value of the attribute {@link #showSQL}.
//	 * @return the value of the attribute {@link #showSQL}.
//	 */
//	public boolean isShowSQL() {
//		return showSQL;
//	}
//	
//	/**
//	 * Sets the value of the attribute {@link #showSQL}.
//	 * @param showSQLP The value for the attribute {@link #showSQL}.
//	 */
//	public void setShowSQL(final boolean showSQLP) {
//		this.showSQL = showSQLP;
//	}
//	
}
