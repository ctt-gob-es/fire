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
 * <b>File:</b><p>es.gob.fire.persistence.configuration.ManagerPersistenceConfigurationServices.java.</p>
 * <b>Description:</b><p>Manager singleton instance for the use of the persistence services
 * of the configuration scheme.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>11/09/2018.</p>
 * @author Gobierno de España.
 * @version 1.0, 17/04/2020.
 */
package es.gob.fire.persistence.service.config;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.gob.fire.persistence.service.IUserService;

/**
 * <p>Manager singleton instance for the use of the persistence services
 * of the configuration scheme.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.3, 17/12/2018.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ManagerPersistenceConfigurationServices {

	/**
	 * Attribute that represents the unique singleton instance of the class.
	 */
	private static ManagerPersistenceConfigurationServices instance = null;

	/**
	 * Gets the unique singleton instance of the class.
	 * @return the unique singleton instance of the class.
	 */
	public static ManagerPersistenceConfigurationServices getInstance() {
		return instance;
	}

	/**
	 * Method that initializes the singleton unique instance.
	 */
	@PostConstruct
	public void init() {
		instance = this;
	}

	/**
	 * Method that destroy the singleton unique instance of this class.
	 */
	@PreDestroy
	public final void destroy() {
		instance = null;

	}
	/**
	 * Attribute that represents the services for the configuration persistence: ValET Users.
	 */
	@Autowired
	private IUserService userFireService;

	
	/**
	 * Gets the value of the attribute {@link #userFireService}.
	 * @return the value of the attribute {@link #userFireService}.
	 */
	public final IUserService getUserFireService() {
		return userFireService;
	}

}