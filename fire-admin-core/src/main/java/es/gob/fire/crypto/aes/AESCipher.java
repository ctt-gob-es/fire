package es.gob.fire.crypto.aes;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.crypto.exceptions.CipherException;
import es.gob.fire.i18n.ICoreMessages;
import es.gob.fire.i18n.Language;

public class AESCipher implements Serializable {

	/**
	 * Constant that represents the parameter log.
	 */
	private static final Logger LOGGER = Logger.getLogger(AESCipher.class);

	/**
	 *
	 */
	private static final long serialVersionUID = -5111831850889412988L;

	/**
	 * Attribute that represents the key for decode the passwords.
	 */
	private static Key key;

	/**
	 * Attribute that represents an instance of the class.
	 */
	private static AESCipher instance = null;

	private static final String AES_PASSWORD = "ABCDEFGHIJKLMNOP";

	private static final String AES_ALGORITHM = "AES";

	private static final String AES_PADDING_ALG = "AES/ECB/PKCS5Padding";

	private static final String AES_NO_PADDING_ALG = "AES/GCM/NoPadding";

	/**
	 * Method that obtains an instance of the class.
	 * @return an instance of the class.
	 * @throws CipherException If the method fails.
	 */
	public static synchronized AESCipher getInstance() throws CipherException {
		if (instance == null) {
			instance = new AESCipher();
		}
		return instance;
	}

	/**
	 * Method that inits the class.
	 * @throws CipherException If the method fails.
	 */
	private AESCipher() throws CipherException {
		key = new SecretKeySpec(AES_PASSWORD.getBytes(), AES_ALGORITHM);
	}

	/**
	 * Method that encrypt a message.
	 * @param msg The message to encrypt.
	 * @return the message encrypted.
	 * @throws CipherException If the method fails.
	 */
	public String encryptMessageWithBC(final String msg) throws CipherException {
	    try {
	        //Cipher cipher = Cipher.getInstance(AES_PADDING_ALG, UtilsProviders.BC_PROVIDER);
	    	final Cipher cipher = Cipher.getInstance(AES_PADDING_ALG);
	        cipher.init(Cipher.ENCRYPT_MODE, key);
	        final byte[] encryptedBytes = cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8));
	        return Base64.getEncoder().encodeToString(encryptedBytes);
	    } catch (/* NoSuchProviderException | */NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
	        throw new CipherException(Language.getResCoreFire(ICoreMessages.LOG_CE001));
	    }
	}

	/**
	 * Method that decrypting a message.
	 * @param msg The message to decrypt.
	 * @return the message decrypted.
	 * @throws CipherException If the method fails.
	 */
	public String decryptMessageBC(final String msg) throws CipherException {
		try {
			//Cipher cipher = Cipher.getInstance(AES_NO_PADDING_ALG, UtilsProviders.BC_PROVIDER);
			final Cipher cipher = Cipher.getInstance(AES_NO_PADDING_ALG);
			final IvParameterSpec ivspec = new IvParameterSpec(key.getEncoded());
			cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
			final byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(msg));
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (final Exception e) {
			try {
				//Cipher cipher = Cipher.getInstance(AES_PADDING_ALG, UtilsProviders.BC_PROVIDER);
				final Cipher cipher = Cipher.getInstance(AES_PADDING_ALG);
				cipher.init(Cipher.DECRYPT_MODE, key);
				final byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(msg));
				return new String(decryptedBytes, StandardCharsets.UTF_8);
			} catch (NoSuchAlgorithmException | /* NoSuchProviderException | */NoSuchPaddingException | InvalidKeyException
					| IllegalBlockSizeException | BadPaddingException e2) {
				throw new CipherException(Language.getResCoreFire(ICoreMessages.LOG_CE001));
			}
		}
	}
}
