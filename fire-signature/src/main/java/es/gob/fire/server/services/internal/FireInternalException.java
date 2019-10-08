package es.gob.fire.server.services.internal;

/**
 * Excepci&oacute;n que identifica un error gen&eacute;rico de FIRe.
 */
public class FireInternalException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -2698722128712649014L;

	/**
	 * Construye la excepci&oacute;n de error interno.
	 */
	public FireInternalException() {
		super();
	}

	/**
	 * Construye la excepci&oacute;n de error interno.
	 * @param msg Descripci&oacute;n del error.
	 */
	public FireInternalException(final String msg) {
		super(msg);
	}

	/**
	 * Construye la excepci&oacute;n de error interno.
	 * @param cause Motivo del error.
	 */
	public FireInternalException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Construye la excepci&oacute;n de error interno.
	 * @param msg Descripci&oacute;n del error.
	 * @param cause Motivo del error.
	 */
	public FireInternalException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
