package es.gob.fire.client;

import java.io.IOException;

/**
 * Define los m&eacute;todos para el descifrado de contrase&ntilde;as. Esto permite que
 * se puedan utilizar contrase&ntilde;as cifradas en los ficheros de configuracion. Estas
 * contrase&ntilde;as se pasar&aacute;n a un objeto de este tipo para descifrarlas antes
 * de utilizarlas.
 */
public interface PasswordDecipher {

	/**
	 * Descifra los datos para usarlos como contrase&ntilde;a de los almacenes de claves que
	 * se utilicen. Los datos que se proporcionan a este m&eacute;todo son el resultado de
	 * decodificar el base 64 indicado como contrase&ntilde;a en los ficheros de
	 * configuraci&oacute;n del componente distribuido.
	 * @param cipheredData Datos cifrados.
	 * @return Cadena de caracteres que se utilizar&aacute; como contrase&ntilde;a.
	 * @throws IOException Cuando no se puede descifrar la contrase&ntilde;a o se indicaron
	 * datos no v&aacute;lidos.
	 */
	char[] decipher(byte[] cipheredData) throws IOException;
}
