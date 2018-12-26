package es.gob.log.consumer.service;

public class SessionException extends Exception {

	/** Serial Id. */
	private static final long serialVersionUID = -3466803454488412040L;

	public SessionException(final String msg) {
		super(msg);
	}

	public SessionException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
