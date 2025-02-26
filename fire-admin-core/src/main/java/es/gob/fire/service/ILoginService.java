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
 * <b>File:</b><p>es.gob.fire.service.ILoginService.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p></p>
 * <b>Date:</b><p>18/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 20/02/2025.
 */
package es.gob.fire.service;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.springframework.security.core.Authentication;

import es.gob.fire.crypto.cades.verifier.CAdESAnalizer;
import es.gob.fire.persistence.entity.ControlAccess;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.repository.ControlAccessRepository;

/**
 * <p>Interface that provides communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p></p>
 * @version 1.2, 20/02/2025.
 */
public interface ILoginService {

	/**
	 * Retrieves all control access records from the repository.
	 * This method fetches all entries from the {@link ControlAccessRepository}.
	 *
	 * @return a {@link List} containing all {@link ControlAccess} records.
	 */
	List<ControlAccess> obtainAllControlAccess();

	/**
     * Saves the provided {@link ControlAccess} object into the repository.
     * <p>If the {@code controlAccess} object is new, it will be inserted. If it already exists, it will be updated.</p>
     *
     * @param controlAccess The {@link ControlAccess} object to be saved. It must not be {@code null}.
     */
	void saveControlAccess(ControlAccess controlAccess);

	/**
    * Checks if the Pasarela service is available by sending a GET request to its URL.
    * <p>This method attempts to establish a connection to the Pasarela service and checks if the response code is 200 (OK). 
    * If successful, it returns {@code true}, otherwise, it returns {@code false}.</p>
    *
    * @return {@code true} if the Pasarela service responds with a status code of 200, otherwise {@code false}.
    */
	boolean isPasarelaAvailable();

	/**
     * Generates a unique cookie value.
     * <p>This method generates a random UUID, encodes it in Base64, and appends a random integer to it. The result is a unique string that can be used as a cookie value.</p>
     *
     * @return A string representing the generated cookie value, which includes a Base64-encoded UUID and a random integer.
     */
	String generateCookieValue();

	 /**
     * Deletes all control access records associated with the given IP address.
     * <p>This method will remove all records from the database that have the specified IP address.</p>
     */
	void deleteAllControlAccess();

	/**
	 * Analyzes a CAdES signature using the provided Base64 encoded byte array.
	 *
	 * @param signBase64Bytes the Base64 encoded CAdES signature bytes
	 * @return an initialized {@link CAdESAnalizer} instance
	 * @throws CertificateException if initialization fails or there is a certificate error
	 * @throws IOException if an I/O error occurs during analysis
	 */
	CAdESAnalizer analizeSignWithCAdES(byte[] signBase64Bytes) throws CertificateException;

	/**
	 * Loads a TrustStore of users from the specified file path.
	 * @return the loaded {@link KeyStore} instance
	 * @throws KeyStoreException if there is an error loading the TrustStore
	 */
	KeyStore loadTrustStoreUsers() throws KeyStoreException;

	/**
	 * Validates the issuer of the given certificate against the provided TrustStore.
	 * @param certificate parameter that contain the issuer of principal certificate.
	 * @param trustStoreUsers the KeyStore containing trusted issuer certificates.
	 *
	 * @return the X.509 certificate of the issuer if found, or {@code null} if not
	 * @throws KeyStoreException if there is an error accessing the TrustStore
	 * @throws CertificateException if the issuer certificate is null
	 */ 
	X509Certificate validateIssuerWithTrustStoreUsers(X509Certificate certificate, KeyStore trustStoreUsers) throws KeyStoreException, CertificateException;

	/**
	 * Verifies the public key of the given certificate using the issuer's public key.
	 *
	 * @param certificate the X.509 certificate to verify
	 * @param issuerCert the X.509 certificate of the issuer
	 * @throws CertificateException if verification fails
	 */
	void verifyPublicKeyToCertUser(X509Certificate certificate, X509Certificate issuerCert) throws CertificateException;

	/**
	 * Validates the validity period of the given X.509 certificate.
	 *
	 * @param certificate the X.509 certificate to validate
	 * @throws CertificateException if the certificate is expired or not yet valid
	 */
	void validatePeriodToCertUser(X509Certificate certificate) throws CertificateException;

	/**
	 * Extracts the DNI (National Identification Number) from a given X.509 certificate.
	 *
	 * @param certificate the X.509 certificate from which to extract the DNI
	 * @return the extracted DNI as a string
	 * @throws CertificateException if the certificate is invalid, does not contain a valid DNI, 
	 *                              or is issued by an unrecognized authority
	 */
	String obtainDNIfromCertUser(X509Certificate certificate) throws CertificateException;

	/**
	 * Obtains an authentication token for the given user, updates their last access time, 
	 * and populates the user session data.
	 *
	 * @param user the user to authenticate and update
	 * @return an {@link Authentication} token containing the user's credentials and roles
	 */
	Authentication obtainAuthAndUpdateLastAccess(User user);
}
