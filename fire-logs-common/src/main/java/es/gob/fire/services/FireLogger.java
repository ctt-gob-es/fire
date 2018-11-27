package es.gob.fire.services;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 *
 * @author Adolfo.Navarro
 *
 */
public class FireLogger {

	static Logger logger ;
	static FileHandler fh;

	private static final String DIARIA = "DIARIA"; //$NON-NLS-1$
	private static final String HORARIA = "HORARIA"; //$NON-NLS-1$


	private static final String PATTR_DIARIA = "yyyy-MM-dd"; //$NON-NLS-1$
	private static final String PATTR_HORARIA = "yyyy-MM-dd-HH"; //$NON-NLS-1$
	private static final String PATTR_POR_MINUTOS = "yyyy-MM-dd-HH-mm"; //$NON-NLS-1$

	private static final long SEG_DIA = 24L * 60L *60L;
	private static final long SEG_HORA = 60L * 60L;
	private static final long SEG_MIN = 60L;
	private static TimeUnit timeUnit = TimeUnit.SECONDS;

	/**
	 * funci&oacute;n principal que inicializa el logger para toda la aplicaci&oacute;n
	 * Uso: fireLogger.installLogger();
	 * Necesario indicar par&aacute;metros en fichero config_logger.properties
	 */
	public static void installLogger() {

		final Logger LOGGER = Logger.getLogger(FireLogger.class.getName());

		//leemos fichero de configuracion
		try {
	    	ConfigManager.checkInitialized();
		}
    	catch (final Exception e) {
    		LOGGER.severe("Error al cargar la configuracion del log: " + e); //$NON-NLS-1$
    		return;
    	}

		if (ConfigManager.getLogsDir() != null && !"".equals(ConfigManager.getLogsDir()) //$NON-NLS-1$
			&& ConfigManager.getRollingDate() != null && !"".equals(ConfigManager.getRollingDate())) { //$NON-NLS-1$

		    if(logger == null){
		        initLogger(); //inicializamos el logger si es la primera vez y esta a nulo.

		        //Se crea un hilo que se ejecuta periodicamente segun el parametro indicado en el fichero de configuracion (DIARIA, HORARIA, POR_MINUTOS)
		        final ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);

		        final Runnable renameLoggerFile = new Runnable(){

		            @Override
		            public void run() {
		            	fh.flush();
			            fh.close();
			            logger.removeHandler(fh);
			            try {
							 	setFileHandlerFormater();
			                    logger.addHandler(fh);
			                    logger.info("######## INICIO LOG ########"); //$NON-NLS-1$
			                } catch (final SecurityException e) {
			                    e.printStackTrace();
			                } catch (final IOException e) {

								e.printStackTrace();
			                }
			            };
		        };
		        sch.scheduleAtFixedRate(renameLoggerFile, getSecondsInitialDelay(), getSecondsRelaunchPeriod(), timeUnit);
		    }
		}
	}

	/**
	 * Crea el FileHandler (fichero de logs) en la carpeta indicada  en el par&aacute;metro logs.dir
	 * con el nombre de fichero seg&uacute;n el par&aacute;metro  logs.rollingDate
	 * @param dateForName
	 * @return
	 * @throws SecurityException
	 * @throws IOException
	 */
	static FileHandler createFilehandler(final String dateForName) throws SecurityException, IOException{
	    final String folder = ConfigManager.getLogsDir();
	    final File fileFolder = new File(folder);

	    if(!fileFolder.exists()){
	        fileFolder.mkdirs();
	    }

	    final String dateFileName = folder + File.separator +"fire_service_"+ dateForName + ".log"; //$NON-NLS-1$

	    final boolean appendToFile = true;

	    return new FileHandler(dateFileName, appendToFile);
	}

	/**
	 * Se inicializa el logger com&uacute;n a todos los packages
	 */
	static void initLogger(){

	    logger = Logger.getLogger(""); //$NON-NLS-1$

	    try {
	    		setFileHandlerFormater();
	 	        logger.addHandler(fh);
	 	        logger.info("Logs de Fire Inicializado..."); //$NON-NLS-1$

	 	    } catch (final SecurityException e) {
	 	        logger.warning("Problema al inicializar del logger... " + e.getMessage()); //$NON-NLS-1$
	 	    } catch (final IOException e) {
	 	        logger.warning("Problema al inicializar del  logger... " + e.getMessage()); //$NON-NLS-1$
	 	    }

	}

	/**
	 * Obtiene el nombre del fichero de log seg&uacute;n par&aacute;metro del fichero de configuraci&oacute;n
	 * DIARIA=yyyy-MM-dd
	 * HORARIA=yyyy-MM-dd-HH
	 * POR_MINUTOS=yyyy-MM-dd-HH-mm
	 * @return
	 */
	static String getLogFileName() {
		if(ConfigManager.getRollingDate().equals(DIARIA)) {
			final SimpleDateFormat format = new SimpleDateFormat(PATTR_DIARIA);
			return format.format(new java.sql.Date(System.currentTimeMillis()));

		}
		else if(ConfigManager.getRollingDate().equals(HORARIA)) {
			final SimpleDateFormat format = new SimpleDateFormat(PATTR_HORARIA);
			return format.format(new java.sql.Date(System.currentTimeMillis()));

		}
		else {
			final SimpleDateFormat format = new SimpleDateFormat(PATTR_POR_MINUTOS);
			return format.format(new java.sql.Date(System.currentTimeMillis()));
		}
	}

	/**
	 * Obtiene los segundos que faltan para iniciar un nuevo fichero de log, respecto al par&aacute;metro indicado en el fichero de configuraci&oacute;n
	 * @return
	 */
	 static long getSecondsInitialDelay() {

		final Calendar c = Calendar.getInstance();
	    final long now = c.getTimeInMillis();

		if(ConfigManager.getRollingDate().equals(DIARIA)) {
			c.set(Calendar.HOUR_OF_DAY, 0);
		    c.set(Calendar.MINUTE, 0);
		    c.set(Calendar.SECOND, 0);
		    c.set(Calendar.MILLISECOND, 0);
		    final long passed = now - c.getTimeInMillis();
		    final long secondsPassedToday = passed / 1000;
			return SEG_DIA - secondsPassedToday;
		}
		else if(ConfigManager.getRollingDate().equals(HORARIA)) {
		    c.set(Calendar.MINUTE, 0);
		    c.set(Calendar.SECOND, 0);
		    c.set(Calendar.MILLISECOND, 0);
		    final long passed = now - c.getTimeInMillis();
		    final long secondsPassedToday = passed / 1000;
			return SEG_HORA - secondsPassedToday;

		}
		else {
		    c.set(Calendar.SECOND, 0);
		    c.set(Calendar.MILLISECOND, 0);
		    final long passed = now - c.getTimeInMillis();
		    final long secondsPassedToday = passed / 1000;
			return SEG_MIN - secondsPassedToday;
		}
	}

	 /**
	  * Obtiene los segundos del periodo de tiempo, en donde se registrar&aacute;n en el fichero de log, respecto al par&aacute;metro indicado en el fichero de configuraci&oacute;n
	  * @return
	  */
	 static long getSecondsRelaunchPeriod() {
		if(ConfigManager.getRollingDate().equals(DIARIA)) {
			return SEG_DIA;
		}
		else if(ConfigManager.getRollingDate().equals(HORARIA)) {
			return SEG_HORA;
		}
		else {
			return SEG_MIN;
		}
	}

	 /**
	  * Se inicializa el FileHandler con el formato de las lineas a %1$tF %1$tT;%3$s%n
	  * @throws SecurityException
	  * @throws IOException
	  */
	 static void setFileHandlerFormater() throws SecurityException, IOException {
		 fh = createFilehandler(getLogFileName());

         /*	# %1  date: un objeto Date que representa la hora del evento del registro.
				# %2  fuente - una cadena que representa al que llama, si está disponible; de lo contrario, el nombre del logger.
				# %3  registrador - el nombre del logger.
				# %4  nivel - el nivel de registro.
				# %5  mensaje: el mensaje de registro formateado devuelto por el método Formatter.formatMessage (LogRecord). Utiliza el formato java.text y no usa el argumento de formato java.util.Formatter.
				# %7  throw - una cadena que representa el throwable asociado con el registro de registro y su backtrace que comienza con un carácter de nueva línea, si lo hay; de lo contrario, una cadena vacía.
				# java.util.logging.SimpleFormatter.format=%4$s: %5$s [%1$tc]%n
				java.util.logging.SimpleFormatter.format="%4$s %1$tF %1$tT %2$s %5$s %n"
				//Ejemplo extendido --> "# D:%1$tF; T:%1$tT; L:%2$s; M:%3$s%n CLASS:%4$s; METHOD:%5$s %n"
				 *  return String.format(format,
	                      new Date(lr.getMillis()),
	                      lr.getLevel().getName(),
	                      lr.getMessage(),
	                      lr.getSourceClassName(),
	                      lr.getSourceMethodName()
	              );
			*/
         fh.setFormatter(new SimpleFormatter() {

	          private static final String format = "%1$tF %1$tT;%3$s%n"; //$NON-NLS-1$

	          @Override
	          public synchronized String format(final LogRecord lr) {
	              return String.format(format,
	                      new Date(lr.getMillis()),
	                      lr.getMessage()
	              );
	          }
	      });
	 }
}
