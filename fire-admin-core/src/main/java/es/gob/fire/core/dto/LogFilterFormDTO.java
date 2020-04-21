package es.gob.fire.core.dto;

/**
 * <p>Data transfer object that encapsulates the information of a request to get
 * filtered lines of a log file.
 * </p><b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 22/04/2019.
 */
public class LogFilterFormDTO {

	/**
	 * Number of lines requested.
	 */
	private int numLines;

	/**
	 * Minimun date limit to recover.
	 */
	private String startDate;

	 /**
	  * Maximun date limit to recover.
	  */
	private String endDate;

	/**
	 * Minimun level of log to recover.
	 */
	private String level;

	/**
	 * Charset of the log text.
	 */
	private String charsetName;

	/**
	 * Flag to request more data from a previous request.
	 */
	private boolean more;

	/**
	 * Gets the number of lines to recover.
	 * @return Number of lines to recover.
	 */
	public int getNumLines() {
		return this.numLines;
	}

	/**
	 * Sets the number of lines to recover.
	 * @param numLines Number of lines to recover.
	 */
	public void setNumLines(final int numLines) {
		this.numLines = numLines;
	}

	/**
	 * Gets the minimun date limit to recover
	 * @return Minimun date limit to recover.
	 */
	public String getStartDate() {
		return this.startDate;
	}

	/**
	 * Sets the minimun date limit to recover.
	 * @param startDate Minimun date limit to recover.
	 */
	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	/**
	 * Gets the maximun date limit to recover
	 * @return Maximun date limit to recover.
	 */
	public String getEndDate() {
		return this.endDate;
	}

	/**
	 * Sets the maximun date limit to recover
	 * @param endDate Maximun date limit to recover.
	 */
	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	/**
	 * Gets the minimun log level to recover.
	 * @return Minimun log level to recover.
	 */
	public String getLevel() {
		return this.level;
	}

	/**
	 * Sets the minimun log level to recover.
	 * @param level Minimun log level to recover.
	 */
	public void setLevel(final String level) {
		this.level = level;
	}

	/**
	 * Gets the charset with the log file is encoded.
	 * @return Charset name.
	 */
	public String getCharsetName() {
		return this.charsetName;
	}

	/**
	 * Sets the charset with the log file is encoded.
	 * @param charsetName Charset name.
	 */
	public void setCharsetName(final String charsetName) {
		this.charsetName = charsetName;
	}

	/**
	 * Gets if need more data from a previous request.
	 * @return {@code true} if need more data, {@code false} otherwise.
	 */
	public boolean isMore() {
		return this.more;
	}

	/**
	 * Sets if need more data from a previous request.
	 * @param more {@code true} if need more data, {@code false} otherwise.
	 */
	public void setMore(final boolean more) {
		this.more = more;
	}
}
