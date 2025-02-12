package es.gob.fire.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.dto.MailInfoDTO;
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
	
	/**
	 * Method that obtains from the persistence all users identified by an email value.
	 * @param email String that represents the user email.
	 * @return List that represents all the users from the persistence.
	 */
	List<User> findAllByEmail(String email); 
	
	/**
	 * Method that obtains from the persistence all users identified by the given username or email.
	 * @param userName String that represents the userName used to log in.
	 * @param email String that represents the user email.
	 * @return List that represents all the users from the persistence.
	 */
	List<User> findAllByUserNameOrEmail(String userName, String email);

	/**
	 * Method that obtains from the persistence all users identified by an dni value.
	 * @param dni String that represents the user dni.
	 * @return List that represents all the users from the persistence.
	 */
	User findAllByDni(String dni);
	
	/**
	 * Retrieves a distinct list of {@link MailInfoDTO} with certificate and responsible user details.
	 * The query filters for users with a role ID of 2.
	 * 
	 * @return a list of {@link MailInfoDTO} containing the user's email, certificate ID, start and expiration dates, and subject.
	 */
	@Query("SELECT DISTINCT new es.gob.fire.persistence.dto.MailInfoDTO(" +
				"ar.responsible.email, " +
				"ca.certificate.idCertificado, " +
				"ca.certificate.fechaInicio, " +
				"ca.certificate.fechaCaducidad, " +
				"ca.certificate.subject) " +
				"FROM CertificatesApplication ca, ApplicationResponsible ar, Rol r " +
				"WHERE ca.application.appId = ar.application.appId " +
				"AND ar.responsible.rol.rolId = r.rolId " +
		       	"AND r.rolId = 2")
	List<MailInfoDTO> obtainAllCertWithAppAndResposible();

}