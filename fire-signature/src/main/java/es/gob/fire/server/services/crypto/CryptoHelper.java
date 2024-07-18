package es.gob.fire.server.services.crypto;

import java.security.PublicKey;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import es.gob.fire.server.services.internal.LogTransactionFormatter;

public class CryptoHelper {

	private static final Logger LOGGER = Logger.getLogger(CryptoHelper.class.getName());

	/**
     * Verifica que un PKCS#1 se pueda descifrar con la clave p&uacute;blica del certificado
     * asociado a la clave privada con la cual se gener&oacute;.
     * privada con la que en .
     * @param signatureValue PKCS#1 de la firma.
     * @param publicKey Clave p&uacute;blica con la que validar la firma.
     * @param logF Formateador de trazas de log.
     * @throws SecurityException Cuando el PKCS#1 se gener&oacute; con una clave privada distinta a
     * la esperada.
     */
    public static void verifyPkcs1(final byte[] signatureValue, final PublicKey publicKey, final LogTransactionFormatter logF) throws SecurityException {
    	try {

    		//TODO: Probar y soportar algoritmos de cifrado de curva eliptica
    		if (!"RSA".equalsIgnoreCase(publicKey.getAlgorithm())) { //$NON-NLS-1$
    			String msg = "Omitimos la validacion del PKCS#1 por no soportar la validacion del cifrado de claves" + publicKey.getAlgorithm(); //$NON-NLS-1$
    			if (logF != null) {
    				msg = logF.f(msg);
    			}
    			LOGGER.fine(msg);
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
