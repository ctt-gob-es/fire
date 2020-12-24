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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.dto.CertificateEditDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.datatable.CertificateDataTablesRepository;
import es.gob.fire.persistence.service.ICertificateService;


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
	private static final String X509 = "X.509";
	
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
	public Certificate getCertificateByCertificateId(Long idCertificado) {
		return repository.findByIdCertificado(idCertificado);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#saveCertificate(es.gob.fire.persistence.entity.Certificate)
	 */
	@Override
	public Certificate saveCertificate(Certificate certificate) {
		return repository.save(certificate);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#deleteCertificate(java.lang.Long)
	 */
	@Override
	@Transactional
	public void deleteCertificate(Long IdCertificate) {
		repository.deleteById(IdCertificate);
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#getAllCertificate()
	 */
	@Override
	public List<Certificate> getAllCertificate() {
		return repository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#getCertificateByCertificateName(java.lang.String)
	 */
	@Override
	public Certificate getCertificateByCertificateName(final String nombre_cert) {
		return repository.findByCertificateName(nombre_cert);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#saveCertificate(es.gob.fire.persistence.dto.CertificateDTO)
	 */
	@Override
	public Certificate saveCertificate(CertificateDTO certificateDto) throws IOException {
		
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
			
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Se intenta calcular la huella de los certificados con un algoritmo no soportado: " + e);
		}
		
		newCertificate = certificateDtoToEntity(certificateDto);
		newCertificate.setfechaAlta(new Date());
		
		newCertificate = repository.save(newCertificate);

		return newCertificate;
	}
		
	
	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#certificateDtoToEntity(es.gob.fire.persistence.dto.CertificateDTO)
	 */
	public Certificate certificateDtoToEntity(CertificateDTO certificateDto) {
		
		Certificate certificate = new Certificate();
		
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
	public CertificateDTO certificateEntityToDto(Certificate certificate) {
		
		CertificateDTO certificateDto = new CertificateDTO();
		
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
	public Certificate updateCertificate(CertificateEditDTO certificateEditDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Certificate> getAllCertificate(List input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataTablesOutput<Certificate> certificatesDataTable(DataTablesInput input) {
		
		return dtRepository.findAll(input);
	}


	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#getSubjectValuesForView(java.util.List)
	 */
	@Override
	public void getSubjectValuesForView(List<Certificate> certificates) {
		
		X509Certificate x509CertPrincipal = null;
		X509Certificate x509CertBackup = null;
		
		
		for (Certificate certificate : certificates) {
			try {
				
				if (certificate.getCertPrincipal() != null && !certificate.getCertPrincipal().isEmpty()) {
				
					x509CertPrincipal = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertPrincipal())));
				} else {
					x509CertPrincipal = null;
				}
				
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				
				if (certificate.getCertBackup() != null && !certificate.getCertBackup().isEmpty()) {
					x509CertBackup = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertBackup())));
				} else {
					x509CertBackup = null;
				}
				
				
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			java.util.Date expDatePrincipal = new java.util.Date();
			
			if (x509CertPrincipal != null) {
				expDatePrincipal = x509CertPrincipal.getNotAfter();
				String certSubject = x509CertPrincipal.getSubjectX500Principal().getName();
				String cnFieldBegin = certSubject.substring(certSubject.indexOf("CN"));
				String[] txtCert = cnFieldBegin.split(",");
				certificate.setCertPrincipal(txtCert[0] + ", Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal));
			} else {
				certificate.setCertPrincipal("");
			}
			
			if (x509CertBackup != null) {
				
				expDatePrincipal = x509CertBackup.getNotAfter();
				String certSubject = x509CertBackup.getSubjectX500Principal().getName();
				String cnFieldBegin = certSubject.substring(certSubject.indexOf("CN"));
				String[] txtCert = cnFieldBegin.split(",");
				certificate.setCertBackup(txtCert[0] + ", Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal));				
			} else {
				certificate.setCertBackup("");
			}
		}
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#getFormatCertText(java.io.InputStream)
	 */
	@Override
	public String getFormatCertText(InputStream certIs) throws CertificateException {
		
		X509Certificate cert = null;
		
		cert = (X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(certIs);
		
		String txtCert = null;
		if (cert != null) {
			final Date expDate = cert.getNotAfter();
			txtCert = cert.getSubjectX500Principal().getName() + ", Fecha de Caducidad=" + DateFormat.getInstance().format(expDate);
		}

		String certData = "";
		if (txtCert != null) {
			
			final String[] datCertificate=txtCert.split(",");
			
			for (int i = 0; i <= datCertificate.length-1; i++){
				certData += datCertificate[i] + "</br>";
			}
			
		}
		else {
			certData = "Error";
		}
		
		return certData;
	}

	
}
