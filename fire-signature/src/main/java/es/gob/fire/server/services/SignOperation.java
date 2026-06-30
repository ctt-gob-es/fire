package es.gob.fire.server.services;

/**
 * Operaci&oacute;n de firma.
 */
public enum SignOperation {

	/** Firma. */
	SIGN("sign"), //$NON-NLS-1$
	/** Cofirma. */
	COSIGN("cosign"), //$NON-NLS-1$
	/** Contrafirma. */
	COUNTERSIGN("countersign"); //$NON-NLS-1$

	private final String op;

	SignOperation(final String op) {
		this.op = op;
	}

	/**
	 * Devuelve la operaci&oacute;n con el nombre se&ntilde;alado.
	 * @param opName Nombre de la operaci&oacute;n.
	 * @return Operaci&oacute;n de firma.
	 */
	public static SignOperation parse(final String opName) {
		if (opName != null) {
			for (final SignOperation signOperation : values()) {
				if (signOperation.toString().equalsIgnoreCase(opName)) {
					return signOperation;
				}
			}
		}
		throw new IllegalArgumentException(
				"Tipo de operacion (suboperation) de firma no soportado: " + opName //$NON-NLS-1$
			);
	}

	/**
	 * Obtiene el nombre de la operaci&oacute;n de firma.
	 */
	@Override
	public String toString() {
		return this.op;
	}
}
