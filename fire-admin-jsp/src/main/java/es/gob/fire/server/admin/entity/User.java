package es.gob.fire.server.admin.entity;

import java.sql.Date;

/**
 * Usuarios
 * @author Adolfo.Navarro
 *
 */
public class User {
	private String id_usuario;
	private String nombre_usuario;
	private String clave;
	private String nombre;
	private String apellidos;
	private String correo_elec;
	private String telf_contacto;
	private String rol;
	private Date fec_alta;
	private String usu_defecto;

	/**
	 * Construcctor de Usuario
	 */
	public User() {}


	/**
	 * Constructor con los campos necesarios para la insertci�n de la entidad en bbdd
	 * @param nombre_usuario
	 * @param clave
	 * @param nombre
	 * @param apellidos
	 * @param rol
	 */
	public User(final String nombre_usuario, final String clave, final String nombre, final String apellidos, final String rol) {
		super();
		this.nombre_usuario = nombre_usuario;
		this.clave = clave;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.rol = rol;
	}


	/**
	 * Constructor con todos los campos
	 * @param id_usuario
	 * @param nombre_usuario
	 * @param clave
	 * @param nombre
	 * @param apellidos
	 * @param correo_elec
	 * @param telf_contacto
	 * @param rol
	 * @param fec_alta
	 */
	public User(final String id_usuario, final String nombre_usuario, final String clave, final String nombre, final String apellidos,
			final String correo_elec, final String telf_contacto, final String rol, final Date fec_alta) {
		super();
		this.id_usuario = id_usuario;
		this.nombre_usuario = nombre_usuario;
		this.clave = clave;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.correo_elec = correo_elec;
		this.telf_contacto = telf_contacto;
		this.rol = rol;
		this.fec_alta = fec_alta;
	}



	/**
	 * Obtiene el id de usuario
	 * @return
	 */
	public final String getId_usuario() {
		return this.id_usuario;
	}
	/**
	 * establece el id de usuario
	 * @param id_usuario
	 */
	public final void setId_usuario(final String id_usuario) {
		this.id_usuario = id_usuario;
	}
	/**
	 * Obtiene el nombre del usuario
	 * @return
	 */
	public final String getNombre_usuario() {
		return this.nombre_usuario;
	}
/**
 * Estableceel nombre del usuario
 * @param nombre_usuario
 */
	public final void setNombre_usuario(final String nombre_usuario) {
		this.nombre_usuario = nombre_usuario;
	}
	/**
	 * Obtiene la clave del usuario
	 * @return
	 */
	public final String getClave() {
		return this.clave;
	}
	/**
	 * Establece la clave del usuario
	 * @param clave
	 */
	public final void setClave(final String clave) {
		this.clave = clave;
	}
	/**
	 * Obtiene la fecha de alta del usuario
	 * @return
	 */
	public final Date getFec_alta() {
		return this.fec_alta;
	}
	/**
	 * Establece la fecha de alta del usuario
	 * @param fec_alta
	 */
	public final void setFec_alta(final Date fec_alta) {
		this.fec_alta = fec_alta;
	}
	/**
	 * Obtiene el nombre completo del usuario
	 * @return
	 */
	public final String getNombre() {
		return this.nombre;
	}
	/**
	 * Establece el nombre completo del usuario
	 * @param nombre
	 */
	public final void setNombre(final String nombre) {
		this.nombre = nombre;
	}
	/**
	 * Obtiene los apellidos del usuario
	 * @return
	 */
	public final String getApellidos() {
		return this.apellidos;
	}
	/**
	 * Establece los apellidos del usuario
	 * @param apellidos
	 */
	public final void setApellidos(final String apellidos) {
		this.apellidos = apellidos;
	}
	/**
	 * Obtiene el correo electr�nico del usuario
	 * @return
	 */
	public final String getCorreo_elec() {
		return this.correo_elec;
	}
	/**
	 * Establece el correo electr�nico del usuario
	 * @param correo_elec
	 */
	public final void setCorreo_elec(final String correo_elec) {
		this.correo_elec = correo_elec;
	}
	/**
	 * Obtiene el tel�fono de contacto del usuario
	 * @return
	 */
	public final String getTelf_contacto() {
		return this.telf_contacto;
	}
	/**
	 * Establece el tel�fono de contacto del usuario
	 * @param telf_contacto
	 */
	public final void setTelf_contacto(final String telf_contacto) {
		this.telf_contacto = telf_contacto;
	}
	/**
	 * Obtiene el rol (tipo de usuario en el sistema)
	 * @return
	 */
	public final String getRol() {
		return this.rol;
	}
	/**
	 * Establece el rol (tipo de usuario en el sistema)
	 * @param rol
	 */
	public final void setRol(final String rol) {
		this.rol = rol;
	}


	public final String getUsu_defecto() {
		return this.usu_defecto;
	}


	public final void setUsu_defecto(final String usu_defecto) {
		this.usu_defecto = usu_defecto;
	}


}
