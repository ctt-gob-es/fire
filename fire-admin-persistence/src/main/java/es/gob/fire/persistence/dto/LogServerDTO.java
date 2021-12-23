/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
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
 * <b>File:</b><p>es.gob.fire.core.dto.LogServerDTO.java.</p>
 * <b>Description:</b><p>Class that represents the transfer object and backing form for a log server.</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 02/06/2021.
 */
package es.gob.fire.persistence.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.persistence.dto.validation.ThenCheckIt;

/** 
 * <p>Class that represents the transfer object and backing form for a log server.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.1, 02/06/2021.
 */
public class LogServerDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	private Long logServerId;

	/**
	 * Attribute that represents the value of the input name of the log server in the form.
	 */
	@NotNull(message = "{form.log.server.name.pattern}")
	@Size(min = 1, max = NumberConstants.NUM45, groups=ThenCheckIt.class)
	private String name = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input urlService of the log server in the form.
	 */
	@NotNull(message = "{form.log.server.url.service.pattern}")
	@Size(min = NumberConstants.NUM3, max = NumberConstants.NUM500, groups=ThenCheckIt.class)
	private String urlService = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input key of the key in the form.
	 */
	@NotNull(message = "{form.log.server.key.pattern}")
	@Size(min = NumberConstants.NUM3, max = NumberConstants.NUM45, groups=ThenCheckIt.class)
	private String key = UtilsStringChar.EMPTY_STRING;

	/**
	 * Attribute that represents the value of the input verifySSL of the log server in the form.
	 */
	@NotNull(message = "{form.log.server.secure.notnull}")
	private Boolean verifySSL = Boolean.FALSE;

	/**
	 * Gets the value of the attribute {@link #logServerId}.
	 * @return the value of the attribute {@link #logServerId}.
	 */
	public Long getLogServerId() {
		return this.logServerId;
	}

	/**
	 * Sets the value of the attribute {@link #logServerId}.
	 * @param logServerIdP the value for the attribute {@link #logServerId} to set.
	 */
	public void setLogServerId(final Long logServerIdP) {
		this.logServerId = logServerIdP;
	}

	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameP the value for the attribute {@link #name} to set.
	 */
	public void setName(final String nameP) {
		this.name = nameP;
	}

	/**
	 * Gets the value of the attribute {@link #urlService}.
	 * @return the value of the attribute {@link #urlService}.
	 */
	public String getUrlService() {
		return this.urlService;
	}

	/**
	 * Sets the value of the attribute {@link #urlService}.
	 * @param urlServiceP the value for the attribute {@link #urlService} to set.
	 */
	public void setUrlService(final String urlServiceP) {
		this.urlService = urlServiceP;
	}

	/**
	 * Gets the value of the attribute {@link #url}.
	 * @return the value of the attribute {@link #url}.
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Sets the value of the attribute {@link #key}.
	 * @param keyP the value for the attribute {@link #key} to set.
	 */
	public void setKey(final String keyP) {
		this.key = keyP;
	}

	/**
	 * Gets the value of the attribute {@link #verifySSL}.
	 * @return the value of the attribute {@link #verifySSL}.
	 */

	public final boolean getVerifySSL() {
		return this.verifySSL;

	}

	/**
	 * Sets the value of the attribute {@link #verifySSL}.
	 * @param verifySSLP the value for the attribute {@link #verifySSL} to set.
	 */
	public final void setVerifySSL(final Boolean verifySSLP) {
		this.verifySSL = verifySSLP;
	}

}
