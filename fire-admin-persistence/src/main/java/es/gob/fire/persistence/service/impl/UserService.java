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
 * <b>File:</b><p>es.gob.fire.persistence.service.impl.UserService.java.</p> * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/06/2018.
 */
package es.gob.fire.persistence.service.impl;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import es.gob.fire.persistence.dto.UserDTO;
import es.gob.fire.persistence.dto.UserEditDTO;
import es.gob.fire.persistence.dto.UserPasswordDTO;
import es.gob.fire.persistence.entity.Rol;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.repository.RolRepository;
import es.gob.fire.persistence.repository.UserRepository;
import es.gob.fire.persistence.repository.datatable.UserDataTablesRepository;
import es.gob.fire.persistence.service.IUserService;

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
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private RolRepository rolRepository;

	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private UserDataTablesRepository dtRepository;

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUsertByUserId(java.lang.Long)
	 */
	@Override
	public User getUserByUserId(Long userId) {
		return repository.findByUserId(userId);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#saveUser(es.gob.fire.persistence.entity.User)
	 */
	@Override
	public User saveUser(User user) {
		return repository.save(user);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.monitoriza.service.IUserService#saveUser(es.gob.monitoriza.persistence.configuration.dto.UserDTO)
	 */
	@Override
	@Transactional
	public User saveUser(UserDTO userDto) {
		User user = null;
		if (userDto.getUserId() != null) {
			user = repository.findByUserId(userDto.getUserId());
		} else {
			user = new User();
		}
		if (!StringUtils.isEmpty(userDto.getPassword())) {
			String pwd = userDto.getPassword();
			BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
			String hashPwd = bcpe.encode(pwd);

			user.setPassword(hashPwd);
		}
		
		user.setUserName(userDto.getLogin());
		user.setName(userDto.getName());
		user.setSurnames(userDto.getSurnames());
		user.setEmail(userDto.getEmail());
		user.setStartDate(new Date());
		user.setRol(rolRepository.findByRolId(userDto.getRolId()));
		user.setRenovationDate(new Date());
		user.setRoot(Boolean.FALSE);
		//TODO Rellenar los campos que faltan
		return repository.save(user);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.monitoriza.service.IUserMonitorizaService#updateUserMonitoriza(es.gob.monitoriza.persistence.configuration.dto.UserDTO)
	 */
	@Override
	@Transactional
	public User updateUser(UserEditDTO userDto) {
		
		User user = null;
		
		if (userDto.getIdUserFireEdit() != null) {
			user = repository.findByUserId(userDto.getIdUserFireEdit());
		} else {
			user = new User();
		}
		user.setName(userDto.getNameEdit());
		user.setSurnames(userDto.getSurnamesEdit());
		user.setEmail(userDto.getEmailEdit());
		//TODO Rellenar los campos que faltan

		return repository.save(user);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#deleteUser(java.lang.Long)
	 */
	@Override
	public void deleteUser(Long userId) {
		repository.deleteById(userId);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getAllUse()
	 */
	@Override
	public List<User> getAllUser() {
		return repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUserByUserName(java.lang.String)
	 */
	@Override
	public User getUserByUserName(final String userName) {
		return repository.findByUserName(userName);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUserByRenovationCode(java.lang.String)
	 */
	@Override
	public User getUserByRenovationCode(final String renovationCode) {
		return repository.findByRenovationCode(renovationCode);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUserByUserNameOrEmail(java.lang.String,java.lang.String)
	 */
	@Override
	public User getUserByUserNameOrEmail(final String userName, final String email) {
		return repository.findByUserNameOrEmail(userName, email);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
	@Override
	public DataTablesOutput<User> getAllUser(DataTablesInput input) {
		return dtRepository.findAll(input);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.monitoriza.service.IUserMonitorizaService#changeUserMonitorizaPassword(es.gob.monitoriza.persistence.configuration.dto.UserPasswordDTO)
	 */
	@Override
	@Transactional
	public String changeUserPassword(UserPasswordDTO userPasswordDto) {
		User user = repository.findByUserId(userPasswordDto.getIdUserFirePass());
		String result = null;
		String oldPwd = userPasswordDto.getOldPassword();
		String pwd = userPasswordDto.getPassword();
		BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
		String hashPwd = bcpe.encode(pwd);
		try {
			if (bcpe.matches(oldPwd, user.getPassword())) {
				user.setPassword(hashPwd);
				repository.save(user);
				result = "0";
			} else {
				result = "-1";
			}
		} catch (Exception e) {
			result = "-2";
			throw e;
		}
		return result;	
	}

	@Override
	public List<Rol> getAllRol() {
		
		return rolRepository.findAll();
	}

}
