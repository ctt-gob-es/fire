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
 * <b>File:</b><p>es.gob.afirma.persistence.configuration.model.entity.config.CAuthenticationType.java.</p>
 * <b>Description:</b><p>Class that represents the representation of the <i>TB_C_TIPO_AUTENTICACION</i> database table as a
 * Plain Old Java Object.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>19/06/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 19/06/2020.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that represents the representation of the <i>TB_C_TIPO_AUTENTICACION</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p></p>
 * @version 1.0, 13/02/2012.
 */
@Entity
@Table(name = "TB_C_TIPO_AUTENTICACION")
public class CAuthenticationType implements Serializable {

	/**
	 * Class serial version.
	 */
	private static final long serialVersionUID = -7101917130553871676L;

	/**
	 * Attribute that represents the object ID.
	 */
	@Id
	@Column(name = "ID_TIPO_AUTENTICACION", unique = true, nullable = false, precision = NumberConstants.NUM19)
	private Long idAuthenticationType;

	/**
	 * Attribute that represents the name of the token with the description stored in properties file for internationalization.
	 */
	@Column(name = "NOMBRE_TOKEN", nullable = false, length = NumberConstants.NUM45)
	private String tokenName;

	/**
	 * Gets the value of the attribute {@link #idAuthenticationType}.
	 * @return the value of the attribute {@link #idAuthenticationType}.
	 */
	public Long getIdAuthenticationType() {
		return idAuthenticationType;
	}

	/**
	 * Sets the value of the attribute {@link #idAuthenticationType}.
	 * @param idAuthenticationTypeParam The value for the attribute {@link #idAuthenticationType}.
	 */
	public void setIdAuthenticationType(Long idAuthenticationTypeParam) {
		this.idAuthenticationType = idAuthenticationTypeParam;
	}

	/**
	 * Gets the value of the attribute {@link #tokenName}.
	 * @return the value of the attribute {@link #tokenName}.
	 */
	public String getTokenName() {
		return tokenName;
	}

	/**
	 * Sets the value of the attribute {@link #tokenName}.
	 * @param tokenNameParam The value for the attribute {@link #tokenName}.
	 */
	public void setTokenName(String tokenNameParam) {
		this.tokenName = tokenNameParam;
	}

}