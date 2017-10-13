/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador de la configuraci&oacute;n del sistema.
 */
public class ConfigurationDAO {

	private static final String DEFAULT_CHARSET = "utf-8"; //$NON-NLS-1$

	private static final String MD_ALGORITHM = "SHA-256"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(ConfigurationDAO.class.getName());

	private static final String STATEMENT_SELECT_CONFIG_VALUE = "SELECT valor FROM tb_configuracion WHERE parametro = ?"; //$NON-NLS-1$

	private static final String KEY_ADMIN_PASS = "admin_pass"; //$NON-NLS-1$


	/**
	 * Comprueba contra base de datos que la contrase&ntilde;a indicada se corresponda
	 * con la del administrador del sistema.
	 * @param psswd Contrase&ntilde;a.
	 * @return {@code true} si la contrase&ntilde;a es del administrador, {@code false} en caso contrario.
	 * @throws SQLException Cuando ocurre alg&uacute;n error durante la comprobaci&oacute;n.
	 */
	public static boolean checkAdminPassword(final String psswd) throws SQLException {

		final byte[] md;
		try {
			md = MessageDigest.getInstance(MD_ALGORITHM).digest(psswd.getBytes(DEFAULT_CHARSET));
		} catch (final NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, "Error de configuracion en el servicio de administracion. Algoritmo de huella incorrecto", e); //$NON-NLS-1$
			return false;
		} catch (final UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, "Error de configuracion en el servicio de administracion. Codificacion incorrecta", e); //$NON-NLS-1$
			return false;
		}

		boolean result;
		final String keyAdminB64 = getConfigValue(KEY_ADMIN_PASS);
		if (keyAdminB64 ==  null || !keyAdminB64.equals(Base64.encode(md))) {
			LOGGER.severe("Se ha insertado una contrasena de administrador no valida"); //$NON-NLS-1$
			result = false;
		}
		else {
			LOGGER.info("El administrador se ha logueado correctamente"); //$NON-NLS-1$
			result = true;
		}

		return result;
	}

	/**
	 * Obtiene el valor de una clave de la tabla de par&aacute;metros de configuraci&oacute;n.
	 * @param conn Conexi&oacute;n con base de datos.
	 * @param param Clave del par&aacute;metro a obtener.
	 * @return Valor del par&aacute;metro de configuraci&oacute;n.
	 * @throws SQLException Cuando ocurre un error en el acceso al par&aacute;metro.
	 */
	private static String getConfigValue(final String param) throws SQLException {
		String value = null;
		try {
			final PreparedStatement st = DbManager.prepareStatement(STATEMENT_SELECT_CONFIG_VALUE);
			st.setString(1, param);
			final ResultSet rs = st.executeQuery();
			if (rs.next()) {
				value = rs.getString(1);
			}
			rs.close();
			st.close();

		} catch (final Exception e) {
			LOGGER.severe("Se ha producido un error al realizar la consulta en la base de datos:" + e); //$NON-NLS-1$
			throw new SQLException(e);
		}
		return value;
	}

}
