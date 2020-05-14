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
 * <b>File:</b><p>es.gob.fire.persistence.entity.Signature.java.</p>
 * <b>Description:</b><p>Class that maps the <i>TB_FIRMAS</i> database table as a Plain Old Java Object.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.core.constant.NumberConstants;

/**
 * <p>
 * Class that maps the <i>TB_FIRMAS</i> database table as a Plain Old
 * Java Object.
 * </p>
 * <b>Project:</b>
 * <p>
 * Application for signing documents of @firma suite systems.
 * </p>
 * 
 * @version 1.0, 14/04/2020.
 */
@Entity
@Table(name = "TB_FIRMAS")
@SequenceGenerator(name = "TB_FIRMAS_SEQ", sequenceName = "TB_FIRMAS_SEQ", initialValue = 1, allocationSize = 1)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Signature implements Serializable {

	/**
	 * Attribute that represents the serial version.
	 */
	private static final long serialVersionUID = 7182719829397964176L;

	/**
	 * Attribute that represents the signature id.
	 */
	private Long signatureId;

	/**
	 * Attribute that represents the signature date.
	 */
	private Date date;

	/**
	 * Attribute that represents the signature application.
	 */
	private String application;

	/**
	 * Attribute that represents the signature format.
	 */
	private String format;
	
	/**
	 * Attribute that represents the signature improved format.
	 */
	private String improvedFormat;

	/**
	 * Attribute that represents the signature algorithm.
	 */
	private String algorithm;
	
	/**
	 * Attribute that represents the signature provider.
	 */
	private String provider;
	
	/**
	 * Attribute that represents the signature browser.
	 */
	private String browser;

	/**
	 * Attribute that represents the signature correct.
	 */
	private Boolean correct;
	
	/**
	 * Attribute that represents the signature incorrect.
	 */
	private Boolean incorrect;

	/**
	 * Attribute that represents the signature total.
	 */
	private Long total;

	/**
	 * Gets the value of the attribute {@link #logServerId}.
	 * @return the value of the attribute {@link #logServerId}.
	 */
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = NumberConstants.NUM11)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TB_FIRMAS_SEQ")
	@JsonView(DataTablesOutput.View.class)
	public Long getSignatureId() {
		return this.signatureId;
	}

	/**
	 * Sets the value of the attribute {@link #signatureId}.
	 * @param signatureIdP The value for the attribute {@link #signatureId}.
	 */
	public void setSignatureId(final Long signatureIdP) {
		this.signatureId = signatureIdP;
	}

	/**
	 * Gets the value of the attribute {@link #date}.
	 * @return the value of the attribute {@link #date}.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FECHA", nullable = false, length = NumberConstants.NUM6)
	@NotNull
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the value of the attribute {@link #date}.
	 * @param dateP The value for the attribute {@link #date}.
	 */
	public void setDate(final Date dateP) {
		this.date = dateP;
	}

	/**
	 * Gets the value of the attribute {@link #application}.
	 * @return the value of the attribute {@link #application}.
	 */
	@Column(name = "APLICACION", nullable = false, length = NumberConstants.NUM45)
	@Size(max = NumberConstants.NUM45)
	@NotNull
	@JsonView(DataTablesOutput.View.class)
	public String getApplication() {
		return this.application;
	}

	/**
	 * Sets the value of the attribute {@link #application}.
	 * @param applicationP The value for the attribute {@link #application}.
	 */
	public void setApplication(final String applicationP) {
		this.application = applicationP;
	}
	
	/**
	 * Gets the value of the attribute {@link #format}.
	 * @return the value of the attribute {@link #format}.
	 */
	@Column(name = "FORMATO", nullable = false, length = NumberConstants.NUM20)
	@Size(max = NumberConstants.NUM20)
	@NotNull
	public String getFormat() {
		return this.format;
	}

	/**
	 * Sets the value of the attribute {@link #format}.
	 * @param formatP The value for the attribute {@link #format}.
	 */
	public void setFormat(final String formatP) {
		this.format = formatP;
	}

	/**
	 * Gets the value of the attribute {@link #improvedFormat}.
	 * @return the value of the attribute {@link #improvedFormat}.
	 */
	@Column(name = "FORMATO_MEJORADO", nullable = false, length = NumberConstants.NUM20)
	@Size(max = NumberConstants.NUM20)
	@NotNull
	public String getImprovedFormat() {
		return this.improvedFormat;
	}

	/**
	 * Sets the value of the attribute {@link #improvedFormat}.
	 * @param improvedFormatP The value for the attribute {@link #improvedFormat}.
	 */
	public void setImprovedFormat(final String improvedFormatP) {
		this.improvedFormat = improvedFormatP;
	}
	
	/**
	 * Gets the value of the attribute {@link #algorithm}.
	 * @return the value of the attribute {@link #algorithm}.
	 */
	@Column(name = "ALGORITMO", nullable = false, length = NumberConstants.NUM20)
	@Size(max = NumberConstants.NUM20)
	@NotNull
	public String getAlgorithm() {
		return this.algorithm;
	}

	/**
	 * Sets the value of the attribute {@link #algorithm}.
	 * @param algorithmP The value for the attribute {@link #algorithm}.
	 */
	public void setAlgorithm(final String algorithmP) {
		this.algorithm = algorithmP;
	}
	
	/**
	 * Gets the value of the attribute {@link #provider}.
	 * @return the value of the attribute {@link #provider}.
	 */
	@Column(name = "PROVEEDOR", nullable = false, length = NumberConstants.NUM45)
	@Size(max = NumberConstants.NUM45)
	@NotNull
	public String getProvider() {
		return this.provider;
	}

	/**
	 * Sets the value of the attribute {@link #provider}.
	 * @param providerP The value for the attribute {@link #provider}.
	 */
	public void setProvider(final String providerP) {
		this.provider = providerP;
	}
	
	/**
	 * Gets the value of the attribute {@link #browser}.
	 * @return the value of the attribute {@link #browser}.
	 */
	@Column(name = "NAVEGADOR", nullable = false, length = NumberConstants.NUM45)
	@Size(max = NumberConstants.NUM45)
	@NotNull
	public String getBrowser() {
		return this.browser;
	}

	/**
	 * Sets the value of the attribute {@link #browser}.
	 * @param browserP The value for the attribute {@link #browser}.
	 */
	public void setBrowser(final String browserP) {
		this.browser = browserP;
	}

	/**
	 * Gets the value of the attribute {@link #correct}.
	 * @return the value of the attribute {@link #correct}.
	 */
	@Column(name = "CORRECTA", nullable = false, precision = 1)
	@NotNull
	@JsonView(DataTablesOutput.View.class)
	public Boolean getCorrect() {
		return correct;
	}

	/**
	 * Sets the value of the attribute {@link #correct}.
	 * @param correctP The value for the attribute {@link #correct}.
	 */
	public void setCorrect(final Boolean correctP) {
		this.correct = correctP;
	}
	
	/**
	 * Gets the value of the attribute {@link #incorrect}.
	 * @return the value of the attribute {@link #incorrect}.
	 */
	@Transient
	public Boolean getIncorrect() {
		return incorrect;
	}

	/**
	 * Sets the value of the attribute {@link #incorrect}.
	 * @param incorrectP The value for the attribute {@link #incorrect}.
	 */
	public void setIncorrect(final Boolean incorrectP) {
		this.incorrect = incorrectP;
	}
	
	/**
	 * Gets the value of the attribute {@link #total}.
	 * @return the value of the attribute {@link #total}.
	 */
	@Column(name = "TOTAL", nullable = false, precision = NumberConstants.NUM11)
	@Size(max = NumberConstants.NUM11)
	@NotNull
	public Long getTotal() {
		return total;
	}

	/**
	 * Sets the value of the attribute {@link #total}.
	 * @param totalP The value for the attribute {@link #total}.
	 */
	public void setTotal(final Long totalP) {
		this.total = totalP;
	}

}
