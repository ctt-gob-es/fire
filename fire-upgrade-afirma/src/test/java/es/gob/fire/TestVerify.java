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
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assume;

import es.gob.fire.upgrade.VerifyResult;
import es.gob.fire.upgrade.afirma.AfirmaConnector;
import es.gob.fire.upgrade.afirma.Verify;
import es.gob.fire.upgrade.afirma.VerifyAfirmaCertificateResponse;

/** Prueba de mejora de firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestVerify {

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
	@Ignore
	public void testSignUpgrade() throws Exception {

		final VerifyResult result = Verify.verifySignature(this.conn, this.testFile, this.config.getProperty(PROP_APPID));

		System.out.println("Firma correcta: " + result.isOk()); //$NON-NLS-1$
	}

	/** Verifica el estado de un certificado contra el servicio local con UsernameToken. */
	@Test
	public void testVerifyCertificateWithUsernameToken() throws Exception {

		final String username = getTestCredential("afirma.test.username", "AFIRMA_TEST_USERNAME");
		final String password = getTestCredential("afirma.test.password", "AFIRMA_TEST_PASSWORD");
		Assume.assumeTrue("Defina afirma.test.username/afirma.test.password para ejecutar la prueba", //$NON-NLS-1$
				username != null && password != null);

		this.config.setProperty("webservices.endpoint", "http://localhost:8081/afirmaws/services/"); //$NON-NLS-1$
		this.config.setProperty("webservices.service.verifyCertificate", "DSSAfirmaVerifyCertificate"); //$NON-NLS-1$
		this.config.setProperty("webservices.authorization.method", "UsernameToken"); //$NON-NLS-1$
		this.config.setProperty("webservices.authorization.user.name", username); //$NON-NLS-1$
		this.config.setProperty("webservices.authorization.user.password", password); //$NON-NLS-1$
		this.config.remove("webservices.authorization.ks.path"); //$NON-NLS-1$
		this.config.remove("webservices.authorization.ks.type"); //$NON-NLS-1$
		this.config.remove("webservices.authorization.ks.password"); //$NON-NLS-1$
		this.config.remove("webservices.authorization.ks.cert.alias"); //$NON-NLS-1$
		this.config.remove("webservices.authorization.ks.cert.password"); //$NON-NLS-1$
		this.config.remove("webservices.authentication.ts.path"); //$NON-NLS-1$
		this.config.remove("webservices.authentication.ts.password"); //$NON-NLS-1$
		this.config.remove("webservices.authentication.ts.type"); //$NON-NLS-1$
		this.config.remove("webservices.authentication.cert.alias"); //$NON-NLS-1$

		this.conn.init(this.config);

		final X509Certificate certificate;
		try (InputStream is = TestVerify.class.getResourceAsStream("/redsara.es.der")) { //$NON-NLS-1$
			Assume.assumeTrue("No se encuentra el certificado de prueba", is != null); //$NON-NLS-1$
			certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is); //$NON-NLS-1$
		}

		final VerifyAfirmaCertificateResponse result = Verify.verifyCertificate(
				this.conn, certificate, this.config.getProperty(PROP_APPID));

		org.junit.Assert.assertNotNull("La respuesta de @Firma no puede ser nula", result); //$NON-NLS-1$
	}

	private static String getTestCredential(final String propertyName, final String environmentName) {
		final String propertyValue = System.getProperty(propertyName);
		if (propertyValue != null && !propertyValue.trim().isEmpty()) {
			return propertyValue.trim();
		}
		final String environmentValue = System.getenv(environmentName);
		return environmentValue == null || environmentValue.trim().isEmpty() ? null : environmentValue.trim();
	}

}
