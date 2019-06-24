package es.gob.fire.server.admin.service;

/**
 * Clase para realizar un codigo que nos sirva para no tener saltos de linea ni espacios en blanco
 * @author Carlos.J.Raboso
 */
public class LogUtils {

	/**
	 * Se obtiene una sintaxis para quitar los saltos de linea y los espacios en blanco del texto
	 * @param text
	 * @return
	 */
	public static String cleanText(final String text) {
		return text.replaceAll("[\r\n]", "");  //$NON-NLS-1$//$NON-NLS-2$
	}


}
