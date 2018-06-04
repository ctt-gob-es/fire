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
	private final LogReader reader;
//	private int status = HttpServletResponse.SC_OK;


	private static final Logger LOGGER = Logger.getLogger(LogSearchText.class.getName());
	/**Constructor
	 * @throws InvalidPatternException */
	public LogSearchText(final LogInfo logInfo,final LogReader reader) throws InvalidPatternException {
		this.logInfor = logInfo;
		this.reader = reader;
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
	public final  byte[] searchText(final int numLines, final String text) throws IOException, InvalidPatternException, InterruptedException, ExecutionException {
		return this.searchText(numLines,text,-1);
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
	public final  byte[] searchText(final int numLines, final String text, final long dateTimeMillisec) throws IOException, InvalidPatternException, InterruptedException, ExecutionException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		boolean found = false;
		final LogFilter filter = new LogFilter(this.logInfor);
		filter.load(this.reader);
			/*Se obtiene la fecha de b&uacute;squeda pasada en milisegundos si se ha pasado por par&aacute;metro
			 * y se obtiene la posici&oacute;n del comienzo de la l&iacute;nea en la que se encuentra la fecha indicada*/
		if(dateTimeMillisec != -1) {
			final Criteria crit = new Criteria();
			crit.setStartDate(dateTimeMillisec);
			filter.setCriteria(crit);
		}

		while (!found && !this.reader.isEndFile()) {
			final byte[] filteredLines = filter.filter(10);
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
			baos.write(this.getText( numLines - linesReaded));
		}
		else {
			LOGGER.log(Level.INFO,"No se han encontrado m&aacute;s ocurrencias en la  b&uacute;squeda"); //$NON-NLS-1$
			return null;
//			setStatus(HttpServletResponse.SC_ACCEPTED);
//			baos.write("No se han encontrado m&aacute;s ocurrencias en la b&uacute;squeda".getBytes(this.logInfor.getCharset()));//$NON-NLS-1$
		}

		return baos.toByteArray();
	}


	private final byte[] getText( final int lines) throws IOException {
		String result = ""; //$NON-NLS-1$
		CharBuffer cbLine;
		int numLines = 1;
		while(numLines < lines && (cbLine = this.reader.readLine()) != null) {
			result = result.concat(cbLine.toString()).concat("\n"); //$NON-NLS-1$
			numLines++;
		}
		setFilePosition(this.reader.getFilePosition());
		return result.getBytes(this.reader.getCharset());
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

//	public final int getStatus() {
//		return this.status;
//	}
//
//	public final void setStatus(final int status) {
//		this.status = status;
//	}




}
