/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <b>File:</b><p>es.gob.fire.core.dto.LogLastLinesFormDTO.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>8 oct. 2018.</p>
 * @author Gobierno de España.
 * @version 1.1, 17/10/2018.
 */
package es.gob.fire.core.dto;

/**
 * <p>Data transfer object that encapsulates the information of a request to get the
 * last lines of a log file.
 * </p><b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 17/04/2019.
 */
public class LogLastLinesFormDTO {

	/**
	 * Number of lines requested.
	 */
	private int numLines;

	/**
	 * Log file's name.
	 */
	private String logFilename;

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
	 * Gets the log filename.
	 * @return Log filename.
	 */
	public String getLogFilename() {
		return this.logFilename;
	}

	/**
	 * Sets the log filename.
	 * @param logFilename Log filename.
	 */
	public void setLogFilename(final String logFilename) {
		this.logFilename = logFilename;
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
