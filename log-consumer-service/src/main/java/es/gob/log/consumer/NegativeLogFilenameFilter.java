package es.gob.log.consumer;

import java.io.File;
import java.util.ArrayList;

/**
 * Filtro de nombres de fichero invertido a aplicar a los ficheros de log. Si el nombre
 * se ajusta a los patrones del filtro, no se mostrar&aacute;. El filtro se construye siempre
 * con los patrones "*.loginfo" y "*.lck" para no mostrar los ficheros con estas extensiones.
 * A estos patrones se les pueden agregar los que se deseen.
 */
public class NegativeLogFilenameFilter extends LogFilenameFilter {

	/**
	 * Crea el filtro s&oacute;lo con los patrones por defecto.
	 */
	public NegativeLogFilenameFilter() {
		this(null);
	}

	/**
	 * Crea el filtro con los patrones por defecto y los indicados.
	 */
	public NegativeLogFilenameFilter(final String[] patterns) {
		this.patternList = new ArrayList<>();
		this.patternList.add(new String[] { "", LogConstants.FILE_EXT_LOGINFO }); //$NON-NLS-1$
		this.patternList.add(new String[] { "", LogConstants.FILE_EXT_LCK }); //$NON-NLS-1$
		if (patterns != null) {
			for (final String pattern : patterns) {
				this.patternList.add(split(pattern, EXP_ASTERISK));
			}
		}
	}

	@Override
	public boolean accept(final File dir, final String name) {
		return !super.accept(dir, name);
	}

}
