
/**
 * <b>Description:</b><p>Utility class for server properties and others.</p>
 * <b>Date:</b><p>14 oct. 2019.</p>
 * @author Consejeia de Turismo, Regeneracion, Justicia y Administracion Local de la Junta de Andalucia.
 * @version 1.0, 14 oct. 2019.
 */
package es.gob.fire.commons.utils;


/**
 * <p>Utility class for server properties and others.</p>
 */
public final class UtilsServer {

	/**
	 * Constant attribute that represents the property key fire.config.path.
	 */
	public static final String PROP_SERVER_CONFIG_DIR = "fire.config.path";

	/**
	 * Constructor method for the class UtilsServer.java.
	 */
	private UtilsServer() {

	}

	/**
	 * Method that returns the value of the system property fire.config.path.
	 * @return Value of the system property fire.config.path. Null if not exist.
	 */
	public static String getServerConfigDir() {
		return System.getProperty(PROP_SERVER_CONFIG_DIR);
	}

	/**
	 * Method that returns the value of the system property fire.config.path.
	 * @return Value of the system property fire.config.path. Null if not exist.
	 */
	public static String getMessagesDir() {
		return FileUtilsDirectory.createAbsolutePath(getServerConfigDir(), Constants.MESSAGES);
	}

}
