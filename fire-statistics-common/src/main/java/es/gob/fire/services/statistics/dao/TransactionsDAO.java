package es.gob.fire.services.statistics.dao;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
	private static final String ST_INSERT_TRANSACTION = "INSERT INTO TB_TRANSACCIONES " //$NON-NLS-1$
			+ "(fecha, aplicacion, operacion, proveedor, proveedor_forzado, tamanno, correcta, total)" //$NON-NLS-1$
			+ " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)";//$NON-NLS-1$


	/*Consultas estadisticas de transacciones*/
	/**Transacciones finalizadas correctamente/ incorrectamente por cada aplicaci&oacute;n(Filtrado por a&ntilde;o y mes)*/
	private static final String TRANSACTIONS_BYAPP = "SELECT  t.aplicacion AS NOMBRE_APP, " + //$NON-NLS-1$
			" SUM(CASE When t.correcta = 'false' then t.total else 0 end) AS INCORRECTAS, " + //$NON-NLS-1$
			" SUM(CASE When t.correcta = 'true' then t.total  else 0 end) AS CORRECTAS " + //$NON-NLS-1$
			" FROM tb_transacciones t " + //$NON-NLS-1$
			" WHERE year(t.fecha) = ? AND month(t.fecha) = ? " + //$NON-NLS-1$
			" GROUP BY NOMBRE_APP "; //$NON-NLS-1$

	/**Transacciones finalizadas correctamente/ incorrectamente por cada origen de certificados/proveedor. (Filtrado por a&ntilde;o y mes)*/
	private static final String TRANSACTIONS_BYPROVIDER = "SELECT t.proveedor AS PROVEEDOR, " +  //$NON-NLS-1$
			" SUM(CASE When t.correcta = 'false' then t.total else 0 end) AS INCORRECTAS, " +  //$NON-NLS-1$
			" SUM(CASE When t.correcta = 'true' then t.total else 0 end) AS CORRECTAS " +  //$NON-NLS-1$
			" FROM tb_transacciones t " +  //$NON-NLS-1$
			" WHERE  year(t.fecha) = ? AND month(t.fecha) = ? " +  //$NON-NLS-1$
			" GROUP BY t.proveedor"; //$NON-NLS-1$

	/**Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n (Filtrado por a&ntilde;o y mes)*/
	private static final String TRANSACTIONS_BYDOCSIZE = "SELECT SUM(t.tamanno)/1024 AS Kbytes, t.aplicacion AS nombre_app " + //$NON-NLS-1$
			" FROM tb_transacciones t "+ //$NON-NLS-1$
			" WHERE  year(t.fecha) = ? AND month(t.fecha) = ? "+  //$NON-NLS-1$
			" GROUP BY t.aplicacion"; //$NON-NLS-1$

	/**Transacciones realizadas seg&uacute;n el tipo de transacci&oacute;n (simple o lote). (Filtrado por a&ntilde;o y mes)*/
	private static final String TRANSACTIONS_BYOPERATION = "SELECT t.aplicacion, "+ //$NON-NLS-1$
			 " sum(case when t.operacion = 'SIGN' then (case when t.correcta = 'true' then t.total else 0 end) else 0 end )FirmasSimplesCorrectas,"+ //$NON-NLS-1$
			 " sum(case when t.operacion = 'SIGN' then (case when t.correcta = 'false' then t.total else 0 end) else 0 end )FirmasSimplesINCorrectas,"+ //$NON-NLS-1$
			 " sum(case when t.operacion = 'BATCH' then (case when t.correcta = 'true' then t.total else 0 end) else 0 end )FirmasLotesCorrectas,"+ //$NON-NLS-1$
			 " sum(case when t.operacion = 'BATCH' then (case when t.correcta = 'false' then t.total else 0 end) else 0 end )FirmasLotesINCorrectas"+ //$NON-NLS-1$
			 " FROM tb_transacciones t"+ //$NON-NLS-1$
			 " WHERE year(t.fecha) = ? "+  //$NON-NLS-1$
			 " AND month(t.fecha) = ? "+		 //$NON-NLS-1$
			 " GROUP BY t.aplicacion" ; //$NON-NLS-1$

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
		st.setString(2, transaction.getAplicacion());
		st.setString(3, transaction.getOperacion());
		st.setString(4, transaction.getProveedor());
		st.setString (5, Boolean.toString(transaction.isProveedorForzado()));
		st.setLong(6, transaction.getSize().longValue());
		st.setString (7, Boolean.toString(transaction.isResultTransaction()));
		st.setLong(8, transaction.getTotal().longValue());
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
	 * @throws UnsupportedEncodingException
	 * @throws NumberFormatException
	 */
	public static String getTransactionsByAppJSON(final int year, final int month) throws SQLException, DBConnectionException, ConfigFilesException, NumberFormatException, UnsupportedEncodingException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYAPP);
		st.setInt(1, year);
		st.setInt(2, month);
		final ResultSet rs = st.executeQuery();
		if(rs.last()) {
			rs.beforeFirst();
			while (rs.next()) {
				data.add(Json.createObjectBuilder()
						.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
						.add("INCORRECTAS", rs.getString(2)) //$NON-NLS-1$
						.add("CORRECTAS", rs.getString(3)) //$NON-NLS-1$
						.add("TOTAL", String.valueOf( Integer.parseInt(rs.getString(2))+Integer.parseInt(rs.getString(3)))) //$NON-NLS-1$
						);
			}
			jsonObj.add("TransByApp", data); //$NON-NLS-1$
		}
		else {
			//No tiene registros
			data.add(Json.createObjectBuilder()
					.add("Code", 204) //$NON-NLS-1$
					.add("Message","No existen registros para la consulta 'Transacciones finalizadas correctamente/ incorrectamente por cada aplicaci&oacute;n' para el Mes: " + String.valueOf(month) +"/" + String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			jsonObj.add("Error", data); //$NON-NLS-1$
		}

		rs.close();
		st.close();



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

		if(rs.last()) {
			rs.beforeFirst();

			while (rs.next()) {
				data.add(Json.createObjectBuilder()
					.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
					.add("INCORRECTAS", rs.getString(2)) //$NON-NLS-1$
					.add("CORRECTAS", rs.getString(3)) //$NON-NLS-1$
					.add("TOTAL", String.valueOf( Integer.parseInt(rs.getString(2))+Integer.parseInt(rs.getString(3)))) //$NON-NLS-1$
					);
			}
			jsonObj.add("TransByProv", data); //$NON-NLS-1$
		}
		else {
			//No tiene registros
			data.add(Json.createObjectBuilder()
					.add("Code", 204) //$NON-NLS-1$
					.add("Message","No existen registros la consulta 'Transacciones finalizadas correctamente/ incorrectamente por cada origen de certificados/proveedor' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+"."));    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
			jsonObj.add("Error", data); //$NON-NLS-1$
		}
		rs.close();
		st.close();


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
		if(rs.last()) {
			rs.beforeFirst();

			while (rs.next()) {
				data.add(Json.createObjectBuilder()
						.add("NOMBRE", rs.getString(2)) //$NON-NLS-1$
						.add("MB", String.valueOf( Math.round( Double.parseDouble(rs.getString(1))/1024 * 100.0) / 100.0 )) //$NON-NLS-1$
						);
			}
			jsonObj.add("TransByDocSize", data); //$NON-NLS-1$
		}
		else {
			//No tiene registros
			data.add(Json.createObjectBuilder()
					.add("Code", 204) //$NON-NLS-1$
					.add("Message","No existen registros para la consulta 'Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			jsonObj.add("Error", data); //$NON-NLS-1$
		}
		rs.close();
		st.close();


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
		if(rs.last()) {
			rs.beforeFirst();
			while (rs.next()) {
				data.add(Json.createObjectBuilder()
						.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
						.add("FirmasSimplesCorrectas", rs.getString(2)) //$NON-NLS-1$
						.add("FirmasSimplesINCorrectas", rs.getString(3)) //$NON-NLS-1$
						.add("TOTAL_SIMPLES", String.valueOf( Integer.parseInt(rs.getString(2))+Integer.parseInt(rs.getString(3)))) //$NON-NLS-1$
						.add("FirmasLotesCorrectas", rs.getString(4)) //$NON-NLS-1$
						.add("FirmasLotesINCorrectas", rs.getString(5)) //$NON-NLS-1$
						.add("TOTAL_LOTES", String.valueOf( Integer.parseInt(rs.getString(4))+Integer.parseInt(rs.getString(5)))) //$NON-NLS-1$
						);
			}
			jsonObj.add("TransByOperation", data); //$NON-NLS-1$
		}
		else {
			//No tiene registros
			data.add(Json.createObjectBuilder()
					.add("Code", 204) //$NON-NLS-1$
					.add("Message","No existen registros para la consulta 'Transacciones realizadas seg&uacute;n el tipo de transacci&oacute;n (simple o lote)' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			jsonObj.add("Error", data); //$NON-NLS-1$
		}
		rs.close();
		st.close();


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
