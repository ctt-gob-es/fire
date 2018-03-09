/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.server.connector.FIReConnector;
import es.gob.fire.server.connector.GenerateCertificateResult;
import es.gob.fire.server.connector.LoadResult;
import es.gob.fire.server.connector.clavefirma.ClaveFirmaConnector;

/** Pruebas simples del servicio de custodia de Cl@ve Firma.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestPrincipal {

	// Usuario dado de alta con certificados
	private static final String USER = "52020202K"; //$NON-NLS-1$
	// Usuario que no tiene certificados
	//private static final String USER = "52044296B"; //$NON-NLS-1$
	// Usuario que no existe
	//private static final String USER = "11830960J"; //$NON-NLS-1$

	/** Prueba de obtenci&oacute;n de certificado.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testGetCertificate() throws Exception {
		final X509Certificate[] certs;
		try {
			certs = new ClaveFirmaConnector().getCertificates(USER);
		}
		catch(final Exception e) {
			e.printStackTrace();
//			System.out.println(e.getCause().getMessage());
//			System.out.println(e.getCause().getCause().getCause().getClass().getName());
//			System.out.println(e.getCause().getCause().getCause().getMessage());
			throw e;
		}
		System.out.println("Encontrados " + certs.length + " certificados para el usuario " + USER + ":");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		for(final X509Certificate cert : certs) {
			System.out.println("  " + AOUtil.getCN(cert)); //$NON-NLS-1$
		}
	}

	/** Prueba de carga de datos a firmar.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testLoadData() throws Exception {

		final FIReConnector nbh = new ClaveFirmaConnector();

		final Properties config = new Properties();
		config.put("redirectOkUrl", "http://www.google.com"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("redirectErrorUrl", "http://www.ibm.com"); //$NON-NLS-1$ //$NON-NLS-2$
		nbh.init(config);

		final X509Certificate cert = nbh.getCertificates(USER)[0];

		System.out.println();
		System.out.println(AOUtil.getCN(cert));
		System.out.println();

		final TriphaseData td = FIReTriHelper.getPreSign(
			"sign", //$NON-NLS-1$
			"XAdES", //$NON-NLS-1$
			"SHA1withRSA", //$NON-NLS-1$
			null,
			cert,
			"Hola mundo!".getBytes() //$NON-NLS-1$
		);

		final LoadResult res = nbh.loadDataToSign(
			USER,
			"SHA512withRSA", //$NON-NLS-1$
			FIReTriHelper.fromTriPhaseDataAfirmaToFire(td),
			cert
		);

		System.out.println(res);

	}

	/** Prueba de transacci&oacute;n completa de firma.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testSignData() throws Exception {

		final FIReConnector nbh = new ClaveFirmaConnector();

		final Properties config = new Properties();
		config.put("redirectOkUrl", "http://www.google.com"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("redirectErrorUrl", "http://www.ibm.com"); //$NON-NLS-1$ //$NON-NLS-2$
		nbh.init(config);

		final X509Certificate cert = nbh.getCertificates(USER)[0];

		System.out.println();
		System.out.println(AOUtil.getCN(cert));
		System.out.println();

		final Properties extraParams = new Properties();
		extraParams.setProperty("mode", "implicit"); //$NON-NLS-1$ //$NON-NLS-2$

		final TriphaseData td = FIReTriHelper.getPreSign(
			"sign", //$NON-NLS-1$
			"CAdES", //$NON-NLS-1$
			"SHA1withRSA", //$NON-NLS-1$
			extraParams,
			cert,
			"Hola mundo!".getBytes() //$NON-NLS-1$
		);

		System.out.println();
		System.out.println(td);
		System.out.println();

		final LoadResult res = nbh.loadDataToSign(
			USER,
			"SHA1withRSA", //$NON-NLS-1$
			FIReTriHelper.fromTriPhaseDataAfirmaToFire(td),
			cert
		);

		System.out.println(res);
		System.out.println();

		// HAY QUE PARAR AQUI LA EJECUCION Y ENTRAR MANUALMENTE EN LA URL QUE SE HA MOSTRADO
		// COMO REDIRECCION EN EL PRINTLN
		// La contrasena para pruebas oscilara entre "Entra001" y "Entra002" (hay que renovarla regularmente)
		// El codigo OTP de prueba es "111111"

		final Map<String, byte[]> ret;
		try {
			ret = nbh.sign(res.getTransactionId());
		}
		catch (final Throwable t) {
			t.printStackTrace();
			return;
		}

		// Insertamos los PKCS#1 en la sesion trifasica
		final Set<String> keys = ret.keySet();
		for (final String key : keys) {
			System.out.println("Firma " + key + " = " + AOUtil.hexify(ret.get(key), false)); //$NON-NLS-1$ //$NON-NLS-2$
			FIReTriHelper.addPkcs1ToTriSign(ret.get(key), key, td);
		}

		System.out.println("TripaseData:\n" + td.toString()); //$NON-NLS-1$

		// Ya con el TriphaseData relleno, hacemos la postfirma
		final byte[] signature = FIReTriHelper.getPostSign(
				"sign", //$NON-NLS-1$
				"CAdES", //$NON-NLS-1$
				"SHA1withRSA", //$NON-NLS-1$
				extraParams,
				cert,
				"Hola mundo!".getBytes(), //$NON-NLS-1$
				td
			);

		// El resultado obtenido es la firma completa
		final FileOutputStream fos = new FileOutputStream("C:/Users/carlos/Desktop/firma.xsig"); //$NON-NLS-1$
		fos.write(signature);
		fos.close();

		System.out.println("OK"); //$NON-NLS-1$
	}

	/** Prueba de generaci&oacute;n de certificados.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testGenerateNewCertificate() throws Exception {

		final String subjectId = "52044291Y"; //$NON-NLS-1$

		final FIReConnector nbh = new ClaveFirmaConnector();

		final Properties config = new Properties();
		config.put("redirectOkUrl", "http://www.google.com"); //$NON-NLS-1$ //$NON-NLS-2$
		config.put("redirectErrorUrl", "http://www.ibm.com"); //$NON-NLS-1$ //$NON-NLS-2$
		nbh.init(config);

		final GenerateCertificateResult intermediateResult = nbh.generateCertificate(subjectId);

		final String idTransaction = intermediateResult.getTransactionId();
		final String confirmUrl = intermediateResult.getRedirectUrl();

		// Somos redirigidos a la pagina de confirmacion
		System.out.println("Id transaccion: " + idTransaction); //$NON-NLS-1$
		System.out.println("URL redireccion: " + confirmUrl); //$NON-NLS-1$
		System.out.println(" ============== "); //$NON-NLS-1$

		// HAY QUE PARAR AQUI LA EJECUCION Y ENTRAR MANUALMENTE EN LA URL QUE SE HA MOSTRADO
		// COMO REDIRECCION EN EL PRINTLN


		final byte[] certEncoded = nbh.recoverCertificate(idTransaction);

		final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509") //$NON-NLS-1$
				.generateCertificate(new ByteArrayInputStream(certEncoded));

		System.out.println("CN del certificado generado: " + AOUtil.getCN(cert)); //$NON-NLS-1$
	}
}
