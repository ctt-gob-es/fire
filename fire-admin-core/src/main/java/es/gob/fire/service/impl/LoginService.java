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
 * <b>File:</b><p>es.gob.fire.service.ILoginService.java.</p>
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
  * <b>Project:</b><p></p>
 * <b>Date:</b><p>18/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 19/02/2025.
 */
package es.gob.fire.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsKeystore;
import es.gob.fire.crypto.cades.verifier.CAdESAnalizer;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.entity.ControlAccess;
import es.gob.fire.persistence.repository.ControlAccessRepository;
import es.gob.fire.service.ILoginService;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p></p>
 * @version 1.1, 19/02/2025.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LoginService implements ILoginService {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(LoginService.class);
	
	@Value("${conf.cert.path.truestore.issuers}")
	private String confCertPathTruestoreIssuers;
	
	/**
	 * Attribute that represents the url to service pasarela.
	 */
	private static final String URL_SERVICE_PASARELA = "https://pasarela.clave.gob.es/Proxy2/Certificates";
	
	/**
	 * Attribute that represents the service object for accessing the repository of control access.
	 */
	@Autowired
	private ControlAccessRepository controlAccessRepository;
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainAllControlAccess()
	 */
	@Override
	public List<ControlAccess> obtainAllControlAccess() {
		return controlAccessRepository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#saveControlAccess(ControlAccess)
	 */
	@Override
	public void saveControlAccess(ControlAccess controlAccess) {
		controlAccessRepository.save(controlAccess);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#generateCookieValue()
	 */
	public String generateCookieValue() {
        // Generamos un UUID aleatorio
        String uuid = UUID.randomUUID().toString().replace("-", ""); // Eliminar guiones
        
        // Convertimos UUID a bytes y codificar en Base64 para mayor entropía
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.getBytes(StandardCharsets.UTF_8));
        
        // Agregamos un número aleatorio al final similar a la estructura del valor
        int randomInt = (int) (Math.random() * Integer.MAX_VALUE);

        // Concatenamos con un símbolo especial
        return encoded + "!-" + randomInt;
    }
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#isPasarelaAvailable()
	 */
	public boolean isPasarelaAvailable() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(URL_SERVICE_PASARELA);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 segundos de timeout
            connection.setReadTimeout(5000);
            connection.connect();

            return connection.getResponseCode() == NumberConstants.NUM200;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#deleteControlAccessByIp(java.lang.String)
	 */
    @Transactional
	@Override
	public void deleteControlAccessByIp(String ipUser) {
		controlAccessRepository.deleteAllByIp(ipUser);
	}
    
    /**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#analizeSignWithCAdES(byte[])
	 */
    public CAdESAnalizer analizeSignWithCAdES(byte[] signBase64Bytes) throws CertificateException {
		CAdESAnalizer analizer = new CAdESAnalizer();
		try {
			analizer.init(signBase64Bytes);
		} catch (CertificateException | IOException e) {
			LOGGER.error(e);
			throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML003));
		}
		return analizer;
	}
    
    /**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#loadTrustStoreUsers(java.lang.String, java.security.KeyStore)
	 */
    public KeyStore loadTrustStoreUsers(String passTrustStoreUsers, KeyStore trustStoreUsers) throws KeyStoreException {
		// Cargar el TrustStore
		try (FileInputStream keyStoreFile = new FileInputStream(confCertPathTruestoreIssuers)) {
		    trustStoreUsers = KeyStore.getInstance(UtilsKeystore.JKS);
		    trustStoreUsers.load(keyStoreFile, passTrustStoreUsers.toCharArray());
		} catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
		    throw new KeyStoreException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML008));
		}
		return trustStoreUsers;
	}
    
    /**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#validateIssuerWithTrustStore(java.security.cert.X509Certificate, java.security.KeyStore)
	 */
    public X509Certificate validateIssuerWithTrustStore(X509Certificate certificate, KeyStore trustStoreUsers) throws KeyStoreException {
    	X509Certificate issuerCert = null;
    	try {
    		String issuerDN = certificate.getIssuerX500Principal().getName();
    		
    		Enumeration<String> aliases = trustStoreUsers.aliases();
    		while (aliases.hasMoreElements()) {
    		    String alias = aliases.nextElement();
    		    Certificate cert = trustStoreUsers.getCertificate(alias);
    		    if (cert instanceof X509Certificate) {
    		        X509Certificate x509Cert = (X509Certificate) cert;
    		        if (x509Cert.getSubjectX500Principal().getName().equals(issuerDN)) {
    		            LOGGER.info(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_ML011, new Object[]{x509Cert.getSubjectX500Principal().getName()}));
    		        	issuerCert = x509Cert;
    		            break;
    		        }
    		    }
    		}
    	} catch (KeyStoreException e) {
    		throw new KeyStoreException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML008));
		}
		
		return issuerCert;
	}
    
    /**
   	 * {@inheritDoc}
   	 * @see es.gob.fire.persistence.service#verifyPublicKey(java.security.cert.X509Certificate, java.security.cert.X509Certificate)
   	 */
    public void verifyPublicKey(X509Certificate certificate, X509Certificate issuerCert) throws CertificateException {
		if (issuerCert != null) {
			try {
				certificate.verify(issuerCert.getPublicKey());
			} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
				throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML009));
			}
		} else {
		    throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML010));
		}
	}

    /**
   	 * {@inheritDoc}
   	 * @see es.gob.fire.persistence.service#validatePeriodCertificate(java.security.cert.X509Certificate)
   	 */
	@Override
	public void validatePeriodCertificate(X509Certificate certificate) throws CertificateException {
		try {
			certificate.checkValidity();
		} catch (CertificateExpiredException e) {
			throw new CertificateException(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_ML001, new Object[]{certificate.getSubjectX500Principal()}));
		} catch (CertificateNotYetValidException e) {
			throw new CertificateException(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_ML002, new Object[]{certificate.getSubjectX500Principal()}));
		}
		
	}
}
