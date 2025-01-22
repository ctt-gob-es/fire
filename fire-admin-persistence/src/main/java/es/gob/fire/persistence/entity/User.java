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
 * <b>File:</b><p>es.gob.fire.persistence.entity.User.java.</p>
 * <b>Description:</b><p>Class that maps the <i>TB_USUARIOS</i> database table as a Plain Old Java Object.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * <b>Date:</b><p>07/07/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 20/01/2025.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>TB_USUARIOS</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.
 * @version 1.2, 20/01/2025.
 */
@Entity
@Table(name = "TB_USUARIOS")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class User implements Serializable {

	/**
	 * Class serial version.
	 */
	private static final long serialVersionUID = -60419018366799736L;

	/**
	 * Attribute that represents the user id.
	 */
	private Long userId;

	/**
	 * Attribute that represents the user name.
	 */
	private String userName;

	/**
	 * Attribute that represents the email.
	 */
	private String email;

	/**
	 * Attribute that represents the name.
	 */
	private String name;

	/**
	 * Attribute that represents the password.
	 */
	private String password;

	/**
	 * Attribute that represents the surnames.
	 */
	private String surnames;

	/**
	 * Attribute that represents the phone.
	 */
	private String phone;

	/**
	 * Attribute that represents the startDate.
	 */
	private Date startDate;

	/**
	 * Attribute that represents the root.
	 */
	private Boolean root;

	/**
	 * Attribute that represents the renovationCode.
	 */
	private String renovationCode;

	/**
	 * Attribute that represents the renovationDate.
	 */
	private Date renovationDate;

	/**
	 * Attribute that represents the restPassword.
	 */
	private Boolean restPassword;

	/**
	 * Attribute that represents the rol.
	 */
	private Rol rol;

	/**
	 * Attribute that represents the DNI.
	 */
	private String dni;
	
	/**
	 * Attribute that represents the date last access.
	 */
	private Date fecUltimoAcceso;
	
	/**
	 * Gets the value of the attribute {@link #userId}.
	 * @return the value of the attribute {@link #userId}.
	 */
	@Id
	@Column(name = "ID_USUARIO", unique = true, nullable = false, precision = NumberConstants.NUM11)
	@GeneratedValue(generator = "tb_usuarios_seq")
	@GenericGenerator(name = "tb_usuarios_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "TB_USUARIOS_SEQ"), @Parameter(name = "initial_value", value = "2"), @Parameter(name = "increment_size", value = "1") })
	@JsonView(DataTablesOutput.View.class)
	public Long getUserId() {
		return this.userId;
	}

	/**
	 * Sets the value of the attribute {@link #userId}.
	 * @param userIdP The value for the attribute {@link #userId}.
	 */
	public void setUserId(final Long userIdP) {
		this.userId = userIdP;
	}

	/**
	 * Gets the value of the attribute {@link #userName}.
	 * @return the value of the attribute {@link #userName}.
	 */

	@Column(name = "NOMBRE_USUARIO", nullable = false, length = NumberConstants.NUM30, unique = true)
	@JsonView(DataTablesOutput.View.class)
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Sets the value of the attribute {@link #userName}.
	 * @param userNameP The value for the attribute {@link #userName}.
	 */
	public void setUserName(final String userNameP) {
		this.userName = userNameP;
	}

	/**
	 * Gets the value of the attribute {@link #email}.
	 * @return the value of the attribute {@link #email}.
	 */
	@Column(name = "CORREO_ELEC", nullable = true, length = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getEmail() {
		return this.email;
	}

	/**
	 * Sets the value of the attribute {@link #email}.
	 * @param emailP The value for the attribute {@link #email}.
	 */
	public void setEmail(final String emailP) {
		this.email = emailP;
	}

	/**
	 * Gets the value of the attribute {@link #phone}.
	 * @return the value of the attribute {@link #phone}.
	 */
	@Column(name = "TELF_CONTACTO", nullable = true, length = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getPhone() {
		return this.phone;
	}

	/**
	 * Sets the value of the attribute {@link #phone}.
	 * @param phoneP The value for the attribute {@link #phone}.
	 */
	public void setPhone(final String phoneP) {
		this.phone = phoneP;
	}
	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	@Column(name = "NOMBRE", nullable = false, length = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameP The value for the attribute {@link #name}.
	 */
	public void setName(final String nameP) {
		this.name = nameP;
	}

	/**
	 * Gets the value of the attribute {@link #password}.
	 * @return the value of the attribute {@link #password}.
	 */
	@Column(name = "CLAVE", nullable = true, length = NumberConstants.NUM100)
	@JsonView(DataTablesOutput.View.class)
	public String getPassword() {
		return this.password;
	}

	/**
	 * Sets the value of the attribute {@link #password}.
	 * @param passwordP The value for the attribute {@link #password}.
	 */
	public void setPassword(final String passwordP) {
		this.password = passwordP;
	}

	/**
	 * Gets the value of the attribute {@link #surnames}.
	 * @return the value of the attribute {@link #surnames}.
	 */
	@Column(name = "APELLIDOS", nullable = false, length = NumberConstants.NUM120)
	@JsonView(DataTablesOutput.View.class)
	public String getSurnames() {
		return this.surnames;
	}

	/**
	 * Sets the value of the attribute {@link #surnames}.
	 * @param surnamesP The value for the attribute {@link #surnames}.
	 */
	public void setSurnames(final String surnamesP) {
		this.surnames = surnamesP;
	}

	/**
	 * Gets the value of the attribute {@link #startDate}.
	 * @return the value of the attribute {@link #startDate}.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FEC_ALTA", nullable = false, length = NumberConstants.NUM6)
	public Date getStartDate() {
		return this.startDate;
	}

	/**
	 * Sets the value of the attribute {@link #startDate}.
	 * @param startDateP The value for the attribute {@link #startDate}.
	 */
	public void setStartDate(final Date startDateP) {
		this.startDate = startDateP;
	}

	/**
	 * Gets the value of the attribute {@link #root}.
	 * @return the value of the attribute {@link #root}.
	 */
   @Column(name = "USU_DEFECTO", nullable = false)
   public Boolean getRoot() {
	return this.root;
   }

   /**
	 * Sets the value of the attribute {@link #root}.
	 * @param rootP The value for the attribute {@link #root}.
	 */
   public void setRoot(final Boolean rootP) {
	this.root = rootP;
   }

   /**
	 * Gets the value of the attribute {@link #renovationCode}.
	 * @return the value of the attribute {@link #renovationCode}.
	 */
	@Column(name = "CODIGO_RENOVACION", nullable = true, unique = true, length = NumberConstants.NUM100)
	public String getRenovationCode() {
		return this.renovationCode;
	}

	/**
	 * Sets the value of the attribute {@link #renovationCode}.
	 * @param renovationCodeP The value for the attribute {@link #renovationCode}.
	 */
	public void setRenovationCode(final String renovationCodeP) {
		this.renovationCode = renovationCodeP;
	}

	/**
	 * Gets the value of the attribute {@link #renovationDate}.
	 * @return the value of the attribute {@link #renovationDate}.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FEC_RENOVACION", nullable = false, length = NumberConstants.NUM6)
	public Date getRenovationDate() {
		return this.renovationDate;
	}

	/**
	 * Sets the value of the attribute {@link #renovationDate}.
	 * @param renovationDateP The value for the attribute {@link #renovationDate}.
	 */
	public void setRenovationDate(final Date renovationDateP) {
		this.renovationDate = renovationDateP;
	}

	/**
	 * Gets the value of the attribute {@link #restPassword}.
	 * @return the value of the attribute {@link #restPassword}.
	 */
   @Column(name = "REST_CLAVE", nullable = false)
   public Boolean getRestPassword() {
	return this.restPassword;
   }

   /**
	 * Sets the value of the attribute {@link #restPassword}.
	 * @param restPasswordP The value for the attribute {@link #restPassword}.
	 */
   public void setRestPassword(final Boolean restPasswordP) {
	this.restPassword = restPasswordP;
   }

   /**
	 * Gets the value of the attribute {@link #rol}.
	 * @return the value of the attribute {@link #rol}.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "FK_ROL", nullable = false)
	@JsonView(DataTablesOutput.View.class)
	public Rol getRol() {
		return this.rol;
	}

	/**
	 * Sets the value of the attribute {@link #rol}.
	 * @param rolP The value for the attribute {@link #rol}.
	 */
	public void setRol(final Rol rolP) {
		this.rol = rolP;
	}

	/**
	 * Gets the value of the attribute {@link #dni}.
	 * @return the value of the attribute {@link #dni}.
	 */
	@Column(name = "DNI", nullable = true, length = NumberConstants.NUM9)
	public String getDni() {
		return dni;
	}

	/**
	 * Sets the value of the attribute {@link #dni}.
	 * @param dni The value for the attribute {@link #dni}.
	 */
	public void setDni(String dni) {
		this.dni = dni;
	}

	/**
	 * Gets the value of the attribute {@link #fecUltimoAcceso}.
	 * @return the value of the attribute {@link #fecUltimoAcceso}.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FEC_ULTIMO_ACCESO", nullable = false, length = NumberConstants.NUM6)
	public Date getFecUltimoAcceso() {
		return fecUltimoAcceso;
	}

	/**
	 * Sets the value of the attribute {@link #fecUltimoAcceso}.
	 * @param fecUltimoAcceso The value for the attribute {@link #fecUltimoAcceso}.
	 */
	public void setFecUltimoAcceso(Date fecUltimoAcceso) {
		this.fecUltimoAcceso = fecUltimoAcceso;
	}
	
}