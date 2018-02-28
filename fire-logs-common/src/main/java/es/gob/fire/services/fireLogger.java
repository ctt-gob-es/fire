package es.gob.fire.services;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class fireLogger {

	private static Logger logger;
	private static FileHandler fh;
	
	private static final String DIARIA="DIARIA";
	private static final String HORARIA="HORARIA";
//	private static final String POR_MINUTOS="POR_MINUTOS";
	
	private static final String PATTR_DIARIA="yyyy-MM-dd";
	private static final String PATTR_HORARIA="yyyy-MM-dd-HH";
	private static final String PATTR_POR_MINUTOS="yyyy-MM-dd-HH-mm";
	
	private static final long SEG_DIA=24L*60L*60L;
	private static final long SEG_HORA=60L*60L;
	private static final long SEG_MIN=60L;
	private static TimeUnit timeUnit = TimeUnit.SECONDS;

	public static void installLogger(String [] class_names) {
		
		final Logger LOGGER = Logger.getLogger(fireLogger.class.getName());
		
		try {
	    	ConfigManager.checkInitialized();
		}
    	catch (final Exception e) {
    		LOGGER.severe("Error al cargar la configuracion del log: " + e); //$NON-NLS-1$
    		return;
    	}
		
		if(ConfigManager.getLogsDir()!=null && !"".equals(ConfigManager.getLogsDir()) 
			&& ConfigManager.getRollingDate()!=null && !"".equals(ConfigManager.getRollingDate())) {
				
		    if(logger == null){
		        initLogger(class_names);
	
		        ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1); 
		        // Create a task for one-shot execution using schedule()
		        Runnable renameLoggerFile = new Runnable(){
	//	              int countRuns = 0;  // For testing only
		            @Override
		            public void run() {
		                fh.flush();
		                fh.close();
		                try {
		                    fh = createFilehandler(getLogFileName());
	//	                    fh = createFilehandler(new java.sql.Date(System.currentTimeMillis()).toString()+"_"+countRuns++); // for testing	
		                    SimpleFormatter formatter = new SimpleFormatter();
		                    fh.setFormatter(formatter);  
		                    logger.addHandler(fh);
		                    logger.warning("Runnable executed, new FileHandler is in use!");
		                } catch (SecurityException e) {
		                    // TODO Auto-generated catch block
		                    e.printStackTrace();
		                } catch (IOException e) {
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
	private static FileHandler createFilehandler(String dateForName) throws SecurityException, IOException{
	    String folder = ConfigManager.getLogsDir();
	    File fileFolder = new File(folder);
	    // Create folder log if it doesn't exist
	    if(!fileFolder.exists()){
	        fileFolder.mkdirs();
	    }

	    dateForName = folder + File.separator + dateForName + ".log";

	    boolean appendToFile = true;

	    return new FileHandler(dateForName, appendToFile);  
	}
	
	private static void initLogger(String [] class_names){
		
		
	    String folder = ConfigManager.getLogsDir();
	    File fileFolder = new File(folder);
	    // Create folder "log" if it doesn't exist
	    if(!fileFolder.exists()){
	        fileFolder.mkdirs();
	    }

	    for(String class_name:class_names) {
	    	 logger = Logger.getLogger(class_name);  
	    	 
	    	 try {  
	 	        // This block configure the logger with handler and formatter
	 	        boolean appendToFile = true;
	 	        fh = new FileHandler(folder + File.separator + getLogFileName()+ ".log", appendToFile);  

	 	        logger.addHandler(fh);
	 	        SimpleFormatter formatter = new SimpleFormatter();  
	 	        fh.setFormatter(formatter);  

	 	        // the following statement is used to log any messages
	 	        logger.info("Logs de Fire Inicializado...");  

	 	    } catch (SecurityException e) {  
	 	        logger.warning("Problema al inicializar el logger... " + e.getMessage());
	 	    } catch (IOException e) {  
	 	        logger.warning("Problema al inicializar el  logger... " + e.getMessage());
	 	    }  
	    	 
	    }
	     
	}

	private static String getLogFileName() {
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
		
		Calendar c = Calendar.getInstance();
	    long now = c.getTimeInMillis();
	        	       	        	       	       			        
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
