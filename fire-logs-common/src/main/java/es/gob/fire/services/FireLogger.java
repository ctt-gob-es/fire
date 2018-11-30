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

	private Logger logger = null;
	protected FileHandler fh = null;

	private static final String DIARIA = "DIARIA"; //$NON-NLS-1$
	private static final String HORARIA = "HORARIA"; //$NON-NLS-1$


	private static final String PATTR_DIARIA = "yyyy-MM-dd"; //$NON-NLS-1$
	private static final String PATTR_HORARIA = "yyyy-MM-dd-HH"; //$NON-NLS-1$
	private static final String PATTR_POR_MINUTOS = "yyyy-MM-dd-HH-mm"; //$NON-NLS-1$

	private static final long SEG_DIA = 24L * 60L *60L;
	private static final long SEG_HORA = 60L * 60L;
	private static final long SEG_MIN = 60L;
	private static TimeUnit timeUnit = TimeUnit.SECONDS;
	private String logName = null;
	private  String path_filelog = null;
	private static  String rollingDate = null ;




	final Logger _LOGGER = Logger.getLogger(FireLogger.class.getName());

	 /**
	  * Constructor con el nombre del Logger, los valores de configuraci&oacute;n se indican desde el fichero logger_config.properties
	  * @param loggerName
	  */
	public FireLogger(final String loggerName ) {
		super();
		this.installLogger(loggerName,null,null);
	}

	 /**
	  * Constructor con el nombre del Logger y la ruta de creaci&oacute;n del fichero log y el tiempo de rotado:
	  * DIARIA,HORARIA, POR_MINUTOS
	  * @param loggerName
	  */
	public FireLogger(final String loggerName, final String path, final String rollingDate ) {
		super();
		this.installLogger(loggerName,path,rollingDate);
	}

	/**
	 * funci&oacute;n principal que inicializa el logger para toda la aplicaci&oacute;n
	 * Uso: fireLogger.installLogger(statisticsName);
	 * Necesario indicar par&aacute;metros en fichero config_logger.properties
	 */
	public void installLogger(final String logParticleName, final String path, final String rollingDate) {

		if(logParticleName != null ) {
			setLogName(logParticleName);
    	}
		//leemos fichero de configuracion
		try {
	    	ConfigManager.checkInitialized();

		}
    	catch (final Exception e) {
    		this._LOGGER.severe("Error al cargar la configuracion del log: " + e); //$NON-NLS-1$
    		return;
    	}

		// obtenemos el path y el rollingDate
		if(path != null && !"".equals(path)) { //$NON-NLS-1$
			this.setPath_filelog(path);
		}else if(ConfigManager.getLogsDir() != null && !"".equals(ConfigManager.getLogsDir())) { //$NON-NLS-1$
			this.setPath_filelog(ConfigManager.getLogsDir());
		}

		if(rollingDate != null && !"".equals(rollingDate)){ //$NON-NLS-1$
			FireLogger.setRollingDate(rollingDate);
		}else if(ConfigManager.getRollingDate() != null && !"".equals(ConfigManager.getRollingDate())) { //$NON-NLS-1$
			FireLogger.setRollingDate(ConfigManager.getRollingDate());
		}


		if (this.getPath_filelog() != null && FireLogger.getRollingDate() != null)  {

		    if(this.getLogger() == null){
		        initLogger(logParticleName); //inicializamos el logger si es la primera vez y esta a nulo.

		        //Se crea un hilo que se ejecuta periodicamente segun el parametro indicado en el fichero de configuracion (DIARIA, HORARIA, POR_MINUTOS)
		        final ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);

		        final Runnable renameLoggerFile = new Runnable(){

		            @Override
		            public void run() {
		            	getFh().flush();
		            	getFh().close();
			          getLogger().removeHandler(getFh());
			            try {
							 	setFileHandlerFormater();
							 	getLogger().addHandler(getFh());
							 	getLogger().info("######## INICIO LOG ########"); //$NON-NLS-1$
			                } catch (final SecurityException e) {
			                	FireLogger.this._LOGGER.severe("Error en hilo del Log :".concat(e.getMessage())); //$NON-NLS-1$
			                } catch (final IOException e) {
			                	FireLogger.this._LOGGER.severe("Error en hilo del Log :".concat(e.getMessage())); //$NON-NLS-1$
			                }
			            }

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
	final FileHandler createFilehandler(final String dateForName) throws SecurityException, IOException{

	    final File fileFolder = new File(this.getPath_filelog());

	    if(!fileFolder.exists()){
	        fileFolder.mkdirs();
	    }

	    final String dateFileName = this.getPath_filelog() + File.separator + "FIRe" + getLogName() + "_" + dateForName + ".log"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    final boolean appendToFile = true;
	    return new FileHandler(dateFileName, appendToFile);
	}

	/**
	 * Se inicializa el logger com&uacute;n a todos los packages
	 */
	private void initLogger(final String nameLogger){ //se cambia static por private

		final Logger mylogger = Logger.getLogger(nameLogger);
		this.setLogger(mylogger);

	    try {
	    		setFileHandlerFormater();
	    		getLogger().addHandler(this.getFh());
	    		//this.logger.info("Log de Fire"+ nameLogger + " Inicializado"); //$NON-NLS-1$ //$NON-NLS-2$

	 	    } catch (final SecurityException e) {
	 	    	FireLogger.this._LOGGER.warning("Problema al inicializar del logger... " + e.getMessage()); //$NON-NLS-1$
	 	    } catch (final IOException e) {
	 	    	FireLogger.this._LOGGER.warning("Problema al inicializar del  logger... " + e.getMessage()); //$NON-NLS-1$
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

		if(getRollingDate().equals(DIARIA)) {
			final SimpleDateFormat format = new SimpleDateFormat(PATTR_DIARIA);
			return format.format(new java.sql.Date(System.currentTimeMillis()));

		}
		else if(getRollingDate().equals(HORARIA)) {
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

		if(getRollingDate().equals(DIARIA)) {
			c.set(Calendar.HOUR_OF_DAY, 0);
		    c.set(Calendar.MINUTE, 0);
		    c.set(Calendar.SECOND, 0);
		    c.set(Calendar.MILLISECOND, 0);
		    final long passed = now - c.getTimeInMillis();
		    final long secondsPassedToday = passed / 1000;
			return SEG_DIA - secondsPassedToday;
		}
		else if(getRollingDate().equals(HORARIA)) {
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
		if(getRollingDate().equals(DIARIA)) {
			return SEG_DIA;
		}
		else if(getRollingDate().equals(HORARIA)) {
			return SEG_HORA;
		}
		else {
			return SEG_MIN;
		}
	}

	 /**
	  * Se inicializa el FileHandler con el formato de las lineas a %1$tF %1$tT;%2$s%n
	  * @throws SecurityException
	  * @throws IOException
	  */
	 void setFileHandlerFormater() throws SecurityException, IOException {

		this.setFh(createFilehandler(getLogFileName()));

         /*	# %1  date: un objeto Date que representa la hora del evento del registro.
				# %2  fuente - una cadena que representa al que llama, si est&aacute; disponible; de lo contrario, el nombre del logger.
				# %3  registrador - el nombre del logger.
				# %4  nivel - el nivel de registro.
				# %5  mensaje: el mensaje de registro formateado devuelto por el m&eacute;todo Formatter.formatMessage (LogRecord). Utiliza el formato java.text y no usa el argumento de formato java.util.Formatter.
				# %7  throw - una cadena que representa el throwable asociado con el registro de registro y su backtrace que comienza con un car&aacute;cter de nueva l&iacute;nea, si lo hay; de lo contrario, una cadena vac&iacute;a.
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
		this.getFh().setFormatter(new SimpleFormatter() {

	   private static final String format = "%1$tF %1$tT;%2$s%n"; //$NON-NLS-1$

	          @Override
	          public synchronized String format(final LogRecord lr) {
	              return String.format(format,
	                      new Date(lr.getMillis()),
	                      lr.getMessage()
	              );
	          }
	      });
	 }



	public final Logger getLogger() {
		return this.logger;
	}

	private final void setLogger(final Logger logger) {
		this.logger = logger;
	}

	protected final  FileHandler getFh() {
		return 	this.fh;
	}

	protected final  void setFh(final FileHandler fh) {
		this.fh = fh;
	}

	private final  String getLogName() {
		return this.logName;
	}

	private final  void setLogName(final String logName) {
		this.logName = logName;
	}

	private final String getPath_filelog() {
		return this.path_filelog;
	}

	private final void setPath_filelog(final String path_filelog) {
		this.path_filelog = path_filelog;
	}

	private static final String getRollingDate() {
		return rollingDate;
	}

	private static final void setRollingDate(final String rollingDate) {
		FireLogger.rollingDate = rollingDate;
	}

}

