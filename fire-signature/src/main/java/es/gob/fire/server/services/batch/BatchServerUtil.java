/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.batch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.TriphaseData;

final class BatchServerUtil {

	private BatchServerUtil() {
		// No instanciable
	}

	static TriphaseData getTriphaseData(final String triphaseDataAsUrlSafeBase64) throws IOException {
		return TriphaseData.parser(
			Base64.decode(unDoUrlSafe(triphaseDataAsUrlSafeBase64))
		);
	}

	static byte[] getSignBatchConfig(final String jsonAsUrlSafeBase64) throws IOException {
		if (jsonAsUrlSafeBase64 == null) {
			throw new IllegalArgumentException(
				"La definicion de lote no puede ser nula" //$NON-NLS-1$
			);
		}
		final byte[] json = Base64.isBase64(jsonAsUrlSafeBase64.getBytes()) ?
			Base64.decode(unDoUrlSafe(jsonAsUrlSafeBase64)) :
				jsonAsUrlSafeBase64.getBytes();

		return json;
	}

	static X509Certificate[] getCertificates(final String certListUrlSafeBase64) throws CertificateException,
	                                                                                    IOException {
		if (certListUrlSafeBase64 == null) {
			throw new IllegalArgumentException(
				"La lista de certificados no puede ser nula" //$NON-NLS-1$
			);
		}

		final String[] certs = unDoUrlSafe(certListUrlSafeBase64).split(";"); //$NON-NLS-1$
		final CertificateFactory cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
		final List<X509Certificate> ret = new ArrayList<>(certs.length);
		for (final String cert : certs) {
			ret.add(
				(X509Certificate) cf.generateCertificate(
					new ByteArrayInputStream(
						Base64.decode(
							unDoUrlSafe(cert)
						)
					)
				)
			);
		}
		return ret.toArray(new X509Certificate[0]);
	}

	private static String unDoUrlSafe(final String b64) {
		return b64.replace("-", "+").replace("_", "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	static TriphaseData getTriphaseDataFromJSON(final byte[] triphaseDataAsUrlSafeBase64) throws IOException {
		return TriphaseDataParser.parseFromJSON(
			Base64.decode(triphaseDataAsUrlSafeBase64, 0, triphaseDataAsUrlSafeBase64.length, true)
		);
	}
}
