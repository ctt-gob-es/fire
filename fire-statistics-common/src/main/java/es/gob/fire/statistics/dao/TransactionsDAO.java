package es.gob.fire.statistics.dao;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.statistics.config.DBConnectionException;
import es.gob.fire.statistics.config.DbManager;
import es.gob.fire.statistics.entity.TransactionCube;
import es.gob.fire.statistics.entity.TransactionTotal;

/** Gestor para la manipulaci&oacute;n de la informaci&oacute;n de las transacciones en base de datos. */
public class TransactionsDAO {

	/** SQL para insertar una transacci&oacute;n. */
	private static final String ST_INSERT_TRANSACTION = "INSERT INTO TB_TRANSACCIONES " //$NON-NLS-1$
			+ "(fecha, aplicacion, operacion, proveedor, proveedor_forzado, correcta, tamanno, total)" //$NON-NLS-1$
			+ " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)"; //$NON-NLS-1$

	/** Transacciones finalizadas correctamente/ incorrectamente por cada aplicaci&oacute;n
	 * (Filtrado por a&ntilde;o y mes). */
	private static final String TRANSACTIONS_BYAPP = "SELECT  t.aplicacion AS NOMBRE_APP, " + //$NON-NLS-1$
			" SUM(CASE When t.correcta = '1' then t.total  else 0 end) AS CORRECTAS, " + //$NON-NLS-1$
			" SUM(CASE When t.correcta = '0' then t.total else 0 end) AS INCORRECTAS " + //$NON-NLS-1$
			" FROM tb_transacciones t " + //$NON-NLS-1$
			" WHERE year(t.fecha) = ? AND month(t.fecha) = ? " + //$NON-NLS-1$
			" GROUP BY NOMBRE_APP "; //$NON-NLS-1$

	/** Transacciones finalizadas correctamente/ incorrectamente por cada origen de
	 * certificados/proveedor. (Filtrado por a&ntilde;o y mes). */
	private static final String TRANSACTIONS_BYPROVIDER = "SELECT t.proveedor AS PROVEEDOR, " +  //$NON-NLS-1$
			" SUM(CASE When t.correcta = '1' then t.total else 0 end) AS CORRECTAS, " +  //$NON-NLS-1$
			" SUM(CASE When t.correcta = '0' then t.total else 0 end) AS INCORRECTAS " +  //$NON-NLS-1$
			" FROM tb_transacciones t " +  //$NON-NLS-1$
			" WHERE  year(t.fecha) = ? AND month(t.fecha) = ? " +  //$NON-NLS-1$
			" GROUP BY t.proveedor"; //$NON-NLS-1$

	/** Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n
	 * (Filtrado por a&ntilde;o y mes) */
	private static final String TRANSACTIONS_BYDOCSIZE = "SELECT t.aplicacion AS nombre_app, SUM(t.tamanno) AS bytes " + //$NON-NLS-1$
			" FROM tb_transacciones t "+ //$NON-NLS-1$
			" WHERE  year(t.fecha) = ? AND month(t.fecha) = ? "+  //$NON-NLS-1$
			" GROUP BY t.aplicacion"; //$NON-NLS-1$

	/** Transacciones realizadas seg&uacute;n el tipo de transacci&oacute;n (simple o lote).
	 * (Filtrado por a&ntilde;o y mes) */
	private static final String TRANSACTIONS_BYOPERATION = "SELECT t.aplicacion, "+ //$NON-NLS-1$
			 " sum(case when t.operacion = 'SIGN' then (case when t.correcta = '1' then t.total else 0 end) else 0 end )FirmasSimplesCorrectas,"+ //$NON-NLS-1$
			 " sum(case when t.operacion = 'SIGN' then (case when t.correcta = '0' then t.total else 0 end) else 0 end )FirmasSimplesINCorrectas,"+ //$NON-NLS-1$
			 " sum(case when t.operacion = 'BATCH' then (case when t.correcta = '1' then t.total else 0 end) else 0 end )FirmasLotesCorrectas,"+ //$NON-NLS-1$
			 " sum(case when t.operacion = 'BATCH' then (case when t.correcta = '0' then t.total else 0 end) else 0 end )FirmasLotesINCorrectas"+ //$NON-NLS-1$
			 " FROM tb_transacciones t"+ //$NON-NLS-1$
			 " WHERE year(t.fecha) = ? "+  //$NON-NLS-1$
			 " AND month(t.fecha) = ? "+		 //$NON-NLS-1$
			 " GROUP BY t.aplicacion" ; //$NON-NLS-1$

