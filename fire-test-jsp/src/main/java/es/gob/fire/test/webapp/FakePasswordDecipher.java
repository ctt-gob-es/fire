package es.gob.fire.test.webapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import es.gob.fire.client.PasswordDecipher;

/**
 * Descifrador de contrase&ntilde;as de ejemplo. Esta implementaci&oacute;n
 * realmente no descifra. Utiliza como cadena descifrada la propia entrada.
 * Esto permitir&iacute;a que en el fichero de configuraci&oacute;n se indicase
 * como cadena "cifrada" el base64 del texto y como resultado se obtenga el
 * propio texto.
 */
public class FakePasswordDecipher implements PasswordDecipher {

	@Override
	public char[] decipher(final byte[] cipheredPassword) throws IOException {
		return new String(
				cipheredPassword,
				StandardCharsets.UTF_8).toCharArray();
	}
}
