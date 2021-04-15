package es.gob.fire.persistence.dto;

import java.nio.charset.Charset;

/**
 * <p>Data transfer object that contains the results of a log request.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 22/04/2019.
 */
public class LogDataDTO {

	/**
	 * Attribute with the requested log fragment.
	 */
	private byte[] log;

	/**
	 * Attribute with the charset to encode the log fragment.
	 */
	private Charset charset;

	/**
	 * Attribute with a error code.
	 */
	private int errorCode = 500;

	/**
	 * Attribute with a error message.
	 */
	private String errorMessage;

	/**
	 * Sets a log fragment.
	 * @param log Log fragment.
	 */
	public void setLog(final byte[] log) {
		this.log = log;
	}

	/**
	 * Gets the log fragment.
	 * @return Log fragment.
	 */
	public byte[] getLog() {
		return this.log;
	}

	/**
	 * Sets the charset to encode the log fragment.
	 * @param charset Charset to encode the log fragment.
	 */
	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

	/**
	 * Gets the charset to encode the log fragment.
	 * @return Charset to encode the log fragment.
	 */
	public Charset getCharset() {
		return this.charset;
	}

	/**
	 * Sets the error code.
	 * @param errorCode Error code.
	 */
	public void setErrorCode(final int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Gets the error code.
	 * @return Error code.
	 */
	public int getErrorCode() {
		return this.errorCode;
	}

	/**
	 * Sets the error message.
	 * @param errorMessage Error message.
	 */
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the error message.
	 * @return Error message.
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}
}