	/** Inserta una configuraci&oacute;n de transacci&oacute;n en base de datos indicando cuantas veces
	 * se dio esta configuraci&oacute;n un d&iacute;a concreto.
	 * @param date Fecha del d&iacute;a en la que se realiz&oacute; la transacci&oacute;n.
	 * @param transaction Conjunto de datos de la transacci&oacute;n.
	 * @return {@code true} si la configuraci&oacute;n se inserto correctamente. {@code false}
	 *         en caso contrario.
	 * @throws SQLException Cuando se produce un error al insertar los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos. */
	public static boolean insertTransaction(final Date date,
			                                final TransactionCube transaction,
			                                final TransactionTotal total) throws SQLException,
	                                                                             DBConnectionException {
		final int totalInsertReg;
		try (
			final PreparedStatement st = DbManager.prepareStatement(ST_INSERT_TRANSACTION,false)
		) {
			st.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
			st.setString(2, transaction.getApplication());
			st.setString(3, transaction.getOperation());
			st.setString(4, transaction.getProvider());
			st.setBoolean(5, transaction.isMandatoryProvider());
			st.setBoolean(6, transaction.isResultTransaction());
			st.setLong(7, total.getDataSize());
			st.setLong(8, total.getTotal());
			totalInsertReg = st.executeUpdate();
		}
		return totalInsertReg == 1;
	}

	/** Obtiene las transacciones finalizadas correctamente e incorrectamente por cada
	 * aplicaci&oacute;n (Filtrado por a&ntilde;o y mes).
	 * @param year A&ntilde;o de la transacci&oacute;n.
	 * @param month Mes de la transacci&oacute;n.
	 * @return JSON con el nombre de la aplicaci&oacute;n, total de transaciones correctas, total
	 *         de transaciones incorrectas y total global.
	 * @throws SQLException Cuando se produce un error al acceder a los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 * @throws NumberFormatException Cuando se obtienen totales incorrectos de la base de datos. */
	public static String getTransactionsByAppJSON(final int year,
			                                      final int month) throws SQLException,
	                                                                      DBConnectionException,
	                                                                      NumberFormatException {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		try (final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYAPP);) {
			st.setInt(1, year);
			st.setInt(2, month);
			try (final ResultSet rs = st.executeQuery();) {

				final JsonArrayBuilder data = Json.createArrayBuilder();

				// Si no hay registros, devolveremos un error
				if (!rs.next()) {
					data.add(Json.createObjectBuilder()
							.add("Code", 204) //$NON-NLS-1$
							.add("Message", "No existen registros para la consulta 'Transacciones finalizadas correctamente/ incorrectamente por cada aplicaci&oacute;n' para el Mes: " + String.valueOf(month) +"/" + String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					jsonObj.add("Error", data); //$NON-NLS-1$
				}
				// Si hay registros, los agregamos al JSON de salida
				else {
					do {
						data.add(Json.createObjectBuilder()
								.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
								.add("CORRECTAS", rs.getString(2)) //$NON-NLS-1$
								.add("INCORRECTAS", rs.getString(3)) //$NON-NLS-1$
								//TODO: Revisar si con MySQL y Oracle
								.add("TOTAL", rs.getInt(2) + rs.getInt(3)) //$NON-NLS-1$
								);
					} while (rs.next());

					jsonObj.add("TransByApp", data); //$NON-NLS-1$
				}
			}
		}

		// Pasamos a texto el objeto JSON
		final StringWriter writer = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(writer);) {
		    jw.writeObject(jsonObj.build());
		}

		return writer.toString();
	}

