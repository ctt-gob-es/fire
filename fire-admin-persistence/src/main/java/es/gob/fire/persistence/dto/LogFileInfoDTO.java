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
 * <b>File:</b><p>es.gob.monitoriza.persistence.configuration.dto.StatusDTO.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for monitoring services of @firma suite systems</p>
 * <b>Date:</b><p>8 oct. 2018.</p>
 * @author Gobierno de España.
 * @version 1.1, 17/10/2018.
 */
package es.gob.fire.persistence.dto;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * <p>
 * Data transfer object that encapsulates the information of a log file.
 * </p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 01/04/2019.
 */
public class LogFileInfoDTO {

	@JsonView(View.class)
	private String filename;

	/**
	 * Optional: Log file's character encoding. If no setterDo not include if there is no error.
	 */
	@JsonView(View.class)
	private String charset = "utf-8";

	@JsonView(View.class)
	private boolean date;

	@JsonView(View.class)
	private boolean time;

	@JsonView(View.class)
	private String dateTimeFormat;

	@JsonView(View.class)
	private String[] levels;

	/**
	 * Optional: If an error occurs during the running of the server-side processing script, you can
	 * inform the user of this error by passing back the error message to be displayed using this
	 * parameter. Do not include if there is no error.
	 */
	@JsonView(View.class)
	private String error;


	public String getFilename() {
		return this.filename;
	}


	public void setFilename(final String logFilename) {
		this.filename = logFilename;
	}


	public String getCharset() {
		return this.charset;
	}


	public void setCharset(final String charset) {
		this.charset = charset;
	}


	public boolean isDate() {
		return this.date;
	}


	public void setDate(final boolean date) {
		this.date = date;
	}


	public boolean isTime() {
		return this.time;
	}


	public void setTime(final boolean time) {
		this.time = time;
	}


	public String getDateTimeFormat() {
		return this.dateTimeFormat;
	}


	public void setDateTimeFormat(final String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}


	public String[ ] getLevels() {
		return this.levels;
	}


	public void setLevels(final String[ ] levels) {
		this.levels = levels;
	}

	/**
	 * Gets the value of the attribute {@link #error}.
	 * @return the value of the attribute {@link #error}.
	 */
	public String getError() {
		return this.error;
	}

	/**
	 * Sets the value of the attribute {@link #error}.
	 * @param errorMsg the value for the attribute {@link #error} to set.
	 */
	public void setError(final String errorMsg) {
		this.error = errorMsg;
	}

	/**
	 * <p>Interface used by Jackson (JSON) for annotate view fields.</p>
	 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
	 * @version 1.0, 05/10/2018.
	 */
	public interface View {
		// No necesita contenido
	}
}
