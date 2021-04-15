package es.gob.fire.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.gob.fire.persistence.dto.ApplicationCertDTO;
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
	
	/**
	 * Method that obtains from the persistence a List of Application 
	 * @param idCertificado Long that represents the Certificate identifier.
	 * @return Object that represents a user from the persistence.
	 */
	List<Application> findByCertificateIdCertificado(Long idCertificado);

	
	/**
	 * Method that gets the applications associated to the Certificate identified by idCertificate.
	 * @return List<ApplicationCertDTO>
	 */
	@Query("SELECT new es.gob.fire.persistence.dto.ApplicationCertDTO(ap.appId, ap.appName, ap.fechaAltaApp) FROM Application ap, Certificate cer WHERE ap.certificate.idCertificado = cer.idCertificado AND ap.certificate.idCertificado = :idCertificado order by ap.appName")
	List<ApplicationCertDTO> findApplicationCert(@Param("idCertificado") Long idCertificado);
	
	/**
	 * Method that gets the applications associated to the Certificate identified by idCertificate.
	 * @return ApplicationCertDTO
	 */
	@Query("SELECT new es.gob.fire.persistence.dto.ApplicationCertDTO(ap.appId, ap.appName, cer.idCertificado, cer.certificateName) FROM Application ap, Certificate cer WHERE ap.certificate.idCertificado = cer.idCertificado AND ap.appId = :appId")
	ApplicationCertDTO findViewApplication(@Param("appId") String appId);
	
}
