
/** 
 * <b>File:</b><p>es.gob.fire.web.util.UtilsServer.java.</p>
 * <b>Description:</b><p>Utility class for server properties and others.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * <b>Date:</b><p>14 oct. 2019.</p>
 * @author Consejería de Turismo, Regeneración, Justicia y Administración Local de la Junta de Andalucía.
 * @version 1.0, 14 oct. 2019.
 */
package es.gob.fire.web.util;

import es.gob.fire.core.constant.Constants;

/** 
 * <p>Utility class for server properties and others.</p>
 * <b>Project:</b><p>Servicios Integrales de Firma Electrónica para el Ámbito Judicial.</p>
 * @version 1.0, 14 oct. 2019.
 */
public final class UtilsServer {

	/**
	 * Constant attribute that represents the property key server.config.dir. 
	 */
	public static final String PROP_SERVER_CONFIG_DIR = "server.config.dir";
	
	/**
	 * Constructor method for the class UtilsServer.java. 
	 */
	private UtilsServer() {
		
	}

	/**
	 * Method that returns the value of the system property server.config.dir.
	 * @return Value of the system property server.config.dir. Null if not exist.
	 */
	public static String getServerConfigDir() {
		return System.getProperty(PROP_SERVER_CONFIG_DIR);
	}
	
	/**
	 * Method that returns the value of the system property server.config.dir.
	 * @return Value of the system property server.config.dir. Null if not exist.
	 */
	public static String getMessagesDir() {
		return FileUtilsDirectory.createAbsolutePath(getServerConfigDir(), Constants.MESSAGES);
	}
	
}
