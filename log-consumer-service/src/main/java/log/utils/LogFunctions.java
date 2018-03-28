package log.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Future;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import log.entity.LogInfo;


public class LogFunctions {

	private static final String FILE_EXT_LOGINFO=".loginfo"; //$NON-NLS-1$
	private static final String FILE_EXT_LCK=".lck"; //$NON-NLS-1$
	private static final String DIR_LOGS="C:\\LOGS\\fire"; //$NON-NLS-1$
	/**
	 * M&eacute;todo de consulta de ficheros de logs.
	 * @return Cadena de caracteres en formato JSON indicando el nombre de los ficheros log y su tama&ntilde;o
	 */
	public static String getLogFiles() {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();

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
			try  {
				final JsonWriter jw = Json.createWriter(writer);
		        jw.writeObject(jsonObj.build());
		        jw.close();
		    }
			catch (final Exception e) {
				//LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
			}
		    return writer.toString();
		}
		return null;
	}

	/**
	 *
	 * @param logFileName
	 * @return
	 */
	public static String openLog(final String logFileName) {
		final JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		final JsonArrayBuilder data = Json.createArrayBuilder();
		final String [] fileNamesLoginfo = {null,null,null,null,null} ;
		final LogInfo loginfo = new LogInfo();
		final File f = new File(DIR_LOGS);
		String nameLogInfo = null;
		if(f.exists()) {
			//bObtenrmos un listado filtrado con solo los ficheros con extensión .loginfo
			final File[] files = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(final File dir, final String name) {
					if(name.lastIndexOf(".") > 0) { //$NON-NLS-1$
						final int dot = name.lastIndexOf('.');
		                final String ext = name.substring(dot);
		                if(ext.equalsIgnoreCase(FILE_EXT_LOGINFO)) {
		                	return true;
		                }
		                return false;
					}
					return false;
				}
			});

			File configFile = null;
			if(files.length > 0) {
				//Obtenemos el nombre del fichero que mas se aproxime al nombre fire_service_*.loginfo
				for (int i = 0; i < files.length; i++){

					if (logFileName.startsWith(files[i].getName().replace(FILE_EXT_LOGINFO, ""))) {
						if (configFile != null && files[i].getName().length() > configFile.getName().length()) {
							configFile = files[i];
						}
					}

					if(files[i].getName().matches("^[A-Z][a-z]fire_services_*.loginfo")) {//coincide 100% //$NON-NLS-1$
						fileNamesLoginfo[0] = files[i].getName();
					}
					else if(files[i].getName().matches("^[A-Z][a-z]fireservices*.loginfo") ) {//coincide 75% //$NON-NLS-1$
						fileNamesLoginfo[1] = files[i].getName();
					}
					else if(files[i].getName().matches("^[A-Z][a-z]fire*.loginfo") || files[i].getName().matches("^[A-Z][a-z]services*.loginfo") ) {//coincide 50% //$NON-NLS-1$ //$NON-NLS-2$
						fileNamesLoginfo[2] = files[i].getName();
					}
					else if(files[i].getName().matches("^[A-Z][a-z]*.loginfo")) {//coincide 25% //$NON-NLS-1$
						fileNamesLoginfo[3] = files[i].getName();
					}
					else {
						fileNamesLoginfo[4] = files[i].getName();
					}
				}

				if( fileNamesLoginfo.length > 0) {
					for(int i = 0; i < fileNamesLoginfo.length; i++) {
						if(fileNamesLoginfo[i] != null && !"".equals(fileNamesLoginfo[i])) { //$NON-NLS-1$
							nameLogInfo=fileNamesLoginfo[i];
							break;
						}
					}
				}
			}
			// leer fichero loginfo y crear entidad LogInfo con los datos obtenidos del fichero
			try {
				LogInfoManager.loadConfig(nameLogInfo);
				loginfo.setCode(LogInfoManager.getCode());
				loginfo.setDateFormat(LogInfoManager.getDateFormat());
				loginfo.setLevels(LogInfoManager.getLevels());
				loginfo.setLogPattern(LogInfoManager.getLogPattern());

			} catch (final FilesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			jsonObj.add("FileList", data); //$NON-NLS-1$
			final StringWriter writer = new StringWriter();
			try  {
				final JsonWriter jw = Json.createWriter(writer);
		        jw.writeObject(jsonObj.build());
		        jw.close();
		    }
			catch (final Exception e) {
				//LOGGER.log(Level.WARNING, "Error al leer los registros en la tabla de aplicaciones", e); //$NON-NLS-1$
			}
		    return writer.toString();
		}
		return null;
	  }


	  public static void main(final String[] args) {
//		  final String salida =LogFunctions.getLogFiles();
//		  if(salida!=null) {
//			  System.out.println(salida);
//		  }
//		  else {
//			  System.out.println("No hay ficheros"); //$NON-NLS-1$
//		  }
		  final SimpleDateFormat sdf= new SimpleDateFormat("# yyyy-mm-dd; HH:mm:ss;"); //$NON-NLS-1$
		  final String fecha="# 2018-03-14; 18:19:03;"; //$NON-NLS-1$
		  try {
			final Date date = sdf.parse(fecha);
			  System.out.println(sdf.format(date));
		} catch (final ParseException e) {
			e.printStackTrace();

		}

		  final String patron="# D:[DATE]; T:[TIME]; L:[LEVEL];  M:[MESSAGE] /n CLASS:[CLASS]; METHOD:[METHOD]; "; //$NON-NLS-1$/THROW:[THROWABLE];

		  final StringBuffer sbDato = new StringBuffer();
		  final StringBuffer sbTitulo = new StringBuffer();
		  final String charSplit=";"; //$NON-NLS-1$
		  boolean  obtenerDato = false;
		  for(int i = 1; i < patron.length(); i++) {
			  if(patron.charAt(i-1) != '['
				&& patron.charAt(i-1) != ']'
				&& !obtenerDato
				&& !charSplit.equals(Character.toString(patron.charAt(i-1)))) {

				  sbTitulo.append(patron.charAt(i-1));
			  }

			  if(patron.charAt(i-1) == '[')  {
				  	sbTitulo.append(';');
					obtenerDato=true;
			  }
			  else if(patron.charAt(i) == ']')  {
				  obtenerDato=false;
				  sbDato.append(';');
			  }
			  if(obtenerDato) {
				  sbDato.append(patron.charAt(i));
			  }

		  }
		  final String [] indicePatron = sbDato.toString().split(";"); //$NON-NLS-1$
		  final String [] indiceTitulo = sbTitulo.toString().split(";"); //$NON-NLS-1$
		  for (int i = 0; i <= indicePatron.length - 1; i++) {
			  System.out.println(String.valueOf(i+1).concat(" ").concat(indiceTitulo[i]).concat(indicePatron[i])); //$NON-NLS-1$
		  }
		  System.out.println("Aplicar patron"); //$NON-NLS-1$
		  final Path path = Paths.get("C:/LOGS/fire/2018-03-14-18-18.log"); //$NON-NLS-1$

		  AsynchronousFileChannel fileChannel = null;
		  try {
			  fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
		  } catch (final IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }

		  final ByteBuffer buffer = ByteBuffer.allocate(1024);
		  final long position = 0;
		  if(fileChannel!=null) {
			  final Future<Integer> operation = fileChannel.read(buffer, position);
			  while(!operation.isDone()) {
				  ;
			  }
			  buffer.flip();
			  final byte[] data = new byte[buffer.limit()];
			  buffer.get(data);
			  System.out.println(new String(data));
			  buffer.clear();
		  }



	  }



}
