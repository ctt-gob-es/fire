package es.gob.log.consumer.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.channels.AsynchronousFileChannel;
import java.util.Iterator;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogConstants;
import es.gob.log.consumer.LogDirInfo;
import es.gob.log.consumer.LogFilenameFilter;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogOpen;
import es.gob.log.consumer.LogReader;
import es.gob.log.consumer.NegativeLogFilenameFilter;

public class LogOpenServiceManager implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 3381831208944096820L;

	/**
	 * Lanza el proceso de obtener los datos loginfo asociado al fichero log indicado por
	 * par&aacute;metro "fname" de HttpServletRequest (req)
	 * @param req Petici&oacute;n HTTP.
	 * @param dirInfo Configuraci&oacute;n referente al directorio que se quiere listar.
	 * @return JSON con la informaci&oacute;n necesaria para el tratamiento del fichero.
	 * @throws IOException Cuando ocurre un error en la apertura.
	 * @throws IllegalArgumentException Cuando no se proporcionan los par&aacute;metros necesarios.
	 */
	public final static byte[] process(final HttpServletRequest req, final LogDirInfo dirInfo) throws IOException, IllegalArgumentException {

		final HttpSession session = req.getSession(false);

		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		if (logFileName == null || logFileName.isEmpty()) {
			throw new IllegalArgumentException("No se ha proporcionado el parametro con el nombre de fichero: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
		}

		// Bloqueamos la apertura de cualquier fichero que no se deba listar
		final String[] prohibitedPatterns = dirInfo != null ? dirInfo.getHiddenPatterns() : null;
		if (!new NegativeLogFilenameFilter(prohibitedPatterns).accept(null, logFileName)) {
			throw new IllegalArgumentException("Se ha solicitado cargar un fichero no visible"); //$NON-NLS-1$
		}

		// Comprobacion de seguridad de que no se pida un fichero fuera del directorio configurado
		final File logsDir = ConfigManager.getInstance().getLogsDir();
		final File logFile = new File(logsDir, logFileName);
		try {
			if (!logFile.getCanonicalPath().startsWith(logsDir.getCanonicalPath())) {
				throw new IllegalArgumentException("Intento cargarse un fichero de log de fuera del directorio configurado: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
			}
		}
		catch (final Exception e) {
			throw new IOException("No pudo completarse la validacion de seguridad sobre la ruta del fichero: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
		}

		if (!logFile.isFile() || !logFile.canRead()) {
			throw new IOException("El fichero de log indicado no se encontro o no pudo leerse: " + ServiceParams.LOG_FILE_NAME); //$NON-NLS-1$
		}

		LogInfo logInfo;
		try {
			final File logInfoFile = findLogInfoFile(logFile, dirInfo);

			final LogOpen logOpen = new LogOpen(logFile, logInfoFile);
			logOpen.openFile();

			logInfo = logOpen.getLogInfo();
			session.setAttribute(SessionParams.LOG_INFO, logInfo);

			final AsynchronousFileChannel logChannel = logOpen.getChannel();
			session.setAttribute(SessionParams.FILE_CHANNEL, logChannel);
			session.setAttribute(SessionParams.FILE_SIZE, new Long (logChannel.size()));

			final LogReader logReader = logOpen.getReader();
			session.setAttribute(SessionParams.FILE_READER, logReader);
			session.setAttribute(SessionParams.FILE_POSITION, new Long(0L));

		} catch (final IOException e) {
			throw new IOException("Error al abrir el fichero de log " + logFile.getAbsolutePath(), e); //$NON-NLS-1$
		}

		return buildResult(logInfo);
	}

	/**
	 * Recupera el fichero loginfo correspondiente al fichero de log indicado.
	 * @param logFile Fichero de log.
	 * @param dirInfo Configuraci&oacute;n espec&iacute;fica para el directorio.
	 * @return Fichero loginfo que corresponde al log o {@code null} si no est&aacute; definido.
	 */
	private static File findLogInfoFile(final File logFile, final LogDirInfo dirInfo) {

		final File logsDir = logFile.getParentFile();
		final String logFilename = logFile.getName();

		// Comprobamos si en la configuracion del directorio se asigno un fichero logInfo
		// para este fichero de log
		File logInfoFile = null;
		if (dirInfo != null && dirInfo.getFileInfoAssociation() != null) {
			logInfoFile = findLogInfoByDirInfo(logsDir, logFilename, dirInfo);
		}

		// Si no se encuentra una asignacion directa de LogInfo se busca en base al nombre del
		// fichero
		if (logInfoFile == null) {
			logInfoFile = findLogInfoByName(logsDir, logFilename);
		}

		return logInfoFile;
	}

	/**
	 * Recupera el fichero loginfo asociado en la informaci&oacute;n del directorio al fichero
	 * de log indicado.
	 * @param logsDir Directorio de ficheros de log.
	 * @param logFilename Nombre del fichero de log para el que se busca el loginfo.
	 * @param dirInfo Informaci&oacute;n del directorio de logs.
	 * @return Fichero loginfo del correspondiente al log o {@code null} si no hay ninguno.
	 */
	private static File findLogInfoByDirInfo(final File logsDir, final String logFilename, final LogDirInfo dirInfo) {

		final Map<String, String[]> associations = dirInfo.getFileInfoAssociation();

		File logInfoFile = null;
		final Iterator<String> logInfoIt = associations.keySet().iterator();
		while (logInfoFile == null && logInfoIt.hasNext()) {
			final String logInfoFilename = logInfoIt.next();
			final String[] patterns = associations.get(logInfoFilename);
			if (new LogFilenameFilter(patterns).accept(logsDir, logFilename)) {
				logInfoFile = new File(logsDir, logInfoFilename);
			}
		}
		return logInfoFile;
	}

	/**
	 * Busca en el directorio de logs un fichero loginfo que se ajuste en nombre al fichero
	 * de log seleccionado.
	 * @param logsDir Directorio de ficheros de log.
	 * @param logFilename Nombre del fichero de log.
	 * @return Fichero loginfo del correspondiente al log o {@code null} si no hay ninguno.
	 */
	private static File findLogInfoByName(final File logsDir, final String logFilename) {

		String filename = logFilename;

		// Tomamos el nombre de fichero sin extension para buscar el loginfo que le corresponde
		final int extDotPos = filename.lastIndexOf('.');
		if (extDotPos != -1) {
			filename = filename.substring(0, extDotPos);
		}

		// Obtenemos un listado con los ficheros con extension .loginfo del directorio
		final File[] logInfoFiles = logsDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				if (name.lastIndexOf('.') > -1) {
					final int dot = name.lastIndexOf('.');
					final String ext = name.substring(dot);
					return ext.equalsIgnoreCase(LogConstants.FILE_EXT_LOGINFO);
				}
				return false;
			}
		});

		// Si se encuentran ficheros loginfo, se busca aquel cuyo nombre mas se aproxime
		// al nombre del fichero de log
		int maxLength = -1;
		File selectedLogInfoFile = null;
		for (final File logInfoFile : logInfoFiles) {

			final String logInfoFileName = logInfoFile.getName().substring(0, logInfoFile.getName().lastIndexOf('.'));

			// Si el nombre del loginfo es mayor que el del log, no se corresponden
			if (logInfoFileName.length() > filename.length()) {
				continue;
			}

			// Comprobamos si todo del loginfo se corresponde con el principio del nombre
			// del log.
			if (filename.startsWith(logInfoFileName) || logInfoFileName.length() == 0) {
				// Si la coincidencia en nombre es superior a cualquier otro loginfo anterior,
				// lo sustituimos
				if (logInfoFileName.length() > maxLength) {
					maxLength = logInfoFileName.length();
					selectedLogInfoFile = logInfoFile;
				}
			}
		}

		return selectedLogInfoFile;
	}

	/**
	 * Construye el resultado de la operaci&oacute;n, consistente en un JSON con la
	 * configuraci&oacute;n asignada al fichero de log.
	 * @param logInfo Configuraci&oacute;n asignada al fichero de log.
	 * @return JSON con la configuraci&oacute;n del fichero.
	 */
	private static byte[] buildResult(final LogInfo logInfo) {

		final String charset = logInfo.getCharset().name();

		String levels = ""; //$NON-NLS-1$
		if (logInfo.getLevels() != null) {
			for (int i = 0; i < logInfo.getLevels().length; i++) {
				levels += logInfo.getLevels()[i];
				if (i < logInfo.getLevels().length - 1) {
					levels += ","; //$NON-NLS-1$
				}
			}
		}

		String dateTimeFormat = ""; //$NON-NLS-1$
		if (logInfo.getDateFormat() != null) {
			dateTimeFormat = logInfo.getDateFormat();
		}

		final String date = Boolean.toString(logInfo.hasDateComponent());
		final String time = Boolean.toString(logInfo.hasTimeComponent());

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

		return writer.toString().getBytes(logInfo.getCharset());
	}
}
