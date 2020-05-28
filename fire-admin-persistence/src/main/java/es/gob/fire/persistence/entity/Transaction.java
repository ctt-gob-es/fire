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
 * <b>File:</b><p>es.gob.fire.persistence.entity.Transaction.java.</p>
 * <b>Description:</b><p>Class that maps the <i>TB_TRANSACCIONES</i> database table as a Plain Old Java Object.</p>
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

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>
 * Class that maps the <i>TB_TRANSACCIONES</i> database table as a Plain Old
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
@Table(name = "TB_TRANSACCIONES")
@SequenceGenerator(name = "TB_TRANSACCIONES_SEQ", sequenceName = "TB_TRANSACCIONES_SEQ", initialValue = 1, allocationSize = 1)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Transaction implements Serializable {

	/**
	 * Attribute that represents the serial version.
	 */
	private static final long serialVersionUID = -6306483033111421674L;

	/**
	 * Attribute that represents the transaction id.
	 */
	private Long transactionId;

	/**
	 * Attribute that represents the transaction date.
	 */
	private Date date;

	/**
	 * Attribute that represents the transaction name.
	 */
	private String application;

	/**
	 * Attribute that represents the transaction operation.
	 */
	private String operation;

	/**
	 * Attribute that represents the transaction provider.
	 */
	private String provider;

	/**
	 * Attribute that represents the transaction forced provider.
	 */
	private Boolean forcedProvider;

	/**
	 * Attribute that represents the transaction correct.
	 */
	private Boolean correct;
	
	/**
	 * Attribute that represents the transaction incorrect.
	 */
	private Boolean incorrect;

	/**
	 * Attribute that represents the transaction size.
	 */
	private Long size;

	/**
	 * Attribute that represents the transaction total.
	 */
	private Long total;

	/**
	 * Gets the value of the attribute {@link #logServerId}.
	 * @return the value of the attribute {@link #logServerId}.
	 */
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = NumberConstants.NUM11)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TB_TRANSACCIONES_SEQ")
	@JsonView(DataTablesOutput.View.class)
	public Long getTransactionId() {
		return this.transactionId;
	}

	/**
	 * Sets the value of the attribute {@link #transactionId}.
	 * @param transactionIdP The value for the attribute {@link #transactionId}.
	 */
	public void setTransactionId(final Long transactionIdP) {
		this.transactionId = transactionIdP;
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
	 * Gets the value of the attribute {@link #operation}.
	 * @return the value of the attribute {@link #operation}.
	 */
	@Column(name = "OPERACION", nullable = false, length = NumberConstants.NUM10)
	@Size(max = NumberConstants.NUM10)
	@NotNull
	public String getOperation() {
		return this.operation;
	}

	/**
	 * Sets the value of the attribute {@link #operation}.
	 * @param operationeP The value for the attribute {@link #operation}.
	 */
	public void setOperation(final String operationP) {
		this.operation = operationP;
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
	 * Gets the value of the attribute {@link #forcedProvider}.
	 * @return the value of the attribute {@link #forcedProvider}.
	 */
	@Column(name = "PROVEEDOR_FORZADO", nullable = false, precision = 1)
	@NotNull
	public Boolean getForcedProvider() {
		return forcedProvider;
	}

	/**
	 * Sets the value of the attribute {@link #forcedProvider}.
	 * @param forcedProviderP The value for the attribute {@link #forcedProvider}.
	 */
	public void setForcedProvider(final Boolean forcedProviderP) {
		this.forcedProvider = forcedProviderP;
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
	 * Gets the value of the attribute {@link #size}.
	 * @return the value of the attribute {@link #size}.
	 */
	@Column(name = "TAMANNO", nullable = false, precision = NumberConstants.NUM11)
	@Size(max = NumberConstants.NUM11)
	@NotNull
	public Long getSize() {
		return size;
	}

	/**
	 * Sets the value of the attribute {@link #size}.
	 * @param sizeP The value for the attribute {@link #size}.
	 */
	public void setSize(final Long sizeP) {
		this.size = sizeP;
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
