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
 * @version 1.3, 06/03/2025.
 */
package es.gob.fire.service.impl;

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
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsCertificate;
import es.gob.fire.commons.utils.UtilsDate;
import es.gob.fire.commons.utils.UtilsKeystore;
import es.gob.fire.crypto.cades.verifier.CAdESAnalizer;
import es.gob.fire.i18n.IWebAdminGeneral;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.UserLoggedDTO;
import es.gob.fire.persistence.entity.ControlAccess;
import es.gob.fire.persistence.entity.User;
import es.gob.fire.persistence.permissions.Permissions;
import es.gob.fire.persistence.permissions.PermissionsChecker;
import es.gob.fire.persistence.repository.ControlAccessRepository;
import es.gob.fire.persistence.service.IUserService;
import es.gob.fire.service.ILoginService;
import es.gob.fire.web.authentication.DniAuthenticationToken;

/**
 * <p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p></p>
 * @version 1.3, 06/03/2025.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LoginService implements ILoginService {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(LoginService.class);

	@Value("${contingency.truststore.path}")
	private String contingencyTruststorePath;

	@Value("${contingency.truststore.password}")
	private String contingencyTruststorePassword;

	/**
	 * Attribute that represents the url to service pasarela.
	 */
	private static final String URL_SERVICE_PASARELA = "https://pasarela.clave.gob.es/Proxy2/Certificates";

	/**
	 * Attribute that represents the administrator role.
	 */
	public static final String ROLE_ADMIN = "Administrator"; //$NON-NLS-1$

	public static final String PARAM_RANDOM_STRING_LOGIN = "randomStringLogin"; //$NON-NLS-1$

	public static final String PARAM_LIMIT_SIGN_GEN = "limitSignGen"; //$NON-NLS-1$

	/**
	 * Attribute that represents the service object for accessing the repository of control access.
	 */
	@Autowired
	private ControlAccessRepository controlAccessRepository;

	/**
	 * Attribute that represents the DTO to transport information about user logged.
	 */
	@Autowired
	private UserLoggedDTO userLoggedDTO;

	/**
	 * Attribute that represents the service object for accessing the repository of control access.
	 */
	@Autowired
	private IUserService iUserService;

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainAllControlAccess()
	 */
	@Override
	public List<ControlAccess> obtainAllControlAccess() {
		return this.controlAccessRepository.findAll();
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#saveControlAccess(ControlAccess)
	 */
	@Override
	public void saveControlAccess(final ControlAccess controlAccess) {
		this.controlAccessRepository.save(controlAccess);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#generateCookieValue()
	 */
	@Override
	public String generateCookieValue() {
        // Generamos un UUID aleatorio
        final String uuid = UUID.randomUUID().toString().replace("-", ""); // Eliminar guiones //$NON-NLS-1$ //$NON-NLS-2$

        // Convertimos UUID a bytes y codificar en Base64 para mayor entropia
        final String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.getBytes(StandardCharsets.UTF_8));

        // Agregamos un numero aleatorio al final similar a la estructura del valor
        final int randomInt = (int) (Math.random() * Integer.MAX_VALUE);

        // Concatenamos con un simbolo especial
        return encoded + "!-" + randomInt; //$NON-NLS-1$
    }

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#isPasarelaAvailable()
	 */
	@Override
	public boolean isPasarelaAvailable() {
        HttpURLConnection connection = null;
        try {
            final URL url = new URL(URL_SERVICE_PASARELA);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); //$NON-NLS-1$
            connection.setConnectTimeout(5000); // 5 segundos de timeout
            connection.setReadTimeout(5000);
            connection.connect();

            return connection.getResponseCode() == NumberConstants.NUM200;
        } catch (final IOException e) {
            LOGGER.error("La pasarela de Cl@ve no esta disponible: " + e); //$NON-NLS-1$
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#deleteAllControlAccess()
	 */
    @Transactional
	@Override
	public void deleteAllControlAccess() {
		this.controlAccessRepository.deleteAll();
	}

    /**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#analizeSignWithCAdES(byte[])
	 */
    @Override
	public CAdESAnalizer analizeSignWithCAdES(final byte[] signBase64Bytes) throws CertificateException {
		final CAdESAnalizer analizer = new CAdESAnalizer();
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
	 * @see es.gob.fire.persistence.service#loadTrustStoreUsers()
	 */
    @Override
	public KeyStore loadTrustStoreUsers() throws KeyStoreException {
    	KeyStore trustStoreUsers = null;
    	try {
    		// Cargamos el TrustStore
			trustStoreUsers = UtilsKeystore.loadTrustStore(this.contingencyTruststorePath, UtilsKeystore.JKS, this.contingencyTruststorePassword);
		} catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
			LOGGER.error(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML008), e);
		    throw new KeyStoreException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML008));
		}
		return trustStoreUsers;
	}

    /**
	 * {@inheritDoc}
     * @see es.gob.fire.persistence.service#validateIssuerWithTrustStoreUsers(java.security.cert.X509Certificate, java.security.KeyStore)
	 */
    @Override
	public X509Certificate validateIssuerWithTrustStoreUsers(final X509Certificate certificate, final KeyStore trustStoreUsers) throws KeyStoreException, CertificateException {

    	X509Certificate issuerCert = null;
    	try {

    		// Obtenemos el emisor del certificado
	        final String issuerDN = certificate.getIssuerX500Principal().getName();

    		// Evaluamos si el certificado elegido tiene como emisor alguno de los certificados de nuestro almacen de confianza
    		issuerCert = UtilsKeystore.isIssuer(issuerDN, trustStoreUsers);

    		if(issuerCert == null) {
    			throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML010));
    		}
			LOGGER.info(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_ML011, new Object[]{issuerDN}));
    	} catch (final KeyStoreException e) {
    		LOGGER.error(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML008), e);
    		throw new KeyStoreException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML008));
		}

		return issuerCert;
	}

    /**
   	 * {@inheritDoc}
   	 * @see es.gob.fire.persistence.service#verifyPublicKeyToCertUser(java.security.cert.X509Certificate, java.security.cert.X509Certificate)
   	 */
    @Override
	public void verifyPublicKeyToCertUser(final X509Certificate certificate, final X509Certificate issuerCert) throws CertificateException {
    	try {
    		UtilsKeystore.verify(certificate, issuerCert);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
			LOGGER.error(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML009), e);
			throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML009));
		}
	}

    /**
   	 * {@inheritDoc}
   	 * @see es.gob.fire.persistence.service#validatePeriodToCertUser(java.security.cert.X509Certificate)
   	 */
	@Override
	public void validatePeriodToCertUser(final X509Certificate certificate) throws CertificateException {
		try {
			UtilsCertificate.checkValidity(certificate);
		} catch (final CertificateExpiredException e) {
			throw new CertificateException(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_ML001, new Object[]{certificate.getSubjectX500Principal()}));
		} catch (final CertificateNotYetValidException e) {
			throw new CertificateException(Language.getFormatResWebAdminGeneral(IWebAdminGeneral.LOG_ML002, new Object[]{certificate.getSubjectX500Principal()}));
		}

	}

	/**
   	 * {@inheritDoc}
   	 * @see es.gob.fire.persistence.service#obtainDNIfromCertUser(java.security.cert.X509Certificate)
   	 */
	@Override
	public String obtainDNIfromCertUser(final X509Certificate certificate) throws CertificateException {
		String dni = null;
		final String CN = UtilsCertificate.extractDN(certificate.getIssuerX500Principal().getName(), UtilsCertificate.DN_CN);
		if(CN.equals(UtilsCertificate.ISSUED_BY_AC_SECTOR_PUBLICO)) {
			// Obtenemos la identidad administrativa del certificado proveniente de SANs (Subject Alternative Names)
			final Map<String, String> mapIdentityAdministrative = UtilsCertificate.getSANsType4(certificate);
			// En base al tipo de Certificado enviamos un OID u otro
			if(mapIdentityAdministrative.get(UtilsCertificate.OID_CERT_TYPE_EMPL_PUBLIC_NIVEL_MEDIO) != null) {
				dni = UtilsCertificate.decodeASN1Hex(mapIdentityAdministrative.get(UtilsCertificate.OID_NIF_ENTIDAD_EMPL_PUBLIC_NIVEL_MEDIO).substring(1));
			} else if(mapIdentityAdministrative.get(UtilsCertificate.OID_CERT_TYPE_EMPL_PUBLIC_NIVEL_ALTO) != null) {
				dni = UtilsCertificate.decodeASN1Hex(mapIdentityAdministrative.get(UtilsCertificate.OID_NIF_ENTIDAD_EMPL_PUBLIC_NIVEL_ALTO).substring(1));
			} else if(mapIdentityAdministrative.get(UtilsCertificate.OID_CERT_TYPE_EMPL_PUBLIC_CON_PSEUDONIMO_NIVEL_MEDIO) != null) {
				dni = UtilsCertificate.decodeASN1Hex(mapIdentityAdministrative.get(UtilsCertificate.OID_NIF_ENTIDAD_EMPL_PUBLIC_CON_PSEUDONIMO_NIVEL_MEDIO).substring(1));
			} else if(mapIdentityAdministrative.get(UtilsCertificate.OID_CERT_TYPE_EMPL_PUBLIC_CON_PSEUDONIMO_NIVEL_ALTO) != null) {
				dni = UtilsCertificate.decodeASN1Hex(mapIdentityAdministrative.get(UtilsCertificate.OID_NIF_ENTIDAD_EMPL_PUBLIC_CON_PSEUDONIMO_NIVEL_ALTO).substring(1));
			} else if(mapIdentityAdministrative.get(UtilsCertificate.OID_CERT_TYPE_EMPL_PUBLIC_SELLO_ELECT_NIVEL_MEDIO) != null) {
				dni = UtilsCertificate.decodeASN1Hex(mapIdentityAdministrative.get(UtilsCertificate.OID_NIF_ENTIDAD_EMPL_PUBLIC_SELLO_ELECT_NIVEL_MEDIO).substring(1));
			}
		} else if(CN.equals(UtilsCertificate.ISSUED_BY_AC_FNMT_USUARIOS)) {
			// Obtenemos la identidad administrativa del certificado proveniente de SANs (Subject Alternative Names)
			final Map<String, String> mapIdentityAdministrative = UtilsCertificate.getSANsType4(certificate);
			dni = UtilsCertificate.decodeASN1Hex(mapIdentityAdministrative.get(UtilsCertificate.OID_AC_FNMT_USUARIOS).substring(1));
		} else if(CN.equals(UtilsCertificate.ISSUED_BY_AC_DNIE_004) || CN.equals(UtilsCertificate.ISSUED_BY_AC_DNIE_005) || CN.equals(UtilsCertificate.ISSUED_BY_AC_DNIE_006)) {
			final Map<String, String>  mapDN = UtilsCertificate.parseDN(certificate.getSubjectX500Principal().getName());
			dni = UtilsCertificate.decodeASN1Hex(mapDN.get(UtilsCertificate.OID_AC_DNIE));
		} else {
			throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML013));
		}

		if(dni == null || dni.isEmpty()) {
			throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML012));
		}

		return dni;
	}

	/**
   	 * {@inheritDoc}
   	 * @see es.gob.fire.persistence.service#obtainAuthAndUpdateLastAccess(es.gob.fire.persistence.entity.User)
   	 */
	@Override
	public Authentication obtainAuthAndUpdateLastAccess(final User user) {
		// Creamos el token de autenticacion
    	final List<GrantedAuthority> grantedAuths = new ArrayList<>();
		grantedAuths.add(new SimpleGrantedAuthority(ROLE_ADMIN));
    	final Authentication authentication = new DniAuthenticationToken(user.getDni(), grantedAuths);

    	if (!PermissionsChecker.hasPermission(user, Permissions.ACCESS)) {
    	    LOGGER.error("El usuario no tiene permisos de acceso."); //$NON-NLS-1$
    	    throw new InsufficientAuthenticationException("Acceso denegado: permisos insuficientes."); //$NON-NLS-1$
		}

    	// Asignamos al bean de spring del usuario para usarlo en la app
		this.userLoggedDTO.setDni(user.getDni());
		this.userLoggedDTO.setEmail(user.getEmail());
		this.userLoggedDTO.setIdRol(user.getRol().getRolId());
		this.userLoggedDTO.setName(user.getName());
		this.userLoggedDTO.setPhone(user.getPhone());
		this.userLoggedDTO.setRenovationCode(user.getRenovationCode());
		this.userLoggedDTO.setRenovationDate(user.getRenovationDate() == null ? null : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(user.getRenovationDate())); //$NON-NLS-1$
		this.userLoggedDTO.setRestPassword(user.getRestPassword());
		this.userLoggedDTO.setRoot(user.getRoot());
		this.userLoggedDTO.setStartDate(user.getStartDate() == null ? null : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(user.getStartDate())); //$NON-NLS-1$
		this.userLoggedDTO.setSurnames(user.getSurnames());
		this.userLoggedDTO.setUserId(user.getUserId());
		this.userLoggedDTO.setFecUltimoAcceso(user.getFecUltimoAcceso() == null ? null : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(user.getFecUltimoAcceso())); //$NON-NLS-1$

		// Actualizamos la fecha de ultimo acceso
		user.setFecUltimoAcceso(Calendar.getInstance().getTime());
		this.iUserService.saveUser(user);

		return authentication;
	}

	/**
   	 * {@inheritDoc}
	 * @throws TimeoutException
   	 * @see es.gob.fire.persistence.service#validateIfSignSecure(es.gob.fire.crypto.cades.verifier.CAdESAnalizer)
   	 */
	@Override
	public void validateIfSignSecure(final CAdESAnalizer analizer, final String token, final String limitSignGen) throws CertificateException, ParseException, TimeoutException {
		final String strSigned = new String(analizer.getContent());

		if (token == null) {
			LOGGER.error(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML019));
			throw new TimeoutException (Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML016));
		}
		if (!strSigned.equalsIgnoreCase(token)) {
			LOGGER.error(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML015));
			throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML016));
		}

		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UtilsDate.FORMAT_DATE_TIME_STANDARD);

		final Date pastDate = simpleDateFormat.parse(limitSignGen);

		final Date currentDate = new Date();

		long differenceInMillis = currentDate.getTime() - pastDate.getTime();

		// Convertimos a minutos
		long differenceInMinutes = differenceInMillis / (60 * 1000);

		if (differenceInMinutes >= NumberConstants.NUM_5_LONG) {
			LOGGER.error(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML017));
			throw new TimeoutException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML016));
		}

		differenceInMillis = currentDate.getTime() - analizer.getSigningTime().getTime();

		differenceInMinutes = differenceInMillis / (60 * 1000);

		if (differenceInMinutes >= NumberConstants.NUM_5_LONG) {
			LOGGER.error(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML018));
			throw new CertificateException(Language.getResWebAdminGeneral(IWebAdminGeneral.LOG_ML016));
		}
	}
}
