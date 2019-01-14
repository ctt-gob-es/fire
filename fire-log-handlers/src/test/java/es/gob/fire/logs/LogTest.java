package es.gob.fire.logs;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import es.gob.fire.logs.handlers.PeriodicRotatingFileHandler;


public class LogTest {

	static {

//		//try (InputStream is = LogTest.class.getResourceAsStream("logging.properties")) {
//		try (InputStream is = new FileInputStream("C:/Users/carlos.gamuci/Documents/FIRe/Repositorios_GitHub/fire/fire-log-handlers/src/main/resources/logging.properties")) {
////			final Properties config = new Properties();
////			config.load(is);
////			System.out.println("Loggers: " + config.getProperty("loggers"));
//
//			final LogManager logManager = LogManager.getLogManager();
//			logManager.readConfiguration(is);
//
//			final Enumeration<String> names = logManager.getLoggerNames();
//			while (names.hasMoreElements()) {
//				System.out.println(names.nextElement());
//			}
//
//
//
//			LOGGER = logManager.getLogger(LogTest.class.getName());
//		}
//		catch (final Exception e) {
//			Logger.getLogger(LogTest.class.getName()).warning("No se ha podido cargar el fichero de configuracion del log: " + e);
//		}

		PeriodicRotatingFileHandler h;

		try {
			h = new PeriodicRotatingFileHandler("C:/Users/carlos.gamuci/Desktop/fire_signature.log", true);
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			h = new PeriodicRotatingFileHandler();
		}
		h.setAutoFlush(true);
		h.setFormatter(new SimpleFormatter());
		h.setLevel(Level.INFO);
		h.setEnabled(true);
		h.setSuffix(".yyyy-MM-dd_hh-mm");

		final Logger LOGGER = Logger.getLogger("es.gob.fire");
		LOGGER.addHandler(h);
	}

	public static void main(final String[] args) throws Exception {

		while (true) {
			Logger.getLogger(LogTest.class.getName()).info("Hola Mundo!! 1");
			Logger.getLogger(LogTest.class.getName()).warning("Hola Mundo!! 2");
			Logger.getLogger(LogTest.class.getName()).severe("Hola Mundo!! 3");

			Thread.sleep(5000);
		}

	}
}
