package es.gob.log.consumer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Adolfo.Navarro
 *
 */
public class LogTail {

	private  final int PART_SIZE = 1024;
	private  long filePosition = 0L;
	private  int totalBufferLines = 0;
	private final LogInfo logInfor;
	private final Path path;

	/**Constructor
	 * @throws InvalidPatternException */
	public LogTail(final LogInfo logInfo, final String path)  {
		this.logInfor = logInfo;
		this.path = Paths.get(path);

	}

	/**
	 * Permite consultar las &uacute;ltimas l&iacute;neas de un fichero de log.
	 * Se almacena la posici&oacute;n en la que se termina de leer (final del fichero) para que
	 * en una pr&oacute;xima llamada a getMoreLog(String log) se pueda continuar cargando desde
	 * ese punto.
	 * @param numLines N&uacute;mero de l&iacute;neas m&aacute;ximas a recuperar.
	 * @return &Uacute;ltimas l&iacute;neas del fichero.
	 * @throws IOException Cuando ocurre un error durante la lectura del fichero.
	 */
	public final StringBuilder getLogTail(final int numLines) throws IOException {
		StringBuilder result = new StringBuilder();
		// Creamos el canal asociado al fichero
		try (SeekableByteChannel channel = Files.newByteChannel(this.path, StandardOpenOption.READ)) {
			// Obtenemos el tamano del fichero. Creamos un array con las posiciones
			// obtenidas de la division del fichero en partes (PART_SIZE)
			final long totalSize = Files.size(this.path);
			setFilePosition(totalSize);
			final int totalNext =  (int) (totalSize/this.PART_SIZE);
			final int [] positions = new int [totalNext];
			for (int i = 0; i < totalNext; i++) {
				positions[i] = i * this.PART_SIZE;
			}
			if (positions.length > 0) {
				int i = 1;
				// Leemos el fichero n veces hasta obtener el numero de lineas indicadas,
				// obteniendo en cada lectura un nuevo bloque del fichero
				while (numLines > getTotalBufferLines()  && i <= positions.length) {
					final ByteBuffer buf =  ByteBuffer.allocate((int) totalSize - positions[positions.length - i]);
					channel.position(positions[positions.length - i]);
					channel.read(buf);
					buf.flip();
					final byte[]  data = new byte[buf.limit()];
					buf.get(data);
					//XXX: Esto carece de sentido, ya que en cada iteracion del bucle se
					// pisaria el objeto. Hay que revisarlo.
					result = readLines(data, numLines);
					buf.clear() ;
					i++;
				}
			}
		}
		return result;
	}

	/**
	 * Obtiene el m&aacute;ximo n&uacute;mero de l&iacute;neas del bloque de datos pasado como
	 * par&aacute;metro (data) hasta completar  el n&uacute;mero indicado en el par&aacute;metro
	 * (lines).
	 * @param data Datos de los que leer.
	 * @param lines N&uacute;mero de l&iacute;neas que leer.
	 * @return L&iacute;neas le&iacute;das.
	 * @throws IOException Cuando falla la lectura.
	 */
	private  StringBuilder readLines(final byte[] data, final int lines) throws IOException {
		final StringBuilder linesDataRead = new StringBuilder();
		try (	final ByteArrayInputStream bais = new ByteArrayInputStream(data);
				final InputStreamReader isr = new InputStreamReader(bais, this.logInfor.getCharset());
				final BufferedReader reader = new BufferedReader (isr)) {
			String line;
			int numLines = 0;
			final int totalLines = getNumLines(data);
			while ((line = reader.readLine()) != null) {
				numLines++;
				if (numLines > totalLines - lines) {
					linesDataRead.append(line).append('\n');
				}
			}
			setTotalBufferLines(numLines);
		}
		return linesDataRead;
	}

	/**
	 * Obtiene el n&uacute;mero de l&iacute;neas del bloque de datos pasado como par&aacute;metro (data)
	 * @param data
	 * @return
	 * @throws IOException
	 */
	private static  int getNumLines(final byte[] data) throws IOException {
		int numLines = 0;
		try (	final ByteArrayInputStream bais = new ByteArrayInputStream(data);
				final InputStreamReader isr = new InputStreamReader(bais);
				final BufferedReader reader = new BufferedReader (isr)) {
			while (reader.readLine() != null){
				numLines++;
			}
		}
		return numLines;
	}

	/**
	 * Establece la posici&oacute;n
	 * @param filePosition
	 */
	private final  void setFilePosition(final long filePosition) {
		this.filePosition = filePosition;
	}

	/**
	 * Obtiene la posici&oacute;n
	 * @return
	 */
	public final long getFilePosition() {
		return this.filePosition;
	}

	/**
	 * Obtiene el total de l&iacute;neas leidas
	 * @return
	 */
	private final int getTotalBufferLines() {
		return this.totalBufferLines;
	}

	/**
	 * Establece el total de l&iacute;neas leidas
	 * @param totalBufferLines
	 */
	private final  void setTotalBufferLines(final int totalBufferLines) {
		this.totalBufferLines = totalBufferLines;
	}
}
