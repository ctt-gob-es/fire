package es.gob.fire.upgrade;

/**
 * Resultado de una operaci&oacute;n de validaci&oacute;n de firma.
 */
public class VerifyResult {

	private final boolean ok;

	/**
	 * Construye el resultado de la validaci&oacute;n de la firma.
	 * @param ok {@code true} si la firma es v&aacute;lida, {@code false} en caso contrario.
	 */
	public VerifyResult(final boolean ok) {
		this.ok = ok;
	}

	/**
	 * Indica si la firma es v&aacute;lida.
	 * @return {@code true} si la firma es v&aacute;lida, {@code false} en caso contrario.
	 */
	public boolean isOk() {
		return this.ok;
	}
}
