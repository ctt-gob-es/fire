package es.gob.fire.server.admin.entity;

import es.gob.fire.server.admin.service.RolePermissions;

/**
 *clase para establecer los roles
 */

public class Role {

	public static final int ID_ADMIN = 1;
	public static final int ID_RESPONSIBLE = 2;
	public static final int ID_CONTACT = 3;

	private final int id;
	private final String name;
	private RolePermissions permissions;

	/**
	 * Constructor con todos los campos
	 * @param id
	 * @param name
	 */
	public Role(final int id, final String name) {
		this.id = id;
		this.name = name;
		this.permissions = new RolePermissions();
	}

	/**
	 * Obtiene el id del rol.
	 * @return id del rol.
	 */

	public int getId() {
		return this.id;
	}

	/**
	 * Obtiene el nombre del rol.
	 * @return nombre del rol.
	 */

	public String getName() {
		return this.name;
	}

	/**
	 * Obtiene los permisos del rol.
	 * @return permisos del rol.
	 */

	public RolePermissions getPermissions() {
		return this.permissions;
	}

	/**
	 * Establece los permisos del rol.
	 * @param los permisos del rol.
	 */
	public final void setPermissions(final RolePermissions permissions) {
		this.permissions = permissions;
	}

	/**
	 * Devuelve un nombre legible del rol o, si no se tiene registrado un nombre, el
	 * establecido por el propio rol.
	 * @param role Rol del que recuperar el nombre.
	 * @return Nombre legible del rol.
	 */
	public static String getRoleLegibleText(final Role role) {

		switch (role.getId()) {
		case ID_ADMIN:
			return "Administrador"; //$NON-NLS-1$
		case ID_RESPONSIBLE:
			return "Responsable"; //$NON-NLS-1$
		case ID_CONTACT:
			return "Contacto"; //$NON-NLS-1$
		default:
			return role.getName();
		}
	}
}
