/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.afirma.AfirmaConnector;
import es.gob.fire.upgrade.afirma.Upgrade;
import es.gob.fire.upgrade.afirma.UpgradeTarget;

/** Prueba de mejora de firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestUpgrade {

	/** Identificador del documento que se deseea recuperar as&iacute;ncronamente. Este valor
	 * deber&iacute;a ajustarse para cada prueba. */
	private static final String DOCID = "1111"; //$NON-NLS-1$

	/** Nombre de la propiedad en la que se guarda el nombre de la aplicacion con el que debe
	 * conectarse a la plataforma @firma. */
	private static final String PROP_APPID = "afirma.appId"; //$NON-NLS-1$

	private byte[] testFile;
	private AfirmaConnector conn;
	private Properties config;

	@Before
	public void loadSignatures() throws IOException {

		// Cargamos los ficheros de prueba
		try (
				//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_detached_bin.xml"); //$NON-NLS-1$
				//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_enveloping_bin.xml"); //$NON-NLS-1$
				//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_enveloped_xml.xml"); //$NON-NLS-1$
				final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_detached_xml.xml"); //$NON-NLS-1$
				//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_enveloping_xml.xml"); //$NON-NLS-1$
				//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/pades.pdf"); //$NON-NLS-1$
				) {
			int nBytes = 0;
			final byte[] buffer = new byte[4096];
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((nBytes = input.read(buffer)) != -1) {
				baos.write(buffer, 0, nBytes);
			}
			this.testFile =  baos.toByteArray();
		}

		// Cargamos el conector para acceder a la plataforma
		this.conn = new AfirmaConnector();
		this.config = new Properties();
		try (InputStream is = TestUpgrade.class.getResourceAsStream("/platform.properties");) { //$NON-NLS-1$
			this.config.load(is);
		}
		this.conn.init(this.config);
	}

	/** Prueba de mejora de firma.
	 * @throws Exception en cualquier error. */
	@Test
	//@Ignore
	public void testSignUpgrade() throws Exception {

		final UpgradeTarget format = UpgradeTarget.T_LEVEL_FORMAT;

		final UpgradeResult result = Upgrade.upgradeSignature(this.conn, this.testFile, format, this.config.getProperty(PROP_APPID), true);

		System.out.println("La firma se actualizo a " + result.getFormat()); //$NON-NLS-1$

		final File saveFile = File.createTempFile("TEST-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
		try (final java.io.OutputStream os = new java.io.FileOutputStream(saveFile)) {
			os.write(result.getResult());
			os.flush();
		}
		System.out.println("Temporal para comprobacion manual: " + saveFile.getAbsolutePath()); //$NON-NLS-1$
	}

	/** Prueba de mejora de firma as&iacute;ncrona. Requiere que la aplicaci&oacute;n configurada
	 * en la plataforma respete los periodos de gracia para la actualizaci&oacute;n de la firma.
	 * @throws Exception en cualquier error. */
	@Test
	//@Ignore
	public void testSignUpgradeAsync() throws Exception {

		final UpgradeTarget format = UpgradeTarget.T_LEVEL_FORMAT;

		final UpgradeResult result = Upgrade.upgradeSignature(this.conn, this.testFile, format, this.config.getProperty(PROP_APPID), false);

		System.out.println("La firma se actualizo a " + result.getFormat()); //$NON-NLS-1$

		final File saveFile = File.createTempFile("TEST-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
		try (final java.io.OutputStream os = new java.io.FileOutputStream(saveFile)) {
			os.write(result.getResult());
			os.flush();
		}
		System.out.println("Temporal para comprobacion manual: " + saveFile.getAbsolutePath()); //$NON-NLS-1$
	}

	/** Prueba de recuperaci&oacute;n de firma enviada a actualizar anteriormente.
	 * @throws Exception en cualquier error. */
	@Test
	//@Ignore
	public void testRecoverSignatureAsync() throws Exception {

		final UpgradeTarget format = UpgradeTarget.T_LEVEL_FORMAT;

		final UpgradeResult result = Upgrade.recoverUpgradedSignature(this.conn, DOCID, format, this.config.getProperty(PROP_APPID));

		System.out.println("La firma se actualizo a " + result.getFormat()); //$NON-NLS-1$

		final File saveFile = File.createTempFile("TEST-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
		try (final java.io.OutputStream os = new java.io.FileOutputStream(saveFile)) {
			os.write(result.getResult());
			os.flush();
		}
		System.out.println("Temporal para comprobacion manual: " + saveFile.getAbsolutePath()); //$NON-NLS-1$
	}

}
