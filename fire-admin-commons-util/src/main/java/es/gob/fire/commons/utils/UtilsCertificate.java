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
 * <b>File:</b><p>es.gob.fire.commons.utils.UtilsCertificate.java.</p>
 * <b>Description:</b><p>Class that manages operations related with the management of certificates.</p>
  * <b>Project:</b><p></p>
 * <b>Date:</b><p>18/02/2025.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.2, 20/02/2025.
 */
package es.gob.fire.commons.utils;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;

/** 
 * <p>Class that manages operations related with the management of certificates.</p>
 * <b>Project:</b><p></p>
 * @version 1.2, 20/02/2025.
 */
public class UtilsCertificate {

	/**
	 * Issuer name for certificates issued by "AC Sector Público".
	 */
	public static final String ISSUED_BY_AC_SECTOR_PUBLICO = "AC Sector Público";

	/**
	 * Issuer name for certificates issued by "AC FNMT Usuarios".
	 */
	public static final String ISSUED_BY_AC_FNMT_USUARIOS = "AC FNMT Usuarios";

	/**
	 * Issuer name for certificates issued by "AC DNIE 004".
	 */
	public static final String ISSUED_BY_AC_DNIE_004 = "AC DNIE 004";

	/**
	 * Issuer name for certificates issued by "AC DNIE 005".
	 */
	public static final String ISSUED_BY_AC_DNIE_005 = "AC DNIE 005";

	/**
	 * Issuer name for certificates issued by "AC DNIE 006".
	 */
	public static final String ISSUED_BY_AC_DNIE_006 = "AC DNIE 006";

	/**
	 * OID for electronic public employee certificates (medium level).
	 */
	public static final String OID_CERT_TYPE_EMPL_PUBLIC_NIVEL_MEDIO = "2.16.724.1.3.5.7.2.1";

	/**
	 * OID for the NIF of public employee electronic certificates (medium level).
	 */
	public static final String OID_NIF_ENTIDAD_EMPL_PUBLIC_NIVEL_MEDIO = "2.16.724.1.3.5.7.2.4";

	/**
	 * OID for electronic public employee certificates (high level).
	 */
	public static final String OID_CERT_TYPE_EMPL_PUBLIC_NIVEL_ALTO = "2.16.724.1.3.5.7.1.1";

	/**
	 * OID for the NIF of public employee electronic certificates (high level).
	 */
	public static final String OID_NIF_ENTIDAD_EMPL_PUBLIC_NIVEL_ALTO = "2.16.724.1.3.5.7.1.4";

	/**
	 * OID for electronic public employee certificates with pseudonym (medium level).
	 */
	public static final String OID_CERT_TYPE_EMPL_PUBLIC_CON_PSEUDONIMO_NIVEL_MEDIO = "2.16.724.1.3.5.4.2.1";

	/**
	 * OID for the NIF of public employee electronic certificates with pseudonym (medium level).
	 */
	public static final String OID_NIF_ENTIDAD_EMPL_PUBLIC_CON_PSEUDONIMO_NIVEL_MEDIO = "2.16.724.1.3.5.4.2.3";

	/**
	 * OID for electronic public employee certificates with pseudonym (high level).
	 */
	public static final String OID_CERT_TYPE_EMPL_PUBLIC_CON_PSEUDONIMO_NIVEL_ALTO = "2.16.724.1.3.5.4.1.1";

	/**
	 * OID for the NIF of public employee electronic certificates with pseudonym (high level).
	 */
	public static final String OID_NIF_ENTIDAD_EMPL_PUBLIC_CON_PSEUDONIMO_NIVEL_ALTO = "2.16.724.1.3.5.4.1.3";

	/**
	 * OID for electronic seal certificates (medium level).
	 */
	public static final String OID_CERT_TYPE_EMPL_PUBLIC_SELLO_ELECT_NIVEL_MEDIO = "2.16.724.1.3.5.6.2.1";

	/**
	 * OID for the NIF of electronic seal certificates (medium level).
	 */
	public static final String OID_NIF_ENTIDAD_EMPL_PUBLIC_SELLO_ELECT_NIVEL_MEDIO = "2.16.724.1.3.5.6.2.3";

	/**
	 * OID for certificates issued by "AC FNMT Usuarios".
	 */
	public static final String OID_AC_FNMT_USUARIOS = "1.3.6.1.4.1.5734.1.4";

	/**
	 * OID for the DNI (National Identity Document) field in certificates.
	 */
	public static final String OID_AC_DNIE = "2.5.4.5";

	/**
	 * Prefix for the Common Name (CN) field in a Distinguished Name (DN).
	 */
	public static final String DN_CN = "CN=";

