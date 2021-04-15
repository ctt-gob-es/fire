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
 * <b>File:</b><p>es.gob.valet.persistence.configuration.model.entity.Keystore.java.</p>
 * <b>Description:</b><p>Class that maps the <i>CERTIFICATE</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>01/08/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 18/09/2018.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that maps the <i>KEYSTORE</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 18/09/2018.
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
	private String certPrincipal;

	/**
	 * Attribute that represents the certBackup.
	 */
	private String certBackup;
	
	/**
	 * Attribute that represents the huellaPrincipal.
	 */
	private String huellaPrincipal;
	
	/**
	 * Attribute that represents the huellaBackup.
	 */
	private String huellaBackup;
	
    
	
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
		return idCertificado;
	}

	/**
	 * Sets the value of the attribute {@link #idCertificate}.
	 * @param idKeystoreParam The value for the attribute {@link #idCertificate}.
	 */
	public void setIdCertificado(Long idCertParam) {
		this.idCertificado = idCertParam;
	}

	/**
	 * Gets the value of the attribute {@link #name}.
	 * @return the value of the attribute {@link #name}.
	 */
	@Column(name = "NOMBRE_CERT", nullable = false, length = NumberConstants.NUM45)
	@JsonView(DataTablesOutput.View.class)
	public String getCertificateName() {
		return certificateName;
	}

	/**
	 * Sets the value of the attribute {@link #name}.
	 * @param nameParam The value for the attribute {@link #name}.
	 */
	public void setCertificateName(String nombreParam) {
		this.certificateName = nombreParam;
	}
	/**
	 * Gets the value of the attribute {@link #fecha_alta}.
	 * @return the value of the attribute {@link #fecha_alta}.
	 */
	@Column(name = "FEC_ALTA", nullable = false, length = NumberConstants.NUM19)
	@JsonView(DataTablesOutput.View.class)
	public Date getfechaAlta() {
		return fechaAlta;
	}

	/**
	 * Sets the value of the attribute {@link #fechaAlta}.
	 * @param fechaAltaParam The value for the attribute {@link #fechaAlta}.
	 */
	public void setfechaAlta(Date fechaAltaParam) {
		this.fechaAlta = fechaAltaParam;
	}
	
	

	/**
	 * Gets the value of the attribute {@link #certPrincipal}.
	 * @return the value of the attribute {@link #certPrincipal}.
	 */
	@Column(name = "CERT_PRINCIPAL", nullable = false, length = NumberConstants.NUM5000)
	@JsonView(DataTablesOutput.View.class)
	public String getCertPrincipal() {
		return certPrincipal;
	}

	/**
	 * Sets the value of the attribute {@link #certPrincipal}.
	 * @param keystoreTypeParam The value for the attribute {@link #certPrincipal}.
	 */
	
	public void setCertPrincipal (String certPrincipalParam) {
		this.certPrincipal = certPrincipalParam;
	}

	/**
	 * Gets the value of the attribute {@link #certBackup}.
	 * @return the value of the attribute {@link #certBackup}.
	 */
	@Column(name = "CERT_BACKUP", nullable = false, precision = NumberConstants.NUM5000)
	@JsonView(DataTablesOutput.View.class)
	public String getCertBackup() {
		return certBackup;
	}

	/**
	 * Sets the value of the attribute {@link #certBackup}.
	 * @param versionParam The value for the attribute {@link #certBackup}.
	 */
	public void setCertBackup(String certBackupParam) {
		this.certBackup = certBackupParam;
	}

	/**
	 * Gets the value of the attribute {@link #certPrincipal}.
	 * @return the value of the attribute {@link #certPrincipal}.
	 */
	@Column(name = "HUELLA_PRINCIPAL", nullable = false, length = NumberConstants.NUM50)
	@JsonView(DataTablesOutput.View.class)
	public String getHuellaPrincipal() {
		return huellaPrincipal;
	}

	/**
	 * Sets the value of the attribute {@link #huellaPrincipal}.
	 * @param huellaPrincipalParam The value for the attribute {@link #huellaPrincipal}.
	 */
	
	public void setHuellaPrincipal (String huellaPrincipalParam) {
		this.huellaPrincipal = huellaPrincipalParam;
	}

	/**
	 * Gets the value of the attribute {@link #huellaBackup}.
	 * @return the value of the attribute {@link #huellaBackup}.
	 */
	@Column(name = "HUELLA_BACKUP", nullable = false, precision = NumberConstants.NUM19)
	@JsonView(DataTablesOutput.View.class)
	public String getHuellaBackup() {
		return huellaBackup;
	}

	/**
	 * Sets the value of the attribute {@link #huellaBackup}.
	 * @param huellaBackupParam The value for the attribute {@link #huellaBackup}.
	 */
	public void setHuellaBackup(String huellaBackupParam) {
		this.huellaBackup = huellaBackupParam;
	}


	


}




