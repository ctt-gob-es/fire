package es.gob.log.consumer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

public class LogFiles {

	private static final String FILE_EXT_LOGINFO = LogConstants.FILE_EXT_LOGINFO;
	private static final String FILE_EXT_LCK = LogConstants.FILE_EXT_LCK;
	private static final String DIR_LOGS = LogConstants.DIR_FILE_LOG;


	public LogFiles() {

	}
	/**
	 * M&eacute;todo de consulta de ficheros de logs.
	 * @return Array de bytes que contiene cadena de caracteres en formato JSON indicando el nombre de los ficheros log y su tama&ntilde;o
	 * @throws UnsupportedEncodingException
	 */
	public  byte[] getLogFiles() throws UnsupportedEncodingException {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		byte[] result = null;
		final File f = new File(DIR_LOGS);
		if(f.exists()) {
			final File[] files = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					if(name.lastIndexOf(".") > 0) { //$NON-NLS-1$
						final int dot = name.lastIndexOf('.');
		                final String ext = name.substring(dot);
		                if(!ext.equalsIgnoreCase(FILE_EXT_LOGINFO) && !ext.equalsIgnoreCase(FILE_EXT_LCK)) {
		                	return true;
		                }
		                return false;
					}
					return false;
				}
			});
			for (int i = 0; i < files.length; i++){
				data.add(Json.createObjectBuilder()
						.add("nombre",files[i].getName()) //$NON-NLS-1$
						.add("tamanno", String.valueOf(files[i].length()/1024L).concat("Kbytes")) //$NON-NLS-1$ //$NON-NLS-2$
				);
			}
			jsonObj.add("FileList", data); //$NON-NLS-1$
			final StringWriter writer = new StringWriter();

			try(final JsonWriter jw = Json.createWriter(writer)) {
		        jw.writeObject(jsonObj.build());
		        jw.close();
		    }
			catch (final Exception e) {
				//LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
			}
			result = writer.toString().getBytes("UTF-8"); //$NON-NLS-1$
		}
		return result;
	}
}
