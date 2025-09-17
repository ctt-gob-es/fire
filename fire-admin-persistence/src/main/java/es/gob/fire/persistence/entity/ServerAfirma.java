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
 * <b>File:</b><p>es.gob.afirma.persistence.configuration.model.entity.config.ServerAfirma.java.</p>
 * <b>Description:</b><p>Class that represents the representation of the <i>TB_SERVIDOR_AFIRMA</i> database table as a
 * Plain Old Java Object.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>19/06/2020.</p>
 * @author Gobierno de España.
 * @version 1.1, 06/03/2025.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that represents the representation of the <i>TB_SERVIDOR_AFIRMA</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p></p>
 * @version 1.1, 06/03/2025.
 */
@Entity
@Table(name = "TB_SERVIDOR_AFIRMA")
public class ServerAfirma implements Serializable {

	/**
	 * Class serial version.
	 */
	private static final long serialVersionUID = 5690082072220917882L;

	/**
	 * Attribute that represents the object ID.
	 */
	@Id
	@Column(name = "ID_SERVIDOR_AFIRMA", unique = true, nullable = false, precision = NumberConstants.NUM19)
	private Long idServerAfirma;

	/**
	 * Attribute that represents the name of the url server.
	 */
	@Column(name = "URL_SERVIDOR", nullable = false, length = NumberConstants.NUM255)
	private String urlServer;

	/**
	 * Attribute that represents the name of the connection timeout.
	 */
	@Column(name = "FIN_CONEXION", nullable = false, length = NumberConstants.NUM32)
	private Long timeout;

	/**
	 * Attribute that represents the name of the name application.
	 */
	@Column(name = "NOMBRE_APLICACION", nullable = false, length = NumberConstants.NUM45)
	private String nameApp;

	/**
	 * Attribute that represents the name of the authentication type.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_TIPO_AUTENTICACION")
	private CAuthenticationType cAuthenticationType;

	/**
	 * Attribute that represents the name of the user.
	 */
	@Column(name = "USUARIO", nullable = true, length = NumberConstants.NUM45)
	private String user;

	/**
	 * Attribute that represents the name of the password.
	 */
	@Column(name = "PASSWORD", nullable = true)
	private String password;

	/* =========================
	 * TrustStore fields (TRUSTSTORE_*)
	 * ========================= */

