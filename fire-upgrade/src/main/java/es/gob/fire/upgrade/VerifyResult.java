package es.gob.fire.upgrade;

/**
 * Resultado de una operaci&oacute;n de validaci&oacute;n de firma.
 */
public class VerifyResult {

	private final boolean ok;
	private final String description;

	/**
	 * Construye el resultado de la validaci&oacute;n de la firma.
	 * @param ok {@code true} si la firma es v&aacute;lida, {@code false} en caso contrario.
	 */
	public VerifyResult(final boolean ok) {
		this.ok = ok;
		this.description = null;
	}

	/**
	 * Construye el resultado de la validaci&oacute;n de la firma.
	 * @param ok {@code true} si la firma es v&aacute;lida, {@code false} en caso contrario.
	 * @param description Descripci&oacute;n adicional del resultado.
	 */
	public VerifyResult(final boolean ok, final String description) {
		this.ok = ok;
		this.description = description;
	}

	/**
	 * Indica si la firma es v&aacute;lida.
	 * @return {@code true} si la firma es v&aacute;lida, {@code false} en caso contrario.
	 */
	public boolean isOk() {
		return this.ok;
	}

	/**
	 * Recupera la descripci&oacute;n del resultado. Esto normalmente permite indicar
	 * los errores detectados durante la validaci&oacute;n.
	 * @return
	 */
	public String getDescription() {
		return this.description;
	}
}
