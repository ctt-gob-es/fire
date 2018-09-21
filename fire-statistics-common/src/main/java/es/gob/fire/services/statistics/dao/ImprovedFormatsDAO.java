package es.gob.fire.services.statistics.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.config.DbManager;
import es.gob.fire.services.statistics.entity.ImprovedFormat;

public class ImprovedFormatsDAO {

	/**Consulta que obtiene */
	private static final String ST_SELECT_BYID = "SELECT fm.id_formato_mejorado, fm.nombre  FROM tb_formatos_mejorados fm  WHERE fm.id_formato_mejorado = ? "; //$NON-NLS-1$

	private static final String ST_SELECT_BYNAME = "SELECT fm.id_formato_mejorado, fm.nombre  FROM tb_formatos_mejorados fm  WHERE fm.nombre = ? "; //$NON-NLS-1$


	/**
	 *
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static final ImprovedFormat getFormatById(final int id) throws SQLException, DBConnectionException {
		ImprovedFormat impFormat = null;
		if(id != 0) {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_BYID);
			st.setInt(1, id);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				impFormat = new ImprovedFormat();
				impFormat.setIdFormatoMejorado(rs.getInt(1));
				impFormat.setNombre(rs.getString(2));
			}
		}
		return impFormat;
	}
	/**
	 *
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static final ImprovedFormat getFormatByName(final String name) throws SQLException, DBConnectionException {
		ImprovedFormat impFormat = null;
		if(name != null && !"".equals(name)) { //$NON-NLS-1$
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_BYNAME);
			st.setString(1, name);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				impFormat = new ImprovedFormat();
				impFormat.setIdFormatoMejorado(rs.getInt(1));
				impFormat.setNombre(rs.getString(2));
			}
		}
		return impFormat;
	}

}
