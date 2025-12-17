package es.gob.fire.web.clave.sp.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
    	final String configDir = UtilsServer.getServerConfigDir();
    	if (configDir == null) {
    		return null;
    	}

    	return UtilsServer.createAbsolutePath(UtilsServer.getServerConfigDir(), UtilsServer.CLAVE_DIRECTORY) + File.separator;
    }

    /**
     * Loads configurations from a specified file.
     *
     * @param fileName The name of the configuration file.
     * @return A Properties object containing the configurations.
     * @throws IOException If an error occurs during file reading.
     */
    private static Properties loadConfigs(final String fileName) throws IOException {

    	Properties properties = null;

    	// Tratamos de cargar el fichero de configuracion del directorio de configuracion
    	final String configPath = SPConfig.getConfigFilePath();
    	if (configPath != null) {
    		final File f = new File(configPath, fileName);
    		if (f.isFile()) {
    			try (FileReader fileReader = new FileReader(f, StandardCharsets.UTF_8)) {
    				properties = new Properties();
    				properties.load(fileReader);
    			}
    		}
    	}

    	// Si no se declaro el directorio o no se encontro el fichero, intentamos cargarlo
    	// desde el classpath
    	if (properties == null) {
    		properties = new Properties();
    		try (final InputStream is = SPConfig.class.getResourceAsStream('/' + fileName);) {
    			if (is != null) {
    				try (final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
    					properties.load(reader);
    				}
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
