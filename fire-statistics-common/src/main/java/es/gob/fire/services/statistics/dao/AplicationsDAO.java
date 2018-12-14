/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.services.statistics.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.config.DbManager;
import es.gob.fire.services.statistics.entity.Application;





/**
 * DAO para la gesti&oacute;n de aplicaciones dadas de alta en el sistema.
 */
public class AplicationsDAO {

	private static final String STATEMENT_SELECT_APPLICATION_BYID = "SELECT id, nombre, responsable, resp_correo, resp_telefono, fecha_alta,fk_certificado FROM tb_aplicaciones WHERE id= ?"; //$NON-NLS-1$


	/**
	 * Devuelve una aplicaci&oacute;n registrada en el sistema dado su id.
	 * @param id de la aplicaci&oacute;n a encontrar.
	 * @return aplicaci&oacute;n encontrada.
	 * @throws SQLException si hay un problema en la conexi&oacute;n con la base de datos
	 * @throws DBConnectionException
	 */
	public static Application selectApplication(final String id) throws SQLException, DBConnectionException {
		final Application result = new Application();
		final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_APPLICATION_BYID);
		st.setString(1, id);
		final ResultSet rs = st.executeQuery();
		if (rs.next()){
			result.setId(rs.getString(1));
			result.setNombre(rs.getString(2));
			result.setResponsable(rs.getString(3));
			result.setCorreo(rs.getString(4));
			result.setTelefono(rs.getString(5));
			result.setAlta(rs.getDate(6));
			result.setFk_certificado(rs.getString(7));
		}
		rs.close();
		st.close();
		return result;

	}



}
