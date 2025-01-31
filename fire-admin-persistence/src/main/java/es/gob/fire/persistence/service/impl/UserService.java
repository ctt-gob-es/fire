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
 * <b>File:</b><p>es.gob.fire.persistence.service.impl.UserService.java.</p> *
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>21/06/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.3, 20/01/2025.
 */
package es.gob.fire.persistence.service.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Base64;
import es.gob.fire.persistence.dto.UserDTO;
import es.gob.fire.persistence.dto.UserEditDTO;
import es.gob.fire.persistence.dto.UserPasswordDTO;
import es.gob.fire.persistence.entity.Rol;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.permissions.Permissions;
import es.gob.fire.persistence.permissions.PermissionsChecker;
import es.gob.fire.persistence.repository.RolRepository;
import es.gob.fire.persistence.repository.UserRepository;
import es.gob.fire.persistence.repository.datatable.UserDataTablesRepository;
import es.gob.fire.persistence.service.IUserService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.3, 20/01/2025.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UserService implements IUserService {

	/** The Constant LOG. */
	private static final Logger LOGGER = Logger.getLogger(UserService.class);

	/**
	 * Attribute that represents the default charset.
	 */
	private static final String DEFAULT_CHARSET = "utf-8"; //$NON-NLS-1$

	/**
	 * Attribute that represents the md algorithm.
	 */
	private static final String MD_ALGORITHM = "SHA-256"; //$NON-NLS-1$

	/**
	 * Constant attribute that represents the value of the administrator permission.
	 */
	private static final String ROLE_ADMIN_PERMISSON = "1"; //$NON-NLS-1$

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
	public User getUserByUserId(final Long userId) {
		return this.repository.findByUserId(userId);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#saveUser(es.gob.fire.persistence.entity.User)
	 */
	@Override
	public User saveUser(final User user) {
		return this.repository.save(user);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.IUserService#saveUser(es.gob.fire.persistence.configuration.dto.UserDTO)
	 */
	@Override
	@Transactional
	public User saveUser(final UserDTO userDto) {
		User user = null;
		if (userDto.getUserId() != null) {
			user = this.repository.findByUserId(userDto.getUserId());
		} else {
			user = new User();
			user.setRoot(Boolean.FALSE);
		}

		// Actualizaremos la contrasena solo si se establece
		if (!StringUtils.isEmpty(userDto.getPasswordAdd())) {
			final String pwd = userDto.getPasswordAdd();
			final BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
			final String hashPwd = bcpe.encode(pwd);

			user.setPassword(hashPwd);
		}

		// Al usuario root nunca le cambiaremos el nombre de usuario ni el rol
		if (user.getRoot() != Boolean.TRUE) {
			user.setUserName(userDto.getLoginAdd());
			user.setRol(this.rolRepository.findByRolId(userDto.getRolId()));
		}
		user.setName(userDto.getNameAdd());
		user.setSurnames(userDto.getSurnamesAdd());
		user.setEmail(userDto.getEmailAdd());
		user.setPhone(userDto.getTelfAdd());
		user.setStartDate(new Date());
		user.setRenovationDate(new Date());
		user.setDni(userDto.getDniAdd());
		
		//TODO Rellenar los campos que faltan
		return this.repository.save(user);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.IUserService#updateUser(es.gob.fire.persistence.configuration.dto.UserDTO)
	 */
	@Override
	@Transactional
	public User updateUser(final UserEditDTO userDto) {

		User user = null;

		if (userDto.getIdUserFireEdit() != null) {
			user = this.repository.findByUserId(userDto.getIdUserFireEdit());
		} else {
			user = new User();
			user.setRoot(Boolean.FALSE);
		}

		// Al usuario root nunca le cambiaremos el nombre de usuario ni el rol
		if (user.getRoot() != Boolean.TRUE) {
			user.setUserName(userDto.getUsernameEdit());
			user.setRol(this.rolRepository.findByRolId(userDto.getRolId()));
		}


		// Eliminamos la contrasena, si el rol del usuario no tiene permisos de acceso
		// Se da por hecho, que el permiso de acceso siempre se representara con un valor concreto
		if (!PermissionsChecker.hasPermission(user, Permissions.ACCESS)) {
			user.setPassword(null);
		}         
		// Actualizaremos la contrasena si se establece y si el usuario
        // no tenia ya una contrasena, ya que en ese caso la estariamos cambiando.
        else if (!StringUtils.isEmpty(userDto.getPasswordEdit()) && StringUtils.isEmpty(user.getPassword())) {
            final String pwd = userDto.getPasswordEdit();
            final BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
            final String hashPwd = bcpe.encode(pwd);

            user.setPassword(hashPwd);
        }

		user.setName(userDto.getNameEdit());
		user.setSurnames(userDto.getSurnamesEdit());
		user.setEmail(userDto.getEmailEdit());
		user.setPhone(userDto.getTelfEdit());
		user.setDni(userDto.getDniEdit());
		
		return this.repository.save(user);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#deleteUser(java.lang.Long)
	 */
	@Override
	@Transactional
	public void deleteUser(final Long userId) {
		this.repository.deleteById(userId);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getAllUse()
	 */
	@Override
	public List<User> getAllUser() {
		return this.repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUserByUserName(java.lang.String)
	 */
	@Override
	public User getUserByUserName(final String userName) {
		return this.repository.findByUserName(userName);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUserByRenovationCode(java.lang.String)
	 */
	@Override
	public User getUserByRenovationCode(final String renovationCode) {
		return this.repository.findByRenovationCode(renovationCode);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUserByUserNameOrEmail(java.lang.String,java.lang.String)
	 */
	@Override
	public User getUserByUserNameOrEmail(final String userName, final String email) {
		return this.repository.findByUserNameOrEmail(userName, email);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#findAll(org.springframework.data.jpa.datatables.mapping.DataTablesInput)
	 */
	@Override
	public DataTablesOutput<User> getAllUser(final DataTablesInput input) {

		return this.dtRepository.findAll(input);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.IUserService#changeUserPassword(es.gob.fire.persistence.configuration.dto.UserPasswordDTO)
	 */
	@Override
	@Transactional
	public String changeUserPassword(final UserPasswordDTO userPasswordDto) {

		final User user = this.repository.findByUserId(userPasswordDto.getIdUser());

		String result = null;
		final String oldPwd = userPasswordDto.getOldPassword();
		final String pwd = userPasswordDto.getPassword();
		final BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();

		final String hashPwd = bcpe.encode(pwd);
		try {
			if (bcpe.matches(oldPwd, user.getPassword()) || checkPasswordOldSystem(oldPwd, user.getPassword())) {
				user.setPassword(hashPwd);
				this.repository.save(user);
				result = "0"; //$NON-NLS-1$
			} else {
				result = "-1"; //$NON-NLS-1$
			}
		} catch (final Exception e) {
			result = "-2"; //$NON-NLS-1$
			throw e;
		}
		return result;
	}

	/**
	 * Method that checks if the password belong to the user and is
	 * stored with the old format.
	 * @param insertedPassword
	 *            inserted password
	 * @param expectedPassword
	 *            configured password Base64 encoded
	 * @return {@code true} if the password is of the user, {@code false} an
	 *         other case.
	 */
	private static boolean checkPasswordOldSystem(final String insertedPassword, final String expectedPassword) {

		final byte[] md;
		boolean result = false;
		try {
			md = MessageDigest.getInstance(MD_ALGORITHM).digest(insertedPassword.getBytes(DEFAULT_CHARSET));
			if (expectedPassword != null && expectedPassword.equals(Base64.encode(md))) {
				result = true;
			}
			else {
				LOGGER.warn("La contrasena insertada no es valida"); //$NON-NLS-1$
			}
		} catch (final NoSuchAlgorithmException nsae) {
			LOGGER.error("Error de configuracion en el servicio de administracion. Algoritmo de huella incorrecto", //$NON-NLS-1$
					nsae);
		} catch (final UnsupportedEncodingException uee) {
			LOGGER.error("Error de configuracion en el servicio de administracion. Codificacion incorrecta", uee); //$NON-NLS-1$
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IUserService#getAllRol()
	 */
	@Override
	public List<Rol> getAllRol() {

		return this.rolRepository.findAll();
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.IUserService#getRol(java.lang.Long)
	 */
	@Override
	public Rol getRol(final Long idRol) {
		return this.rolRepository.findByRolId(idRol);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUserByEmail(java.lang.String)
	 */
	@Override
	public User getUserByEmail(final String email) {
		return this.repository.findByEmail(email);
	}

	@Override
	public List<User> getAllUserByUserNameOrEmail(String userName, String email) {
		return this.repository.findAllByUserNameOrEmail(userName, email);
	}

	@Override
	public List<User> getAllUserByEmail(String email) {
		return this.repository.findAllByEmail(email);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IUserService#getUserByDni(java.lang.String)
	 */
	public User getUserByDni(String dni) {
		return this.repository.findAllByDni(dni);
	}
}
