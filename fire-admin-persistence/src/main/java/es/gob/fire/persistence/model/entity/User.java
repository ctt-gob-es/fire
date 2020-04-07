package es.gob.fire.persistence.model.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonView;

//import es.gob.fire.util.constant.NumberConstants;
//import es.gob.fire.util.StatusCertificateEnum;

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
	 * Attribute that represents the object ID.
	 */
	private Long id;

	/**
	 * Attribute that represents the name for access to the platform.
	 */
	private String username;
	
	/**
	 * Attribute that represents the email of the user.
	 */
	private String email;

	/**
	 * Attribute that represents the user name.
	 */
	private String name;

	/**
	 * Attribute that represents the hash of the user password.
	 */
	private String password;

	/**
	 * Attribute that represents the surnames of the user.
	 */
	private String surnames;

	/**
	 * Attribute that represents the telefono of the user.
	 */
	private Integer telefono;
	
	/**
	 * Attribute that represents the startDate of the user.
	 */
	private Date startDate;
	
	/**
	 * Attribute that represents the root of the user.
	 */
	private Boolean root;
	
	/**
	 * Attribute that represents the renovationCode of the user.
	 */
	private String renovationCode;
	
	/**
	 * Attribute that represents the renovationDate of the user.
	 */
	private Date renovationDate;
	
	/**
	 * Attribute that represents the restPassword of the user.
	 */
	private Boolean restPassword;

	/**
	 * Attribute that represents the system certificates of the user.
	 */
	//private List<SystemCertificate> systemCertificates;

	/**
	 * Gets the value of the attribute {@link #idUserMonitoriza}.
	 * @return the value of the attribute {@link #idUserMonitoriza}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Id
	@Column(name = "ID_USUARIO", unique = true, nullable = false, precision = 11)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonView(DataTablesOutput.View.class)
	public Long getIdUser() {
		// CHECKSTYLE:ON
		return this.id;
	}

	/**
	 * Sets the value of the attribute {@link #idUserMonitoriza}.
	 * @param idUserMonitorizaP The value for the attribute {@link #idUserMonitoriza}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setIdUser(final Long idParam) {
		// CHECKSTYLE:ON
		this.id = idParam;
	}



	/**
	 * Gets the value of the attribute {@link #login}.
	 * @return the value of the attribute {@link #login}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Column(name = "NOMBRE_USUARIO", nullable = false, length = 30, unique = true)
	@JsonView(DataTablesOutput.View.class)
	public String getUsername() {
		return this.username;
	}

	/**
	 * Sets the value of the attribute {@link #login}.
	 * @param loginParam The value for the attribute {@link #login}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setUsername(final String usernameParam) {
		this.username = usernameParam;
	}

	/**
	 * Gets the value of the attribute {@link #email}.
	 * @return the value of the attribute {@link #email}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Column(name = "CORREO_ELEC", nullable = true, length = 45)
	@JsonView(DataTablesOutput.View.class)
	public String getEmail() {
		// CHECKSTYLE:ON
		return this.email;
	}

	/**
	 * Sets the value of the attribute {@link #mail}.
	 * @param mailParam The value for the attribute {@link #mail}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setEmail(final String mailParam) {
		// CHECKSTYLE:ON
		this.email = mailParam;
	}
	/**
	 * Gets the value of the attribute {@link #email}.
	 * @return the value of the attribute {@link #email}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Column(name = "TELF_CONTACTO", nullable = true, length = 45)
	@JsonView(DataTablesOutput.View.class)
	public Integer getTelephone() {
		// CHECKSTYLE:ON
		return this.telefono;
	}

	/**
	 * Sets the value of the attribute {@link #mail}.
	 * @param mailParam The value for the attribute {@link #mail}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setTelephone(final Integer telfParam) {
		// CHECKSTYLE:ON
		this.telefono = telfParam;
	}
	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Column(name = "NOMBRE", nullable = false, length = 45)
	@JsonView(DataTablesOutput.View.class)
	public String getName() {
		// CHECKSTYLE:ON
		return this.name;
	}

	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameParam The value for the attribute {@link #name}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setName(final String nameParam) {
		// CHECKSTYLE:ON
		this.name = nameParam;
	}

	/**
	 * Gets the value of the attribute {@link #password}.
	 * @return the value of the attribute {@link #password}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Column(name = "CLAVE", nullable = false, length = 2000)
	@JsonView(DataTablesOutput.View.class)
	public String getPassword() {
		// CHECKSTYLE:ON
		return this.password;
	}

	/**
	 * Sets the value of the attribute {@link #password}.
	 * @param passwordParam The value for the attribute {@link #password}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setPassword(final String passwordParam) {
		// CHECKSTYLE:ON
		this.password = passwordParam;
	}

	/**
	 * Gets the value of the attribute {@link #surnames}.
	 * @return the value of the attribute {@link #surnames}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	@Column(name = "APELLIDOS", nullable = false, length = 150)
	@JsonView(DataTablesOutput.View.class)
	public String getSurnames() {
		// CHECKSTYLE:ON
		return this.surnames;
	}

	/**
	 * Sets the value of the attribute {@link #surnames}.
	 * @param surnamesParam The value for the attribute {@link #surnames}.
	 */
	// CHECKSTYLE:OFF -- Checkstyle rule "Design for Extension" is not applied
	// because Hibernate JPA needs not final access methods.
	public void setSurnames(final String surnamesParam) {
		// CHECKSTYLE:ON
		this.surnames = surnamesParam;
	}
	
	/**
	 * Get the startDate.
	 * @return startDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FEC_ALTA", nullable = false, length = 6)
	@NotNull
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Set startDate.
	 * @param startDateP set the startDate
	 */
	public void setStartDate(final Date startDateP) {
		this.startDate = startDateP;
	}

	/**
	 * Get the root.
	 * @return root
	 */
   @Column(name = "USU_DEFECTO", nullable = false)
   @NotNull
   public Boolean getRoot() {
	return root;
   }

   /**
	 * Set root.
	 * @param rootP set the root
	 */
   public void setRoot(Boolean rootP) {
	this.root = rootP;
   }

   /**
	 * Get the renovationCode.
	 * @return renovationCode
	 */
	@Column(name = "CODIGO_RENOVACION", nullable = true, length = 90)
	@Size(max = 90)
	public String getRenovationCode() {
		return renovationCode;
	}

	/**
	 * Set renovationCode.
	 * @param renovationCodeP set the renovationCode
	 */
	public void setRenovationCode(final String renovationCodeP) {
		this.renovationCode = renovationCodeP;
	}

	/**
	 * Get the renovationDate.
	 * @return renovationDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FEC_RENOVACION", nullable = false, length = 6)
	@NotNull
	public Date getRenovationDate() {
		return renovationDate;
	}

	/**
	 * Set renovationDate.
	 * @param renovationDateP set the renovationDate
	 */
	public void setRenovationDate(final Date renovationDateP) {
		this.renovationDate = renovationDateP;
	}
	
	/**
	 * Get the restPassword.
	 * @return restPassword
	 */
   @Column(name = "REST_CLAVE", nullable = false)
   @NotNull
   public Boolean getRestPassword() {
	return restPassword;
   }

   /**
	 * Set restPassword.
	 * @param restPasswordP set the restPassword
	 */
   public void setRestPassword(Boolean restPasswordP) {
	this.restPassword = restPasswordP;
   }
	
}

