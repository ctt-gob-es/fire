/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signature;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.signers.tsp.pkcs7.CMSTimestamper;
import es.gob.afirma.signers.tsp.pkcs7.TsaParams;

/** Gestor de registro con sello de tiempo.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class LoggingHandler extends Handler {

	private static final Logger LOGGER = Logger.getLogger(LoggingHandler.class.getName());
	private static final String LOG_TIMESTAMP_ALGORITHM = "SHA-1"; //$NON-NLS-1$


	private static CMSTimestamper timestamper = null;

	private static PreparedStatement pstmt = null;
	private static Connection conn = null;

	private static final String STATEMENT_INSERT_LOG =
		"INSERT INTO tb_log " //$NON-NLS-1$
		+ "(fecha, level_Error, time_stam, source_class_name, source_method_name, message, thrown) VALUES" //$NON-NLS-1$
		+ "(?,?,?,?,?,?,?)"; //$NON-NLS-1$

	static {

		if (ConfigManager.existUseTsp()) {
			try {
				timestamper = getTimestamper(ConfigManager.getPropertyFile());
			}
			catch(final Exception e) {
				LOGGER.severe(
					"No ha podido crearse el sellador de tiempo, el registro no se sellara: " + e //$NON-NLS-1$
				);
				timestamper = null;
			}
		}

		if (ConfigManager.isUsedLoggingBd()) {
			try {
				conn = getConnection();
				conn.setAutoCommit(false);
			}
			catch(final Exception e) {
				LOGGER.severe(
					"No ha podido crearse la conexion con la BBDD, el registro no se almacenara: " + e //$NON-NLS-1$
				);
				conn = null;
			}
			if (conn != null) {
				try {
					LOGGER.info("Conexion con la BBDD establecida: " + !conn.isClosed()); //$NON-NLS-1$
				}
				catch (final SQLException e) {
					LOGGER.severe(
						"No ha podido comprobar el estado de la conexion con la BBDD: " + e //$NON-NLS-1$
					);
				}
				try {
					pstmt = conn.prepareStatement(STATEMENT_INSERT_LOG);
				}
				catch (final SQLException e) {
					LOGGER.severe(
						"No ha podido crearse la sentencia SQL, el registro no se almacenara: " + e //$NON-NLS-1$
					);
					pstmt = null;
				}
			}
		}
	}

	private static Connection getConnection() throws InstantiationException,
	                                                                   IllegalAccessException,
	                                                                   ClassNotFoundException,
	                                                                   SQLException {
		Class.forName(ConfigManager.getJdbcDriverString()).newInstance();
		return DriverManager.getConnection(ConfigManager.getDataBaseConnectionString());
	}

	private static CMSTimestamper getTimestamper(final Properties p) {
		return new CMSTimestamper(new TsaParams(p));
	}

	/** Instala el gestor de log seguro para <code>es.gob.fire</code>.
	 * @throws SecurityException Si no hay permisos para la instalaci&oacute;n.
	 * @throws IOException Si no se puede leer o aplicar la configuraci&oacute;n del
	 *                     registro. */
	public static void install() throws SecurityException, IOException {
		for (final Handler h : LOGGER.getHandlers()) {
			if (h instanceof LoggingHandler) {
				return;
			}
		}
		LOGGER.addHandler(new LoggingHandler());
	}

	@Override
	public void close() throws SecurityException {
		if (pstmt != null) {
			try {
				pstmt.close();
			}
			catch (final Exception e) {
				System.out.println("Error cerrando la sentencia SQL: " + e); //$NON-NLS-1$
			}
		}
		if (conn != null) {
			try {
				conn.close();
			}
			catch (final Exception e) {
				System.out.println("Error cerrando la conexion con la BBDD: " + e); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void flush() {
		// Vacio
	}

	@Override
	public void publish(final LogRecord lr) {
		if (lr.getLevel() instanceof LoggingAuditableLevel) {
			final Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(lr.getMillis());
			byte[] tsp = "NOTSP".getBytes(); //$NON-NLS-1$
			if (timestamper != null) {
				try {
					tsp = timestamper.getTimeStampToken(
						(lr.getMillis() + " : " + lr.getMessage()).getBytes(), //$NON-NLS-1$
						LOG_TIMESTAMP_ALGORITHM,
						cal
					);
				}
				catch (final Exception e) {
					System.out.println(
						"Error obteniendo el sello de tiempo, el registro no lo incluira: " + e //$NON-NLS-1$
					);
				}
			}
			if (conn != null && pstmt != null) {
				try {
					insertLogRecord(lr, Base64.encode(tsp));
				}
				catch(final Exception e) {
					e.printStackTrace();
					System.out.println(
						"No se ha podido insertar el registro en BBDD ('" + lr.getMessage() + "'): " + e //$NON-NLS-1$ //$NON-NLS-2$
					);
				}
			}
		}
	}

	private static void insertLogRecord(final LogRecord lr, final String tspAsBase64) throws SQLException {
		pstmt.setLong  (1, lr.getMillis());
		pstmt.setString(2, lr.getLevel().toString());
		pstmt.setString(3, tspAsBase64);
		pstmt.setString(4, lr.getSourceClassName());
		pstmt.setString(5, lr.getSourceMethodName());
		pstmt.setString(6, lr.getMessage());
		pstmt.setString(7, lr.getThrown() != null ? lr.getThrown().toString() : null);
		pstmt.executeUpdate();
		conn.commit();
	}

}

