package es.gob.fire.server.admin.service;

import java.util.logging.Logger;

public class RolePermissions {

	private static final Logger LOGGER = Logger.getLogger(RolePermissions.class.getName());
	private static final String PARAM_USER_ROLE = "role-usr";//$NON-NLS-1$

	public static final String PERMISION_LOGIN_ACCESS = "1";
	public static final String PERMISION_APP_RESPONSABLE = "2";


	private boolean loginPermission;
	private boolean appResponsable;

/**
 * Se obtiene los permisos que estan asociados a cada rol
 * @param permissions
 * @return
 */
	public static RolePermissions getInstance(final String permissions) {

		final RolePermissions userPermissions = new RolePermissions();

		if (permissions != null) {
			for (final String permission : permissions.split(",")) {
				switch (permission) {

				case PERMISION_LOGIN_ACCESS:	// Permiso de acceso
					userPermissions.setLoginPermission(true);
					break;

				case PERMISION_APP_RESPONSABLE:	// Permiso de responsable de app
					userPermissions.setAppResponsable(true);
					break;

				default:
					break;
				}
			}
		}

		return userPermissions;
	}

	/**
	 * Busca en un listado de permisos extraidos de BD si se encuentra un permiso particular.
	 * @param permissionsList Lista de permisos extraida de BD.
	 * @param permission Permiso que queremos buscar.
	 * @return Indica si el permiso se encuentra entre el listado proporcionado.
	 */
	public static boolean hasPermission(final String permissionsList, final String permission) {
		return permissionsList != null &&
				(permissionsList.equals(permission) ||
				permissionsList.startsWith(permission + ",") || //$NON-NLS-1$
				permissionsList.endsWith("," + permission) || //$NON-NLS-1$
				permissionsList.contains("," + permission + ",")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean hasLoginPermission() {
		return this.loginPermission;
	}

    private void setLoginPermission(final boolean loginPermission) {
		this.loginPermission = loginPermission;

			}

    public boolean hasAppResponsable() {
    	return this.appResponsable;
    	    }
    private void setAppResponsable(final boolean appResponsable) {
		this.appResponsable = appResponsable;
    }

}
