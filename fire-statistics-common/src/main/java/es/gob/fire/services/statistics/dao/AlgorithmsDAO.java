package es.gob.fire.services.statistics.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.config.DbManager;
import es.gob.fire.services.statistics.entity.Algorithm;

public class AlgorithmsDAO {

	/**Consulta que obtiene */
	private static final String ST_SELECT_ALGORITH_BYID = "SELECT id_algoritmo ,nombre  FROM tb_algoritmos a WHERE a.id_algoritmo = ? "; //$NON-NLS-1$


	/**Consulta que obtiene */
	private static final String ST_SELECT_ALGORITH_BYNAME = "SELECT id_algoritmo ,nombre  FROM tb_algoritmos a WHERE a.nombre = ? "; //$NON-NLS-1$

	/**
	 *
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static final Algorithm getAlgorithmById(final int id) throws SQLException, DBConnectionException {
		Algorithm algorith = null;
		if(id != 0) {
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_ALGORITH_BYID);
			st.setInt(1, id);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				algorith = new Algorithm();
				algorith.setIdAlgoritmo(rs.getInt(1));
				algorith.setNombre(rs.getString(2));
			}
		}
		return algorith;
	}

	/**
	 *
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static final Algorithm getAlgorithmByName(final String name) throws SQLException, DBConnectionException {
		Algorithm algorith = null;
		if(name != null && !"".equals(name)) { //$NON-NLS-1$
			final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_ALGORITH_BYNAME);
			st.setString(1, name);
			final ResultSet rs = st.executeQuery();
			if(rs.next()) {
				algorith = new Algorithm();
				algorith.setIdAlgoritmo(rs.getInt(1));
				algorith.setNombre(rs.getString(2));
			}
		}
		return algorith;
	}
}
