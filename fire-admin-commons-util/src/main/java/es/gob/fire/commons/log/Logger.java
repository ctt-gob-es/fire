/*******************************************************************************
 * Copyright (C) 2021, Gobierno de España
 * This program is licensed and may be used, modified and redistributed under the terms
 * of the European Public License (EUPL), either version 1.1 or (at your
 * option) any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/
package es.gob.fire.commons.log;

import org.apache.logging.log4j.LogManager;

/**
 * <p>Class that encapsulates the logging methods.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 02/02/2022.
 */
public class Logger {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static org.apache.logging.log4j.Logger LOGGER;
	
	/**
	 * Constructor for instancing the configured Logger class using an appender name
	 * @param name String that represents an appender name configured.
	 */
	private Logger(String name) {
		
		LOGGER = LogManager.getLogger(name);
	}
	
	/**
	 * Constructor for instancing the configured Logger class using a class object.
	 * @param clazz Class object of the Logger.
	 */
	private Logger(Class<?> clazz) {
		
		LOGGER = LogManager.getLogger(clazz);
	}
	
	/**
	 * Method that encapsulates the call to the logger retrieval according to the chosen logger system implementation.
	 * @param name The name of the configured logger.
	 * @return a new {@link Logger}
	 */
	public static Logger getLogger(String name) {
				
		return new Logger(name);
	}
	
	/**
	 * Method that encapsulates the call to the logger retrieval according to the chosen logger system implementation.
	 * @param clazz The .class of the class whose logger we want to obtain.
	 * @return a new {@link Logger}
	 */
	public static Logger getLogger(Class<?> clazz) {
		
		return new Logger(clazz);
	}
	
	/**
	 * Method that encapsulates the call to the info logger method.
	 * @param message String that represents the message to log with the INFO level.
	 */
	public void info(final Object message) {
		
		LOGGER.info(message);
    }
   
	/**
	 * Method that encapsulates the call to the info logger method, including the stack trace.
	 * @param message String that represents the message to log with the INFO level.
	 * @param the exception to log, including its stack trace.
	 */
    public void info(final Object message, final Throwable t) {
    	
    	LOGGER.info(message, t);
    }
	
    /**
	 * Method that encapsulates the call to the info logger method.
	 * @param message String that represents the message to log with the DEBUG level.
	 */
    public void debug(final Object message) {
    	
    	LOGGER.debug(message);
    }
   
    /**
	 * Method that encapsulates the call to the info logger method, including the stack trace.
	 * @param message String that represents the message to log with the DEBUG level.
	 * @param the exception to log, including its stack trace.
	 */
    public void debug(final Object message, final Throwable t) {
    	
    	LOGGER.debug(message, t);
    }
    
    /**
	 * Method that encapsulates the call to the info logger method.
	 * @param message String that represents the message to log with the WARN level.
	 */
    public void warn(final Object message) {
    	
    	LOGGER.warn(message);
    }
   
    /**
   	 * Method that encapsulates the call to the info logger method, including the stack trace.
   	 * @param message String that represents the message to log with the WARN level.
   	 * @param the exception to log, including its stack trace.
   	 */
    public void warn(final Object message, final Throwable t) {
    	
    	LOGGER.warn(message, t);
    }
   
    /**
	 * Method that encapsulates the call to the info logger method.
	 * @param message String that represents the message to log with the ERROR level.
	 */
    public void error(final Object message) {
    	
    	LOGGER.error(message);
    }
    
    /**
   	 * Method that encapsulates the call to the info logger method, including the stack trace.
   	 * @param message String that represents the message to log with the ERROR level.
   	 * @param the exception to log, including its stack trace.
   	 */
    public void error(final Object message, final Throwable t) {
    	
    	LOGGER.error(message, t);
    }
    
    /**
   	 * Method that encapsulates the call to the info logger method.
   	 * @param message String that represents the message to log with the FATAL level.
   	 */
    public void fatal(final Object message) {
    	
    	LOGGER.fatal(message);
    }
    
    /**
   	 * Method that encapsulates the call to the info logger method, including the stack trace.
   	 * @param message String that represents the message to log with the FATAL level.
   	 * @param the exception to log, including its stack trace.
   	 */
    public void fatal(final Object message, final Throwable t) {
    	
    	LOGGER.fatal(message, t);
    } 

}
