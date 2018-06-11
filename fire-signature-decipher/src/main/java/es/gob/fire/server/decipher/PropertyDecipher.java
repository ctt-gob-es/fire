package es.gob.fire.server.decipher;

import java.io.IOException;

/**
 * Interfaz que define los m&eacute;todos que se deben implementar para el descifrado de
 * propiedades de los ficheros de configuraci&oacute;n de FIRe.
 */
public interface PropertyDecipher {

	/**
	 * Descifra los datos cifrados indicados en base 64 en los ficheros de
	 * configuraci&oacute;n.
	 * @param cipheredData Datos cifrados despu&eacute;s de decodificar el base 64.
	 * @return Cadena de texto descifrada.
	 * @throws IOException Cuando ocurre alg&uacute;n error durante el descifrado.
	 */
	String decipher (byte[] cipheredData) throws IOException;
}
