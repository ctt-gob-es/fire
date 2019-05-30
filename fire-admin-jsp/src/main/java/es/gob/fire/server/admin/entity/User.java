package es.gob.fire.server.admin.entity;

import java.sql.Date;

import es.gob.fire.server.admin.service.RolePermissions;

/**
 * Usuarios
 * @author Adolfo.Navarro
 *
 */
public class User {
	private String id;
	private String userName;
	private String password;
	private String name;
	private String surname;
	private String mail;
	private String telephone;
	private int role;
	private Date startdate;
	private boolean root;
	private CertificateFire certificate;
	private Application responsibleName;




	private RolePermissions permissions;

	/**
	 * Constructor de Usuario
	 */
	public User() {
		this.id = null;
		this.userName = null;
		this.password = null;
		this.name = null;
		this.surname = null;
		this.mail = null;
		this.telephone = null;
		this.role = 0;
		this.startdate = null;
		this.root = false;
		this.responsibleName = null;
		this.certificate = null;



	}


	/**
	 * Constructor con los campos necesarios para la insertci&oacute;n de la entidad en bbdd
	 * @param user_name
	 * @param password
	 * @param name
	 * @param surname
	 * @param role
	 */
	public User(final String user_name, final String password, final String name, final String surname, final int role) {
		super();
		this.userName = user_name;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.role = role;
		this.root = false;
	}


	/**
	 * Constructor con todos los campos
	 * @param id_user
	 * @param user_name
	 * @param password
	 * @param name
	 * @param surname
	 * @param mail
	 * @param contac_phone
	 * @param role
	 * @param datahigh
	 * @param codeRenovation
	 * @param renovationDate
	 */
	public User(final String id_user, final String user_name, final String password, final String name, final String surname,
			final String mail, final String contac_phone, final int role, final Date datahigh
			) {
		super();
		this.id = id_user;
		this.userName = user_name;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.mail = mail;
		this.telephone = contac_phone;
		this.role = role;
		this.startdate = datahigh;

		this.root = false;

	}



	/**
	 * Obtiene el id de usuario.
	 * @return Id del usuario.
	 */
	public final String getId() {
		return this.id;
	}
	/**
	 * establece el id de usuario.
	 * @param id Id de usuario.
	 */
	public final void setId(final String id) {
		this.id = id;
	}
	/**
	 * Obtiene el nombre del usuario.
	 * @return Nombre del usuario.
	 */
	public final String getUserName() {
		return this.userName;
	}
	/**
	 * Establece el Nombre del usuario.
	 * @param name Nombre del usuario.
	 */
	public final void setUserName(final String name) {
		this.userName = name;
	}
	/**
	 * Obtiene la clave del usuario.
	 * @return Clave del usuario.
	 */
	public final String getPassword() {
		return this.password;
	}
	/**
	 * Establece la clave del usuario.
	 * @param password Clave del usuario.
	 */
	public final void setpassword(final String password) {
		this.password = password;
	}
	/**
	 * Obtiene la fecha de alta del usuario.
	 * @return Fecha de alta del usuario.
	 */
	public final Date getStartDate() {
		return this.startdate;
	}
	/**
	 * Establece la fecha de alta del usuario.
	 * @param date Fecha de alta del usuario.
	 */
	public final void setStartDate(final Date date) {
		this.startdate = date;
	}
	/**
	 * Obtiene el nombre de pila del usuario.
	 * @return Nombre de pila del usuario.
	 */
	public final String getName() {
		return this.name;
	}
	/**
	 * Establece el nombre de pila del usuario.
	 * @param name Nombre de pila del usuario.
	 */
	public final void setName(final String name) {
		this.name = name;
	}
	/**
	 * Obtiene los apellidos del usuario.
	 * @return Apellidos del usuario.
	 */
	public final String getSurname() {
		return this.surname;
	}
	/**
	 * Establece los apellidos del usuario.
	 * @param surname Apellidos del usuario.
	 */
	public final void setSurname(final String surname) {
		this.surname = surname;
	}
	/**
	 * Obtiene el correo electr&oacute;nico del usuario.
	 * @return Correo electr&oacute;nico.
	 */
	public final String getMail() {
		return this.mail;
	}
	/**
	 * Establece el correo electr&oacute;nico del usuario.
	 * @param mail Correo electr&oacute;nico.
	 */
	public final void setMail(final String mail) {
		this.mail = mail;
	}
	/**
	 * Obtiene el tel&eacute;fono de contacto del usuario.
	 * @return Tel&eacute;fono de contacto.
	 */
	public final String getTelephone() {
		return this.telephone;
	}
	/**
	 * Establece el tel&eacute;fono de contacto del usuario.
	 * @param telephone Tel&eacute;fono de contacto
	 */
	public final void setTelephone(final String telephone) {
		this.telephone = telephone;
	}
	/**
	 * Obtiene el rol (tipo de usuario en el sistema)
	 * @return Rol del usuario.
	 */
	public final int getRole() {
		return this.role;
	}
	/**
	 * Establece el rol (tipo de usuario en el sistema)
	 * @param role Rol del usuario.
	 */
	public final void setRole(final int role) {
		this.role = role;
	}


	/**
	 * Indica si el usuario es el superadministrador o no.
	 * @return Devuelve {@code true} si el usuario es el superaministrador, {@code false}
	 * si no lo es.
	 */
	 public final boolean isRoot() {
		return this.root;
	}

	/**
	 * Establece si un usuario es el por defecto ("1") o no ("0").
	 * @param root Valor que determina si el usuario es el por defecto o no.
	 */
	public final void setRoot(final boolean root) {
		this.root = root;
	}


	public  RolePermissions getPermissions() {
		return this.permissions;
	}


	 public void setPermissions(final RolePermissions permissions) {
		this.permissions = permissions;
	}

	 /**
	  * Obtiene el usuario responsable de la aplicaci&oacute;n
	  * @return responsibleName nombre del responsable
	  */
	   public Application getResponsibleName() {
		return this.responsibleName;
	}
	   /**
	    * Establece el usuario responsable (tipo de usuario responsable en el sistema)
	    * @param responsibleName
	    */
	   public void setResponsibleName(final Application responsibleName) {
		this.responsibleName = responsibleName;
	}
	  /**
	   *  Obtiene el certificado de la aplicaci&oacute;n
	   * @return certificate certificado de la aplicaci&oacute;n
	   */
	   public CertificateFire getCertificate() {
		return this.certificate;
	}
	   /**
	    * Establece el certificado de la aplicaci&oacute;n
	    * @param certificate certificado de la aplicaci&oacute;n
	    */
	   public void setCertificate(final CertificateFire certificate) {
		this.certificate = certificate;
	}

	@Override
	public String toString() {
		return this.name + " " + this.surname; //$NON-NLS-1$
	}



}
