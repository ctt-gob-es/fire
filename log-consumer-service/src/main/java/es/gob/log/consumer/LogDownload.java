package es.gob.log.consumer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Clase para la compresi&oacute;n en fragmentos de un fichero a partir de
 * un canal de datos.
 */
public class LogDownload {

	private static final Logger LOGGER = Logger.getLogger(LogDownload.class.getName());

	private static final Charset ENTRY_NAMES_CHARSET = StandardCharsets.UTF_8;

	private final String filename;
	private final SeekableByteChannel channel;

	private ByteArrayOutputStream bos;
	private ZipOutputStream zipOutputStream;
	private boolean closed;


	/**
	 * Construye el objeto para la descarga de un fichero de log comprimido. Se trata
	 * de situar la posici&oacute;n del canal a su inicio.
	 * @param filename Nombre del fichero de log.
	 * @param channel Canal con los datos a comprimir.
	 */
	public LogDownload (final String filename, final SeekableByteChannel channel)  {
		this.filename = filename;
		this.channel = channel;

		this.zipOutputStream = null;
		this.closed = false;

		try {
			this.channel.position(0);
		}
		catch (final Exception e) {
			LOGGER.warning("No se ha podido establecer la posicion del canal a su inicio: " + e); //$NON-NLS-1$
		}
	}

	/**
	 * Descarga un fragmento comprimido del fichero de log. Para componer el
	 * fichero completo, es necesario concatenar todos los resultados devueltos
	 * por cada una de las llamadas a este m&eacute;todo. Para saber si hay
	 * m&aacute;s contenido pendiente de descargar, se puede utilizar el
	 * m&eacute;todo {@link #hasMore}.<br>
	 * En la primera llamada a este m&eacute;todo, se establece la posicion a su
	 * inicio para que se descargue todo el log.
	 * @return Fragmento comprimido del log o {@code null} si no hab&iacute}
	 * @throws IOException Cuando ocurre un error en la lectura de datos del canal
	 * o en la compresi&oacute;n de los datos de salida.
	 */
	public byte[] download() throws IOException {

		if (this.closed) {
			return null;
		}

		if (this.zipOutputStream == null) {
			prepareOutput();
			// Comprobamos que la posicion este al inicio de la
			if (this.channel.position() != 0) {
				this.channel.position(0);
			}
		}

		// Comprimimos un fragmento de los datos
		final ByteBuffer buf =  ByteBuffer.allocate(LogConstants.PART_SIZE);
		this.channel.read(buf);
		buf.flip();
		final byte[] data = new byte[buf.limit()];
		buf.get(data);
        try {
        	this.zipOutputStream.write(data);
        } catch(final IOException e) {
        	throw new IOException("Error al comprimir un fragmento de los datos", e); //$NON-NLS-1$
        }
		buf.clear();

		// Si no quedan mas datos, cerramos el zip y devolvemos el resultado
		if (!hasMore()) {
			closeZip();
		}

		// Devolvemos el fragmento comprimido y reestablecemos el buffer para
		// que los siguientes fragmentos no se acumulen con este
		final byte[] fragment = this.bos.toByteArray();
		this.bos.reset();

		return fragment;
	}

	/**
	 * Inicializa los objetos para la compresi&oacute;n del log.
	 * @throws IOException Cuando ocurre un error al inicializar
	 * el fichero comprimido.
	 */
	private void prepareOutput() throws IOException {
		this.bos = new ByteArrayOutputStream();
		this.zipOutputStream = new ZipOutputStream(this.bos, ENTRY_NAMES_CHARSET);
		this.zipOutputStream.putNextEntry(new ZipEntry(this.filename));
	}

	/**
	 * Cierra el archivo ZIP y marca que el proceso de descarga ha finalizado.
	 * @throws IOException
	 */
	private void closeZip() throws IOException {
		this.zipOutputStream.closeEntry();
		this.zipOutputStream.close();

		this.closed = true;
	}

	/**
	 * Indica si hay m&aacute;s datos pendientes de descargar.
	 * @return {@code true} si hay m&aacute;s datos pendientes de descargar,
	 * {@code false} en caso contrario.
	 * @throws IOException Si no es posible determinar el estado del canal.
	 */
	public boolean hasMore() throws IOException {
		return !this.closed && this.channel.position() != this.channel.size();
	}

	public static void main(final String[] args) throws Exception {

		final File dataFile = new File("C:/Users/carlos.gamuci/Desktop/Datos/test.pdf");
		final SeekableByteChannel channel = FileChannel.open(dataFile.toPath(), StandardOpenOption.READ);

		final LogDownload donwloader = new LogDownload(dataFile.getName(), channel);

		try (FileOutputStream fos = new FileOutputStream("C:/Users/carlos.gamuci/Desktop/salida.zip")) {
			while (donwloader.hasMore()) {
				System.out.println("Descargamos fragmento");
				final byte[] fragment = donwloader.download();
				System.out.println("Bytes: " + fragment.length);
				fos.write(fragment);
			}
		}

		channel.close();
	}

}
