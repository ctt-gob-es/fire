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
/**
 * @author Alvaro.DePorras
 *
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
	 * Attribute that represent the value to certificate in base64.
	 */
	private String certificateB64;

	/**
	 * Attribute that represent the value to disabled user input.
	 */
	private boolean isDisabledUserPass = true;

	/**
	 * Attribute that represent the value to disabled keystore input.
	 */
	private boolean isDisabledKeystore = true;
	
	/**
	 * Attribute that represent the value to error.
	 */
	private String error;

	/* ==== Keystore (KS_*) ==== */
	/**
	 * Attribute that represent the value to keystore file.
	 */
	private MultipartFile keystoreFile;
	
	/**
	 * Attribute that represent the value to keystore in base64.
	 */
	private String keystoreB64;
	
	/**
	 * Attribute that represent the value to password keystore.
	 */
	private String ksPassword;
	
	/**
	 * Attribute that represent the value to keystore type.
	 */
    private String ksType;
    
    /**
	 * Attribute that represent the value to keystore certificate alias.
	 */
    private String ksCertAlias;
    
    /**
	 * Attribute that represent the value to keystore certificate password.
	 */
    private String ksCertPassword;
    
    /**
	 * Attribute that represent the value to keystore version.
	 */
    private Long keystoreVersion;

    /**
	 * Attribute that represent the value to truststore file.
	 */
    private MultipartFile truststoreFile;
    
    /**
	 * Attribute that represent the value to truststore in base64.
	 */
    private String truststoreB64;
    
    /**
	 * Attribute that represent the value to truststore password.
	 */
	private String truststorePassword;
	
	/**
	 * Attribute that represent the value to truststore type.
	 */
    private String truststoreType;
    
    /**
	 * Attribute that represent the value to truststore version.
	 */
    private Long truststoreVersion;
	
    /* ==== Authentication TrustStore (AUTH_TS_*) ==== */
    /**
	 * Attribute that represent the value to truststore file.
	 */
    private MultipartFile authTruststoreFile;
    
    /**
	 * Attribute that represent the value to authentication truststore in base64.
	 */
    private String authTruststoreB64;
    
    /**
	 * Attribute that represent the value to authentication truststore password.
	 */
	private String authTruststorePassword;
    
    /**
	 * Attribute that represent the value to authentication truststore type.
	 */
    private String authTruststoreType;
    
    /**
	 * Attribute that represent the value to authentication truststore certificate alias.
	 */
    private String authCertAlias;
    
    /**
	 * Attribute that represent the value to authentication truststore version.
	 */
    private Long authenticationVersion;
    
    /**
     * Attribute that indicates the Auth Truststore must be cleared.
     */
    private boolean clearAuthTruststore;
    
    /**
     * Attribute that indicates if the ServerAfirma has a keystore password.
     */
    private boolean hasKsPassword;
    
    /**
     * Attribute that indicates if the ServerAfirma has a keystore certificate password.
     */
    private boolean hasKsCertPassword;
    
    /**
     * Attribute that indicates if the ServerAfirma has a truststore password.
     */
    private boolean hasTruststorePassword;
    
    /**
     * Attribute that indicates if the ServerAfirma has a authentication truststore password.
     */
    private boolean hasAuthTruststorePassword;
    
    /**
     * Attribute that indicates if the ServerAfirma has user password.
     */
    private boolean hasUserPassword;
	
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

	/**
	 * @return the ksPassword
	 */
	public String getKsPassword() {
		return ksPassword;
	}

	/**
	 * @param ksPassword the ksPassword to set
	 */
	public void setKsPassword(String ksPassword) {
		this.ksPassword = ksPassword;
	}

	/**
	 * @return the ksType
	 */
	public String getKsType() {
		return ksType;
	}

	/**
	 * @param ksType the ksType to set
	 */
	public void setKsType(String ksType) {
		this.ksType = ksType;
	}

	/**
	 * @return the ksCertAlias
	 */
	public String getKsCertAlias() {
		return ksCertAlias;
	}

	/**
	 * @param ksCertAlias the ksCertAlias to set
	 */
	public void setKsCertAlias(String ksCertAlias) {
		this.ksCertAlias = ksCertAlias;
	}

	/**
	 * @return the ksCertPassword
	 */
	public String getKsCertPassword() {
		return ksCertPassword;
	}

	/**
	 * @param ksCertPassword the ksCertPassword to set
	 */
	public void setKsCertPassword(String ksCertPassword) {
		this.ksCertPassword = ksCertPassword;
	}

	/**
	 * @return the keystoreVersion
	 */
	public Long getKeystoreVersion() {
		return keystoreVersion;
	}

	/**
	 * @param keystoreVersion the keystoreVersion to set
	 */
	public void setKeystoreVersion(Long keystoreVersion) {
		this.keystoreVersion = keystoreVersion;
	}

	/**
	 * @return the truststoreFile
	 */
	public MultipartFile getTruststoreFile() {
		return truststoreFile;
	}

	/**
	 * @param truststoreFile the truststoreFile to set
	 */
	public void setTruststoreFile(MultipartFile truststoreFile) {
		this.truststoreFile = truststoreFile;
	}

	/**
	 * @return the truststoreB64
	 */
	public String getTruststoreB64() {
		return truststoreB64;
	}

	/**
	 * @param truststoreB64 the truststoreB64 to set
	 */
	public void setTruststoreB64(String truststoreB64) {
		this.truststoreB64 = truststoreB64;
	}

	/**
	 * @return the truststorePassword
	 */
	public String getTruststorePassword() {
		return truststorePassword;
	}

	/**
	 * @param truststorePassword the truststorePassword to set
	 */
	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	/**
	 * @return the truststoreType
	 */
	public String getTruststoreType() {
		return truststoreType;
	}

	/**
	 * @param truststoreType the truststoreType to set
	 */
	public void setTruststoreType(String truststoreType) {
		this.truststoreType = truststoreType;
	}

	/**
	 * @return the truststoreVersion
	 */
	public Long getTruststoreVersion() {
		return truststoreVersion;
	}

	/**
	 * @param truststoreVersion the truststoreVersion to set
	 */
	public void setTruststoreVersion(Long truststoreVersion) {
		this.truststoreVersion = truststoreVersion;
	}

	/**
	 * @return the authTruststoreFile
	 */
	public MultipartFile getAuthTruststoreFile() {
		return authTruststoreFile;
	}

	/**
	 * @param authTruststoreFile the authTruststoreFile to set
	 */
	public void setAuthTruststoreFile(MultipartFile authTruststoreFile) {
		this.authTruststoreFile = authTruststoreFile;
	}

	/**
	 * @return the authTruststoreB64
	 */
	public String getAuthTruststoreB64() {
		return authTruststoreB64;
	}

	/**
	 * @param authTruststoreB64 the authTruststoreB64 to set
	 */
	public void setAuthTruststoreB64(String authTruststoreB64) {
		this.authTruststoreB64 = authTruststoreB64;
	}

	/**
	 * @return the authTruststorePassword
	 */
	public String getAuthTruststorePassword() {
		return authTruststorePassword;
	}

	/**
	 * @param authTruststorePassword the authTruststorePassword to set
	 */
	public void setAuthTruststorePassword(String authTruststorePassword) {
		this.authTruststorePassword = authTruststorePassword;
	}

	/**
	 * @return the authTruststoreType
	 */
	public String getAuthTruststoreType() {
		return authTruststoreType;
	}

	/**
	 * @param authTruststoreType the authTruststoreType to set
	 */
	public void setAuthTruststoreType(String authTruststoreType) {
		this.authTruststoreType = authTruststoreType;
	}

	/**
	 * @return the authCertAlias
	 */
	public String getAuthCertAlias() {
		return authCertAlias;
	}

	/**
	 * @param authCertAlias the authCertAlias to set
	 */
	public void setAuthCertAlias(String authCertAlias) {
		this.authCertAlias = authCertAlias;
	}

	/**
	 * @return the authenticationVersion
	 */
	public Long getAuthenticationVersion() {
		return authenticationVersion;
	}

	/**
	 * @param authenticationVersion the authenticationVersion to set
	 */
	public void setAuthenticationVersion(Long authenticationVersion) {
		this.authenticationVersion = authenticationVersion;
	}

	/**
	 * @return the isDisabledUserPass
	 */
	public boolean isDisabledUserPass() {
		return isDisabledUserPass;
	}

	/**
	 * @return the isDisabledKeystore
	 */
	public boolean isDisabledKeystore() {
		return isDisabledKeystore;
	}
	
	/**
	 * @return the clearAuthTruststore
	 */
	public boolean isClearAuthTruststore() {
	    return clearAuthTruststore;
	}
	
	/**
	 * @param clearAuthTruststore the clearAuthTruststore to set
	 */
	public void setClearAuthTruststore(boolean clearAuthTruststore) {
	    this.clearAuthTruststore = clearAuthTruststore;
	}

	/**
	 * @return the hasKsPassword
	 */
	public boolean isHasKsPassword() {
		return hasKsPassword;
	}

	/**
	 * @param hasKsPassword the hasKsPassword to set
	 */
	public void setHasKsPassword(boolean hasKsPassword) {
		this.hasKsPassword = hasKsPassword;
	}

	/**
	 * @return the hasKsCertPassword
	 */
	public boolean isHasKsCertPassword() {
		return hasKsCertPassword;
	}

	/**
	 * @param hasKsCertPassword the hasKsCertPassword to set
	 */
	public void setHasKsCertPassword(boolean hasKsCertPassword) {
		this.hasKsCertPassword = hasKsCertPassword;
	}

	/**
	 * @return the hasTruststorePassword
	 */
	public boolean isHasTruststorePassword() {
		return hasTruststorePassword;
	}

	/**
	 * @param hasTruststorePassword the hasTruststorePassword to set
	 */
	public void setHasTruststorePassword(boolean hasTruststorePassword) {
		this.hasTruststorePassword = hasTruststorePassword;
	}

	/**
	 * @return the hasAuthTruststorePassword
	 */
	public boolean isHasAuthTruststorePassword() {
		return hasAuthTruststorePassword;
	}

	/**
	 * @param hasAuthTruststorePassword the hasAuthTruststorePassword to set
	 */
	public void setHasAuthTruststorePassword(boolean hasAuthTruststorePassword) {
		this.hasAuthTruststorePassword = hasAuthTruststorePassword;
	}

	/**
	 * @return the hasUserPassword
	 */
	public boolean isHasUserPassword() {
		return hasUserPassword;
	}

	/**
	 * @param hasUserPassword the hasUserPassword to set
	 */
	public void setHasUserPassword(boolean hasUserPassword) {
		this.hasUserPassword = hasUserPassword;
	}
}
