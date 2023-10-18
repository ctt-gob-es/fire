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
 * <b>File:</b><p>es.gob.fire.persistence.service.CertificateService.java.</p>
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>22/01/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 02/02/2022.
 */
package es.gob.fire.persistence.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.datatable.CertificateDataTablesRepository;
import es.gob.fire.persistence.service.ICertificateService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.2, 02/02/2022.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CertificateService implements ICertificateService{

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(CertificateService.class);

	/**
	 * Constant that represents the String X.509.
	 */
	private static final String X509 = "X.509"; //$NON-NLS-1$

	/**
	 * Attribute that represents the injected interface that proves CRUD operations for the persistence.
	 */
	@Autowired
	private CertificateRepository repository;


	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private CertificateDataTablesRepository dtRepository;

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#getCertificatetByCertificateId(java.lang.Long)
	 */
	@Override
	public Certificate getCertificateByCertificateId(final Long idCertificado) {
		return this.repository.findByIdCertificado(idCertificado);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#saveCertificate(es.gob.fire.persistence.entity.Certificate)
	 */
	@Override
	public Certificate saveCertificate(final Certificate certificate) {
		return this.repository.save(certificate);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#deleteCertificate(java.lang.Long)
	 */
	@Override
	@Transactional
	public void deleteCertificate(final Long IdCertificate) {
		this.repository.deleteById(IdCertificate);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#getAllCertificate()
	 */
	@Override
	public List<Certificate> getAllCertificate() {
		return this.repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#getCertificateByCertificateName(java.lang.String)
	 */
	@Override
	public Certificate getCertificateByCertificateName(final String nombre_cert) {
		return this.repository.findByCertificateName(nombre_cert);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#saveCertificate(es.gob.fire.persistence.dto.CertificateDTO)
	 */
	@Override
	public Certificate saveCertificate(final CertificateDTO certificateDto) throws IOException {

		Certificate newCertificate = null;

		// Calculamos la huella de los certificados
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$

			if (certificateDto.getCertBytes1() != null) {

				final byte[] digest = md.digest(certificateDto.getCertBytes1());
				certificateDto.setHuellaPrincipal(Base64.encode(digest));
				certificateDto.setCertPrincipal(Base64.encode(certificateDto.getCertBytes1()));
			}
			if (certificateDto.getCertBytes2() != null) {

				final byte[] digest = md.digest(certificateDto.getCertBytes2());
				certificateDto.setHuellaBackup(Base64.encode(digest));
				certificateDto.setCertBackup(Base64.encode(certificateDto.getCertBytes2()));
			}

		} catch (final NoSuchAlgorithmException e) {
			LOGGER.error("Se intenta calcular la huella de los certificados con un algoritmo no soportado: " + e); //$NON-NLS-1$
		}

		newCertificate = certificateDtoToEntity(certificateDto);
		newCertificate.setFechaAlta(new Date());

		newCertificate = this.repository.save(newCertificate);

		return newCertificate;
	}


	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#certificateDtoToEntity(es.gob.fire.persistence.dto.CertificateDTO)
	 */
	@Override
	public Certificate certificateDtoToEntity(final CertificateDTO certificateDto) {

		final Certificate certificate = new Certificate();

		certificate.setIdCertificado(certificateDto.getIdCertificate());
		certificate.setCertificateName(certificateDto.getAlias());
		certificate.setCertPrincipal(certificateDto.getCertPrincipal());
		certificate.setCertBackup(certificateDto.getCertBackup());
		certificate.setHuellaPrincipal(certificateDto.getHuellaPrincipal());
		certificate.setHuellaBackup(certificateDto.getHuellaBackup());

		return certificate;
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#certificateEntityToDtoTo(es.gob.fire.persistence.entity.Certificate)
	 */
	@Override
	public CertificateDTO certificateEntityToDto(final Certificate certificate) {

		final CertificateDTO certificateDto = new CertificateDTO();

		certificateDto.setIdCertificate(certificate.getIdCertificado());
		certificateDto.setAlias(certificate.getCertificateName());
		certificateDto.setCertPrincipal(certificate.getCertPrincipal());
		certificateDto.setCertBackup(certificate.getCertBackup());
		certificateDto.setHuellaPrincipal(certificate.getHuellaPrincipal());
		certificateDto.setHuellaBackup(certificate.getHuellaBackup());
		certificateDto.setCertPrincipalB64(certificate.getCertPrincipal());
		certificateDto.setCertBackupB64(certificate.getCertBackup());

		return certificateDto;
	}

	@Override
	public List<Certificate> getAllCertificate(final List input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataTablesOutput<Certificate> certificatesDataTable(final DataTablesInput input) {

		return this.dtRepository.findAll(input);
	}


	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#getSubjectValuesForView(java.util.List)
	 */
	@Override
	public void getSubjectValuesForView(final List<Certificate> certificates) {

		X509Certificate x509CertPrincipal = null;
		X509Certificate x509CertBackup = null;

		for (final Certificate certificate : certificates) {
			try {

				if (certificate.getCertPrincipal() != null && !certificate.getCertPrincipal().isEmpty()) {

					x509CertPrincipal = (X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertPrincipal()))); //$NON-NLS-1$
				} else {
					x509CertPrincipal = null;
				}

			} catch (final IOException e) {
				LOGGER.error("No se ha podido leer el certificado", e); //$NON-NLS-1$
			} catch (final CertificateException e) {
				LOGGER.error("Los datos proporcionados no se corresponden con un certificado", e); //$NON-NLS-1$
			}

			try {

				if (certificate.getCertBackup() != null && !certificate.getCertBackup().isEmpty()) {
					x509CertBackup = (X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertBackup()))); //$NON-NLS-1$
				} else {
					x509CertBackup = null;
				}

			} catch (final IOException e) {
				LOGGER.error("No se ha podido leer el certificado", e); //$NON-NLS-1$
			} catch (final CertificateException e) {
				LOGGER.error("Los datos proporcionados no se corresponden con un certificado", e); //$NON-NLS-1$
			}

			java.util.Date expDatePrincipal = new java.util.Date();

			if (x509CertPrincipal != null) {
				expDatePrincipal = x509CertPrincipal.getNotAfter();
				final String certSubject = x509CertPrincipal.getSubjectX500Principal().getName();
				//String cnFieldBegin = certSubject.substring(certSubject.indexOf("CN"));
				final String[] txtCert = certSubject.split(","); //$NON-NLS-1$
				certificate.setCertPrincipal(txtCert[0] + "<br/> Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal)); //$NON-NLS-1$
			} else {
				certificate.setCertPrincipal(""); //$NON-NLS-1$
			}

			if (x509CertBackup != null) {

				expDatePrincipal = x509CertBackup.getNotAfter();
				final String certSubject = x509CertBackup.getSubjectX500Principal().getName();
				//String cnFieldBegin = certSubject.substring(certSubject.indexOf("CN"));
				final String[] txtCert = certSubject.split(","); //$NON-NLS-1$
				certificate.setCertBackup(txtCert[0] + "</br> Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal)); //$NON-NLS-1$
			} else {
				certificate.setCertBackup(""); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#getFormatCertText(java.io.InputStream)
	 */
	@Override
	public String getFormatCertText(final InputStream certIs) throws CertificateException {

		X509Certificate cert = null;

		cert = (X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(certIs);

		String txtCert = null;
		if (cert != null) {
			final Date expDate = cert.getNotAfter();
			txtCert = cert.getSubjectX500Principal().getName() + ", Fecha de Caducidad=" + DateFormat.getInstance().format(expDate); //$NON-NLS-1$
		}

		String certData = ""; //$NON-NLS-1$
		if (txtCert != null) {

			final String[] datCertificate=txtCert.split(","); //$NON-NLS-1$

			for (int i = 0; i <= datCertificate.length-1; i++){
				certData += datCertificate[i] + "</br>"; //$NON-NLS-1$
			}

		}
		else {
			certData = "Error"; //$NON-NLS-1$
		}

		return certData;
	}

	@Override
	public String getCertificateText(final String certificate) {

		String certText = ""; //$NON-NLS-1$

		if (certificate != null && !certificate.isEmpty()) {
			try (final InputStream certIs = new ByteArrayInputStream(Base64.decode(certificate));) {

				certText = getFormatCertText(certIs);

			} catch (final IOException e) {
				LOGGER.error("No se ha podido leer el certificado", e); //$NON-NLS-1$
			} catch (final CertificateException e) {
				LOGGER.error("Los datos proporcionados no se corresponden con un certificado", e); //$NON-NLS-1$
			}
		}

		return certText;

	}

}
