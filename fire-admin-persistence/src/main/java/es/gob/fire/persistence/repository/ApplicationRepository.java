package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String>{
	
	
	/**
	  * Method that obtains from the persistence a application identified by its primary key.
	 * @param appId String that represents the primary key of the application in the persistence.
	 * @return Object that represents a user from the persistence.
	 */
	Application findByAppId(String appId);
	
	/**
	 * Method that obtains from the persistence a user identified by its user name.
	 * @param userName String that represents the userName used to log in.
	 * @return Object that represents a user from the persistence.
	 */
	Application findByAppName(String appName);

	
	
	

}
