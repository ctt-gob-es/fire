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

import org.junit.Ignore;
import org.junit.Test;

import es.gob.fire.upgrade.Verify;
import es.gob.fire.upgrade.VerifyResponse;

/** Pruebas de verificaci&oacute;n de certificados.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestVerify {

	private static final String AFIRMA_APPNAME = "minhap.seap.dtic.dninb"; //$NON-NLS-1$

	/** Prueba la validaci&oacute;n de un certificado.
	 * @throws Exception en cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	@Ignore
	public void testVerifyCert() throws Exception {
		try (
			final java.io.InputStream input = TestVerify.class.getResourceAsStream("/redsara.es.der"); //$NON-NLS-1$
		) {
	        int nBytes = 0;
	        final byte[] buffer = new byte[4096];
	        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        while ((nBytes = input.read(buffer)) != -1) {
	            baos.write(buffer, 0, nBytes);
	        }
	        final byte[] testFile =  baos.toByteArray();

	        input.close();

	        final VerifyResponse vr = Verify.vertifyCertificate(testFile, AFIRMA_APPNAME);

	        System.out.println(vr.isOk());
	        System.out.println(vr.getDescription());
		}
	}

}
