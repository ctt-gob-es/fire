/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * <b>File:</b><p>es.gob.fire.persistence.dto.CAuthenticationTypeDTO.java.</p>
 * <b>Description:</b><p> Class that represents the backing form for editing a Server Afirma.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 04/03/2025.
 */
package es.gob.fire.persistence.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>Class that represents the backing form for adding a user.</p>
 * <b>Project:</b><p> Class that represents the backing form for editing a Server Afirma.</p>
 * @version 1.0, 04/03/2025.
 */
public class ServerAfirmaDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	private Long idServerAfirma;

	/**
	 * Attribute that represent the value to url server.
	 */
	private String urlServer;

	/**
	 * Attribute that represent the value to timeout connection.
	 */
	private Long timeout;

	/**
	 * Attribute that represent the value to identificator application.
	 */
	private String nameApp;

	/**
	 * Attribute that represent the value to id of authentication type.
	 */
	private Long idAuthenticationType;

	/**
	 * Attribute that represent the value to user.
	 */
	private String user;

	/**
	 * Attribute that represent the value to password.
	 */
	private String password;

	/**
	 * Attribute that represent the value to subject.
	 */
	private String subject;

	/**
	 * Attribute that represent the value to password keystore.
	 */
	private String passwordKeystore;

	/**
	 * Attribute that represent the value to disabled user input.
	 */
	private boolean isDisabledUserPass = true;

	/**
	 * Attribute that represent the value to disabled keystore input.
	 */
	private boolean isDisabledKeystore = true;

	/**
	 * Attribute that represent the value to keystore in base64.
	 */
	private String keystoreB64;

	/**
	 * Attribute that represent the value to error.
	 */
	private String error;
	
	/**
	 * Attribute that represent the value to keystore file.
	 */
	private MultipartFile keystoreFile;
	
	/**
	 * Attribute that represent the value to certificate in base64.
	 */
	private String certificateB64;
	
	/**
	 * Default constructor for {@link ServerAfirmaDTO}.
	 *
	 * <p>Creates an empty instance of {@code ServerAfirmaDTO} with default values.</p>
	 */
	public ServerAfirmaDTO() {
	}

	/**
	 * Constructs a {@link ServerAfirmaDTO} with the specified parameters.
	 *
	 * @param idAuthenticationType The unique identifier for the authentication type.
	 * @param idServerAfirma The unique identifier for the server configuration.
	 * @param nameApp The name of the application using the server.
	 * @param timeout The timeout value for server requests, in milliseconds.
	 * @param urlServer The URL of the Afirma server.
	 */
	public ServerAfirmaDTO(Long idAuthenticationType, Long idServerAfirma, String nameApp, Long timeout,
			String urlServer) {
		this.idAuthenticationType = idAuthenticationType;
		this.idServerAfirma = idServerAfirma;
		this.nameApp = nameApp;
		this.timeout = timeout;
		this.urlServer = urlServer;
	}

	/**
	 * Gets the value of the attribute {@link #idServerAfirma}.
	 * @return the value of the attribute {@link #idServerAfirma}.
	 */
	public Long getIdServerAfirma() {
		return idServerAfirma;
	}

	/**
	 * Sets the value of the attribute {@link #idServerAfirma}.
	 * @param idServerAfirma the value for the attribute {@link #idServerAfirma} to set.
	 */
	public void setIdServerAfirma(Long idServerAfirma) {
		this.idServerAfirma = idServerAfirma;
	}

	/**
	 * Gets the value of the attribute {@link #urlServer}.
	 * @return the value of the attribute {@link #urlServer}.
	 */
	public String getUrlServer() {
		return urlServer;
	}

	/**
	 * Sets the value of the attribute {@link #urlServer}.
	 * @param urlServer the value for the attribute {@link #urlServer} to set.
	 */
	public void setUrlServer(String urlServer) {
		this.urlServer = urlServer;
	}

	/**
	 * Gets the value of the attribute {@link #timeout}.
	 * @return the value of the attribute {@link #timeout}.
	 */
	public Long getTimeout() {
		return timeout;
	}

	/**
	 * Sets the value of the attribute {@link #timeout}.
	 * @param timeout the value for the attribute {@link #timeout} to set.
	 */
	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	/**
	 * Gets the value of the attribute {@link #nameApp}.
	 * @return the value of the attribute {@link #nameApp}.
	 */
	public String getNameApp() {
		return nameApp;
	}

	/**
	 * Sets the value of the attribute {@link #nameApp}.
	 * @param nameApp the value for the attribute {@link #nameApp} to set.
	 */
	public void setNameApp(String nameApp) {
		this.nameApp = nameApp;
	}

	/**
	 * Gets the value of the attribute {@link #idAuthenticationType}.
	 * @return the value of the attribute {@link #idAuthenticationType}.
	 */
	public Long getIdAuthenticationType() {
		return idAuthenticationType;
	}

	/**
	 * Sets the value of the attribute {@link #idAuthenticationType}.
	 * @param idAuthenticationType the value for the attribute {@link #idAuthenticationType} to set.
	 */
	public void setIdAuthenticationType(Long idAuthenticationType) {
		this.idAuthenticationType = idAuthenticationType;
	}

	/**
	 * Gets the value of the attribute {@link #user}.
	 * @return the value of the attribute {@link #user}.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the value of the attribute {@link #user}.
	 * @param user the value for the attribute {@link #user} to set.
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the value of the attribute {@link #password}.
	 * @return the value of the attribute {@link #password}.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the value of the attribute {@link #password}.
	 * @param password the value for the attribute {@link #password} to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the value of the attribute {@link #subject}.
	 * @return the value of the attribute {@link #subject}.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Sets the value of the attribute {@link #subject}.
	 * @param password the value for the attribute {@link #subject} to set.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Gets the value of the attribute {@link #passwordKeystore}.
	 * @return the value of the attribute {@link #passwordKeystore}.
	 */
	public String getPasswordKeystore() {
		return passwordKeystore;
	}

	/**
	 * Sets the value of the attribute {@link #passwordKeystore}.
	 * @param passwordKeystore the value for the attribute {@link #passwordKeystore} to set.
	 */
	public void setPasswordKeystore(String passwordKeystore) {
		this.passwordKeystore = passwordKeystore;
	}

	/**
	 * Gets the value of the attribute {@link #isDisabledUserPass}.
	 * @return the value of the attribute {@link #isDisabledUserPass}.
	 */
	public boolean getDisabledUserPass() {
		return isDisabledUserPass;
	}

	/**
	 * Sets the value of the attribute {@link #isDisabledUserPass}.
	 * @param isDisabledUserPass the value for the attribute {@link #isDisabledUserPass} to set.
	 */
	public void setDisabledUserPass(boolean isDisabledUserPass) {
		this.isDisabledUserPass = isDisabledUserPass;
	}

	/**
	 * Gets the value of the attribute {@link #isDisabledKeystore}.
	 * @return the value of the attribute {@link #isDisabledKeystore}.
	 */
	public boolean getDisabledKeystore() {
		return isDisabledKeystore;
	}

	/**
	 * Sets the value of the attribute {@link #isDisabledKeystore}.
	 * @param isDisabledKeystore the value for the attribute {@link #isDisabledKeystore} to set.
	 */
	public void setDisabledKeystore(boolean isDisabledKeystore) {
		this.isDisabledKeystore = isDisabledKeystore;
	}

	/**
	 * Gets the value of the attribute {@link #keystoreB64}.
	 * @return the value of the attribute {@link #keystoreB64}.
	 */
	public String getKeystoreB64() {
		return keystoreB64;
	}

	/**
	 * Sets the value of the attribute {@link #keystoreB64}.
	 * @param keystoreB64 the value for the attribute {@link #keystoreB64} to set.
	 */
	public void setKeystoreB64(String keystoreB64) {
		this.keystoreB64 = keystoreB64;
	}

	/**
	 * Gets the value of the attribute {@link #error}.
	 * @return the value of the attribute {@link #error}.
	 */
	public String getError() {
		return error;
	}

	/**
	 * Sets the value of the attribute {@link #error}.
	 * @param error the value for the attribute {@link #error} to set.
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * Gets the value of the attribute {@link #keystoreFile}.
	 * @return the value of the attribute {@link #keystoreFile}.
	 */
	public MultipartFile getKeystoreFile() {
		return keystoreFile;
	}

	/**
	 * Sets the value of the attribute {@link #keystoreFile}.
	 * @param keystoreFile the value for the attribute {@link #keystoreFile} to set.
	 */
	public void setKeystoreFile(MultipartFile keystoreFile) {
		this.keystoreFile = keystoreFile;
	}

	/**
	 * Gets the value of the attribute {@link #certificateB64}.
	 * @return the value of the attribute {@link #certificateB64}.
	 */
	public String getCertificateB64() {
		return certificateB64;
	}

	/**
	 * Sets the value of the attribute {@link #certificateB64}.
	 * @param certificateB64 the value for the attribute {@link #certificateB64} to set.
	 */
	public void setCertificateB64(String certificateB64) {
		this.certificateB64 = certificateB64;
	}
	
}
