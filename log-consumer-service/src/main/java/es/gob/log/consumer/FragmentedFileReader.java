package es.gob.log.consumer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

/**
 * Clase para la lectura completa de un fichero por medio cargas segmentadas
 * en memoria.
 */
public class FragmentedFileReader implements LogReader {

	/** Car&aacute;cter de BOM. */
	private static final int CHAR_BOM_PREFIX = 65279;

	/** Juego de caracteres por defecto. */
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/** Car&aacute;cter de salto de l&iacute;nea. */
	private static final char N_INTRO =  '\n';
	/** Car&aacute;cter de salto de retorno de carro. */
	private static final char R_INTRO =  '\r';

	/** Tama&ntilde;o de cada fragmento cacheado. */
	private static final int BUFFER_SIZE = 512000;

	/** Buffer donde se almacenaran los fragmentos que funcionaran a modo de cache
	 * con tamano {@link #BUFFER_SIZE}. */
	private final ByteBuffer bBuffer;

	private final AsynchronousFileChannel channel;

	private final Charset charset;

	private boolean ignoreEmptyLines = false;

	private long filePosition = 0;

	private long fileFragmentPosition = 0;

	private boolean moreData = true;

	/** Lector de bytes del fragmento actualmente cargado. */
	private BufferedReader bufferReader = null;

	private CharBuffer currentLine = null;

	private CharBuffer nextLine = null;

	private CharBuffer nextLine2 = null;

	private  boolean endFile = false;

	private   int charactersReaded = 0;

	private   int charactersToRead = 0;

	private  boolean reloaded = false;
	/**
	 * Crea el objeto para la carga de ficheros.
	 * @param channel Canal para la lectura del fichero.
	 * @param charset Conjunto de caracteres del fichero. Por defecto, UTF-8.
	 */
	public FragmentedFileReader(final AsynchronousFileChannel channel, final Charset charset) {

		if (channel == null) {
			throw new NullPointerException("El canal del lector no puede ser nulo"); //$NON-NLS-1$
		}

		this.bBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		this.channel = channel;
		this.charset = charset != null ? charset : DEFAULT_CHARSET;
	}

	@Override
	public void load() throws IOException {
		load(0L);
	}

