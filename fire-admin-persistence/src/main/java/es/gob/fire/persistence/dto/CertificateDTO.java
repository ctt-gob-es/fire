package es.gob.fire.persistence.dto;

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
 * <b>File:</b><p>es.gob.fire.persistence.configuration.dto.CertificateDTO.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>17/05/2018.</p>
 * @author Gobierno de España.
 * @version 1.2, 25/01/2019.
 */


import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.text.View;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.persistence.dto.validation.CheckItFirst;
import es.gob.fire.persistence.dto.validation.ThenCheckIt;

/** 
 * <p>Class that represents the backing form for adding/editing a system certificate.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.2, 25/01/2019.
 */
public class CertificateDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form. 
	 */
	private Long idCertificate;
	
	/**
	 * Attribute that represents the value of the input alias of the system certificate in the form. 
	 */
	@NotBlank(groups=CheckItFirst.class, message="{form.valid.keystore.alias.notempty}")
    @Size(min=NumberConstants.NUM1, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
	private String alias;
	
	
	
	/**
	 * Attribute that represents the subject of the system certificate. 
	 */
	private String subject;
	
	/**
	 * Attribute that represents the subject of the   certificate principal.  
	 */
	@NotBlank(groups=CheckItFirst.class, message="{form.valid.keystore.certPrincipal.notempty}")
    @Size(min=NumberConstants.NUM1, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
	private String certPrincipal;
	
	/**
	 * Attribute that represents the subject of the  certificate backup. 
	 */
	@NotBlank(groups=CheckItFirst.class, message="{form.valid.keystore.certBackup.notempty}")
    @Size(min=NumberConstants.NUM1, max=NumberConstants.NUM30, groups=ThenCheckIt.class)
	private String	certBackup;
	
	
	/**
	 * Attribute that represents the uploaded file of the system certificate. 
	 */
	
	
	@NotNull(groups=CheckItFirst.class, message="{form.valid.keystore.file.notnull}")
	private MultipartFile file;
	
	/**
	 * Attribute that represents the uploaded file of the system certificate. 
	 */
	
	
	@NotNull(groups=CheckItFirst.class, message="{form.valid.keystore.file.notnull}")
	private MultipartFile file2;
	
	/**
	 * Attribute that represents byte array of the system certificate. 
	 */
	private byte[] certBytes;
	
	/**
	 * Attribute that represents the subject of the system certificate. 
	 */
	
	private String	issuer;
	/**
	 * Attribute that represents the user of the system certificate. 
	 */
	private Long idUser;
	
	/**
	 * Attribute that represents the status of the system certificate. 
	 */
	private Long idStatusCertificate;
	
	/**
	 * Attribute that represents the data.
	 */
	private Date fechaAlta;
	
	/**
	 * Attribute that represents the x509Principal.
	 */
	
	private X509Certificate x509Principal;
	
	/**
	 * Attribute that represents the x509Backup.
	 */

	private X509Certificate x509Backup;

	
	
	/**
	 * Gets the value of the attribute {@link #idSystemCertificate}.
	 * @return the value of the attribute {@link #idSystemCertificate}.
	 */	
	public Long getIdCertificate() {
		return idCertificate;
	}
	
	/**
	 * Sets the value of the attribute {@link #idSystemCertificate}.
	 * @param idSystemCertificateParam the value for the attribute {@link #idSystemCertificate} to set.
	 */
	public void setIdCertificate(final Long idCertificateParam) {
		this.idCertificate = idCertificateParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #alias}.
	 * @return the value of the attribute {@link #alias}.
	 */	
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Sets the value of the attribute {@link #alias}.
	 * @param aliasParam the value for the attribute {@link #alias} to set.
	 */
	public void setAlias(final String aliasParam) {
		this.alias = aliasParam;
	}
		
	
	

	/**
	 * Sets the value of the attribute {@link #fechaAlta}.
	 * @param fechaAltaParam The value for the attribute {@link #fechaAlta}.
	 */
	
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
	 * Gets the value of the attribute {@link #subject}.
	 * @return the value of the attribute {@link #subject}.
	 */	
	public String getSubject() {
		return subject;
	}

	
	/**
	 * Sets the value of the attribute {@link #subject}.
	 * @param subjectParam the value for the attribute {@link #subject} to set.
	 */
	public void setSubject(final String subjectParam) {
		this.subject = subjectParam;
	}
	
	

	
	
	
	
	
	
	public Date getFechaAlta() {
		return fechaAlta;
	}

	public void setFechaAlta(Date fechaAlta) {
		this.fechaAlta = fechaAlta;
	}

	public X509Certificate getX509Principal() {
		return x509Principal;
	}

	public void setX509Principal(X509Certificate x509Principal) {
		this.x509Principal = x509Principal;
	}

	public X509Certificate getX509Backup() {
		return x509Backup;
	}

	public void setX509Backup(X509Certificate x509Backup) {
		this.x509Backup = x509Backup;
	}

	public String getCertPrincipal() {
		return certPrincipal;
	}

	public void setCertPrincipal(String certPrincipal) {
		this.certPrincipal = certPrincipal;
	}

	public String getCertBackup() {
		return certBackup;
	}

	public void setCertBackup(String certBackup) {
		this.certBackup = certBackup;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	
	/**
	 * Gets the value of the attribute {@link #file}.
	 * @return the value of the attribute {@link #file}.
	 */	
	public MultipartFile getFile() {
		return file;
	}
	
	/**
	 * Sets the value of the attribute {@link #file}.
	 * @param sslCertificate the value for the attribute {@link #file} to set.
	 */
	public void setFile(final MultipartFile sslCertificate) {
		this.file = sslCertificate;
	}
	
	/**
	 * Gets the value of the attribute {@link #file}.
	 * @return the value of the attribute {@link #file}.
	 */	
	public MultipartFile getFile2() {
		return file2;
	}
	
	/**
	 * Sets the value of the attribute {@link #file}.
	 * @param sslCertificate the value for the attribute {@link #file} to set.
	 */
	public void setFile2(final MultipartFile sslCertificate) {
		this.file2 = sslCertificate;
	}
	
}


