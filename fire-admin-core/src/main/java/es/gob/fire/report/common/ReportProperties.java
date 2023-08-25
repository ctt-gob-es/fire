package es.gob.fire.report.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.FileUtilsDirectory;
import es.gob.fire.commons.utils.UtilsServer;

public class ReportProperties {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ReportProperties.class);
	
	/**
	 * Constant attribute that represents the name of the properties file.
	 */
	private static final String PROPS_FILENAME = "report.properties";
	
	/**
	 * Attribute that represents the file of properties associated with the class.
	 */
	private static Properties properties;
	
	/**
	 * Constructor method for the class ReportProperties.java.
	 */
	private ReportProperties() {
	}
	
	/**
	 * Gets all properties from original file.
	 * @return all properties
	 */
	public static Properties getProperties() {
		if (properties == null) {
			reloadReportProperties();
		}
		return properties;
	}
	
	/**
	 * Method that load/reload the static report properties.
	 * @return <code>true</code> if the properties file has been loaded,
	 * otherwise <code>false</code>.
	 */
	private static boolean reloadReportProperties() {
		boolean result = false;

		synchronized (ReportProperties.class) {
			if (properties == null) {
				properties = new Properties();
				FileInputStream fileInput = null;
				try {
					LOGGER.info("Se crea el FileInputStream de configuracion");
					fileInput = new FileInputStream(FileUtilsDirectory.createAbsolutePath(UtilsServer.getServerConfigDir(), PROPS_FILENAME));
					properties.load(fileInput);
					LOGGER.info("Se cargan las propiedades en la variables estatica");
					result = true;
				} catch (IOException e) {
					LOGGER.error("Ha ocurrido un error", e);
				} finally {
					try {
						fileInput.close();
					} catch (IOException e) {
						LOGGER.error("Ha ocurrido un error", e);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns the value of property given.
	 * @param propertyName name of @Firma property.
	 * @return the value of property given.
	 */
	public static String getProperty(String propertyName) {
		return (String) getProperties().get(propertyName);
	}
	
	/**
	 * Obtains a collection of static properties which key name start with the prefix given.
	 * @param prefix word placed in the beginning of the key name of property.
	 * @return a collection of static properties.
	 */
	public static Properties getProperties(String prefix) {
		Properties result = new Properties();
		if (prefix != null) {
			for (Object key: getProperties().keySet()) {
				if (key != null && key.toString().startsWith(prefix)) {
					result.put(key, getProperties().get(key));
				}
			}
		}
		return result;
	}
	
	/**
	 * Method that obtains the value of a constant property.
	 * @param propertyParam Parameter that represents the property's name.
	 * @param key Parameter that represents the property's key.
	 * @return the value of the constant property.
	 */
	public static String getConstantValue(String propertyParam, String key) {
		if (properties.containsKey(propertyParam)) {
			Properties p = (Properties) properties.get(propertyParam);
			return p.getProperty(key);
		} else {
			return null;
		}
	}

}