	/**
	 * Checks the validity of an X.509 certificate.
	 * 
	 * @param certificate The X.509 certificate to validate.
	 * @throws CertificateExpiredException      If the certificate has expired.
	 * @throws CertificateNotYetValidException If the certificate is not yet valid.
	 */
	public static void checkValidity(X509Certificate certificate)
			throws CertificateExpiredException, CertificateNotYetValidException {
		certificate.checkValidity();
	}

	/**
	 * Extracts a specific Distinguished Name (DN) attribute from a given DN string.
	 * 
	 * @param issuerDN The full Distinguished Name (DN) string.
	 * @param DN       The DN attribute to extract (e.g., "CN=", "SERIALNUMBER=").
	 * @return The extracted value of the DN attribute, or {@code null} if not found.
	 */
	public static String extractDN(String issuerDN, String DN) {
		// Separar el DN en sus componentes
		String[] components = issuerDN.split(",");

		// Buscar el CN
		for (String component : components) {
			if (component.trim().startsWith(DN)) {
				// Devolver el valor después de "CN="
				return component.substring(3).trim();
			}
		}
		// Devolver null si no se encontró el CN
		return null;
	}

	/**
	 * Decodes an ASN.1 encoded hexadecimal string into a human-readable string.
	 * 
	 * @param hex The hexadecimal string representing an ASN.1 encoded value.
	 * @return The decoded string, or {@code null} if decoding fails.
	 */
	public static String decodeASN1Hex(String hex) {
		try {
			byte[] bytes = hexStringToByteArray(hex);
			try (ASN1InputStream asn1Stream = new ASN1InputStream(new ByteArrayInputStream(bytes))) {
				ASN1Primitive asn1Object = asn1Stream.readObject();
				if (asn1Object instanceof ASN1OctetString) {
					return new String(((ASN1OctetString) asn1Object).getOctets(), "UTF-8");
				} else {
					return asn1Object.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Converts a hexadecimal string into a byte array.
	 * 
	 * @param hex The hexadecimal string to convert.
	 * @return A byte array representing the hexadecimal string.
	 * @throws IllegalArgumentException If the input string has an odd length or contains invalid characters.
	 */
	private static byte[] hexStringToByteArray(String hex) {
		int len = hex.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Extracts Subject Alternative Names (SANs) of type 4 (directory names) from an X.509 certificate 
	 * and maps them by their OID (Object Identifier).
	 * 
	 * @param certificate The X.509 certificate from which to extract SANs.
	 * @return A map where the keys are OIDs and the values are their corresponding hexadecimal-encoded values.
	 *         Returns an empty map if no SANs of type 4 are found or if an error occurs.
	 */
	public static Map<String, String> getSANsType4(X509Certificate certificate) {
		Map<String, String> oidMap = new HashMap<>();

		try {
			Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
			if (subjectAltNames != null) {
				for (List<?> san : subjectAltNames) {
					if (!san.isEmpty() && san.get(0) instanceof Integer && (Integer) san.get(0) == 4) {
						for (int i = 1; i < san.size(); i++) {
							if (san.get(i) instanceof String) {
								String oidEntry = (String) san.get(i);

								// Dividimos correctamente si hay múltiples OID en una sola cadena
								String[] oidPairs = oidEntry.split(",");

								for (String pair : oidPairs) {
									int separatorIndex = pair.indexOf("#");
									if (separatorIndex > 0) {
										String oid = pair.substring(0, separatorIndex - 1).trim();
										String value = pair.substring(separatorIndex).trim();
										oidMap.put(oid, value);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return oidMap;
	}
	
	/**
	 * Parses a distinguished name (DN) string into a map of attributes and their corresponding values.
	 * 
	 * The DN is split by commas, taking care to handle escaped commas (\,). If an attribute value starts 
	 * with a hash (#), the method removes this prefix. Escaped commas within keys and values are replaced 
	 * with regular commas.
	 * 
	 * @param subjectDN The distinguished name string to parse.
	 * @return A map where the keys are attribute names and the values are their corresponding values 
	 *         extracted from the DN string. The map maintains the order of attributes as they appear 
	 *         in the DN string.
	 */
	public static Map<String, String> parseDN(String subjectDN) {
        Map<String, String> dnMap = new LinkedHashMap<>();

        // Dividimos por comas, asegurando no romper escapes con "\,"
        String[] attributes = subjectDN.split("(?<!\\\\),");

        for (String attribute : attributes) {
            int separatorIndex = attribute.indexOf("=");

            if (separatorIndex > 0) {
                String key = attribute.substring(0, separatorIndex).trim();
                String value = attribute.substring(separatorIndex + 1).trim();

                // Quitamos la parte hexadecimal si está presente (ejemplo: 2.5.4.42=#0c044a75616e)
                if (value.startsWith("#")) {
                    value = value.substring(1).trim();
                }

                // Reemplazamos comas escapadas "\," por ","
                key = key.replace("\\,", ",");
                value = value.replace("\\,", ",");

                dnMap.put(key, value);
            }
        }
        return dnMap;
    }
}
