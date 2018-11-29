package es.gob.log.consumer;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.logging.Logger;


/**
 *
 * @author Adolfo.Navarro
 *
 */
public class LogMore {


	private static final Logger LOGGER = Logger.getLogger(LogMore.class.getName());



	/**
	 *
	 * @param numLines
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public   byte[]  getLogMore( final int numLines, final LogReader reader) throws IOException {
		String result = ""; //$NON-NLS-1$
		 if (reader == null) {
				throw new IOException("No se ha cargado un fichero de log"); //$NON-NLS-1$
			}

			// Leemos el numero de lineas solicitadas,
			int lines = 1;
			 CharBuffer lineReaded;

			while ( lines <= numLines && (lineReaded = reader.readLine()) != null) {
				lineReaded.rewind();
				result = result.concat(lineReaded.toString()).concat("\n"); //$NON-NLS-1$
				lines ++;

			}

			return result.getBytes(reader.getCharset());

	}



}
