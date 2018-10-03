package es.gob.fire.services.statistics.dao;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.services.statistics.config.ConfigFilesException;
import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.config.DbManager;
import es.gob.fire.services.statistics.entity.TransactionCube;

public class TransactionsDAO {


	/**SQL Inserta una transaccion*/
	private static final String ST_INSERT_TRANSACTION = "INSERT INTO TB_TRANSACCIONES (fecha, id_aplicacion, id_operacion, id_proveedor, proveedor_forzado, correcta, id_transaccion) VALUES (?, ?, ?, ?, ?, ?, ?)";//$NON-NLS-1$


	/*Consultas estadisticas de transacciones*/
	/**Transacciones finalizadas correctamente/ incorrectamente por cada aplicaci&oacute;n(Filtrado por a&ntilde;o y mes)*/
	private static final String TRANSACTIONS_BYAPP = "SELECT  t.id_aplicacion AS ID_APP, " +  //$NON-NLS-1$
			" a.nombre AS NOMBRE_APP," + //$NON-NLS-1$
			" SUM(CASE When t.correcta = 'false' then 1 end) AS INCORRECTAS, " + //$NON-NLS-1$
			" SUM(CASE When t.correcta = 'true' then 1 end) AS CORRECTAS " + //$NON-NLS-1$
			" FROM tb_transacciones t, tb_aplicaciones a " + //$NON-NLS-1$
			" WHERE t.id_aplicacion = a.id " + //$NON-NLS-1$
			" AND year(t.fecha) = ? " + //$NON-NLS-1$
			" AND month(t.fecha) = ? " + //$NON-NLS-1$
			" GROUP BY ID_APP";//$NON-NLS-1$
	/**Transacciones finalizadas correctamente/ incorrectamente por cada origen de certificados/proveedor. (Filtrado por a&ntilde;o y mes)*/
	private static final String TRANSACTIONS_BYPROVIDER = "SELECT p.nombre AS PROVEEDOR," +//$NON-NLS-1$
		    " SUM(CASE When t.correcta = 'false' then 1 end) AS INCORRECTAS," +//$NON-NLS-1$
			" SUM(CASE When t.correcta = 'true' then 1 end) AS CORRECTAS "	+    //$NON-NLS-1$
			" FROM tb_transacciones t,  tb_proveedores p " +//$NON-NLS-1$
			" WHERE t.id_proveedor = p.id_proveedor " + //$NON-NLS-1$
			" AND year(t.fecha) = ? "+//$NON-NLS-1$
			" AND month(t.fecha) = ? "+//$NON-NLS-1$
			" GROUP BY t.id_proveedor";//$NON-NLS-1$
	/**Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n (Filtrado por a&ntilde;o y mes)*/
	private static final String TRANSACTIONS_BYDOCSIZE = "SELECT SUM(f.tamanno)/1024 AS Kbytes, t.id_aplicacion AS id_app, a.nombre AS nombre_app " +//$NON-NLS-1$
			" FROM tb_transacciones t, tb_aplicaciones a, tb_firmas f " +//$NON-NLS-1$
			" WHERE t.id_aplicacion = a.id " +//$NON-NLS-1$
			" AND f.id_transaccion = t.id_transaccion " +//$NON-NLS-1$
			" AND year(t.fecha) = ? " +//$NON-NLS-1$
			" AND month(t.fecha) = ? " +//$NON-NLS-1$
			" GROUP BY t.id_aplicacion ";//$NON-NLS-1$

	/**Transacciones realizadas seg&uacute;n el tipo de transacci&oacute;n (simple o lote). (Filtrado por a&ntilde;o y mes)*/
	private static final String TRANSACTIONS_BYOPERATION = "SELECT " + //$NON-NLS-1$
		" sum(case when t.id_operacion = 1 then (case when t.correcta = 'true' then 1 else 0 end) else 0 end )FirmasSimplesCorrectas," + //$NON-NLS-1$
		" sum(case when t.id_operacion = 1 then (case when t.correcta = 'false' then 1 else 0 end) else 0 end )FirmasSimplesINCorrectas," + 	//$NON-NLS-1$
		" sum(case when t.id_operacion = 2 then (case when t.correcta = 'true' then 1 else 0 end) else 0 end )FirmasLotesCorrectas," + //$NON-NLS-1$
		" sum(case when t.id_operacion = 2 then (case when t.correcta = 'false' then 1 else 0 end) else 0 end )FirmasLotesINCorrectas" + //$NON-NLS-1$
		" FROM tb_transacciones t" + //$NON-NLS-1$
		" WHERE year(t.fecha) = ?" + //$NON-NLS-1$
		" AND month(t.fecha) = ?";//$NON-NLS-1$


	/**
	 *
	 * @param transaction
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 * @throws ConfigFilesException
	 */
	public static int insertTransaction(final TransactionCube transaction) throws SQLException, DBConnectionException, ConfigFilesException {
		int totalInsertReg = 0;

		final PreparedStatement st = DbManager.prepareStatement(ST_INSERT_TRANSACTION,false);
		st.setTimestamp(1, new java.sql.Timestamp(transaction.getFecha().getTime()));
		st.setString(2, transaction.getIdAplicacion());
		st.setInt(3, transaction.getIdOperacion().intValue());
		st.setInt(4, transaction.getIdProveedor());
		st.setString (5, Boolean.toString(transaction.isProveedorForzado()));
		st.setString (6, Boolean.toString(transaction.isResultTransaction()));
		st.setString(7, transaction.getId_transaccion());
		totalInsertReg = st.executeUpdate();
		st.close();

		return totalInsertReg;
	}

