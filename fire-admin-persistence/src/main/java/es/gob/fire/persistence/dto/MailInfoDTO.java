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
 * <b>File:</b><p>es.gob.fire.core.dto.MailInfoDTO.java.</p>
 * <b>Description:</b><p>Class that represents the transfer object and backing form for a send email.</p>
  * <b>Project:</b><p>Application for monitoring the services of FIRe suite systems</p>
 * <b>Date:</b><p>12/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 12/02/2025.
 */
package es.gob.fire.persistence.dto;

import java.util.Date;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Utils;

/** 
 * <p>Class that represents the transfer object and backing form for a send email.</p>
 * <b>Project:</b><p>Application for monitoring services of FIRe suite systems.</p>
 * @version 1.0, 12/02/2025.
 */
public class MailInfoDTO {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(MailInfoDTO.class);

	/**
	 * Attribute that represents the value of the input email responsible of the email in the form.
	 */
	private String emailResponsible;
	
	/**
	 * Attribute that represents the value of the input subject certificate of the email in the form.
	 */
	private String subjectCertificate;

	/**
	 * Attribute that represents the value of the input date initialize to valid certificate of the email in the form.
	 */
	private String dateCertInit;

	/**
	 * Attribute that represents the value of the input date expired to valid certificate of the email in the form.
	 */
	private String dateCertExpired;

	/**
	 * Attribute that represents the value of the pk certificate of the email in the form.
	 */
	private Long idCertificado;

	public MailInfoDTO(String emailResponsible, Long idCertificado, Date fechaInicio, Date fechaCaducidad, String subject) {
		this.emailResponsible = emailResponsible;
		this.idCertificado = idCertificado;
		this.subjectCertificate = subject;
		this.dateCertInit = Utils.getStringDateFormat(fechaInicio);
		this.dateCertExpired = Utils.getStringDateFormat(fechaCaducidad);
	}

	/**
	 * Gets the value of the attribute {@link #emailResponsible}.
	 * @return the value of the attribute {@link #emailResponsible}.
	 */
	public String getEmailResponsible() {
		return emailResponsible;
	}

	/**
	 * Sets the value of the attribute {@link #emailResponsible}.
	 * @param emailResponsible the value for the attribute {@link #emailResponsible} to set.
	 */
	public void setEmailResponsible(String emailResponsible) {
		this.emailResponsible = emailResponsible;
	}

	/**
	 * Gets the value of the attribute {@link #subjectCertificate}.
	 * @return the value of the attribute {@link #subjectCertificate}.
	 */
	public String getSubjectCertificate() {
		return subjectCertificate;
	}

	/**
	 * Sets the value of the attribute {@link #subjectCertificate}.
	 * @param subjectCertificate the value for the attribute {@link #subjectCertificate} to set.
	 */
	public void setSubjectCertificate(String subjectCertificate) {
		this.subjectCertificate = subjectCertificate;
	}

	/**
	 * Gets the value of the attribute {@link #dateCertInit}.
	 * @return the value of the attribute {@link #dateCertInit}.
	 */
	public String getDateCertInit() {
		return dateCertInit;
	}

	/**
	 * Sets the value of the attribute {@link #dateCertInit}.
	 * @param dateCertInit the value for the attribute {@link #dateCertInit} to set.
	 */
	public void setDateCertInit(String dateCertInit) {
		this.dateCertInit = dateCertInit;
	}

	/**
	 * Gets the value of the attribute {@link #dateCertExpired}.
	 * @return the value of the attribute {@link #dateCertExpired}.
	 */
	public String getDateCertExpired() {
		return dateCertExpired;
	}

	/**
	 * Sets the value of the attribute {@link #dateCertExpired}.
	 * @param dateCertExpired the value for the attribute {@link #dateCertExpired} to set.
	 */
	public void setDateCertExpired(String dateCertExpired) {
		this.dateCertExpired = dateCertExpired;
	}

	/**
	 * Gets the value of the attribute {@link #idCertificado}.
	 * @return the value of the attribute {@link #idCertificado}.
	 */
	public Long getIdCertificado() {
		return idCertificado;
	}

	/**
	 * Sets the value of the attribute {@link #idCertificado}.
	 * @param idCertificado the value for the attribute {@link #idCertificado} to set.
	 */
	public void setIdCertificado(Long idCertificado) {
		this.idCertificado = idCertificado;
	}

}
