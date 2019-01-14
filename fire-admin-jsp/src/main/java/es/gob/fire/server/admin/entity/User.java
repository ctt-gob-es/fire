package es.gob.fire.server.admin.entity;

import java.sql.Date;

/**
 * Usuarios
 * @author Adolfo.Navarro
 *
 */
public class User {
	private String id;
	private String nombreUsuario;
	private String clave;
	private String nombre;
	private String apellidos;
	private String correo;
	private String telefono;
	private String rol;
	private Date fechaAlta;
	private String porDefecto;

	/**
	 * Construcctor de Usuario
	 */
	public User() {}


	/**
	 * Constructor con los campos necesarios para la insertci&oacute;n de la entidad en bbdd
	 * @param nombre_usuario
	 * @param clave
	 * @param nombre
	 * @param apellidos
	 * @param rol
	 */
	public User(final String nombre_usuario, final String clave, final String nombre, final String apellidos, final String rol) {
		super();
		this.nombreUsuario = nombre_usuario;
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
		this.id = id_usuario;
		this.nombreUsuario = nombre_usuario;
		this.clave = clave;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.correo = correo_elec;
		this.telefono = telf_contacto;
		this.rol = rol;
		this.fechaAlta = fec_alta;
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
	public final String getNombreUsuario() {
		return this.nombreUsuario;
	}
/**
 * Estableceel Nombre del usuario.
 * @param nombre Nombre del usuario.
 */
	public final void setNombreUsuario(final String nombre) {
		this.nombreUsuario = nombre;
	}
	/**
	 * Obtiene la clave del usuario.
	 * @return Clave del usuario.
	 */
	public final String getClave() {
		return this.clave;
	}
	/**
	 * Establece la clave del usuario.
	 * @param clave Clave del usuario.
	 */
	public final void setClave(final String clave) {
		this.clave = clave;
	}
	/**
	 * Obtiene la fecha de alta del usuario.
	 * @return Fecha de alta del usuario.
	 */
	public final Date getFechaAlta() {
		return this.fechaAlta;
	}
	/**
	 * Establece la fecha de alta del usuario.
	 * @param fecha Fecha de alta del usuario.
	 */
	public final void setFechaAlta(final Date fecha) {
		this.fechaAlta = fecha;
	}
	/**
	 * Obtiene el nombre de pila del usuario.
	 * @return Nombre de pila del usuario.
	 */
	public final String getNombre() {
		return this.nombre;
	}
	/**
	 * Establece el nombre de pila del usuario.
	 * @param nombre Nombre de pila del usuario.
	 */
	public final void setNombre(final String nombre) {
		this.nombre = nombre;
	}
	/**
	 * Obtiene los apellidos del usuario.
	 * @return Apellidos del usuario.
	 */
	public final String getApellidos() {
		return this.apellidos;
	}
	/**
	 * Establece los apellidos del usuario.
	 * @param apellidos Apellidos del usuario.
	 */
	public final void setApellidos(final String apellidos) {
		this.apellidos = apellidos;
	}
	/**
	 * Obtiene el correo electr&oacute;nico del usuario.
	 * @return Correo electr&oacute;nico.
	 */
	public final String getCorreo() {
		return this.correo;
	}
	/**
	 * Establece el correo electr&oacute;nico del usuario.
	 * @param correo Correo electr&oacute;nico.
	 */
	public final void setCorreo_elec(final String correo) {
		this.correo = correo;
	}
	/**
	 * Obtiene el tel&eacute;fono de contacto del usuario.
	 * @return Tel&eacute;fono de contacto.
	 */
	public final String getTelefono() {
		return this.telefono;
	}
	/**
	 * Establece el tel&eacute;fono de contacto del usuario.
	 * @param telefono Tel&eacute;fono de contacto
	 */
	public final void setTelfefono(final String telefono) {
		this.telefono = telefono;
	}
	/**
	 * Obtiene el rol (tipo de usuario en el sistema)
	 * @return Rol del usuario.
	 */
	public final String getRol() {
		return this.rol;
	}
	/**
	 * Establece el rol (tipo de usuario en el sistema)
	 * @param rol Rol del usuario.
	 */
	public final void setRol(final String rol) {
		this.rol = rol;
	}


	/**
	 * Indica si el usuario es el por defecto ("1") o no ("0").
	 * @return Devuelve "1" si el usuario es el por defecto (el usuario original)
	 * y "0" si no lo es.
	 */
	public final String getPorDefecto() {
		return this.porDefecto;
	}

	/**
	 * Establece si un usuario es el por defecto ("1") o no ("0").
	 * @param porDefecto Valor que determina si el usuario es el por defecto o no.
	 */
	public final void setPorDefecto(final String porDefecto) {
		this.porDefecto = porDefecto;
	}


}
