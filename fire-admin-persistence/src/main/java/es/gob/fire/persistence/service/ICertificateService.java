/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/** 
 * <b>File:</b><p>es.gob.fire.persistence.service.ICertificateService.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 04/02/2025.
 */
package es.gob.fire.persistence.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.upgrade.afirma.PlatformWsException;
import es.gob.fire.upgrade.afirma.VerifyAfirmaCertificateResponse;
import es.gob.fire.upgrade.afirma.ws.WSServiceInvokerException;

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
	Certificate saveCertificate(CertificateDTO certificateDto, X509Certificate x509Certificate) throws IOException;
					
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

	/**
	 * Converts a list of {@code Certificate} objects to a list of {@code CertificateDTO} objects.
	 * 
	 * <p>This method maps each {@code Certificate} object to a {@code CertificateDTO} and enriches the DTO
	 * with additional information such as the certificate's validity status and formatted expiration date.
	 * The validity of the certificate is determined using its X.509 structure.</p>
	 *
	 * @param listCertificate the list of {@code Certificate} objects to be converted.
	 * @return a list of {@code CertificateDTO} objects containing mapped and enriched data.
	 *
	 * @throws CertificateException if an error occurs while parsing the certificate.
	 * @throws IOException if an error occurs during Base64 decoding of the certificate.
	 */
	List<CertificateDTO> obtainAllCertificateToDTO(List<Certificate> listCertificate);

	/**
	 * Validates the status of an X.509 certificate using the Afirma web service.
	 *
	 * <p>This method establishes a connection with AfirmaWS, initializes the necessary 
	 * configuration properties, and invokes the certificate verification service.</p>
	 *
	 * @param x509Certificate The X.509 certificate to be validated.
	 * @return A {@link VerifyAfirmaCertificateResponse} containing the verification result.
	 * @throws CertificateEncodingException If there is an issue encoding the certificate.
	 * @throws PlatformWsException If there is an error with the platform web service.
	 * @throws WSServiceInvokerException If there is an issue invoking the web service.
	 */
	VerifyAfirmaCertificateResponse validateStatusCertificateInAfirmaWS(X509Certificate x509Certificate) throws CertificateEncodingException, PlatformWsException, WSServiceInvokerException;
}
