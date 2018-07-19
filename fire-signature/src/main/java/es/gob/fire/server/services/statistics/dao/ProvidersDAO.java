package es.gob.fire.server.services.statistics.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.gob.fire.server.services.statistics.entity.Provider;
import es.gob.fire.signature.DBConnectionException;
import es.gob.fire.signature.DbManager;

public class ProvidersDAO {

	/**Consulta que obtiene */
	private static final String ST_SELECT_PROV_BYNAME = "SELECT id_proveedor ,nombre, conector  FROM tb_proveedores p WHERE p.nombre = ? "; //$NON-NLS-1$

	/**Consulta que obtiene */
	private static final String ST_SELECT_PROV_BYID = "SELECT id_proveedor ,nombre, conector  FROM tb_proveedores p WHERE p.id_proveedor = ? "; //$NON-NLS-1$


	/**
	 * Realiza consulta a bbdd, obteniendo los datos del proveedor indicado por el par&aacute;metro name.
	 * @param name
	 * @return Provider / null
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static final Provider getProviderByName(final String name) throws SQLException, DBConnectionException {
		Provider prov = null;
		if(name != null && !"".equals(name)) { //$NON-NLS-1$
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_PROV_BYNAME);
			st.setString(1, name);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				prov = new Provider();
				prov.setIdProveedor(rs.getInt(1));
				prov.setNombre(rs.getString(2));
				if(rs.getString(3) != null && !"".equals(rs.getString(3))) { //$NON-NLS-1$
					prov.setConector(rs.getString(3));
				}
			}
		}
		return prov;
	}

	public static final Provider getProviderById(final int id) throws SQLException, DBConnectionException {
		Provider prov = null;
		if(id != 0) {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_PROV_BYID);
			st.setInt(1, id);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				prov = new Provider();
				prov.setIdProveedor(rs.getInt(1));
				prov.setNombre(rs.getString(2));
				if(rs.getString(3) != null && !"".equals(rs.getString(3))) { //$NON-NLS-1$
					prov.setConector(rs.getString(3));
				}
			}
		}
		return prov;
	}

}
