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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.gob.fire.server.admin.conf.DbManager;
import es.gob.fire.server.admin.entity.Log;

/**
 * Manejador para la gesti&oacute;n de las trazas de la aplicaci&oacute;n.
 */
public class LogsDAO {

	private static final String STATEMENT_ALL_LOG = "SELECT fecha, level_Error, source_class_name, source_method_name, message, thrown FROM tb_log"; //$NON-NLS-1$

	/**
	 * Recupera los elementos de la tabla tb_log.
	 * @return Lista con los log existentes en la tabla.
	 * @throws SQLException Cuando ocurre un error al acceder a las trazas de registro.
	 */
	public static List<Log> getLogs()throws SQLException {

	    final List<Log> result = new ArrayList<Log>();
	    final String statement = STATEMENT_ALL_LOG;
	    final PreparedStatement st = DbManager.prepareStatement(statement);
	    final ResultSet rs = st.executeQuery();
	    while (rs.next()) {
	        final Log log = new Log();
	        log.setFecha(rs.getLong(1));
	        log.setLevelError(rs.getString(2));
	        log.setSourceClassName(rs.getString(3));
	        log.setSourceMethod(rs.getString(4));
	        log.setMessage(rs.getString(5));
	        log.setThrowable(rs.getString(6));
	        result.add(log);
	    }
	    rs.close();
        st.close();
	    return result;
	}
}
