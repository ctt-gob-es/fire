package es.gob.fire.persistence.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.Application;
import es.gob.fire.persistence.entity.AuditTransaction;

@Repository
public interface AuditTransactionRepository extends JpaRepository<AuditTransaction, String>{
	
	/**
	  * Method that obtains from the persistence a petition identified by its primary key.
	 * @param idAuditTransaction Integer that represents the primary key of the application in the persistence.
	 * @return Object that represents a user from the persistence.
	 */
	AuditTransaction findByIdAuditTransaction(Integer idAuditTransaction);
	
	/**
	 * Method that obtains from the persistence a list of petitions.
	 * @param start Date that represents the start date to filter.
	 * @param end Date that represents the end date to filter.
	 * @return List<Petition> that represents a list of petitions.
	 */
	List<AuditTransaction> findByDateBetween(Date start, Date end);
	
	/**
	 * Method that obtains from the persistence a list of petitions.
	 * @param date Date that represents the start date to filter.
	 * @return List<Petition> that represents a list of petitions.
	 */
	List<AuditTransaction> findByDateAfter(Date date);
	
	/**
	 * Method that obtains from the persistence a list of petitions.
	 * @param date Date that represents the end date to filter.
	 * @return List<Petition> that represents a list of petitions.
	 */
	List<AuditTransaction> findByDateBefore(Date date);
	
	@Query("SELECT at FROM AuditTransaction at WHERE (:to IS NULL OR :to >= at.date) AND (:from IS NULL OR :from <= at.date) AND at.nameApp IN (SELECT at2.nameApp FROM AuditTransaction at2 WHERE (:app IS NULL OR at2.nameApp = :app))")
	List<AuditTransaction> findByDateRangeAndApplication(@Param("from") Date from, @Param("to") Date to, @Param("app") String app);
	
	@Query("SELECT DISTINCT(at.nameApp) from AuditTransaction at ORDER BY at.nameApp ASC")
	List<String> findDistinctApp();
}
