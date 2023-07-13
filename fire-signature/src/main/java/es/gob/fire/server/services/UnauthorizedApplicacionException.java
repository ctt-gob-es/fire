package es.gob.fire.server.services;

public class UnauthorizedApplicacionException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -1823985935818318590L;

	public UnauthorizedApplicacionException(final String msg) {
		super(msg);
	}

	public UnauthorizedApplicacionException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