	/**
	 * Obtiene las Transacciones finalizadas correctamente e incorrectamente por cada aplicaci&oacute;n (Filtrado por a&ntilde;o y mes)
	 * @param year
	 * @param month
	 * @return String con formato JSON,  id aplicaci&oacute;n nombre aplicaci&oacute;n, total transaciones incorrectas, total transaciones correctas
	 * @throws SQLException
	 * @throws DBConnectionException
	 * @throws ConfigFilesException
	 */
	public static String getTransactionsByAppJSON(final int year, final int month) throws SQLException, DBConnectionException, ConfigFilesException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYAPP);
		st.setInt(1, year);
		st.setInt(2, month);
		final ResultSet rs = st.executeQuery();
		while (rs.next()) {
			data.add(Json.createObjectBuilder()
					.add("ID_APP", rs.getString(1)) //$NON-NLS-1$
					.add("NOMBRE", rs.getString(2)) //$NON-NLS-1$
					.add("INCORRECTAS", rs.getString(3)) //$NON-NLS-1$
					.add("CORRECTAS", rs.getString(4)) //$NON-NLS-1$

					);
		}
		rs.close();
		st.close();
		jsonObj.add("TransByApp", data); //$NON-NLS-1$

		try  {
			final JsonWriter jw = Json.createWriter(writer);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		catch (final Exception e) {
			//LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de servidores de log", e); //$NON-NLS-1$
		}
		return writer.toString();

	}

	/**
	 * Obtiene las Transacciones finalizadas correctamente e incorrectamente por cada origen de certificados/proveedor (Filtrado por a&ntilde;o y mes)
	 * @param year
	 * @param month
	 * @return String con formato JSON,  id aplicaci&oacute;n nombre aplicaci&oacute;n, total transaciones incorrectas, total transaciones correctas
	 * @throws SQLException
	 * @throws DBConnectionException
	 * @throws ConfigFilesException
	 */
	public static String getTransactionsByProviderJSON(final int year, final int month) throws SQLException, DBConnectionException, ConfigFilesException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYPROVIDER);
		st.setInt(1, year);
		st.setInt(2, month);
		final ResultSet rs = st.executeQuery();
		while (rs.next()) {
			data.add(Json.createObjectBuilder()
					.add("p.nombre", rs.getString(1)) //$NON-NLS-1$
					.add("INCORRECTAS", rs.getString(2)) //$NON-NLS-1$
					.add("CORRECTAS ", rs.getString(3)) //$NON-NLS-1$
					);
		}
		rs.close();
		st.close();
		jsonObj.add("TransByProv", data); //$NON-NLS-1$

		try  {
			final JsonWriter jw = Json.createWriter(writer);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		catch (final Exception e) {
			//LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de servidores de log", e); //$NON-NLS-1$
		}
		return writer.toString();

	}

	/**
	 * Obtiene las Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n (Filtrado por a&ntilde;o y mes)
	 * @param year
	 * @param month
	 * @return String con formato JSON,  id aplicaci&oacute;n nombre aplicaci&oacute;n, total transaciones incorrectas, total transaciones correctas
	 * @throws SQLException
	 * @throws DBConnectionException
	 * @throws ConfigFilesException
	 */
	public static String getTransactionsByDocSizeJSON(final int year, final int month) throws SQLException, DBConnectionException, ConfigFilesException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYDOCSIZE);
		st.setInt(1, year);
		st.setInt(2, month);
		final ResultSet rs = st.executeQuery();
		while (rs.next()) {
			data.add(Json.createObjectBuilder()
					.add("Kbytes", rs.getString(1)) //$NON-NLS-1$
					.add("id_app", rs.getString(2)) //$NON-NLS-1$
					.add("nombre_app ", rs.getString(3)) //$NON-NLS-1$
					);
		}
		rs.close();
		st.close();
		jsonObj.add("TransByDocSize", data); //$NON-NLS-1$

		try  {
			final JsonWriter jw = Json.createWriter(writer);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		catch (final Exception e) {
			//LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de servidores de log", e); //$NON-NLS-1$
		}
		return writer.toString();

	}


	/**
	 * Obtiene las Transacciones realizadas seg&uacute;n el tipo de transacci&oacute;n (simple o lote). (Filtrado por a&ntilde;o y mes)
	 * @param year
	 * @param month
	 * @return String con formato JSON, Total Firmas Simples Correctas, Total Firmas Simples Incorrectas, total Firmas Lotes Correctas, total Firmas Lotes Incorrectas
	 * @throws SQLException
	 * @throws DBConnectionException
	 * @throws ConfigFilesException
	 */
	public static String getTransactionsByOperationJSON(final int year, final int month) throws SQLException, DBConnectionException, ConfigFilesException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYOPERATION);
		st.setInt(1, year);
		st.setInt(2, month);
		final ResultSet rs = st.executeQuery();
		while (rs.next()) {
			data.add(Json.createObjectBuilder()
					.add("FirmasSimplesCorrectas", rs.getString(1)) //$NON-NLS-1$
					.add("FirmasSimplesINCorrectas", rs.getString(2)) //$NON-NLS-1$
					.add("FirmasLotesCorrectas ", rs.getString(3)) //$NON-NLS-1$
					.add("FirmasLotesINCorrectas  ", rs.getString(4)) //$NON-NLS-1$

					);
		}
		rs.close();
		st.close();
		jsonObj.add("TransByOperation", data); //$NON-NLS-1$

		try  {
			final JsonWriter jw = Json.createWriter(writer);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		catch (final Exception e) {
			//LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de servidores de log", e); //$NON-NLS-1$
		}
		return writer.toString();

	}

}
