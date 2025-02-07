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
 * <b>File:</b><p>es.gob.fire.persistence.dto.ConstantsDTO.java.</p>
 * <b>Description:</b><p>Class that represents a constant in clients view.</p>
  * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/05/2020.
 */
package es.gob.fire.persistence.dto;

import java.io.Serializable;

/** 
 * <p>Class that represents a constant in clients view.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature..</p>
 * @version 1.0, 15/05/2020.
 */
public class ConstantsDTO implements Serializable, Comparable<ConstantsDTO> {

	/**
	 * Attribute that represents the serial version UID. 
	 */
	private static final long serialVersionUID = -6468608122931567487L;

	/**
	 * Constant id.
	 */
	private Long idConstant;

	/**
	 * Constant value obtained from multilanguage.
	 */
	private String value;

	
	/**
	 * Gets the value of the attribute {@link #idConstant}.
	 * @return the value of the attribute {@link #idConstant}.
	 */
	
	public Long getIdConstant() {
		return idConstant;
	}

		
	/**
	 * Gets the value of the attribute {@link #value}.
	 * @return the value of the attribute {@link #value}.
	 */
	
	public String getValue() {
		return value;
	}

	/**
	 * Constructor method for the class ConstantsForm.java.
	 * @param idConstantParam Constant id from db;
	 * @param valueParam  Constant value
	 */
	public ConstantsDTO(Long idConstantParam, String valueParam) {
		this.idConstant = idConstantParam;
		this.value = valueParam;
	}


	/**
	 * Constructor method for the class ConstantsForm.java. 
	 */
	public ConstantsDTO() {
		super();
	}


	/**
	 * {@inheritDoc}
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ConstantsDTO c) {
		if (!idConstant.equals(c.getIdConstant())){
			return value.compareTo(c.getValue());
		}
		return 0;
	}
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	// CHECKSTYLE:OFF -- Managed Beans can´t have final methods.
	@Override
	public boolean equals(Object obj) {
		// CHECKSTYLE:ON
		if (obj instanceof ConstantsDTO) {
			return ((ConstantsDTO) obj).getIdConstant().equals(idConstant);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	// CHECKSTYLE:OFF -- Managed Beans can´t have final methods.
	@Override
	public String toString() {
		// CHECKSTYLE:OFF -- Managed Beans can´t have final methods.
		return value;
	}
	
}
