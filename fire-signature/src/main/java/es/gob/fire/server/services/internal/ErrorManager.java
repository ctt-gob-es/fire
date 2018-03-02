package es.gob.fire.server.services.internal;


import java.util.Properties;
import java.util.logging.Logger;
import es.gob.fire.signature.ConfigFileLoader;

public class ErrorManager {

	private static final String ERR_FILE = "errors_es_ES.messages"; //$NON-NLS-1$
	private static final Logger LOGGER = Logger.getLogger(ErrorManager.class.getName());
	private static Properties error=null;
	
	
	
	/**
	 * Carga el fichero de configuraci&oacute;n del m&oacute;dulo.
	 * @throws ConfigFilesException Cuando no se encuentra o no se puede cargar el fichero de configuraci&oacute;n.
	 */
	static {

		if (error == null) {
			try {
				error = ConfigFileLoader.loadConfigFile(ERR_FILE);
			}
			catch (final Exception e) {
				LOGGER.severe("No se pudo cargar el fichero de configuracion " + ERR_FILE); //$NON-NLS-1$
			}
		}
	}
	
	
	public static final String getMessage(final String code) {
		return error.getProperty(code);
	}
	

	
}
