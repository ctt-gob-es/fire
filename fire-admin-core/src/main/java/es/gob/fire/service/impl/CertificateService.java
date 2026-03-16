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
  * <b>Project:</b><p>Application for signing documents of FIRe system</p>
 * <b>Date:</b><p>22/01/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.7, 13/02/2025.
 */
package es.gob.fire.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.utils.Base64;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.Utils;
import es.gob.fire.crypto.aes.AESCipher;
import es.gob.fire.exceptions.AfirmaConfigurationException;
import es.gob.fire.exceptions.FireException;
import es.gob.fire.i18n.IPersistenceGeneral;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.CertificateDTO;
import es.gob.fire.persistence.entity.Certificate;
import es.gob.fire.persistence.entity.ServerAfirma;
import es.gob.fire.persistence.repository.CertificateRepository;
import es.gob.fire.persistence.repository.datatable.CertificateDataTablesRepository;
import es.gob.fire.service.ICertificateService;
import es.gob.fire.service.IServerAfirmaService;
import es.gob.fire.upgrade.afirma.AfirmaConnector;
import es.gob.fire.upgrade.afirma.PlatformWsException;
import es.gob.fire.upgrade.afirma.Verify;
import es.gob.fire.upgrade.afirma.VerifyAfirmaCertificateResponse;
import es.gob.fire.upgrade.afirma.ws.WSServiceInvokerException;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Application for signing documents of FIRe system.</p>
 * @version 1.7, 13/02/2025.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CertificateService implements ICertificateService{

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(CertificateService.class);

	private static final boolean DEBUG = Boolean.getBoolean("debug"); //$NON-NLS-1$

	/**
	 * Constant that represents the String X.509.
	 */
	public static final String X509 = "X.509"; //$NON-NLS-1$

	/** Nombre de la propiedad en la que se guarda el nombre de la aplicacion con el que debe
	 * conectarse a la plataforma @firma. */
	private static final String PROP_APPID = "afirma.appId"; //$NON-NLS-1$

	/**
	 * Constant that represents the name of the file that stores the keystore versions.
	 */
	public static final String KEYSTORE_VERSIONS_FILENAME = "keystoreVersions.properties"; //$NON-NLS-1$

	/**
	 * Constant that represents the webservices verify certificate property.
	 */
	@Value("${webservices.service.verifyCertificate}")
	private String webServiceVerifyCertificate;

	/**
     * Attribute that path to the file which stores the versions of the keystores saved in file.
     */
    @Value("${keystore.versions.path}")
    private String keystoreVersionsPath;

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

	@Autowired
	private IServerAfirmaService iServerAfirmaService;

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.services.ICertificateService#getCertificatetByCertificateId(java.lang.Long)
	 */
	@Override
	public Certificate getCertificateByCertificateId(final Long idCertificado) {
		return this.repository.findByIdCertificado(idCertificado);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.services.ICertificateService#updateCertificateFromTaskValidation(es.gob.fire.persistence.entity.Certificate)
	 */
	@Override
	public Certificate saveCertificate(final Certificate certificate) {
		return this.repository.save(certificate);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.services.ICertificateService#deleteCertificate(java.lang.Long)
	 */
	@Override
	@Transactional
	public void deleteCertificate(final Long IdCertificate) {
		this.repository.deleteById(IdCertificate);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.services.ICertificateService#getAllCertificate()
	 */
	@Override
	public List<Certificate> getAllCertificate() {

		final List<Certificate> certificates = this.repository.findAll();

		for (final Certificate cert : certificates) {
			if (cert.getFechaInicio() == null || cert.getFechaCaducidad() == null || cert.getSubject() == null || cert.getSubject().isEmpty()) {
				try {
					// Decodificar el certificado desde Base64
					final byte[] certBytes = Base64.decode(cert.getCertificate());

					// Convertir a X509Certificate
					final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
					final ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
					final X509Certificate x509Certificate = (X509Certificate) certFactory.generateCertificate(bais);

					// Establecer fechas si son nulas
					if (cert.getFechaInicio() == null) {
						cert.setFechaInicio(x509Certificate.getNotBefore());
					}
					if (cert.getFechaCaducidad() == null) {
						cert.setFechaCaducidad(x509Certificate.getNotAfter());
					}
					// Establecer subject si es nulo o vacio
					if (cert.getSubject() == null || cert.getSubject().isEmpty()) {
						final String certSubject = x509Certificate.getSubjectX500Principal().getName();
						final String[] subjectParts = certSubject.split(",");
						cert.setSubject(subjectParts[0]);
					}

					// Persistir certificado actualizado
					this.repository.save(cert);

				} catch (CertificateException | IllegalArgumentException | IOException e) {
					// Loguea el error pero no interrumpe el proceso para otros certificados
					LOGGER.error("Error al procesar fechas del certificado con ID: " + cert.getIdCertificado(), e);
				}
			}
		}

		return certificates;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.services.ICertificateService#getCertificateByCertificateName(java.lang.String)
	 */
	@Override
	public Certificate getCertificateByCertificateName(final String nombre_cert) {
		return this.repository.findByCertificateName(nombre_cert);
	}

	/* (non-Javadoc)
	 * @see es.gob.fire.persistence.service.ICertificateService#saveCertificate(es.gob.fire.persistence.dto.CertificateDTO)
	 */
	@Override
	public Certificate saveCertificate(final CertificateDTO certificateDto, final X509Certificate x509Certificate) throws FireException {

		Certificate newCertificate = null;

		// Calculamos la huella de los certificados
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$

			if (certificateDto.getCertBytes() != null) {
				final byte[] digest = md.digest(certificateDto.getCertBytes());
	            final String huella = Base64.encode(digest);

	            // Check if certificate with same fingerprint exists
	            if (this.repository.findByHuella(huella).isPresent()) {
	                throw new FireException("Certificado con la misma huella ya existente en base de datos");
	            }

	            certificateDto.setHuella(huella);
	            certificateDto.setCertificate(Base64.encode(certificateDto.getCertBytes()));
			}

		} catch (final NoSuchAlgorithmException e) {
			LOGGER.error("Se intenta calcular la huella de los certificados con un algoritmo no soportado: " + e); //$NON-NLS-1$
		}

		newCertificate = certificateDtoToEntity(certificateDto);
		newCertificate.setFechaAlta(new Date());
		newCertificate.setFechaInicio(x509Certificate.getNotBefore());
		newCertificate.setFechaCaducidad(x509Certificate.getNotAfter());

		final String certSubject = x509Certificate.getSubjectX500Principal().getName();
		final String[] txtCert = certSubject.split(",");
		newCertificate.setSubject(txtCert[0]);

		newCertificate = this.repository.save(newCertificate);

		return newCertificate;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.service.ICertificateService#updateCertificateFromTaskValidation(es.gob.fire.persistence.entity.Certificate, java.security.cert.X509Certificate)
	 */
	@Override
	public Certificate updateCertificateFromTaskValidation(final Certificate certificate, final X509Certificate x509Certificate)
	        throws IOException, CertificateEncodingException {

	    // Extraer los bytes y calcular la huella digital
	    final byte[] certBytes = x509Certificate.getEncoded();
	    final String certBase64 = Base64.encode(certBytes);

	    MessageDigest md;
	    String fingerprint = null;
	    try {
	        md = MessageDigest.getInstance("SHA-1");
	        fingerprint = Base64.encode(md.digest(certBytes));
	    } catch (final NoSuchAlgorithmException e) {
	        LOGGER.error("Se intenta calcular la huella de los certificados con un algoritmo no soportado: " + e);
	    }

	    // Extraer otros campos del certificado
	    final String subject = x509Certificate.getSubjectX500Principal().getName();
	    final Date notBefore = x509Certificate.getNotBefore();
	    final Date notAfter = x509Certificate.getNotAfter();

	    // Comparacion segura de valores previos con los nuevos
	    final boolean isUpdated =
	        certificate.getCertificate() == null || !certificate.getCertificate().equals(certBase64) ||
	        certificate.getHuella() == null || !certificate.getHuella().equals(fingerprint) ||
	        certificate.getSubject() == null || !certificate.getSubject().equals(subject) ||
	        certificate.getFechaInicio() == null || certificate.getFechaInicio().getTime() != notBefore.getTime() ||
	        certificate.getFechaCaducidad() == null || certificate.getFechaCaducidad().getTime() != notAfter.getTime();

	    // Si hubo cambios, actualizamos los valores y la fecha de alta
	    if (isUpdated) {
	        certificate.setCertificate(certBase64);
	        certificate.setHuella(fingerprint);
	        certificate.setSubject(subject);
	        certificate.setFechaInicio(notBefore);
	        certificate.setFechaCaducidad(notAfter);
	        certificate.setFechaAlta(new Date());
	    }

	    return this.repository.save(certificate);
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

					x509Certificate = (X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(new ByteArrayInputStream(Base64.decode(certificate.getCertificate())));
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
			txtCert = cert.getSubjectX500Principal().getName() + ", Fecha de Caducidad=" + Utils.getStringDateFormat(expDate); //$NON-NLS-1$
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
	public List<CertificateDTO> obtainAllCertificateToDTO(final List<Certificate> listCertificate) {
		final List<CertificateDTO> listCertificateDTO = new ArrayList<>();
		for (final Certificate certificate : listCertificate) {
			final CertificateDTO certificateDTO = new CertificateDTO();
			certificateDTO.setIdCertificate(certificate.getIdCertificado());
			certificateDTO.setCertificateName(certificate.getCertificateName());
			certificateDTO.setCertificate(certificate.getCertificate());
			certificateDTO.setFechaAlta(certificate.getfechaAlta());

			final java.util.Date expDate = certificate.getFechaCaducidad();
			final java.util.Date startDate = certificate.getFechaInicio();
			final java.util.Date dateNow = Calendar.getInstance().getTime();

			if (dateNow.before(startDate)) {
			    // El certificado aún no es valido
			    certificateDTO.setStatus(Language.getResPersistenceGeneral(IPersistenceGeneral.LOG_SV004));
			} else if (dateNow.after(expDate)) {
			    // El certificado esta caducado
			    certificateDTO.setStatus(Language.getResPersistenceGeneral(IPersistenceGeneral.LOG_SV002));
			} else {
			    // El certificado es valido
			    certificateDTO.setStatus(Language.getResPersistenceGeneral(IPersistenceGeneral.LOG_SV001));
			}

			certificateDTO.setCertificate(certificate.getSubject() + "<br/> Fecha de Caducidad=" + Utils.getStringDateFormat(expDate));

			listCertificateDTO.add(certificateDTO);
		}
		return listCertificateDTO;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.services.ICertificateService#validateStatusCertificateInAfirmaWS(final X509Certificate x509Certificate)
	 */
	@Override
	public VerifyAfirmaCertificateResponse validateStatusCertificateInAfirmaWS(final X509Certificate x509Certificate)
	        throws CertificateEncodingException, PlatformWsException, WSServiceInvokerException, AfirmaConfigurationException {
	    // 1) Cargar configuracion de servidor desde BD

	    final ServerAfirma sa = this.iServerAfirmaService.obtainServerAfirmaService(NumberConstants.NUM_1_LONG);
	    if (sa == null) {
	        LOGGER.error("[validateStatusCertificateInAfirmaWS] Configuracion ServerAfirma no encontrada (id=1)");
	        throw new AfirmaConfigurationException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_MC015));
	    }


	    if (DEBUG) {
	    	return new VerifyAfirmaCertificateResponse("Success", "Definitive", "Resultado dummy de exito");
	    }

	    final Long authType = sa.getcAuthenticationType() != null
	            ? sa.getcAuthenticationType().getIdAuthenticationType()
	            : null;
	    final Long AUTH_UT  = NumberConstants.NUM_1_LONG; // UsernameToken
	    final Long AUTH_BST = NumberConstants.NUM_2_LONG; // BinarySecurityToken

	    // 2) Resolver directorio base para ficheros versionados (multiplataforma)
	    Path baseDir = null;
	    try {
	        baseDir = ensureDirectoryExistsCrossPlatform(this.keystoreVersionsPath);
	        LOGGER.debug("[validateStatusCertificateInAfirmaWS] Base dir: {}", baseDir);
	    } catch (final IOException e) {
	        LOGGER.warn("[validateStatusCertificateInAfirmaWS] keystore.versions.path inusable: '{}'. Se usara directorio temporal.", this.keystoreVersionsPath);
	        try {
	            baseDir = ensureTempFallbackDirectory("fire-afirma");
	            LOGGER.debug("[validateStatusCertificateInAfirmaWS] Base dir temporal: {}", baseDir);
	        } catch (final IOException ex) {
	            LOGGER.error("[validateStatusCertificateInAfirmaWS] No se pudo crear el directorio temporal de respaldo", ex);
	        }
	    }

	    // 3) Si BST, sincronizar fichero local de versiones (crear/actualizar si es necesario)
	    if (safeEquals(authType, AUTH_BST)) {
	        try {
	            if (baseDir == null) {
	                throw new IOException("baseDir es null (no se puede sincronizar versiones).");
	            }
	            final Path versionsFile = baseDir.resolve(KEYSTORE_VERSIONS_FILENAME).toAbsolutePath().normalize();
	            final Properties localProps = new Properties();

	            if (Files.exists(versionsFile)) {
	                try (InputStream in = Files.newInputStream(versionsFile)) {
	                    localProps.load(in);
	                }
	            }

	            final Map<String, Long> localMap = propsToVersionMap(localProps);
	            final Long dbKs = sa.getKeystoreVersion();
	            final Long dbTs = sa.getTruststoreVersion();
	            final Long dbAu = sa.getAuthenticationVersion();

	            final boolean ksOk = safeEquals(dbKs, localMap.get("keystoreVersion"));
	            final boolean tsOk = safeEquals(dbTs, localMap.get("truststoreVersion"));
	            final boolean auOk = safeEquals(dbAu, localMap.get("authenticationVersion"));

	            if (!(ksOk && tsOk && auOk) || !Files.exists(versionsFile)) {
	                final Properties dbProps = new Properties();
	                dbProps.setProperty("keystoreVersion",       toStringOrEmpty(dbKs));
	                dbProps.setProperty("truststoreVersion",     toStringOrEmpty(dbTs));
	                dbProps.setProperty("authenticationVersion", toStringOrEmpty(dbAu));
	                dbProps.setProperty("lastSyncEpochMillis",   String.valueOf(System.currentTimeMillis()));
	                Files.createDirectories(versionsFile.getParent());
	                try (OutputStream out = Files.newOutputStream(versionsFile,
	                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
	                    dbProps.store(out, "FIRe Afirma local versions (synced from DB)");
	                }
	                LOGGER.debug("[validateStatusCertificateInAfirmaWS] Fichero de versiones sincronizado: {}", versionsFile);
	            }
	        } catch (final IOException ioEx) {
	            LOGGER.warn("[validateStatusCertificateInAfirmaWS] No se pudo sincronizar el fichero de versiones. Se continua sin sincronizacion. path={}",
	                    this.keystoreVersionsPath, ioEx);
	        }
	    }

	    // 4) Construir configuracion común del conector
	    final Properties cfg = new Properties();
	    cfg.setProperty("afirma.appId", sa.getNameApp());
	    cfg.setProperty("webservices.timeout", String.valueOf(sa.getTimeout()));
	    cfg.setProperty("webservices.endpoint", sa.getUrlServer());
	    cfg.setProperty("webservices.service.verifyCertificate", this.webServiceVerifyCertificate);

	    // 5) Autorizacion / stores en funcion del método de autenticacion
	    if (safeEquals(authType, AUTH_BST)) {
	        cfg.setProperty("webservices.authorization.method", "BinarySecurityToken");
	        LOGGER.info("[validateStatusCertificateInAfirmaWS] Auth: BST");

	        // --- Keystore ---
	        if (sa.getKsBlob() != null && sa.getKsBlob().length > 0 && baseDir != null) {
	            try {
	                final String ksType = defaultString(sa.getKsType(), "PKCS12");
	                final Long   ksVer  = firstNonNull(sa.getKeystoreVersion(), 0L);
	                final Path   ksPath = resolveVersionedPath(baseDir, "client-keystore", ksVer, ksType);

	                final Path effectiveKsPath = materializeIfMissing(sa.getKsBlob(), ksPath);
	                if (effectiveKsPath != null) {
	                    cfg.setProperty("webservices.authorization.ks.path", effectiveKsPath.toString());
	                    cfg.setProperty("webservices.authorization.ks.type", ksType);

	                    cleanupOlderVersionedFiles(baseDir, "client-keystore", ksVer);

	                    if (sa.getKsPassword() != null) {
	                        final String dec = AESCipher.getInstance().decryptMessageBC(sa.getKsPassword());
	                        cfg.setProperty("webservices.authorization.ks.password", dec);
	                    }
	                    if (!isBlank(sa.getKsCertAlias())) {
	                        cfg.setProperty("webservices.authorization.ks.cert.alias", sa.getKsCertAlias().trim());
	                    }
	                    if (sa.getKsCertPassword() != null) {
	                        final String dec = AESCipher.getInstance().decryptMessageBC(sa.getKsCertPassword());
	                        cfg.setProperty("webservices.authorization.ks.cert.password", dec);
	                    }
	                }
	            } catch (final Exception ex) {
	                LOGGER.error("[validateStatusCertificateInAfirmaWS] Keystore BST no materializado/configurado", ex);
	            }
	        }

	        // --- TLS truststore ---
	        if (sa.getTruststoreBlob() != null && sa.getTruststoreBlob().length > 0 && baseDir != null) {
	            try {
	                final String tsType = defaultString(sa.getTruststoreType(), "JKS");
	                final Long   tsVer  = firstNonNull(sa.getTruststoreVersion(), 0L);
	                final Path   tsPath = resolveVersionedPath(baseDir, "tls-truststore", tsVer, tsType);

	                final Path effectiveTsPath = materializeIfMissing(sa.getTruststoreBlob(), tsPath);
	                if (effectiveTsPath != null) {
	                    cfg.setProperty("com.trustedstore.path", effectiveTsPath.toString());
	                    if (sa.getTruststorePassword() != null) {
	                        final String dec = AESCipher.getInstance().decryptMessageBC(sa.getTruststorePassword());
	                        cfg.setProperty("com.trustedstore.password", dec);
	                    }
	                    cfg.setProperty("com.trustedstore.type", tsType);

	                    cleanupOlderVersionedFiles(baseDir, "tls-truststore", tsVer);
	                }
	            } catch (final Exception ex) {
	                LOGGER.warn("[validateStatusCertificateInAfirmaWS] TLS truststore no aplicado; se usara el del JRE por defecto", ex);
	            }
	        }

	        // --- Auth truststore ---
	        final boolean hasAuthTs = sa.getAuthTsBlob() != null && sa.getAuthTsBlob().length > 0;
	        if (hasAuthTs && baseDir != null) {
	            try {
	                final String atType = defaultString(sa.getAuthTsType(), "JKS");
	                final Long   atVer  = firstNonNull(sa.getAuthenticationVersion(), 0L);
	                final Path   atPath = resolveVersionedPath(baseDir, "auth-truststore", atVer, atType);

	                final Path effectiveAtPath = materializeIfMissing(sa.getAuthTsBlob(), atPath);
	                if (effectiveAtPath != null) {
	                    cfg.setProperty("webservices.authentication.ts.path", effectiveAtPath.toString());
	                    if (sa.getAuthTsPassword() != null) {
	                        final String dec = AESCipher.getInstance().decryptMessageBC(sa.getAuthTsPassword());
	                        cfg.setProperty("webservices.authentication.ts.password", dec);
	                    }
	                    cfg.setProperty("webservices.authentication.ts.type", atType);
	                    if (!isBlank(sa.getAuthCertAlias())) {
	                        cfg.setProperty("webservices.authentication.cert.alias", sa.getAuthCertAlias().trim());
	                    }

	                    cleanupOlderVersionedFiles(baseDir, "auth-truststore", atVer);
	                } else {
	                    cfg.remove("webservices.authentication.ts.path");
	                    cfg.remove("webservices.authentication.ts.password");
	                    cfg.remove("webservices.authentication.ts.type");
	                    cfg.remove("webservices.authentication.cert.alias");
	                }
	            } catch (final Exception ex) {
	                cfg.remove("webservices.authentication.ts.path");
	                cfg.remove("webservices.authentication.ts.password");
	                cfg.remove("webservices.authentication.ts.type");
	                cfg.remove("webservices.authentication.cert.alias");
	                LOGGER.warn("[validateStatusCertificateInAfirmaWS] Auth truststore no aplicado; se usaran valores por defecto", ex);
	            }
	        } else {
	            cfg.remove("webservices.authentication.ts.path");
	            cfg.remove("webservices.authentication.ts.password");
	            cfg.remove("webservices.authentication.ts.type");
	            cfg.remove("webservices.authentication.cert.alias");
	        }

	    } else if (safeEquals(authType, AUTH_UT)) {
	        cfg.setProperty("webservices.authorization.method", "UsernameToken");
	        LOGGER.info("[validateStatusCertificateInAfirmaWS] Auth: UsernameToken");

	        if (!isBlank(sa.getUser())) {
	            cfg.setProperty("webservices.authorization.user.name", sa.getUser().trim());
	        }
	        if (sa.getPassword() != null) {
	            try {
	                final String dec = AESCipher.getInstance().decryptMessageBC(sa.getPassword());
	                cfg.setProperty("webservices.authorization.user.password", dec);
	            } catch (final Exception ex) {
	                LOGGER.warn("[validateStatusCertificateInAfirmaWS] No se pudo descifrar la password de UsernameToken", ex);
	            }
	        }
	    } else {
	        cfg.setProperty("webservices.authorization.method", "none");
	        LOGGER.info("[validateStatusCertificateInAfirmaWS] Auth: NONE");
	    }

	    // Volcado de config en LOG
	    //logFullWsConfig(cfg);

	    // Preflight (solo relevante para BST)

	    if (safeEquals(authType, AUTH_BST)) {
	    	try {
		        checkPropertiesForBstKeystore(cfg);
		        LOGGER.debug("[BST Check Properties] OK");
		    } catch (final Exception pfEx) {
		        LOGGER.error("[BST Check Properties] Keystore/alias/password no validos o no utilizables", pfEx);
		        throw new WSServiceInvokerException("Fallo el preflight del keystore BST", pfEx);
		    }
	    }

	    // 6) Inicializar conector y realizar la llamada
	    final AfirmaConnector afirmaConnector = new AfirmaConnector();
	    afirmaConnector.init(cfg);

	    final VerifyAfirmaCertificateResponse resp =
	            Verify.verifyCertificate(afirmaConnector, x509Certificate, cfg.getProperty(PROP_APPID));

	    LOGGER.info("[validateStatusCertificateInAfirmaWS] Resultado verifyCertificate: major={}, minor={}, desc={}",
	            resp != null ? resp.getMajorCode() : "null",
	            resp != null ? resp.getMinorCode() : "null",
	            resp != null ? resp.getDescription() : "null");

	    return resp;
	}

	/**
	 * Writes the given bytes to the target path only if the file does not already exist.
	 *
	 * Ensures parent directories exist, writes atomically (TRUNCATE_EXISTING),
	 * and verifies the resulting file is readable across platforms.
	 *
	 * @param bytes      binary content to write.
	 * @param targetPath destination path (typically versioned).
	 * @return the effective {@link Path} (existing or newly materialized), or {@code null} if nothing to write.
	 * @throws IOException if directory creation, file write, or readability check fails.
	 */
	private static Path materializeIfMissing(final byte[] bytes, final Path targetPath) throws IOException {
	    if (bytes == null || bytes.length == 0 || targetPath == null) {
	        LOGGER.debug("[materializeIfMissing] Nothing to write (bytes null/empty or targetPath null).");
	        return null;
	    }
	    final Path abs = targetPath.toAbsolutePath().normalize();
	    if (Files.exists(abs)) {
	        LOGGER.debug("[materializeIfMissing] File already exists (no write): {}", abs);
	        return abs;
	    }
	    Files.createDirectories(abs.getParent());
	    try (OutputStream out = Files.newOutputStream(abs,
	            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
	        out.write(bytes);
	    }
	    // Verify readable after write (Windows/Linux friendly)
	    if (!Files.isReadable(abs)) {
	        throw new IOException("File written but not readable: " + abs);
	    }
	    LOGGER.info("[materializeIfMissing] File materialized: {}", abs);
	    return abs;
	}

	/**
	 * Builds an absolute, normalized, versioned file path using a base directory, a logical prefix,
	 * a numeric version, and the store type to determine the file extension.
	 *
	 * @param baseDir base directory where the file will reside.
	 * @param prefix  logical resource prefix (e.g., "client-keystore").
	 * @param version version number to encode in the file name.
	 * @param type    store type hint (e.g., "JKS", "PKCS12").
	 * @return a normalized absolute {@link Path} to the versioned file.
	 */

	private static Path resolveVersionedPath(final Path baseDir, final String prefix, final Long version, final String type) {
	    final String ext = guessExtensionByType(type);
	    final long v = version != null ? version.longValue() : 0L;
	    final Path p = baseDir.resolve(prefix + "-v" + v + ext).toAbsolutePath().normalize();
	    LOGGER.debug("[resolveVersionedPath] Resolved path: {}", p);
	    return p;
	}

	/**
	 * Infers a file extension from a keystore/truststore type.
	 * Returns ".jks" for JKS (case-insensitive), ".p12" otherwise.
	 *
	 * @param type keystore/truststore type (e.g., "JKS", "PKCS12"); can be {@code null}.
	 * @return ".jks" if type indicates JKS, otherwise ".p12".
	 */
	private static String guessExtensionByType(final String type) {
	    // Accepts JKS/PKCS12/PKCS#12 variations; defaults to .p12 for non-JKS
	    if (type == null) {
			return ".p12";
		}
	    final String t = type.trim();
	    return "JKS".equalsIgnoreCase(t) ? ".jks" : ".p12";
	}

	/**
	 * Ensures the given directory path exists and is usable across platforms.
	 * Expands '~' to the user home when present, creates directories if needed,
	 * and validates read/write access.
	 *
	 * @param dirPath configured directory path; may include '~'.
	 * @return an absolute, normalized, existing, readable and writable {@link Path}.
	 * @throws IOException if the path is invalid, not a directory, or lacks required permissions.
	 */
	private static Path ensureDirectoryExistsCrossPlatform(final String dirPath) throws IOException {
	    if (dirPath == null || dirPath.trim().isEmpty()) {
	        throw new IOException("keystore.versions.path is empty or null");
	    }
	    String expanded = dirPath.trim();

	    // Support '~' home on Unix-like systems (and works on Windows if user.home is set)
	    if (expanded.startsWith("~")) {
	        final String home = System.getProperty("user.home");
	        if (home != null && !home.isEmpty()) {
	            expanded = home + expanded.substring(1);
	        }
	    }

	    final Path dir = Paths.get(expanded).toAbsolutePath().normalize();
	    Files.createDirectories(dir);

	    if (!Files.isDirectory(dir)) {
	        throw new IOException("Path is not a directory: " + dir);
	    }
	    if (!Files.isWritable(dir)) {
	        throw new IOException("Directory not writable: " + dir);
	    }
	    if (!Files.isReadable(dir)) {
	        throw new IOException("Directory not readable: " + dir);
	    }

	    LOGGER.debug("[ensureDirectoryExistsCrossPlatform] Directory ready: {}", dir);
	    return dir;
	}

	/**
	 * Creates a temporary fallback directory with the provided prefix and returns its absolute,
	 * normalized path.
	 *
	 * @param prefix prefix to use for the temporary directory name.
	 * @return the created temporary directory {@link Path}.
	 * @throws IOException if the temporary directory cannot be created.
	 */
	private static Path ensureTempFallbackDirectory(final String prefix) throws IOException {
	    final Path tmp = Files.createTempDirectory(prefix + "-");
	    final Path dir = tmp.toAbsolutePath().normalize();
	    LOGGER.warn("[ensureTempFallbackDirectory] Using temporary directory: {}", dir);
	    return dir;
	}

	/**
	 * Converts a {@link Properties} bag into a map of version numbers used by local caches.
	 *
	 * Expected keys: "keystoreVersion", "truststoreVersion", "authenticationVersion".
	 * Missing or non-numeric values are converted to {@code null}.
	 *
	 * @param props source properties.
	 * @return a mutable {@link Map} with version names mapped to {@link Long} values (nullable).
	 */
	private static Map<String, Long> propsToVersionMap(final Properties props) {
	    final Map<String, Long> map = new HashMap<>();
	    map.put("keystoreVersion",       parseLongOrNull(props.getProperty("keystoreVersion")));
	    map.put("truststoreVersion",     parseLongOrNull(props.getProperty("truststoreVersion")));
	    map.put("authenticationVersion", parseLongOrNull(props.getProperty("authenticationVersion")));
	    return map;
	}

	/**
	 * Parses a {@link Long} from the given string, tolerating {@code null} and blank input.
	 *
	 * @param v textual value to parse.
	 * @return parsed {@link Long} or {@code null} if input is null/blank or not a valid number.
	 */
	private static Long parseLongOrNull(final String v) {
	    if (v == null) {
			return null;
		}
	    final String t = v.trim();
	    if (t.isEmpty()) {
			return null;
		}
	    try { return Long.valueOf(t); }
	    catch (final NumberFormatException ex) { return null; }
	}

	/**
	 * Null-safe equality comparison for {@link Long} values.
	 *
	 * @param a left value.
	 * @param b right value.
	 * @return {@code true} if both are equal (including both {@code null}); otherwise {@code false}.
	 */
	private static boolean safeEquals(final Long a, final Long b) {
		return java.util.Objects.equals(a, b);
	}

	/**
	 * Returns the trimmed input or a default when the input is {@code null} or blank.
	 *
	 * @param v   input string (nullable).
	 * @param def default value to return when input is blank.
	 * @return trimmed input or {@code def} if blank.
	 */
	private static String defaultString(final String v, final String def) {
		return v == null || v.trim().isEmpty() ? def : v.trim();
	}

	/**
	 * Checks whether a string is {@code null} or contains only whitespace.
	 *
	 * @param s input string.
	 * @return {@code true} if {@code s} is null or blank; otherwise {@code false}.
	 */
	private static boolean isBlank(final String s) {
		return s == null || s.trim().isEmpty();
	}

	/**
	 * Returns the first non-null {@link Long} between two candidates.
	 *
	 * @param a first candidate.
	 * @param b fallback candidate.
	 * @return {@code a} if not null; otherwise {@code b}.
	 */
	private static Long firstNonNull(final Long a, final Long b) {
		return a != null ? a : b;
	}

	/**
	 * Converts a {@link Long} to its decimal string representation or returns an empty string when null.
	 *
	 * @param v input value.
	 * @return stringified value, or "" if {@code v} is {@code null}.
	 */
	private static String toStringOrEmpty(final Long v) {
		return v == null ? "" : String.valueOf(v);
	}

	/**
	 * Performs a validation for BST (BinarySecurityToken) keystore usage.
	 *
	 * Validates keystore path, type, and passwords, loads the keystore, resolves a usable
	 * private key alias (autodetects if missing), checks the certificate, and executes a
	 * real signature to ensure the key/provider are functional. May update the config with
	 * an autodetected alias.
	 *
	 * @param cfg effective configuration properties (expects ks path/type/password, optional alias/key password).
	 * @throws Exception if any validation fails (missing config, unreadable keystore, bad passwords, no private key, etc.).
	 */
	private static void checkPropertiesForBstKeystore(final Properties cfg) throws Exception {
	    final String ksPathStr   = cfg.getProperty("webservices.authorization.ks.path");
	    final String ksType      = defaultString(cfg.getProperty("webservices.authorization.ks.type"), "PKCS12");
	    final String ksPassStr   = cfg.getProperty("webservices.authorization.ks.password");
	    String       alias       = cfg.getProperty("webservices.authorization.ks.cert.alias");
	    final String keyPassStr  = defaultString(cfg.getProperty("webservices.authorization.ks.cert.password"), ksPassStr);

	    if (isBlank(ksPathStr) || isBlank(ksType) || isBlank(ksPassStr)) {
	        throw new IllegalStateException("Falta configuracion del keystore (path/type/password).");
	    }

	    final Path ksPath = Paths.get(ksPathStr).toAbsolutePath().normalize();
	    LOGGER.debug("[BST Check Properties] Keystore path={}, type={}", ksPath, ksType);

	    if (!Files.exists(ksPath)) {
	        throw new IOException("El fichero del almacen no existe: " + ksPath);
	    }
	    if (!Files.isReadable(ksPath)) {
	        throw new IOException("El fichero del almacen no se puede leer: " + ksPath);
	    }

	    final char[] ksPassword  = ksPassStr.toCharArray();
	    final char[] keyPassword = keyPassStr.toCharArray();

	    final java.security.KeyStore ks = java.security.KeyStore.getInstance(ksType);
	    try (InputStream in = Files.newInputStream(ksPath)) {
	        ks.load(in, ksPassword);
	    }

	    // Autodetect alias if missing or wrong
	    if (isBlank(alias) || !ks.isKeyEntry(alias)) {
	        alias = findFirstPrivateKeyAlias(ks, keyPassword);
	        if (alias == null) {
	            throw new IllegalStateException("Ninguna clave privada detectada en el almacen.");
	        }
	        LOGGER.warn("[BST Check Properties] Usando el alias autodetectado: {}", alias);
	        // Opcional: propagar al cfg para que el handler lo use
	        cfg.setProperty("webservices.authorization.ks.cert.alias", alias);
	    }

	    final Key key = ks.getKey(alias, keyPassword);
	    if (!(key instanceof PrivateKey)) {
	        throw new IllegalStateException("Alias does not point to a PrivateKey: " + alias);
	    }
	    final X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
	    if (cert == null) {
	        throw new IllegalStateException("Certificado no encontrado para el alias: " + alias);
	    }

	    LOGGER.debug("[BST Check Properties] Alias={}, keyAlg={}, sigAlgNameCert={}, subjectCN={}, notBefore={}, notAfter={}",
	            alias,
	            key.getAlgorithm(),
	            safeCertSigAlg(cert),
	            subjectCN(cert),
	            cert.getNotBefore(),
	            cert.getNotAfter());

	    // Test a real signature to ensure provider/algorithm OK
	    final String jcaSigAlg = pickJcaSignatureAlg((PrivateKey) key);
	    final java.security.Signature sig = java.security.Signature.getInstance(jcaSigAlg);
	    sig.initSign((PrivateKey) key);
	    sig.update("probe".getBytes(java.nio.charset.StandardCharsets.UTF_8));
	    final byte[] signed = sig.sign();
	    if (signed == null || signed.length == 0) {
	        throw new IllegalStateException("Test signature produced no output.");
	    }
	    LOGGER.debug("[BST Preflight] Test signature OK with {}", jcaSigAlg);
	}

	/**
	 * Selects a suitable JCA signature algorithm name based on the private key algorithm.
	 *
	 * Uses SHA-256 with RSA/ECDSA/DSA; defaults to SHA256withRSA otherwise.
	 *
	 * @param pk private key.
	 * @return JCA signature algorithm name compatible with {@code pk}.
	 */
	private static String pickJcaSignatureAlg(final PrivateKey pk) {
	    final String alg = pk.getAlgorithm();
	    if ("RSA".equalsIgnoreCase(alg)) {
			return "SHA256withRSA";
		}
	    if ("EC".equalsIgnoreCase(alg)) {
			return "SHA256withECDSA";
		}
	    if ("DSA".equalsIgnoreCase(alg)) {
			return "SHA256withDSA";
		}
	    // Fallback
	    return "SHA256withRSA";
	}

	/**
	 * Extracts the Common Name (CN) from the subject DN of an X.509 certificate.
	 * Falls back to the full DN if CN cannot be determined.
	 *
	 * @param cert X.509 certificate.
	 * @return subject CN or the full subject DN when CN is unavailable.
	 */
	private static String subjectCN(final X509Certificate cert) {
	    try {
	        final String dn = cert.getSubjectX500Principal().getName();
	        for (final String part : dn.split(",")) {
	            final String p = part.trim();
	            if (p.startsWith("CN=")) {
					return p.substring(3);
				}
	        }
	    } catch (final Exception ignore) { }
	    return cert.getSubjectX500Principal().getName();
	}

	/**
	 * Retrieves the certificate signature algorithm name without throwing.
	 *
	 * @param cert X.509 certificate.
	 * @return signature algorithm name, or "unknown" on error.
	 */
	private static String safeCertSigAlg(final X509Certificate cert) {
	    try { return cert.getSigAlgName(); } catch (final Exception e) { return "unknown"; }
	}

	/**
	 * Scans the keystore and returns the first alias that resolves to a usable private key entry
	 * with the provided key password.
	 *
	 * @param ks          loaded {@link java.security.KeyStore}.
	 * @param keyPassword password for private key entries.
	 * @return a private-key alias, or {@code null} if none is found/usable.
	 * @throws Exception if keystore access fails.
	 */
	private static String findFirstPrivateKeyAlias(final java.security.KeyStore ks, final char[] keyPassword) throws Exception {
	    final java.util.Enumeration<String> e = ks.aliases();
	    while (e.hasMoreElements()) {
	        final String a = e.nextElement();
	        try {
	            if (ks.isKeyEntry(a)) {
	                final Key k = ks.getKey(a, keyPassword);
	                if (k instanceof PrivateKey) {
						return a;
					}
	            }
	        } catch (final Exception ignore) { /* try next */ }
	    }
	    return null;
	}

//	/**
//	 * Logs an effective (sanitized) web services configuration at DEBUG level only.
//	 * Password values are masked; no secrets are printed.
//	 *
//	 * @param cfg effective configuration properties to dump.
//	 */
//	private static void logFullWsConfig(final Properties cfg) {
//	    LOGGER.info("========== [BST Volcado de Configuracion] ==========");
//	    for (final String key : cfg.stringPropertyNames()) {
//	        final String val = cfg.getProperty(key);
//	        if (key.toLowerCase().contains("password")) {
//	            if (val != null && !val.isEmpty()) {
//	                LOGGER.info("{} = **** ({} chars)", key, val.length());
//	            } else {
//	                LOGGER.info("{} = <nulo/vacio>", key);
//	            }
//	        } else {
//	            LOGGER.info("{} = {}", key, val);
//	        }
//	    }
//	    LOGGER.info("======================================");
//	}

	/**
	 * Deletes older versioned files for a given resource prefix under the base directory,
	 * keeping only the specified version (case-insensitive for ".jks"/".p12").
	 *
	 * @param baseDir     base directory to scan.
	 * @param prefix      resource prefix (e.g., "client-keystore").
	 * @param keepVersion version number to retain.
	 */
	private static void cleanupOlderVersionedFiles(final Path baseDir, final String prefix, final long keepVersion) {
	    if (baseDir == null) {
			return;
		}
	    final String regex = "^" + java.util.regex.Pattern.quote(prefix) + "-v(\\d+)\\.(?:jks|p12)$";
	    final java.util.regex.Pattern pat = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);

	    try (java.nio.file.DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(baseDir)) {
	        for (final Path p : stream) {
	            final String name = p.getFileName().toString();
	            final java.util.regex.Matcher m = pat.matcher(name);
	            if (!m.matches()) {
					continue;
				}
	            final long v;
	            try { v = Long.parseLong(m.group(1)); }
	            catch (final NumberFormatException ignore) { continue; }

	            if (v != keepVersion) {
	                try {
	                    java.nio.file.Files.deleteIfExists(p);
	                    LOGGER.info("[cleanupOlderVersionedFiles] Eliminado fichero de version antigua: {}", p);
	                } catch (final Exception ex) {
	                    LOGGER.warn("[cleanupOlderVersionedFiles] No se pudo eliminar la version antigua: {}", p, ex);
	                }
	            }
	        }
	    } catch (final Exception ex) {
	        LOGGER.warn("[cleanupOlderVersionedFiles] No se pudo listar el directorio para limpiar versiones antiguas: {}", baseDir, ex);
	    }
	}
}
