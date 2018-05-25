package es.gob.log.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

public class LogSearchText {

	private final LogInfo logInfor;
	private long filePosition;
	private final LogReader reader;
	private  LogErrors error = null;


	private String lineTextFound =  null;

	private static final Logger LOGGER = Logger.getLogger(LogSearchText.class.getName());
	/**Constructor
	 * @throws InvalidPatternException */
	public LogSearchText(final LogInfo logInfo,final LogReader reader) throws InvalidPatternException {
		this.logInfor = logInfo;
		this.reader = reader;
		this.setFilePosition(0L);
		this.error = null;

	}

	/**
	 * Permite buscar un texto en un log. Una vez encontrado mostrar&aacute; la línea donde se encuentra dicho texto
	 * y las consecutivas l&iacute;neas hasta el m&aacute;ximo indicado como par&aacute;metro.
	 * @param numLines
	 * @param text
	 * @return
	 * @throws IOException
	 */
	public final  byte[] searchText(final int numLines, final String text, final boolean reset) throws IOException {
		return this.searchText(numLines,text,-1,reset);
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
	 */
	public final  byte[] searchText(final int numLines, final String text, final long dateTimeMillisec, final boolean reset) throws IOException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {

			if(this.getError()!=null && this.getError().getMsgError()!= null && !"".equals(this.getError().getMsgError())) { //$NON-NLS-1$
				this.setError(null);
			}

			if(reset) {
				this.reader.load();
				this.setFilePosition(0L);
			}

			boolean found = false;
			/*Se obtiene la fecha de b&uacute;squeda pasada en milisegundos si se ha pasado por par&aacute;metro
			 * y se obtiene la posici&oacute;n del comienzo de la l&iacute;nea en la que se encuentra la fecha indicada*/
			if(dateTimeMillisec != -1) {

				final Criteria crit = new Criteria();
				crit.setStartDate(dateTimeMillisec);
				final LogFilter filter = new LogFilter(this.logInfor);
				filter.load(this.reader);
				filter.setCriteria(crit);
				final byte[] firstLine = filter.filter(1);
				if(firstLine != null && firstLine.length > 0 ) {
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
				found = this.search(text);
				if(found) {
					if(this.lineTextFound != null) {
						baos.write(this.lineTextFound.getBytes(this.reader.getCharset()));
						baos.write('\n');
					}
					int linesReaded = 0;
					if(baos.size() > 0 && baos.toByteArray() != null) {
						linesReaded = this.countLines(baos.toByteArray());
					}
					baos.write(this.getText( numLines - linesReaded));
				}
			}

			if (!found) {
				LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n buscar."); //$NON-NLS-1$
				this.error = new LogErrors("Error al procesar la petici&oacute;n buscar texto.",HttpServletResponse.SC_NOT_FOUND);			 //$NON-NLS-1$
				baos.write(this.error.getMsgError().getBytes(this.logInfor.getCharset()));
			}
			//reader.close();
		}
		catch (final InvalidPatternException e) {// | InterruptedException | ExecutionException e) {
			LOGGER.log(Level.SEVERE,"Error el patrón indicado con la forma de los registros del log, no es válido"); //$NON-NLS-1$
			this.error = new LogErrors("El patrón indicado con la forma de los registros del log, no es válido.",HttpServletResponse.SC_PRECONDITION_FAILED); //$NON-NLS-1$

			baos.write(this.error.getMsgError().getBytes(this.logInfor.getCharset()));
		}
		catch (final InterruptedException e) {
			LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n buscar."); //$NON-NLS-1$
			this.error = new LogErrors("Error al procesar la petici&oacute;n buscar texto.",HttpServletResponse.SC_CONFLICT); //$NON-NLS-1$
			baos.write(this.error.getMsgError().getBytes(this.logInfor.getCharset()));
		}
		catch (final ExecutionException e) {
			LOGGER.log(Level.SEVERE,"Error al procesar la petici&oacute;n buscar."); //$NON-NLS-1$
			this.error = new LogErrors("Error al procesar la petici&oacute;n buscar texto.",HttpServletResponse.SC_BAD_REQUEST); //$NON-NLS-1$
			baos.write(this.error.getMsgError().getBytes(this.logInfor.getCharset()));
		}
		return baos.toByteArray();
	}

	/**
	 * Establece (setFilePosition()) la posici&oacute;n del comienzo de la l&iacute;nea en la que se encuentra el texto indicado
	 * @param text
	 * @throws IOException
	 */
	private final boolean search(final String text) throws IOException {
			CharBuffer cbLine;
			while((cbLine = this.reader.readLine()) != null) {
				cbLine.rewind();
				final String line = cbLine.toString();
				if(line.indexOf(text) != -1) {
					this.lineTextFound = line;
					return true;
				}
			}
			setFilePosition(this.reader.getFilePosition());
			return false;
	}

	private final byte[] getText( final int lines) throws IOException {
		String result = ""; //$NON-NLS-1$
		CharBuffer cbLine;
		int numLines = 1;
		while(numLines < lines && (cbLine = this.reader.readLine()) != null) {
			result = result.concat(cbLine.toString()).concat("\n"); //$NON-NLS-1$
			numLines++;
		}
		return result.getBytes(this.reader.getCharset());
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

	public  final LogErrors getError() {
		return this.error;
	}
	/**
	 * Establece la posici&oacute;n
	 * @return
	 */
	public final void setError(final LogErrors error) {
		this.error = error;
	}
}
