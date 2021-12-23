package es.gob.fire.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.entity.Certificate;


@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long>{


	/**
	  * Method that obtains from the persistence a user identified by its primary key.
	 * @param userId String that represents the primary key of the user in the persistence.
	 * @return Object that represents a user from the persistence.
	 */
	Certificate findByIdCertificado(Long idCertificado);
	
	/**
	 * Method that obtains from the persistence a user identified by its user name.
	 * @param userName String that represents the userName used to log in.
	 * @return Object that represents a user from the persistence.
	 */
	Certificate findByCertificateName(String nombre_cert);
}
