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
 * @version 1.2, 20/09/2024.
 */
package es.gob.fire.commons.utils;

/** 
 * <p>Class that manages operations related with the management of keystores.</p>
 * <b>Project:</b><p></p>
 * @version 1.2, 20/09/2024.
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

}
