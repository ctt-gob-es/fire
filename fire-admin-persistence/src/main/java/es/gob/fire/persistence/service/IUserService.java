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
 * <b>File:</b><p>es.gob.valet.service.IUserValetService.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/06/2018.
 */
package es.gob.fire.persistence.service;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.persistence.model.entity.User;

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
	 * @return {@link UserValet}
	 */
	User getUserByUserName(String userName);
	
	/**
	 * Method that stores a user in the persistence.
	 * @param user a {@link UserValet} with the information of the user.
	 * @return {@link UserValet} The user. 
	 */
	User saveUser(User user);
			
	/**
	 * Method that deletes a user in the persistence.
	 * @param id {@link Integer} that represents the user identifier to delete.
	 */
	void deleteUser(Long id);
	
	/**
	 * Method that gets all the users from the persistence.
	 * @return a {@link Iterable<UserValet>} with the information of all users.
	 */
	Iterable<User> getAllUser();
		
	/**
	 * Method that gets the list for the given {@link DataTablesInput}.
	 * @param input the {@link DataTablesInput} mapped from the Ajax request.
	 * @return {@link DataTablesOutput}
	 */
	DataTablesOutput<User> getAllUser(DataTablesInput input);
}






