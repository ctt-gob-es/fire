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

/**
 * Informaci&oacute;n de una huella digital.
 */
public class DigestInfo {

	private static final String SHA1 = "SHA-1"; //$NON-NLS-1$
	private static final int SHA1_LENGTH = 1;
	private static final String SHA256 = "SHA256"; //$NON-NLS-1$
	private static final int SHA256_LENGTH = 2;
	private static final String SHA512 = "SHA512"; //$NON-NLS-1$
	private static final int SHA512_LENGTH = 3;

	/** Algoritmo con el que se gener&oacute; la huella. */
	private final String algorihtm;

	/** Huella digital. */
	private final byte[] hash;

	/**
	 * Construye la informaci&oacute;n de la huella.
	 * @param algorithm Algoritmo con el que se gener&oacute; la huella.
	 * @param hash Huella digital.
	 */
	public DigestInfo(final String algorithm, final byte[] hash) {
		this.algorihtm = algorithm;
		this.hash = hash;
	}

	/**
	 * Recupera el algoritmo con el que se gener&oacute; la huella.
	 * @return Algoritmo.
	 */
	public String getAlgorihtm() {
		return this.algorihtm;
	}

	/**
	 * Recupera la huella digital.
	 * @return Huella digital.
	 */
	public byte[] getHash() {
		return this.hash;
	}

	/**
	 * Create un objeto de informaci&oacute;n de huella con la huella indicada siempre.
	 * @param hash Hash para el que crear la informaci&oacute;n huella.
	 * @return Informaci&oacute;n de huella.
	 * @throws IllegalArgumentException Cuando no se reconozca el algoritmo utilizado para la
	 * creaci&oacute;n de la huella.
	 */
	public static DigestInfo create(final byte[] hash) throws IllegalArgumentException {

		if (hash == null || hash.length == 0) {
			throw new IllegalArgumentException("Se ha proporcionado un hash nulo o vacio"); //$NON-NLS-1$
		}

		switch (hash.length) {
		case SHA1_LENGTH:
			return new DigestInfo(SHA1, hash);
		case SHA256_LENGTH:
			return new DigestInfo(SHA256, hash);
		case SHA512_LENGTH:
			return new DigestInfo(SHA512, hash);
		default:
			throw new IllegalArgumentException("La huella no se genero con un algoritmo soportado. Longitud: " + hash.length); //$NON-NLS-1$
		}

	}
}
