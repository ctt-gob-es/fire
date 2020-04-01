package es.gob.fire.persistence.configuration.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(name = "\"tb_usuarios\"")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class User implements Serializable {

	/**
	 * Constant attribute that represents the string <i>"yes_no"</i>.
	 */
	private static final String CONS_YES_NO = "yes_no";

	/**
	 * Constant attribute that represents the string <i>"Si"</i>.
	 */
	private static final String CONS_SI = "S\u00ED";

	/**
	 * Constant attribute that represents the string <i>"No"</i>.
	 */
	private static final String CONS_NO = "No";

	/**
	 * Class serial version.
	 */
	private static final long serialVersionUID = -60419018366799736L;

	/**
	 * Attribute that represents the object ID.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "\"id_usuario\"")
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
	@Column(name = "id_usuario", unique = true, nullable = false, precision = 19)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
//	@GenericGenerator(name = "tb_usuarios", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "SQ_USER_MONITORIZA"), @Parameter(name = "initial_value", value = "1"), @Parameter(name = "increment_size", value = "1") })
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
	@Column(name = "nombre_usuario", nullable = false, length = 100, unique = true)
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
	@Column(name = "correo_elec", nullable = false, length = 150)
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
	@Column(name = "telf_contacto", nullable = false, length =150)
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
	@Column(name = "nombre", nullable = false, length = 100)
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
	@Column(name = "clave", nullable = false, length = 512)
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
	@Column(name = "apellidos", nullable = false, length = 150)
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



}