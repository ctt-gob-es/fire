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
import es.gob.fire.signature.ProviderElements;


/**
 * DAO para la gesti&oacute;n de aplicaciones dadas de alta en base de datos.
 */
public class DBApplicationsDAO implements ApplicationsDAO {

	private static final Logger LOGGER = Logger.getLogger(DBApplicationsDAO.class.getName());

	private static final String STATEMENT_SELECT_ACCESS_INFO = "SELECT configurado, nombre, habilitado, huella_principal, huella_backup FROM tb_aplicaciones, tb_certificados  WHERE  tb_aplicaciones.id =  ?  AND tb_aplicaciones.fk_certificado=tb_certificados.id_certificado"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_OPERATION_CONFIG = "SELECT tamano_peticion, tamano_documento, tamano_lote, proveedores FROM tb_aplicaciones  WHERE  tb_aplicaciones.id =  ?"; //$NON-NLS-1$


	@Override
	public ApplicationAccessInfo getApplicationAccessInfo(final String appId, final TransactionAuxParams trAux)
			throws IOException {

		ApplicationAccessInfo result;

		// Comprobamos en BD
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(STATEMENT_SELECT_ACCESS_INFO);) {

			st.setString(1, appId);

			try (ResultSet rs = st.executeQuery()) {
				if (!rs.next()) {
					LOGGER.fine(trAux.getLogFormatter().f("No se ha encontrado en el sistema la aplicacion con el ID: " + appId)); //$NON-NLS-1$
					return null;
				}

				DigestInfo[] digestInfo = null;

				final boolean enabled = rs.getBoolean(2);
				if (enabled) {
					digestInfo = loadCertificatesInfo(rs.getString(3), rs.getString(4), trAux);
				}

				result = new ApplicationAccessInfo(appId, rs.getString(1), enabled, digestInfo);
			}
		}
		catch (final SQLException e) {
    		AlarmsManager.notify(Alarm.CONNECTION_DB);
			throw new IOException("Error al consultar en BD la informacion de acceso de la aplicacion", e); //$NON-NLS-1$
		}

		return result;
	}

	private static DigestInfo[] loadCertificatesInfo(final String certDigest1, final String certDigest2, final TransactionAuxParams trAux) {

		final List<DigestInfo> certDigests = new ArrayList<>();

		final DigestInfo digest1 = loadCertDigest(certDigest1, trAux);
		if (digest1 != null) {
			certDigests.add(digest1);
		}

		final DigestInfo digest2 = loadCertDigest(certDigest2, trAux);
		if (digest2 != null) {
			certDigests.add(digest2);
		}

		return !certDigests.isEmpty() ? certDigests.toArray(new DigestInfo[0]) : null;
	}

	private static DigestInfo loadCertDigest(final String certDigest, final TransactionAuxParams trAux) {

		if (certDigest == null || certDigest.isEmpty()) {
			return null;
		}

		final byte[] hash = Base64.getDecoder().decode(certDigest);

		DigestInfo digestInfo = null;
		try {
			digestInfo = DigestInfo.create(hash);
		}
		catch (final Exception e) {
			LOGGER.warning(trAux.getLogFormatter().f("Una de las huellas registrada por la aplicacion no es valida: " + e)); //$NON-NLS-1$
			digestInfo = null;
		}
		return digestInfo;
	}

	@Override
	public AplicationOperationConfig getOperationConfig(final String appId, final TransactionAuxParams trAux) throws IOException {

		AplicationOperationConfig result = null;

		// Comprobamos en BD
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(STATEMENT_SELECT_OPERATION_CONFIG);) {

			st.setString(1, appId);

			try (ResultSet rs = st.executeQuery()) {
				if (!rs.next()) {
					LOGGER.fine(trAux.getLogFormatter().f("No se ha encontrado en el sistema la aplicacion con el ID: " + appId)); //$NON-NLS-1$
					return null;
				}

				final boolean configured = rs.getBoolean(1);
				if (configured) {
					result = new AplicationOperationConfig();
					result.setRequestMaxSize(rs.getInt(2));
					result.setParamsMaxSize(rs.getInt(3));
					result.setBatchMaxDocuments(rs.getInt(4));
					result.setProviders(ProviderElements.parse(rs.getString(5)));
				}
			}
		}
		catch (final SQLException e) {
    		AlarmsManager.notify(Alarm.CONNECTION_DB);
			throw new IOException("Error al consultar en BD la configuracion de la apicacion", e); //$NON-NLS-1$
		}

		return result;
	}
}
