package es.gob.fire.web.clave.sp.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private static final String CLAVE_DIRECTORY = "clave";
    
    /**
     * Retrieves the configuration file path from the resources directory.
     *
     * @return The configuration file path as a String.
     * @throws IllegalStateException If the resource directory is not found.
     */
    public static String getConfigFilePath() {
        URL resource = SPConfig.class.getClassLoader().getResource(CLAVE_DIRECTORY);
        if (resource == null) {
            throw new IllegalStateException("Resource directory 'clave' not found in the classpath.");
        }
        return resource.getPath();
    }
    
    /**
     * Loads configurations from a specified file in the resources/clave directory.
     *
     * @param fileName The name of the configuration file.
     * @return A Properties object containing the configurations.
     * @throws IOException If an error occurs during file reading.
     */
    private static Properties loadConfigs(String fileName) throws IOException {
        Properties properties = new Properties();
        
        // Usa el ClassLoader para cargar el archivo del classpath
        try (InputStream inputStream = SPConfig.class.getClassLoader().getResourceAsStream(CLAVE_DIRECTORY + File.separator + fileName)) {
            if (inputStream == null) {
                throw new IOException("Configuration file not found: clave/" + fileName);
            }
            properties.load(inputStream);
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
        } catch (IOException e) {
            LOG.error("Failed to load Service Provider configurations from properties file.", e);
        }
        return result;
    }
}
