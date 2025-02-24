/* Copyright (C) 2023 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 13/07/2023
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services.internal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.signature.ConfigManager;

/**
 * DAO para la gesti&oacute;n de aplicaciones dadas de alta en el sistema.
 */
public class ConfigFileApplicationsDAO implements ApplicationsDAO {

	private static final Logger LOGGER = Logger.getLogger(ConfigFileApplicationsDAO.class.getName());

	/** Algoritmo de huella. */
	private static final String SHA_256 = "SHA-256"; //$NON-NLS-1$

	private ApplicationAccessInfo accessInfo = null;

	private boolean loaded = false;

	@Override
	public ApplicationAccessInfo getApplicationAccessInfo(final String appId, final TransactionAuxParams trAux)
			throws IOException {


		if (!this.loaded) {
			this.accessInfo = loadAccessInfo(appId, trAux);
			this.loaded = true;
		}

		return this.accessInfo;
	}

	private static ApplicationAccessInfo loadAccessInfo(final String appId, final TransactionAuxParams trAux) {

		// Comprobamos que haya una aplicacion registrada en el fichero
		final String configuredAppId = ConfigManager.getAppId();
		if (configuredAppId == null || configuredAppId.isEmpty()) {
			LOGGER.warning(trAux.getLogFormatter().f("No hay ID de aplicacion dado de alta en el fichero de configuracion. No se permitira el acceso.")); //$NON-NLS-1$
			return null;
		}

		// Identificamos si la aplicacion es la dada de alta en el sistema
		if (!configuredAppId.equals(appId)) {
			return null;
		}

		final DigestInfo digestInfo = loadDigestInfoFromConfig(trAux);

		return new ApplicationAccessInfo(
				configuredAppId,
				configuredAppId,
				true,
				digestInfo != null ? new DigestInfo[] { digestInfo } : null );
	}

	private static DigestInfo loadDigestInfoFromConfig(final TransactionAuxParams trAux) {

		DigestInfo digestInfo = null;

		final String certB64 = ConfigManager.getCert();
		if (certB64 != null && !certB64.isEmpty()) {

			X509Certificate cert;
			try {
				cert = buildCertificate(certB64);
			}
			catch (final Exception e) {
				LOGGER.log(Level.SEVERE, trAux.getLogFormatter().f("No se pudo construir el certificado configurado", e)); //$NON-NLS-1$
				cert = null;
			}

			if (cert != null) {
				try {
					final byte[] certDigest = MessageDigest.getInstance(SHA_256).digest(cert.getEncoded());
					digestInfo = new DigestInfo(SHA_256, certDigest);
				} catch (final Exception e) {
					LOGGER.log(Level.SEVERE, trAux.getLogFormatter().f("No se pudo calcular la huella del certificado configurado", e)); //$NON-NLS-1$
				}
			}
		}

		return digestInfo;
	}

	private static X509Certificate buildCertificate(final String certB64)
			throws CertificateException, IOException {

		X509Certificate cert;
		final byte[] certEncoded = Base64.getDecoder().decode(certB64);
		try (InputStream bis = new ByteArrayInputStream(certEncoded)) {
			final CertificateFactory fact = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
			cert = (X509Certificate) fact.generateCertificate(bis);
		}
		return cert;
	}

	@Override
	public AplicationOperationConfig getOperationConfig(final String appId,
			final TransactionAuxParams trAux) throws IOException {

		final AplicationOperationConfig config = new AplicationOperationConfig();
		config.setParamsMaxSize(ConfigManager.getParamMaxSize());
		config.setRequestMaxSize(ConfigManager.getRequestMaxSize());
		config.setBatchMaxDocuments(ConfigManager.getBatchMaxDocuments());
		config.setProviders(ConfigManager.getProviders());

		return config;
	}


}
