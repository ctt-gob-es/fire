package es.gob.fire.test.webapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import es.gob.fire.client.PasswordDecipher;

/**
 * Descifrador de contrase&ntilde;as de ejemplo. Esta implementaci&oacute;n
 * realmente no descifra. Utiliza como contrase&ntilde;a la propia entrada.
 */
public class FakePasswordDecipher implements PasswordDecipher {

	@Override
	public char[] decipher(final byte[] cipheredPassword) throws IOException {
		return new String(
				cipheredPassword,
				StandardCharsets.UTF_8).toCharArray();
	}
}
