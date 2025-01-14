package es.gob.fire.server.services;

/**
 * Clase con m&eacute;todos de utilidad para la impresi&oacute;n de logs.
 * @author Carlos.J.Raboso
 */
public class LogUtils {

	/** Tama&ntilde;o al que por defecto se recortan los textos. */
	public static final int DEFAULT_MAX_TEXT_LENGTH = 100;

	/**
	 * Eliminan los saltos de l&iacute;nea.
	 * @param text Texto de entrada.
	 * @return Texto sin saltos de l&iacute;nea o {@code null} si la entrada fue {@code null}.
	 */
	public static String cleanText(final String text) {
		return text != null ? text.replaceAll("[\r\n]", "") : null;  //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Recorta una cadena de texto a {@code DEFAULT_MAX_TEXT_LENGTH} caracteres y elimina los
	 * saltos de l&iacute;nea que contenga.
	 * @param text Texto de entrada.
	 * @return Texto recortado a los primeros {@code DEFAULT_MAX_TEXT_LENGTH} caracteres o el
	 * mismo texto de entrada si era menor.
	 */
	public static String limitText(final String text) {
		return limitText(text, DEFAULT_MAX_TEXT_LENGTH);
	}

	/**
	 * Recorta una cadena de texto si es mayor que el tama&ntilde;o indicado y elimina los
	 * saltos de l&iacute;nea que contenga.
	 * @param text Texto de entrada.
	 * @param limit Tama&ntilde;o m&aacute;ximo permitido.
	 * @return Texto recortado a los primeros {@code limit} caracteres o el mismo texto de
	 * entrada si era menor.
	 */
	public static String limitText(final String text, final int limit) {
		return text != null
				? cleanText(text.length() > limit
						? text.substring(0, limit)
						: text)
				: null;
	}
}
