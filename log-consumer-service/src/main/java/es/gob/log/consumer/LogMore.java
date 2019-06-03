package es.gob.log.consumer;

import java.io.IOException;
import java.nio.CharBuffer;

/**
 * Clase para la obtenci&oacute;n de m&aacute;s registros de log partiendo de una
 * petici&oacute;n anterior.
 */
public class LogMore {

	/**
	 * Obtiene m&aacute;s registros de log partiendo de una
	 * petici&oacute;n anterior.
	 * @param numLines N&uacute;mero de l&iacute;neas a obtener.
	 * @param reader Lector del log.
	 * @return Bytes de las nuevas l&iacute;neas de log o {@code null} si no se ha le&iacute;do nada.
	 * @throws IOException Cuando ocurra un error durante la lectura.
	 */
	public static byte[] getLogMore( final int numLines, final LogReader reader) throws IOException {

		if (reader == null) {
			throw new IOException("No se ha cargado un fichero de log"); //$NON-NLS-1$
		}

		// Leemos el numero de lineas solicitadas,
		int lines = 1;
		CharBuffer lineReaded;

		final StringBuilder result = new StringBuilder();
		while (lines <= numLines && (lineReaded = reader.readLine()) != null) {
			lineReaded.rewind();
			result.append(lineReaded.toString()).append("\n"); //$NON-NLS-1$
			lines++;
		}

		// Si lines sigue siendo 1, es que no se ha leido nada
		if (lines == 1) {
			return null;
		}
		return result.toString().getBytes(reader.getCharset());
	}
}
