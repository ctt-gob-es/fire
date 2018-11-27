package es.gob.fire.server.services.statistics;

import es.gob.fire.server.services.internal.SignConstants;

/** Formatos mejorados de firma soportados. */
public enum ImprovedSignatureFormats {

	ES_A(1,SignConstants.SIGN_LONGFORMATS_ES_A),
	/** 	Para la actualización del formato CAdES a CAdES-T.<br>
			Para la actualización del formato XAdES a XAdES-T.<br>
			A partir de la versión 6.2 de la Plataforma @firma, debe usarse “T-LEVEL”.
	*/
	ES_T(2,SignConstants.SIGN_LONGFORMATS_ES_T),
	/** 	Para la actualización del formato CAdES a CAdES-C.<br>
			Para la actualización del formato XAdES a XAdES-C.
	 */
	ES_C(3, SignConstants.SIGN_LONGFORMATS_ES_C),
	/** 	Para la actualización del formato CAdES a CAdES-X.<br>
			Para la actualización del formato XAdES a XAdES-X.<br>
	 */
	ES_X(4, SignConstants.SIGN_LONGFORMATS_ES_X),
	/** 	Para la actualización del formato CAdES a CAdES-X1.<br>
			Para la actualización del formato XAdES a XAdES-X1.
	 */
	ES_X_1(5, SignConstants.SIGN_LONGFORMATS_ES_X_1),
	/** 	Para la actualización del formato CAdES a CAdES-X2.<br>
			Para la actualización del formato XAdES a XAdES-X2.
	 */
	ES_X_2(6, SignConstants.SIGN_LONGFORMATS_ES_X_2),
	/** 	Para la actualización del formato CAdES a CAdES-XL1.<br>
			Para la actualización del formato XAdES a XAdES-XL1.
	 */
	ES_X_L1(7, SignConstants.SIGN_LONGFORMATS_ES_X_L1),
	/** 	Para la actualización del formato CAdES a CAdES-XL2.<br>
			Para la actualización del formato XAdES a XAdES-XL2.
	 */
	ES_X_L2 (8, SignConstants.SIGN_LONGFORMATS_ES_X_L2 ),
	/** 	Para la actualización del formato PAdES a PAdES-LTV. */
	ES_LTV (9, SignConstants.SIGN_LONGFORMATS_ES_LTV ),
	/** 	Para la actualización del formato CAdES a T-LEVEL.<br>
			Para la actualización del formato XAdES a T-LEVEL.<br>
			Para la actualización del formato PAdES a T-LEVEL.<br>
			Sólo disponible a partir de la versión 6.2 de la Plataforma @firma.
	 */
	T_LEVEL(10, SignConstants.SIGN_LONGFORMATS_T_LEVEL),
	/**		Para la actualización del formato CAdES a LT-LEVEL.<br>
			Para la actualización del formato XAdES a LT-LEVEL.<br>
			Para la actualización del formato PAdES a LT-LEVEL.<br>
			Sólo disponible a partir de la versión 6.2 de la Plataforma @firma.
	 */
	LT_LEVEL(11, SignConstants.SIGN_LONGFORMATS_LT_LEVEL),
	/**		Para la actualización del formato CAdES a LTA-LEVEL.<br>
			Para la actualización del formato XAdES a LTA-LEVEL.<br>
			Para la actualización del formato PAdES a LTA-LEVEL.<br>
			Sólo disponible a partir de la versión 6.2 de la Plataforma @firma.
	*/
	LTA_LEVEL(12, SignConstants.SIGN_LONGFORMATS_LTA_LEVEL),

	/** Cualquier otro formato. */
	OTHER(99, SignConstants.SIGN_LONGFORMATS_OTROS);

	private int id;
	private String format;

	private ImprovedSignatureFormats(final int id, final String format) {
		this.id = id;
		this.format = format;
	}

	/**
	 * Recupera el identificador de un formato de firma. Este identificador puede
	 * utilizarse para el registro de estad&iacute;sticas.
	 * @param format Formato de firma.
	 * @return Identificador del formato o {@code null} si no se conoce.
	 */
	public static String getId(final String format) {
		for (final ImprovedSignatureFormats value : values()) {
			if (value.format.equalsIgnoreCase(format)) {
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
