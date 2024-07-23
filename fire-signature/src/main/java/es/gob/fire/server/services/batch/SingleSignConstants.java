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

import java.security.Key;

import es.gob.afirma.core.AOInvalidFormatException;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.triphase.signer.processors.CAdESASiCSTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.CAdESTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.FacturaETriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.PAdESTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.Pkcs1TriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.TriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.XAdESASiCSTriPhasePreProcessor;
import es.gob.afirma.triphase.signer.processors.XAdESTriPhasePreProcessor;

/** Constantes para la definici&oacute;n de una firma independiente.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class SingleSignConstants {

	/** Tipo de operaci&oacute;n de firma. */
	public enum SignSubOperation {

		/** Firma. */
		SIGN("sign"), //$NON-NLS-1$

		/** Cofirma. */
		COSIGN("cosign"), //$NON-NLS-1$

		/** Contrafirma. */
		COUNTERSIGN("countersign"); //$NON-NLS-1$

		private final String name;

		SignSubOperation(final String n) {
			this.name = n;
		}

		@Override
		public String toString() {
			return this.name;
		}

		/** Obtiene el tipo de operaci&oacute;n de firma a partir de su nombre.
		 * @param name Nombre del tipo de operaci&oacute;n de firma.
		 * @return Tipo de operaci&oacute;n de firma. */
		public static SignSubOperation getSubOperation(final String name) {
			if (SIGN.toString().equalsIgnoreCase(name)) {
				return SIGN;
			}
			if (COSIGN.toString().equalsIgnoreCase(name)) {
				return COSIGN;
			}
			if (COUNTERSIGN.toString().equalsIgnoreCase(name)) {
				return COUNTERSIGN;
			}
			throw new IllegalArgumentException(
				"Tipo de operacion (suboperation) de firma no soportado: " + name //$NON-NLS-1$
			);
		}
	}

	/** Formato de firma. */
	public enum SignFormat {

		/** CAdES. */
		CADES(AOSignConstants.SIGN_FORMAT_CADES),

		/** CAdES ASiC. */
		CADES_ASIC(AOSignConstants.SIGN_FORMAT_CADES_ASIC_S),

		/** XAdES. */
		XADES(AOSignConstants.SIGN_FORMAT_XADES),

		/** XAdES ASiC. */
		XADES_ASIC(AOSignConstants.SIGN_FORMAT_XADES_ASIC_S),

		/** PAdES. */
		PADES(AOSignConstants.SIGN_FORMAT_PADES),

		/** FacturaE. */
		FACTURAE(AOSignConstants.SIGN_FORMAT_FACTURAE),

		/** PKCS#1. */
		PKCS1(AOSignConstants.SIGN_FORMAT_PKCS1);

		private final String name;

		SignFormat(final String n) {
			this.name = n;
		}

		@Override
		public String toString() {
			return this.name;
		}

		/** Obtiene el formato de firma a partir de su nombre.
		 * @param name Nombre del formato de firma.
		 * @return Formato firma. */
		public static SignFormat getFormat(final String name) {
			if (name != null) {
				for (final SignFormat format : values()) {
					if (format.toString().equalsIgnoreCase(name.trim())) {
						return format;
					}
				}
			}
			throw new IllegalArgumentException(
				"Tipo de formato de firma no soportado: " + name //$NON-NLS-1$
			);
		}
	}

	/** Algoritmo de firma. */
	public enum DigestAlgorithm {

		/** SHA1. */
		SHA1(AOSignConstants.DIGEST_ALGORITHM_SHA1, "SHA-1"), //$NON-NLS-1$

		/** SHA256. */
		SHA256(AOSignConstants.DIGEST_ALGORITHM_SHA256, "SHA-256"), //$NON-NLS-1$

		/** SHA284. */
		SHA384(AOSignConstants.DIGEST_ALGORITHM_SHA384, "SHA-384"), //$NON-NLS-1$

		/** SHA512. */
		SHA512(AOSignConstants.DIGEST_ALGORITHM_SHA512, "SHA-512"); //$NON-NLS-1$

		private final String name;
		private final String jcaName;

		DigestAlgorithm(final String n, final String jcaName) {
			this.name = n;
			this.jcaName = jcaName;
		}

		/**
		 * Obtiene el nombre del algoritmo..
		 * @return Nombre del algoritmo.
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Obtiene el nombre del algoritmo JCA.
		 * @return Nombre del algoritmo JCA.
		 */
		public String getJcaName() {
			return this.jcaName;
		}

		@Override
		public String toString() {
			return getName();
		}

		/**
		 * Obtiene el algoritmo de huella a partir de su nombre.
		 * @param name Nombre del algoritmo de huella.
		 * @return Algoritmo de huella o de firma RSA/ECDSA.
		 */
		public static DigestAlgorithm getAlgorithm(final String name) {
			// Ademas del algoritmo de huella, comparamos si nos pasan un algoritmo de firma RSA
			// por retrocompatibilidad (v1.9 y anteriores)
			final String cleanName = name != null ? name.replace("-", "") : null; //$NON-NLS-1$ //$NON-NLS-2$
			if (SHA1.getName().equalsIgnoreCase(cleanName)
					|| "SHA1withRSA".equalsIgnoreCase(cleanName) //$NON-NLS-1$
					|| "SHA1withECDSA".equalsIgnoreCase(cleanName)) { //$NON-NLS-1$
				return SHA1;
			}
			if (SHA256.getName().equalsIgnoreCase(cleanName)
					|| "SHA256withRSA".equalsIgnoreCase(cleanName) //$NON-NLS-1$
					|| "SHA256withECDSA".equalsIgnoreCase(cleanName)) { //$NON-NLS-1$) {
				return SHA256;
			}
			if (SHA384.getName().equalsIgnoreCase(cleanName)
					|| "SHA384withRSA".equalsIgnoreCase(cleanName) //$NON-NLS-1$
					|| "SHA284withECDSA".equalsIgnoreCase(cleanName)) { //$NON-NLS-1$) {
				return SHA384;
			}
			if (SHA512.getName().equalsIgnoreCase(cleanName)
					|| "SHA512withRSA".equalsIgnoreCase(cleanName) //$NON-NLS-1$
					|| "SHA512withECDSA".equalsIgnoreCase(cleanName)) { //$NON-NLS-1$) {
				return SHA512;
			}
			throw new IllegalArgumentException(
				"Algoritmo de huella no soportado: " + name //$NON-NLS-1$
			);
		}
	}

	/** Algoritmo de firma. */
	public enum AsyncCipherAlgorithm {

		/** RSA. */
		RSA("RSA", "RSA"), //$NON-NLS-1$ //$NON-NLS-2$

		/** ECDSA. */
		ECDSA("ECDSA", "EC"); //$NON-NLS-1$ //$NON-NLS-2$

		private final String name;
		private final String certKey;

		AsyncCipherAlgorithm(final String name, final String certKey) {
			this.name = name;
			this.certKey = certKey;
		}

		/**
		 * Obtiene el nombre del algoritmo.
		 * @return Nombre del algoritmo.
		 */
		public String getName() {
			return this.name;
		}

		@Override
		public String toString() {
			return getName();
		}

		static AsyncCipherAlgorithm getInstanceFromKey(final Key key)
				throws IllegalArgumentException {
			for (final AsyncCipherAlgorithm algo : values()) {
				if (algo.certKey.equals(key.getAlgorithm())) {
					return algo;
				}
			}
			throw new IllegalArgumentException("Tipo de clave no soportada: " + key.getAlgorithm()); //$NON-NLS-1$
		}
	}

	static TriPhasePreProcessor getTriPhasePreProcessor(final SingleSign sSign) throws AOInvalidFormatException {
		if (sSign == null) {
			throw new IllegalArgumentException("La firma no puede ser nula"); //$NON-NLS-1$
		}
		switch(sSign.getFormat()) {
			case PADES:
				return new PAdESTriPhasePreProcessor();
			case CADES:
				return new CAdESTriPhasePreProcessor();
			case CADES_ASIC:
				return new CAdESASiCSTriPhasePreProcessor();
			case XADES:
				return new XAdESTriPhasePreProcessor();
			case XADES_ASIC:
				return new XAdESASiCSTriPhasePreProcessor();
			case FACTURAE:
				return new FacturaETriPhasePreProcessor();
			case PKCS1:
				return new Pkcs1TriPhasePreProcessor();
			default:
				throw new AOInvalidFormatException("Formato de firma no soportado: " + sSign.getFormat()); //$NON-NLS-1$
		}
	}
}
