package es.gob.log.consumer;

import java.io.IOException;
import java.nio.CharBuffer;


/**
 * Clase para la lectura de m&aacute;s l&iacute;neas de un fichero de log.
 */
public class LogMore {

	/**
	 * Lee nuevas l&iacute;neas de un log.
	 * @param numLines N&uacute;mero de l&iacute;neas a leer.
	 * @param reader Lector de logs.
	 * @return Bytes de las l&iacute;neas le&iacute;das.
	 * @throws IOException Si se produce un error durante la lectura o si se ha
	 * indicado un lector nulo.
	 */
	public static byte[] getLogMore( final int numLines, final LogReader reader) throws IOException {
		if (reader == null) {
			throw new IOException("No se ha cargado un fichero de log"); //$NON-NLS-1$
		}

		// Leemos el numero de lineas solicitadas,
		int lines = 1;
		CharBuffer lineReaded;
		String result = ""; //$NON-NLS-1$
		while (lines <= numLines && (lineReaded = reader.readLine()) != null) {
			lineReaded.rewind();
			result += lineReaded.toString() + "\n"; //$NON-NLS-1$
			lines ++;
		}
		return result.getBytes(reader.getCharset());
	}
}
