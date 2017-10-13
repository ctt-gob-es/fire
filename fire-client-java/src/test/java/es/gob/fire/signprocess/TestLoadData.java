/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.signprocess;

import java.security.cert.X509Certificate;
import java.util.Properties;

import org.junit.Test;

import es.gob.clavefirma.client.certificatelist.HttpCertificateList;
import es.gob.clavefirma.client.signprocess.HttpLoadProcess;
import es.gob.clavefirma.client.signprocess.HttpSignProcessConstants.SignatureAlgorithm;
import es.gob.clavefirma.client.signprocess.HttpSignProcessConstants.SignatureFormat;
import es.gob.clavefirma.client.signprocess.HttpSignProcessConstants.SignatureOperation;
import es.gob.clavefirma.client.signprocess.LoadResult;

/** Pruebas de carga de datos.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestLoadData {

	private static final String APP_ID = "1"; //$NON-NLS-1$
	private static final String SUBJECT = "52020201C"; //$NON-NLS-1$

	/** prueba de carga de datos.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testClientLoadData() throws Exception {

		final X509Certificate cert = HttpCertificateList.getList(APP_ID, SUBJECT).get(0);
		System.out.println("Certificado : " + cert.getSubjectDN()); //$NON-NLS-1$

		final Properties config = new Properties();
		config.put("redirectOkUrl", "http://www.google.com"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("redirectErrorUrl", "http://www.ibm.com"); //$NON-NLS-1$ //$NON-NLS-2$

		final LoadResult res = HttpLoadProcess.loadData(
			APP_ID,
			SUBJECT,
			SignatureOperation.SIGN,
			SignatureFormat.XADES,
			SignatureAlgorithm.SHA384WITHRSA,
			null, // ExtraParams
			cert,
			"Hola mundo!".getBytes(), //$NON-NLS-1$
			config // Configuracion del servicio servidor
		);

		System.out.println(res);
	}

}
