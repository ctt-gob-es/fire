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
 * <b>File:</b><p>es.gob.valet.service.impl.UserValetService.java.</p> * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/06/2018.
 */
package es.gob.fire.persistence.service.impl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.model.entity.User;
import es.gob.fire.persistence.model.repository.UserRepository;
import es.gob.fire.persistence.model.repository.datatable.UserDataTablesRepository;
import es.gob.fire.persistence.service.ifaces.IUserService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 15/06/2018.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UserService implements IUserService {

	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private UserRepository repository;

	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private UserDataTablesRepository dtRepository;

	/**
	 * {@inheritDoc}
	 * @see es.gob.valet.persistence.configuration.services.ifaces.IUserValetService#getUserValetById(java.lang.Long)
	 */
	@Override
	public User getUserById(Long id) {
		return repository.findByIdUser(id);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.valet.persistence.configuration.services.ifaces.IUserValetService#saveUserValet(es.gob.valet.persistence.configuration.model.entity.UserValet)
	 */
	@Override
	public User saveUser(User user) {
		return repository.save(user);

	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.valet.persistence.configuration.services.ifaces.IUserValetService#deleteUserValet(java.lang.Long)
	 */
	@Override
	public void deleteUser(Long userId) {
		repository.deleteById(userId);

	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.valet.persistence.configuration.services.ifaces.IUserValetService#getAllUserValet()
	 */
	@Override
	public Iterable<User> getAllUser() {
		return repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.valet.persistence.configuration.services.ifaces.IUserValetService#getUserValetByLogin(java.lang.String)
	 */
	@Override
	public User getUserByUsername(String username) {
		return repository.findByUsername(username);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.valet.persistence.configuration.services.ifaces.IUserValetService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
	@Override
	public DataTablesOutput<User> getAllUser(DataTablesInput input) {

		return dtRepository.findAll(input);
	}

}




