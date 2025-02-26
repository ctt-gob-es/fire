package es.gob.fire.web.clave.sp.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
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
     * @return The configuration file path as a String.
     */
    public static String getConfigFilePath() {
        // Commented out code that retrieves the path from environment or system properties.
        /*String envLocation = System.getenv().get(Constants.SP_CONFIG_REPOSITORY);
        String configLocation = System.getProperty(Constants.SP_CONFIG_REPOSITORY, envLocation);
        return configLocation;*/
    	return UtilsServer.createAbsolutePath(UtilsServer.getServerConfigDir(), UtilsServer.CLAVE_DIRECTORY) + File.separator;
    }
    
    /**
     * Loads configurations from a specified file.
     *
     * @param fileName The name of the configuration file.
     * @return A Properties object containing the configurations.
     * @throws IOException If an error occurs during file reading.
     */
    private static Properties loadConfigs(String fileName) throws IOException {
        Properties properties = new Properties();
        FileReader fileReader = null;
        
        try {
            File f = new File(SPConfig.getConfigFilePath() + fileName);
            fileReader = new FileReader(f);
            properties.load(fileReader);
        } finally {
            IOUtils.closeQuietly(fileReader);
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
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
}
