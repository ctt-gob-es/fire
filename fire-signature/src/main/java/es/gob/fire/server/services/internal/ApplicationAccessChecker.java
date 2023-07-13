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

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import es.gob.fire.server.services.CertificateValidationException;
import es.gob.fire.server.services.FIReError;

/**
 * Clase para la comprobaci&oacute;n de los datos de acceso de una aplicaci&oacute;n.
 * @author carlos.gamuci
 *
 */
public class ApplicationAccessChecker {


	/**
	 * Se comprueba que una aplicaci&oacute;n aparezca como habilitada.
	 * @param registeredAppInfo Informac&oacute;n de acceso recuperada de la aplicaci&oacute;n.
	 * @throws IllegalAccessException Cuando la informaci&oacute;n de acceso sea nula o la
	 * aplicaci&oacute;n no est&eacute; habilitada.
	 */
	public static void checkAppEnabled(final ApplicationAccessInfo registeredAppInfo) throws IllegalAccessException {

		if (registeredAppInfo == null) {
			throw new IllegalAccessException("La aplicacion no se encuentra registrada en el sistema"); //$NON-NLS-1$
		}

		if (!registeredAppInfo.isEnabled()) {
			throw new IllegalAccessException("La aplicacion se encuentra deshabilitada"); //$NON-NLS-1$
		}
	}

	/**
	 * Comprueba que un certificado se encuentre entre los permitidos para una aplicaci&oacute;n.
	 * @param certificates Cadena de certificaci&oacute;n del certificado de acceso.
	 * @param registeredAppInfo Informac&oacute;n de acceso recuperada de la aplicaci&oacute;n.
	 * @throws IllegalAccessException Cuando la informaci&oacute;n de acceso sea nula o no se encuentre
	 * el certificado cliente entre los permitidos.
	 * @throws CertificateValidationException Cuando ocurra un error durante la validaci&oacute;n
	 * del certificado.
	 */
	public static void checkValidCertificate(final X509Certificate[] certificates,
			final ApplicationAccessInfo registeredAppInfo) throws IllegalAccessException, CertificateValidationException {

		if (registeredAppInfo == null) {
			throw new IllegalAccessException("La aplicacion no se encuentra registrada en el sistema"); //$NON-NLS-1$
		}

		final DigestInfo[] digestInfos = registeredAppInfo.getCertDigests();
		if (digestInfos == null || digestInfos.length == 0) {
			throw new IllegalAccessException("La aplicacion no tiene habilitados certificados de acceso"); //$NON-NLS-1$
		}

		final Map<String, byte[]> certDigests = new HashMap<>();

		for (final DigestInfo digestInfo : digestInfos) {
			byte[] certDigest = certDigests.get(digestInfo.getAlgorihtm());
			if (certDigest == null) {
				try {
					final MessageDigest md = MessageDigest.getInstance(digestInfo.getAlgorihtm());
					certDigest = md.digest(certificates[0].getEncoded());
					certDigests.put(digestInfo.getAlgorihtm(), certDigest);
				}
				catch (final Exception e) {
					throw new CertificateValidationException(FIReError.INTERNAL_ERROR, "Error al comprobar la validez del certificado", e); //$NON-NLS-1$
				}
			}
			// Si la huella del certificado coincide con la registrada, se da por bueno
			if (Arrays.equals(certDigest, digestInfo.getHash())) {
				return;
			}
		}

		throw new IllegalAccessException("El certificado recibido no es ninguno de los registrados para la aplicacion"); //$NON-NLS-1$
	}


}
