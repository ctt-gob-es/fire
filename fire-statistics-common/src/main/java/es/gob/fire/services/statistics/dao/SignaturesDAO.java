package es.gob.fire.services.statistics.dao;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.config.DbManager;
import es.gob.fire.services.statistics.entity.SignatureCube;

public class SignaturesDAO {


	/**Inserta */
	private static final String ST_INSERT_SIGNATURE = "INSERT INTO TB_FIRMAS " //$NON-NLS-1$
			+ "(fecha, formato ,formato_mejorado, algoritmo, proveedor, " //$NON-NLS-1$
			+ "navegador, correcta, id_transaccion, total) " //$NON-NLS-1$
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)"; //$NON-NLS-1$

	/*Consultas estadisticas de firmas*/

	/**Documentos firmados correctamente / incorrectamente por cada aplicaci&oacute;n. (Filtrado por a&ntilde;o y mes)*/
	private static final String SIGNATURES_BYAPP = "SELECT a.id, a.nombre," +//$NON-NLS-1$
			" SUM(CASE When f.correcta = 'false' then 1 else 0 end ) AS INCORRECTAS," +//$NON-NLS-1$
			" SUM(CASE When f.correcta = 'true' then 1 else 0 end ) AS CORRECTAS" +//$NON-NLS-1$
			" FROM tb_firmas f, tb_aplicaciones a, tb_transacciones t" +//$NON-NLS-1$
			" WHERE	a.id = t.id_aplicacion" +//$NON-NLS-1$
			" AND t.id_transaccion = f.id_transaccion" +//$NON-NLS-1$
			" AND year(f.fecha) = ?" +//$NON-NLS-1$
			" AND month(f.fecha) = ?" +//$NON-NLS-1$
			" GROUP BY a.id";//$NON-NLS-1$

	/**Documentos firmados por cada origen de certificados/proveedor. (Filtrado por a&ntilde;o y mes)*/
	private static final String SIGNATURES_BYPROVIDER = "SELECT p.nombre, " + //$NON-NLS-1$
			" SUM(CASE When f.correcta = 'false' then 1 else 0 end ) AS INCORRECTAS," + //$NON-NLS-1$
			" SUM(CASE When f.correcta = 'true' then 1 else 0 end ) AS CORRECTAS" + 	//$NON-NLS-1$
			" FROM tb_firmas f, tb_proveedores p" + //$NON-NLS-1$
			" WHERE	f.id_proveedor = p.id_proveedor	" + //$NON-NLS-1$
			" AND year(f.fecha) = ?" + //$NON-NLS-1$
			" AND month(f.fecha) = ?" + //$NON-NLS-1$
			" GROUP BY p.nombre";//$NON-NLS-1$

	/**Documentos firmados en cada formato de firma. (Filtrado por a&ntilde;o y mes)*/
	private static final String SIGNATURES_BYFORMAT = "SELECT fr.nombre, " + //$NON-NLS-1$
			" SUM(CASE When f.correcta = 'false' then 1 else 0 end ) AS INCORRECTAS, " + //$NON-NLS-1$
			" SUM(CASE When f.correcta = 'true' then 1 else 0 end ) AS CORRECTAS " + 	//$NON-NLS-1$
			" FROM tb_firmas f, tb_formatos fr " + //$NON-NLS-1$
			" WHERE	f.id_formato = fr.id_formato " + //$NON-NLS-1$
			" AND year(f.fecha) = ? " + //$NON-NLS-1$
			" AND month(f.fecha) = ? " + //$NON-NLS-1$
			" GROUP BY fr.nombre ";//$NON-NLS-1$

	/**Documentos que utilizan cada formato de firma longevo. (Filtrado por a&ntilde;o y mes)*/
	private static final String SIGNMATURES_BYLONGLIVE_FORMAT = "SELECT fm.nombre, " +  //$NON-NLS-1$
			" SUM(CASE When f.correcta = 'false' then 1 else 0 end ) AS INCORRECTAS, " +  //$NON-NLS-1$
			" SUM(CASE When f.correcta = 'true' then 1 else 0 end ) AS CORRECTAS " +  //$NON-NLS-1$
			" FROM tb_firmas f, tb_formatos_mejorados fm " +   //$NON-NLS-1$
			" WHERE f.id_formato_mejorado = fm.id_formato_mejorado " +  //$NON-NLS-1$
			" AND year(f.fecha) = ? " +  //$NON-NLS-1$
			" AND month(f.fecha) = ? " +  //$NON-NLS-1$
			" GROUP BY f.id_formato_mejorado"; //$NON-NLS-1$

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
			st.setString(2, sign.getFormat());
			st.setString(3, sign.getImprovedFormat());
			st.setString(4, sign.getAlgorithm());
			st.setString(5, sign.getProveedor());
			st.setString(6, sign.getNavegador().getName());
			st.setString (7, Boolean.toString(sign.isResultSign()));
			st.setString(8, sign.getId_transaccion());
			st.setLong(9, sign.getTotal().longValue());
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


			st.setString(2, signature.getFormat());
			if(signature.getImprovedFormat() !=  null && ! signature.getImprovedFormat().isEmpty()) {
				st.setString(3, signature.getImprovedFormat());
			}
			st.setString(4, signature.getAlgorithm());
			st.setString(5, signature.getProveedor());
			st.setString(6, signature.getNavegador().getName());
			st.setString (7, Boolean.toString(signature.isResultSign()));
			st.setString(8, signature.getId_transaccion());
			st.setLong(9, signature.getTotal().longValue());


