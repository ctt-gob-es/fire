package es.gob.log.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogSearchText {

	private final LogInfo logInfor;
	private long filePosition;
	private int nLinesReaded = 0;


	private static final Logger LOGGER = Logger.getLogger(LogSearchText.class.getName());
	/**Constructor
	 * @throws InvalidPatternException */
	public LogSearchText(final LogInfo logInfo) throws InvalidPatternException {
		this.logInfor = logInfo;

		this.setFilePosition(0L);

	}

	/**
	 * Permite buscar un texto en un log. Una vez encontrado mostrar&aacute; la línea donde se encuentra dicho texto
	 * y las consecutivas l&iacute;neas hasta el m&aacute;ximo indicado como par&aacute;metro.
	 * @param numLines
	 * @param text
	 * @return
	 * @throws IOException
	 * @throws InvalidPatternException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public final  byte[] searchText(final int numLines, final String text,final LogReader reader) throws IOException, InvalidPatternException, InterruptedException, ExecutionException {
		return this.searchText(numLines,text,-1,reader);
	}

	/**
	 *  Permite buscar un texto en un log a partir de una Fecha indicada como par&aacute;metro.
	 *  Una vez encontrado mostrar&aacute; la línea donde se encuentra dicho texto
	 * 	y las consecutivas l&iacute;neas hasta el m&aacute;ximo indicado como par&aacute;metro.
	 * @param numLines
	 * @param text
	 * @param date
	 * @return
	 * @throws IOException
	 * @throws InvalidPatternException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public final  byte[] searchText(final int numLines, final String text, final long dateTimeMillisec, final LogReader reader) throws IOException, InvalidPatternException, InterruptedException, ExecutionException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		boolean found = false;
		final LogFilter filter = new LogFilter(this.logInfor);

		filter.loadReaderToSearch(reader);
			/*Se obtiene la fecha de b&uacute;squeda pasada en milisegundos si se ha pasado por par&aacute;metro
			 * y se obtiene la posici&oacute;n del comienzo de la l&iacute;nea en la que se encuentra la fecha indicada*/
		if(dateTimeMillisec != -1) {
			final Criteria crit = new Criteria();
			crit.setStartDate(dateTimeMillisec);
			filter.setCriteria(crit);
		}

		while (!found && !reader.isEndFile()) {
			final byte[] filteredLines = filter.filter(2);
			if( new String(filteredLines).indexOf(text) != -1) {
				baos.write(filteredLines);
				found = true;
			}
		}

		/* Se a&ntilde;aden el resto de l&iacute;neas */
		if(found) {

			int linesReaded = 0;
			if(baos.size() > 0 && baos.toByteArray() != null) {
				linesReaded = LogSearchText.countLines(baos.toByteArray());
			}
			baos.write(this.getText(numLines - linesReaded, reader));
			setnLinesReaded(getnLinesReaded() + linesReaded);
		}
		else {
			LOGGER.log(Level.INFO,"No se han encontrado m&aacute;s ocurrencias en la  b&uacute;squeda"); //$NON-NLS-1$
			return null;
//			setStatus(HttpServletResponse.SC_ACCEPTED);
//			baos.write("No se han encontrado m&aacute;s ocurrencias en la b&uacute;squeda".getBytes(this.logInfor.getCharset()));//$NON-NLS-1$
		}

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
		//setFilePosition(this.reader.getLinesToRead());
		setnLinesReaded(getnLinesReaded() + numLines);

		return result.getBytes(reader.getCharset());
	}

	private final static int countLines(final byte[] data) {
		int numLines = 0;
		for (int i = 0; i < data.length; i++ ) {
			if(data[i] == '\n') {
				numLines++;
			}
		}
		return numLines;
	}
	/**
	 * Obtiene la posici&oacute;n
	 * @return
	 */
	public final long getFilePosition() {
		return this.filePosition;
	}

	/**
	 * Establece la posici&oacute;n
	 * @return
	 */
	private final void setFilePosition(final long filePosition) {
		this.filePosition = filePosition;
	}

	public final int getnLinesReaded() {
		return this.nLinesReaded;
	}

	private final void setnLinesReaded(final int nLinesReaded) {
		this.nLinesReaded = nLinesReaded;
	}





}
