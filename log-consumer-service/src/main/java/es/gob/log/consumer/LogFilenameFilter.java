package es.gob.log.consumer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtro de nombres de fichero a aplicar a los ficheros de log.
 */
public class LogFilenameFilter implements FilenameFilter {

	protected static final String EXP_ASTERISK = "*"; //$NON-NLS-1$

	protected List<String[]> patternList;

	/**
	 * Crea el filtro sin patrones.
	 */
	protected LogFilenameFilter() {
		this(null);
	}

	/**
	 * Crea el filtro con los patrones indicados.
	 * @param patterns Patrones de fichero aceptados.
	 */
	public LogFilenameFilter(final String[] patterns) {
		this.patternList = new ArrayList<>();
		if (patterns != null) {
			for (final String pattern : patterns) {
				this.patternList.add(split(pattern, EXP_ASTERISK));
			}
		}
	}

	@Override
	public boolean accept(final File dir, final String name) {
		for (final String[] pattern : this.patternList) {
			if (match(name, pattern)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Funci&oacute;n de division de cadenas que tiene en cuenta los separadores al
	 * final de la cadena.
	 * @param text Texto a dividir.
	 * @param sep Separador de textos.
	 * @return Listado de subcadenas.
	 */
	public static String[] split(final String text, final String sep) {
		int pos1 = 0;
		int pos2;
		final List<String> parts = new ArrayList<>();
		do {
			pos2 = text.indexOf(sep, pos1);
			if (pos2 != -1) {
				parts.add(text.substring(pos1, pos2));
				pos1 = pos2 + sep.length();
			}
		} while (pos2 != -1);

		// Lo que sigue al ultimo separador, es el ultimo fragmento
		parts.add(text.substring(pos1));

		return parts.toArray(new String[0]);
	}

	/**
	 * Comprueba si el texto se ajusta a un patr&oacute;n formato por partes que debe
	 * contener el texto en orden.
	 * @param text Texto a comprobar.
	 * @param pattern Patr&oacute;n.
	 * @return {@code true} si el texto se ajusta al patr&oacute;n, {@code false} en caso
	 * contrario.
	 */
	public static boolean match(final String text, final String[] pattern) {

		// Si solo hay un patron, la cadena debe ser igual a el
		if (pattern.length == 1) {
			return pattern[0].equals(text);
		}

		int pos = 0;
		for (int i = 0; i < pattern.length; i++) {
			final String part = pattern[i];

			// Si una parte del patron esta vacia, se cumple automaticamente esta parte
			if (part.isEmpty()) {
				continue;
			}

			pos = text.indexOf(part, pos);

			// Si no encontramos el fragmento, falla el patron
			if (pos == -1) {
				return false;
			}
			// Si esta parte es la primera, el texto tiene que empezar por ella
			if (i == 0 && !text.startsWith(part)) {
				return false;
			}
			// Si esta parte es la ultima, el texto tiene que terminar por ella
			if (i == pattern.length - 1 &&  !text.endsWith(part)) {
				return false;
			}
			pos += part.length();
		}
		return true;
	}
}
