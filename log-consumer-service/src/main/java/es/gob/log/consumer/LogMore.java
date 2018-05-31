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

//	private final   LogRegistryReader registryReader;
	private long filePosition ;

	private static final Logger LOGGER = Logger.getLogger(LogMore.class.getName());


	/**Constructor
	 * @throws InvalidPatternException */
//	public LogMore(final LogInfo logInfo) throws InvalidPatternException {
//		this.registryReader = new LogRegistryReader(logInfo);
//	}

	/**
	 *
	 * @param numLines
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public   byte[]  getLogMore( final int numLines, final LogReader reader ) throws IOException {
		String result = ""; //$NON-NLS-1$
		 if (reader == null) {
				throw new IOException("No se ha cargado un fichero de log"); //$NON-NLS-1$
			}

			// Leemos el numero de lineas solicitadas,
			int lines = 0;
			 CharBuffer lineReaded;
			while ( lines <= numLines && (lineReaded = reader.readLine()) != null) {
				lineReaded.rewind();
				result = result.concat(lineReaded.toString()).concat("\n"); //$NON-NLS-1$
				lines ++;
			}

			setFilePosition(reader.getFilePosition());
			return result.getBytes(reader.getCharset());

	}



	/**
	 *
	 * @return
	 */
	public final  long getFilePosition() {
		return this.filePosition;
	}


	public final void setFilePosition(final long filePosition) {
		this.filePosition = filePosition;
	}




}
