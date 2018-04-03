package es.gob.log.consumer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Clase para la lectura completa de un fichero por medio cargas segmentadas
 * en memoria.
 */
class FragmentedFileReader implements LogReader {

	/** Car&aacute;cter de BOM. */
	private static final int CHAR_BOM_PREFIX = 65279;

	/** Car&aacute;cter de fin. */
	private static final int CHAR_NULL = 0;

	/** Juego de caracteres por defecto. */
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/** Buffer de 512Kb a modo de cache. */
	private final ByteBuffer bBuffer;

	private final AsynchronousFileChannel channel;

	private final Charset charset;

	private boolean ignoreEmptyLines = false;

	private long filePosition = 0;

	private boolean moreData = true;

	private BufferedReader linesReader = null;

	private CharBuffer line = null;

	private CharBuffer nextLine = null;

	private CharBuffer nextLine2 = null;

	/**
	 * Crea el objeto para la carga de ficheros.
	 * @param channel Canal para la lectura del fichero.
	 * @param charset Conjunto de caracteres del fichero. Por defecto, UTF-8.
	 */
	public FragmentedFileReader(final AsynchronousFileChannel channel, final Charset charset) {

		if (channel == null) {
			throw new NullPointerException("El canal del lector no puede ser nulo"); //$NON-NLS-1$
		}

		this.bBuffer = ByteBuffer.allocate(512000);
		this.channel = channel;
		this.charset = charset != null ? charset : DEFAULT_CHARSET;
	}

	@Override
	public void setIgnoreEmptyLines(final boolean ignoreEmptyLines) {
		this.ignoreEmptyLines = ignoreEmptyLines;
	}

	@Override
	public Charset getCharset() {
		return this.charset;
	}

	@Override
	public void load() throws IOException {

		// Cacheamos un fragmento del fichero
		final Future<Integer> readerProcess = this.channel.read(this.bBuffer, this.filePosition);

		// Esperamos a que termine la lectura (espera intrinseca del get()) y vemos
		// cuanto hemos leido
		final int readedCount;
		try {
			readedCount =  readerProcess.get().intValue();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new IOException("Se interrumpio la carga del fichero", e); //$NON-NLS-1$
		}

		// Si se ha leido menos de la capacidad del buffer, es que hemos
		// llegado al final del fichero
		if (readedCount < this.bBuffer.capacity()) {
			this.moreData = false;
		}

		// Actualizamos la posicion del fichero
		this.filePosition += readedCount;

		// Preparamos un lector para la carga de lineas concretas y cargamos las dos primeras. Esta logica
		// presupone que la longitud de las dos lineas no excede el tamano del buffer
		this.linesReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.bBuffer.array()), this.charset));
		this.nextLine = readNewLine();
		// Si la primera linea tiene el caracter de BOM, nos lo saltamos
		if (this.nextLine.length() > 0) {
			if (this.nextLine.get() != CHAR_BOM_PREFIX) {
				this.nextLine.rewind();
			}
		}
		this.nextLine2 = readNewLine();
	}

	@Override
	public CharBuffer getCurrentLine() {
		return this.line;
	}

	@Override
	public CharBuffer readLine() throws IOException {

		// Si no encontramos el lector de lineas cargado, es que no se ha llamado al metodo de carga
		if (this.linesReader == null) {
			throw new IOException("No se ha llamado a la funcion de carga del fichero"); //$NON-NLS-1$
		}

		CharBuffer result = null;

		// Solo devolveremos una linea si ya la tenemos cargada (o al menos en parte)
		if (this.nextLine != null) {
			// Si hay una linea despues de la actual, es que la actual esta completa y la podemos devolver
			if (this.nextLine2 != null) {
				result = this.nextLine;
				this.nextLine = this.nextLine2;
				this.nextLine2 = readNewLine();
			}
			else {
				// Si no hay una linea despues de la actual pero no hemos detectado que se haya llegado al
				// final del fichero, cargamos un nuevo fragmento del fichero y recargamos las lineas actual
				// (que podria haberse quedado a mitad) y siguiente.
				if (this.moreData) {
					loadNextFragment();
					result = readNewLine();
					this.nextLine = readNewLine();
					this.nextLine2 = readNewLine();
				}
				// Si no hay mas datos, entonces esta es la ultima linea
				else {
					result = this.nextLine;
					this.nextLine = null;
					close();
				}
			}

			// Si se ha pedido que se ignoren las lineas vacias y se da esta condicion, se pasa directamente
			// a la siguiente
			if (this.ignoreEmptyLines && (result == null || result.length() == 0)) {
				result = readLine();
			}
		}

		this.line = result;

		return result;
	}

	/**
	 * Lee una nueva l&iacute;nea del fichero. Esta l&iacute;nea podr&iacute;a estar incompleta
	 * si nos encontramos al final de un fragmento.
	 * @return L&iacute;nea del fichero.
	 * @throws IOException Cuando ocurre un error en la lectura.
	 */
	private CharBuffer readNewLine() throws IOException {

		String newLine = this.linesReader.readLine();

		// Si el ultimo caracter de la linea es el del fin de fichero, cortamos la linea
		// para no incluir ninguno de estos caracteres
		if (newLine != null && newLine.length() > 0) {
			if (newLine.charAt(newLine.length() - 1) == CHAR_NULL) {
				int i = 0;
				while (newLine.charAt(i) != CHAR_NULL) {
					i++;
				}
				newLine = newLine.substring(0,  i);
			}
		}
		return newLine != null ? CharBuffer.wrap(newLine) : null;
	}

	/**
	 * Carga el siguiente fragmento del fichero.
	 * @throws IOException Cuando se produce un error al cargar el nuevo fragmento del fichero
	 * o al gestionar los recursos internos.
	 */
	private void loadNextFragment() throws IOException {

		// Retrasamos la posicion del fichero hasta el principio de la linea actual, para volver a
		// leer este trozo de linea en el nuevo fragmento
		final int remainingBytes = this.charset.encode(this.nextLine).flip().limit();
		this.filePosition -= remainingBytes;

		// Cacheamos un nuevo fragmento del fichero
		final Future<Integer> readerProcess = this.channel.read(this.bBuffer, this.filePosition);

		// Esperamos a que termine la lectura (espera intrinseca del get()) y vemos
		// cuanto hemos leido
		final int readedCount = 0;
		try {
			readerProcess.get().intValue();
		}
		catch (final Exception e) {
			throw new IOException("Error al cargar el nuevo fragmento del fichero", e); //$NON-NLS-1$
		}

		// Si se ha leido menos de la capacidad del buffer, es que hemos
		// llegado al final del fichero
		if (readedCount < this.bBuffer.capacity()) {
			this.moreData = false;
		}

		// Actualizamos la posicion en el fichero
		this.filePosition += readedCount;

		// Cerramos la conexion
		this.linesReader.close();

		// Preparamos el nuevo lector de lineas
		this.linesReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.bBuffer.array()), this.charset));
	}

	@Override
	public void rewind() throws IOException {
		this.filePosition = 0;
		load();
	}

	@Override
	public void close() throws IOException {
		this.linesReader.close();
		this.bBuffer.clear();
	}
}