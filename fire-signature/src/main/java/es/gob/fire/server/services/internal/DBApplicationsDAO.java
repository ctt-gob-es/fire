/* Copyright (C) 2023 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 13/07/2023
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import es.gob.fire.alarms.Alarm;
import es.gob.fire.signature.DbManager;


/**
 * DAO para la gesti&oacute;n de aplicaciones dadas de alta en base de datos.
 */
public class DBApplicationsDAO implements ApplicationsDAO {

	private static final Logger LOGGER = Logger.getLogger(DBApplicationsDAO.class.getName());

	private static final String STATEMENT_SELECT_ACCESS_INFO = "SELECT tb_aplicaciones.nombre, tb_aplicaciones.habilitado, tb_certificados.huella, tb_aplicaciones.dir3_code " //$NON-NLS-1$
			+ "FROM tb_aplicaciones, tb_certificados, tb_certificados_de_aplicacion " //$NON-NLS-1$
			+ "WHERE tb_aplicaciones.id =  ? " //$NON-NLS-1$
				+ "AND tb_aplicaciones.id=tb_certificados_de_aplicacion.id_aplicaciones " //$NON-NLS-1$
				+ "AND tb_certificados.id_certificado=tb_certificados_de_aplicacion.id_certificados"; //$NON-NLS-1$

	private final DBOperationConfigLoader operationConfigLoader;

	public DBApplicationsDAO() {
		this.operationConfigLoader = new DBOperationConfigLoader();
	}

	@Override
	public ApplicationAccessInfo getApplicationAccessInfo(final String appId, final LogTransactionFormatter logF)
			throws IOException {

		ApplicationAccessInfo result;

		// Comprobamos en BD
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(STATEMENT_SELECT_ACCESS_INFO);) {

			st.setString(1, appId);

			try (ResultSet rs = st.executeQuery()) {
				if (!rs.next()) {
					LOGGER.fine(logF.f("No se ha encontrado en el sistema la aplicacion con el ID: " + appId)); //$NON-NLS-1$
					return null;
				}

				final String appName = rs.getString(1);
				final boolean enabled = rs.getBoolean(2);
				DigestInfo[] digestInfos = null;
				final String dir3Code = rs.getString(4);

				if (enabled) {
					digestInfos = loadCertificatesInfo(rs, logF);
				}

				result = new ApplicationAccessInfo(appId, appName, enabled, digestInfos, dir3Code);
			}
		}
		catch (final SQLException e) {
    		AlarmsManager.notify(Alarm.CONNECTION_DB);
			throw new IOException("Error al consultar en BD la informacion de acceso de la aplicacion", e); //$NON-NLS-1$
		}

		return result;
	}

	private static DigestInfo[] loadCertificatesInfo(final ResultSet rs, final LogTransactionFormatter logF) throws SQLException {

		final List<DigestInfo> certDigests = new ArrayList<>();

		do {
			final DigestInfo digest = loadCertDigest(rs.getString(3), logF);
			if (digest != null) {
				certDigests.add(digest);
			}
		} while (rs.next());

		return !certDigests.isEmpty() ? certDigests.toArray(new DigestInfo[0]) : null;
	}

	private static DigestInfo loadCertDigest(final String certDigest, final LogTransactionFormatter logF) {

		if (certDigest == null || certDigest.isEmpty()) {
			return null;
		}

		final byte[] hash = Base64.getDecoder().decode(certDigest);

		DigestInfo digestInfo = null;
		try {
			digestInfo = DigestInfo.create(hash);
		}
		catch (final Exception e) {
			LOGGER.warning(logF.f("Una de las huellas registrada por la aplicacion no es valida: " + e)); //$NON-NLS-1$
			digestInfo = null;
		}
		return digestInfo;
	}

	@Override
	public ApplicationOperationConfig getOperationConfig(final String appId,
			final LogTransactionFormatter logF) throws IOException {

		ApplicationOperationConfig config = this.operationConfigLoader.getOperationConfig(appId);

		if (config == null) {
			LOGGER.warning(logF.f("No se encuentra establecida ninguna configuracion para las aplicaciones. " //$NON-NLS-1$
					+ "No se estableceran limites para el servicio")); //$NON-NLS-1$
			config = new ApplicationOperationConfig();
		}

		return config;
	}
}
