package es.gob.log.consumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogOpen {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogOpen.class);

	private final  File logFile;
	private final  File logInfoFile;
	private  LogInfo logInfo;
	private  LogReader reader;
	private AsynchronousFileChannel channel;

	public LogOpen (final File logFile, final File logInfoFile) {
		this.logFile = logFile;
		this.logInfoFile = logInfoFile;
	}

	/**
	 * Abre y carga el fichero de log y su fichero "logInfo" asociado. Si no se indica un fichero
	 * "logInfo", se usar&aacute; la configuraci&oacute;n por defecto para procesar el fichero de
	 * log.
	 * @throws IOException Cuando ocurre un error en la apertura del fichero de log.
	 */
	public final void openFile() throws IOException  {

		// Si disponemos de un ".loginfo" especifico para el log, se carga su informacion. Si no, se devuelve
		// la configuracion por defecto.
		this.logInfo = new LogInfo();
		if (this.logInfoFile != null) {
			try (FileInputStream fis = new FileInputStream(this.logInfoFile)) {
				this.logInfo.load(fis);
			}
			catch (final IOException e) {
				LOGGER.error("Error al leer el fichero LogInfo " + this.logInfoFile.getName() + ". Se utilizara la configuracion por defecto", e); //$NON-NLS-1$
			}
		}
		else {
			LOGGER.warn("No se ha encontrado un fichero LogInfo para el fichero " + this.logFile.getName()); //$NON-NLS-1$
		}

		// Abrimos el fichero de log y su lector
		try {
			this.channel = AsynchronousFileChannel.open(this.logFile.toPath(), StandardOpenOption.READ);
			this.reader = new FragmentedFileReader(this.channel, this.logInfo.getCharset());
			this.reader.load(0L);
		}
		catch (final Exception e) {
			throw new IOException("Error al abrir el fichero de log", e); //$NON-NLS-1$
		}
	}

	/**
	 * Obtiene la entidad LogInfo con la informaci&oacute;n cargada
	 * @return
	 */
	public final LogInfo getLogInfo() {
		return this.logInfo;
	}

	/**
	 * Obtiene  LogReader del fichero abierto en modo lectura, inicializado en la posici&oacute;n 0
	 * @return
	 */
	public final LogReader getReader() {
		return this.reader;
	}

	/**
	 * Obtiene  AsynchronousFileChannel, canal de fichero abierto en modo lectura
	 * @return
	 */
	public final AsynchronousFileChannel getChannel() {
		return this.channel;
	}
}
