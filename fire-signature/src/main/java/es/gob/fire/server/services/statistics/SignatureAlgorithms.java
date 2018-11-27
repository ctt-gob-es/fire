package es.gob.fire.server.services.statistics;

import es.gob.afirma.core.signers.AOSignConstants;

/** Algoritmos de firma soportados. */
public enum SignatureAlgorithms {

	/** Algoritmo de firma SHA1withRSA. */
	SHA1RSA(1, AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA),
	/** Algoritmo de firma SHA256withRSA. */
	SHA256RSA(2, AOSignConstants.SIGN_ALGORITHM_SHA256WITHRSA),
	/** Algoritmo de firma SH384withRSA. */
	SHA384RSA(3, AOSignConstants.SIGN_ALGORITHM_SHA384WITHRSA),
	/** Algoritmo de firma SHA512withRSA. */
	SHA512RSA(4, AOSignConstants.SIGN_ALGORITHM_SHA512WITHRSA),
	/** Cualquier otro algoritmo. */
	OTHER(99, ""); //$NON-NLS-1$

	private int id;
	private String algorithm;

	private SignatureAlgorithms(final int id, final String algorithm) {
		this.id = id;
		this.algorithm = algorithm;
	}

	/**
	 * Recupera el identificador de un algoritmo de firma. Este identificador puede
	 * utilizarse para el registro de estad&iacute;sticas.
	 * @param algorithm Algoritmo de firma.
	 * @return Identificador del algoritmo o {@code null} si no se conoce.
	 */
	public static String getId(final String algorithm) {
		for (final SignatureAlgorithms value : values()) {
			if (value.algorithm.equalsIgnoreCase(algorithm)) {
				return value.toString();
			}
		}
		return OTHER.toString();
	}

	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
}
