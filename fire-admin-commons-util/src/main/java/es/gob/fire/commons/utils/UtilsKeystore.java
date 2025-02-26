/* 
* Este fichero forma parte de la plataforma de @firma. 
* La plataforma de @firma es de libre distribución cuyo código fuente puede ser consultado
* y descargado desde http://administracionelectronica.gob.es
*
* Copyright 2005-2019 Gobierno de España
* Este fichero se distribuye bajo las licencias EUPL versión 1.1 según las
* condiciones que figuran en el fichero 'LICENSE.txt' que se acompaña.  Si se   distribuyera este 
* fichero individualmente, deben incluirse aquí las condiciones expresadas allí.
*/

/** 
 * <b>File:</b><p>es.gob.afirma.utilidades.UtilsKeystore.java.</p>
 * <b>Description:</b><p>Class that manages operations related with the management of keystores.</p>
 * <b>Project:</b><p></p>
 * <b>Date:</b><p> 01/10/2020.</p>
 * @author Gobierno de España.
 * @version 1.3, 20/02/2025.
 */
package es.gob.fire.commons.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/** 
 * <p>Class that manages operations related with the management of keystores.</p>
 * <b>Project:</b><p></p>
 * @version 1.3, 20/02/2025.
 */
public final class UtilsKeystore {

	/**
	 * Attribute that represents the PKCS#12 keystore type.
	 */
	public static final String PKCS12 = "PKCS12";

	/**
	 * Attribute that represents the JCEKS keystore type.
	 */
	public static final String JCEKS = "JCEKS";

	/**
	 * Attribute that represents the Java Key Store keystore type.
	 */
	public static final String JKS = "JKS";

	/**
	 * Attribute that represents the PKCS#11 keystore type.
	 */
	public static final String PKCS11 = "PKCS11";

	/**
	 * Constant attribute that represents the X.509 certificate type.
	 */
	public static final String X509_CERTIFICATE_TYPE = "X.509";

	/**
	 * Attribute that represents a p12 key store file extension.
	 */
	public static final String P12_KEYSTORE_EXTENSION = "p12";

	/**
	 * Attribute that represents a pfx key store file extension.
	 */
	public static final String PFX_KEYSTORE_EXTENSION = "pfx";

	/**
	 * Constructor method for the class KeystoreUtils.java.
	 */
	private UtilsKeystore() {

	}
	
	/**
	 * Loads a trust store from a specified file.
	 *
	 * @param pathKeystore   the file path of the keystore
	 * @param typeKeystore   the type of the keystore (e.g., "JKS", "PKCS12")
	 * @param passTrustStore the password to access the keystore
	 * @return the loaded KeyStore instance
	 * @throws FileNotFoundException   if the keystore file is not found
	 * @throws IOException             if an I/O error occurs
	 * @throws KeyStoreException       if the keystore cannot be initialized
	 * @throws NoSuchAlgorithmException if the algorithm used to check integrity is not available
	 * @throws CertificateException     if there is an issue loading the certificates
	 */
	public static KeyStore loadTrustStore(String pathKeystore, String typeKeystore, String passTrustStore) throws FileNotFoundException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		KeyStore trustStoreUsers;
		try (FileInputStream keyStoreFile = new FileInputStream(pathKeystore)) {
		    trustStoreUsers = KeyStore.getInstance(typeKeystore);
		    trustStoreUsers.load(keyStoreFile, passTrustStore.toCharArray());
		}
		return trustStoreUsers;
	}

	/**
	 * Checks if a given issuer distinguished name (DN) exists in the trust store and retrieves the corresponding certificate.
	 *
	 * @param issuerDN       the distinguished name of the issuer
	 * @param trustStoreUsers the trust store containing certificates
	 * @return the X509Certificate of the issuer if found, otherwise null
	 * @throws KeyStoreException if an error occurs while accessing the keystore
	 */
	public static X509Certificate isIssuer(String issuerDN, KeyStore trustStoreUsers) throws KeyStoreException {
		X509Certificate issuerCert = null;
		
		Enumeration<String> aliases = trustStoreUsers.aliases();
		while (aliases.hasMoreElements()) {
		    String alias = aliases.nextElement();
		    Certificate cert = trustStoreUsers.getCertificate(alias);
		    if (cert instanceof X509Certificate) {
		        X509Certificate x509Cert = (X509Certificate) cert;
		        if (x509Cert.getSubjectX500Principal().getName().equals(issuerDN)) {
		            issuerCert = x509Cert;
		            break;
		        }
		    }
		}
		
		return issuerCert;
	}
	
	/**
	 * Verifies that a given certificate is signed by the specified issuer certificate.
	 *
	 * @param certificate the certificate to be verified
	 * @param issuerCert  the issuer's certificate
	 * @throws InvalidKeyException      if the issuer's public key is invalid
	 * @throws CertificateException     if the certificate is invalid
	 * @throws NoSuchAlgorithmException if the algorithm used for verification is not available
	 * @throws NoSuchProviderException  if the security provider is not available
	 * @throws SignatureException       if the certificate signature does not match the issuer's public key
	 */
	public static void verify(X509Certificate certificate, X509Certificate issuerCert) throws InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		certificate.verify(issuerCert.getPublicKey());
	}
}
