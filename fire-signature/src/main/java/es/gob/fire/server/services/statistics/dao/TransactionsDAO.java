package es.gob.fire.server.services.statistics.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import es.gob.fire.server.services.statistics.entity.TransactionCube;
import es.gob.fire.signature.DBConnectionException;
import es.gob.fire.signature.DbManager;

public class TransactionsDAO {

	/**Consulta que obtiene todos los registros para una fecha*/
	//private static final String ST_SELECT_TRANS_BYDATE = "SELECT fecha, id_aplicacion, id_operacion, id_proveedor, proveedor_forzado  FROM tb_transacciones t where t.fecha = ? "; //$NON-NLS-1$

	/**SQL Inserta una transaccion*/
	private static final String ST_INSERT_TRANSACTION = "INSERT INTO TB_TRANSACCIONES (fecha, id_aplicacion, id_operacion, id_proveedor, proveedor_forzado) VALUES (?, ?, ?, ?, ?)";//$NON-NLS-1$


	/**
	 *
	 * @param transaction
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static int insertTransaction(final TransactionCube transaction) throws SQLException, DBConnectionException {
		int totalInsertReg = 0;

		final PreparedStatement st = DbManager.prepareStatement(ST_INSERT_TRANSACTION,false);
		st.setTimestamp(1, new java.sql.Timestamp(transaction.getFecha().getTime()));
		st.setString(2, transaction.getIdAplicacion());
		st.setInt(3, transaction.getIdOperacion().intValue());
		st.setInt(4, transaction.getIdProveedor());
		st.setString (5, Boolean.toString(transaction.isProveedorForzado()));
		totalInsertReg = st.executeUpdate();
		st.close();

		return totalInsertReg;
	}


}
