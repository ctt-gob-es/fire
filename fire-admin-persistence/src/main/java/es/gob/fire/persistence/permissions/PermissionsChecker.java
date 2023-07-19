package es.gob.fire.persistence.permissions;

import es.gob.fire.persistence.entity.Rol;
import es.gob.fire.persistence.entity.User;

/**
 * Clave para la comprobaci&oacute;n de los permisos de los roles y usuarios.
 */
public class PermissionsChecker {

	private static final String PERMISSION_SEPARATOR = ","; //$NON-NLS-1$

	/**
	 * Comprueba que un usuario tenga un permiso determinado.
	 * @param user Usuario del que comprobar el permiso.
	 * @param permission Permiso que debe tener.
	 * @return {@code true} si el usuario tiene el permiso, {@code false} en caso contrario.
	 */
	public static boolean hasPermission(final User user, final Permissions permission) {
		final Rol role = user.getRol();
		if (role != null) {
			return hasPermission(role, permission);
		}
		return false;
	}

	/**
	 * Comprueba que un rol tenga asociado un permiso determinado.
	 * @param role Rol del que comprobar el permiso.
	 * @param permission Permiso que debe tener.
	 * @return {@code true} si el rol tiene el asociado el permiso,
	 * {@code false} en caso contrario.
	 */
	public static boolean hasPermission(final Rol role, final Permissions permission) {
		final String permissions = role.getPermissions();
		if (permissions != null) {
			for (final String permissionId : permissions.split(PERMISSION_SEPARATOR)) {
				if (permissionId.equalsIgnoreCase(permission.getId())) {
					return true;
				}
			}
		}
		return false;
	}
}
