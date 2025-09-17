/* 
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
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
 * <b>File:</b><p>.gob.fire.service.impl.ServerAfirmaService.java.</p>
 * <b>Description:</b><p> Class that implements the communication with the operations of the persistence layer for Server Afirma.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.2, 10/03/2025.
 */
package es.gob.fire.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsCertificate;
import es.gob.fire.commons.utils.UtilsKeystore;
import es.gob.fire.crypto.aes.AESCipher;
import es.gob.fire.crypto.exceptions.CipherException;
import es.gob.fire.persistence.dto.ServerAfirmaDTO;
import es.gob.fire.persistence.entity.ServerAfirma;
import es.gob.fire.persistence.repository.ServerAfirmaRepository;
import es.gob.fire.service.IServerAfirmaService;

/** 
 * <p>Class that implements the communication with the operations of the persistence layer for Server Afirma.</p>
 * <b>Project:</b><p></p>
 * @version 1.2, 10/03/2025.
 */
@Service("serverAfirmaService")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ServerAfirmaService implements IServerAfirmaService {

	/**
	 * Constant that represents the parameter log.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ServerAfirmaService.class);
	
	/**
	 * Attribute that represents the injected interface that provides CRUD operations for the persistence.
	 */
	@Autowired
	private ServerAfirmaRepository serverAfirmaRepository;
	
	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainServerAfirmaServiceDTO(java.lang.Long)
	 */
	@Override
	public ServerAfirmaDTO obtainServerAfirmaServiceDTO(final Long idServerAfirma) {
	    ServerAfirmaDTO dto = new ServerAfirmaDTO();
	    try {
	        final ServerAfirma sa = this.serverAfirmaRepository.findByIdServerAfirma(idServerAfirma);
	        if (sa == null) {
	            return dto;
	        }

	        // Base info
	        dto = new ServerAfirmaDTO(
	                sa.getcAuthenticationType() != null ? sa.getcAuthenticationType().getIdAuthenticationType() : null,
	                sa.getIdServerAfirma(),
	                sa.getNameApp(),
	                sa.getTimeout(),
	                sa.getUrlServer()
	        );

	        // Versions (display-only)
	        dto.setKeystoreVersion(safeToLong(sa.getKeystoreVersion()));
	        dto.setTruststoreVersion(safeToLong(sa.getTruststoreVersion()));
	        dto.setAuthenticationVersion(safeToLong(sa.getAuthenticationVersion()));

	        // ===== Keystore =====
	        dto.setKsType(defaultString(sa.getKsType(), "PKCS12"));
	        dto.setKsCertAlias(sa.getKsCertAlias());

	        dto.setKsPassword(null);
	        dto.setKsCertPassword(null);

	        dto.setHasKsPassword(sa.getKsPassword() != null);
	        dto.setHasKsCertPassword(sa.getKsCertPassword() != null);
	        
	        if (sa.getKsBlob() != null && sa.getKsBlob().length > 0) {
	            dto.setKeystoreB64(java.util.Base64.getEncoder().encodeToString(sa.getKsBlob()));
	        }

	        try {
	            final byte[] ksBytes = sa.getKsBlob();
	            final String ksPass  = dto.getKsPassword();
	            if (ksBytes != null && ksBytes.length > 0 && ksPass != null) {
	                final String ksType = defaultString(sa.getKsType(), "PKCS12");
	                final KeyStore ks = KeyStore.getInstance(ksType);
	                try (InputStream is = new ByteArrayInputStream(ksBytes)) {
	                    ks.load(is, ksPass.toCharArray());
	                }
	                String alias = sa.getKsCertAlias();
	                if (alias == null || alias.isEmpty() || ks.getCertificate(alias) == null) {
	                    alias = firstCertOrKeyAlias(ks);
	                }
	                final java.security.cert.Certificate cert = (alias != null) ? ks.getCertificate(alias) : null;
	                if (cert instanceof X509Certificate) {
	                    final X509Certificate x509 = (X509Certificate) cert;
	                    dto.setSubject(UtilsCertificate.getReadableSubject(x509));
	                    dto.setCertificateB64(java.util.Base64.getEncoder().encodeToString(x509.getEncoded()));
	                }
	            }
	        } catch (Exception ke) {
	            LOGGER.warn("Unable to read keystore or extract certificate subject.", ke);
	        }

	        // ===== Truststore =====
	        dto.setTruststoreType(defaultString(sa.getTruststoreType(), "JKS"));
	        dto.setTruststorePassword(null);
	        dto.setHasTruststorePassword(sa.getTruststorePassword() != null);
	        
	        if (sa.getTruststoreBlob() != null && sa.getTruststoreBlob().length > 0) {
	            dto.setTruststoreB64(java.util.Base64.getEncoder().encodeToString(sa.getTruststoreBlob()));
	        }

	        // ===== Auth Truststore =====
	        dto.setAuthTruststoreType(defaultString(sa.getAuthTsType(), "JKS"));
	        dto.setAuthTruststorePassword(null);
	        dto.setAuthCertAlias(sa.getAuthCertAlias());
	        dto.setHasAuthTruststorePassword(sa.getAuthTsPassword() != null);

	        if (sa.getAuthTsBlob() != null && sa.getAuthTsBlob().length > 0) {
	            dto.setAuthTruststoreB64(java.util.Base64.getEncoder().encodeToString(sa.getAuthTsBlob()));
	        }

	        // ===== UsernameToken =====
	        dto.setUser(sa.getUser());
	        dto.setPassword(null);
	        dto.setHasUserPassword(sa.getPassword() != null);
	        
	        // ===== UI flags según tipo actual (sin borrar campos) =====
	        final Long authType = sa.getcAuthenticationType() != null
	                ? sa.getcAuthenticationType().getIdAuthenticationType()
	                : null;
	        final Long AUTH_UT  = NumberConstants.NUM_1_LONG;
	        final Long AUTH_BST = NumberConstants.NUM_2_LONG;

	        if (java.util.Objects.equals(authType, AUTH_BST)) {
	            dto.setDisabledKeystore(false);
	            dto.setDisabledUserPass(true);
	        } else if (java.util.Objects.equals(authType, AUTH_UT)) {
	            dto.setDisabledKeystore(true);
	            dto.setDisabledUserPass(false);
	        } else {
	            dto.setDisabledKeystore(true);
	            dto.setDisabledUserPass(true);
	        }
	    } catch (Exception e) {
	        LOGGER.error("Error obtaining ServerAfirmaDTO", e);
	    }
	    return dto;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#obtainServerAfirmaService(java.lang.Long)
	 */
	@Override
	public ServerAfirma obtainServerAfirmaService(Long idServerAfirma) {
		return serverAfirmaRepository.findByIdServerAfirma(idServerAfirma);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.persistence.service#saveServerAfirma(es.gob.fire.persistence.entity.ServerAfirma)
	 */
	@Override
	public void saveServerAfirma(ServerAfirma serverAfirma) {
		serverAfirmaRepository.save(serverAfirma);
	}

	/**
	 * Null-safe equality check for {@link Long} values.
	 *
	 * @param a first value (nullable).
	 * @param b second value (nullable).
	 * @return {@code true} if both are equal (including both {@code null}); otherwise {@code false}.
	 */
	private static boolean safeEquals(final Long a, final Long b) {
	    return java.util.Objects.equals(a, b);
	}

	/**
	 * Returns the provided string if non-null and non-empty, otherwise returns the given default.
	 *
	 * @param value candidate string (nullable).
	 * @param def   default value to return when input is null or empty.
	 * @return trimmed string or {@code def} if input is null/empty.
	 */
	private static String defaultString(final String value, final String def) {
	    return (value == null || value.isEmpty()) ? def : value;
	}

	/**
	 * Safely converts different object types to a {@link Long}.
	 * <p>
	 * Accepts {@link Long}, other {@link Number} subclasses, or {@link String}.
	 * Returns {@code null} if input is null, empty, or cannot be parsed.
	 *
	 * @param v input object (may be {@code Long}, {@code Number}, or {@code String}).
	 * @return parsed {@link Long}, or {@code null} if invalid or unparsable.
	 */
	private static Long safeToLong(final Object v) {
	    if (v == null) return null;
	    if (v instanceof Number) return ((Number) v).longValue();
	    final String s = String.valueOf(v).trim();
	    if (s.isEmpty()) return null;
	    try { return Long.parseLong(s); }
	    catch (NumberFormatException ex) { LOGGER.warn("Invalid version value: '{}'", v); return null; }
	}

	/**
	 * Retrieves the first alias from a {@link KeyStore} that corresponds to either
	 * a key entry or a certificate entry.
	 *
	 * @param ks initialized {@link KeyStore}.
	 * @return the first matching alias, or {@code null} if none found.
	 * @throws KeyStoreException if an error occurs while accessing the keystore.
	 */
	private static String firstCertOrKeyAlias(final KeyStore ks) throws KeyStoreException {
	    final java.util.Enumeration<String> aliases = ks.aliases();
	    while (aliases.hasMoreElements()) {
	        final String a = aliases.nextElement();
	        if (ks.isKeyEntry(a) || ks.isCertificateEntry(a)) {
	            return a;
	        }
	    }
	    return null;
	}
}
