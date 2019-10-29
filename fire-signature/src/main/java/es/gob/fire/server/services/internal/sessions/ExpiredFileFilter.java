package es.gob.fire.server.services.internal.sessions;

import java.io.File;
import java.io.FileFilter;

/**
 * Filtro de ficheros para la obtenci&oacute;n de ficheros de datos
 * que se hayan modificado hace m&aacute;s del tiempo indicado.
 */
class ExpiredFileFilter implements FileFilter {

	private final long timeoutMillis;

	/**
	 * Tiempo m&aacute;ximo de vigencia de un fichero.
	 * @param timeout Tiempo de vigencia en milisegundos.
	 */
	public ExpiredFileFilter(final long timeout) {
		this.timeoutMillis = timeout;
	}

	@Override
	public boolean accept(final File pathname) {
		return pathname.isFile() && System.currentTimeMillis() > pathname.lastModified() + this.timeoutMillis;
	}
}
