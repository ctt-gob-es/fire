package es.gob.fire.persistence.dto;

import java.util.Arrays;
import java.util.Date;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * <p>Class that represents the backing form for adding/editing a system certificate.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.4, 28/01/2025.
 */
public class CertificateDTO {

	/**
	 * Attribute that represents the value of the primary key as a hidden input in the form.
	 */
	@JsonView(DataTablesOutput.View.class)
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
	@JsonView(DataTablesOutput.View.class)
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
	@JsonView(DataTablesOutput.View.class)
	@JsonFormat(pattern="yyyy-MM-dd HH:mm")
	private Date fechaAlta;
	
	/**
	 * Attribute that represents the huellaPrincipal.
	 */
	private String huella;

	/**
	 * Attribute that represents index of the row of the selected certificate.
	 */
	private String rowIndexCert;

	/**
	 * Attribute that represents the data of the principal certificate in base64.
	 */
	private String certificateB64;

	/**
	 * Attribute that represents the data of the certificate name.
	 */
	@JsonView(DataTablesOutput.View.class)
	private String certificateName;
	
	/**
	 * Attribute that represents the data of the status.
	 */
	@JsonView(DataTablesOutput.View.class)
	private String status;
		
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
	 * Gets the value of the attribute {@link #huella}.
	 * @return the value of the attribute {@link #huella}.
	 */
	public String getHuella() {
		return this.huella;
	}

	/**
	 * Sets the value of the attribute {@link #certFile2}.
	 * @param huellaPrincipal the value for the attribute {@link #certFile2} to set.
	 */
	public void setHuella(final String huellaPrincipal) {
		this.huella = huellaPrincipal;
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

	/**
	 * Gets the value of the attribute {@link #certificateName}.
	 * @return the value of the attribute {@link #certificateName}.
	 */
	public String getCertificateName() {
		return certificateName;
	}

	/**
	 * Sets the value of the attribute {@link #certificateName}.
	 * @param certificateName The value for the attribute {@link #certificateName}.
	 */
	public void setCertificateName(String certificateName) {
		this.certificateName = certificateName;
	}

	/**
	 * Gets the value of the attribute {@link #status}.
	 * @return the value of the attribute {@link #status}.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the value of the attribute {@link #status}.
	 * @param status The value for the attribute {@link #status}.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

}