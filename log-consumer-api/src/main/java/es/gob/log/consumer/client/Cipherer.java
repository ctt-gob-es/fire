package es.gob.log.consumer.client;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Clase para el cifrado del token de inicio de sesi&oacute;n.
 */
class Cipherer {

	private static final String CIPHER_CONFIG = "AES/GCM/NoPadding"; //$NON-NLS-1$

	private static final String CIPHER_ALGORITHM = "AES"; //$NON-NLS-1$

	private static final String CIPHER_PROVIDER = "SunJCE"; //$NON-NLS-1$

	static byte[] cipher(final byte[] data, final byte[] key, final byte[] iv)
			throws	GeneralSecurityException {

		// Se han encontrado problemas con el proveedor de BouncyCastle cuando se encuentra este
		// instalado con alta prioridad, asi que especificamos el proveedor concreto a utilizar
		final Cipher cipher = Cipher.getInstance(CIPHER_CONFIG, CIPHER_PROVIDER);

		final SecretKeySpec secretKey = new SecretKeySpec(key, CIPHER_ALGORITHM);
		final GCMParameterSpec ivSpec = new GCMParameterSpec(16 * Byte.SIZE, iv);

		cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

		return cipher.doFinal(data);
	}

//	public static void main(final String[] args) throws NoSuchAlgorithmException {
//		final KeyGenerator keyGen = KeyGenerator.getInstance(CIPHER_ALGORITHM);
//        keyGen.init(128);
//        final SecretKey secretKey = keyGen.generateKey();
//
//        System.out.println(Base64.encode(secretKey.getEncoded()));
//	}
}
