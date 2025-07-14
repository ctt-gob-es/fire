package es.gob.fire.crypto.exceptions;

import es.gob.fire.exceptions.FireException;

public class CipherException extends FireException {

	/**
	 * Constant attribute that represents the serial version UID.
	 */
	private static final long serialVersionUID = -6434261310096445951L;

	/**
	 * Constructor method for the class CipherException.java.
	 */
	public CipherException() {
		super();
	}

	/**
	 * Constructor method for the class CipherException.java.
	 * @param errorCode Error code.
	 * @param errorDesc Description for the error.
	 */
	public CipherException(String errorCode, String errorDesc) {
		super(errorCode, errorDesc);
	}

	/**
	 * Constructor method for the class CipherException.java.
	 * @param errorCode Error code.
	 * @param errorDesc Description for the error.
	 * @param exception Exception that causes the error.
	 */
	public CipherException(String errorCode, String errorDesc, Exception exception) {
		super(errorCode, errorDesc, exception);
	}
	
	/**
	 * Constructor method for the class CipherException.java.
	 * @param message Error message.
	 */
	public CipherException(String message) {
		super(message);

	}

}
