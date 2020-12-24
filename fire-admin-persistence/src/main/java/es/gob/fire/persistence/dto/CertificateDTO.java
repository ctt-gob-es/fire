package es.gob.fire.persistence.dto;

import java.security.cert.X509Certificate;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

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
	private String alias;	
	
	/**
	 * Attribute that represents the subject of the system certificate. 
	 */
	private String subject;
	
	/**
	 * Attribute that represents the subject of the certificate principal.  
	 */
	private String certPrincipal;
	
	/**
	 * Attribute that represents the subject of the certificate backup. 
	 */
	private String	certBackup;	
	
	/**
	 * Attribute that represents the uploaded file of the system certificate. 
	 */
	private MultipartFile certFile1;
	
	/**
	 * Attribute that represents the uploaded file of the system certificate. 
	 */
	private MultipartFile certFile2;
	
	/**
	 * Attribute that represents byte array of the system certificate. 
	 */
	private byte[] certBytes1;
	
	/**
	 * Attribute that represents byte array of the system certificate. 
	 */
	private byte[] certBytes2;
	
	/**
	 * Attribute that represents the subject of the system certificate. 
	 */
	
	private String	issuer;
		
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
	 * Attribute that represents the huellaPrincipal.
	 */
	private String huellaPrincipal;
	
	/**
	 * Attribute that represents the huellaBackup.
	 */
	private String huellaBackup;
	
	/**
	 * Attribute that represents index of the row of the selected certificate.
	 */
	private String rowIndexCert;
	
	/**
	 * Attribute that represents the data of the principal certificate in base64.  
	 */
	private String certPrincipalB64;
	
	/**
	 * Attribute that represents the data of the backup certificate in base64. 
	 */
	private String	certBackupB64;	
	
	
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
	
	/**
	 * Gets the value of the attribute {@link #fechaAlta}.
	 * @return the value of the attribute {@link #fechaAlta}.
	 */	
	public Date getFechaAlta() {
		return fechaAlta;
	}

	/**
	 * Sets the value of the attribute {@link #fechaAlta}.
	 * @param fechaAlta the value for the attribute {@link #fechaAlta} to set.
	 */
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
	 * Gets the value of the attribute {@link #certFile1}.
	 * @return the value of the attribute {@link #certFile1}.
	 */	
	public MultipartFile getCertFile1() {
		return certFile1;
	}
	
	/**
	 * Sets the value of the attribute {@link #file}.
	 * @param certFile1 the value for the attribute {@link #certFile1} to set.
	 */
	public void setCertFile1(final MultipartFile certFile1) {
		this.certFile1 = certFile1;
	}
	
	/**
	 * Gets the value of the attribute {@link #certFile2}.
	 * @return the value of the attribute {@link #certFile2}.
	 */	
	public MultipartFile getCertFile2() {
		return certFile2;
	}
	
	/**
	 * Sets the value of the attribute {@link #certFile2}.
	 * @param certFile2 the value for the attribute {@link #certFile2} to set.
	 */
	public void setCertFile2(final MultipartFile certFile2) {
		this.certFile2 = certFile2;
	}

	/**
	 * Gets the value of the attribute {@link #huellaPrincipal}.
	 * @return the value of the attribute {@link #huellaPrincipal}.
	 */	
	public String getHuellaPrincipal() {
		return huellaPrincipal;
	}

	/**
	 * Sets the value of the attribute {@link #certFile2}.
	 * @param huellaPrincipal the value for the attribute {@link #certFile2} to set.
	 */
	public void setHuellaPrincipal(String huellaPrincipal) {
		this.huellaPrincipal = huellaPrincipal;
	}

	/**
	 * Gets the value of the attribute {@link #huellaBackup}.
	 * @return the value of the attribute {@link #huellaBackup}.
	 */	
	public String getHuellaBackup() {
		return huellaBackup;
	}

	/**
	 * Sets the value of the attribute {@link #certFile2}.
	 * @param huellaBackup the value for the attribute {@link #huellaBackup} to set.
	 */
	public void setHuellaBackup(String huellaBackup) {
		this.huellaBackup = huellaBackup;
	}
	
	/**
	 * Gets the value of the attribute {@link #rowIndexCert}.
	 * @return the value of the attribute {@link #rowIndexCert}.
	 */
	public String getRowIndexCert() {
		return rowIndexCert;
	}

	/**
	 * Sets the value of the attribute {@link #rowIndexCert}.
	 * @param rowIndexCert The value for the attribute {@link #rowIndexCert}.
	 */
	public void setRowIndexCert(String rowIndexCert) {
		this.rowIndexCert = rowIndexCert;
	}

	public String getCertPrincipalB64() {
		return certPrincipalB64;
	}

	public void setCertPrincipalB64(String certPrincipalB64) {
		this.certPrincipalB64 = certPrincipalB64;
	}

	public String getCertBackupB64() {
		return certBackupB64;
	}

	public void setCertBackupB64(String certBackupB64) {
		this.certBackupB64 = certBackupB64;
	}

	public byte[] getCertBytes1() {
		return certBytes1;
	}

	public void setCertBytes1(byte[] certBytes1) {
		this.certBytes1 = certBytes1;
	}

	public byte[] getCertBytes2() {
		return certBytes2;
	}

	public void setCertBytes2(byte[] certBytes2) {
		this.certBytes2 = certBytes2;
	}
	
	
		
	
}