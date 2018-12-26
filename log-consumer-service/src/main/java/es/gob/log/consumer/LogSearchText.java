package es.gob.log.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogSearchText {

	private static final Logger LOGGER = Logger.getLogger(LogSearchText.class.getName());

	private final LogInfo logInfo;
	private int nLinesReaded = 0;

	/** Construye el objeto para la b&uacute;squeda de texto en un log.  */
	public LogSearchText(final LogInfo logInfo) {
		this.logInfo = logInfo;
	}

	/**
	 * Permite buscar un texto en un log. Una vez encontrado mostrar&aacute; la l&iacute;nea
	 * donde se encuentra dicho texto y las consecutivas l&iacute;neas hasta el m&aacute;ximo
	 * indicado como par&aacute;metro.
	 * @param numLines N&uacute;mero m&aacute;ximo de l&iacute;neas a recuperar.
	 * @param text Texto a buscar.
	 * @param reader Lector del fichero de log.
	 * @return Bytes de los registros recuperados.
	 * @throws IOException Cuando ocurre un error durante la operaci&oacute;n.
	 * @throws InvalidPatternException Cuando se encuentra un loginfo para el fichero de log
	 * que configura un patr&oacute;n de registro inv&aacute;lido.
	 */
	public final  byte[] searchText(final int numLines, final String text, final LogReader reader) throws IOException, InvalidPatternException {
		return this.searchText(numLines, text, -1, reader);
	}

	/**
	 *  Permite buscar un texto en un log a partir de una Fecha indicada como par&aacute;metro.
	 *  Una vez encontrado mostrar&aacute; la l&iacute;nea donde se encuentra dicho texto
	 * 	y las consecutivas l&iacute;neas hasta el m&aacute;ximo indicado como par&aacute;metro.
	 * @param numLines N&uacute;mero m&aacute;ximo de l&iacute;neas a recuperar.
	 * @param text Texto a buscar.
	 * @param date Fecha m&iacute;nima en la que se puede debieron imprimir los registros.
	 * @return Bytes de los registros recuperados.
	 * @throws IOException Cuando ocurre un error durante la operaci&oacute;n.
	 * @throws InvalidPatternException Cuando se encuentra un loginfo para el fichero de log
	 * que configura un patr&oacute;n de registro inv&aacute;lido.
	 */
	public final byte[] searchText(final int numLines, final String text, final long dateTimeMillisec, final LogReader reader) throws IOException, InvalidPatternException {

		boolean found = false;
		final LogFilter filter = new LogFilter(this.logInfo);

		filter.loadReaderToSearch(reader);

		// Se obtiene la fecha de busqueda pasada en milisegundos si se ha pasado por parametro
		// y se obtiene la posicion del comienzo de la linea en la que se encuentra la fecha indicada
		if (dateTimeMillisec != -1) {
			final Criteria crit = new Criteria();
			crit.setStartDate(dateTimeMillisec);
			filter.setCriteria(crit);
		}

		// Se leen lineas hasta encontrar el texto o llegar al final del fichero
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!found && !reader.isEndFile()) {
			final byte[] filteredLines = filter.filter(2);
			if (new String(filteredLines).indexOf(text) != -1) {
				baos.write(filteredLines);
				found = true;
			}
		}

		// Se anaden el resto de lineas
		if (!found) {
			LOGGER.log(Level.INFO,"No se han encontrado mas ocurrencias en la  busqueda"); //$NON-NLS-1$
			return null;
		}

		int linesReaded = 0;
		if (baos.size() > 0 && baos.toByteArray() != null) {
			linesReaded = LogSearchText.countLines(baos.toByteArray());
		}
		baos.write(this.getText(numLines - linesReaded, reader));
		setnLinesReaded(getnLinesReaded() + linesReaded);

		return baos.toByteArray();
	}


	private final byte[] getText( final int lines, final LogReader reader) throws IOException {
		String result = ""; //$NON-NLS-1$
		CharBuffer cbLine;
		int numLines = 1;
		if(reader.getCurrentLine() != null) {
			result = result.concat(reader.getCurrentLine().toString()).concat("\n"); //$NON-NLS-1$
		}

		while(numLines < lines && (cbLine = reader.readLine()) != null) {
			cbLine.rewind();
			result = result.concat(cbLine.toString()).concat("\n"); //$NON-NLS-1$
			numLines++;
		}
		setnLinesReaded(getnLinesReaded() + numLines);

		return result.getBytes(reader.getCharset());
	}

	private final static int countLines(final byte[] data) {
		int numLines = 0;
		for (int i = 0; i < data.length; i++ ) {
			if (data[i] == '\n') {
				numLines++;
			}
		}
		return numLines;
	}

	private final int getnLinesReaded() {
		return this.nLinesReaded;
	}

	private final void setnLinesReaded(final int nLinesReaded) {
		this.nLinesReaded = nLinesReaded;
	}
}
