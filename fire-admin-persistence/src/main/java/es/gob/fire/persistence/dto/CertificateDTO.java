package es.gob.fire.persistence.dto;

import java.util.Arrays;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>Class that represents the backing form for adding/editing a system certificate.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.3, 27/01/2025.
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
	private String certificate;

	/**
	 * Attribute that represents the uploaded file of the system certificate.
	 */
	private MultipartFile certFile;

	/**
	 * Attribute that represents byte array of the system certificate.
	 */
	private byte[] certBytes;

	/**
	 * Attribute that represents the subject of the system certificate.
	 */

	private String	issuer;

	/**
	 * Attribute that represents the data.
	 */
	private Date fechaAlta;
	
	/**
	 * Attribute that represents the huellaPrincipal.
	 */
	private String huellaPrincipal;

	/**
	 * Attribute that represents index of the row of the selected certificate.
	 */
	private String rowIndexCert;

	/**
	 * Attribute that represents the data of the principal certificate in base64.
	 */
	private String certificateB64;

	/**
	 * Gets the value of the attribute {@link #idSystemCertificate}.
	 * @return the value of the attribute {@link #idSystemCertificate}.
	 */
	public Long getIdCertificate() {
		return this.idCertificate;
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
		return this.alias;
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
		return this.subject;
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
		return this.fechaAlta;
	}

	/**
	 * Sets the value of the attribute {@link #fechaAlta}.
	 * @param fechaAlta the value for the attribute {@link #fechaAlta} to set.
	 */
	public void setFechaAlta(final Date fechaAlta) {
		this.fechaAlta = fechaAlta;
	}
	
	public String getCertificate() {
		return this.certificate;
	}

	public void setCertificate(final String certificate) {
		this.certificate = certificate;
	}

	public String getIssuer() {
		return this.issuer;
	}

	public void setIssuer(final String issuer) {
		this.issuer = issuer;
	}

	/**
	 * Gets the value of the attribute {@link #certFile}.
	 * @return the value of the attribute {@link #certFile}.
	 */
	public MultipartFile getCertFile() {
		return this.certFile;
	}

	/**
	 * Sets the value of the attribute {@link #file}.
	 * @param certFile the value for the attribute {@link #certFile} to set.
	 */
	public void setCertFile(final MultipartFile certFile) {
		this.certFile = certFile;
	}

	/**
	 * Gets the value of the attribute {@link #huellaPrincipal}.
	 * @return the value of the attribute {@link #huellaPrincipal}.
	 */
	public String getHuellaPrincipal() {
		return this.huellaPrincipal;
	}

	/**
	 * Sets the value of the attribute {@link #certFile2}.
	 * @param huellaPrincipal the value for the attribute {@link #certFile2} to set.
	 */
	public void setHuellaPrincipal(final String huellaPrincipal) {
		this.huellaPrincipal = huellaPrincipal;
	}

	/**
	 * Gets the value of the attribute {@link #rowIndexCert}.
	 * @return the value of the attribute {@link #rowIndexCert}.
	 */
	public String getRowIndexCert() {
		return this.rowIndexCert;
	}

	/**
	 * Sets the value of the attribute {@link #rowIndexCert}.
	 * @param rowIndexCert The value for the attribute {@link #rowIndexCert}.
	 */
	public void setRowIndexCert(final String rowIndexCert) {
		this.rowIndexCert = rowIndexCert;
	}

	public String getCertificateB64() {
		return this.certificateB64;
	}

	public void setCertificateB64(final String certificateB64) {
		this.certificateB64 = certificateB64;
	}

	public byte[] getCertBytes() {
		return this.certBytes != null
				? Arrays.copyOf(this.certBytes, this.certBytes.length) : null;
	}

	public void setCertBytes(final byte[] certBytes) {
		this.certBytes = certBytes != null
				? Arrays.copyOf(certBytes, certBytes.length) : certBytes;
	}

}