package es.gob.fire.server.services.statistics;

import es.gob.afirma.core.signers.AOSignConstants;

/** Formatos de firma soportados. */
public enum SignatureFormats {

	/** Formato de firma CAdES. */
	CADES(1, AOSignConstants.SIGN_FORMAT_CADES),
	/** Formato de firma XAdES. */
	XADES(2, AOSignConstants.SIGN_FORMAT_XADES),
	/** Formato de firma PAdES. */
	PADES(3, AOSignConstants.SIGN_FORMAT_PADES),
	/** Formato de firma FacturaE. */
	FACTURAE(4, AOSignConstants.SIGN_FORMAT_FACTURAE),
	/** Formato de firma CAdES-ASiC. */
	CADES_ASIC(5, AOSignConstants.SIGN_FORMAT_CADES_ASIC_S),
	/** Formato de firma XAdES-ASiC. */
	XADES_ASIC(6, AOSignConstants.SIGN_FORMAT_XADES_ASIC_S),
	/** Cualquier otro formato. */
	OTHER(99, "Otro"); //$NON-NLS-1$

	private int id;
	private String name;

	private SignatureFormats(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Recupera el identificador de un formato de firma. Este identificador puede
	 * utilizarse para el registro de estad&iacute;sticas.
	 * @param format Formato de firma.
	 * @return Identificador del formato o {@code null} si no se conoce.
	 */
	public static String getId(final String format) {
		for (final SignatureFormats value : values()) {
			if (value.name.equalsIgnoreCase(format)) {
				return value.toString();
			}
		}
		return OTHER.toString();
	}

	/**
	 * Recupera el nombre del formato.
	 * @return Nombre del formato.
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
}
