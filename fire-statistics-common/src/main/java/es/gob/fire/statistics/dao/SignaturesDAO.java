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
import es.gob.fire.statistics.entity.SignatureCube;

/**
 * Gestor para la manipulaci&oacute;n de la informaci&oacute;n de las firmas en base de datos.
 */
public class SignaturesDAO {


	/** SQL para insertar una firma. */
	private static final String ST_INSERT_SIGNATURE = "INSERT INTO TB_FIRMAS " //$NON-NLS-1$
			+ "(fecha, formato, formato_mejorado, algoritmo, proveedor, navegador, correcta, total, aplicacion) " //$NON-NLS-1$
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)"; //$NON-NLS-1$

	/*Consultas estadisticas de firmas*/

	/** Documentos firmados correctamente / incorrectamente por cada aplicaci&oacute;n.
	 * (Filtrado por a&ntilde;o y mes). */
	private static final String SIGNATURES_BYAPP = "SELECT f.aplicacion, "+ //$NON-NLS-1$
			" SUM(CASE When f.correcta = '1' then f.total else 0 end) AS CORRECTAS, " + //$NON-NLS-1$
			" SUM(CASE When f.correcta = '0' then f.total else 0 end) AS INCORRECTAS "+ //$NON-NLS-1$
			" FROM tb_firmas f "+ //$NON-NLS-1$
			" WHERE	year(f.fecha) = ? "+ //$NON-NLS-1$
			" AND month(f.fecha) = ? "+ //$NON-NLS-1$
			" GROUP BY f.aplicacion "; //$NON-NLS-1$

	/** Documentos firmados por cada origen de certificados/proveedor. (Filtrado por a&ntilde;o y mes). */
	private static final String SIGNATURES_BYPROVIDER = "SELECT f.proveedor, "+ //$NON-NLS-1$
			 " SUM(CASE When f.correcta = '1' then f.total else 0 end) AS CORRECTAS, "+ //$NON-NLS-1$
			 " SUM(CASE When f.correcta = '0' then f.total else 0 end) AS INCORRECTAS "+ //$NON-NLS-1$
			 " FROM tb_firmas f "+ //$NON-NLS-1$
			 " WHERE year(f.fecha) = ? AND month(f.fecha) = ? "+ //$NON-NLS-1$
			 " GROUP BY f.proveedor"; //$NON-NLS-1$


	/** Documentos firmados en cada formato de firma. (Filtrado por a&ntilde;o y mes). */
	private static final String SIGNATURES_BYFORMAT = "SELECT f.formato,"+  //$NON-NLS-1$
			 " SUM(CASE When f.correcta = '1' then f.total else 0 end) AS CORRECTAS, "+ //$NON-NLS-1$
			 " SUM(CASE When f.correcta = '0' then f.total else 0 end) AS INCORRECTAS "+ //$NON-NLS-1$
			 " FROM tb_firmas f "+ //$NON-NLS-1$
			 " WHERE year(f.fecha) = ? AND month(f.fecha) = ? "+ //$NON-NLS-1$
			 " GROUP BY f.formato "; //$NON-NLS-1$

	/** Documentos que utilizan cada formato de firma longevo. (Filtrado por a&ntilde;o y mes). */
	private static final String SIGNMATURES_BYLONGLIVE_FORMAT = "SELECT f.formato_mejorado, "+  //$NON-NLS-1$
			 " SUM(CASE When f.correcta = '1' then f.total else 0 end) AS CORRECTAS, "+ //$NON-NLS-1$
			 " SUM(CASE When f.correcta = '0' then f.total else 0 end) AS INCORRECTAS " + //$NON-NLS-1$
			 " FROM tb_firmas f "+ //$NON-NLS-1$
			 " WHERE f.formato_mejorado IS NOT NULL AND  year(f.fecha) = ? AND month(f.fecha) = ? "+  //$NON-NLS-1$
			 " GROUP BY f.formato_mejorado "; //$NON-NLS-1$

	/**
	 * Inserta una configuraci&oacute;n de operaci&oacute;n de firma en base de datos indicando
	 * cuantas veces se dio esta configuraci&oacute;n un d&iacute;a concreto.
	 * @param date Fecha del d&iacute;a en la que se realiz&oacute; la firma.
	 * @param signature Configuraci&oacute;n de la operaci&oacute;n de firma.
	 * @param total N&uacute;mero total de firmas que se generaron ese d&iacute;a con la
	 * configuraci&oacute;n indicada.
	 * @return {@code true} si la configuraci&oacute;n se inserto correctamente. {@code false}
	 * en caso contrario.
	 * @throws SQLException Cuando se produce un error al insertar los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static boolean insertSignature(final Date date, final SignatureCube signature, final long total)
			throws SQLException, DBConnectionException {

		try (final PreparedStatement st = DbManager.prepareStatement(ST_INSERT_SIGNATURE, false)) {
			st.setTimestamp (1, new java.sql.Timestamp(date.getTime()));
			st.setString(2, signature.getFormat());
			st.setString(3, signature.getImprovedFormat());
			st.setString(4, signature.getAlgorithm());
			st.setString(5, signature.getProvider());
			st.setString(6, signature.getBrowser());
			st.setBoolean(7, signature.isResultSign());
			st.setLong(8, total);
			st.setString(9, signature.getApplication());
			if (st.executeUpdate() < 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Obtiene el total de documentos firmados correctamente e incorrectamente por cada origen
	 * de certificados/proveedor. (Filtrado por a&ntilde;o y mes)
	 * @param year A&ntilde;o de la transacci&oacute;n.
	 * @param month Mes de la transacci&oacute;n.
	 * @return JSON con el nombre del proveedor, total de firmas correctas, total de
	 * firmas incorrectas y total global.
	 * @throws SQLException Cuando se produce un error al acceder a los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static String getSignaturesByProviderJSON(final int year, final int month) throws SQLException, DBConnectionException{

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		try (final PreparedStatement st = DbManager.prepareStatement(SIGNATURES_BYPROVIDER);) {
			st.setInt(1, year);
			st.setInt(2, month);
			try (final ResultSet rs = st.executeQuery();) {

				final JsonArrayBuilder data = Json.createArrayBuilder();

				// Si no hay registros, devolveremos un error
				if (!rs.next()) {
					data.add(Json.createObjectBuilder()
							.add("Code", 204) //$NON-NLS-1$
							.add("Message","No existen registros para la consulta 'Documentos firmados por cada origen de certificados/proveedor' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					jsonObj.add("Error", data); //$NON-NLS-1$
				}
				// Si hay registros, los agregamos al JSON de salida
				else {
					do {
						data.add(Json.createObjectBuilder()
								.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
								.add("CORRECTAS", rs.getString(2)) //$NON-NLS-1$
								.add("INCORRECTAS", rs.getString(3)) //$NON-NLS-1$
								.add("TOTAL", rs.getInt(2) + rs.getInt(3)) //$NON-NLS-1$
								);
					} while (rs.next());

					jsonObj.add("SignByProv", data); //$NON-NLS-1$
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

	/**
	 * Obtiene el total de documentos firmados correctamente e incorrectamente por cada
	 * aplicaci&oacute;n. (Filtrado por a&ntilde;o y mes)
	 * @param year A&ntilde;o de la transacci&oacute;n.
	 * @param month Mes de la transacci&oacute;n.
	 * @return JSON con el nombre de la aplicacion, total de firmas correctas, total de
	 * firmas incorrectas y total global.
	 * @throws SQLException Cuando se produce un error al acceder a los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static String getSignaturesByAppJSON(final int year, final int month) throws SQLException, DBConnectionException{

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		try (final PreparedStatement st = DbManager.prepareStatement(SIGNATURES_BYAPP);) {
			st.setInt(1, year);
			st.setInt(2, month);
			try (final ResultSet rs = st.executeQuery();) {

				final JsonArrayBuilder data = Json.createArrayBuilder();

				// Si no hay registros, devolveremos un error
				if (!rs.next()) {
					data.add(Json.createObjectBuilder()
							.add("Code", 204) //$NON-NLS-1$
							.add("Message","No existen registros para la consulta 'Documentos firmados correctamente / incorrectamente por cada aplicaci&oacute;n' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					jsonObj.add("Error", data); //$NON-NLS-1$
				}
				// Si hay registros, los agregamos al JSON de salida
				else {
					do {
						data.add(Json.createObjectBuilder()
								.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
								.add("CORRECTAS", rs.getString(2)) //$NON-NLS-1$
								.add("INCORRECTAS", rs.getString(3)) //$NON-NLS-1$
								.add("TOTAL", rs.getInt(2) + rs.getInt(3)) //$NON-NLS-1$
								);
					} while (rs.next());

					jsonObj.add("SignByApp", data); //$NON-NLS-1$
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

	/**
	 * Obtiene el total de documentos firmados correctamente e incorrectamente por cada formato de
	 * firma (Filtrado por a&ntilde;o y mes).
	 * @param year A&ntilde;o de la transacci&oacute;n.
	 * @param month Mes de la transacci&oacute;n.
	 * @return JSON con el formato de firma, total de firmas correctas, total de firmas incorrectas
	 * y total global.
	 * @throws SQLException Cuando se produce un error al acceder a los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static String getSignaturesByFormatJSON(final int year, final int month) throws SQLException, DBConnectionException{

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		try (final PreparedStatement st = DbManager.prepareStatement(SIGNATURES_BYFORMAT);) {
			st.setInt(1, year);
			st.setInt(2, month);
			try (final ResultSet rs = st.executeQuery();) {

				final JsonArrayBuilder data = Json.createArrayBuilder();

				// Si no hay registros, devolveremos un error
				if (!rs.next()) {
					data.add(Json.createObjectBuilder()
							.add("Code", 204) //$NON-NLS-1$
							.add("Message","No existen registros para la consulta 'Documentos firmados en cada formato de firma' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					jsonObj.add("Error", data); //$NON-NLS-1$
				}
				// Si hay registros, los agregamos al JSON de salida
				else {
					do {
						data.add(Json.createObjectBuilder()
								.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
								.add("CORRECTAS", rs.getString(2)) //$NON-NLS-1$
								.add("INCORRECTAS", rs.getString(3)) //$NON-NLS-1$
								.add("TOTAL", rs.getInt(2) + rs.getInt(3)) //$NON-NLS-1$
								);
					} while (rs.next());

					jsonObj.add("SignByFormat", data); //$NON-NLS-1$
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

	/**
	 * Obtiene el total de documentos firmados correctamente e incorrectamente por cada formato de
	 * firma longevo (Filtrado por a&ntilde;o y mes).
	 * @param year A&ntilde;o de la transacci&oacute;n.
	 * @param month Mes de la transacci&oacute;n.
	 * @return JSON con el formato mejorado de firma, total de firmas correctas, total de firmas incorrectas
	 * y total global.
	 * @throws SQLException Cuando se produce un error al acceder a los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static String getSignaturesByLongLiveFormatJSON(final int year, final int month) throws SQLException, DBConnectionException{

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();

		try (final PreparedStatement st = DbManager.prepareStatement(SIGNMATURES_BYLONGLIVE_FORMAT);) {
			st.setInt(1, year);
			st.setInt(2, month);

			try (final ResultSet rs = st.executeQuery();) {

				final JsonArrayBuilder data = Json.createArrayBuilder();

				// Si no hay registros, devolveremos un error
				if (!rs.next()) {
					data.add(Json.createObjectBuilder()
							.add("Code", 204) //$NON-NLS-1$
							.add("Message","No existen registros para la consulta 'Documentos que utilizan cada formato de firma longevo' para el Mes: " + String.valueOf(month) +"/"+ String.valueOf(year)+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					jsonObj.add("Error", data); //$NON-NLS-1$
				}
				// Si hay registros, los agregamos al JSON de salida
				else {
					do {
						data.add(Json.createObjectBuilder()
								.add("NOMBRE", rs.getString(1)) //$NON-NLS-1$
								.add("CORRECTAS", rs.getString(2)) //$NON-NLS-1$
								.add("INCORRECTAS", rs.getString(3)) //$NON-NLS-1$
								.add("TOTAL", rs.getInt(2) + rs.getInt(3)) //$NON-NLS-1$
								);
					} while (rs.next());

					jsonObj.add("SignByLongLiveFormat", data); //$NON-NLS-1$
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
