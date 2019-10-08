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
import java.io.InputStream;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import es.gob.fire.upgrade.UpgradeResult;
import es.gob.fire.upgrade.afirma.AfirmaConnector;
import es.gob.fire.upgrade.afirma.Upgrade;
import es.gob.fire.upgrade.afirma.UpgradeTarget;

/** Prueba de mejora de firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestUpgrade {

	/** Prueba de mejora de firma.
	 * @throws Exception en cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	@Ignore
	public void testSignUpgrade() throws Exception {

		final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_detached_bin.xml"); //$NON-NLS-1$
		//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_enveloping_bin.xml"); //$NON-NLS-1$
		//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_enveloped_xml.xml"); //$NON-NLS-1$
		//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_detached_xml.xml"); //$NON-NLS-1$
		//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_enveloping_xml.xml"); //$NON-NLS-1$
		//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/pades.pdf"); //$NON-NLS-1$
		//final java.io.InputStream input = new FileInputStream("C:/Users/carlos/Desktop/02 - firma CADES.csig"); //$NON-NLS-1$

        int nBytes = 0;
        final byte[] buffer = new byte[4096];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((nBytes = input.read(buffer)) != -1) {
            baos.write(buffer, 0, nBytes);
        }
        final byte[] testFile =  baos.toByteArray();

        input.close();

		final UpgradeTarget format = UpgradeTarget.T_FORMAT;

		final AfirmaConnector conn = new AfirmaConnector();
		final Properties config = new Properties();
		try (InputStream is = TestUpgrade.class.getResourceAsStream("/platform.properties");) {
			config.load(is);
		}
		conn.init(config);

		final UpgradeResult result = Upgrade.signUpgradeCreate(conn, testFile, format, "minhap.seap.dtic.clavefirma", true); //$NON-NLS-1$

		System.out.println("La firma se actualizo a " + result.getFormat()); //$NON-NLS-1$

		final File saveFile = File.createTempFile("TEST-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
		final java.io.OutputStream os = new java.io.FileOutputStream(saveFile);
		os.write(result.getResult());
		os.flush();
		os.close();
		System.out.println("Temporal para comprobacion manual: " + saveFile.getAbsolutePath()); //$NON-NLS-1$

	}

}
