package es.gob.fire.server.services.crypto;

import java.security.PublicKey;
import java.util.logging.Logger;

import javax.crypto.Cipher;

public class CryptoHelper {

	private static final Logger LOGGER = Logger.getLogger(CryptoHelper.class.getName());

	/**
     * Verifica que un PKCS#1 se pueda descifrar con la clave p&uacute;blica del certificado
     * asociado a la clave privada con la cual se gener&oacute;.
     * privada con la que en .
     * @param signatureValue PKCS#1 de la firma.
     * @param publicKey Clave p&uacute;blica con la que validar la firma.
     * @param signatureAlgoritm Algoritmo de firma.
     * @throws InvalidVerificationCodeException Cuando no se proporciona un par&aacute;metro v&aacute;lido o
     * el PKCS#1 se gener&oacute; con una clave privada distinta a la esperada.
     */
    public static void verifyPkcs1(final byte[] signatureValue, final PublicKey publicKey) throws SecurityException {
    	try {

    		//TODO: Probar y soportar algoritmos de cifrado de curva eliptica
    		if (!"RSA".equalsIgnoreCase(publicKey.getAlgorithm())) { //$NON-NLS-1$
    			LOGGER.warning("No se soporta la validacion del PKCS#1 con el algoritmo de cifrado asociado a la clave de firma utilizada"); //$NON-NLS-1$
    			return;
    		}

    		final Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
    		cipher.init(Cipher.DECRYPT_MODE, publicKey);
    		cipher.doFinal(signatureValue);
    	}
    	catch (final Exception e) {
    		throw new SecurityException("El PKCS#1 de la firma no se ha generado con el certificado indicado", e); //$NON-NLS-1$
    	}
    }
}
