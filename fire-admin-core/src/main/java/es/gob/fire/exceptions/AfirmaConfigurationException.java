package es.gob.fire.exceptions;

public class AfirmaConfigurationException extends FireException {

	/**
	 * Constant attribute that represents the serial version UID.
	 */
	private static final long serialVersionUID = 9036695879090248701L;

	/**
	 * Constructor method for the class CipherException.java.
	 */
	public AfirmaConfigurationException() {
		super();
	}

	/**
	 * Constructor method for the class CipherException.java.
	 * @param message Error message.
	 */
	public AfirmaConfigurationException(final String message) {
		super(message);
	}

	/**
	 * Constructor method for the class CipherException.java.
	 * @param errorCode Error code.
	 * @param message Description for the error.
	 */
	public AfirmaConfigurationException(final String errorCode, final String message) {
		super(errorCode, message);
	}

	/**
	 * Constructor method for the class CipherException.java.
	 * @param errorCode Error code.
	 * @param message Description for the error.
	 * @param exception Exception that causes the error.
	 */
	public AfirmaConfigurationException(final String errorCode, final String message, final Exception exception) {
		super(errorCode, message, exception);
	}
}
