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
 * @version 1.5, 30/01/2025.
 */
package es.gob.fire.persistence.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.i18n.IPersistenceGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.datatable.CertificateDataTablesRepository;
import es.gob.fire.persistence.service.ICertificateService;
import es.gob.fire.upgrade.afirma.AfirmaConnector;
import es.gob.fire.upgrade.afirma.PlatformWsException;
import es.gob.fire.upgrade.afirma.Verify;
import es.gob.fire.upgrade.afirma.VerifyAfirmaCertificateResponse;
import es.gob.fire.upgrade.afirma.ws.WSServiceInvokerException;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.5, 30/01/2025.
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

	/** Nombre de la propiedad en la que se guarda el nombre de la aplicacion con el que debe
	 * conectarse a la plataforma @firma. */
	private static final String PROP_APPID = "afirma.appId"; //$NON-NLS-1$
	
	/**
	 * Constant that represents the afirma appId property.
	 */
	@Value("${afirma.appId}")
	private String afirmaAppId;
	
	/**
	 * Constant that represents the webservices timeout property.
	 */
	@Value("${webservices.timeout}")
	private String webServiceTimeout;
	
	/**
	 * Constant that represents the webservices endpoint property.
	 */
	@Value("${webservices.endpoint}")
	private String webServicesEndpoint;
	
	/**
	 * Constant that represents the webservices verify certificate property.
	 */
	@Value("${webservices.service.verifyCertificate}")
	private String webServiceVerifyCertificate;
	
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

			if (certificateDto.getCertBytes() != null) {

				final byte[] digest = md.digest(certificateDto.getCertBytes());
				certificateDto.setHuella(Base64.encode(digest));
				certificateDto.setCertificate(Base64.encode(certificateDto.getCertBytes()));
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
		certificate.setCertificate(certificateDto.getCertificate());
		certificate.setHuella(certificateDto.getHuella());
		
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
		certificateDto.setCertificate(certificate.getCertificate());
		certificateDto.setHuella(certificate.getHuella());
		certificateDto.setCertificateB64(certificate.getCertificate());
		
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

		X509Certificate x509Certificate = null;
		
		for (final Certificate certificate : certificates) {
			try {

				if (certificate.getCertificate() != null && !certificate.getCertificate().isEmpty()) {

					x509Certificate = (X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertificate()))); //$NON-NLS-1$
				} else {
					x509Certificate = null;
				}

			} catch (final IOException e) {
				LOGGER.error("No se ha podido leer el certificado", e); //$NON-NLS-1$
			} catch (final CertificateException e) {
				LOGGER.error("Los datos proporcionados no se corresponden con un certificado", e); //$NON-NLS-1$
			}

			java.util.Date expDatePrincipal = new java.util.Date();

			if (x509Certificate != null) {
				expDatePrincipal = x509Certificate.getNotAfter();
				final String certSubject = x509Certificate.getSubjectX500Principal().getName();
				//String cnFieldBegin = certSubject.substring(certSubject.indexOf("CN"));
				final String[] txtCert = certSubject.split(","); //$NON-NLS-1$
				certificate.setCertificate(txtCert[0] + "<br/> Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal)); //$NON-NLS-1$
			} else {
				certificate.setCertificate(""); //$NON-NLS-1$
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

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IApplicationService#obtainZipWithCertificatesApp(java.util.List<Certificate>)
	 */
	@Override
	public List<CertificateDTO> obtainAllCertificateToDTO(List<Certificate> listCertificate) {
		List<CertificateDTO> listCertificateDTO = new ArrayList<>();
		for (Certificate certificate : listCertificate) {
			CertificateDTO certificateDTO = new CertificateDTO();
			certificateDTO.setIdCertificate(certificate.getIdCertificado());
			certificateDTO.setCertificateName(certificate.getCertificateName());
			certificateDTO.setCertificate(certificate.getCertificate());
			certificateDTO.setFechaAlta(certificate.getfechaAlta());
			
			try {
				
				X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertificate()))); //$NON-NLS-1$
	            
				// Comprobamos la validez del certificado
	            try {
	            	x509Certificate.checkValidity();
	            	// El certificado sera valido
	            	certificateDTO.setStatus(Language.getResPersistenceGeneral(IPersistenceGeneral.LOG_SV001));
	            } catch (CertificateExpiredException e) {
	            	// El certificado esta caducado
	            	certificateDTO.setStatus(Language.getResPersistenceGeneral(IPersistenceGeneral.LOG_SV002));
	            } catch (CertificateNotYetValidException e) {
	            	// El certificado aun no es valido
	            	certificateDTO.setStatus(Language.getResPersistenceGeneral(IPersistenceGeneral.LOG_SV004));
	            }
	            
	            java.util.Date expDatePrincipal = x509Certificate.getNotAfter();
				final String certSubject = x509Certificate.getSubjectX500Principal().getName();
				//String cnFieldBegin = certSubject.substring(certSubject.indexOf("CN"));
				final String[] txtCert = certSubject.split(","); //$NON-NLS-1$
				certificateDTO.setCertificate(txtCert[0] + "<br/> Fecha de Caducidad=" + Utils.getStringDateFormat(expDatePrincipal)); //$NON-NLS-1$
	            
	        } catch (CertificateException | IOException e) {
	        	LOGGER.error(Language.getResPersistenceGeneral(IPersistenceGeneral.LOG_SV003) , e );
			}
			
			listCertificateDTO.add(certificateDTO);
		}
		return listCertificateDTO;
	}
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.IApplicationService#validateStatusCertificateInAfirmaWS(java.security.cert.X509Certificate)
	 */
	public VerifyAfirmaCertificateResponse validateStatusCertificateInAfirmaWS(X509Certificate x509Certificate) throws CertificateEncodingException, PlatformWsException, WSServiceInvokerException {
		// Obtenemos la conexión con AfirmaWS
		AfirmaConnector afirmaConnector = new AfirmaConnector();
		Properties config = new Properties();
		config.setProperty("afirma.appId", afirmaAppId);
		config.setProperty("webservices.timeout", webServiceTimeout);
		config.setProperty("webservices.endpoint", webServicesEndpoint);
		config.setProperty("webservices.service.verifyCertificate", webServiceVerifyCertificate);
		
		afirmaConnector.init(config);
		
		return Verify.verifyCertificate(afirmaConnector, x509Certificate, config.getProperty(PROP_APPID));
	}
}
