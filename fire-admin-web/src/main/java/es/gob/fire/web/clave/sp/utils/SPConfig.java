package es.gob.fire.web.clave.sp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.fire.commons.utils.UtilsServer;

/**
 * Utility class for Service Provider configurations.
 */
public class SPConfig {

	/**
     * Constructor for SPUtil. Private to prevent instantiation.
     */
    SPConfig() {}

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SPConfig.class);

    /**
     * Retrieves the configuration file path.
     *
     * @return The configuration file path or {@code null} if it's not defined.
     */
    public static String getConfigFilePath() {
    	String configDir = UtilsServer.getServerConfigDir();
    	
    	// Si no existe la ruta o el directorio de Clave en ella, se intenta buscar en la ruta del ejecutable
    	if (configDir == null || !new File(configDir, UtilsServer.CLAVE_DIRECTORY).isDirectory()) {
			try {
				String alternativeDir = SPConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				if (new File(alternativeDir, UtilsServer.CLAVE_DIRECTORY).isDirectory()) {
					configDir = alternativeDir;
				}
			} catch (Exception e) {
				LOG.warn("No se pudo buscar la configuracion de Clave en el directorio del proyecto", e); //$NON-NLS-1$
			}
    	}

    	return UtilsServer.createAbsolutePath(configDir, UtilsServer.CLAVE_DIRECTORY) + File.separator;
    }

    /**
     * Loads configurations from a specified file.
     *
     * @param fileName The name of the configuration file.
     * @return A Properties object containing the configurations.
     * @throws IOException If an error occurs during file reading.
     */
    private static Properties loadConfigs(final String fileName) throws IOException {

    	Properties properties = new Properties();

    	// Tratamos de cargar el fichero de configuracion del directorio de configuracion
    	final String configPath = SPConfig.getConfigFilePath();
    	if (configPath != null) {
    		final File f = new File(configPath, fileName);
    		if (f.isFile()) {
    			try (InputStream is = new FileInputStream(f);
    				 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
    				properties.load(isr);
    			}
    		}
    	}

        return properties;
    }

    /**
     * Loads Service Provider configurations from the default properties file.
     *
     * @return A Properties object containing the Service Provider configurations.
     */
    public static Properties loadSPConfigs() {
        Properties result = null;
        try {
            result = SPConfig.loadConfigs(Constants.CLAVE_CONFIG_PROPERTIES);
        } catch (final IOException e) {
            LOG.error("No se ha podido cargar el fichero de configuracion de Cl@ve", e); //$NON-NLS-1$
        }
        return result;
    }
}
