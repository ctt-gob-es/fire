/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin.dao;

import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.fire.server.admin.conf.DbManager;
import es.gob.fire.server.admin.entity.LogServer;

/**
 * Manejador para la gesti&oacute;n de las trazas de la aplicaci&oacute;n.
 */
public class LogServersDAO {

	/**Lista todos los registros de la tabla tb_servidores_log, Consulta : SELECT id_servidor, nombre, url_servicio_log, clave FROM tb_servidores_log*/
	private static final String STMT_ALL_SERVER = "SELECT id_servidor, nombre, url_servicio_log FROM tb_servidores_log"; //$NON-NLS-1$

	/**Obtiene un registro de la tabla tb_servidores_log por id_servidor, Consulta: SELECT id_servidor, nombre, url_servicio_log, clave FROM tb_servidores_log WHERE id_servidor = ?*/
	private static final String STMT_SERVER_BYID = "SELECT id_servidor, nombre, url_servicio_log, clave, verificar_ssl FROM tb_servidores_log WHERE id_servidor = ?"; //$NON-NLS-1$

	/**Instrucci&oacute;n de Inserci&oacute;n : INSERT INTO tb_servidores_log (nombre, url_servicio_log,clave) VALUES(?,?,?)*/
	private static final String STMT_INSERT_SERVER = "INSERT INTO tb_servidores_log (nombre, url_servicio_log,clave,verificar_ssl) VALUES(?,?,?,?)"; //$NON-NLS-1$

	/**Instrucci&oacute;n de Actualizaci&oacute;n : UPDATE tb_servidores_log SET nombre=?, url_servicio_log = ?, clave = ?  WHERE id_servidor = ?)*/
	private static final String STMT_UPDATE_SERVER = "UPDATE tb_servidores_log SET nombre=?, url_servicio_log = ?, clave = ?, verificar_ssl = ?  WHERE id_servidor = ?"; //$NON-NLS-1$

	/**Instrucci&oacute;n de eliminaci&oacute;n: DELETE FROM tb_servidores_log WHERE id_servidor=?*/
	private static final String STMT_DELETE_SERVER = "DELETE FROM tb_servidores_log WHERE id_servidor = ?"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(LogServersDAO.class.getName());
	/**
	 * Recupera los elementos de la tabla tb_servidores_log.
	 * @return Lista con los servidores existentes en la tabla.
	 * @throws SQLException Cuando ocurre un error al acceder a las trazas de registro.
	 */
	public static String getLogServersJSON() throws SQLException {
		final StringWriter writer = new StringWriter();
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();


		final PreparedStatement st = DbManager.prepareStatement(STMT_ALL_SERVER);
		final ResultSet rs = st.executeQuery();
		while (rs.next()) {
			data.add(Json.createObjectBuilder()
					.add("id_servidor", rs.getInt(1)) //$NON-NLS-1$
					.add("nombre", rs.getString(2)) //$NON-NLS-1$
					.add("url_servicio_log", rs.getString(3)) //$NON-NLS-1$
					);
		}
		rs.close();
		st.close();
		jsonObj.add("LogSrvs", data); //$NON-NLS-1$

		try  {
			final JsonWriter jw = Json.createWriter(writer);
		    jw.writeObject(jsonObj.build());
		    jw.close();
		}
		catch (final Exception e) {
			LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de servidores de log", e); //$NON-NLS-1$
		}
		return writer.toString();
	}

	/**
	 * Obtiene un registro de la tabla tb_servidores_log por id_servidor
	 * @param id
	 * @return LogServer
	 * @throws SQLException
	 */
	public static LogServer selectLogServer(final String id) throws SQLException {
		final LogServer result = new LogServer();
		final PreparedStatement st = DbManager.prepareStatement(STMT_SERVER_BYID);
		st.setString(1, id);
		final ResultSet rs = st.executeQuery();
		if (rs.next()){
			result.setId(rs.getInt(1));
			result.setNombre(rs.getString(2));
			result.setUrl(rs.getString(3));
			result.setClave(rs.getString(4));
			result.setVerificarSsl(rs.getBoolean(5));
		}
		rs.close();
		st.close();
		return result;

	}

	/**
	 * Agrega un nuevo servidor de log.
	 * @param nombre Nombre descriptivo del servidor.
	 * @param url_servicio URL completa del servicio proporcionado por el servidor de gestion de logs.
	 * @param clave Clave para la validaci&oacute;n de la conexi&oacute;n.
	 * @param verificarSsl Indica si se debe verificar el certificado SSL del servidor.
	 * @return Devuelve 0 si no se pudo dar de alta el servidor y un valor mayor a 0 si s&iacute;
	 * se pudo.
	 * @throws SQLException Cuando ocurre algun error durante la sensaci&oacute;n.
	 * @throws GeneralSecurityException Cuando ocurri&oacute; un problema de permisos.
	 */
	public static int createLogServer(final String nombre, final String url_servicio, final String clave, final boolean verificarSsl)  throws SQLException, GeneralSecurityException {

		final PreparedStatement st = DbManager.prepareStatement(STMT_INSERT_SERVER);

		st.setString(1, nombre);
		st.setString(2, url_servicio);
		st.setString(3, clave);
		st.setBoolean(4, verificarSsl);

		LOGGER.info("Damos de alta el servidor '" + nombre + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		final int result = st.executeUpdate();
		st.close();
		return result;
	}


	/**
	 *
	 * @param id
	 * @param nombre
	 * @param url
	 * @param clave Clave para el acceso al servidor.
	 * @param verificarSsl Indica si se debe verificar el certificado SSL del servidor.
	 * @return Devuelve 0 si no se pudo actualizar el servidor y un valor mayor a 0 si s&iacute;
	 * se pudo.
	 * @throws SQLException
	 */
	public static int updateLogServer (final String id, final String nombre, final String url,
			final String clave, final boolean verificarSsl) throws SQLException{
		final PreparedStatement st = DbManager.prepareStatement(STMT_UPDATE_SERVER);

		st.setString(1, nombre);
		st.setString(2, url);
		st.setString(3, clave);
		st.setBoolean(4, verificarSsl);
		st.setString(5, id);

		LOGGER.info("Actualizamos el servidor '" + nombre + "' con el ID: " + id); //$NON-NLS-1$ //$NON-NLS-2$
		final int result = st.executeUpdate();
		st.close();
		return result;
	}

	/**
	 * Elimina un servidor de la tabla tb_servidores_log
	 * @param id
	 * @return Devuelve 0 si no se pudo eliminar el servidor y un valor mayor a 0 si s&iacute;
	 * se pudo.
	 * @throws SQLException
	 */
	public static int removeServer(final String id) throws SQLException {

		final PreparedStatement st = DbManager.prepareStatement(STMT_DELETE_SERVER);
		st.setString(1, id);
		LOGGER.info("Damos de baja el servidor con el ID: " + id); //$NON-NLS-1$
		final int result = st.executeUpdate();
		st.close();
		return result;
	}

}
