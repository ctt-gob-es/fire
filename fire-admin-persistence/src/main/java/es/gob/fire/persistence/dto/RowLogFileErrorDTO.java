/*
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
 * <b>File:</b><p>es.gob.monitoriza.persistence.configuration.dto.RowStatusDTO.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>11/10/2018.</p>
 * @author Gobierno de España.
 * @version 1.1, 18/10/2018.
 */
package es.gob.fire.persistence.dto;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * <p>Class that represents a row of error in the log files datatable.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 20/03/2019.
 */
public class RowLogFileErrorDTO {

	/**
	 * Attribute that represents the errpr code.
	 */
	private int code;

	/**
	 * Attribute that represents the file's message.
	 */
	private String message;

	/**
	 * Gets the value of the attribute {@link #Code}.
	 * @return the value of the attribute {@link #Code}.
	 */
	@JsonView(LogFilesDTO.View.class)
	public int getCode() {
		return this.code;
	}

	/**
	 * Sets the value of the attribute {@link #Code}.
	 * @param code The value for the attribute {@link #Code}.
	 */
	public void setCode(final int code) {
		this.code = code;
	}

	/**
	 * Gets the value of the attribute {@link #date}.
	 * @return the value of the attribute {@link #date}.
	 */
	@JsonView(LogFilesDTO.View.class)
	public String getMessage() {
		return this.message;
	}

	/**
	 * Sets the value of the attribute {@link #date}.
	 * @param date The value for the attribute {@link #date}.
	 */
	public void setMessage(final String message) {
		this.message = message;
	}
}
