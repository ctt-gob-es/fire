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
 * <b>File:</b><p>es.gob.fire.persistence.dto.CAuthenticationTypeDTO.java.</p>
 * <b>Description:</b><p> Class that represents the backing form for editing a Authentication type.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 04/03/2025.
 */
package es.gob.fire.persistence.dto;

/**
 * <p>Class that represents the backing form for adding a user.</p>
 * <b>Project:</b><p> Class that represents the backing form for editing a Authentication type.</p>
 * @version 1.0, 04/03/2025.
 */
public class CAuthenticationTypeDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	private Long idAuthenticationType;

	/**
	 * Attribute that represent the value to name token.
	 */
	private String value;

	/**
	 * Default constructor for {@link CAuthenticationTypeDTO}.
	 *
	 * <p>Creates an empty instance of {@code CAuthenticationTypeDTO} with default values.</p>
	 */
	public CAuthenticationTypeDTO() {}

	/**
	 * Constructs a {@link CAuthenticationTypeDTO} with the specified authentication type ID and token name value.
	 *
	 * @param idAuthenticationType The unique identifier for the authentication type.
	 * @param tokenNameValue The value associated with the authentication type token.
	 */
	public CAuthenticationTypeDTO(Long idAuthenticationType, String tokenNameValue) {
		this.idAuthenticationType = idAuthenticationType;
		this.value = tokenNameValue;
	}

	/**
	 * Gets the value of the attribute {@link #idAuthenticationType}.
	 * @return the value of the attribute {@link #idAuthenticationType}.
	 */
	public Long getIdAuthenticationType() {
		return idAuthenticationType;
	}

	/**
	 * Sets the value of the attribute {@link #idAuthenticationType}.
	 * @param idAuthenticationType the value for the attribute {@link #idAuthenticationType} to set.
	 */
	public void setIdAuthenticationType(Long idAuthenticationType) {
		this.idAuthenticationType = idAuthenticationType;
	}

	/**
	 * Gets the value of the attribute {@link #value}.
	 * @return the value of the attribute {@link #value}.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the attribute {@link #value}.
	 * @param value the value for the attribute {@link #value} to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
