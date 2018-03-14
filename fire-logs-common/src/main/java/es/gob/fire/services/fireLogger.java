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


public class fireLogger {

	static Logger logger;
	static FileHandler fh;

	private static final String DIARIA="DIARIA"; //$NON-NLS-1$
	private static final String HORARIA="HORARIA"; //$NON-NLS-1$
//	private static final String POR_MINUTOS="POR_MINUTOS";

	private static final String PATTR_DIARIA="yyyy-MM-dd"; //$NON-NLS-1$
	private static final String PATTR_HORARIA="yyyy-MM-dd-HH"; //$NON-NLS-1$
	private static final String PATTR_POR_MINUTOS="yyyy-MM-dd-HH-mm"; //$NON-NLS-1$

	private static final long SEG_DIA=24L*60L*60L;
	private static final long SEG_HORA=60L*60L;
	private static final long SEG_MIN=60L;
	private static TimeUnit timeUnit = TimeUnit.SECONDS;

	public static void installLogger() { //final String [] class_names

		final Logger LOGGER = Logger.getLogger(fireLogger.class.getName());

		try {
	    	ConfigManager.checkInitialized();
		}
    	catch (final Exception e) {
    		LOGGER.severe("Error al cargar la configuracion del log: " + e); //$NON-NLS-1$
    		return;
    	}

		if(ConfigManager.getLogsDir()!=null && !"".equals(ConfigManager.getLogsDir()) //$NON-NLS-1$
			&& ConfigManager.getRollingDate()!=null && !"".equals(ConfigManager.getRollingDate())) { //$NON-NLS-1$

		    if(logger == null){
		        initLogger(ConfigManager.getPackages().split(",")); //$NON-NLS-1$

		        final ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
		        // Create a task for one-shot execution using schedule()
		        final Runnable renameLoggerFile = new Runnable(){
	//	              int countRuns = 0;  // For testing only
		            @Override
		            public void run() {
		                fh.flush();
		                fh.close();
		                try {
		                    fh = createFilehandler(getLogFileName());
	//	                    fh = createFilehandler(new java.sql.Date(System.currentTimeMillis()).toString()+"_"+countRuns++); // for testing

		                    /*	# %1  date: un objeto Date que representa la hora del evento del registro.
								# %2  fuente - una cadena que representa al que llama, si está disponible; de lo contrario, el nombre del logger.
								# %3  registrador - el nombre del logger.
								# %4  nivel - el nivel de registro.
								# %5  mensaje: el mensaje de registro formateado devuelto por el método Formatter.formatMessage (LogRecord). Utiliza el formato java.text y no usa el argumento de formato java.util.Formatter.
								# %7  throw - una cadena que representa el throwable asociado con el registro de registro y su backtrace que comienza con un carácter de nueva línea, si lo hay; de lo contrario, una cadena vacía.
								# java.util.logging.SimpleFormatter.format=%4$s: %5$s [%1$tc]%n
								java.util.logging.SimpleFormatter.format="%4$s %1$tF %1$tT %2$s %5$s %n"
							*/
		                    fh.setFormatter(new SimpleFormatter() {
		  	 		          private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %4$s %n"; //$NON-NLS-1$

		  	 		          @Override
		  	 		          public synchronized String format(final LogRecord lr) {
		  	 		              return String.format(format,
		  	 		                      new Date(lr.getMillis()),
		  	 		                      lr.getLevel().getLocalizedName(),
		  	 		                      lr.getSourceMethodName(),
		  	 		                      lr.getMessage()
		  	 		              );
		  	 		          }
		  	 		      });
		                    logger.addHandler(fh);
		                    logger.warning("Runnable executed, new FileHandler is in use!"); //$NON-NLS-1$
		                } catch (final SecurityException e) {
		                    // TODO Auto-generated catch block
		                    e.printStackTrace();
		                } catch (final IOException e) {
		                    // TODO Auto-generated catch block
		                    e.printStackTrace();
		                }
		            }
		        };

		 //     sch.scheduleAtFixedRate(renameLoggerFile, 5, 10, timeUnit); // for testing
		        sch.scheduleAtFixedRate(renameLoggerFile, getSecondsInitialDelay(), getSecondsRelaunchPeriod(), timeUnit);

		    }
		}
//	    return logger;
	}
	static FileHandler createFilehandler(final String dateForName) throws SecurityException, IOException{
	    final String folder = ConfigManager.getLogsDir();
	    final File fileFolder = new File(folder);
	    // Create folder log if it doesn't exist
	    if(!fileFolder.exists()){
	        fileFolder.mkdirs();
	    }

	    final String dateFileName = folder + File.separator + dateForName + ".log"; //$NON-NLS-1$

	    final boolean appendToFile = true;

	    return new FileHandler(dateFileName, appendToFile);
	}

	private static void initLogger(final String [] class_names){


	    final String folder = ConfigManager.getLogsDir();
	    final File fileFolder = new File(folder);
	    // Create folder "log" if it doesn't exist
	    if(!fileFolder.exists()){
	        fileFolder.mkdirs();
	    }

	    for(final String class_name:class_names) {
	    	 logger = Logger.getLogger(class_name);
	    	 logger.setUseParentHandlers(false);

	    	 try {
	 	        // This block configure the logger with handler and formatter
	    		 final boolean appendToFile = true;
	 	         fh = new FileHandler(folder + File.separator + getLogFileName()+ ".log", appendToFile); //$NON-NLS-1$
		 	     fh.setFormatter(new SimpleFormatter() {
		 		          private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %4$s %n"; //$NON-NLS-1$

		 		          @Override
		 		          public synchronized String format(final LogRecord lr) {
		 		              return String.format(format,
		 		                      new Date(lr.getMillis()),
		 		                      lr.getLevel().getLocalizedName(),
		 		                      lr.getSourceMethodName(),
		 		                      lr.getMessage()
		 		              );
		 		          }
		 		      });

	 	        logger.addHandler(fh);


	 	        // the following statement is used to log any messages
	 	        logger.info("Logs de Fire Inicializado..."); //$NON-NLS-1$

	 	    } catch (final SecurityException e) {
	 	        logger.warning("Problema al inicializar el logger... " + e.getMessage()); //$NON-NLS-1$
	 	    } catch (final IOException e) {
	 	        logger.warning("Problema al inicializar el  logger... " + e.getMessage()); //$NON-NLS-1$
	 	    }

	    }

	}

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

	private static long getSecondsInitialDelay() {

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

	private static long getSecondsRelaunchPeriod() {
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

}
