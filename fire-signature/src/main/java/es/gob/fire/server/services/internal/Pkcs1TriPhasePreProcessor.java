/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;
import es.gob.afirma.triphase.signer.processors.TriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.TriPhaseUtil;

/**
 * Procesador para simular la realizaci&oacutre;n de firmas PKCS#1 en 3 fases. Ya que este
 * tipo de firmas se realiza por completo en la segunda fase de un proceso trif&aacute;sico,
 * la primera fase consistir&aacute; en establecer los datos recibidos como el valor de
 * prefirma de la operaci&oacute;n y la tercera fase &uacute;nicamente devolver&aacute; el
 * PKCS#1 generado en la segunda fase. Las firmas PKCS#1 no admiten cofirmas ni contrafirmas.
 */
public class Pkcs1TriPhasePreProcessor implements TriPhasePreProcessor {

	/** Prefijo para cada prefirma. */
	private static final String PROPERTY_NAME_PRESIGN = "PRE"; //$NON-NLS-1$

	/** Etiqueta de firma PKCS#1 en el XML de sesi&oacute;n trif&aacute;sica. */
	private static final String PROPERTY_NAME_PKCS1_SIGN = "PK1"; //$NON-NLS-1$

	@Override
	public TriphaseData preProcessPreSign(final byte[] data, final String algorithm, final X509Certificate[] cert, final Properties extraParams)
			throws IOException, AOException {

		// En una firma PKCS1, los datos que nos pasan son directamente la prefirma
		final Map<String, String> signConfig = new ConcurrentHashMap<>();
		signConfig.put(PROPERTY_NAME_PRESIGN, Base64.encode(data));

		final TriphaseData triphaseData = new TriphaseData();
		triphaseData.addSignOperation(new TriSign(signConfig, TriPhaseUtil.getSignatureId(extraParams)));

		return triphaseData;
	}

	@Override
	public TriphaseData preProcessPreCoSign(final byte[] data, final String algorithm, final X509Certificate[] cert, final Properties extraParams)
			throws IOException, AOException {
		throw new UnsupportedOperationException("Las firmas PKCS#1 no admiten multifirmas"); //$NON-NLS-1$
	}

	@Override
	public TriphaseData preProcessPreCounterSign(final byte[] sign, final String algorithm, final X509Certificate[] cert, final Properties extraParams,
			final CounterSignTarget targets) throws IOException, AOException {
		throw new UnsupportedOperationException("Las firmas PKCS#1 no admiten multifirmas"); //$NON-NLS-1$
	}

	@Override
	public byte[] preProcessPostSign(final byte[] data, final String algorithm, final X509Certificate[] cert, final Properties extraParams, final byte[] session)
			throws NoSuchAlgorithmException, IOException, AOException {

		if (session == null) {
			throw new IllegalArgumentException("Los datos de prefirma no pueden ser nulos"); //$NON-NLS-1$
		}

		final TriSign config = TriphaseData.parser(session).getSign(0);
		return Base64.decode(config.getProperty(PROPERTY_NAME_PKCS1_SIGN));
	}

	@Override
	public byte[] preProcessPostSign(final byte[] data, final String algorithm, final X509Certificate[] cert, final Properties extraParams,
			final TriphaseData triphaseData) throws NoSuchAlgorithmException, IOException, AOException {

		if (triphaseData == null) {
			throw new IllegalArgumentException("Los datos de prefirma no pueden ser nulos"); //$NON-NLS-1$
		}

		final TriSign config = triphaseData.getSign(0);
		return Base64.decode(config.getProperty(PROPERTY_NAME_PKCS1_SIGN));
	}

	@Override
	public byte[] preProcessPostCoSign(final byte[] data, final String algorithm, final X509Certificate[] cert, final Properties extraParams, final byte[] session)
			throws NoSuchAlgorithmException, AOException, IOException {
		throw new UnsupportedOperationException("Las firmas PKCS#1 no admiten multifirmas"); //$NON-NLS-1$
	}

	@Override
	public byte[] preProcessPostCoSign(final byte[] data, final String algorithm, final X509Certificate[] cert, final Properties extraParams,
			final TriphaseData sessionData) throws NoSuchAlgorithmException, AOException, IOException {
		throw new UnsupportedOperationException("Las firmas PKCS#1 no admiten multifirmas"); //$NON-NLS-1$
	}

	@Override
	public byte[] preProcessPostCounterSign(final byte[] sign, final String algorithm, final X509Certificate[] cert, final Properties extraParams,
			final byte[] session, final CounterSignTarget targets) throws NoSuchAlgorithmException, AOException, IOException {
		throw new UnsupportedOperationException("Las firmas PKCS#1 no admiten multifirmas"); //$NON-NLS-1$
	}

	@Override
	public byte[] preProcessPostCounterSign(final byte[] sign, final String algorithm, final X509Certificate[] cert, final Properties extraParams,
			final TriphaseData sessionData, final CounterSignTarget targets) throws NoSuchAlgorithmException, AOException, IOException {
		throw new UnsupportedOperationException("Las firmas PKCS#1 no admiten multifirmas"); //$NON-NLS-1$
	}
}