	/** TrustStore binary (BLOB). */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "TRUSTSTORE_BLOB")
	private byte[] truststoreBlob;

	/** TrustStore password. */
	@Column(name = "TRUSTSTORE_PASSWORD", length = NumberConstants.NUM255)
	private String truststorePassword;

	/** TrustStore type (e.g., JKS, PKCS12). */
	@Column(name = "TRUSTSTORE_TYPE", length = 16)
	private String truststoreType;

	/** TrustStore version. */
	@Column(name = "TRUSTSTORE_VERSION", precision = NumberConstants.NUM32)
	private Long truststoreVersion;

	/* =========================
	 * KeyStore fields (KS_*)
	 * ========================= */

	/** KeyStore binary (BLOB). */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "KS_BLOB")
	private byte[] ksBlob;

	/** KeyStore password. */
	@Column(name = "KS_PASSWORD", length = NumberConstants.NUM255)
	private String ksPassword;

	/** KeyStore type (e.g., JKS, PKCS12). */
	@Column(name = "KS_TYPE", length = 16)
	private String ksType;

	/** Certificate alias inside KeyStore. */
	@Column(name = "KS_CERT_ALIAS", length = NumberConstants.NUM255)
	private String ksCertAlias;

	/** Private key/cert password for the alias. */
	@Column(name = "KS_CERT_PASSWORD", length = NumberConstants.NUM255)
	private String ksCertPassword;

	/** KeyStore version. */
	@Column(name = "KEYSTORE_VERSION", precision = NumberConstants.NUM32)
	private Long keystoreVersion;

	/* =======================================
	 * Authentication TrustStore fields (AUTH_TS_*)
	 * ======================================= */

	/** Auth TrustStore binary (BLOB). */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "AUTH_TS_BLOB")
	private byte[] authTsBlob;

	/** Auth TrustStore password. */
	@Column(name = "AUTH_TS_PASSWORD", length = NumberConstants.NUM255)
	private String authTsPassword;

	/** Auth TrustStore type. */
	@Column(name = "AUTH_TS_TYPE", length = 16)
	private String authTsType;

	/** Authentication certificate alias. */
	@Column(name = "AUTH_CERT_ALIAS", length = NumberConstants.NUM255)
	private String authCertAlias;

	/** Authentication configuration version. */
	@Column(name = "AUTHENTICATION_VERSION", precision = NumberConstants.NUM32)
	private Long authenticationVersion;

	
	/**
	 * Attribute that represents the date of the last period communication.
	 */
	@Column(name = "FECHA_ULTIMA_COMUNICACION", nullable = true)
	private Date dateLastCommunication;
	
	/**
	 * Gets the value of the attribute {@link #idServerAfirma}.
	 * @return the value of the attribute {@link #idServerAfirma}.
	 */
	public Long getIdServerAfirma() {
		return idServerAfirma;
	}

	/**
	 * Sets the value of the attribute {@link #idServerAfirma}.
	 * @param idServerAfirma The value for the attribute {@link #idServerAfirma}.
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
	 * @param urlServer The value for the attribute {@link #urlServer}.
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
	 * @param timeout The value for the attribute {@link #timeout}.
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
	 * @param nameApp The value for the attribute {@link #nameApp}.
	 */
	public void setNameApp(String nameApp) {
		this.nameApp = nameApp;
	}

	/**
	 * Gets the value of the attribute {@link #cAuthenticationType}.
	 * @return the value of the attribute {@link #cAuthenticationType}.
	 */
	public CAuthenticationType getcAuthenticationType() {
		return cAuthenticationType;
	}

	/**
	 * Sets the value of the attribute {@link #cAuthenticationType}.
	 * @param cAuthenticationType The value for the attribute {@link #cAuthenticationType}.
	 */
	public void setcAuthenticationType(CAuthenticationType cAuthenticationType) {
		this.cAuthenticationType = cAuthenticationType;
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
	 * @param user The value for the attribute {@link #user}.
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
	 * @param password The value for the attribute {@link #password}.
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Gets the value of the attribute {@link #truststoreBlob}.
	 * @return the value of the attribute {@link #truststoreBlob}.
	 */
	public byte[] getTruststoreBlob() { return truststoreBlob; }
	
	/**
	 * Sets the value of the attribute {@link #truststoreBlob}.
	 * @param password The value for the attribute {@link #truststoreBlob}.
	 */
	public void setTruststoreBlob(byte[] truststoreBlob) { this.truststoreBlob = truststoreBlob; }

	/**
	 * Gets the value of the attribute {@link #truststorePassword}.
	 * @return the value of the attribute {@link #truststorePassword}.
	 */
	public String getTruststorePassword() { return truststorePassword; }
	
	/**
	 * Sets the value of the attribute {@link #truststorePassword}.
	 * @param password The value for the attribute {@link #truststorePassword}.
	 */
	public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }

	/**
	 * Gets the value of the attribute {@link #truststoreType}.
	 * @return the value of the attribute {@link #truststoreType}.
	 */
	public String getTruststoreType() { return truststoreType; }
	
	/**
	 * Sets the value of the attribute {@link #truststoreType}.
	 * @param password The value for the attribute {@link #truststoreType}.
	 */
	public void setTruststoreType(String truststoreType) { this.truststoreType = truststoreType; }

	/**
	 * Gets the value of the attribute {@link #truststoreVersion}.
	 * @return the value of the attribute {@link #truststoreVersion}.
	 */
	public Long getTruststoreVersion() { return truststoreVersion; }
	
	/**
	 * Sets the value of the attribute {@link #truststoreVersion}.
	 * @param password The value for the attribute {@link #truststoreVersion}.
	 */
	public void setTruststoreVersion(Long truststoreVersion) { this.truststoreVersion = truststoreVersion; }

	/**
	 * Gets the value of the attribute {@link #ksBlob}.
	 * @return the value of the attribute {@link #ksBlob}.
	 */
	public byte[] getKsBlob() { return ksBlob; }
	
	/**
	 * Sets the value of the attribute {@link #ksBlob}.
	 * @param password The value for the attribute {@link #ksBlob}.
	 */
	public void setKsBlob(byte[] ksBlob) { this.ksBlob = ksBlob; }

	/**
	 * Gets the value of the attribute {@link #ksPassword}.
	 * @return the value of the attribute {@link #ksPassword}.
	 */
	public String getKsPassword() { return ksPassword; }
	
	/**
	 * Sets the value of the attribute {@link #ksPassword}.
	 * @param password The value for the attribute {@link #ksPassword}.
	 */
	public void setKsPassword(String ksPassword) { this.ksPassword = ksPassword; }

	/**
	 * Gets the value of the attribute {@link #ksType}.
	 * @return the value of the attribute {@link #ksType}.
	 */
	public String getKsType() { return ksType; }
	
	/**
	 * Sets the value of the attribute {@link #ksType}.
	 * @param password The value for the attribute {@link #ksType}.
	 */
	public void setKsType(String ksType) { this.ksType = ksType; }

	/**
	 * Gets the value of the attribute {@link #ksCertAlias}.
	 * @return the value of the attribute {@link #ksCertAlias}.
	 */
	public String getKsCertAlias() { return ksCertAlias; }
	
	/**
	 * Sets the value of the attribute {@link #ksCertAlias}.
	 * @param password The value for the attribute {@link #ksCertAlias}.
	 */
	public void setKsCertAlias(String ksCertAlias) { this.ksCertAlias = ksCertAlias; }

	/**
	 * Gets the value of the attribute {@link #ksCertPassword}.
	 * @return the value of the attribute {@link #ksCertPassword}.
	 */
	public String getKsCertPassword() { return ksCertPassword; }
	
	/**
	 * Sets the value of the attribute {@link #ksCertPassword}.
	 * @param password The value for the attribute {@link #ksCertPassword}.
	 */
	public void setKsCertPassword(String ksCertPassword) { this.ksCertPassword = ksCertPassword; }

	/**
	 * Gets the value of the attribute {@link #keystoreVersion}.
	 * @return the value of the attribute {@link #keystoreVersion}.
	 */
	public Long getKeystoreVersion() { return keystoreVersion; }
	
	/**
	 * Sets the value of the attribute {@link #keystoreVersion}.
	 * @param password The value for the attribute {@link #keystoreVersion}.
	 */
	public void setKeystoreVersion(Long keystoreVersion) { this.keystoreVersion = keystoreVersion; }

	/**
	 * Gets the value of the attribute {@link #authTsBlob}.
	 * @return the value of the attribute {@link #authTsBlob}.
	 */
	public byte[] getAuthTsBlob() { return authTsBlob; }
	
	/**
	 * Sets the value of the attribute {@link #authTsBlob}.
	 * @param password The value for the attribute {@link #authTsBlob}.
	 */
	public void setAuthTsBlob(byte[] authTsBlob) { this.authTsBlob = authTsBlob; }

	/**
	 * Gets the value of the attribute {@link #authTsPassword}.
	 * @return the value of the attribute {@link #authTsPassword}.
	 */
	public String getAuthTsPassword() { return authTsPassword; }
	
	/**
	 * Sets the value of the attribute {@link #authTsPassword}.
	 * @param password The value for the attribute {@link #authTsPassword}.
	 */
	public void setAuthTsPassword(String authTsPassword) { this.authTsPassword = authTsPassword; }

	/**
	 * Gets the value of the attribute {@link #authTsType}.
	 * @return the value of the attribute {@link #authTsType}.
	 */
	public String getAuthTsType() { return authTsType; }
	
	/**
	 * Sets the value of the attribute {@link #authTsType}.
	 * @param password The value for the attribute {@link #authTsType}.
	 */
	public void setAuthTsType(String authTsType) { this.authTsType = authTsType; }

	/**
	 * Gets the value of the attribute {@link #authCertAlias}.
	 * @return the value of the attribute {@link #authCertAlias}.
	 */
	public String getAuthCertAlias() { return authCertAlias; }
	
	/**
	 * Sets the value of the attribute {@link #authCertAlias}.
	 * @param password The value for the attribute {@link #authCertAlias}.
	 */
	public void setAuthCertAlias(String authCertAlias) { this.authCertAlias = authCertAlias; }

	/**
	 * Gets the value of the attribute {@link #authenticationVersion}.
	 * @return the value of the attribute {@link #authenticationVersion}.
	 */
	public Long getAuthenticationVersion() { return authenticationVersion; }
	
	/**
	 * Sets the value of the attribute {@link #authenticationVersion}.
	 * @param password The value for the attribute {@link #authenticationVersion}.
	 */
	public void setAuthenticationVersion(Long authenticationVersion) { this.authenticationVersion = authenticationVersion; }

	/**
	 * Gets the value of the attribute {@link #dateLastCommunication}.
	 * @return the value of the attribute {@link #dateLastCommunication}.
	 */
	public Date getDateLastCommunication() {
		return dateLastCommunication;
	}
	
	/**
	 * Sets the value of the attribute {@link #dateLastCommunication}.
	 * @param dateLastCommunication The value for the attribute {@link #dateLastCommunication}.
	 */
	public void setDateLastCommunication(Date dateLastCommunication) {
		this.dateLastCommunication = dateLastCommunication;
	}
	
}
