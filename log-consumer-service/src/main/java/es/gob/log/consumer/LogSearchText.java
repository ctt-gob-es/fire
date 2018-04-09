package es.gob.log.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class LogSearchText {

	private final LogInfo logInfor;
	private long filePosition;
	private final Path path;

	private String lineTextFound =  null;


	/**Constructor
	 * @throws InvalidPatternException */
	public LogSearchText(final LogInfo logInfo,final String path) throws InvalidPatternException {
		this.logInfor = logInfo;
		this.path = Paths.get(path);
		this.setFilePosition(0L);

	}

	/**
	 * Permite buscar un texto en un log. Una vez encontrado mostrar&aacute; la línea donde se encuentra dicho texto
	 * y las consecutivas l&iacute;neas hasta el m&aacute;ximo indicado como par&aacute;metro.
	 * @param numLines
	 * @param text
	 * @return
	 */
	public final  byte[] searchText(final int numLines, final String text) {
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
	 */
	public final  byte[] searchText(final int numLines, final String text, final long dateTimeMillisec) {
		final Calendar calendar = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final AsynchronousFileChannel channel = AsynchronousFileChannel.open(this.path,
				StandardOpenOption.READ);) {

			boolean found = false;
			final LogReader reader = new FragmentedFileReader(channel, this.logInfor.getCharset());
			reader.load();
			/*Se obtiene la fecha de b&uacute;squeda pasada en milisegundos si se ha pasado por par&aacute;metro
			 * y se obtiene la posici&oacute;n del comienzo de la l&iacute;nea en la que se encuentra la fecha indicada*/
			if(dateTimeMillisec != -1) {

//				final String logFormatDateTime = this.logInfor.getDateFormat();
//				final DateFormat formatter = new SimpleDateFormat(logFormatDateTime);

				final Criteria crit = new Criteria();
				crit.setStartDate(dateTimeMillisec);
//				crit.setEndDate(calendar.getTimeInMillis());
				final LogFilter filter = new LogFilter(this.logInfor);
				filter.load(reader);
				filter.setCriteria(crit);
				final byte[] firstLine = filter.filter(1);
				if(firstLine!=null && firstLine.length > 0 ) {
					found = true;
				}

				if( new String(firstLine).indexOf(text) != -1) {
					baos.write(firstLine);
					baos.write('\n');
					found = true;
				}
			}
			/*Caso de haber encontrado la fecha o que no se haya pedido fecha,
			 * se continua buscando la cadena de texto*/
			if((found || dateTimeMillisec == -1) && text != null && !"".equals(text)) { //$NON-NLS-1$
				found = this.search(text, reader);
				if(found) {
					if(this.lineTextFound != null) {
						baos.write(this.lineTextFound.getBytes());
						baos.write('\n');
					}
					int linesReaded = 0;
					if(baos.size() > 0 && baos.toByteArray() != null) {
						linesReaded = this.countLines(baos.toByteArray());
					}
					baos.write(this.getText(reader, numLines - linesReaded));
				}
			}

			if (!found) {
				baos.write( "Texto No encontrado".getBytes()); //$NON-NLS-1$
			}
			reader.close();
		}
		catch (final IOException | InvalidPatternException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return baos.toByteArray();
	}

	/**
	 * Establece (setFilePosition()) la posici&oacute;n del comienzo de la l&iacute;nea en la que se encuentra el texto indicado
	 * @param text
	 * @throws IOException
	 */
	private final boolean search(final String text, final LogReader reader) throws IOException {
			CharBuffer cbLine;
			while((cbLine = reader.readLine()) != null) {
				cbLine.rewind();
				final String line = cbLine.toString();
				if(line.indexOf(text) != -1) {
					this.lineTextFound = line;
					return true;
				}
			}
			return false;
	}

	private final byte[] getText(final LogReader reader, final int lines) throws IOException {
		String result = ""; //$NON-NLS-1$
		CharBuffer cbLine;
		int numLines = 1;
		while(numLines < lines && (cbLine = reader.readLine()) != null) {
			result = result.concat(cbLine.toString()).concat("\n"); //$NON-NLS-1$
			numLines++;
		}
		return result.getBytes();
	}

	private final int countLines(final byte[] data) {
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
	protected final long getFilePosition() {
		return this.filePosition;
	}

	/**
	 * Establece la posici&oacute;n
	 * @return
	 */
	private final void setFilePosition(final long filePosition) {
		this.filePosition = filePosition;
	}


}
