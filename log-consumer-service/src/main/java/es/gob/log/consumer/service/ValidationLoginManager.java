package es.gob.log.consumer.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ValidationLoginManager {

	private static final String CIPHER_CONFIG = "AES/CBC/PKCS5PADDING"; //$NON-NLS-1$

	private static final String CIPHER_ALGORITHM = "AES"; //$NON-NLS-1$

	public static byte[] process(final HttpServletRequest req, final HttpSession session)
			throws SessionException {

		final String cryptoToken = req.getParameter(ServiceParams.CIPHERED_TOKEN);
		if (cryptoToken == null) {
			throw new IllegalArgumentException("No se ha proporcionado el token cifrado"); //$NON-NLS-1$
		}

		byte[] cipheredToken;
		try {
			cipheredToken = decodeData(cryptoToken);
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("El token proporcionado no esta correctamente codificado", e); //$NON-NLS-1$
		}

		// Comprobamos que se hubiese solicitado el inicio de sesion
		final byte[] originalToken = (byte[]) session.getAttribute(SessionParams.TOKEN);
		if (originalToken == null) {
			throw new SessionException("No se realizo una solicitud previa de inicio de sesion"); //$NON-NLS-1$
		}

		final byte[] iv = (byte[]) session.getAttribute(SessionParams.IV);

		// Desciframos el token codificado
		byte[] decipheredToken;
		try {
			decipheredToken = decipherCryptoToken(cipheredToken, iv);
		}
		catch (final Exception e) {
			throw new SessionException("La informacion de acceso proporcionada no es valida", e); //$NON-NLS-1$
		}

		// Comprobamos que el token descifrado sea igual al token que se envio
		if (!Arrays.equals(originalToken, decipheredToken)) {
			session.invalidate();
			throw new SessionException("Token de sesion incorrecto"); //$NON-NLS-1$
		}

		// Validamos la sesion
		session.setAttribute(SessionParams.LOGGED, Boolean.TRUE);

		return buildResult();
	}

	private static byte[] decipherCryptoToken(final byte[] cipheredToken, final byte[] iv)
			throws	GeneralSecurityException {

		final byte[] cipherKey = ConfigManager.getInstance().getCipherKey();

		return decipher(cipheredToken, cipherKey, iv);
	}

	private static byte[] decipher(final byte[] cipheredData, final byte[] key, final byte[] iv)
			throws	GeneralSecurityException {

		final SecretKeySpec secretKey = new SecretKeySpec(key, CIPHER_ALGORITHM);
		final Cipher cipher = Cipher.getInstance(CIPHER_CONFIG);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
		final byte[] decipheredToken = cipher.doFinal(cipheredData);

		return decipheredToken;
	}

	private static byte[] buildResult() {

		final JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("ok", true); //$NON-NLS-1$

		return builder.build().toString().getBytes();
	}

	private static byte[] decodeData(final String encodedData) throws IOException {
		return Base64.decode(encodedData, true);
	}
}
