package es.gob.fire.upgrade;

/** Resultado de una operaci&oacute;n de actualizaci&oacute;n. Compuesto por la firma resultante y
 * el identificador del formato longevo al que se ha actualizado. */
public final class UpgradeResult {

	private final byte[] result;

	private final String format;

	/** Construye el resultado de la actualizaci&oacute;n.
	 * @param result Firma resultante.
	 * @param format Identificador del formato longevo. */
	public UpgradeResult(final byte[] result, final String format) {
		this.result = result;
		this.format = format;
	}

	/** Recupera la firma resultante de la actualizaci&oacute;n.
	 * @return Firma electr&oacute;nica. */
	public byte[] getResult() {
		return this.result;
	}

	/** Recupera el identificador del formato al que se ha actualizado
	 * la firma.
	 * @return Idenficador del formato longevo. */
	public String getFormat() {
		return this.format;
	}
}
