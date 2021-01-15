package es.gob.fire.persistence.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.dto.CertificateEditDTO;
import es.gob.fire.persistence.entity.Certificate;

public interface ICertificateService {
	/**
	 * Method that obtains an user by its identifier.
	 * @param userId The user identifier.
	 * @return {@link User}
	 */
	Certificate getCertificateByCertificateId(Long idCertificado);
	
	/**
	 * Method that obtains an user by its user name.
	 * @param userName The Certificate login.
	 * @return {@link Certificate}
	 */
	Certificate getCertificateByCertificateName(String nombre_cert);
	
	
	/**
	 * Method that stores a certificate in the persistence.
	 * @param user a {@link Certificate} with the information of the certificate.
	 * @return {@link Certificate} The certificate.
	 */
	Certificate saveCertificate(Certificate certificate);
	
	/** Method that stores a certificate in the persistence from Certificate DTO object.
	 * @param userDto a {@link CertificateDTO} with the information of the certificate.
	 * @return {@link Certificate} The Certificate.
	 */
	Certificate saveCertificate(CertificateDTO certificateDto) throws IOException;
	
	/**
	 * Method that updates a certificate in the persistence.
	 * @param userEditDto a {@link UserEditDTO} with the information of the certificate.
	 * @return {@link User} The certificate.
	 */
	Certificate updateCertificate(CertificateEditDTO certificateEditDto);
			
	/**
	 * Method that deletes a certificate in the persistence.
	 * @param userId {@link Integer} that represents the certificate identifier to delete.
	 */
	void deleteCertificate(Long idCertificado);
	
	/**
	 * Method that gets all the certificate from the persistence.
	 * @return a {@link Iterable<Certificate>} with the information of all certificate.
	 */
	List<Certificate> getAllCertificate();
		
	/**
	 * @param input
	 * @return
	 */
	List<Certificate> getAllCertificate(List input);
	
	/**
	 * Method that maos the values of CertificateDTO to Certificate.
	 * @param certificateDto Object that represents the values of a certificate taken from the view.
	 * @return Object thtat represetns a Certificate entity with the values of the CertificateDTO.
	 */
	Certificate certificateDtoToEntity(CertificateDTO certificateDto);
	
	/**
	 * Method that maos the values of CertificateDTO to Certificate.
	 * @param certificat Object that represents the mapping entity for Certificate.
	 * @return Object that represents a Certificate DTO.
	 */
	CertificateDTO certificateEntityToDto(Certificate certificate);
	
	/**
	 * Method that gets the list for the given {@link DataTablesInput}.
	 * @param input the {@link DataTablesInput} mapped from the Ajax request.
	 * @return {@link DataTablesOutput}
	 */
	DataTablesOutput<Certificate> certificatesDataTable(DataTablesInput input);
	
	/**
	 * Method that replaces the Base64 contents of each certificate of the list of Certificate to a String that represents the CN and expiration date.
	 * @param certificates List<Certificate> that contains all certificates of Fire.
	 */
	void getSubjectValuesForView(List<Certificate> certificates);
	
	/**
	 * @param certIs
	 * @return
	 */
	String getFormatCertText(InputStream certIs) throws CertificateException;
	
	/**
	 * @param certificate
	 * @return
	 */
	String getCertificateText(String certificate);
}
