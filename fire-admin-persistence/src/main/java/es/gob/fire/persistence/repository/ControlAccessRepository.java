package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.ControlAccess;

@Repository
public interface ControlAccessRepository extends JpaRepository<ControlAccess, Long>{
	
	/**
	 * Deletes all control access records associated with the specified IP address.
	 *
	 * @param ipUser the IP address for which control access records should be deleted
	 */
	void deleteAllByIp(String ipUser);

}
