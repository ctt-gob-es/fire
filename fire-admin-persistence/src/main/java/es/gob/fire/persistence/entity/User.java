package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.core.constant.NumberConstants;

/**
 * <p>Class that maps the <i>USER_MONITORIZA</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.2, 25/01/2019.
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
	private Integer phone;
	
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
	private List<Rol> rol;
	
	/**
	 * Attribute that represents the system certificates.
	 */
	//private List<SystemCertificate> systemCertificates;

	/**
	 * Gets the value of the attribute {@link #userId}.
	 * @return the value of the attribute {@link #userId}.
	 */
	@Id
	@Column(name = "ID_USUARIO", unique = true, nullable = false, precision = NumberConstants.NUM11)
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	@NotNull
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
	@Size(max = NumberConstants.NUM30)
	@NotNull
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
	@Size(max = NumberConstants.NUM45)
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
	public Integer getPhone() {
		return this.phone;
	}

	/**
	 * Sets the value of the attribute {@link #phone}.
	 * @param phoneP The value for the attribute {@link #phone}.
	 */
	public void setPhone(final Integer phoneP) {
		this.phone = phoneP;
	}
	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	@Column(name = "NOMBRE", nullable = false, length = NumberConstants.NUM45)
	@Size(max = NumberConstants.NUM45)
	@NotNull
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
	 * Gets the value of the attribute {@link #surnames}.
	 * @return the value of the attribute {@link #surnames}.
	 */
	@Column(name = "APELLIDOS", nullable = false, length = NumberConstants.NUM150)
	@Size(max = NumberConstants.NUM150)
	@NotNull
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
	 * Gets the value of the attribute {@link #password}.
	 * @return the value of the attribute {@link #password}.
	 */
	@Column(name = "CLAVE", nullable = false, length = NumberConstants.NUM2000)
	@Size(max = NumberConstants.NUM2000)
	@NotNull
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
	 * Gets the value of the attribute {@link #startDate}.
	 * @return the value of the attribute {@link #startDate}.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FEC_ALTA", nullable = false, length = NumberConstants.NUM6)
	@NotNull
	public Date getStartDate() {
		return startDate;
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
   @NotNull
   public Boolean getRoot() {
	return root;
   }

   /**
	 * Sets the value of the attribute {@link #root}.
	 * @param rootP The value for the attribute {@link #root}.
	 */
   public void setRoot(Boolean rootP) {
	this.root = rootP;
   }

   /**
	 * Gets the value of the attribute {@link #renovationCode}.
	 * @return the value of the attribute {@link #renovationCode}.
	 */
	@Column(name = "CODIGO_RENOVACION", nullable = true, unique = true, length = NumberConstants.NUM100)
	@Size(max = NumberConstants.NUM100)
	public String getRenovationCode() {
		return renovationCode;
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
	@NotNull
	public Date getRenovationDate() {
		return renovationDate;
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
   @NotNull
   public Boolean getRestPassword() {
	return restPassword;
   }

   /**
	 * Sets the value of the attribute {@link #restPassword}.
	 * @param restPasswordP The value for the attribute {@link #restPassword}.
	 */
   public void setRestPassword(Boolean restPasswordP) {
	this.restPassword = restPasswordP;
   }
   

	/**
	 * Gets the value of the attribute {@link #rol}.
	 * @return the value of the attribute {@link #rol}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	//@OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
   @OneToMany(mappedBy="user", cascade={CascadeType.ALL})
	
	public List<Rol> getRol() {
		// CHECKSTYLE:ON
		return rol;
	}

	/**
	 * Sets the value of the attribute {@link #rol}.
	 * @param systemCertificates The value for the attribute {@link #rol}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setRol(List<Rol> rolP) {
		// CHECKSTYLE:ON
		this.rol = rolP;
	}

	
}