package es.gob.fire.persistence.dto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.Utils;

public class MailInfoDTO {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(MailInfoDTO.class);

	private String emailResponsible;
	
	private String subjectCertificate;

	private String dateCertInit;

	private String dateCertExpired;

	private Long idCertificado;

	public MailInfoDTO(String emailResponsible, Long idCertificado, Date fechaInicio, Date fechaCaducidad, String subject) {
		this.emailResponsible = emailResponsible;
		this.idCertificado = idCertificado;
		this.subjectCertificate = subject;
		this.dateCertInit = Utils.getStringDateFormat(fechaInicio);
		this.dateCertExpired = Utils.getStringDateFormat(fechaCaducidad);
	}

	public String getEmailResponsible() {
		return emailResponsible;
	}

	public void setEmailResponsible(String emailResponsible) {
		this.emailResponsible = emailResponsible;
	}

	public String getSubjectCertificate() {
		return subjectCertificate;
	}

	public void setSubjectCertificate(String subjectCertificate) {
		this.subjectCertificate = subjectCertificate;
	}

	public String getDateCertInit() {
		return dateCertInit;
	}

	public void setDateCertInit(String dateCertInit) {
		this.dateCertInit = dateCertInit;
	}

	public String getDateCertExpired() {
		return dateCertExpired;
	}

	public void setDateCertExpired(String dateCertExpired) {
		this.dateCertExpired = dateCertExpired;
	}

	public Long getIdCertificado() {
		return idCertificado;
	}

	public void setIdCertificado(Long idCertificado) {
		this.idCertificado = idCertificado;
	}

}
