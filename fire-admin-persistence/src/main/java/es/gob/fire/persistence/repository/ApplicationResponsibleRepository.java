package es.gob.fire.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.ApplicationResponsible;
import es.gob.fire.persistence.entity.ApplicationResponsiblePK;

@Repository
public interface ApplicationResponsibleRepository extends JpaRepository<ApplicationResponsible, ApplicationResponsiblePK>{
		
	/**
	 * Method that gets from persistence all ApplicationResponsible whose User identifier matches with userId.
	 * @param userId USer identifier
	 * @return List<ApplicationResponsible>
	 */
	List<ApplicationResponsible> findByResponsibleUserId(Long userId);
	
	/**
	 * Method that gets from persistence all ApplicationResponsible whose User identifier matches with appId.
	 * @param appId Application identifier
	 * @return List<ApplicationResponsible>
	 */
	List<ApplicationResponsible> findByApplicationAppId(String appId);
	
	/**
	 * Method that removes from persistence all ApplicationResponsible whose User identifier matches with appId.
	 * @param appId Application identifier
	 */
	void deleteByApplicationAppId(String appId);
}
