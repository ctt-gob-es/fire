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
public class FragmentedFileReader implements LogReader {

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

	private long fileFragmentPosition = 0;

	private boolean moreData = true;

	private BufferedReader linesReader = null;

	private CharBuffer line = null;

	private CharBuffer nextLine = null;

	private CharBuffer nextLine2 = null;

	private  boolean endFile = false;

	private  int linesReaded = 0;

	private  int linesToRead = 0;

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
		load(0L);
	}

	@Override
	public void load(final long position) throws IOException {

		setLinesReaded(0);
		setLinesToRead(0);
		setEndFile(false);

		this.fileFragmentPosition = position;

		// Cacheamos un fragmento del fichero


		//this.bBuffer.limit((int)this.channel.size());
		final Future<Integer> readerProcess = this.channel.read(this.bBuffer, this.fileFragmentPosition);

		// Esperamos a que termine la lectura (espera intrinseca del get()) y vemos
		// cuanto hemos leido

		try {
			//se establece el numero de lineas que van a ser leidas en el fragmento cargado;
			setLinesToRead(readerProcess.get().intValue());
			if(getLinesToRead() == -1) {
				throw new IOException("No hay datos a leer en este momento"); //$NON-NLS-1$
			}

		}
		catch (InterruptedException | ExecutionException e) {
			throw new IOException("Se interrumpio la carga del fichero", e); //$NON-NLS-1$
		}

		// Si se ha leido menos de la capacidad del buffer, es que hemos
		// llegado al final del fichero

		if (getLinesToRead() < this.bBuffer.capacity()) {
			this.moreData = false;
		}

		// Actualizamos la posicion del fichero
		this.fileFragmentPosition += getLinesToRead();

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

		if(this.endFile) {
			return result;
		}

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
					this.endFile = true;
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

		//Comprobamos que el acumulado de las lineas leidas son iguales a las lineas cargadas que se iban a leer
		// en dicho caso hemos llegado al final de las lineas que se tenian que leer.
		if(getLinesReaded() >= getLinesToRead() && this.nextLine == null ) {//getLinesReaded() > getLinesToRead()
			this.endFile = true;
		}

		return result;
	}

	/**
	 * Lee una nueva l&iacute;nea del fichero. Esta l&iacute;nea podr&iacute;a estar incompleta
	 * si nos encontramos al final de un fragmento.
	 * @return L&iacute;nea del fichero.
	 * @throws IOException Cuando ocurre un error en la lectura.
	 */
	private CharBuffer readNewLine() throws IOException {

		boolean moreLines = true;

		final CharBuffer  line1 = CharBuffer.allocate(12000);


		Character next_character = new Character((char) this.linesReader.read());
		final Character nIntro =  new Character('\n');
		final Character rIntro =  new Character('\r');

		while (next_character.charValue() != -1) {

			if(next_character.compareTo(rIntro) == 0 ) {
				setLinesReaded(getLinesReaded() + 2);
				final int next = this.linesReader.read();
				break;
			}
			if(next_character.compareTo(nIntro) == 0 ) {
				setLinesReaded(getLinesReaded() + 1);
				break;
			}

			setLinesReaded(getLinesReaded() + 1);
			line1.append(next_character.charValue());
			final String line = line1.toString();
			if(getLinesReaded() > getLinesToRead() ) {
				setLinesReaded(getLinesReaded() - 1 );
				this.filePosition += getLinesReaded();
				moreLines = false;
				break;
			}

			next_character = new Character((char) this.linesReader.read());


		}
//
//		//String newLine = this.linesReader.readLine();
		line1.flip();
		final String newLine = line1.toString();
		//System.out.println("Linea leida = " + newLine);
		line1.clear();
//		final String newLine = sbLine.toString();

		// Si el ultimo caracter de la linea es el del fin de fichero, cortamos la linea
		// para no incluir ninguno de estos caracteres
//		if (newLine != null && newLine.length() > 0) {
//
//			if (newLine.charAt(newLine.length() - 1) == CHAR_NULL) {
//				int i = 0;
//				while (newLine.charAt(i) != CHAR_NULL) {
//					i++;
//				}
//				newLine = newLine.substring(0,  i);
//			}
//		}
		return newLine != null && moreLines  ? CharBuffer.wrap(newLine) : null;
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
		this.fileFragmentPosition -= remainingBytes;

		// Cacheamos un nuevo fragmento del fichero
		final Future<Integer> readerProcess = this.channel.read(this.bBuffer, this.fileFragmentPosition);

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
		if (readedCount < this.bBuffer.capacity()) { //readedCount < this.bBuffer.capacity()
			this.moreData = false;
		}

		// Actualizamos la posicion en el fichero
		this.fileFragmentPosition += readedCount;//readedCount

		// Cerramos la conexion
		this.linesReader.close();

		// Preparamos el nuevo lector de lineas
		this.linesReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.bBuffer.array()), this.charset));

		//inicializamos la posición dentro
	}


	@Override
	public void reload(final long position) throws IOException {
//		if(getLinesReaded() > position) {
//			setLinesReaded(getLinesReaded() - 1);
//			this.filePosition += getLinesReaded();
//		}
		if(this.filePosition < position) {
			this.filePosition = position;
		}
		final long currentFilePosition = this.filePosition;
		close();
		load(currentFilePosition);
	}

	@Override
	public final long getFilePosition() {
		return this.filePosition;
	}

	@Override
	public long getFileFragmentedPosition() {
		return this.fileFragmentPosition;
	}

	@Override
	public void rewind() throws IOException {
		this.fileFragmentPosition = 0;
		load(this.fileFragmentPosition);
	}

	@Override
	public void close() throws IOException {

		this.linesReader.close();
		this.bBuffer.clear();
	}

	@Override
	public boolean isEndFile() {
		return this.endFile;
	}

	@Override
	public void setEndFile( final boolean endOfFile) {
		this.endFile = endOfFile;

	}



	@Override
	public int getLinesReaded() {
		return this.linesReaded;
	}


	@Override
	public void setLinesReaded(final int linesReaded) {
		this.linesReaded = linesReaded;

	}



	@Override
	public int getLinesToRead() {
		return this.linesToRead;
	}



	@Override
	public void setLinesToRead(final int linesToRead) {
		this.linesToRead = linesToRead;

	}
















}