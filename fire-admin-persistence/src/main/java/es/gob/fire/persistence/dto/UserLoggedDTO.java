package es.gob.fire.persistence.dto;

import org.springframework.stereotype.Component;

@Component
public class UserLoggedDTO {
	
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
	private String startDate;

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
	private String renovationDate;

	/**
	 * Attribute that represents the restPassword.
	 */
	private Boolean restPassword;

	/**
	 * Attribute that represents the rol.
	 */
	private Long idRol;

	/**
	 * Attribute that represents the DNI.
	 */
	private String dni;
	
	/**
	 * Attribute that represents the date last access.
	 */
	private String fecUltimoAcceso;

	/**
	 * Constructor default
	 */
	public UserLoggedDTO() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Gets the value of the attribute {@link #userId}.
	 * @return the value of the attribute {@link #userId}.
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * Sets the value of the attribute {@link #userId}.
	 * @param userIdP The value for the attribute {@link #userId}.
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * Gets the value of the attribute {@link #userName}.
	 * @return the value of the attribute {@link #userName}.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the value of the attribute {@link #userName}.
	 * @param userName The value for the attribute {@link #userName}.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the value of the attribute {@link #email}.
	 * @return the value of the attribute {@link #email}.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the value of the attribute {@link #email}.
	 * @param email The value for the attribute {@link #email}.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param email The value for the attribute {@link #name}.
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @param email The value for the attribute {@link #password}.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the value of the attribute {@link #surnames}.
	 * @return the value of the attribute {@link #surnames}.
	 */
	public String getSurnames() {
		return surnames;
	}

	/**
	 * Sets the value of the attribute {@link #surnames}.
	 * @param email The value for the attribute {@link #surnames}.
	 */
	public void setSurnames(String surnames) {
		this.surnames = surnames;
	}

	/**
	 * Gets the value of the attribute {@link #phone}.
	 * @return the value of the attribute {@link #phone}.
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Sets the value of the attribute {@link #phone}.
	 * @param phone The value for the attribute {@link #phone}.
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Gets the value of the attribute {@link #startDate}.
	 * @return the value of the attribute {@link #startDate}.
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * Sets the value of the attribute {@link #startDate}.
	 * @param startDate The value for the attribute {@link #startDate}.
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * Gets the value of the attribute {@link #root}.
	 * @return the value of the attribute {@link #root}.
	 */
	public Boolean getRoot() {
		return root;
	}

	/**
	 * Sets the value of the attribute {@link #root}.
	 * @param root The value for the attribute {@link #root}.
	 */
	public void setRoot(Boolean root) {
		this.root = root;
	}

	/**
	 * Gets the value of the attribute {@link #renovationCode}.
	 * @return the value of the attribute {@link #renovationCode}.
	 */
	public String getRenovationCode() {
		return renovationCode;
	}

	/**
	 * Sets the value of the attribute {@link #renovationCode}.
	 * @param renovationCode The value for the attribute {@link #renovationCode}.
	 */
	public void setRenovationCode(String renovationCode) {
		this.renovationCode = renovationCode;
	}

	/**
	 * Gets the value of the attribute {@link #renovationDate}.
	 * @return the value of the attribute {@link #renovationDate}.
	 */
	public String getRenovationDate() {
		return renovationDate;
	}

	/**
	 * Sets the value of the attribute {@link #renovationDate}.
	 * @param renovationDate The value for the attribute {@link #renovationDate}.
	 */
	public void setRenovationDate(String renovationDate) {
		this.renovationDate = renovationDate;
	}

	/**
	 * Gets the value of the attribute {@link #restPassword}.
	 * @return the value of the attribute {@link #restPassword}.
	 */
	public Boolean getRestPassword() {
		return restPassword;
	}

	/**
	 * Sets the value of the attribute {@link #restPassword}.
	 * @param restPassword The value for the attribute {@link #restPassword}.
	 */
	public void setRestPassword(Boolean restPassword) {
		this.restPassword = restPassword;
	}

	/**
	 * Gets the value of the attribute {@link #idRol}.
	 * @return the value of the attribute {@link #idRol}.
	 */
	public Long getIdRol() {
		return idRol;
	}

	/**
	 * Sets the value of the attribute {@link #idRol}.
	 * @param idRol The value for the attribute {@link #idRol}.
	 */
	public void setIdRol(Long idRol) {
		this.idRol = idRol;
	}

	/**
	 * Gets the value of the attribute {@link #dni}.
	 * @return the value of the attribute {@link #dni}.
	 */
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
	public String getFecUltimoAcceso() {
		return fecUltimoAcceso;
	}

	/**
	 * Sets the value of the attribute {@link #fecUltimoAcceso}.
	 * @param fecUltimoAcceso The value for the attribute {@link #fecUltimoAcceso}.
	 */
	public void setFecUltimoAcceso(String fecUltimoAcceso) {
		this.fecUltimoAcceso = fecUltimoAcceso;
	}
	
}
