package es.gob.fire.persistence.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.AuditSignature;


@Repository
public interface AuditSignatureRepository extends JpaRepository<AuditSignature, String>{
	
	/**
	 * Method that obtains from the persistence a petition identified by its primary key.
	 * @param idAuditSignature Integer that represents the primary key of the application in the persistence.
	 * @return Object that represents a petition from the persistence.
	 */
	AuditSignature findByIdAuditSignature(Integer idAuditSignature);
	
	/**
	 * Method that obtains from the persistence a petition identified by its primary key.
	 * @param idAuditSignature Integer that represents the primary key of the application in the persistence.
	 * @return Object that represents a petition from the persistence.
	 */
	List<AuditSignature> findByIdTransaction(String idTransaction);
}
