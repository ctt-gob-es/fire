package es.gob.log.consumer.service;

public class SessionException extends Exception {

	public SessionException(final String msg) {
		super(msg);
	}

	public SessionException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
