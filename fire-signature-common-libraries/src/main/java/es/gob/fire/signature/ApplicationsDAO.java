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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.Base64;


/**
 * DAO para la gesti&oacute;n de aplicaciones dadas de alta en el sistema.
 */
public class ApplicationsDAO {

	private static final Logger LOGGER = Logger.getLogger(ApplicationsDAO.class.getName());

	private static final String STATEMENT_SELECT_APP_NAME = "SELECT nombre, habilitado FROM tb_aplicaciones WHERE id = ?"; //$NON-NLS-1$

	private static final String STATEMENT_SELECT_CERT ="SELECT COUNT(*) FROM tb_aplicaciones, tb_certificados  WHERE  tb_aplicaciones.id =  ?  AND tb_aplicaciones.fk_certificado=tb_certificados.id_certificado AND (tb_certificados.huella_principal = ? OR tb_certificados.huella_backup=?)"; //$NON-NLS-1$


	/** Comprueba si una aplicaci&oacute;n est&aacute; habilitada en el sistema.
	 * @param appId Identificador de la aplicaci&oacute;n.
	 * @return Resultado de la comprobaci&oacute;n.
	 * @throws SQLException Cuando no se puede realizar la comprobaci&oacute;n.
	 */
	public static ApplicationChecking checkApplicationId(final String appId) throws SQLException {

		// Si no hay conexion con la BD y si esta la aplicacion en el fichero de configuracion,
		// comprobamos el identificador proporcionado contra el declarado en el fichero
		if (!DbManager.isConfigured() && ConfigManager.getAppId() != null) {
			final boolean valid = ConfigManager.getAppId().equals(appId);
			return new ApplicationChecking(appId, appId, valid, true);
		}

		ApplicationChecking result;

		// Comprobamos en BD
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(STATEMENT_SELECT_APP_NAME);) {


			st.setString(1, appId);

			if (!st.execute()) {
				LOGGER.fine("Error al buscar la aplicacion con el ID: " + appId); //$NON-NLS-1$
				return new ApplicationChecking(appId, null, false, false);
			}

			try (ResultSet rs = st.getResultSet();) {
				if (rs.next()) {
					LOGGER.fine("Se ha identificado correctamente una peticion de la aplicacion: " + appId); //$NON-NLS-1$
					result = new ApplicationChecking(appId, rs.getString(1), true, rs.getBoolean(2));
				}
				else {
					LOGGER.fine("No se ha encontrado en el sistema la aplicacion con el ID: " + appId); //$NON-NLS-1$
					result =  new ApplicationChecking(appId, null, false, false);
				}
			}
		}

		return result;
	}

	/**
	 * Comprueba si la huella digital que se pasa por par&aacute;metro existe en el sistema.
	 * @param appId Identificador de la aplicaci&oacute;n declarado.
	 * @param thumb Huella digital en base 64 que se va a comprobar.
	 * @return <code> true </code> en caso de que la huella exista en el sistema. <code> false </code> en caso contrario
	 * @throws SQLException En caso de ocurrir un error al acceder a la base de datos.
	 * @throws IOException Si hay un error de entrada o salida.
	 * @throws CertificateException Si hay un problema al decodificar el certificado.
	 * @throws NoSuchAlgorithmException No se encuentra el algoritmo en el sistema.
	 */
	public static boolean checkThumbPrint(final String appId, final String thumb) throws SQLException, CertificateException, IOException, NoSuchAlgorithmException {

		// Si no hay conexion con la BD y si esta el certificado en el fichero de configuracion, lo comprobamos
		if (!DbManager.isConfigured() && ConfigManager.getCert() != null) {
			// TODO: Comprobar si realmente es necesario componer el certificado y no basta con decodificarlo
			final X509Certificate cer = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate //$NON-NLS-1$
					(new ByteArrayInputStream(Base64.decode(ConfigManager.getCert())));

			// Sacamos la huella del certificado que tenemos en el fichero de propiedades
			final MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
			final String propertyThumb = Base64.encode(md.digest(cer.getEncoded()));

			return propertyThumb.equals(thumb);
		}

		boolean result;

		/*SELECT COUNT(*) FROM tb_aplicaciones, tb_certificados  WHERE  tb_aplicaciones.id =  ?
		 * AND tb_aplicaciones.fk_certificado=tb_certificados.id_certificado
		 * AND (tb_certificados.huella_principal = ? OR tb_certificados.huella_backup=?)*/
		try (Connection conn = DbManager.getConnection();
				PreparedStatement st = conn.prepareStatement(STATEMENT_SELECT_CERT);) {
			st.setString(1, appId);
			st.setString(2, thumb);
			st.setString(3, thumb);

			if (!st.execute()) {
				LOGGER.fine("No existe ningun certificado con la huella: " + thumb); //$NON-NLS-1$
				return false;
			}

			try (ResultSet rs = st.getResultSet();) {
				if (!rs.next()) {
					LOGGER.fine("No se ha podido leer la huella del certificado: " + thumb); //$NON-NLS-1$
					result = false;
				}
				else {
					LOGGER.fine("La huella del certificado se encuentra registrada en el sistema: " + thumb); //$NON-NLS-1$
					result = rs.getInt(1) > 0;
				}
			}
		}
		return result;
	}
}
