package es.gob.fire.commons.log;

/**
 * <p>Enum to define the errors what you can get to search logs.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 09/05/2019.
 */
public enum LogErrors {

	/**
	 * Attribute that represents the unknown error.
	 */
	UNKNOWN_ERROR(500, "log.search.error.unknown"),
	
	/**
	 * Attribute that represents the no more lines error.
	 */
	NO_MORE_LINES(204, "log.search.error.noMoreLines");

	/**
	 * Attribute that represents the code error.
	 */
	private int code;

	/**
	 * Attribute that represents the message error.
	 */
	private String message;


	LogErrors(final int code, final String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * Gets the code.
	 * @return code
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * Gets the message.
	 * @return message
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Method that gets the LogErrors with a code.
	 * @param code Code of the error.
	 * @return Error info.
	 */
	public static LogErrors parse(final int code) {
		for (final LogErrors err : values()) {
			if (err.getCode() == code) {
				return err;
			}
		}
		return UNKNOWN_ERROR;
	}
}
