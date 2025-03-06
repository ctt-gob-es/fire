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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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

	/**
	 * Attribute that represents the name of the keystore.
	 */
	@Column(name = "ALMACEN", nullable = true)
	private String keystore;
	
	/**
	 * Attribute that represents the name of the password keystore.
	 */
	@Column(name = "PASSWORD_ALMACEN", nullable = true)
	private String passwordKeystore;
	
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
	 * Gets the value of the attribute {@link #keystore}.
	 * @return the value of the attribute {@link #keystore}.
	 */
	public String getKeystore() {
		return keystore;
	}

	/**
	 * Sets the value of the attribute {@link #keystore}.
	 * @param keystore The value for the attribute {@link #keystore}.
	 */
	public void setKeystore(String keystore) {
		this.keystore = keystore;
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
	 * @param passwordKeystore The value for the attribute {@link #passwordKeystore}.
	 */
	public void setPasswordKeystore(String passwordKeystore) {
		this.passwordKeystore = passwordKeystore;
	}

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
