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

import org.junit.Test;

import es.gob.fire.upgrade.Upgrade;
import es.gob.fire.upgrade.UpgradeTarget;

/** Prueba de mejora de firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestUpgrade {

	/** Prueba de mejora de firma.
	 * @throws Exception en cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testSignUpgrade() throws Exception {

		//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_enveloped_xml.xml"); //$NON-NLS-1$
		//final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/xades_enveloping_xml.xml"); //$NON-NLS-1$
		final java.io.InputStream input = TestUpgrade.class.getResourceAsStream("/pades.pdf"); //$NON-NLS-1$

        int nBytes = 0;
        final byte[] buffer = new byte[4096];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((nBytes = input.read(buffer)) != -1) {
            baos.write(buffer, 0, nBytes);
        }
        final byte[] testFile =  baos.toByteArray();

        input.close();

		final UpgradeTarget format = UpgradeTarget.PADES_LTV_FORMAT;

		final byte[] result = Upgrade.signUpgradeCreate(testFile, format, "minhap.seap.dtic.dninb"); //$NON-NLS-1$

		final File saveFile = File.createTempFile("TESTPDF-", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
		final java.io.OutputStream os = new java.io.FileOutputStream(saveFile);
		os.write(result);
		os.flush();
		os.close();
		System.out.println("Temporal para comprobacion manual: " + saveFile.getAbsolutePath()); //$NON-NLS-1$

	}

}
