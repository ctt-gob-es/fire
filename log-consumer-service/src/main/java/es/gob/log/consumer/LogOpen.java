package es.gob.log.consumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import es.gob.log.consumer.service.ConfigManager;


public class LogOpen {

	private static final Logger LOGGER = Logger.getLogger(LogOpen.class.getName());

	private final Path path;
	private  LogInfo linfo;
	private  LogReader reader;
	private AsynchronousFileChannel channel;

	/**
	 *
	 */
	public LogOpen (final String path) {
		this.path = Paths.get(path);
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */
	public final byte[] openFile(final String logFileName) throws IOException {

		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

		final String [] fileNamesLoginfo = {null,null,null,null,null} ;
		final File f = ConfigManager.getInstance().getLogsDir().getCanonicalFile();

		String nameLogInfo = logFileName.replace(LogConstants.FILE_EXT_LOG, ""); //$NON-NLS-1$

		if(f.exists()) {
			//Obtenemos un listado filtrado con s&oacute;lo los ficheros con extensi&oacute;n .loginfo
			final File[] files = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					if(name.lastIndexOf(".") > 0) { //$NON-NLS-1$
						final int dot = name.lastIndexOf('.');
		                final String ext = name.substring(dot);
		                if(ext.equalsIgnoreCase(LogConstants.FILE_EXT_LOGINFO)) {
		                	return true;
		                }
		                return false;
					}
					return false;
				}
			});


			if(files.length > 0) {
				//Obtenemos el nombre del fichero .loginfo que mas se aproxime al nombre del fichero de log
				for (int i = 0; i < files.length; i++){

//					if (logFileName.startsWith(files[i].getName().replace(LogConstants.FILE_EXT_LOGINFO, ""))) {
//
//						if (configFile != null && files[i].getName().length() > configFile.getName().length()) {
//							configFile = files[i];
//						}
//					}

					if(files[i].getName().matches("^[A-Z][a-z]".concat(nameLogInfo).concat("*.loginfo"))) {//coincide 100% //$NON-NLS-1$ //$NON-NLS-2$
						fileNamesLoginfo[0] = files[i].getName();
					}
					else if(files[i].getName().matches("^[A-Z][a-z]"+ nameLogInfo.trim().replace("_", "") +"*.loginfo") ) {//coincide 75% //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						fileNamesLoginfo[1] = files[i].getName();
					}
					else if(files[i].getName().matches("^[A-Z][a-z]"+nameLogInfo.trim()+"*.loginfo") && files[i].getName().replace(LogConstants.FILE_EXT_LOG, "").length() == nameLogInfo.length() ) {//coincide 50% //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						fileNamesLoginfo[2] = files[i].getName();
					}
					else if(files[i].getName().matches("^[A-Z][a-z]*.loginfo")) {//coincide 0% //$NON-NLS-1$
						fileNamesLoginfo[3] = files[i].getName();
					}
					else {
						fileNamesLoginfo[4] = files[i].getName();
					}
				}

				if( fileNamesLoginfo.length > 0) {
					for(int i = 0; i < fileNamesLoginfo.length; i++) {
						if(fileNamesLoginfo[i] != null && !"".equals(fileNamesLoginfo[i])) { //$NON-NLS-1$
							nameLogInfo = fileNamesLoginfo[i];
							break;
						}
					}
				}
			}
			// leer fichero loginfo y crear entidad LogInfo con los datos obtenidos del fichero

			final String pathLogInfo = ConfigManager.getInstance().getLogsDir().getAbsolutePath().concat(File.separator).concat(nameLogInfo);
			final File fLogInfo =  new File(pathLogInfo).getCanonicalFile();
			// Abrir fichero log, se inicializa el canal y el lector.
			try(FileInputStream fis = new FileInputStream(fLogInfo)) {
				this.linfo = new LogInfo();
				this.linfo.load(fis);
				this.channel = AsynchronousFileChannel.open(this.path,StandardOpenOption.READ);
				this.reader = new FragmentedFileReader(this.channel, this.linfo.getCharset());
				this.reader.load(0L);
			}

			//Generamos el resultado en formato JSON de la salida
			final StringWriter writer = new StringWriter();
			try  {

				final String charset = this.linfo.getCharset().name();
				String levels=""; //$NON-NLS-1$
				for (int i=0; i < this.linfo.getLevels().length; i++) {
					if(i < this.linfo.getLevels().length - 1) {
						levels += this.linfo.getLevels()[i].concat(","); //$NON-NLS-1$
					}
					else {
						levels += this.linfo.getLevels()[i];
					}
				}
				final String dateTimeFormat = this.linfo.getDateFormat();
				final String date = this.linfo.hasDateComponent()?"true":"false"; //$NON-NLS-1$ //$NON-NLS-2$
				final String time = this.linfo.hasTimeComponent()?"true":"false"; //$NON-NLS-1$ //$NON-NLS-2$

				data.add(Json.createObjectBuilder()
						.add("Charset",charset) //$NON-NLS-1$
						.add("Levels", levels) //$NON-NLS-1$
						.add("Date", date) //$NON-NLS-1$
						.add("Time", time) //$NON-NLS-1$
						.add("DateTimeFormat", dateTimeFormat)); //$NON-NLS-1$
				jsonObj.add("LogInfo", data); //$NON-NLS-1$
				final JsonWriter jw = Json.createWriter(writer);
		        jw.writeObject(jsonObj.build());
		        jw.close();
		    }
			catch (final Exception e) {
				LOGGER.log(Level.WARNING, "Error ", e); //$NON-NLS-1$
			}
		    try {
				return writer.toString().getBytes(this.linfo.getCharset().name());
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Obtiene la entidad LogInfo con la informaci&oacute;n cargada
	 * @return
	 */
	public final LogInfo getLinfo() {
		return this.linfo;
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
