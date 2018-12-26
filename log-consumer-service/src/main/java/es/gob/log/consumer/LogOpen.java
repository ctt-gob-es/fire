package es.gob.log.consumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;


public class LogOpen {

	private static final Logger LOGGER = Logger.getLogger(LogOpen.class.getName());

	private final  File logFile;
	private  LogInfo logInfo;
	private  LogReader reader;
	private AsynchronousFileChannel channel;

	public LogOpen (final File logFile) {
		this.logFile = logFile;
	}

	/**
	 * Busca el fichero con extensi&oacute;n .loginfo cuyo nombre m&aacute;s se le parezca al fichero seleccionado y si lo encuentra lo carga con las
	 *	propiedades leidas de dicho fichero loginfo, en caso contrario al crear el objeto loginfo lo inicializa
	 *	con las propiedades por defecto.
	 * @return Array de bytes con la informaci&oacute;n obtenida del fichero .loginfo asociado al
	 * fichero de log.
	 * @throws IOException
	 */
	public final byte[] openFile() throws IOException  {

		final File logsDir = this.logFile.getParentFile();
		String logFileName = this.logFile.getName();

		// Tomamos el nombre de fichero sin extension para buscar el loginfo que le corresponde
		final int extDotPos = logFileName.lastIndexOf('.');
		if (extDotPos != -1) {
			logFileName = logFileName.substring(0, extDotPos);
		}

		// Obtenemos un listado con los ficheros con extension .loginfo del directorio
		final File[] logInfoFiles = logsDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				if (name.lastIndexOf('.') > 0) {
					final int dot = name.lastIndexOf('.');
					final String ext = name.substring(dot);
					return ext.equalsIgnoreCase(LogConstants.FILE_EXT_LOGINFO);
				}
				return false;
			}
		});

		// Si se encuentran ficheros loginfo, se busca aquel cuyo nombre mas se aproxime
		// al nombre del fichero de log

		int maxLength = 0;
		File selectedLogInfoFile = null;
		for (final File logInfoFile : logInfoFiles) {

			final String logInfoFileName = logInfoFile.getName().substring(0, logInfoFile.getName().lastIndexOf('.'));

			// Si el nombre del loginfo es mayor que el del log, no se corresponden
			if (logInfoFileName.length() > logFileName.length()) {
				continue;
			}

			// Comprobamos si todo del loginfo se corresponde con el principio del nombre
			// del log.
			if (logFileName.startsWith(logInfoFileName)) {
				// Si la coincidencia en nombre es superior a cualquier otro loginfo anterior,
				// lo sustituimos
				if (logInfoFileName.length() > maxLength) {
					maxLength = logInfoFileName.length();
					selectedLogInfoFile = logInfoFile;
				}
			}
		}

		// Leemos el loginfo y obtenemos la informacion del fichero de log
		this.logInfo = new LogInfo();

		// Si se identifico el loginfo del fichero, se carga su informacion. Si no, se devuelve
		// la configuracion por defecto.
		if (selectedLogInfoFile != null) {
			try (FileInputStream fis = new FileInputStream(selectedLogInfoFile)) {
				this.logInfo.load(fis);
			}
			catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "Error al leer el fichero .loginfo correspondiente al fichero log. Se utilizara la configuracion por defecto", e); //$NON-NLS-1$
			}
		}
		else {
			LOGGER.warning("No se ha encontrado el .loginfo del fichero " + this.logFile.getName()); //$NON-NLS-1$
		}

		// Abrimos el fichero de log y su lector
		try {
			this.channel = AsynchronousFileChannel.open(this.logFile.toPath(), StandardOpenOption.READ);
			this.reader = new FragmentedFileReader(this.channel, this.logInfo.getCharset());
			this.reader.load(0L);
		}
		catch (final Exception e) {
			LOGGER.log(Level.SEVERE, "Error al abrir rl fichero de log", e); //$NON-NLS-1$
			throw new IOException("Error al abrir el fichero de log", e); //$NON-NLS-1$
		}

		// Generamos el resultado en formato JSON de la salida
		final byte[] logInfoJson = writeAsJson(this.logInfo);

		return logInfoJson;
	}

	/**
	 * Representa un objeto LogInfo a modo de JSON.
	 * @param info Objeto a representar.
	 * @return Bytes del JSON.
	 */
	private static byte[] writeAsJson(final LogInfo info) {

		final String charset = info.getCharset().name();

		String levels = ""; //$NON-NLS-1$
		if (info.getLevels() != null) {
			for (int i = 0; i < info.getLevels().length; i++) {
				levels += info.getLevels()[i];
				if (i < info.getLevels().length - 1) {
					levels += ","; //$NON-NLS-1$
				}
			}
		}

		String dateTimeFormat = ""; //$NON-NLS-1$
		if (info.getDateFormat() != null) {
			dateTimeFormat = info.getDateFormat();
		}

		final String date = Boolean.toString(info.hasDateComponent());
		final String time = Boolean.toString(info.hasTimeComponent());

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		data.add(Json.createObjectBuilder()
				.add("Charset", charset) //$NON-NLS-1$
				.add("Levels", levels) //$NON-NLS-1$
				.add("Date", date) //$NON-NLS-1$
				.add("Time", time) //$NON-NLS-1$
				.add("DateTimeFormat", dateTimeFormat)); //$NON-NLS-1$
		jsonObj.add("LogInfo", data); //$NON-NLS-1$

		final StringWriter writer = new StringWriter();
		try (JsonWriter jw = Json.createWriter(writer)) {
			jw.writeObject(jsonObj.build());
		}

		return writer.toString().getBytes(info.getCharset());
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
