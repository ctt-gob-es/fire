package es.gob.fire.persistence.permissions;

/**
 * Permisos de los que puede disponer un rol de usuario.
 */
public enum Permissions {
	/** Permiso de acceso al m&oacute;dulo de administraci&oacute;n. */
	ACCESS("1"), //$NON-NLS-1$
	/** Permiso para ser responsable de una aplicaci&oacute;n. */
	RESPONSIBLE("2"); //$NON-NLS-1$

	private String id;

	/**
	 * Crea el permiso asoci&aacute;ndole un identificador.
	 * @param id
	 */
	private Permissions(final String id) {
		this.id = id;
	}

	/**
	 * Obtiene el identificador del permiso.
	 * @return Identificador del permiso.
	 */
	public String getId() {
		return this.id;
	}
}
