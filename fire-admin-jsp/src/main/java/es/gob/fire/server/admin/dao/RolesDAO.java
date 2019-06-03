package es.gob.fire.server.admin.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.server.admin.conf.DbManager;
import es.gob.fire.server.admin.entity.Role;
import es.gob.fire.server.admin.service.RolePermissions;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.server.admin.conf.DbManager;
import es.gob.fire.server.admin.entity.User;
import es.gob.fire.server.admin.service.RolePermissions;
import es.gob.fire.server.admin.tool.Base64;
import es.gob.fire.server.admin.tool.Utils;


public class RolesDAO {

	private static final Logger LOGGER = Logger.getLogger(RolesDAO.class.getName());

	private static final String ST_SELECT_PERMISSIONS_BY_ROLE = "SELECT permisos FROM tb_roles WHERE id = ?"; //$NON-NLS-1$
	private static final String ST_SELECT_ROLES = "SELECT id, nombre_rol, permisos FROM tb_roles"; //$NON-NLS-1$



	public static RolePermissions getPermissions(final int role) {
		RolePermissions permissions = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_PERMISSIONS_BY_ROLE);
			st.setInt(1, role);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				permissions = RolePermissions.getInstance(rs.getString(1));
			}
			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo obtener la informacion del usuario de la base de datos", e); //$NON-NLS-1$
			permissions = new RolePermissions();
		}
		return permissions == null ? new RolePermissions() :permissions;
	}



	/**
	 * Recorremos todos los valores de los roles que existen en base de datos
	 * @param id del rol.
	 * @param uname Nombre del rol
	 * @param permisos del rol
	 * @throws SQLException
	 *
	 */
	public static Role[] getRoles() {

		final List<Role> rolesList = new ArrayList<>();
		try {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_ROLES);
			final ResultSet rs = st.executeQuery();
			while (rs.next()) {
				final Role role = new Role(rs.getInt(1), rs.getString(2));
				final RolePermissions permissions = RolePermissions.getInstance(rs.getString(3));
				role.setPermissions(permissions);

				rolesList.add(role);
			}
			st.close();
			rs.close();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "No se pudo obtener el listado de roles de la base de datos", e); //$NON-NLS-1$
		}
		return rolesList.toArray(new Role[rolesList.size()]);
	}


}
