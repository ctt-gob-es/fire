package es.gob.fire.server.services.statistics;

/** Formatos mejorados de firma soportados. */
public enum ImprovedSignatureFormats {

	/**
	 * Para la actualizaci&oacute;n del formato CAdES a CAdES-A.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a XAdES-A.<br>
	 */
	ES_A(1, "ES-A"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a CAdES-T.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a XAdES-T.<br>
	 * A partir de la versi&oacute;n 6.2 de la Plataforma @firma, debe usarse "T-LEVEL".
	 */
	ES_T(2, "ES-T"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a CAdES-C.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a XAdES-C.
	 */
	ES_C(3, "ES-C"), //$NON-NLS-1$
	/** Para la actualizaci&oacute;n del formato CAdES a CAdES-X.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a XAdES-X.<br>
	 */
	ES_X(4, "ES-X"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a CAdES-X1.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a XAdES-X1.
	 */
	ES_X_1(5, "ES-X-1"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a CAdES-X2.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a XAdES-X2.
	 */
	ES_X_2(6, "ES-X-2"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a CAdES-XL1.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a XAdES-XL1.
	 */
	ES_X_L1(7, "ES-X-L-1"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a CAdES-XL2.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a XAdES-XL2.
	 */
	ES_X_L2 (8, "ES-X-L-2"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato PAdES a PAdES-LTV.
	 */
	ES_LTV (9, "ES-LTV"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a T-LEVEL.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a T-LEVEL.<br>
	 * Para la actualizaci&oacute;n del formato PAdES a T-LEVEL.<br>
	 * S&oacute;lo disponible a partir de la versi&oacute;n 6.2 de la Plataforma @firma.
	 */
	T_LEVEL(10, "T-LEVEL"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a LT-LEVEL.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a LT-LEVEL.<br>
	 * Para la actualizaci&oacute;n del formato PAdES a LT-LEVEL.<br>
	 * S&oacute;lo disponible a partir de la versi&oacute;n 6.2 de la Plataforma @firma.
	 */
	LT_LEVEL(11, "LT-LEVEL"), //$NON-NLS-1$
	/**
	 * Para la actualizaci&oacute;n del formato CAdES a LTA-LEVEL.<br>
	 * Para la actualizaci&oacute;n del formato XAdES a LTA-LEVEL.<br>
	 * Para la actualizaci&oacute;n del formato PAdES a LTA-LEVEL.<br>
	 * S&oacute;lo disponible a partir de la versi&oacute;n 6.2 de la Plataforma @firma.
	 */
	LTA_LEVEL(12, "LTA-LEVEL"), //$NON-NLS-1$

	/** Cualquier otro formato. */
	OTHER(99, "OTROS", true); //$NON-NLS-1$

	private int id;
	private String format;
	private boolean undefined;

	private ImprovedSignatureFormats(final int id, final String format) {
		this.id = id;
		this.format = format;
		this.undefined = false;
	}

	private ImprovedSignatureFormats(final int id, final String format, final boolean undefined) {
		this.id = id;
		this.format = format;
		this.undefined = undefined;
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

	public String getFormat() {
		return this.format;
	}

	public boolean isUndefined() {
		return this.undefined;
	}

	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
}
