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
 * <b>File:</b><p>es.gob.fire.core.dto.SignatureDTO.java.</p>
 * <b>Description:</b><p>Class that represents the transfer object and backing form for a log server.</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.persistence.dto;

/** 
 * <p>Class that represents the transfer object and backing form for a log server.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
public class SignatureDTO {

	/**
	 * Attribute that represents the value of the application, provider, format or improved format signature.
	 */
	private String name;
	
	/**
	 * Attribute that represents the value of the correct signatures.
	 */
	private Integer corrects;
	
	/**
	 * Attribute that represents the value of the incorrect signatures.
	 */
	private Integer incorrects;
	
	/**
	 * Attribute that represents the value of the total signatures.
	 */
	private Integer total;

	public SignatureDTO(final String name, final Integer corrects, final Integer incorrects, final Integer total) {
		this.name = name;
		this.corrects = corrects;
		this.incorrects = incorrects;
		this.total = total;
	}
	
	public String getName() {
		return name;
	}

	public void setName(final String nameP) {
		this.name = nameP;
	}

	public Integer getCorrects() {
		return corrects;
	}

	public void setCorrects(final Integer correctsP) {
		this.corrects = correctsP;
	}

	public Integer getIncorrects() {
		return incorrects;
	}

	public void setIncorrects(final Integer incorrectsP) {
		this.incorrects = incorrectsP;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(final Integer totalP) {
		this.total = totalP;
	}

}