/*@Entity
@Table(name = "TB_USUARIOS")
@SequenceGenerator(name = "TB_USUARIOS_SE", initialValue = 1, allocationSize = 1)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class User implements Serializable {

	*//**
	 * Attribute that represents the serial number.
	 *//*
	private static final long serialVersionUID = 7884993085036937451L;

	private Long idUser;
	private String username;
	private String password;
	private String firstName;*
	private String surname;
	private String mail;
	private String telephone;
	private Date startDate;*
	private Boolean root;*
	private String renovationCode;*
	private Date renovationDate;*
	private Boolean restPassword;*
	
	private Long id;
	private String username;
	private String email;
	private String name;
	private String password;
	private String surnames;
	private Integer telefono;
	
	//private Long role;
	//private CertificateFire certificate;
	//private List<Application> responsibles;
	//private RolePermissions permissions;

	*//**
	 * Constructor method for the class User.java. 
	 *//*
	public User() {

	}

	*//**
	 * Constructor con los campos necesarios para la insertci&oacute;n de la entidad en bbdd
	 * @param username
	 * @param password
	 * @param name
	 * @param surname
	 * @param role
	 *//*
	public User(final String username, final String password, final String name, final String surname, final Long role) {
		super();
		this.username = username;
		this.password = password;
		this.firstName = name;
		this.surname = surname;
		//this.role = role;
		this.root = false;
	}


	*//**
	 * Constructor con todos los campos
	 * @param idUser
	 * @param username
	 * @param password
	 * @param name
	 * @param surname
	 * @param mail
	 * @param contacPhone
	 * @param role
	 * @param datahigh
	 * @param codeRenovation
	 * @param renovationDate
	 *//*
	public User(final Long idUser, final String username, final String password, final String name, final String surname,
			final String mail, final String contacPhone, final Long role, final Date datahigh) {
		super();
		this.idUser = idUser;
		this.username = username;
		this.password = password;
		this.firstName = name;
		this.surname = surname;
		this.mail = mail;
		this.telephone = contacPhone;
		//this.role = role;
		this.startDate = datahigh;
		this.root = false;

	}

	*//**
	 * Get the userId.
	 * @return userId
	 *//*
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TB_USUARIOS_SE")
	@Column(name = "ID_USUARIO", unique = true, nullable = false)
	public Long getIdUser() {
		return idUser;
	}

	*//**
	 * Set idUser.
	 * @param idUserP set the idUser
	 *//*
	public void setIdUser(final Long idUserP) {
		this.idUser = idUserP;
	}

	*//**
	 * Get the username.
	 * @return username
	 *//*
	@Column(name = "NOMBRE_USUARIO", nullable = false, length = 30)
	@NotNull
	@Size(max = 30)
	public String getUsername() {
		return username;
	}

	*//**
	 * Set username.
	 * @param usernameP set the username
	 *//*
	public void setUsername(final String usernameP) {
		this.username = usernameP;
	}

	*//**
	 * Get the password.
	 * @return password
	 *//*
	@Column(name = "CLAVE", nullable = false, length = 45)
	@NotNull
	@Size(max = 45)
	public String getPassword() {
		return password;
	}

	*//**
	 * Set password.
	 * @param passwordP set the password
	 *//*
	public void setPassword(final String passwordP) {
		this.password = passwordP;
	}
	
	*//**
	 * Get the firstName.
	 * @return firstName
	 *//*
	@Column(name = "NOMBRE", nullable = false, length = 45)
	@NotNull
	@Size(max = 45)
	public String getFirstName() {
		return firstName;
	}

	*//**
	 * Set firstName.
	 * @param firstNameP set the firstName
	 *//*
	public void setFirstName(final String firstNameP) {
		this.firstName = firstNameP;
	}
	
	*//**
	 * Get the surname.
	 * @return surname
	 *//*
	@Column(name = "APELLIDOS", nullable = false, length = 120)
	@NotNull
	@Size(max = 120)
	public String getSurname() {
		return surname;
	}

	*//**
	 * Set surname.
	 * @param surnameP set the surname
	 *//*
	public void setSurname(final String surnameP) {
		this.surname = surnameP;
	}
	
	*//**
	 * Get the mail.
	 * @return mail
	 *//*
	@Column(name = "CORREO_ELEC", nullable = true, length = 45)
	@Size(max = 45)
	public String getMail() {
		return mail;
	}

	*//**
	 * Set mail.
	 * @param mailP set the mail
	 *//*
	public void setMail(final String mailP) {
		this.mail = mailP;
	}
	
	*//**
	 * Get the telephone.
	 * @return telephone
	 *//*
	@Column(name = "TELF_CONTACTO", nullable = true, length = 45)
	@Size(max = 45)
	public String getTelephone() {
		return telephone;
	}

	*//**
	 * Set telephone.
	 * @param telephoneP set the telephone
	 *//*
	public void setTelephone(final String telephoneP) {
		this.telephone = telephoneP;
	}

	*//**
	 * Get the startDate.
	 * @return startDate
	 *//*
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FEC_ALTA", nullable = false, length = 6)
	@NotNull
	public Date getStartDate() {
		return startDate;
	}

	*//**
	 * Set startDate.
	 * @param startDateP set the startDate
	 *//*
	public void setStartDate(final Date startDateP) {
		this.startDate = startDateP;
	}

	*//**
	 * Get the root.
	 * @return root
	 *//*
    @Column(name = "USU_DEFECTO", nullable = false)
    @NotNull
    public Boolean getRoot() {
	return root;
    }

    *//**
	 * Set root.
	 * @param rootP set the root
	 *//*
    public void setRoot(Boolean rootP) {
	this.root = rootP;
    }

    *//**
	 * Get the renovationCode.
	 * @return renovationCode
	 *//*
	@Column(name = "CODIGO_RENOVACION", nullable = true, length = 90)
	@Size(max = 90)
	public String getRenovationCode() {
		return renovationCode;
	}

	*//**
	 * Set renovationCode.
	 * @param renovationCodeP set the renovationCode
	 *//*
	public void setRenovationCode(final String renovationCodeP) {
		this.renovationCode = renovationCodeP;
	}

	*//**
	 * Get the renovationDate.
	 * @return renovationDate
	 *//*
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FEC_RENOVACION", nullable = false, length = 6)
	@NotNull
	public Date getRenovationDate() {
		return renovationDate;
	}

	*//**
	 * Set renovationDate.
	 * @param renovationDateP set the renovationDate
	 *//*
	public void setRenovationDate(final Date renovationDateP) {
		this.renovationDate = renovationDateP;
	}
	
	*//**
	 * Get the restPassword.
	 * @return restPassword
	 *//*
    @Column(name = "REST_CLAVE", nullable = false)
    @NotNull
    public Boolean getRestPassword() {
	return restPassword;
    }

    *//**
	 * Set restPassword.
	 * @param restPasswordP set the restPassword
	 *//*
    public void setRestPassword(Boolean restPasswordP) {
	this.restPassword = restPasswordP;
    }
	
}*/