	@Override
	public void load(final long position) throws IOException {

		this.charactersReaded = 0;
		this.charactersToRead = 0;
		this.currentLine = null;
		this.moreData = true;

		setEndFile(false);

		this.fileFragmentPosition = position;

		// Cacheamos un fragmento del fichero
		//this.bBuffer.limit((int)this.channel.size());
		final Future<Integer> readerProcess = this.channel.read(this.bBuffer, this.fileFragmentPosition);

		// Esperamos a que termine la lectura (espera intrinseca del get()) y vemos
		// el tamano del fragmento de fichero que hemos cargado

		try {
			// Se establece el numero de caracteres disponibles en el fragmento cargado
			this.charactersToRead = readerProcess.get().intValue();

			// Si el fragmento esta vacio, hemos terminado
			if (this.charactersToRead == -1) {
				this.setEndFile(true);
				return;
			}
		}
		catch (InterruptedException | ExecutionException e) {
			throw new IOException("Se interrumpio la carga del fichero", e); //$NON-NLS-1$
		}

		// Si el fragmento que hemos cargado para lectura no es tan grande como el buffer que
		// teniamos preparado, es porque este es el ultimo fragmento del fichero
		if (this.charactersToRead < this.bBuffer.capacity()) {
			this.moreData = false;
		}

		// Actualizamos la posicion del fichero para que futuras lecturas se hagan a partir de la misma
		this.fileFragmentPosition += this.charactersToRead;

		// Creamos un lector sobre el fragmento leido y leemos las dos primeras. Esta logica
		// presupone que la longitud de las dos lineas no excede el tamano del buffer
		this.bufferReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.bBuffer.array()), this.charset));
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
	public CharBuffer readLine() throws IOException {

		this.reloaded = false;

		// Si no encontramos un fragmento cargado, es que no se ha llamado al metodo de carga
		if (this.bufferReader == null) {
			throw new IOException("No se ha llamado a la funcion de carga del fichero"); //$NON-NLS-1$
		}

		// Si no tenemos la siguiente linea cargada, es que la ultima que se devolvio era realmente la ultima
		if (this.nextLine == null) {
			this.endFile = true;
			return null;
		}

		// Si hay una linea despues de la siguiente, es que la siguiente esta completa y la podemos devolver.
		// Despues, actualizamos las marcadas como "lineas siguientes"
		if (this.nextLine2 != null) {
			this.currentLine = this.nextLine;
			this.nextLine = this.nextLine2;
			this.nextLine2 = readNewLine();
		}
		// Si no hay una linea despues de la actual pero aun quedan mas datos en el fichero, cargamos un
		// nuevo fragmento partiendo desde el inicio de la linea actual y leemos las siguientes lineas. Esto
		// permite que si la ultima linea se quedo a mitad ahora se cargue completa. La primera linea que
		// sera la actual
		else if (this.moreData) {
			loadNextFragment();
			this.currentLine = readNewLine();
			this.nextLine = readNewLine();
			this.nextLine2 = readNewLine();
		}
		// Si no hay mas fragmentos, entonces la unica linea que tenemos es la actual
		else {
			this.currentLine = this.nextLine;
			this.nextLine = null;
			this.nextLine2 = null;
			close();				//TODO: POR QUE?? REVISAR SI SE PUEDE SUSTITUIR EL PROCESO DE RECARGA POR UNA MEJOR GESTION DEL CIERRE
		}

//		// Comprobamos si el numero de caracteres leidos es igual o superior al numero de caracteres que se debian
//		// leer. En dicho caso, hemos llegado al final del fichero
//		if (this.charactersReaded >= this.charactersToRead && this.nextLine == null) {
//			this.endFile = true;
//		}

		// Si se ha pedido que se ignoren las lineas vacias y esta lo esta, se lee la siguiente
		if (this.ignoreEmptyLines && this.currentLine != null && this.currentLine.length() == 0) {
			this.currentLine = readLine();
		}

		return this.currentLine;
	}

	/**
	 * Lee una nueva l&iacute;nea del fichero. Esta l&iacute;nea podr&iacute;a estar incompleta
	 * si nos encontramos al final de un fragmento.
	 * Lee caracter a caracter hasta encontrar un intro (windows \r\n, linux \n)
	   formando la l&iacute;nea completa y obteniendo el total de caracteres leidos incluidos los intro,
	   si no hay final de fichero y los caracteres leidos son superiores a los que se iban a leer,
	   se intrerrumpe pues no hay m&aacute;s l&iacute;neas a leer en ese momento.
	 * @return L&iacute;nea del fichero.
	 * @throws IOException Cuando ocurre un error en la lectura.
	 */
	private CharBuffer readNewLine() throws IOException {

		boolean moreLines = true;

		final CharBuffer  line = CharBuffer.allocate(25000);

		char nextCharacter;
		while ((nextCharacter = (char) this.bufferReader.read()) != -1) {

			if (nextCharacter == R_INTRO) {
				this.charactersReaded += 2;
				this.bufferReader.read();
				break;
			}
			if (nextCharacter == N_INTRO) {
				this.charactersReaded++;
				break;
			}

			this.charactersReaded++;
			try {
				line.append(nextCharacter);
			}
			catch (final Exception e) {
				// La linea es demasiado larga y la cortamos ahi
				LoggerFactory.getLogger(FragmentedFileReader.class).warn("Linea demasiado larga. Se cortara"); //$NON-NLS-1$
				this.charactersReaded--;
				this.filePosition += this.charactersReaded;
				moreLines = true;
				break;
			}

			if (this.charactersReaded > this.charactersToRead ) {
				this.charactersReaded--;
				this.filePosition += this.charactersReaded;
				moreLines = false;
				break;
			}
		}

		line.flip();
		final String newLine = line.toString();
		line.clear();

		return newLine != null && moreLines ? CharBuffer.wrap(newLine) : null;
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
		this.charactersToRead -= remainingBytes;

		// Vaciamos el buffer y cacheamos un nuevo fragmento del fichero
		this.bBuffer.clear();
		final Future<Integer> readerProcess = this.channel.read(this.bBuffer, this.fileFragmentPosition);

		// Esperamos a que termine la lectura (espera intrinseca del get()) y vemos
		// cuanto hemos leido
		int readedCount = 0;
		try {
			readedCount = readerProcess.get().intValue();
		}
		catch (final Exception e) {
			throw new IOException("Error al cargar el nuevo fragmento del fichero", e); //$NON-NLS-1$
		}

		// Si se ha leido menos de la capacidad del buffer, es que hemos
		// llegado al final del fichero
		if (readedCount < this.bBuffer.capacity()) {
			this.moreData = false;
		}

		this.charactersToRead += readedCount;

		// Actualizamos la posicion en el fichero
		this.fileFragmentPosition += readedCount;//readedCount

		// Cerramos la conexion
		this.bufferReader.close();

		// Preparamos el nuevo lector de lineas. En caso de que se haya leido menos de la capacidad del buffer
		// extraemos el contenido leido para evitar que leer la basura que pueda quedar mas alla de la cantidad leida

		final byte[] dataArray = this.bBuffer.array();
		final InputStream is = dataArray.length > readedCount ?
				new ByteArrayInputStream(Arrays.copyOf(dataArray, readedCount)) :
				new ByteArrayInputStream(dataArray);

		this.bufferReader = new BufferedReader(new InputStreamReader(is, this.charset));
	}

	/**
	 * Funci&oacute;n que recarga el registro a partir de una posici&oacute;n concreta
	 */
	@Override
	public void reload(final long position) throws IOException {
		if (this.filePosition < position) {
			this.filePosition = position;
		}
		final long currentFilePosition = this.filePosition;
		close();
		load(currentFilePosition);
		this.reloaded = true;
	}

	@Override
	public void rewind() throws IOException {
		this.fileFragmentPosition = 0;
		load(this.fileFragmentPosition);
	}

	@Override
	public void close() throws IOException {
		this.bufferReader.close();
		this.bBuffer.clear();
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
	public CharBuffer getCurrentLine() {
		return this.currentLine;
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
	public final long getFilePosition() {
		return this.filePosition;
	}

	@Override
	public long getFileFragmentedPosition() {
		return this.fileFragmentPosition;
	}

	@Override
	public boolean isReloaded() {
		return this.reloaded;
	}
}