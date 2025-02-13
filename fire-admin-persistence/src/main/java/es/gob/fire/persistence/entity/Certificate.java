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
 * <b>File:</b><p>es.gob.valet.persistence.configuration.model.entity.Keystore.java.</p>
 * <b>Description:</b><p>Class that maps the <i>CERTIFICATE</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>01/08/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.3, 13/02/2025.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>KEYSTORE</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.3, 13/02/2025.
 */
@Entity
@Table(name = "TB_CERTIFICADOS")
public class Certificate implements Serializable {

	/**
	 * Constant attribute that represents the serial version UID.
	 */
	private static final long serialVersionUID = -5704821671476223968L;

	/**
	 * Attribute that represents the object ID.
	 */
	private Long idCertificado;

	/**
	 * Attribute that represents the name of the certificate.
	 */
	private String certificateName;

	/**
	 * Attribute that represents the data.
	 */
	 @JsonFormat(pattern="yyyy-MM-dd HH:mm")
	private Date fechaAlta;

	/**
	 * Attribute that represents the certPrincipal.
	 */
	private String certificate;

	/**
	 * Attribute that represents the huellaPrincipal.
	 */
	private String huella;
	
	/**
	 * Attribute that represents the init date.
	 */
	private Date fechaInicio;
	
	/**
	 * Attribute that represents the date expired.
	 */
	private Date fechaCaducidad;

	/**
	 * Attribute that represents the subject.
	 */
	private String subject;
	
	/**
	 * Attribute that represents the date of the last period communication.
	 */	
    private Date dateLastCommunication;
    
	/**
	 * Gets the value of the attribute {@link #idKeystore}.
	 * @return the value of the attribute {@link #idKeystore}.
	 */
	@Id
	@Column(name = "ID_CERTIFICADO", unique = true, nullable = false, precision = NumberConstants.NUM19)
	@GeneratedValue(generator = "tb_certificados_seq")
	@GenericGenerator(name = "tb_certificados_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "TB_CERTIFICADOS_SEQ"), @Parameter(name = "initial_value", value = "1"), @Parameter(name = "increment_size", value = "1") })
	@JsonView(DataTablesOutput.View.class)
	public Long getIdCertificado() {
		return this.idCertificado;
	}

	/**
	 * Sets the value of the attribute {@link #idCertificate}.
	 * @param idKeystoreParam The value for the attribute {@link #idCertificate}.
	 */
	public void setIdCertificado(final Long idCertParam) {
		this.idCertificado = idCertParam;
	}

	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	@Column(name = "NOMBRE_CERT", nullable = false, length = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getCertificateName() {
		return this.certificateName;
	}

	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameParam The value for the attribute {@link #name}.
	 */
	public void setCertificateName(final String nombreParam) {
		this.certificateName = nombreParam;
	}
	/**
	 * Gets the value of the attribute {@link #fecha_alta}.
	 * @return the value of the attribute {@link #fecha_alta}.
	 */
	@Column(name = "FEC_ALTA", nullable = false, length = NumberConstants.NUM19)
	@JsonView(DataTablesOutput.View.class)
	public Date getfechaAlta() {
		return this.fechaAlta;
	}

	/**
	 * Sets the value of the attribute {@link #fechaAlta}.
	 * @param fechaAltaParam The value for the attribute {@link #fechaAlta}.
	 */
	public void setFechaAlta(final Date fechaAltaParam) {
		this.fechaAlta = fechaAltaParam;
	}



	/**
	 * Gets the value of the attribute {@link #certificate}.
	 * @return the value of the attribute {@link #certificate}.
	 */
	@Column(name = "CERTIFICADO", nullable = false, length = NumberConstants.NUM5000)
	@JsonView(DataTablesOutput.View.class)
	public String getCertificate() {
		return this.certificate;
	}

	/**
	 * Sets the value of the attribute {@link #certificate}.
	 * @param keystoreTypeParam The value for the attribute {@link #certificate}.
	 */

	public void setCertificate (final String certificate) {
		this.certificate = certificate;
	}

	/**
	 * Gets the value of the attribute {@link #certificate}.
	 * @return the value of the attribute {@link #certificate}.
	 */
	@Column(name = "HUELLA", nullable = false, length = NumberConstants.NUM50)
	@JsonView(DataTablesOutput.View.class)
	public String getHuella() {
		return this.huella;
	}

	/**
	 * Sets the value of the attribute {@link #huella}.
	 * @param huellaPrincipalParam The value for the attribute {@link #huella}.
	 */

	public void setHuella (final String huella) {
		this.huella = huella;
	}

	/**
	 * Gets the value of the attribute {@link #fechaInicio}.
	 * @return the value of the attribute {@link #fechaInicio}.
	 */
	@Column(name = "FEC_INICIO", nullable = false, length = NumberConstants.NUM19)
	@JsonView(DataTablesOutput.View.class)
	public Date getFechaInicio() {
		return this.fechaInicio;
	}

	/**
	 * Sets the value of the attribute {@link #fechaInicioParam}.
	 * @param fechaInicioParam The value for the attribute {@link #fechaInicioParam}.
	 */
	public void setFechaInicio(final Date fechaInicioParam) {
		this.fechaInicio = fechaInicioParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #fecha_fechaCaducidadalta}.
	 * @return the value of the attribute {@link #fechaCaducidad}.
	 */
	@Column(name = "FEC_CADUCIDAD", nullable = false, length = NumberConstants.NUM19)
	@JsonView(DataTablesOutput.View.class)
	public Date getFechaCaducidad() {
		return this.fechaCaducidad;
	}

	/**
	 * Sets the value of the attribute {@link #fechaCaducidadParam}.
	 * @param fechaCaducidadParam The value for the attribute {@link #fechaCaducidadParam}.
	 */
	public void setFechaCaducidad(final Date fechaCaducidadParam) {
		this.fechaCaducidad = fechaCaducidadParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	@Column(name = "SUBJECT", nullable = false, length = NumberConstants.NUM4000)
	public String getSubject() {
		return this.subject;
	}

	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameParam The value for the attribute {@link #name}.
	 */
	public void setSubject(final String subjectParam) {
		this.subject = subjectParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #dateLastCommunication}.
	 * @return the value of the attribute {@link #dateLastCommunication}.
	 */
	@Column(name = "FECHA_ULTIMA_COMUNICACION", nullable = true)
	public Date getDateLastCommunication() {
		return dateLastCommunication;
	}

	/**
	 * Sets the value of the attribute {@link #dateLastCommunication}.
	 * @param dateLastCommunication The value for the attribute {@link #dateLastCommunication}.
	 */
	public void setDateLastCommunication(Date dateLastCommunication) {
		this.dateLastCommunication = dateLastCommunication;
	}
}