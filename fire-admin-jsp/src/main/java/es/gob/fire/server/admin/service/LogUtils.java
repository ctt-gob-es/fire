package es.gob.fire.server.admin.service;

/**
 * Clase con m&eacute;todos de utilidad para la impresi&oacute;n de logs.
 * @author Carlos.J.Raboso
 */
public class LogUtils {

	/**
	 * Eliminan los saltos de l&iacute;nea.
	 * @param text Texto de entrada.
	 * @return Texto sin saltos de l&iacute;nea o {@code null} si la entrada fue {@code null}.
	 */
	public static String cleanText(final String text) {
		return text != null ? text.replaceAll("[\r\n]", "") : null;  //$NON-NLS-1$ //$NON-NLS-2$
	}
}
