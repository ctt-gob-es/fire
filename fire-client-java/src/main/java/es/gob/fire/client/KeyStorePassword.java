package es.gob.fire.client;

import java.io.IOException;

/**
 * Gestiona la contrase&ntilde;a de un almacen de claves o certificados, encargandose
 * de descifrarlas si es necesario.
 */
public class KeyStorePassword {

	private static final String SEPARATOR = ":"; //$NON-NLS-1$
	private static final String PREFIX_CIPHERED_PASSWORD = "{@ciphered" + SEPARATOR; //$NON-NLS-1$
	private static final String SUFIX_CIPHERED_PASSWORD = "}"; //$NON-NLS-1$

	private final String passwordText;
	private final PasswordDecipher decipher;

	/**
	 * Construye la contrase&ntilde;a de un almac&eacute;n.
	 * @param passwordText Texto configurado como contrase&ntilde;a.
	 * @param decipher Objeto para el descifrado.
	 */
	public KeyStorePassword(final String passwordText, final PasswordDecipher decipher) {

		if (passwordText == null) {
			throw new IllegalArgumentException("El texto de contrasena no puede ser nulo"); //$NON-NLS-1$
		}

		this.passwordText = passwordText;
		this.decipher = decipher;
	}

	/**
	 * Obtiene la contrase&ntilde;a del almac&eacute;n.
	 * @return Contrase&ntilde;a.
	 * @throws IOException Cuando la contrase&ntilde;a esta cifrada y no se ha establecido
	 * el objeto para descifrar o cuando ocurre un error al descifrarla.
	 */
	public char[] getPassword() throws IOException {

		if (checkCipheredPassword(this.passwordText)) {
			if (this.decipher == null) {
				throw new IOException("Se definio una contrasena cifrada y no la clase para descifrarla"); //$NON-NLS-1$
			}
			return this.decipher.decipher(getCipheredText(this.passwordText));
		}
		return this.passwordText.toCharArray();
	}


	/**
	 * Comprueba si la cadena de la contrase&ntilde;a se corresponde con el de una
	 * contrase&ntilde;a cifrada.
	 * @param text Cadena de contrase&ntilde;a.
	 * @return {@code true} si la contrase&ntilde;a est&aacute; cifrada. {@code false},
	 * en caso contrario.
	 */
	private static boolean checkCipheredPassword(final String text) {
		return text.toLowerCase().startsWith(PREFIX_CIPHERED_PASSWORD) &&
				text.toLowerCase().endsWith(SUFIX_CIPHERED_PASSWORD);
	}

	/**
	 * Texto cifrado del que extraer la contrase&ntilde;a.
	 * @param text Texto con los marcadores que se&ntilde;alan que la contrase&ntilde;a esta
	 * cifrada y el propio texto cifrado en base 64.
	 * @return  Constrase&ntilde;a descifrada.
	 * @throws IOException Cuando ocurre un error al descifrar los datos.
	 */
	private static byte[] getCipheredText(final String text) throws IOException {
		final String base64Text = text.substring(
				text.toLowerCase().indexOf(SEPARATOR) + SEPARATOR.length(),
				text.toLowerCase().lastIndexOf(SUFIX_CIPHERED_PASSWORD)).trim();
		return Base64.decode(base64Text);
	}
}
