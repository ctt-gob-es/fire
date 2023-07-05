package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>  {

	/**
	  * Method that obtains from the persistence a user identified by its primary key.
	 * @param userId String that represents the primary key of the user in the persistence.
	 * @return Object that represents a user from the persistence.
	 */
	User findByUserId(Long userId);
	
	/**
	 * Method that obtains from the persistence a user identified by its user name.
	 * @param userName String that represents the userName used to log in.
	 * @return Object that represents a user from the persistence.
	 */
	User findByUserName(String userName);
	
	/**
	 * Method that obtains from the persistence a user identified by its user name or email.
	 * @param userName String that represents the userName used to log in.
	 * @param email String that represents the user email.
	 * @return Object that represents a user from the persistence.
	 */
	User findByUserNameOrEmail(String userName, String email);
	
	/**
	 * Method that obtains an user by its user renovation code.
	 * @param renovationCode The user renovation code.
	 * @return {@link User}
	 */
	User findByRenovationCode(String renovationCode);
	
	/**
	 * Method that obtains from the persistence a user identified by its email.
	 * @param email String that represents the user email.
	 * @return Object that represents a user from the persistence.
	 */
	User findByEmail(String email);

}