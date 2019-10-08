package es.gob.fire.server.services.internal;

/**
 * Excepci&oacute;n utilizada cuando se detecta que una firma es inv&aacute;lida
 * durante un proceso de validaci&oacute;n o actualizaci&oacute;n de firma.
 */
public class InvalidSignatureException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -7171354776470760070L;

	/**
	 * Construye la excepci&oacute;n de firma inv&aacute;lida.
	 */
	public InvalidSignatureException() {
		super();
	}

	/**
	 * Construye la excepci&oacute;n de firma inv&aacute;lida.
	 * @param msg Descripci&oacute;n del error.
	 */
	public InvalidSignatureException(final String msg) {
		super(msg);
	}

	/**
	 * Construye la excepci&oacute;n de firma inv&aacute;lida.
	 * @param cause Motivo del error.
	 */
	public InvalidSignatureException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Construye la excepci&oacute;n de firma inv&aacute;lida.
	 * @param msg Descripci&oacute;n del error.
	 * @param cause Motivo del error.
	 */
	public InvalidSignatureException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