//			if(signature.getIdImprovedFormat() !=  0) {
//				st.setInt(3,  signature.getIdImprovedFormat());
//			}
//			else {
//				st.setNull(3,Types.INTEGER);
//			}

			totalInsertReg = st.executeUpdate();
			st.close();
		}

		return totalInsertReg;
	}

	/**
	 * Obtiene el total de documentos firmados correctamente e incorrectamente por cada origen de certificados/proveedor. (Filtrado por a&ntilde;o y mes)
	 * @param year
	 * @param month
	 * @return String con formato JSON,   nombre proveedor, total incorrectos, total correctos
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static String getSignaturesByProviderJSON(final int year, final int month) throws SQLException, DBConnectionException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();


		final PreparedStatement st = DbManager.prepareStatement(SIGNATURES_BYPROVIDER);
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
			jsonObj.add("SignByProv", data); //$NON-NLS-1$
		}
		else {
			//No tiene registros
			data.add(Json.createObjectBuilder()
					.add("Code", 204) //$NON-NLS-1$
					.add("Message","No existen registros para la consulta 'Documentos firmados por cada origen de certificados/proveedor' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
	 * Obtiene el total de documentos firmados correctamente e incorrectamente por cada aplicaci&oacute;n. (Filtrado por a&ntilde;o y mes)
	 * @param year
	 * @param month
	 * @return String con formato JSON,  id aplicacion, nombre aplicacion, total incorrectos, total correctos
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static String getSignaturesByAppJSON(final int year, final int month) throws SQLException, DBConnectionException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();


		final PreparedStatement st = DbManager.prepareStatement(SIGNATURES_BYAPP);
		st.setInt(1, year);
		st.setInt(2, month);
		final ResultSet rs = st.executeQuery();
		if(rs.last()) {
			rs.beforeFirst();
			while (rs.next()) {
				data.add(Json.createObjectBuilder()
						.add("NOMBRE", rs.getString(2)) //$NON-NLS-1$
						.add("INCORRECTAS", rs.getString(3)) //$NON-NLS-1$
						.add("CORRECTAS", rs.getString(4)) //$NON-NLS-1$
						.add("TOTAL", String.valueOf( Integer.parseInt(rs.getString(3))+Integer.parseInt(rs.getString(4)))) //$NON-NLS-1$
						);
			}
			jsonObj.add("SignByApp", data); //$NON-NLS-1$
		}
		else {
			//No tiene registros
			data.add(Json.createObjectBuilder()
					.add("Code", 204) //$NON-NLS-1$
					.add("Message","No existen registros para la consulta 'Documentos firmados correctamente / incorrectamente por cada aplicaci&oacute;n' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
	 * Obtiene el total de documentos firmados correctamente e incorrectamente por cada formato de firma (Filtrado por a&ntilde;o y mes)
	 * @param year
	 * @param month
	 * @return String con formato JSON,   nombre formato, total incorrectos, total correctos
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static String getSignaturesByFormatJSON(final int year, final int month) throws SQLException, DBConnectionException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final PreparedStatement st = DbManager.prepareStatement(SIGNATURES_BYFORMAT);
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
			jsonObj.add("SignByFormat", data); //$NON-NLS-1$
		}
		else {
			//No tiene registros
			data.add(Json.createObjectBuilder()
					.add("Code", 204) //$NON-NLS-1$
					.add("Message","No existen registros para la consulta 'Documentos firmados en cada formato de firma' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
	 * Obtiene el total de documentos firmados correctamente e incorrectamente por cada formato de firma longevo (Filtrado por a&ntilde;o y mes)
	 * @param year
	 * @param month
	 * @return String con formato JSON,   nombre formato longevo, total incorrectos, total correctos
	 * @throws SQLException
	 * @throws DBConnectionException
	 */
	public static String getSignaturesByLongLiveFormatJSON(final int year, final int month) throws SQLException, DBConnectionException{

		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final PreparedStatement st = DbManager.prepareStatement(SIGNMATURES_BYLONGLIVE_FORMAT);
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
			jsonObj.add("SignByLongLiveFormat", data); //$NON-NLS-1$
		}
		else {
			//No tiene registros
			data.add(Json.createObjectBuilder()
					.add("Code", 204) //$NON-NLS-1$
					.add("Message","No existen registros para la consulta 'Documentos que utilizan cada formato de firma longevo' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
