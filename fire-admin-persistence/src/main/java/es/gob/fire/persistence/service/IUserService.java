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
 * <b>File:</b><p>es.gob.fire.persistence.service.IUserService.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 15/06/2018.
 */
package es.gob.fire.persistence.service;
import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.persistence.dto.UserDTO;
import es.gob.fire.persistence.dto.UserEditDTO;
import es.gob.fire.persistence.dto.UserPasswordDTO;
import es.gob.fire.persistence.entity.Rol;
import es.gob.fire.persistence.entity.User;

/** 
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 15/06/2018.
 */
public interface IUserService {
	/**
	 * Method that obtains an user by its identifier.
	 * @param userId The user identifier.
	 * @return {@link User}
	 */
	User getUserByUserId(Long userId);
	
	/**
	 * Method that obtains an user by its user name.
	 * @param userName The user login.
	 * @return {@link User}
	 */
	User getUserByUserName(String userName);
	
	/**
	 * Method that obtains an user by its user name or email.
	 * @param userName The user login.
	 * @param email The user email.
	 * @return {@link User}
	 */
	User getUserByUserNameOrEmail(String userName, String email);
	
	/**
	 * Method that obtains an user by its email.
	 * @param email The user email.
	 * @return {@link User}
	 */
	User getUserByEmail(String email);
	
	/**
	 * Method that obtains an user by its user renovation code.
	 * @param renovationCode The user renovation code.
	 * @return {@link User}
	 */
	User getUserByRenovationCode(String renovationCode);
	
	/**
	 * Method that stores a user in the persistence.
	 * @param user a {@link User} with the information of the user.
	 * @return {@link User} The user.
	 */
	User saveUser(User user);
	
	/** Method that stores a user in the persistence from User DTO object.
	 * @param userDto a {@link UserDTO} with the information of the user.
	 * @return {@link User} The user.
	 */
	User saveUser(UserDTO userDto);
	
	/**
	 * Method that updates a user in the persistence.
	 * @param userEditDto a {@link UserEditDTO} with the information of the user.
	 * @return {@link User} The user.
	 */
	User updateUser(UserEditDTO userEditDto);
			
	/**
	 * Method that deletes a user in the persistence.
	 * @param userId {@link Integer} that represents the user identifier to delete.
	 */
	void deleteUser(Long userId);
	
	/**
	 * Method that gets all the users from the persistence.
	 * @return a {@link Iterable<User>} with the information of all users.
	 */
	Iterable<User> getAllUser();
	
	/**
	 * Method that gets all the user roles from the persistence.
	 * @return a {@link Iterable<Rol>} with the information of all roles.
	 */
	List<Rol> getAllRol();	
		
	/**
	 * Method that checks if the Rol identified by idRol is Adminstrador
	 * @param idRol Long that represents the Rol identifier.
	 * @return true if the Rol is Administrador
	 */
	boolean isAdminRol(Long idRol);
		
	/**
	 * Method that gets the list for the given {@link DataTablesInput}.
	 * @param input the {@link DataTablesInput} mapped from the Ajax request.
	 * @return {@link DataTablesOutput}
	 */
	DataTablesOutput<User> getAllUser(DataTablesInput input);
	
	/**
	 * Method that change the password of a user.
	 * @param userPasswordDto a {@link UserPasswordDTO} with the information of the user.
	 * @return {@link String} The result of the password change:
	 * 		0: Change success
	 * 	   -1: Old password and new password doesn't match
	 *     -2: Error updating the user with new password
	 */
	String changeUserPassword(UserPasswordDTO userPasswordDto);
	
}
