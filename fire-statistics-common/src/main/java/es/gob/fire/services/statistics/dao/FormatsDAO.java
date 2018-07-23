package es.gob.fire.services.statistics.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.config.DbManager;
import es.gob.fire.services.statistics.entity.Format;

public class FormatsDAO {

	/**Consulta que obtiene */
	private static final String ST_SELECT_FORMAT_BYID = "SELECT id_formato ,nombre  FROM tb_formatos f WHERE f.id_formato = ? "; //$NON-NLS-1$

	private static final String ST_SELECT_FORMAT_BYNAME = "SELECT id_formato ,nombre  FROM tb_formatos f WHERE f.nombre = ? "; //$NON-NLS-1$

	/**
	 *
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static final Format getFormatById(final int id) throws SQLException, DBConnectionException {
		Format format = null;
		if(id != 0) {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_FORMAT_BYID);
			st.setInt(1, id);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				format = new Format();
				format.setIdFormato(rs.getInt(1));
				format.setNombre(rs.getString(2));
			}
		}
		return format;
	}
	/**
	 *
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static final Format getFormatByName(final String name) throws SQLException, DBConnectionException {
		Format format = null;
		if(name != null && !"".equals(name)) { //$NON-NLS-1$
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_FORMAT_BYNAME);
			st.setString(1, name);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				format = new Format();
				format.setIdFormato(rs.getInt(1));
				format.setNombre(rs.getString(2));
			}
		}
		return format;
	}

}
