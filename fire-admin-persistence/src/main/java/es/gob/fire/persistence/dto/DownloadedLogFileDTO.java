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
 * Data transfer object that encapsulates the information of a downloaded log file.
 * </p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 15/04/2019.
 */
public class DownloadedLogFileDTO {

	@JsonView(View.class)
	private String contentType;

	@JsonView(View.class)
	private String filename;

	@JsonView(View.class)
	private byte[] data;

	/**
	 * Optional: If an error occurs during the running of the server-side processing script, you can
	 * inform the user of this error by passing back the error message to be displayed using this
	 * parameter. Do not include if there is no error.
	 */
	@JsonView(View.class)
	private String error;


	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public byte[ ] getData() {
		return this.data;
	}

	public void setData(final byte[ ] data) {
		this.data = data;
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
