package es.gob.fire.services.statistics.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.config.DbManager;
import es.gob.fire.services.statistics.entity.SignatureCube;


public class SignaturesDAO {

	/**Consulta que obtiene todos los registros para una fecha*/
	//private static final String ST_SELECT_SIGN_BYDATE = "SELECT fecha,id_formato ,id_algoritmo, id_proveedor,id_navegador,version_navegador  FROM tb_firmas f WHERE f.fecha = ? "; //$NON-NLS-1$

	/**Inserta */
	private static final String ST_INSERT_SIGNATURE = "INSERT INTO TB_FIRMAS (fecha, id_formato ,id_formato_mejorado, id_algoritmo, id_proveedor, id_navegador, version_navegador, correcta, id_transaccion, tamanno) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; //$NON-NLS-1$

//	public static List<SignatureCube> getSignatureCubeByDate(final Date fecha) throws SQLException, DBConnectionException{
//
//		final List<SignatureCube> result =  new ArrayList<>();
//		final PreparedStatement st = DbManager.prepareStatement(ST_SELECT_SIGN_BYDATE);
//		st.setDate(1, (java.sql.Date) fecha);
//		final ResultSet rs = st.executeQuery();
//		while (rs.next()) {
//			final SignatureCube sign = new SignatureCube();
//			sign.setFecha(rs.getDate(1));
//			sign.setSignFormat(rs.getString(2));
//			sign.setSignAlgorithm(rs.getString(3));
//			sign.setProveedor(rs.getString(4));
//			final Browser browser = new Browser(rs.getInt(5),rs.getString(6));
//			sign.setNavegador(browser);
//			result.add(sign);
//		}
//		rs.close();
//		st.close();
//		return result;
//	}

	/**
	 * Inserta un listado de firmas en el cubo
	 * @param sinatures
	 * @return
	 * @throws DBConnectionException
	 * @throws SQLException
	 */
	public static int insertSignatures(final List<SignatureCube> sinatures) throws SQLException, DBConnectionException {
		int totalInsertReg = 0;
		for(final SignatureCube sign : sinatures) {
			final PreparedStatement st = DbManager.prepareStatement(ST_INSERT_SIGNATURE,false);
			st.setDate(1, new java.sql.Date( sign.getFecha().getTime()));
			st.setInt(2, sign.getIdFormat());
			st.setInt(3, sign.getIdImprovedFormat());
			st.setInt(4, sign.getIdAlgorithm());
			st.setInt(5, sign.getIdProveedor());
			st.setInt(6,  Integer.parseInt(sign.getNavegador().getId()));
			st.setString(7, sign.getNavegador().getVersion());
			st.setString (8, Boolean.toString(sign.isResultSign()));
			st.setString(9, sign.getId_transaccion());
			st.setLong(10, sign.getSize().longValue());
			totalInsertReg = totalInsertReg + st.executeUpdate();
			st.close();
		}
		DbManager.runCommit();

		//LOGGER.info("Damos de alta la aplicacion '" + nombre + "' con el ID: " + id); //$NON-NLS-1$ //$NON-NLS-2

		return totalInsertReg;
	}

	/**
	 *
	 * @param signature
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static int insertSignature(final SignatureCube signature) throws SQLException, DBConnectionException {
		int totalInsertReg = 0;

		try(final PreparedStatement st = DbManager.prepareStatement(ST_INSERT_SIGNATURE,false);){
			st.setTimestamp (1, new java.sql.Timestamp(signature.getFecha().getTime()));
			st.setInt(2, signature.getIdFormat());
			if(signature.getIdImprovedFormat() !=  0) {
				st.setInt(3,  signature.getIdImprovedFormat());
			}
			else {
				st.setNull(3,Types.INTEGER);
			}
			st.setInt(4, signature.getIdAlgorithm());
			st.setInt(5, signature.getIdProveedor());
			st.setInt(6,  Integer.parseInt(signature.getNavegador().getId()));
			st.setString(7, signature.getNavegador().getVersion());
			st.setString (8, Boolean.toString(signature.isResultSign()));
			st.setString(9, signature.getId_transaccion());
			st.setLong(10, signature.getSize().longValue());
			totalInsertReg = st.executeUpdate();
			st.close();
		}

		return totalInsertReg;
	}

}