	/**
	 * Obtiene las Transacciones finalizadas correctamente e incorrectamente por cada origen de
	 * certificados/proveedor (Filtrado por a&ntilde;o y mes)
	 * @param year A&ntilde;o de la transacci&oacute;n.
	 * @param month Mes de la transacci&oacute;n.
	 * @return JSON con el nombre aplicaci&oacute;n, total transaciones correctas, total
	 * transaciones incorrectas y total.
	 * @throws SQLException Cuando se produce un error al acceder a los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static String getTransactionsByProviderJSON(final int year, final int month) throws SQLException, DBConnectionException {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		try (final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYPROVIDER);) {
			st.setInt(1, year);
			st.setInt(2, month);

			try (final ResultSet rs = st.executeQuery();) {

				final JsonArrayBuilder data = Json.createArrayBuilder();

				// Si no hay registros, devolveremos un error
				if (!rs.next()) {
					data.add(Json.createObjectBuilder()
							.add("Code", 204) //$NON-NLS-1$
							.add("Message", "No existen registros la consulta 'Transacciones finalizadas correctamente/ incorrectamente por cada origen de certificados/proveedor' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+"."));    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
					jsonObj.add("Error", data); //$NON-NLS-1$
				}
				// Si hay registros, los agregamos al JSON de salida
				else {
					do {
						data.add(Json.createObjectBuilder()
								.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
								.add("CORRECTAS", rs.getString(2)) //$NON-NLS-1$
								.add("INCORRECTAS", rs.getString(3)) //$NON-NLS-1$
								//TODO: Revisar si con MySQL y Oracle se pueden extraer directamente como long
								.add("TOTAL", rs.getInt(2) + rs.getInt(3)) //$NON-NLS-1$
								);
					} while (rs.next());

					jsonObj.add("TransByProv", data); //$NON-NLS-1$
				}
			}
		}

		// Pasamos a texto el objeto JSON
		final StringWriter writer = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(writer);) {
		    jw.writeObject(jsonObj.build());
		}

		return writer.toString();

	}

	/**
	 * Obtiene las Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n
	 * (Filtrado por a&ntilde;o y mes)
	 * @param year A&ntilde;o de la transacci&oacute;n.
	 * @param month Mes de la transacci&oacute;n.
	 * @return JSON con el nombre aplicaci&oacute;n y el tama&ntilde;o de datos procesados por esa
	 * aplicaci&oacute;n.
	 * @throws SQLException Cuando se produce un error al acceder a los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static String getTransactionsByDocSizeJSON(final int year, final int month) throws SQLException, DBConnectionException {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		try (final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYDOCSIZE);) {
			st.setInt(1, year);
			st.setInt(2, month);
			try (final ResultSet rs = st.executeQuery();) {

				final JsonArrayBuilder data = Json.createArrayBuilder();

				// Si no hay registros, devolveremos un error
				if (!rs.next()) {
					data.add(Json.createObjectBuilder()
							.add("Code", 204) //$NON-NLS-1$
							.add("Message", "No existen registros para la consulta 'Transacciones seg&uacute;n el tama&ntilde;o de los datos de cada aplicaci&oacute;n' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					jsonObj.add("Error", data); //$NON-NLS-1$
				}
				// Si hay registros, los agregamos al JSON de salida
				else {
					do {
						data.add(Json.createObjectBuilder()
								.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
								//TODO: Revisar logica
								// Calculamos los megas y truncamos a 2 decimales
								.add("MB", Math.floor(rs.getLong(2) / (1024 * 1024.0) * 100) / 100) //$NON-NLS-1$
								);
					} while (rs.next());

					jsonObj.add("TransByDocSize", data); //$NON-NLS-1$
				}
			}
		}

		// Pasamos a texto el objeto JSON
		final StringWriter writer = new StringWriter();
		try (final JsonWriter jw = Json.createWriter(writer);) {
		    jw.writeObject(jsonObj.build());
		}

		return writer.toString();

	}


	/**
	 * Obtiene las Transacciones realizadas seg&uacute;n el tipo de transacci&oacute;n (simple o
	 * lote). (Filtrado por a&ntilde;o y mes).
	 * @param year A&ntilde;o de la transacci&oacute;n.
	 * @param month Mes de la transacci&oacute;n.
	 * @return JSON con el nombre aplicaci&oacute;n, el total de transacciones simples correctas e
	 * incorrectas y el total de transacciones de lote correctas e incorrectas.
	 * @throws SQLException Cuando se produce un error al acceder a los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static String getTransactionsByOperationJSON(final int year, final int month) throws SQLException, DBConnectionException {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		try (final PreparedStatement st = DbManager.prepareStatement(TRANSACTIONS_BYOPERATION);) {
			st.setInt(1, year);
			st.setInt(2, month);
			try (final ResultSet rs = st.executeQuery();) {

				final JsonArrayBuilder data = Json.createArrayBuilder();

				// Si no hay registros, devolveremos un error
				if (!rs.next()) {
					data.add(Json.createObjectBuilder()
							.add("Code", 204) //$NON-NLS-1$
							.add("Message","No existen registros para la consulta 'Transacciones realizadas seg&uacute;n el tipo de transacci&oacute;n (simple o lote)' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					jsonObj.add("Error", data); //$NON-NLS-1$
				}
				// Si hay registros, los agregamos al JSON de salida
				else {
					do {
						data.add(Json.createObjectBuilder()
								.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
								.add("FirmasSimplesCorrectas", rs.getString(2)) //$NON-NLS-1$
								.add("FirmasSimplesINCorrectas", rs.getString(3)) //$NON-NLS-1$
								//TODO: Revisar si con MySQL y Oracle se pueden extraer directamente como long
								.add("TOTAL_SIMPLES", Integer.parseInt(rs.getString(2)) + Integer.parseInt(rs.getString(3))) //$NON-NLS-1$
								.add("FirmasLotesCorrectas", rs.getString(4)) //$NON-NLS-1$
								.add("FirmasLotesINCorrectas", rs.getString(5)) //$NON-NLS-1$
								//TODO: Revisar si con MySQL y Oracle se pueden extraer directamente como long
								.add("TOTAL_LOTES",Integer.parseInt(rs.getString(4)) + Integer.parseInt(rs.getString(5))) //$NON-NLS-1$
								);
					} while (rs.next());

					jsonObj.add("TransByOperation", data); //$NON-NLS-1$
				}
			}
		}

		// Pasamos a texto el objeto JSON
		final StringWriter writer = new StringWriter();
		try  (final JsonWriter jw = Json.createWriter(writer);) {
		    jw.writeObject(jsonObj.build());
		}

		return writer.toString();
	}
}
